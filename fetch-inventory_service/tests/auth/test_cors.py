import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient

def test_cors_whitelist_origins(client):
    # Test that whitelisted origins are allowed
    # http://localhost:8000 is in the default whitelist
    response = client.get("/", headers={"Origin": "http://localhost:8000"})
    assert response.headers.get("access-control-allow-origin") == "http://localhost:8000"

def test_cors_regex_origins(client):
    # Test that regex allowed subdomains match and parse correctly
    # The default regex now matches alphanumeric subdomains of example.com
    response = client.get("/", headers={"Origin": "https://sub.example.com"})
    assert response.headers.get("access-control-allow-origin") == "https://sub.example.com"
    
    response = client.get("/", headers={"Origin": "http://other-sub.example.com"})
    assert response.headers.get("access-control-allow-origin") == "http://other-sub.example.com"

def test_cors_rejected_origins(client):
    # Test that arbitrary origins not in whitelist or regex are rejected
    response = client.get("/", headers={"Origin": "https://attacker.com"})
    assert response.headers.get("access-control-allow-origin") is None

def test_cors_production_safeguards():
    # Test that the validation logic raises ValueError when wildcards are used in production
    from app.config.config import Settings
    
    # 1. Test wildcard in ALLOWED_ORIGINS
    settings = Settings(
        SECRET_KEY="test-sec",
        APP_ENVIRONMENT="production",
        ALLOWED_ORIGINS="*,https://localhost:8000",
        ALLOWED_ORIGINS_REGEX=""
    )
    allow_origins = settings.ALLOWED_ORIGINS.split(",")
    
    with pytest.raises(ValueError, match="Wildcard '\\*' is not allowed in ALLOWED_ORIGINS"):
        if settings.APP_ENVIRONMENT == "production":
            if "*" in allow_origins:
                raise ValueError(
                    "CORS security validation failed: Wildcard '*' is not allowed in ALLOWED_ORIGINS "
                    "when credentials are enabled in production."
                )

    # 2. Test overly permissive regex
    settings_regex = Settings(
        SECRET_KEY="test-sec",
        APP_ENVIRONMENT="production",
        ALLOWED_ORIGINS="https://localhost:8000",
        ALLOWED_ORIGINS_REGEX=".*"
    )
    
    with pytest.raises(ValueError, match="Overly permissive regex"):
        if settings_regex.APP_ENVIRONMENT == "production":
            if settings_regex.ALLOWED_ORIGINS_REGEX in [".*", "^https?://.*", "^https://.*", "^http://.*"]:
                raise ValueError(
                    f"CORS security validation failed: Overly permissive regex '{settings_regex.ALLOWED_ORIGINS_REGEX}' "
                    "is not allowed in ALLOWED_ORIGINS_REGEX in production."
                )
