from sqlalchemy import create_engine, text
from app.config.config import get_settings

settings = get_settings()
engine = create_engine(settings.MIGRATION_URL)

try:
    with engine.connect() as conn:
        result = conn.execute(text("SELECT typname FROM pg_type WHERE typtype = 'e'"))
        print("\n=== Enum types found ===")
        for row in result:
            print(row[0])
        print("========================\n")
except Exception as e:
    print(f"Error connecting: {e}")
