import os
import re

# Path to your migrations directory
MIGRATIONS_DIR = os.path.join(os.getcwd(), 'migrations', 'versions')

def process_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    
    # 1. Remove 'import sqlmodel' lines
    content = re.sub(r'^import sqlmodel\s*\n?', '', content, flags=re.MULTILINE)
    content = re.sub(r'^from sqlmodel import .*\n?', '', content, flags=re.MULTILINE)

    # 2. Replace SQLModel types with SQLAlchemy types (assuming 'import sqlalchemy as sa' exists)
    # AutoString -> sa.String
    content = content.replace('sqlmodel.sql.sqltypes.AutoString', 'sa.String')
    content = content.replace('sqlmodel.AutoString', 'sa.String')
    
    # GUID -> sa.UUID (or sa.String if you prefer, but sa.UUID is likely what you want for Postgres)
    # Note: Alembic usually handles UUIDs via sqlalchemy.dialects.postgresql or sa.UUID
    content = content.replace('sqlmodel.sql.sqltypes.GUID', 'sa.UUID')
    content = content.replace('sqlmodel.GUID', 'sa.UUID')
    
    # Boolean -> sa.Boolean
    content = content.replace('sqlmodel.sql.sqltypes.Boolean', 'sa.Boolean')
    
    # Integers
    content = content.replace('sqlmodel.sql.sqltypes.Integer', 'sa.Integer')
    
    if content != original_content:
        print(f"Fixed: {os.path.basename(file_path)}")
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)

def main():
    if not os.path.exists(MIGRATIONS_DIR):
        print(f"Directory not found: {MIGRATIONS_DIR}")
        return

    print(f"Scanning {MIGRATIONS_DIR}...")
    for filename in os.listdir(MIGRATIONS_DIR):
        if filename.endswith(".py"):
            process_file(os.path.join(MIGRATIONS_DIR, filename))
    print("Done.")

if __name__ == "__main__":
    main()