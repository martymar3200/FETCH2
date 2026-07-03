import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session
from app.models.buildings import Building

def test_reporting_endpoints(client: TestClient, session: Session, test_database):
    # 1. Test worker-efficiency paginated endpoint
    resp = client.get("/reporting/worker-efficiency/")
    assert resp.status_code == 200
    
    # 2. Test worker-efficiency download CSV endpoint
    resp = client.get("/reporting/worker-efficiency/download")
    assert resp.status_code == 200
    assert "user_name" in resp.text
    assert "job_type" in resp.text

    # 3. Test hot-zones paginated endpoint
    resp = client.get("/reporting/hot-zones/")
    assert resp.status_code == 200

    # 4. Test hot-zones download CSV endpoint
    resp = client.get("/reporting/hot-zones/download")
    assert resp.status_code == 200
    assert "building_name" in resp.text
    assert "aisle_number" in resp.text

    # Get a building ID for building-based reporting endpoints
    b = session.query(Building).first()
    building_id = b.id if b else 1

    # 5. Test Aisle Item Counts sorted (AisleItemsCountSorter)
    resp = client.get(f"/reporting/aisles/items_count/?building_id={building_id}&sort_by=aisle_number&sort_order=asc")
    assert resp.status_code == 200

    # 6. Test Non-Tray Item Counts sorted (NonTrayItemCountSorter)
    resp = client.get(f"/reporting/non_tray_items/count/?building_id={building_id}&sort_by=size_class_short_name&sort_order=asc")
    assert resp.status_code == 200

    # 7. Test Tray Item Counts sorted (TrayItemCountSorter)
    resp = client.get(f"/reporting/tray_items/count/?building_id={building_id}&sort_by=size_class_short_name&sort_order=asc")
    assert resp.status_code == 200

    # 8. Test Verification Changes sorted (VerificationChangeSorter)
    resp = client.get("/reporting/verification-changes/summary/?sort_by=completed_dt&sort_order=asc")
    assert resp.status_code == 200
