# /app/models/aisles.py - REFACRORED TO SQLALCHEMY V2

import sqlalchemy as sa
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy import Integer, SmallInteger, ForeignKey
from sqlalchemy.schema import UniqueConstraint

from typing import Optional, List
from datetime import datetime, timezone
# REMOVED: from sqlmodel import SQLModel, Field, Relationship

# NEW IMPORT: Import the Base class you created
from app.database.base import Base
from app.models.modules import Module
from app.models.aisle_numbers import AisleNumber


class Aisle(Base): # <--- Inherit from Base
    """
    Model to represent the aisles table.
    """

    # NOTE: __tablename__ is handled by Base.
    # __tablename__ = "aisles"

    __table_args__ = (
        UniqueConstraint(
            "module_id", "aisle_number_id", name="uq_module_aisle_number_id"
        ),
    )

    # Primary Key
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)

    # Sort Priority
    sort_priority: Mapped[Optional[int]] = mapped_column(SmallInteger, nullable=True, default=None)

    # Foreign Keys
    aisle_number_id: Mapped[int] = mapped_column(ForeignKey("aisle_numbers.id"), nullable=False)
    module_id: Mapped[Optional[int]] = mapped_column(ForeignKey("modules.id"), nullable=True, default=None)
    
    # REMOVED: create_dt and update_dt are handled by the Base class.

    # --- RELATIONSHIPS ---
    # aisle number belonging to an aisle
    aisle_number: Mapped[AisleNumber] = relationship(back_populates="aisles")
    
    # module belonging to an aisle
    module: Mapped[Optional[Module]] = relationship(back_populates="aisles") # Changed type hint to Optional[Module] to match module_id nullable=True
    
    # sides belonging to an aisle
    sides: Mapped[List["Side"]] = relationship(back_populates="aisle")