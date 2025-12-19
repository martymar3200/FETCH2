import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.main import app
from app.database.session import get_session
from app.models.barcodes import Barcode
from app.models.items import Item
from app.models.non_tray_items import NonTrayItem
from app.models.container_types import ContainerType
from app.models.media_types import MediaType
from app.models.owners import Owner
from app.models.size_class import SizeClass
from app.models.users import User
from app.models.trays import Tray
from app.models.shelf_positions import ShelfPosition
from app.models.shelves import Shelf
from app.models.ladders import Ladder
from app.models.sides import Side
from app.models.aisles import Aisle
from app.models.modules import Module
from app.models.buildings import Building
from app.models.aisle_numbers import AisleNumber
from app.models.ladder_numbers import LadderNumber
from app.models.shelf_numbers import ShelfNumber
from app.models.shelf_position_numbers import ShelfPositionNumber
from app.models.shelf_types import ShelfType


# Setup in-memory SQLite database
SQLALCHEMY_DATABASE_URL = "sqlite:///:memory:"
engine = create_engine(
    SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}
)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

def override_get_session():
    try:
        db = TestingSessionLocal()
        yield db
    finally:
        db.close()

app.dependency_overrides[get_session] = override_get_session

client = TestClient(app)

@pytest.fixture(scope="module") 
def setup_db():
    from app.database.base import Base
    # Create tables
    Base.metadata.create_all(bind=engine)
    
    # Create dependencies
    db = TestingSessionLocal()
    
    # Create Dummy User
    user = User(email="test@test.com", first_name="Test", last_name="User")
    # user.active is not in __init__ args potentially if it defaults or if mapped columns behave differently. 
    # But checking User model line 38, fetch_auth_token etc. 
    # 'active' is NOT in the User model definition I saw!
    # Let me check User model again. I don't see 'active' column in the `view_file` output above.
    # It has id, first_name, last_name, email, fetch_auth_token, fetch_auth_expiration.
    # So I should remove 'active' as well.
    db.add(user)
    
    # Create Owner
    owner = Owner(name="TestOwner", email="owner@test.com")
    db.add(owner)
    
    # Create SizeClass
    size_class = SizeClass(name="TestSize", short_name="TS", height=10, width=10, depth=10)
    db.add(size_class)
    
    # Create MediaType
    media_type = MediaType(name="TestMedia")
    db.add(media_type)
    
    # Create ContainerType
    container_type = ContainerType(type="TestContainer", check_width=True, check_height=True, check_depth=True)
    db.add(container_type)

    # Create ShelfType
    shelf_type = ShelfType(type="TestShelfType", size_class=size_class, container_type=container_type, max_capacity=10)
    db.add(shelf_type)
    
    # Create Barcode
    barcode_item = Barcode(value="ITEM123")
    db.add(barcode_item)
    barcode_nt = Barcode(value="NT123")
    db.add(barcode_nt)
    barcode_tray = Barcode(value="TRAY123")
    db.add(barcode_tray)

    # Create Building Hierarchy for Tray (needed for item creation if tray is required, but items can be trayless initially?)
    # Actually create_item requires tray_id usually unless... logic allows otherwise. 
    # Let's check create_item logic. It requires tray_id.
    
    # Building...
    building = Building(name="TestBuilding")
    db.add(building)
    db.commit() # commit to get IDs

    module = Module(name="M1", building_id=building.id, x_start=0, x_end=10, y_start=0, y_end=10)
    db.add(module)
    db.commit()
    
    aisle_num = AisleNumber(number=1)
    db.add(aisle_num)
    db.commit()
    
    aisle = Aisle(module_id=module.id, aisle_number_id=aisle_num.id, x_start=0, x_end=0, y_start=0, y_end=0)
    db.add(aisle)
    db.commit()
    
    side = Side(aisle_id=aisle.id, side="A", x_start=0, x_end=0, y_start=0, y_end=0)
    db.add(side)
    db.commit()
    
    ladder_num = LadderNumber(number=1)
    db.add(ladder_num)
    db.commit()

    ladder = Ladder(side_id=side.id, ladder_number_id=ladder_num.id, x_start=0, x_end=0, y_start=0, y_end=0)
    db.add(ladder)
    db.commit()

    shelf_num = ShelfNumber(number=1)
    db.add(shelf_num)
    db.commit()

    shelf = Shelf(ladder_id=ladder.id, shelf_number_id=shelf_num.id, barcode_id=barcode_item.id, shelf_type_id=shelf_type.id, owner_id=owner.id, available_space=100)
    db.add(shelf)
    db.commit()

    shelf_pos_num = ShelfPositionNumber(number=1)
    db.add(shelf_pos_num)
    db.commit()

    shelf_pos = ShelfPosition(shelf_id=shelf.id, shelf_position_number_id=shelf_pos_num.id, location="1-1-1-1-1-1")
    db.add(shelf_pos)
    db.commit()

    tray = Tray(barcode_id=barcode_tray.id, size_class_id=size_class.id, shelf_position_id=shelf_pos.id, owner_id=owner.id)
    db.add(tray)
    db.commit()

    db.close()
    yield

def test_create_item_has_accessioned_status(setup_db):
    db = TestingSessionLocal()
    # Get IDs
    owner_id = db.query(Owner).first().id
    size_class_id = db.query(SizeClass).first().id
    media_type_id = db.query(MediaType).first().id
    barcode_id = db.query(Barcode).filter(Barcode.value=="ITEM123").first().id
    tray_id = db.query(Tray).first().id
    
    item_data = {
        "barcode_value": "ITEM123", # create_item logic might look up barcode or expect valid barcode_id logic
        # Checking create_item in items.py: it takes ItemInput.
        # ItemInput has barcode_id, not barcode_value? 
        # Let's check Schema.
        # It has barcode_id, owner_id, size_class_id, media_type_id, tray_id, etc.
        "barcode_id": barcode_id,
        "owner_id": owner_id,
        "size_class_id": size_class_id,
        "media_type_id": media_type_id,
        "tray_id": tray_id,
        "item_type": "item"
    }
    
    response = client.post("/items/", json=item_data)
    # Note: create_item expects ItemInput. 
    # If successful:
    assert response.status_code == 201, response.text
    assert response.json()["status"] == "Accessioned"

def test_create_non_tray_item_has_accessioned_status(setup_db):
    db = TestingSessionLocal()
    owner_id = db.query(Owner).first().id
    size_class_id = db.query(SizeClass).first().id
    media_type_id = db.query(MediaType).first().id
    barcode_id = db.query(Barcode).filter(Barcode.value=="NT123").first().id
    container_type_id = db.query(ContainerType).first().id
    
    nt_data = {
        "barcode_id": barcode_id,
        "owner_id": owner_id,
        "size_class_id": size_class_id,
        "media_type_id": media_type_id,
        "container_type_id": container_type_id,
        "item_type": "non_tray_item"
    }

    response = client.post("/non_tray_items/", json=nt_data)
    assert response.status_code == 201, response.text
    assert response.json()["status"] == "Accessioned"
