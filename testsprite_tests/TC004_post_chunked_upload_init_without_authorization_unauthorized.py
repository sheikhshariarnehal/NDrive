import requests

def test_post_chunked_upload_init_without_authorization_unauthorized():
    base_url = "http://localhost:3000"
    url = f"{base_url}/chunked-upload/init"
    headers = {
        "Content-Type": "application/json"
    }
    payload = {}

    try:
        response = requests.post(url, json=payload, headers=headers, timeout=30)
    except requests.RequestException as e:
        assert False, f"Request failed: {e}"

    assert response.status_code == 401, f"Expected status 401 Unauthorized but got {response.status_code}"

    try:
        body = response.json()
        assert "error" in body or "message" in body, "Expected error message key in response"
    except Exception:
        pass

test_post_chunked_upload_init_without_authorization_unauthorized()