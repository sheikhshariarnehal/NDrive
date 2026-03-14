import requests
import uuid

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

def test_post_chunked_upload_complete_with_authorization_success():
    headers = {"Content-Type": "application/json"}
    # For this test, guestSessionId is used (no auth token), but test allows userId or guestSessionId.
    guest_session_id = str(uuid.uuid4())
    file_name = "testfile_chunked.txt"
    file_size = 1024 * 10  # 10 KB
    mime_type = "text/plain"
    total_chunks = 2

    upload_id = None

    try:
        # Step 1: POST /api/upload/init to get uploadId and chunkEndpoint
        init_payload = {
            "fileName": file_name,
            "fileSize": file_size,
            "mimeType": mime_type,
            "totalChunks": total_chunks,
            "guestSessionId": guest_session_id
        }
        init_resp = requests.post(
            f"{BASE_URL}/api/upload/init",
            json=init_payload,
            timeout=TIMEOUT
        )
        assert init_resp.status_code == 200, f"Init upload failed with status {init_resp.status_code}"
        init_json = init_resp.json()
        upload_id = init_json.get("uploadId")
        chunk_endpoint = init_json.get("chunkEndpoint")
        assert upload_id and chunk_endpoint, "uploadId or chunkEndpoint missing in init response"

        # Step 2: POST /api/upload/chunk for each chunk
        # Upload chunks with payload fields uploadId and chunkIndex and binary chunk data as raw body
        # We'll send two chunks, each half of file_size

        chunk_data_1 = b"a" * (file_size // total_chunks)
        chunk_data_2 = b"b" * (file_size // total_chunks)

        for chunk_index, chunk_data in enumerate([chunk_data_1, chunk_data_2]):
            chunk_payload = {
                "uploadId": upload_id,
                "chunkIndex": chunk_index
            }
            # POST JSON fields + binary data: Since API expects uploadId and chunkIndex in payload,
            # but chunk data presumably raw body, send JSON params as query string or headers?
            # The PRD states payload fields uploadId and chunkIndex for /api/upload/chunk.
            # We'll send as JSON in request body with chunk data. But chunk data is binary, so send as multipart/form-data?

            # The PRD states to POST /api/upload/chunk, using fields uploadId and chunkIndex
            # The test case description indicates using payload fields uploadId and chunkIndex.
            # We'll send JSON body with uploadId, chunkIndex and chunk as base64? 
            # However, to keep aligned with PRD we send JSON body with uploadId and chunkIndex and raw binary body.

            # For unambiguous implementation here, send JSON body with uploadId and chunkIndex, chunk bytes as the body.
            # Since requests cannot send JSON + binary, we'll use multipart/form-data with fields uploadId, chunkIndex and chunk data.

            multipart_data = {
                "uploadId": (None, upload_id),
                "chunkIndex": (None, str(chunk_index)),
                "chunk": ("chunk", chunk_data, "application/octet-stream")
            }

            chunk_resp = requests.post(
                f"{BASE_URL}/api/upload/chunk",
                files=multipart_data,
                timeout=TIMEOUT
            )
            assert chunk_resp.status_code == 200, f"Chunk upload failed at index {chunk_index} with status {chunk_resp.status_code}"

        # Step 3: POST /api/upload/complete with uploadId,fileName,fileSize,mimeType and guestSessionId
        complete_payload = {
            "uploadId": upload_id,
            "fileName": file_name,
            "fileSize": file_size,
            "mimeType": mime_type,
            "guestSessionId": guest_session_id
        }
        complete_resp = requests.post(
            f"{BASE_URL}/api/upload/complete",
            json=complete_payload,
            timeout=TIMEOUT
        )
        assert complete_resp.status_code == 201, f"Upload complete failed with status {complete_resp.status_code}"
        complete_json = complete_resp.json()
        file_info = complete_json.get("file")
        assert file_info, "Response missing 'file' key"
        assert "id" in file_info and "telegram_file_id" in file_info and "size_bytes" in file_info, "Missing fields in 'file' object"
        assert file_info["size_bytes"] == file_size, "Returned file size does not match uploaded size"
    finally:
        # Cleanup: No explicit resource deletion API described, so if needed can be implemented here
        pass

test_post_chunked_upload_complete_with_authorization_success()