import requests
import uuid

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

def test_post_chunked_upload_init_with_authorization_success():
    # This test initializes a chunked upload session via POST /api/upload/init
    # Requires Authorization header with a Bearer token, so first upload a file to get or simulate a token or guestSessionId

    # For this test, as no JWT generation instructions provided, we will simulate a guestSessionId UUID and use it in body.
    # The endpoint requires Authorization, so we simulate a valid Bearer token (dummy value)
    # According to instructions, chunked flow must use /api/upload/init etc. on port 3000.

    # Setup test data
    file_name = "testfile.txt"
    file_size = 1048576  # 1 MB
    mime_type = "text/plain"
    total_chunks = 10
    guest_session_id = str(uuid.uuid4())
    auth_token = "Bearer dummy-valid-token-for-test"

    url = f"{BASE_URL}/api/upload/init"
    headers = {
        "Authorization": auth_token,
        "Content-Type": "application/json"
    }
    payload = {
        "fileName": file_name,
        "fileSize": file_size,
        "mimeType": mime_type,
        "totalChunks": total_chunks,
        "guestSessionId": guest_session_id
    }

    try:
        response = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)
    except requests.RequestException as e:
        assert False, f"Request to {url} failed with exception: {e}"

    assert response.status_code == 200, f"Expected status 200 but got {response.status_code} with body {response.text}"
    try:
        data = response.json()
    except ValueError:
        assert False, f"Response is not valid JSON: {response.text}"

    # Verify response includes uploadId and chunkEndpoint
    assert "uploadId" in data, "Response JSON missing 'uploadId'"
    assert isinstance(data["uploadId"], str) and data["uploadId"].strip() != "", "'uploadId' must be a non-empty string"

    assert "chunkEndpoint" in data, "Response JSON missing 'chunkEndpoint'"
    assert isinstance(data["chunkEndpoint"], str) and data["chunkEndpoint"].strip() != "", "'chunkEndpoint' must be a non-empty string"


test_post_chunked_upload_init_with_authorization_success()