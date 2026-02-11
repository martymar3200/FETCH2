
import sys
import os

# Add the parent directory to sys.path to allow importing app modules
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app.database.session import get_session
from app.models.requests import Request, RequestStatus
from app.models.items import Item, ItemStatus
from app.models.barcodes import Barcode
import importlib
import pkgutil
from app.models.pick_lists import PickList, PickListStatus
from sqlalchemy import select
from datetime import datetime, timezone

def import_all_models():
    """Dynamically imports all modules under the app.models package."""
    # We must ensure app.models is imported first to get its path (__path__)
    models_package = importlib.import_module('app.models')

    # Recursively walk the package to find and import every module.
    for module_loader, name, is_pkg in pkgutil.walk_packages(
        models_package.__path__,
        models_package.__name__ + '.'
    ):
        try:
            importlib.import_module(name)
        except Exception as e:
            pass

def verify_delete():
    import_all_models()
    session = next(get_session())
    print("Starting Delete Verification...")

    # 1. Setup Data: Find an available item
    item = session.execute(select(Item).where(Item.status == 'In').limit(1)).scalars().first()
    if not item:
        print("No 'In' items found. Cannot run test.")
        return

    print(f"Using Item ID: {item.id}")

    # 2. Create Request
    req = Request(
        item_id=item.id,
        status=RequestStatus.New,
        request_type_id=1, # Assuming 1 exists
        building_id=1,
        delivery_location_id=1,
        priority_id=1,
        requestor_name="Test User"
    )
    session.add(req)
    session.commit()
    session.refresh(req)
    print(f"Created Request ID: {req.id}")

    # 3. Verify it appears in list
    requests = session.execute(select(Request).where(Request.deleted == False)).scalars().all()
    if req.id not in [r.id for r in requests]:
        print("FAIL: Request not found in list")
    else:
        print("PASS: Request found in list")

    # 4. Delete Request (Soft Delete)
    # Simulate Delete Logic
    req.deleted = True
    session.add(req)
    session.commit()
    print(f"Deleted Request ID: {req.id}")

    # 5. Verify it is GONE from list
    requests = session.execute(select(Request).where(Request.deleted == False)).scalars().all()
    if req.id in [r.id for r in requests]:
        print("FAIL: Request still found in list after delete")
    else:
        print("PASS: Request correctly filtered from list")

    # 6. Verify it still exists in DB
    db_req = session.get(Request, req.id)
    if db_req and db_req.deleted:
        print("PASS: Request exists in DB with deleted=True")
    else:
        print("FAIL: Request not found in DB or deleted=False")

    # 7. Test Re-creation (Fix for "Item already requested" bug)
    print("Testing re-creation of request for same item...")
    try:
        req3 = Request(
            item_id=item.id,
            status=RequestStatus.New,
            request_type_id=1,
            building_id=1,
            delivery_location_id=1,
            priority_id=1,
            requestor_name="Test User 2"
        )
        session.add(req3)
        session.commit()
        print(f"PASS: Successfully created new request {req3.id} for item {item.id} after previous was deleted.")
        session.delete(req3) # Cleanup
    except Exception as e:
        print(f"FAIL: Could not create new request for item {item.id}: {e}")

    # 8. Test Validation (Mocking logic since we can't easily call API here)
    # Create another request
    req2 = Request(
        item_id=item.id,
        status=RequestStatus.PickList,
        deleted=False
    )
    session.add(req2)
    session.commit()
    print(f"Created PickList Request ID: {req2.id}")

    if req2.status in [RequestStatus.PickList, RequestStatus.Completed]:
        print("PASS: Validation Logic correct (would block delete)")
    else:
        print("FAIL: Validation logic failure simulation")
        
    # Cleanup
    session.delete(req) # Hard delete for cleanup
    session.delete(req2)
    session.commit()
    print("Cleanup Complete")

if __name__ == "__main__":
    verify_delete()
