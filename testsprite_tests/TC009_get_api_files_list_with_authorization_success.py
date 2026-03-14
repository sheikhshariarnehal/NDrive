import requests

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

def test_get_api_files_list_with_authorization_success():
    upload_url = f"{BASE_URL}/api/upload"
    files_url = f"{BASE_URL}/api/files"

    # Prepare a small dummy file for upload
    file_content = b"Hello CloudVault"
    file_name = "testfile_tc009.txt"
    guest_session_id = "guest_session_tc009"

    files = {
        "file": (file_name, file_content),
    }
    data = {
        "guest_session_id": guest_session_id
    }

    # Upload the file with guest_session_id - expect 201 and response shape { file: ... }
    response_upload = requests.post(upload_url, files=files, data=data, timeout=TIMEOUT)
    assert response_upload.status_code == 201, f"Expected 201 Created, got {response_upload.status_code}"
    json_upload = response_upload.json()
    assert "file" in json_upload, "Response JSON must include 'file' key"
    file_metadata = json_upload["file"]
    for key in ("id", "telegram_file_id", "size_bytes"):
        assert key in file_metadata, f"File metadata must include '{key}'"

    # Use try-finally to cleanup uploaded file after test
    file_id = file_metadata["id"]
    delete_url = f"{BASE_URL}/api/files"
    try:
        # Call GET /api/files with guest_session_id query (no bearer token) and verify response
        params = {"guest_session_id": guest_session_id}
        response_files = requests.get(files_url, params=params, timeout=TIMEOUT)
        assert response_files.status_code == 200, f"Expected 200 OK, got {response_files.status_code}"
        json_files = response_files.json()
        assert "files" in json_files, "Response JSON must include 'files' key"
        assert isinstance(json_files["files"], list), "'files' must be a list"

        # Confirm uploaded file id is in the files list
        file_ids = [f.get("id") for f in json_files["files"] if isinstance(f, dict)]
        assert file_id in file_ids, "Uploaded file id must be present in files list"
    finally:
        # Cleanup: delete the uploaded file by id (assume DELETE /api/files with JSON payload to delete)
        headers = {"Content-Type": "application/json"}
        resp_delete = requests.delete(delete_url, json={"id": file_id}, timeout=TIMEOUT, headers=headers)
        # We allow 200 or 204 as successful delete, but do not assert here to avoid masking test result
        if resp_delete.status_code not in (200, 204):
            print(f"Warning: failed to delete test file id {file_id} with status {resp_delete.status_code}")

test_get_api_files_list_with_authorization_success()