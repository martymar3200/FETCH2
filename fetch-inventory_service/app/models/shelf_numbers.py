# /app/models/shelf_numbers.py - LEGACY: Lookup table kept for reference, relationships removed

import sqlalchemy as sa
from sqlalchemy.orm import Mapped, mapped_column
from sqlalchemy import SmallInteger

from app.database.base import Base


class ShelfNumber(Base):
    """
    Model to represent the Shelf Numbers table.
    DEPRECATED: Numbers are now direct fields on the entity models.
    """
    __tablename__ = "shelf_numbers"

    id: Mapped[int] = mapped_column(SmallInteger, primary_key=True, autoincrement=True)
    number: Mapped[int] = mapped_column(SmallInteger, nullable=False, unique=True)