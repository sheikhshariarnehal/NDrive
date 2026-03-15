import requests
import uuid

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

def test_get_api_files_list_with_authorization_success():
    files_upload_url = f"{BASE_URL}/api/upload"
    files_list_url = f"{BASE_URL}/api/files"

    guest_session_id = str(uuid.uuid4())
    file_content = b"Test file content for TC009"
    file_name = "testfile_tc009.txt"

    # Step 1: Upload a file with guest_session_id expecting 201 and response { file: ... }
    upload_response = None
    try:
        files = {
            'file': (file_name, file_content),
        }
        data = {'guest_session_id': guest_session_id}
        upload_response = requests.post(files_upload_url, files=files, data=data, timeout=TIMEOUT)
        assert upload_response.status_code == 201, f"Upload failed with status {upload_response.status_code}"
        upload_json = upload_response.json()
        assert "file" in upload_json and isinstance(upload_json["file"], dict), "Upload response missing 'file' object"
        file_id = upload_json["file"].get("id")
        assert file_id is not None, "Uploaded file object missing 'id'"

        # Step 2: Call GET /api/files with guest_session_id query parameter; no auth token required
        params = {"guest_session_id": guest_session_id}
        list_response = requests.get(files_list_url, params=params, timeout=TIMEOUT)
        assert list_response.status_code == 200, f"Files list request failed with status {list_response.status_code}"
        list_json = list_response.json()
        assert "files" in list_json and isinstance(list_json["files"], list), "Response missing 'files' array"

        # Optionally verify the uploaded file is present in the files list by id
        file_ids = [f.get("id") for f in list_json["files"] if isinstance(f, dict) and "id" in f]
        assert file_id in file_ids, "Uploaded file id not found in files list"

    finally:
        # Cleanup: delete the uploaded file if possible
        if upload_response is not None and upload_response.status_code == 201:
            file_id_cleanup = upload_response.json().get("file", {}).get("id")
            if file_id_cleanup:
                try:
                    delete_url = f"{BASE_URL}/api/files"
                    # DELETE method not explicitly specified, so assuming PATCH with a 'deleted' flag or similar is unsupported.
                    # No delete endpoint provided in PRD; skip delete if no explicit delete available.
                except Exception:
                    pass

test_get_api_files_list_with_authorization_success()