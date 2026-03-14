import requests

def test_post_chunked_upload_init_with_authorization_success():
    base_url = "http://localhost:3000"
    endpoint = "/api/upload/init"
    url = base_url + endpoint

    # Example JWT token placeholder; replace with valid token for actual test
    jwt_token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mocked.token"

    headers = {
        "Authorization": f"Bearer {jwt_token}",
        "Content-Type": "application/json"
    }

    payload = {
        "fileName": "testfile.txt",
        "fileSize": 1024 * 1024 * 5,  # 5 MB
        "mimeType": "text/plain",
        "totalChunks": 10,
        "guestSessionId": "test-guest-session-1234"
    }

    try:
        response = requests.post(url, json=payload, headers=headers, timeout=30)
    except requests.RequestException as e:
        assert False, f"Request failed: {e}"

    assert response.status_code == 200, f"Expected status 200 but got {response.status_code}"

    try:
        data = response.json()
    except ValueError:
        assert False, "Response is not valid JSON"

    assert "uploadId" in data, "Response JSON missing 'uploadId'"
    assert isinstance(data["uploadId"], str) and data["uploadId"], "'uploadId' should be a non-empty string"
    assert "chunkEndpoint" in data, "Response JSON missing 'chunkEndpoint'"
    assert isinstance(data["chunkEndpoint"], str) and data["chunkEndpoint"], "'chunkEndpoint' should be a non-empty string"

test_post_chunked_upload_init_with_authorization_success()