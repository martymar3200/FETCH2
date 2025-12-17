# /code/app/models/audit_trails.py - REFACRORED TO PURE SQLALCHEMY V2

import sqlalchemy as sa
from sqlalchemy.orm import Mapped, mapped_column, DeclarativeBase
from sqlalchemy import BigInteger, VARCHAR, TIMESTAMP, DateTime, JSON, String, Column, text

from typing import Optional
from datetime import datetime, timezone
# REMOVED: from sqlmodel import SQLModel, Field, Relationship


# NEW IMPORT: Use SQLAlchemy's DeclarativeBase since this table has a custom schema (no create_dt)
class AuditTrailBase(DeclarativeBase):
    pass


class AuditTrail(AuditTrailBase): # <--- Inherit from the local DeclarativeBase
    """
    Model to represent the Audit Tails table.
    """

    __tablename__ = "audit_log"

    # Primary Key
    id: Mapped[Optional[int]] = mapped_column(BigInteger, primary_key=True)

    # V2 Mapped fields (replacing Field(sa_column=sa.Column(...)))
    table_name: Mapped[str] = mapped_column(String(50), nullable=False, default=None)
    record_id: Mapped[str] = mapped_column(String(50), nullable=False, default=None)
    operation_type: Mapped[str] = mapped_column(String(50), nullable=False, default=None)
    last_action: Mapped[Optional[str]] = mapped_column(String(150), nullable=True, default=None)
    
    # Datetime Field (Note: No explicit default because it's usually set by a DB trigger)
    updated_at: Mapped[datetime] = mapped_column(TIMESTAMP(timezone=True), nullable=False)
    
    updated_by: Mapped[str] = mapped_column(String(50), nullable=False, default=None)
    
    # JSON Fields
    original_values: Mapped[Optional[dict]] = mapped_column(JSON, nullable=True, default=None)
    new_values: Mapped[Optional[dict]] = mapped_column(JSON, nullable=True, default=None)
    
    # Updated By User ID (Assuming this is a string in VARCHAR(50) as per your model)
    updated_by_user_id: Mapped[Optional[str]] = mapped_column(String(50), nullable=False, default=None)