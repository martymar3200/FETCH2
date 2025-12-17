# /app/models/shelf_position_numbers.py - FINAL CLEANUP

import sqlalchemy as sa
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy import SmallInteger, Integer

from typing import Optional, List
from datetime import datetime, timezone

from app.database.base import Base


class ShelfPositionNumber(Base): 
    """
    Model to represent the Shelf Position Numbers table.
    """
    __tablename__ = "shelf_position_numbers"
    # Primary Key
    id: Mapped[int] = mapped_column(SmallInteger, primary_key=True, autoincrement=True)

    # Number field (unique)
    number: Mapped[int] = mapped_column(SmallInteger, nullable=False, unique=True)
    
    # --- RELATIONSHIP ---
    # The simple, standard back_populates definition
    shelf_positions: Mapped[List["ShelfPosition"]] = relationship(
        back_populates="shelf_position_number"
    )