import sys
import os
# Add the project root to sys.path
sys.path.append(os.getcwd())

from sqlalchemy import select, text
from app.database.session import session_manager
from app.models.audit_trails import AuditTrail
from app.models.shelving_jobs import ShelvingJob
from app.models.trays import Tray

def debug_history():
    with session_manager() as session:
        # 1. Find a recent ShelvingJob that is completed or has progress
        stmt = select(ShelvingJob).order_by(ShelvingJob.id.desc()).limit(1)
        job = session.execute(stmt).scalars().first()
        
        if not job:
            print("No ShelvingJob found.")
            return

        print(f"Checking ShelvingJob ID: {job.id}, Status: {job.status}")
        
        # 2. Check Trays associated with this job
        trays = job.trays
        print(f"Found {len(trays)} trays associated with this job.")
        
        if not trays:
            print("No trays found for this job. Trying to find a job with trays...")
            # Try to find a job with trays
            stmt = select(ShelvingJob).join(Tray).order_by(ShelvingJob.id.desc()).limit(1)
            job = session.execute(stmt).scalars().first()
            if job:
                print(f"Found better job ID: {job.id} with trays.")
                trays = job.trays
                print(f"Trays count: {len(trays)}")
            else:
                print("Could not find any ShelvingJob with trays.")
                return

        # 3. For each tray, fetch Audit Logs
        for tray in trays:
            print(f"--- Tray ID: {tray.id} ---")
            
            # Query used in get_audit_trails_detail_list
            query = select(AuditTrail).where(AuditTrail.table_name == "trays").where(
                AuditTrail.record_id == str(tray.id)).where(
                AuditTrail.updated_at >= job.create_dt
            )
            
            logs = session.execute(query).scalars().all()
            print(f"Audit Logs found (since job create_dt {job.create_dt}): {len(logs)}")
            
            for log in logs:
                print(f"Log ID: {log.id}, Table: {log.table_name}, Action: {log.operation_type}")
                print(f"New Values: {log.new_values}")
                
                # Check logic from add_last_action
                if "scanned_for_shelving" in (log.new_values or {}):
                    print("  -> MATCH: scanned_for_shelving found!")
                if "shelf_position_id" in (log.new_values or {}):
                    print("  -> MATCH: shelf_position_id found!")

        # 4. Check if we have any logs for 'shelving_jobs' table
        print(f"--- Checking 'shelving_jobs' shelf logs ---")
        query_jobs = select(AuditTrail).where(AuditTrail.table_name == "shelving_jobs").where(
            AuditTrail.record_id == str(job.id)
        )
        job_logs = session.execute(query_jobs).scalars().all()
        print(f"ShelvingJob Logs found: {len(job_logs)}")
        for log in job_logs:
             print(f"Log ID: {log.id}, New Values: {log.new_values}")

if __name__ == "__main__":
    debug_history()
