from fastapi import Request, Depends, HTTPException, status
from sqlalchemy.orm import Session
from sqlalchemy import select
from sqlalchemy.orm import selectinload
import jwt

from app.database.session import get_session
from app.models.users import User
from app.config.config import get_settings

from app.models.groups import Group

def get_current_user_with_permissions(request: Request, session: Session = Depends(get_session)) -> User:
    # First check for the HttpOnly secure cookie
    token = request.cookies.get("fetch_auth_token")
    
    # Fallback to Authorization header for API compatibility/testing
    if not token:
        auth_header = request.headers.get("Authorization")
        if not auth_header or not auth_header.startswith("Bearer "):
            raise HTTPException(status_code=401, detail="Missing Authorization Token")
        token = auth_header.split("Bearer ")[1]

    try:
        decoded_token = jwt.decode(token, get_settings().SECRET_KEY, algorithms=['HS256'])
        email = decoded_token.get('email')
    except Exception:
        raise HTTPException(status_code=401, detail="Invalid Token")

    # query user with groups AND permissions loaded eagerly
    query = select(User).where(User.email == email).options(
        selectinload(User.groups).selectinload(Group.permissions)
    )
    user = session.execute(query).scalars().first()

    if not user:
        raise HTTPException(status_code=401, detail="User not found")
        
    return user


class RequiresPermission:
    def __init__(self, permission_code: str):
        self.permission_code = permission_code

    def __call__(self, user: User = Depends(get_current_user_with_permissions)):
        # Flatten permissions
        user_permissions = set()
        
        for group in user.groups:
            # Permissions are now eager loaded, so this won't trigger IO
            for permission in group.permissions:
                user_permissions.add(permission.name)
                
        if self.permission_code not in user_permissions:
             raise HTTPException(
                status_code=403, 
                detail=f"Permission denied: Missing '{self.permission_code}'"
            )
