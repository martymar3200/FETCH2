import os
import app.main
from sqlalchemy import select
from app.database.session import session_manager
from app.models.non_tray_items import NonTrayItem
from app.models.barcodes import Barcode
from app.models.shelf_positions import ShelfPosition
from app.models.shelves import Shelf
from app.models.ladders import Ladder
from app.models.sides import Side
from app.models.side_orientations import SideOrientation
from app.models.aisles import Aisle
from app.models.modules import Module
from app.models.buildings import Building
from app.models.media_types import MediaType
from app.models.owners import Owner
from app.models.size_class import SizeClass
from app.models.container_types import ContainerType

def check():
    with session_manager() as session:
        for nti_id in [4, 5]:
            print(f"\n--- Checking Non-Tray Item {nti_id} ---")
            nti = session.get(NonTrayItem, nti_id)
            if not nti:
                print("Item not found!")
                continue
            
            # Check barcode
            barcode = session.get(Barcode, nti.barcode_id) if nti.barcode_id else None
            print(f"Barcode ID: {nti.barcode_id} -> {barcode.value if barcode else 'None'}")
            
            # Check owner
            owner = session.get(Owner, nti.owner_id) if nti.owner_id else None
            print(f"Owner ID: {nti.owner_id} -> {owner.name if owner else 'None'}")
            
            # Check size class
            size_class = session.get(SizeClass, nti.size_class_id) if nti.size_class_id else None
            print(f"Size Class ID: {nti.size_class_id} -> {size_class.name if size_class else 'None'}")
            
            # Check media type
            media_type = session.get(MediaType, nti.media_type_id) if nti.media_type_id else None
            print(f"Media Type ID: {nti.media_type_id} -> {media_type.name if media_type else 'None'}")
            
            # Check container type
            container_type = session.get(ContainerType, nti.container_type_id) if nti.container_type_id else None
            print(f"Container Type ID: {nti.container_type_id} -> {container_type.type if container_type else 'None'}")
            
            # Check shelf position
            pos = session.get(ShelfPosition, nti.shelf_position_id) if nti.shelf_position_id else None
            print(f"Shelf Position ID: {nti.shelf_position_id} -> {pos.position_number if pos else 'None'}")
            
            if pos:
                shelf = session.get(Shelf, pos.shelf_id) if pos.shelf_id else None
                print(f"  Shelf ID: {pos.shelf_id} -> {shelf.shelf_number if shelf else 'None'}")
                if shelf:
                    ladder = session.get(Ladder, shelf.ladder_id) if shelf.ladder_id else None
                    print(f"    Ladder ID: {shelf.ladder_id} -> {ladder.ladder_number if ladder else 'None'}")
                    if ladder:
                        side = session.get(Side, ladder.side_id) if ladder.side_id else None
                        print(f"      Side ID: {ladder.side_id} -> {side.id if side else 'None'}")
                        if side:
                            side_orient = session.get(SideOrientation, side.side_orientation_id) if side.side_orientation_id else None
                            print(f"        Side Orientation ID: {side.side_orientation_id} -> {side_orient.name if side_orient else 'None'}")
                            aisle = session.get(Aisle, side.aisle_id) if side.aisle_id else None
                            print(f"        Aisle ID: {side.aisle_id} -> {aisle.aisle_number if aisle else 'None'}")
                            if aisle:
                                module = session.get(Module, aisle.module_id) if aisle.module_id else None
                                print(f"          Module ID: {aisle.module_id} -> {module.module_number if module else 'None'}")
                                if module:
                                    building = session.get(Building, module.building_id) if module.building_id else None
                                    print(f"            Building ID: {module.building_id} -> {building.name if building else 'None'}")

if __name__ == "__main__":
    check()
