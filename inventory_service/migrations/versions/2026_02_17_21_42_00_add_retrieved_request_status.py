"""add retrieved request status

Revision ID: 2026_02_17_21_42_00
Revises: 2026_02_17_15_01_14
Create Date: 2026-02-17 21:42:00.000000

"""
from typing import Sequence, Union

from alembic import op

# revision identifiers, used by Alembic.
revision: str = '2026_02_17_21_42_00'
down_revision: Union[str, None] = '2026_02_17_15_01_14'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # Add 'Retrieved' value to the request_status PostgreSQL enum type
    op.execute("ALTER TYPE request_status ADD VALUE IF NOT EXISTS 'Retrieved'")


def downgrade() -> None:
    # PostgreSQL does not support removing values from enum types directly.
    # A full migration would require recreating the type, which is complex.
    # This is intentionally left as a no-op.
    pass
