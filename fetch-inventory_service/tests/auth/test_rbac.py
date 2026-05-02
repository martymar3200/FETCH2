import pytest
from httpx import AsyncClient

# The 7 core workflow routes we need to test
# Tuple format: (route_path, required_permission, method)
PROTECTED_ROUTES = [
    ("/accession-jobs/", "can_access_accession", "GET"),
    ("/verification-jobs/", "can_access_verification", "GET"),
    ("/shelving-jobs/", "can_create_and_execute_shelving_job", "GET"),
    ("/pick-lists/", "can_create_picklist_job", "GET"),
    ("/shipping-jobs/", "can_access_shipping", "GET"),
    ("/refile-queue/", "can_add_refile_item_to_queue", "GET"),
    ("/withdraw-jobs/", "can_access_withdraw", "GET"),
]

def test_endpoints_reject_unauthenticated_requests(client):
    """
    Test that making a request without a token to protected endpoints returns 401 Unauthorized.
    """
    from app.main import app as fastapi_app
    from app.auth.dependencies import get_current_user_with_permissions
    
    # Temporarily remove global mock to test unauthenticated state
    global_mock = fastapi_app.dependency_overrides.pop(get_current_user_with_permissions, None)
    
    try:
        for route, _, method in PROTECTED_ROUTES:
            response = client.request(method, route)
            assert response.status_code == 401, f"Expected 401 for {route}, got {response.status_code}"
    finally:
        if global_mock:
            fastapi_app.dependency_overrides[get_current_user_with_permissions] = global_mock

def test_endpoints_reject_unauthorized_users(client):
    """
    Test that a valid user missing the specific permission gets a 403 Forbidden.
    """
    from app.main import app as fastapi_app
    from app.auth.dependencies import get_current_user_with_permissions
    
    # Override auth to return a user with NO permissions
    class MockUser:
        id = 1
        first_name = "NoPerm"
        last_name = "User"
        username = "noperm"
        groups = []

    def _mock_no_perms():
        return MockUser()

    # Temporarily remove or overwrite global mock
    global_mock = fastapi_app.dependency_overrides.get(get_current_user_with_permissions)
    fastapi_app.dependency_overrides[get_current_user_with_permissions] = _mock_no_perms

    try:
        for route, _, method in PROTECTED_ROUTES:
            response = client.request(method, route)
            assert response.status_code == 403, f"Expected 403 for {route}, got {response.status_code}. Response: {response.text}"
    finally:
        if global_mock:
            fastapi_app.dependency_overrides[get_current_user_with_permissions] = global_mock
        elif get_current_user_with_permissions in fastapi_app.dependency_overrides:
            del fastapi_app.dependency_overrides[get_current_user_with_permissions]

def test_endpoints_allow_authorized_users(client):
    """
    Test that making a request with a valid token and correct permissions allows access.
    (It might return 404/500 depending on DB state, but NOT 401 or 403).
    """
    from app.main import app as fastapi_app
    from app.auth.dependencies import get_current_user_with_permissions
    
    global_mock = fastapi_app.dependency_overrides.get(get_current_user_with_permissions)
    
    try:
        for route, required_permission, method in PROTECTED_ROUTES:
            class RequiredPermissionObj:
                def __init__(self, name):
                    self.name = name
                    
            class MockGroup:
                permissions = [RequiredPermissionObj(required_permission)]
                
            class MockUser:
                id = 1
                first_name = "Auth"
                last_name = "User"
                username = "auth"
                groups = [MockGroup()]

            def _mock_with_specific_perm():
                return MockUser()

            fastapi_app.dependency_overrides[get_current_user_with_permissions] = _mock_with_specific_perm

            response = client.request(method, route)
            assert response.status_code not in [401, 403], f"Expected success for {route}, got {response.status_code}. Response: {response.text}"
    finally:
        if global_mock:
            fastapi_app.dependency_overrides[get_current_user_with_permissions] = global_mock
        elif get_current_user_with_permissions in fastapi_app.dependency_overrides:
            del fastapi_app.dependency_overrides[get_current_user_with_permissions]
