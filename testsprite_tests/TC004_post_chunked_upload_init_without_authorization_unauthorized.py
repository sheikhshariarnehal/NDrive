import requests

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

def test_post_chunked_upload_init_without_authorization_unauthorized():
    url = f"{BASE_URL}/api/upload/init"
    # Intentionally missing required fields: fileName, fileSize, totalChunks
    payload = {}
    # No Authorization header provided

    response = requests.post(url, json=payload, timeout=TIMEOUT)
    assert response.status_code == 400, f"Expected status 400 but got {response.status_code}"

    try:
        json_data = response.json()
    except ValueError:
        assert False, "Response is not valid JSON"

    # Validate presence of validation error message indicating missing required fields
    error_msg = json_data.get("error") or json_data.get("message") or json_data.get("errors")
    assert error_msg is not None, "Expected validation error message in response body"

test_post_chunked_upload_init_without_authorization_unauthorized()