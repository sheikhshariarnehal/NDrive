import requests
import uuid

BASE_URL = "http://localhost:3000"
TIMEOUT = 30


def test_patch_api_files_update_metadata_success():
    # Step 1: Upload a file (create resource) to get file id for update
    upload_url = f"{BASE_URL}/api/upload"
    files = {'file': ('testfile.txt', b'Initial file content', 'text/plain')}
    try:
        upload_resp = requests.post(upload_url, files=files, timeout=TIMEOUT)
        assert upload_resp.status_code == 200, f"Unexpected upload status: {upload_resp.status_code}"
        upload_json = upload_resp.json()
        file_id = upload_json.get('id')
        original_name = upload_json.get('name', 'testfile.txt')
        assert file_id is not None, "Missing 'id' in uploaded file metadata"

        # Step 2: Patch request to update file metadata (rename)
        patch_url = f"{BASE_URL}/api/files"
        new_name = f"renamed-{uuid.uuid4().hex[:8]}.txt"
        payload = {
            "id": file_id,
            "name": new_name
        }
        patch_resp = requests.patch(patch_url, json=payload, timeout=TIMEOUT)
        assert patch_resp.status_code == 200, f"Unexpected status code: {patch_resp.status_code}"
        patch_json = patch_resp.json()
        # The response contains updated file metadata directly
        assert patch_json.get("id") == file_id, "Returned file id mismatch"
        assert patch_json.get("name") == new_name, "File name not updated correctly"

    finally:
        # Cleanup: delete the created file if possible
        # Assuming a DELETE /api/files endpoint exists for cleanup
        if 'file_id' in locals():
            try:
                del_resp = requests.delete(f"{BASE_URL}/api/files", json={"id": file_id}, timeout=TIMEOUT)
                # ignore errors in cleanup
            except Exception:
                pass


test_patch_api_files_update_metadata_success()
