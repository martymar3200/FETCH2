# /code/app/routers/verification_jobs.py - REFACRORED TO SQLALCHEMY V2

import logging
from typing import List, Optional

from fastapi import APIRouter, HTTPException, Depends, Query, BackgroundTasks
from fastapi_pagination import Page
# CRITICAL FIX: Changed from .ext.sqlmodel to .ext.sqlalchemy
from fastapi_pagination.ext.sqlalchemy import paginate
# CRITICAL FIX: Replaced from sqlmodel import Session, select
from sqlalchemy.orm import Session # Session is imported from sqlalchemy.orm now
from sqlalchemy import select, func, distinct, case, literal_column, update, and_ # select/update/func imported from sqlalchemy now
from datetime import datetime, timezone
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import aliased, selectinload 

from app.database.session import get_session, commit_record
from app.filter_params import SortParams, JobFilterParams
from app.models.barcodes import Barcode
from app.models.container_types import ContainerType
from app.models.items import Item
from app.models.non_tray_items import NonTrayItem
from app.models.trays import Tray
from app.models.users import User
from app.models.verification_changes import VerificationChange
from app.models.verification_jobs import VerificationJob
from app.models.accession_jobs import AccessionJob
from app.schemas.verification_jobs import (
    VerificationJobInput,
    VerificationJobUpdateInput,
    VerificationJobListOutput,
    VerificationJobListDropdownOutput,
    VerificationJobDetailOutput,
    VerificationJobAddInput,
    VerificationJobRemoveInput,
    VerificationJobAccCheckOutput
)
from app.config.exceptions import (
    NotFound,
    ValidationException,
    InternalServerError,
)
from app.sorting import BaseSorter
from app.tasks import (
    complete_verification_job,
    manage_verification_job_transition, manage_verification_job_change_action,
)

router = APIRouter(
    prefix="/verification-jobs",
    tags=["verification jobs"],
)


@router.get("/", response_model=Page[VerificationJobListOutput])
def get_verification_job_list(
    unshelved: bool | None = False,
    session: Session = Depends(get_session),
    params: JobFilterParams = Depends(),
    sort_params: SortParams = Depends()
) -> list:
    """
    Retrieve a paginated list of verification jobs.
    """
    # Use selectinload to fetch related user data in a single, efficient second query.
    query = select(VerificationJob).options(
        selectinload(VerificationJob.user),
        selectinload(VerificationJob.created_by)
    )

    if unshelved:
        # retrieve completed verification jobs that haven't been shelved
        query = query.where(VerificationJob.shelving_job_id == None).where(
            VerificationJob.status == "Completed"
        )
    if params.queue:
        # filter out completed.  maybe someday hide cancelled.
        query = query.where(VerificationJob.status != "Completed")

    # If no status is provided AND we are not in 'unshelved' or 'queue' mode,
    # DEFAULT to fetching only the active jobs to keep the dashboard fast.
    if params.status and len(list(filter(None, params.status))) > 0:
        query = query.where(VerificationJob.status.in_(params.status))
    elif not unshelved and not params.queue:
        query = query.where(
            VerificationJob.status.in_(["Created", "Paused", "Running"])
        )

    if params.workflow_id:
        query = query.where(VerificationJob.workflow_id == params.workflow_id)
    if params.user_id:
        query = query.where(VerificationJob.user_id.in_(params.user_id))
    if params.assigned_user:
        assigned_user_subquery = (
            select(User.id)
            .where(
                func.concat(User.first_name, ' ', User.last_name).in_(
                    params.assigned_user
                )
            )
            .distinct().scalar_subquery()
        )
        query = query.where(VerificationJob.user_id.in_(assigned_user_subquery))
    if params.container_type:
        subquery = (
            select(ContainerType.id)
            .where(ContainerType.type == params.container_type)
            .distinct().scalar_subquery()
        )
        query = query.where(VerificationJob.container_type_id == subquery)
    if params.trayed is not None:
        query = query.where(VerificationJob.trayed == params.trayed)
    if params.created_by_id:
        query = query.where(VerificationJob.created_by_id == params.created_by_id)
    if params.from_dt:
        query = query.where(VerificationJob.create_dt >= params.from_dt)
    if params.to_dt:
        query = query.where(VerificationJob.create_dt <= params.to_dt)

    # Validate and Apply sorting based on sort_params
    if sort_params.sort_by:
        sorter = BaseSorter(VerificationJob)
        query = sorter.apply_sorting(query, sort_params)
    else:
        # Add a default sort for consistent ordering
        query = query.order_by(VerificationJob.id.desc())

    return paginate(session, query)


@router.get("/dropdown/", response_model=Page[VerificationJobListDropdownOutput])
def get_verification_job_list_lite(
    unshelved: bool | None = False,
    session: Session = Depends(get_session),
    params: JobFilterParams = Depends(),
    sort_params: SortParams = Depends()
) -> list:
    filtered_jobs_cte = (
        select(VerificationJob)
        .where(
            VerificationJob.shelving_job_id == None,
            VerificationJob.status == "Completed"
        )
        .cte("filtered_jobs")
    )

    VJ = aliased(filtered_jobs_cte)

    tray_count_subq = (
        select(func.count())
        .select_from(Tray)
        .where(Tray.verification_job_id == VJ.c.id)
        .correlate(filtered_jobs_cte)
        .scalar_subquery()
    )

    item_count_subq = (
        select(func.count())
        .select_from(Item)
        .where(Item.verification_job_id == VJ.c.id)
        .correlate(filtered_jobs_cte)
        .scalar_subquery()
    )

    non_tray_item_count_subq = (
        select(func.count())
        .select_from(NonTrayItem)
        .where(NonTrayItem.verification_job_id == VJ.c.id)
        .correlate(filtered_jobs_cte)
        .scalar_subquery()
    )

    query = (
        select(
            VJ.c.id,
            VJ.c.workflow_id,
            VJ.c.trayed,
            tray_count_subq.label("tray_count"),
            item_count_subq.label("item_count"),
            non_tray_item_count_subq.label("non_tray_item_count"),
        )
        .order_by(VJ.c.workflow_id.asc())
    )

    # CRITICAL FIX: Paginate now takes only the query object
    return paginate(query)


@router.get("/{id}", response_model=VerificationJobDetailOutput)
def get_verification_job_detail(id: int, session: Session = Depends(get_session)):
    """
    Retrieves the verification job detail for the given ID.
    """
    verification_job = session.get(VerificationJob, id)

    if verification_job:
        return verification_job

    raise NotFound(detail=f"Verification Job ID {id} Not Found")


@router.get("/by-accession-job-id/{id}", response_model=VerificationJobAccCheckOutput)
def get_verification_job_id_by_acc_job_id(id: int, session: Session = Depends(get_session)):
    """
    This is a quick check endpoint to help the front-end determine if
    an Accession Job has been lost in limbo when Verification Job transition
    fails to fire off.
    """
    # V2 FIX: session.exec().first() -> session.execute(select(...)).scalars().first()
    verification_job = session.execute(
        select(VerificationJob).where(VerificationJob.accession_job_id == id)
    ).scalars().first()
    if verification_job:
        return verification_job

    raise NotFound(detail=f"No Verification Job found for Accession Job id {id}")


@router.get("/workflow/{id}", response_model=VerificationJobDetailOutput)
def get_verification_job_detail_by_workflow(
    id: int, session: Session = Depends(get_session)
):
    """
    Retrieves the verification job detail for the given workflow ID.
    """
    # V2 FIX: session.exec().first() -> session.execute(select(...)).scalars().first()
    verification_job = session.execute(
        select(VerificationJob).where(VerificationJob.workflow_id == id)
    ).scalars().first()

    if verification_job:
        return verification_job

    raise NotFound(detail=f"Verification Job ID {id} Not Found")


@router.post("/", response_model=VerificationJobDetailOutput, status_code=201)
def create_verification_job(
    verification_job_input: VerificationJobInput,
    session: Session = Depends(get_session),
):
    """
    Create a new verification job:
    """
    try:
        verification_job = VerificationJob(**verification_job_input.model_dump())

        session.add(verification_job)
        session.commit()
        session.refresh(verification_job)

        return verification_job

    except IntegrityError as e:
        raise ValidationException(detail=f"{e}")


@router.patch("/{id}", response_model=VerificationJobDetailOutput)
def update_verification_job(
    id: int,
    verification_job: VerificationJobUpdateInput,
    session: Session = Depends(get_session),
    background_tasks: BackgroundTasks = None,
):
    """
    Update a verification job:
    """
    try:
        existing_verification_job = session.get(VerificationJob, id)

        # capture original status for process check
        original_status = existing_verification_job.status

        # Check if the tray record exists
        if not existing_verification_job:
            raise NotFound(detail=f"Verification Job ID {id} Not Found")

        # Update the tray record with the mutated data
        mutated_data = verification_job.model_dump(exclude_unset=True)

        for key, value in mutated_data.items():
            if (key in ["media_type_id", "size_class_id"] and
                existing_verification_job.__getattribute__(key) != value):
                audit_info = getattr(session, "audit_info", {"name": "System", "id": "0"}).copy()
                background_tasks.add_task(
                    manage_verification_job_change_action(
                        existing_verification_job,
                        key,
                        value,
                        audit_info=audit_info
                    )
                )

            setattr(existing_verification_job, key, value)

        setattr(existing_verification_job, "update_dt", datetime.now(timezone.utc))

        existing_verification_job = commit_record(session, existing_verification_job)

        if mutated_data.get("status") == "Completed":
            audit_info = getattr(session, "audit_info", {"name": "System", "id": "0"}).copy()
            background_tasks.add_task(
                complete_verification_job,
                existing_verification_job.id,
                audit_info=audit_info
            )
            session.refresh(existing_verification_job)
        else:
            audit_info = getattr(session, "audit_info", {"name": "System", "id": "0"}).copy()
            background_tasks.add_task(
                manage_verification_job_transition,
                existing_verification_job,
                original_status,
                audit_info=audit_info
            )

            session.refresh(existing_verification_job)

        return existing_verification_job

    except Exception as e:
        raise InternalServerError(detail=f"{e}")


@router.delete("/{id}")
def delete_verification_job(id: int, session: Session = Depends(get_session)):
    """
    Delete a verification job by its ID.
    """
    verification_job = session.get(VerificationJob, id)

    if verification_job:
        # do not allow deletion of completed jobs
        if verification_job.status == 'Completed':
            return HTTPException(
                status_code=400,
                detail=f"Verification Job id {id} is complete. Can't delete or cancel completed jobs.",
            )

        # find and reset underlying accession job
        acc_job = session.get(AccessionJob, verification_job.accession_job_id)
        acc_job.status = 'Paused'
        session.add(acc_job)

        if verification_job.container_type_id == 1:
            # V2 FIX: session.exec().all() -> session.execute(select(...)).scalars().all()
            trays_in_ver_job_query = select(Tray).where(Tray.verification_job_id == id)
            trays = session.execute(trays_in_ver_job_query).scalars().all()
            for tray in trays:
                tray.scanned_for_verification = False
                tray.verification_job_id = None
                tray.collection_verified = False
                session.add(tray)
                # V2 FIX: session.exec().all() -> session.execute(select(...)).scalars().all()
                for item in session.execute(select(Item).where(Item.verification_job_id == id)).scalars().all():
                    item.scanned_for_verification = False
                    item.verification_job_id = None
                    session.add(item)
        else:
            # V2 FIX
            non_trays_in_ver_job_query = select(NonTrayItem).where(
                NonTrayItem.verification_job_id == id
            )
            non_tray_items = session.execute(non_trays_in_ver_job_query).scalars().all()
            for non_tray_item in non_tray_items:
                non_tray_item.scanned_for_verification = False
                non_tray_item.verification_job_id = None
                session.add(non_tray_item)

        try:
            session.commit()
        except Exception as e:
            return HTTPException(
                status_code=500,
                detail=f"{e}",
            )

        session.delete(verification_job)
        session.commit()

        return HTTPException(
            status_code=204,
            detail=f"Verification Job id {id} Deleted Successfully",
        )

    raise NotFound(detail=f"Verification Job ID {id} Not Found")


@router.patch("/{id}/add", response_model=VerificationJobDetailOutput)
def add_item_to_verification_job(
    id: int,
    input: VerificationJobAddInput,
    session: Session = Depends(get_session)
):
    """
    Add an item to a verification job.
    """

    verification_job = session.get(VerificationJob, id)

    if not verification_job:
        raise NotFound(detail=f"Verification Job ID {id} Not Found")

    # V2 FIX: session.query().filter().first() -> session.execute(select(...)).scalars().first()
    barcode = (
        session.execute(select(Barcode).filter(Barcode.value == input.barcode_value))
        .scalars()
        .first()
    )

    if not barcode:
        raise NotFound(detail=f"Barcode with value {input.barcode_value} Not Found")

    # V2 FIX
    tray = session.execute(select(Tray).filter(Tray.barcode_id == barcode.id)).scalars().first()
    # V2 FIX
    item = session.execute(select(Item).filter(Item.barcode_id == barcode.id)).scalars().first()
    # V2 FIX
    non_tray_item = (
        session.execute(select(NonTrayItem).filter(NonTrayItem.barcode_id == barcode.id))
        .scalars()
        .first()
    )

    if not tray and not item and not non_tray_item:
        raise NotFound(detail=f"Item with barcode value {input.barcode_value} Not "
                              f"Found")
    if tray:
        tray.scanned_for_verification = True
        session.add(tray)
        
        new_verification_changes = []
        items = tray.items
        if items:
            for item in items:
                item.scanned_for_verification = True
                session.add(item)
                
                item_barcode = session.get(Barcode, item.barcode_id)
                new_verification_changes.append(VerificationChange(
                    workflow_id=verification_job.workflow_id,
                    tray_barcode_value=barcode.value,
                    item_barcode_value=item_barcode.value,
                    change_type="Added",
                    completed_by_id=verification_job.user_id
                ))
        session.bulk_save_objects(new_verification_changes)
        session.commit()
    elif item:
        item.scanned_for_verification = True
        session.add(item)
        
        # V2 FIX
        tray_barcode = session.execute(select(Barcode).join(Tray, Barcode.id == Tray.barcode_id).filter(Tray.id == item.tray_id)).scalars().first()
        new_verification_change = VerificationChange(
            workflow_id=verification_job.workflow_id,
            tray_barcode_value=tray_barcode.value,
            item_barcode_value=barcode.value,
            change_type="Added",
            completed_by_id=verification_job.user_id
        )
        commit_record(session, new_verification_change)
    else:
        non_tray_item.scanned_for_verification = True
        session.add(non_tray_item)
        
        new_verification_change = VerificationChange(
            workflow_id=verification_job.workflow_id,
            item_barcode_value=barcode.value,
            change_type="Added",
            completed_by_id=verification_job.user_id
        )
        commit_record(session, new_verification_change)

    verification_job.update_dt = datetime.now(timezone.utc)
    session.refresh(verification_job)

    return verification_job


@router.patch("/{id}/remove", response_model=VerificationJobDetailOutput)
def remove_item_from_verification_job(
    id: int,
    input: VerificationJobRemoveInput,
    session: Session = Depends(get_session)
):
    """
    Remove an item from a verification job.
    """
    verification_job = session.get(VerificationJob, id)

    if not verification_job:
        raise NotFound(detail=f"Verification Job ID {id} Not Found")

    # V2 FIX
    barcode = (
        session.execute(select(Barcode).filter(Barcode.value == input.barcode_value))
        .scalars()
        .first()
    )

    if not barcode:
        raise NotFound(detail=f"Barcode with value {input.barcode_value} Not Found")

    # V2 FIX
    tray = session.execute(select(Tray).filter(Tray.barcode_id == barcode.id)).scalars().first()
    # V2 FIX
    item = session.execute(select(Item).filter(Item.barcode_id == barcode.id)).scalars().first()
    # V2 FIX
    non_tray_item = (
        session.execute(select(NonTrayItem).filter(NonTrayItem.barcode_id == barcode.id))
        .scalars()
        .first()
    )

    if not tray and not item and not non_tray_item:
        raise NotFound(detail=f"Item with barcode value {input.barcode_value} Not "
                              f"Found")
    if tray:
        tray.scanned_for_verification = False
        session.add(tray)
        
        new_verification_changes = []
        items = tray.items
        if items:
            for item in items:
                item.scanned_for_verification = False
                session.add(item)
                
                item_barcode = session.get(Barcode, item.barcode_id)
                new_verification_changes.append(VerificationChange(
                    workflow_id=verification_job.workflow_id,
                    tray_barcode_value=barcode.value,
                    item_barcode_value=item_barcode.value,
                    change_type="Removed",
                    completed_by_id=verification_job.user_id
                ))
        session.bulk_save_objects(new_verification_changes)
        session.commit()
    elif item:
        item.scanned_for_verification = False
        session.add(item)
        
        # V2 FIX
        tray_barcode = session.execute(select(Barcode).join(Tray, Barcode.id == Tray.barcode_id).filter(Tray.id == item.tray_id)).scalars().first()
        new_verification_change = VerificationChange(
            workflow_id=verification_job.workflow_id,
            tray_barcode_value=tray_barcode.value,
            item_barcode_value=barcode.value,
            change_type="Removed",
            completed_by_id=verification_job.user_id
        )
        commit_record(session, new_verification_change)
    else:
        non_tray_item.scanned_for_verification = False
        session.add(non_tray_item)
        
        new_verification_change = VerificationChange(
            workflow_id=verification_job.workflow_id,
            item_barcode_value=barcode.value,
            change_type="Removed",
            completed_by_id=verification_job.user_id
        )
        commit_record(session, new_verification_change)

    verification_job.update_dt = datetime.now(timezone.utc)
    session.refresh(verification_job)

    return verification_job
