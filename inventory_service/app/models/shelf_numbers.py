# /app/models/shelf_numbers.py - ULTIMATE, FINAL CORRECTED V2

import sqlalchemy as sa
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy import SmallInteger, Integer

from typing import Optional, List
from datetime import datetime, timezone

from app.database.base import Base


class ShelfNumber(Base): # <--- Inherit from Base
    """
    Model to represent the Shelf Numbers table.
    """
    __tablename__ = "shelf_numbers"

    # Primary Key
    id: Mapped[int] = mapped_column(SmallInteger, primary_key=True, autoincrement=True)

    # Number field (unique)
    number: Mapped[int] = mapped_column(SmallInteger, nullable=False, unique=True)
    
    # --- RELATIONSHIP ---
    # shelves assigned this number
    shelves: Mapped[List["Shelf"]] = relationship(
        back_populates="shelf_number",
        primaryjoin="Shelf.shelf_number_id==ShelfNumber.id" # <-- FINAL FIX
    )