import json

payload = json.load(open('tests/fixtures/payloads/create_data_sampler.json'))

model_map = {
    "buildings": "Building",
    "modules": "Module",
    "aisle_numbers": "AisleNumber",
    "aisles": "Aisle",
    "side_orientations": "SideOrientation",
    "sides": "Side",
    "shelf_numbers": "ShelfNumber",
    "shelf_position_numbers": "ShelfPositionNumber",
    "container_types": "ContainerType",
    "barcode_types": "BarcodeType",
    "barcodes": "Barcode",
    "ladder_numbers": "LadderNumber",
    "ladders": "Ladder",
    "owner_tiers": "OwnerTier",
    "owners": "Owner",
    "shelving_jobs": "ShelvingJob",
    "accession_jobs": "AccessionJob",
    "verification_jobs": "VerificationJob",
    "subcollections": "Subcollection",
    "items": "Item",
    "size_class": "SizeClass",
    "shelf_types": "ShelfType",
    "media_types": "MediaType",
    "trays": "Tray",
    "users": "User",
    "permissions": "Permission",
    "groups": "Group",
    "request_types": "RequestType",
    "requests": "Request",
    "pick_lists": "PickList",
    "refile_jobs": "RefileJob",
    "shelves": "Shelf",
    "shelf_positions": "ShelfPosition",
    "conveyance_bins": "ConveyanceBin",
}

print("        from app.models.buildings import Building")
print("        from app.models.modules import Module")
print("        from app.models.aisles import Aisle, AisleNumber")
print("        from app.models.sides import Side, SideOrientation")
print("        from app.models.ladders import Ladder, LadderNumber")
print("        from app.models.shelves import Shelf, ShelfNumber")
print("        from app.models.shelf_types import ShelfType")
print("        from app.models.shelf_positions import ShelfPosition, ShelfPositionNumber")
print("        from app.models.barcodes import Barcode")
print("        from app.models.barcode_types import BarcodeType")
print("        from app.models.container_types import ContainerType")
print("        from app.models.owners import Owner, OwnerTier")
print("        from app.models.trays import Tray")
print("        from app.models.items import Item")
print("        from app.models.size_class import SizeClass")
print("        from app.models.media_types import MediaType")
print("        from app.models.subcollections import Subcollection")
print("        from app.models.users import User, Group, Permission, group_permission_association")
print("        from app.models.accession_jobs import AccessionJob")
print("        from app.models.verification_jobs import VerificationJob")
print("        from app.models.shelving_jobs import ShelvingJob")
print("        from app.models.refile_jobs import RefileJob")
print("        from app.models.pick_lists import PickList")
print("        from app.models.requests import Request, RequestType")
print("        from app.models.conveyance_bins import ConveyanceBin")
print("        import datetime")

for table, data in payload.items():
    if table not in model_map:
        continue
    model_name = model_map[table]
    
    # Process dates
    clean_data = {}
    for k, v in data.items():
        if isinstance(v, str) and 'T' in v and ('Z' in v or v.endswith('00')):
            if "Z" in v:
                clean_data[k] = f"datetime.datetime.strptime('{v}', '%Y-%m-%dT%H:%M:%S.%fZ')"
            elif ".00" in v:
                clean_data[k] = f"datetime.datetime.strptime('{v}', '%Y-%m-%dT%H:%M:%S.%f')"
            else:
                clean_data[k] = f"'{v}'"
        else:
            if isinstance(v, str):
                clean_data[k] = f"'{v}'"
            else:
                clean_data[k] = v
                
    params = ", ".join([f"{k}={v}" for k, v in clean_data.items() if k != 'verification_jobs'])
    
    print(f"        try:\n            {table}_obj = {model_name}({params})\n            session.add({table}_obj)\n            session.commit()\n        except Exception as e:\n            print(f'Failed {table}: {{e}}')\n            session.rollback()")

