import requests

def test_post_api_upload_file_upload_success():
    base_url = "http://localhost:3000"
    url = f"{base_url}/api/upload"
    guest_session_id = "test-guest-session-12345"
    file_content = b"Sample file content for upload test."
    files = {
        "file": ("testfile.txt", file_content, "text/plain"),
    }
    data = {
        "guest_session_id": guest_session_id
    }
    try:
        response = requests.post(url, files=files, data=data, timeout=30)
        assert response.status_code == 201, f"Expected status 201, got {response.status_code}"
        json_response = response.json()

        assert "file" in json_response, "Response JSON missing 'file' key"
        file_obj = json_response["file"]
        assert "id" in file_obj and file_obj["id"], "'file' missing 'id'"
        assert "telegram_file_id" in file_obj and file_obj["telegram_file_id"], "'file' missing 'telegram_file_id'"
        assert "size_bytes" in file_obj and isinstance(file_obj["size_bytes"], int) and file_obj["size_bytes"] > 0, "'file' missing valid 'size_bytes'"
    finally:
        # Cleanup: attempt to delete uploaded file if id present
        try:
            if 'file_obj' in locals() and "id" in file_obj:
                delete_url = f"{base_url}/api/files"
                # Assuming DELETE /api/files with JSON body {id: file_id} (not specified in PRD, so best effort)
                requests.delete(delete_url, json={"id": file_obj["id"]}, timeout=10)
        except Exception:
            pass

test_post_api_upload_file_upload_success()