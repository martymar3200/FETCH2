# /code/app/schemas/aisles.py - REFACRORED TO PYDANTIC V2

import uuid

from pydantic import BaseModel, Field, ConfigDict # <-- ADDED Field
from datetime import datetime, timezone
from typing import Optional, List, Annotated # <-- ADDED Annotated

from app.schemas.modules import ModuleCustomDetailReadOutput
from app.schemas.aisle_numbers import AisleNumberBaseOutput


# CRITICAL V2 FIX: Replaced V1 'conint' with V2 Annotated[int, Field(...)]
class AisleInput(BaseModel):
    # int in SQL is 2147483647 max
    aisle_number_id: Optional[Annotated[int, Field(ge=0, le=2147483647)]] = None
    aisle_number: Optional[int] = None # This is used for lookup, not strict constraint
    # SmallInteger in SQL is 32767 max
    module_id: Optional[Annotated[int, Field(ge=0, le=32767)]] = None
    sort_priority: Optional[Annotated[int, Field(ge=0, le=32767)]] = None

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "aisle_number_id": 1,
                "aisle_number": None,
                "module_id": 1,
                "sort_priority": 1
            }
        }
    )


class AisleUpdateInput(BaseModel):
    # CRITICAL V2 FIX
    aisle_number_id: Optional[Annotated[int, Field(ge=0, le=2147483647)]] = None
    # CRITICAL V2 FIX
    module_id: Optional[Annotated[int, Field(ge=0, le=32767)]] = None
    # CRITICAL V2 FIX
    sort_priority: Optional[Annotated[int, Field(ge=0, le=32767)]] = None

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "aisle_number_id": 1,
                "module_id": 1,
                "sort_priority": 1
            }
        }
    )


class AisleBaseReadOutput(BaseModel):
    id: int
    aisle_number_id: int


class SideOrientationNestedForAisle(BaseModel):
    name: str


class SideNestedForAisle(BaseModel):
    id: int
    side_orientation: SideOrientationNestedForAisle


class AisleListOutput(AisleBaseReadOutput):
    sides: List[SideNestedForAisle]
    module: ModuleCustomDetailReadOutput | None
    aisle_number: AisleNumberBaseOutput

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "id": 1,
                "aisle_number_id": 1,
                "module": {
                    "id": 1,
                    "module_number": "1",
                },
                "aisle_number": {
                    "id": 1,
                    "number": 1
                },
                "sides": [
                    {
                        "id": 1,
                        "side_orientation": {
                            "name": "Left"
                        }
                    }
                ]
            }
        }
    )


class AisleDetailWriteOutput(BaseModel):
    id: int
    aisle_number_id: int
    module_id: int | None = None
    sort_priority: Optional[int] = None
    create_dt: datetime
    update_dt: datetime

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "id": 1,
                "aisle_number_id": 1,
                "module_id": 1,
                "sort_priority": 1,
                "create_dt": "2023-10-08T20:46:56.764426",
                "update_dt": "2023-10-08T20:46:56.764398",
            }
        }
    )


class AisleDetailReadOutput(BaseModel):
    id: int
    sort_priority: Optional[int] = None
    create_dt: datetime
    update_dt: datetime
    module: ModuleCustomDetailReadOutput | None
    aisle_number: AisleNumberBaseOutput
    sides: List[SideNestedForAisle]

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "id": 1,
                "number": 1,
                "sort_priority": 1,
                "create_dt": "2023-10-08T20:46:56.764426",
                "update_dt": "2023-10-08T20:46:56.764398",
                "module": {
                    "id": 1,
                    "module_number": "1",
                },
                "aisle_number": {
                    "id": 1,
                    "number": 1
                },
                "sides": [
                    {
                        "id": 1,
                        "side_orientation": {
                            "name": "Left"
                        }
                    }
                ]
            }
        }
    )