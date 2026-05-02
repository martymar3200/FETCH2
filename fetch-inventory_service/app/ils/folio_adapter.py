import datetime
import httpx
from typing import List, Optional

from app.logger import inventory_logger
from app.ils.interfaces import (
    BaseILSAdapter,
    ILSCheckInResponse,
    ILSItemMetadata,
    ILSCheckInStatus,
    ILSRequestItem
)

class FolioILSAdapter(BaseILSAdapter):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.access_token = None
        self.token_expiry = None

    def _get_auth_headers(self) -> dict:
        """
        Retrieves a valid OAuth2 Bearer token from the Keycloak/Kong Eureka gateway.
        Implements Client Credentials Grant.
        """
        now = datetime.datetime.now(datetime.timezone.utc)
        
        # Check if we have a valid token that isn't expiring in the next 30 seconds
        if self.access_token and self.token_expiry and self.token_expiry > now + datetime.timedelta(seconds=30):
            return {
                "Authorization": f"Bearer {self.access_token}",
                "x-okapi-tenant": self.tenant_id,
                "Content-Type": "application/json",
                "Accept": "application/json"
            }

        # Otherwise, we need to request a new one
        if not self.auth_token_url:
             raise ValueError("Folio Eureka adapter requires auth_token_url to negotiate OAuth2.")
             
        try:
            # Standard OAuth 2.0 Client Credentials payload
            payload = {
                "grant_type": "client_credentials",
                "client_id": self.auth_client_id,
                "client_secret": self.auth_client_secret
            }
            
            resp = httpx.post(
                self.auth_token_url,
                data=payload,  # Usually forms/url-encoded for OAuth
                timeout=10
            )
            resp.raise_for_status()
            token_data = resp.json()
            
            self.access_token = token_data.get("access_token")
            expires_in = token_data.get("expires_in", 600)  # Default 10 mins if omitted
            self.token_expiry = now + datetime.timedelta(seconds=expires_in)
            
            return {
                "Authorization": f"Bearer {self.access_token}",
                "x-okapi-tenant": self.tenant_id,
                "Content-Type": "application/json",
                "Accept": "application/json"
            }
        except Exception as e:
            inventory_logger.error(f"Folio OAuth Negotiation Failed for Tenant {self.tenant_id}: {str(e)}")
            raise e

    def validate_item(self, barcode_value: str) -> bool:
        """
        Checks if an item physically exists in FOLIO inventory.
        """
        try:
            headers = self._get_auth_headers()
            url = f"{self.base_url.rstrip('/')}/inventory/items"
            params = {"query": f'(barcode=="{barcode_value}")'}
            
            resp = httpx.get(url, headers=headers, params=params, timeout=10)
            if resp.status_code == 200:
                data = resp.json()
                return data.get("totalRecords", 0) > 0
            return False
            
        except Exception as e:
            inventory_logger.error(f"FOLIO Adapter validate_item error: {str(e)}")
            return False

    def check_in_item(self, barcode_value: str) -> ILSCheckInResponse:
        """
        Performs a check-in action against FOLIO circulation API.
        We attempt to read the raw status from the response.
        """
        try:
            headers = self._get_auth_headers()
            url = f"{self.base_url.rstrip('/')}/circulation/check-in-by-barcode"
            
            # FOLIO check-in requires a service point.
            payload = {
                "itemBarcode": barcode_value,
                "servicePointId": self.ils_service_point_id or "00000000-0000-0000-0000-000000000000",
                "checkInDate": datetime.datetime.now(datetime.timezone.utc).isoformat()
            }
            
            resp = httpx.post(url, headers=headers, json=payload, timeout=10)
            
            if resp.status_code in [200, 201]:
                data = resp.json()
                # FOLIO returns item status inside item object of the response
                item = data.get("item", {})
                status_obj = item.get("status", {})
                raw_status = status_obj.get("name", "Available")
                
                return ILSCheckInResponse(
                    success=True,
                    status=ILSCheckInStatus.AVAILABLE if raw_status == "Available" else ILSCheckInStatus.UNKNOWN,
                    raw_status_string=raw_status,
                )
            else:
                error_msg = resp.text
                try:
                    err_json = resp.json()
                    if "errors" in err_json and len(err_json["errors"]) > 0:
                        error_msg = err_json["errors"][0].get("message", error_msg)
                except:
                    pass
                    
                return ILSCheckInResponse(
                    success=False,
                    status=ILSCheckInStatus.SYNC_ERROR,
                    raw_status_string="",
                    error_message=f"FOLIO Check-in failed: {error_msg}"
                )
                
        except Exception as e:
            inventory_logger.error(f"FOLIO Adapter check_in_item error: {str(e)}")
            return ILSCheckInResponse(
                success=False,
                status=ILSCheckInStatus.SYNC_ERROR,
                raw_status_string="",
                error_message=str(e)
            )

    def fetch_item_metadata(self, barcode_value: str) -> Optional[ILSItemMetadata]:
        """
        Dynamically fetches live data from FOLIO Inventory without saving it to FETCH2.
        """
        try:
            headers = self._get_auth_headers()
            url = f"{self.base_url.rstrip('/')}/inventory/items"
            params = {"query": f'(barcode=="{barcode_value}")'}
            
            resp = httpx.get(url, headers=headers, params=params, timeout=10)
            resp.raise_for_status()
            
            data = resp.json()
            if data.get("totalRecords", 0) > 0:
                item = data.get("items", [])[0]
                
                # FOLIO item structures can be deep, doing a best effort extraction
                title = item.get("title", 'Unknown Title')
                call_number = item.get("itemLevelCallNumber") or item.get("callNumber")
                material_type = item.get("materialType", {}).get("name")
                
                # Author usually comes from instance relationship or contributors
                author = None
                if "contributorNames" in item and item["contributorNames"]:
                    author = item["contributorNames"][0].get("name")
                    
                return ILSItemMetadata(
                    title=title,
                    author=author,
                    call_number=call_number,
                    material_type=material_type,
                    is_valid_location=True
                )
            return None
        except Exception as e:
            inventory_logger.error(f"FOLIO Adapter fetch_item_metadata error: {str(e)}")
            return None

    def fetch_pending_requests(self) -> List[ILSRequestItem]:
        """
        Polls FOLIO for any open, unfulfilled page requests that need fulfillment.
        """
        try:
            headers = self._get_auth_headers()
            url = f"{self.base_url.rstrip('/')}/circulation/requests"
            # Poll for Requests that are open pages targeted at our service point
            query = '(status=="Open - Not yet filled" and requestType=="Page")'
            if self.ils_service_point_id:
                query += f' and pickupServicePointId=="{self.ils_service_point_id}"'
                
            params = {
                "query": query,
                "limit": 50
            }
            
            resp = httpx.get(url, headers=headers, params=params, timeout=15)
            resp.raise_for_status()
            
            data = resp.json()
            requests_list = []
            
            for req in data.get("requests", []):
                item_obj = req.get("item", {})
                requests_list.append(ILSRequestItem(
                    request_id=req.get("id"),
                    item_barcode=item_obj.get("barcode", ""),
                    patron_id=req.get("requesterId", "Unknown"),
                    destination=req.get("pickupServicePointId", "Default Location")
                ))
                
            return requests_list
        except Exception as e:
            inventory_logger.error(f"FOLIO Adapter fetch_pending_requests error: {str(e)}")
            return []
