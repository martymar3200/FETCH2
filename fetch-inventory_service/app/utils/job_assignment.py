"""
Helper functions for job assignment logic.

Provides reusable functions for:
- Auto-assigning jobs to users when they start them
- Updating job status when managers assign users
- Validating assignment permissions
"""

from sqlalchemy.orm import Session
from app.config.exceptions import Forbidden, ValidationException


def auto_assign_on_start(job, new_status: str, current_user_id: int):
    """
    Automatically assign job to current user when they start it.
    
    This allows workers to self-assign jobs from the queue without manager intervention.
    
    Args:
        job: The job object (AccessionJob, VerificationJob, etc.)
        new_status: The new status being set
        current_user_id: ID of the user making the request
        
    Returns:
        The job object (modified in place)
        
    Raises:
        Forbidden: If user tries to start a job assigned to someone else
    """
    if new_status == "Running":
        # Check assignment protection: prevent users from starting jobs assigned to others
        if job.assigned_user_id is not None and job.assigned_user_id != current_user_id:
            raise Forbidden(
                detail=f"This job is assigned to another user (ID: {job.assigned_user_id}). "
                       f"Only the assigned user can start this job."
            )
        
        # Auto-assign unassigned jobs to current user
        if job.assigned_user_id is None:
            job.assigned_user_id = current_user_id
            # Status will go directly from Created → Running
            # (skipping Assigned state for self-service workflow)
    
    return job


def update_status_on_assignment(job, new_assigned_user_id: int = None, current_status: str = None):
    """
    Update job status to 'Assigned' when manager assigns a user.
    
    This is for manager pre-assignment workflow (not self-service).
    
    Args:
        job: The job object
        new_assigned_user_id: The new assigned_user_id value (or None if being unassigned)
        current_status: The current status of the job
        
    Returns:
        The job object (modified in place)
    """
    # Only auto-update status if the job is currently in Created state
    if current_status == "Created":
        if new_assigned_user_id is not None:
            # Manager assigned a user → move to Assigned status
            job.status = "Assigned"
        else:
            # User was unassigned → revert to Created status
            # (This handles the case where assigned_user_id is being set to None)
            pass  # Keep as Created
    
    # If job was already Assigned and user is being changed, keep it as Assigned
    elif current_status == "Assigned":
        if new_assigned_user_id is None:
            # Unassigning → revert to Created
            job.status = "Created"
    
    return job


def validate_status_transition(current_status: str, new_status: str, job_assigned_user_id: int = None, current_user_id: int = None):
    """
    Validate that a status transition is allowed.
    
    Args:
        current_status: Current job status
        new_status: Desired new status
        job_assigned_user_id: User ID currently assigned to the job (or None)
        current_user_id: ID of user making the request
        
    Raises:
        ValidationException: If transition is not allowed
        Forbidden: If user doesn't have permission for this transition
    """
    # Validate Running transition
    if new_status == "Running":
        # Can start from Created, Assigned, or Paused
        if current_status not in ["Created", "Assigned", "Paused"]:
            raise ValidationException(
                detail=f"Cannot transition from {current_status} to Running"
            )
        
        # Assignment protection is handled in auto_assign_on_start
        # We don't validate assigned_user_id here because we auto-assign unassigned jobs
    
    # Validate Assigned transition
    if new_status == "Assigned":
        if job_assigned_user_id is None:
            raise ValidationException(
                detail="Cannot set status to Assigned without assigned_user_id"
            )


def validate_assignment_lock(job, current_user_id: int):
    """
    Validate that the current user is allowed to perform operations on the job.
    
    This acts as a strict lock to prevent users from completing tasks on jobs
    that have been reassigned to someone else while they were working on it.
    
    Args:
        job: The job object to check
        current_user_id: ID of the user attempting the operation
        
    Raises:
        Forbidden: If the job is assigned to someone else
    """
    if job.assigned_user_id is not None and job.assigned_user_id != current_user_id:
        raise Forbidden(
            detail=f"This job is currently assigned to another user (ID: {job.assigned_user_id}). "
                   f"Only the assigned user can perform operations."
        )
