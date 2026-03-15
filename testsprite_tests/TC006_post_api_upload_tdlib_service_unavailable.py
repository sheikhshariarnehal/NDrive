import requests

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

def test_post_api_upload_tdlib_service_unavailable():
    url = f"{BASE_URL}/api/upload"
    headers = {
        # No Authorization or guest_session_id as per description
    }
    # No file sent to test validation error branch
    try:
        response = requests.post(url, headers=headers, timeout=TIMEOUT)
    except requests.RequestException as e:
        assert False, f"Request to {url} failed with exception: {e}"

    # Validate status code 400 for missing file (validation error)
    assert response.status_code == 400, f"Expected HTTP 400, got {response.status_code}"

    # Validate error message presence (assumed JSON with 'error' key or similar validation message)
    try:
        json_data = response.json()
    except ValueError:
        assert False, f"Response is not JSON: {response.text}"

    # Check for clear validation error indication
    assert "error" in json_data or "message" in json_data, "Response JSON must contain error or message field indicating validation error"

test_post_api_upload_tdlib_service_unavailable()