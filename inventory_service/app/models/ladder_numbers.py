# /app/models/ladder_numbers.py - ULTIMATE, FINAL CORRECTED V2

import sqlalchemy as sa
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy import SmallInteger, Integer

from typing import Optional, List
from datetime import datetime, timezone

from app.database.base import Base


class LadderNumber(Base): # <--- Inherit from Base
    """
    Model to represent the Ladder Numbers table.
    """
    __tablename__ = "ladder_numbers"

    # Primary Key
    id: Mapped[int] = mapped_column(SmallInteger, primary_key=True, autoincrement=True)

    # Number field (unique)
    number: Mapped[int] = mapped_column(SmallInteger, nullable=False, unique=True)
    
    # --- RELATIONSHIP ---
    # ladders assigned this number
    # NOTE: The relationship uses a string forward reference ("Ladder") which is correctly configured
    ladders: Mapped[List["Ladder"]] = relationship(
        back_populates="ladder_number",
        primaryjoin="Ladder.ladder_number_id == LadderNumber.id",
        remote_side="LadderNumber.id"
    )