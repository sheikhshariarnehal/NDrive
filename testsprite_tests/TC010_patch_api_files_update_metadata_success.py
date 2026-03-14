import requests

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

def test_patch_api_files_update_metadata_success():
    # Step 1: Upload a file to get a file ID
    upload_url = f"{BASE_URL}/api/upload"
    files = {'file': ('testfile.txt', b'Test file content')}
    data = {'guest_session_id': 'test-guest-session-id'}
    response_upload = requests.post(upload_url, files=files, data=data, timeout=TIMEOUT)
    assert response_upload.status_code in [200, 201], f"Upload failed: {response_upload.status_code} {response_upload.text}"
    upload_json = response_upload.json()
    assert 'file' in upload_json and 'id' in upload_json['file'], "Upload response missing file id"
    file_id = upload_json['file']['id']

    # Prepare patch payload to rename the file
    patch_url = f"{BASE_URL}/api/files"
    new_name = "renamed_testfile.txt"
    headers = {"Content-Type": "application/json"}
    headers["Authorization"] = "Bearer dummy-valid-token"

    patch_payload = {
        "id": file_id,
        "name": new_name
    }

    try:
        response_patch = requests.patch(patch_url, json=patch_payload, headers=headers, timeout=TIMEOUT)
        assert response_patch.status_code == 200, f"Patch failed: {response_patch.status_code} {response_patch.text}"
        patch_json = response_patch.json()
        assert "file" in patch_json and isinstance(patch_json["file"], dict), "Patch response missing file object"
        assert patch_json["file"].get("id") == file_id, "File ID mismatch in patch response"
        assert patch_json["file"].get("name") == new_name, "File name not updated correctly"
    finally:
        pass

test_patch_api_files_update_metadata_success()