import requests
import uuid

def test_post_chunked_upload_init_with_authorization_success():
    base_url = "http://localhost:3000"
    url = f"{base_url}/api/upload/init"
    timeout = 30

    # Normally authorization token would be obtained from a login or fixture.
    # For this test, assume a valid JWT token is provided here.
    # Replace 'your_valid_jwt_token' with an actual valid token for real testing.
    auth_token = "your_valid_jwt_token"

    headers = {
        "Authorization": f"Bearer {auth_token}",
        "Content-Type": "application/json"
    }

    payload = {
        "fileName": "testfile.txt",
        "fileSize": 123456,
        "mimeType": "text/plain",
        "totalChunks": 5,
        # guestSessionId as a UUID string, simulate a guest session
        "guestSessionId": str(uuid.uuid4())
    }

    try:
        response = requests.post(url, json=payload, headers=headers, timeout=timeout)
    except requests.RequestException as e:
        assert False, f"Request failed: {e}"

    assert response.status_code == 200, f"Expected status code 200, got {response.status_code}"

    try:
        data = response.json()
    except ValueError:
        assert False, "Response is not valid JSON"

    # Validate response includes 'uploadId' and 'chunkEndpoint'
    assert "uploadId" in data, "Response JSON missing 'uploadId'"
    assert "chunkEndpoint" in data, "Response JSON missing 'chunkEndpoint'"

test_post_chunked_upload_init_with_authorization_success()