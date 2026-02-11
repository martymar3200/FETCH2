"""Rename InProgress to PickList

Revision ID: 2026_02_10_13_58_30
Revises: 2026_02_08_11_35_00
Create Date: 2026-02-10 13:58:30.465130

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '2026_02_10_13_58_30'
down_revision: Union[str, None] = '2026_02_08_11_35_00'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # Rename 'InProgress' to 'PickList' in the request_status enum
    # This requires Postgres 10+
    op.execute("ALTER TYPE request_status RENAME VALUE 'InProgress' TO 'PickList'")


def downgrade() -> None:
    # Rename 'PickList' back to 'InProgress'
    op.execute("ALTER TYPE request_status RENAME VALUE 'PickList' TO 'InProgress'")
