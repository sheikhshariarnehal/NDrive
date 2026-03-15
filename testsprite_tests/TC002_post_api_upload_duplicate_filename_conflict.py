import requests
import io

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

def test_post_api_upload_duplicate_filename_conflict():
    guest_session_id = "test-guest-session-duplicate"
    filename = "duplicate_test_file.txt"
    file_content = b"Test file content for duplicate filename conflict."

    headers = {}
    files = {"file": (filename, io.BytesIO(file_content))}
    data = {"guest_session_id": guest_session_id}

    created_file_id = None

    # Upload the first file - expected to succeed with 200 or 201 and file metadata
    try:
        resp1 = requests.post(
            f"{BASE_URL}/api/upload",
            files=files,
            data=data,
            headers=headers,
            timeout=TIMEOUT,
        )
        # Validate first upload success response
        assert resp1.status_code in (200, 201), f"First upload status code unexpected: {resp1.status_code}"
        json_resp1 = resp1.json()
        assert "file" in json_resp1, "Response JSON missing 'file' key"
        file_meta = json_resp1["file"]
        assert all(k in file_meta for k in ("id", "telegram_file_id", "size_bytes")), "File metadata missing keys"
        created_file_id = file_meta["id"]
    except Exception as e:
        raise AssertionError(f"Setup upload failed: {e}")

    # Upload the second file with same filename and guest_session_id - expect rejection 409 or 500
    files = {"file": (filename, io.BytesIO(file_content))}
    try:
        resp2 = requests.post(
            f"{BASE_URL}/api/upload",
            files=files,
            data=data,
            headers=headers,
            timeout=TIMEOUT,
        )
    except Exception as e:
        raise AssertionError(f"Second upload request failed: {e}")

    # Validate second upload failure for duplicate filename
    if resp2.status_code == 409:
        # Expected conflict
        pass
    elif resp2.status_code == 500:
        # Acceptable error if server error indicates duplicate handling problem
        pass
    else:
        raise AssertionError(
            f"Second upload expected 409 or 500 but got {resp2.status_code} with body: {resp2.text}"
        )


# Run the test

test_post_api_upload_duplicate_filename_conflict()
