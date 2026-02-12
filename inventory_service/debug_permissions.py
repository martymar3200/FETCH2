
import sys
import os
# Add the project root to sys.path
sys.path.append(os.getcwd())

from sqlalchemy import select
from sqlalchemy.orm import selectinload
from app.database.session import session_manager
from app.models.users import User
from app.models.groups import Group
from app.models.permissions import Permission
from app.models.accession_jobs import AccessionJob
from app.models.shelving_jobs import ShelvingJob
from app.models.verification_jobs import VerificationJob
from app.models.pick_lists import PickList
from app.models.refile_jobs import RefileJob
from app.models.withdraw_jobs import WithdrawJob
from app.models.batch_upload import BatchUpload
from app.models.shelving_job_discrepancies import ShelvingJobDiscrepancy
from app.models.verification_changes import VerificationChange



def debug_user_permissions(email):
    print(f"Checking permissions for user: {email}")
    with session_manager() as session:
        # Query user with groups AND permissions loaded eagerly
        query = select(User).where(User.email == email).options(
            selectinload(User.groups).selectinload(Group.permissions)
        )
        user = session.execute(query).scalars().first()

        if not user:
            print(f"User {email} not found!")
            return

        print(f"User ID: {user.id}")
        print(f"Groups: {[g.name for g in user.groups]}")
        
        all_permissions = set()
        for group in user.groups:
            print(f"  Group '{group.name}' has permissions:")
            for perm in group.permissions:
                print(f"    - {perm.code} ({perm.name})")
                all_permissions.add(perm.code)
        
        print("\nEffective Permissions:")
        for p in sorted(all_permissions):
            print(f"  [x] {p}")
            
        if "can_view_audit_logs" in all_permissions:
            print("\nSUCCESS: User HAS 'can_view_audit_logs' permission.")
        else:
            print("\nFAILURE: User MISSING 'can_view_audit_logs' permission.")

if __name__ == "__main__":
    # Replace with the email of the user you are testing with
    # If uncertain, we can list all users or try a default one
    user_email = "admin@fetch.com" # standard default
    if len(sys.argv) > 1:
        user_email = sys.argv[1]
    debug_user_permissions(user_email)
