# /code/app/routers/history.py - REFACRORED TO SQLALCHEMY V2

from fastapi import APIRouter, Depends, Query, HTTPException
from fastapi_pagination import Page
# CRITICAL FIX: Changed from .ext.sqlmodel to .ext.sqlalchemy
from fastapi_pagination.ext.sqlalchemy import paginate
# CRITICAL FIX: Replaced from sqlmodel import Session, select
from sqlalchemy.orm import Session  # Session is imported from sqlalchemy.orm now
from sqlalchemy import select  # select is imported from sqlalchemy now
from sqlalchemy.exc import IntegrityError
from datetime import datetime, timezone
from typing import Optional, List

# SQLAlchemy V2 imports
from sqlalchemy import and_, text, asc, desc, func, column, or_, not_, update

from app.database.session import get_session
from app.config.exceptions import (
    NotFound,
    ValidationException,
)
from app.filter_params import SortParams
from app.models.accession_jobs import AccessionJob, AccessionJobStatus
from app.models.pick_lists import PickList, PickListStatus
from app.models.refile_jobs import RefileJob, RefileJobStatus
from app.models.shelf_positions import ShelfPosition
from app.models.shelving_jobs import ShelvingJob, ShelvingJobStatus
from app.models.verification_jobs import VerificationJob, VerificationJobStatus
from app.models.withdraw_jobs import WithdrawJob, WithdrawJobStatus
from app.schemas.audit_trails import AuditTrailListOutput, AuditTrailDetailOutput
from app.sorting import BaseSorter
from app.models.audit_trails import AuditTrail

from app.auth.dependencies import RequiresPermission

router = APIRouter(
    prefix="/history",
    tags=["audit tails"],
    dependencies=[Depends(RequiresPermission("can_view_audit_logs"))],
)


@router.get("/", response_model=Page[AuditTrailListOutput])
def get_audit_trails_list(
    queue: Optional[bool] = Query(default=False),
    table_names: Optional[list[str]] = Query(default=None),
    sort_params: SortParams = Depends(),
    session: Session = Depends(get_session),
) -> list:
    """
    Get a paginated list of audit trails.
    """
    # Create a query to select all audit_trails
    query = select(AuditTrail)

    if queue:
        table_names = [
            "accession_jobs",
            "verification_jobs",
            "shelving_jobs",
            "pick_lists",
            "refile_jobs",
            "withdraw_jobs",
        ]
        query = query.filter(AuditTrail.table_name.in_(table_names))

    elif table_names:
        query = query.filter(AuditTrail.table_name.in_(table_names))

    # Validate and Apply sorting based on sort_params
    if sort_params.sort_by:
        # Apply sorting using BaseSorter
        sorter = BaseSorter(AuditTrail)
        query = sorter.apply_sorting(query, sort_params)

    # CRITICAL FIX: Paginate now takes only the query object
    return paginate(query)


@router.get("/{table_name}/{record_id}", response_model=List[AuditTrailDetailOutput])
def get_audit_trails_detail_list(
    table_name: str,
    record_id: str,
    sort_params: SortParams = Depends(),
    session: Session = Depends(get_session),
):
    """
    Get a detailed list of audit trails for a specific record.
    CRITICAL: This function has multiple session.exec() calls that must be converted.
    """

    def add_last_action(logs, audit_item, audit_table_name):
        return_logs = []
        picklist = {}
        refile = None
        for log in logs:
            if audit_table_name == "accession_jobs" and "scanned_for_accession" in log.new_values:
                log.last_action = f"Accessioned {audit_item.barcode.value}"
                return [log]
            if audit_table_name == "verification_jobs" and "scanned_for_verification" in log.new_values:
                log.last_action = f"Verified {audit_item.barcode.value}"
                return [log]
            if audit_table_name == "shelving_jobs" and "scanned_for_shelving" in log.new_values:
                log.last_action = f"Shelved {audit_item.barcode.value}"
                return_logs.append(log)
            if audit_table_name == "shelving_jobs" and "shelf_position_id" in log.new_values:
                if "shelf_position_id" in log.original_values:
                    # V2 FIX: session.exec(select(...)).first() -> session.execute(select(...)).scalars().first()
                    old_shelf = session.execute(
                        select(ShelfPosition).where(ShelfPosition.id == log.original_values["shelf_position_id"])
                    ).scalars().first()
                    # V2 FIX: session.exec(select(...)).first() -> session.execute(select(...)).scalars().first()
                    new_shelf = session.execute(
                        select(ShelfPosition).where(ShelfPosition.id == log.new_values["shelf_position_id"])
                    ).scalars().first()
                    log.last_action = f"Moved {audit_item.barcode.value} from {old_shelf.location} to {new_shelf.location}"
                    return_logs.append(log)
            if audit_table_name == "withdraw_jobs" and "status" in log.new_values:
                log.last_action = f"Withdrew {audit_item.withdrawn_barcode.value}"
                return [log]
            if audit_table_name == "refile_jobs" and "status" in log.new_values and log.new_values["status"] == "In":
                log.last_action = f"Refiled {audit_item.barcode.value}"
                refile = log
            if audit_table_name == "pick_lists" and "status" in log.new_values:
                if "PickList" == log.new_values["status"]:
                    log.last_action = f"Added to Picklist {audit_item.barcode.value}"
                    picklist["picklist"] = log
                elif "Out" == log.new_values["status"]:
                    log.last_action = f"Picked {audit_item.barcode.value}"
                    picklist['picked'] = log
                elif "Withdrawn" == log.new_values["status"]:
                    log.last_action = f"Withdrew {audit_item.barcode_value}"
                    picklist['withdrawn'] = log
        for action_type in ["requested", "picklist", "picked", "withdrawn"]:
            if action_type in picklist:
                return_logs.append(picklist[action_type])
        if refile:
            return_logs.append(refile)
        return return_logs

    def get_audit_query(audit_table_name, audit_table_item_id, since_date):
        log_query = select(AuditTrail).where(AuditTrail.table_name == audit_table_name).where(
            AuditTrail.record_id == str(audit_table_item_id)).where(
            AuditTrail.updated_at >= since_date
        )
        completed_dict = {
            "shelving_jobs": ShelvingJobStatus.Completed,
            "accession_jobs": AccessionJobStatus.Completed,
            "verification_jobs": VerificationJobStatus.Completed,
            "refile_jobs": RefileJobStatus.Completed,
            "withdraw_jobs": WithdrawJobStatus.Completed,
            "pick_lists": PickListStatus.Completed,
        }
        if table_name in completed_dict and completed_dict[table_name] == main_table.status:
            log_query = log_query.where(AuditTrail.updated_at <= main_table.update_dt)
        return log_query

    def add_accession_log(audit_table_name, audit_item):
        return AuditTrailDetailOutput(
            id=1,
            table_name=audit_table_name,
            operation_type="INSERT",
            record_id=str(main_table.id),
            updated_by=f"{main_table.created_by.first_name} {main_table.created_by.last_name}",
            updated_at=audit_item.accession_dt.replace(tzinfo=None),
            last_action=f"Accessioned {audit_item.barcode.value}",
            original_values=None,
            new_values={"scanned_for_accession": "true"}
        )

    if not table_name:
        raise ValidationException(detail="Table name is required.")

    if not record_id:
        raise ValidationException(detail="Record ID is required.")
    table_dict = {
        "accession_jobs": AccessionJob,
        "verification_jobs": VerificationJob,
        "shelving_jobs": ShelvingJob,
        "pick_lists": PickList,
        "refile_jobs": RefileJob,
        "withdraw_jobs": WithdrawJob,
    }
    query = (
        select(AuditTrail)
        .where(AuditTrail.table_name == table_name)
        .where(AuditTrail.record_id == record_id)
    )

    # Validate and Apply sorting based on sort_params
    sorter = BaseSorter(AuditTrail)

    if not sort_params.sort_by:
        sort_params.sort_by = "updated_at"
        sort_params.sort_order = "desc"

    # CRITICAL V2 FIX: session.exec().first() -> session.execute(select(...)).scalars().first()
    main_table = session.execute(select(table_dict[table_name]).where(table_dict[table_name].id == record_id)).scalars().first()
    
    if main_table:
        logs = []
        if getattr(main_table, "non_tray_items", None):
            for non_tray_item in main_table.non_tray_items:
                non_tray_logs = []
                # V2 FIX: session.exec().all() -> session.execute(query).scalars().all()
                audit_logs = session.execute(get_audit_query("non_tray_items", non_tray_item.id, main_table.create_dt)).scalars().all()
                non_tray_logs += add_last_action(audit_logs, non_tray_item, table_name)
                # Set scanned_for_accession on creation. This is for that edge case.
                if table_name == 'accession_jobs' and len(non_tray_logs) == 0 and non_tray_item.scanned_for_accession:
                    non_tray_logs.append(add_accession_log("non_tray_items", non_tray_item))
                logs += non_tray_logs
        if getattr(main_table, "trays", None):
            for tray in main_table.trays:
                tray_logs = []
                # V2 FIX: session.exec().all() -> session.execute(query).scalars().all()
                audit_logs = session.execute(get_audit_query("trays", tray.id, main_table.create_dt)).scalars().all()
                tray_logs += add_last_action(audit_logs, tray, table_name)
                if table_name == 'accession_jobs' and len(tray_logs) == 0 and tray.scanned_for_accession:
                    tray_logs.append(add_accession_log("trays", tray))
                logs += tray_logs
        if getattr(main_table, "items", None):
            for item in main_table.items:
                item_logs = []
                # V2 FIX: session.exec().all() -> session.execute(query).scalars().all()
                audit_logs = session.execute(get_audit_query("items", item.id, main_table.create_dt)).scalars().all()
                item_logs += add_last_action(audit_logs, item, table_name)
                if table_name == 'accession_jobs' and len(item_logs) == 0 and item.scanned_for_accession:
                    item_logs.append(add_accession_log("items", item))
                logs += item_logs
        if table_name == "pick_lists":
            for request in main_table.requests:
                if request.non_tray_item:
                    # V2 FIX: session.exec().all() -> session.execute(query).scalars().all()
                    audit_logs = session.execute(get_audit_query("non_tray_items", request.non_tray_item.id, request.create_dt)).scalars().all()
                    logs += add_last_action(audit_logs, request.non_tray_item, table_name)
                if request.item:
                    # V2 FIX: session.exec().all() -> session.execute(query).scalars().all()
                    audit_logs = session.execute(get_audit_query("items", request.item.id, request.create_dt)).scalars().all()
                    logs += add_last_action(audit_logs, request.item, table_name)
        return logs

    return []
    # V2 FIX: session.exec()