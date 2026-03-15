import requests

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

# If auth is required for PATCH /api/files, update the token below
AUTH_TOKEN = "your_auth_token_here"  # Replace with a valid token


def test_patch_api_files_update_metadata_success():
    headers = {
        "Authorization": f"Bearer {AUTH_TOKEN}",
        "Content-Type": "application/json",
    }

    created_file_id = None
    created_file_name = "testfile_patch_update.txt"
    updated_file_name = "updated_testfile_patch_update.txt"

    # Step 1: Upload a file to create a resource for updating
    files = {
        "file": (created_file_name, b"dummy content for patch update test", "application/octet-stream")
    }
    try:
        upload_response = requests.post(
            f"{BASE_URL}/api/upload",
            headers={},  # No auth required to upload
            files=files,
            timeout=TIMEOUT,
        )
        assert upload_response.status_code == 200, \
            f"Unexpected upload status code: {upload_response.status_code}"
        upload_json = upload_response.json()
        # Check required fields directly in upload_json
        required_fields = ["id", "telegram_file_id", "size_bytes"]
        for field in required_fields:
            assert field in upload_json, f"Upload response missing required field: {field}"
        created_file_id = upload_json["id"]

        # Step 2: Patch /api/files to update metadata (rename file)
        patch_body = {
            "id": created_file_id,
            "name": updated_file_name
        }
        patch_response = requests.patch(
            f"{BASE_URL}/api/files",
            headers=headers,
            json=patch_body,
            timeout=TIMEOUT,
        )
        assert patch_response.status_code == 200, f"Unexpected patch status code: {patch_response.status_code}"
        patch_json = patch_response.json()
        # According to PRD, patch response is updated file metadata JSON, not wrapped in 'file'
        assert isinstance(patch_json, dict), "Patch response is not a JSON object"
        assert "id" in patch_json and patch_json["id"] == created_file_id, "Patch response file id mismatch"
        assert "name" in patch_json and patch_json["name"] == updated_file_name, "Patch response file name mismatch"

    finally:
        # Cleanup: delete the file if API for deletion is available
        # If delete endpoint exists, attempt cleanup
        if created_file_id:
            try:
                del_headers = {
                    "Authorization": f"Bearer {AUTH_TOKEN}",
                }
                requests.delete(
                    f"{BASE_URL}/api/files/{created_file_id}",
                    headers=del_headers,
                    timeout=TIMEOUT,
                )
            except Exception:
                pass


test_patch_api_files_update_metadata_success()
