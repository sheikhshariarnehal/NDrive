import requests

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

def test_get_api_files_list_with_authorization_success():
    # Upload a file with guest_session_id to create guest session context and store file metadata
    upload_url = f"{BASE_URL}/api/upload"
    import uuid
    guest_session_id = str(uuid.uuid4())
    files = {'file': ('testfile.txt', b'Test file content', 'text/plain')}
    data = {'guest_session_id': guest_session_id}

    response_upload = requests.post(upload_url, files=files, data=data, timeout=TIMEOUT)
    assert response_upload.status_code == 201, f"Expected 201 from upload with guest_session_id, got {response_upload.status_code}"
    json_upload = response_upload.json()
    assert "file" in json_upload, "Upload with guest_session_id response missing 'file' key"
    uploaded_file_id = json_upload["file"].get("id")
    assert uploaded_file_id is not None, "Uploaded file ID missing"

    # Now call GET /api/files with guest session query
    get_files_url = f"{BASE_URL}/api/files"
    response_files = requests.get(get_files_url, params={"guest_session_id": guest_session_id}, timeout=TIMEOUT)
    assert response_files.status_code == 200, f"Expected 200 from /api/files with guest_session_id, got {response_files.status_code}"
    files_payload = response_files.json()
    assert "files" in files_payload and isinstance(files_payload["files"], list), "Invalid files list payload"
    listed_ids = [item.get("id") for item in files_payload["files"] if isinstance(item, dict)]
    assert uploaded_file_id in listed_ids, "Uploaded file not found in list response"


test_get_api_files_list_with_authorization_success()
