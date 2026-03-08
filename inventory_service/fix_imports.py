import re

with open("tests/conftest.py", "r") as f:
    content = f.read()

replacements = {
    "from app.models.aisles import Aisle, AisleNumber": "from app.models.aisles import Aisle\n        from app.models.aisle_numbers import AisleNumber",
    "from app.models.sides import Side, SideOrientation": "from app.models.sides import Side\n        from app.models.side_orientations import SideOrientation",
    "from app.models.ladders import Ladder, LadderNumber": "from app.models.ladders import Ladder\n        from app.models.ladder_numbers import LadderNumber",
    "from app.models.shelves import Shelf, ShelfNumber": "from app.models.shelves import Shelf\n        from app.models.shelf_numbers import ShelfNumber",
    "from app.models.shelf_positions import ShelfPosition, ShelfPositionNumber": "from app.models.shelf_positions import ShelfPosition\n        from app.models.shelf_position_numbers import ShelfPositionNumber",
    "from app.models.owners import Owner, OwnerTier": "from app.models.owners import Owner\n        from app.models.owner_tiers import OwnerTier",
    "from app.models.requests import Request, RequestType": "from app.models.requests import Request\n        from app.models.request_types import RequestType",
    "from app.models.users import User, Group, Permission, group_permission_association": "from app.models.users import User\n        from app.models.groups import Group\n        from app.models.permissions import Permission",
}

for old, new in replacements.items():
    content = content.replace(old, new)

with open("tests/conftest.py", "w") as f:
    f.write(content)
