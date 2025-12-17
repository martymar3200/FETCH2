# /app/models/users.py - REFACRORED TO SQLALCHEMY V2

import sqlalchemy as sa
from sqlalchemy.orm import relationship, Mapped, mapped_column
from sqlalchemy import ForeignKey, String, Integer, VARCHAR, TIMESTAMP, Boolean, DateTime, Column, func

from typing import Optional, List
from datetime import datetime, timezone
# REMOVED: from sqlmodel import SQLModel, Field, Relationship

# NEW IMPORT: Import the Base class you just created
from app.database.base import Base # <--- CRITICAL CHANGE
from app.models.user_groups import UserGroup # Assuming this path is correct


# --- User Model ---
# NOTE: The __tablename__ and audit columns (create_dt, update_dt) are now
# automatically handled by the 'Base' class you created in base.py,
# so we remove the explicit definitions here.

class User(Base):
    """
    Model to represent the Users table.
    """
    # REMOVED: __tablename__ = "users" (Handled by Base)

    # --- CORE FIELDS ---
    # Primary Key - using Mapped[int] and mapped_column
    id: Mapped[int] = mapped_column(Integer, primary_key=True)

    # VARCHAR(50) -> String(50)
    first_name: Mapped[str] = mapped_column(String(50), nullable=False)
    last_name: Mapped[str] = mapped_column(String(50), nullable=False)

    # VARCHAR(100) -> String(100), Unique constraint
    email: Mapped[Optional[str]] = mapped_column(String(100), nullable=True, unique=True)
    
    # Auth Token
    fetch_auth_token: Mapped[Optional[str]] = mapped_column(String(300), nullable=True)

    # TIMESTAMP (timezone=True)
    fetch_auth_expiration: Mapped[Optional[datetime]] = mapped_column(TIMESTAMP(timezone=True), nullable=True)
    
    # REMOVED: create_dt and update_dt (Handled by Base)
    # The explicit defaults were moved to Base.

    @property
    def name(self) -> str:
            return f"{self.first_name} {self.last_name}"

    # --- RELATIONSHIPS ---
    # Converted from Relationship(...) to relationship(...)

    # NOTE: SQLAlchemy 2.0 recommends using a lambda for string-based references
    # in relationships, which is a cleaner pattern than the sa_relationship_kwargs.

    # 1. Accession Jobs (Assigned User)
    accession_jobs: Mapped[List["AccessionJob"]] = relationship(
        back_populates="user",
        primaryjoin="AccessionJob.user_id==User.id", # Explicit primaryjoin kept
        lazy="selectin"
    )

    # 2. Shelving Jobs (Assigned User)
    shelving_jobs: Mapped[List["ShelvingJob"]] = relationship(
        back_populates="user",
        primaryjoin="ShelvingJob.user_id==User.id",
        lazy="selectin"
    )

    # 3. Verification Jobs (Assigned User)
    verification_jobs: Mapped[List["VerificationJob"]] = relationship(
        back_populates="user",
        primaryjoin="VerificationJob.user_id==User.id",
        lazy="selectin"
    )

    # 4. Pick Lists (Assigned User)
    pick_lists: Mapped[List["PickList"]] = relationship(
        back_populates="user",
        primaryjoin="PickList.user_id==User.id",
        lazy="selectin"
    )

    # 5. Groups (Many-to-Many)
    groups: Mapped[List["Group"]] = relationship(
        back_populates="users",
        secondary=UserGroup.__table__, # IMPORTANT: Use the __table__ attribute of the link_model
    )
    
    # 6. Refile Jobs (Assigned User)
    refile_jobs: Mapped[List["RefileJob"]] = relationship(
        back_populates="assigned_user",
        primaryjoin="RefileJob.assigned_user_id==User.id",
        lazy="selectin"
    )

    # 7. Withdraw Jobs (Assigned User)
    withdraw_jobs: Mapped[List["WithdrawJob"]] = relationship(
        back_populates="assigned_user",
        primaryjoin="WithdrawJob.assigned_user_id==User.id",
        lazy="selectin"
    )

    # 8. Batch Uploads
    batch_uploads: Mapped[List["BatchUpload"]] = relationship(back_populates="user")

    # 9. Created Accession Jobs
    created_accession_jobs: Mapped[List["AccessionJob"]] = relationship(
        back_populates="created_by",
        primaryjoin="AccessionJob.created_by_id==User.id",
        lazy="selectin"
    )

    # 10. Created Verification Jobs
    created_verification_jobs: Mapped[List["VerificationJob"]] = relationship(
        back_populates="created_by",
        primaryjoin="VerificationJob.created_by_id==User.id",
        lazy="selectin"
    )

    # 11. Created Shelving Jobs
    created_shelving_jobs: Mapped[List["ShelvingJob"]] = relationship(
        back_populates="created_by",
        primaryjoin="ShelvingJob.created_by_id==User.id",
        lazy="selectin"
    )

    # 12. Created Pick Lists
    created_pick_lists: Mapped[List["PickList"]] = relationship(
        back_populates="created_by",
        primaryjoin="PickList.created_by_id==User.id",
        lazy="selectin"
    )

    # 13. Created Refile Jobs
    created_refile_jobs: Mapped[List["RefileJob"]] = relationship(
        back_populates="created_by",
        primaryjoin="RefileJob.created_by_id==User.id",
        lazy="selectin"
    )
    
    # 14. Created Withdraw Jobs
    created_withdraw_jobs: Mapped[List["WithdrawJob"]] = relationship(
        back_populates="created_by",
        primaryjoin="WithdrawJob.created_by_id==User.id",
        lazy="selectin"
    )
    
    # 15. Shelving Job Discrepancies
    shelving_job_discrepancies: Mapped[List["ShelvingJobDiscrepancy"]] = relationship(
        back_populates="assigned_user",
        primaryjoin="ShelvingJobDiscrepancy.assigned_user_id==User.id",
        lazy="selectin"
    )
    
    # 16. Verification Changes
    verification_changes: Mapped[List["VerificationChange"]] = relationship(back_populates="completed_by")
    
    # 17. Move Discrepancies
    move_discrepancies: Mapped[List["MoveDiscrepancy"]] = relationship(
        back_populates="assigned_user",
        primaryjoin="MoveDiscrepancy.assigned_user_id==User.id",
        lazy="selectin"
    )
    
    # 18. Requests
    requests: Mapped[List["Request"]] = relationship(
        back_populates="requested_by",
        primaryjoin="Request.requested_by_id==User.id",
    )