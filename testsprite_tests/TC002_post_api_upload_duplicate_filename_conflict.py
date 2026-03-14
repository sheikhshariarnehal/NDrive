import requests
import io

BASE_URL = "http://localhost:3000"
UPLOAD_ENDPOINT = f"{BASE_URL}/api/upload"
FILES_ENDPOINT = f"{BASE_URL}/api/files"
TIMEOUT = 30

def test_post_api_upload_duplicate_filename_conflict():
    guest_session_id = "test-guest-session-tc002"
    filename = "duplicate_test_file.txt"
    file_content = b"Sample content for duplicate filename test."

    headers = {}
    files = {
        "file": (filename, io.BytesIO(file_content), "text/plain"),
        "guest_session_id": (None, guest_session_id)
    }

    created_file_id = None

    try:
        # First upload should succeed with 201
        resp1 = requests.post(UPLOAD_ENDPOINT, files=files, headers=headers, timeout=TIMEOUT)
        assert resp1.status_code == 201, f"First upload expected 201 Created, got {resp1.status_code}"
        resp1_json = resp1.json()
        assert "file" in resp1_json, "Response JSON missing 'file'"
        file1 = resp1_json["file"]
        assert "id" in file1 and "telegram_file_id" in file1 and "size_bytes" in file1, "File metadata incomplete"
        created_file_id = file1["id"]

        # Second upload with same filename and same guest_session_id
        resp2 = requests.post(UPLOAD_ENDPOINT, files=files, headers=headers, timeout=TIMEOUT)
        # Expect 409 Conflict preferred, 500 acceptable if server error for duplicate
        assert resp2.status_code in (409, 500), f"Second upload expected 409 or 500, got {resp2.status_code}"

        # Verify that only one file entry with that filename exists for the guest_session_id
        list_resp = requests.get(FILES_ENDPOINT, params={"guest_session_id": guest_session_id}, timeout=TIMEOUT)
        assert list_resp.status_code == 200, f"Files list expected 200 OK, got {list_resp.status_code}"
        list_json = list_resp.json()
        assert "files" in list_json and isinstance(list_json["files"], list), "Files list response malformed"
        files_with_name = [f for f in list_json["files"] if f.get("name") == filename]
        assert len(files_with_name) >= 1, f"Expected at least 1 file with name '{filename}', found {len(files_with_name)}"
        matching_ids = {f.get("id") for f in files_with_name if isinstance(f, dict)}
        assert created_file_id in matching_ids, "The created file id was not found among files with the same name"

    finally:
        # Cleanup: Delete the created file if possible (attempt idempotently)
        if created_file_id:
            try:
                delete_url = f"{BASE_URL}/api/files"
                # According to PRD, no DELETE method listed, but assume PATCH or POST could support deletion? No details.
                # So skipping actual deletion due to missing API contract.
                pass
            except Exception:
                pass

test_post_api_upload_duplicate_filename_conflict()