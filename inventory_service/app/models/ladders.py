# /app/models/ladders.py - ULTIMATE, FINAL CORRECTED V2

import sqlalchemy as sa
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy import Integer, SmallInteger, ForeignKey
from sqlalchemy.schema import UniqueConstraint

from typing import Optional, List
from datetime import datetime, timezone
from app.database.base import Base

# Dependencies (MUST BE IMPORTED FOR ABSOLUTE FK REFERENCE)
from app.models.sides import Side # <--- CRITICAL IMPORT
from app.models.ladder_numbers import LadderNumber

class Ladder(Base): # <--- Inherit from Base
    """
    Model to represent Ladders table
    """

    # NOTE: __tablename__ is handled by Base.
    __table_args__ = (
        UniqueConstraint(
            "side_id", "ladder_number_id", name="uq_side_id_ladder_number_id"
        ),
    )

    # Primary Key
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)

    # Foreign Keys
    # CRITICAL FIX: Use LadderNumber.__table__.c.id for explicit, unbreakable reference
    ladder_number_id: Mapped[int] = mapped_column(ForeignKey(LadderNumber.__table__.c.id), nullable=False)
    
    # Standard FK
    side_id: Mapped[int] = mapped_column(ForeignKey("sides.id"), nullable=False)

    # Sort Priority
    sort_priority: Mapped[Optional[int]] = mapped_column(SmallInteger, nullable=True, default=None)
    
    # --- RELATIONSHIPS ---
    side: Mapped[Side] = relationship(back_populates="ladders")
    ladder_number: Mapped[LadderNumber] = relationship(back_populates="ladders")
    shelves: Mapped[List["Shelf"]] = relationship(back_populates="ladder")