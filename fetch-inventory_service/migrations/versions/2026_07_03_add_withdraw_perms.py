"""add withdraw permissions

Revision ID: 2026_07_03_add_withdraw_perms
Revises: 2026_05_04_scheduled_exports
Create Date: 2026-07-03 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '2026_07_03_add_withdraw_perms'
down_revision: Union[str, None] = '2026_05_04_scheduled_exports'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.execute("INSERT INTO permissions (name, description, create_dt, update_dt) VALUES ('create_withdraw_jobs', 'Can create withdraw jobs.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ON CONFLICT (name) DO NOTHING;")
    op.execute("INSERT INTO permissions (name, description, create_dt, update_dt) VALUES ('process_withdraw_jobs', 'Can process (scan items into) withdraw jobs.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ON CONFLICT (name) DO NOTHING;")
    op.execute("INSERT INTO permissions (name, description, create_dt, update_dt) VALUES ('delete_withdraw_jobs', 'Can delete/cancel withdraw jobs.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ON CONFLICT (name) DO NOTHING;")


def downgrade() -> None:
    op.execute("DELETE FROM permissions WHERE name IN ('create_withdraw_jobs', 'process_withdraw_jobs', 'delete_withdraw_jobs');")
