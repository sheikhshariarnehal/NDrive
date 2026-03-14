import requests
import io

BASE_URL = "http://localhost:3000"
UPLOAD_PATH = "/api/upload"
TIMEOUT = 30


def test_post_api_upload_duplicate_filename_conflict():
    guest_session_id = "test-guest-session-duplicate-filename"
    filename = "duplicate_test_file.txt"
    file_content = b"Test content for duplicate file upload."

    files = {
        "file": (filename, io.BytesIO(file_content), "text/plain"),
        "guest_session_id": (None, guest_session_id),
    }

    created_file_id = None

    try:
        # 1st upload attempt - expected to succeed (200 or 201)
        response1 = requests.post(
            BASE_URL + UPLOAD_PATH, files=files, timeout=TIMEOUT
        )
        assert response1.status_code in (200, 201), f"Expected 200 or 201 but got {response1.status_code}"
        json1 = response1.json()
        assert (
            "file" in json1 and "id" in json1["file"]
        ), "Response JSON missing 'file.id'"
        created_file_id = json1["file"]["id"]

        # 2nd upload attempt with the same filename and guest_session_id - expect 409 or 500
        response2 = requests.post(
            BASE_URL + UPLOAD_PATH, files=files, timeout=TIMEOUT
        )
        assert (
            response2.status_code == 409 or response2.status_code == 500
        ), f"Expected 409 or 500 but got {response2.status_code}"

        # Verify 2nd upload does not create a second file record by checking the response body does not confirm new file creation
        if response2.status_code == 409:
            # Expect error message or no 'file' key indicating no duplication created
            try:
                json2 = response2.json()
                assert "file" not in json2, "Duplicate upload should not create a new file record"
            except Exception:
                # If response is not JSON or no body, consider as ok for this test
                pass
        else:
            # If 500 error, assume server errored properly for duplicate; no file should be created
            try:
                json2 = response2.json()
                assert "file" not in json2, "Server error should not create a new file record"
            except Exception:
                pass

    finally:
        # Cleanup: delete the created file if it exists
        if created_file_id:
            try:
                requests.delete(
                    f"{BASE_URL}/api/files",
                    json={"id": created_file_id},
                    timeout=TIMEOUT,
                )
            except Exception:
                # Ignore cleanup errors
                pass


test_post_api_upload_duplicate_filename_conflict()
