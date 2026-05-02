-- Migration: Standardize job assignment field names
-- Date: 2026-02-07
-- Description: Rename user_id to assigned_user_id for consistency across all job types
-- Tables affected: accession_jobs, verification_jobs, shelving_jobs, pick_lists

BEGIN;

-- 1. Accession Jobs
ALTER TABLE accession_jobs 
  RENAME COLUMN user_id TO assigned_user_id;

-- 2. Verification Jobs
ALTER TABLE verification_jobs 
  RENAME COLUMN user_id TO assigned_user_id;

-- 3. Shelving Jobs
ALTER TABLE shelving_jobs 
  RENAME COLUMN user_id TO assigned_user_id;

-- 4. Pick Lists
ALTER TABLE pick_lists 
  RENAME COLUMN user_id TO assigned_user_id;

-- 5. Drop old indexes if they exist
DROP INDEX IF EXISTS idx_accession_jobs_user_id;
DROP INDEX IF EXISTS idx_verification_jobs_user_id;
DROP INDEX IF EXISTS idx_shelving_jobs_user_id;
DROP INDEX IF EXISTS idx_pick_lists_user_id;

-- 6. Create new optimized indexes for job assignment queries
-- These partial indexes exclude completed jobs for better performance
CREATE INDEX idx_accession_jobs_assigned_user_status 
  ON accession_jobs(assigned_user_id, status) 
  WHERE status != 'Completed';

CREATE INDEX idx_verification_jobs_assigned_user_status 
  ON verification_jobs(assigned_user_id, status) 
  WHERE status != 'Completed';

CREATE INDEX idx_shelving_jobs_assigned_user_status 
  ON shelving_jobs(assigned_user_id, status) 
  WHERE status != 'Completed';

CREATE INDEX idx_pick_lists_assigned_user_status 
  ON pick_lists(assigned_user_id, status) 
  WHERE status != 'Completed';

-- 7. Create additional composite indexes for common queries
CREATE INDEX idx_shelving_jobs_composite 
  ON shelving_jobs(status, building_id, assigned_user_id);

COMMIT;

-- Verification queries (run after migration)
-- SELECT column_name, data_type FROM information_schema.columns 
-- WHERE table_name IN ('accession_jobs', 'verification_jobs', 'shelving_jobs', 'pick_lists')
--   AND column_name IN ('user_id', 'assigned_user_id');
-- Expected: Only assigned_user_id should be returned, not user_id
