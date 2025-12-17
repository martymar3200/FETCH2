# /app/models/aisle_numbers.py - REFACRORED TO SQLALCHEMY V2

import sqlalchemy as sa
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy import SmallInteger, Integer, TIMESTAMP, DateTime, func, Column

from typing import Optional, List
from datetime import datetime, timezone
# REMOVED: from sqlmodel import SQLModel, Field, Relationship

# NEW IMPORT: Import the Base class you created
from app.database.base import Base


class AisleNumber(Base): # <--- Inherit from Base
    """
    Model to represent the Aisle Numbers table.
    """

    # NOTE: __tablename__ is handled by Base, but since it's "aisle_numbers" and
    # Base's default pluralization will result in "aislenumberss", we must override.
    __tablename__ = "aisle_numbers"

    # Primary Key - using Mapped[int]
    id: Mapped[int] = mapped_column(SmallInteger, primary_key=True, autoincrement=True)

    # Number field
    number: Mapped[int] = mapped_column(SmallInteger, nullable=False, unique=True)
    
    # REMOVED: create_dt and update_dt are handled by the Base class.

    # aisles assigned this aisle number
    aisles: Mapped[List["Aisle"]] = relationship(back_populates="aisle_number")