
import sys
import os

# Set up the app context
current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
sys.path.append(parent_dir)

from app.database.session import get_session
from app.models.pick_lists import PickList
from app.models.requests import Request, RequestStatus
from app.models.items import Item, ItemStatus
from app.models.owners import Owner
from app.models.buildings import Building
from app.models.trays import Tray
from app.models.shelf_positions import ShelfPosition
from app.models.shelves import Shelf
from app.models.aisles import Aisle
from app.models.item_retrieval_events import ItemRetrievalEvent
from sqlalchemy import select, update
from datetime import datetime, timezone

def test_crash():
    session = next(get_session())
    print("Session started.")

    try:
        # 1. Setup Data
        # Ensure we have a building, owner, etc.
        owner = session.execute(select(Owner)).scalars().first()
        if not owner:
            owner = Owner(name="Test Owner")
            session.add(owner)
            session.commit()
            session.refresh(owner)
            
        building = session.execute(select(Building)).scalars().first()
        if not building:
            building = Building(name="Test Building")
            session.add(building)
            session.commit()
            session.refresh(building)

        # Create Item with Owner
        item = Item(
            barcode_id=None, # simplification
            owner_id=owner.id,
            status=ItemStatus.In,
            title="Test Item for Crash"
        )
        session.add(item)
        session.commit()
        session.refresh(item)
        print(f"Item created: {item.id}, Owner: {item.owner_id}")

        # Create Picklist
        picklist = PickList(
            building_id=building.id,
            status="Running",
            name="Test Crash Picklist"
        )
        session.add(picklist)
        session.commit()
        session.refresh(picklist)
        print(f"Picklist created: {picklist.id}")

        # Create Request
        request = Request(
            item_id=item.id,
            pick_list_id=picklist.id,
            building_id=building.id,
            status=RequestStatus.PickList
        )
        session.add(request)
        session.commit()
        session.refresh(request)
        print(f"Request created: {request.id}")

        # 2. Simulate the CRASH logic
        # Original code used: status=ItemStatus.Out
        print("Attempting update with ItemStatus.Out (Enum)...")
        try:
            session.execute(
                update(Item).where(Item.id == item.id).values(
                    status=ItemStatus.Out, # The suspected culprit
                    update_dt=datetime.now(timezone.utc)
                )
            )
            print("Enum update SUCCESS!")
        except Exception as e:
            print(f"Enum update FAILED: {e}")

        # Original code also created Retrieval Event
        print("Attempting to create ItemRetrievalEvent...")
        try:
            event = ItemRetrievalEvent(
                item_id=item.id,
                owner_id=item.owner_id,
                pick_list_id=picklist.id
            )
            session.add(event)
            session.commit()
            print("Event creation SUCCESS!")
        except Exception as e:
            print(f"Event creation FAILED: {e}")
            session.rollback()

    except Exception as e:
        print(f"General Setup FAILED: {e}")
    finally:
        session.close()

if __name__ == "__main__":
    test_crash()
