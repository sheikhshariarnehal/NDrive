
# TestSprite AI Testing Report(MCP)

---

## 1️⃣ Document Metadata
- **Project Name:** cloudvault
- **Date:** 2026-03-15
- **Prepared by:** TestSprite AI Team

---

## 2️⃣ Requirement Validation Summary

#### Test TC001 post api upload file upload success
- **Test Code:** [TC001_post_api_upload_file_upload_success.py](./TC001_post_api_upload_file_upload_success.py)
- **Test Visualization and Result:** https://www.testsprite.com/dashboard/mcp/tests/e33a7e0c-7776-4ab2-bbd1-e91340bb35cc/f2e29484-4d85-4094-8ebc-5db5ed78b2eb
- **Status:** ✅ Passed
- **Analysis / Findings:** {{TODO:AI_ANALYSIS}}.
---

#### Test TC002 post api upload duplicate filename conflict
- **Test Code:** [TC002_post_api_upload_duplicate_filename_conflict.py](./TC002_post_api_upload_duplicate_filename_conflict.py)
- **Test Error:** Traceback (most recent call last):
  File "/var/task/handler.py", line 258, in run_with_retry
    exec(code, exec_env)
  File "<string>", line 65, in <module>
  File "<string>", line 58, in test_post_api_upload_duplicate_filename_conflict
AssertionError: Second upload expected 409 or 500 but got 201 with body: {"file":{"id":"1098b9bb-2b59-4f65-8b3a-b0c1ee7fedd9","user_id":null,"guest_session_id":"test-guest-session-duplicate","folder_id":null,"name":"duplicate_test_file.txt","original_name":"duplicate_test_file.txt","mime_type":"text/plain","size_bytes":50,"telegram_file_id":"BQACAgUAAyEGAATmNjzOAAIRS2m210TklxuYFp0uvndFM_mIlNREAAJOHAACcxe4VbjQmdjmUAZ-OgQ","telegram_message_id":4642045952,"thumbnail_url":null,"is_starred":false,"is_trashed":false,"trashed_at":null,"created_at":"2026-03-15T15:59:00.194943+00:00","updated_at":"2026-03-15T15:59:00.194943+00:00","tdlib_file_id":19,"file_hash":null,"storage_type":"bot","telegram_chat_id":-1003862314190}}

- **Test Visualization and Result:** https://www.testsprite.com/dashboard/mcp/tests/e33a7e0c-7776-4ab2-bbd1-e91340bb35cc/73024d92-2494-48ef-9b13-0aa00b95f184
- **Status:** ❌ Failed
- **Analysis / Findings:** {{TODO:AI_ANALYSIS}}.
---

#### Test TC003 post chunked upload init with authorization success
- **Test Code:** [TC003_post_chunked_upload_init_with_authorization_success.py](./TC003_post_chunked_upload_init_with_authorization_success.py)
- **Test Visualization and Result:** https://www.testsprite.com/dashboard/mcp/tests/e33a7e0c-7776-4ab2-bbd1-e91340bb35cc/8d87fb3f-900e-4b05-92c4-51b823a30115
- **Status:** ✅ Passed
- **Analysis / Findings:** {{TODO:AI_ANALYSIS}}.
---

#### Test TC004 post chunked upload init without authorization unauthorized
- **Test Code:** [TC004_post_chunked_upload_init_without_authorization_unauthorized.py](./TC004_post_chunked_upload_init_without_authorization_unauthorized.py)
- **Test Error:** Traceback (most recent call last):
  File "/var/task/handler.py", line 258, in run_with_retry
    exec(code, exec_env)
  File "<string>", line 26, in <module>
  File "<string>", line 16, in test_post_chunked_upload_init_without_authorization_unauthorized
AssertionError: Expected status 401 Unauthorized but got 404

- **Test Visualization and Result:** https://www.testsprite.com/dashboard/mcp/tests/e33a7e0c-7776-4ab2-bbd1-e91340bb35cc/1e1401b0-f256-4482-9314-2eb0f34af5e8
- **Status:** ❌ Failed
- **Analysis / Findings:** {{TODO:AI_ANALYSIS}}.
---

#### Test TC005 post chunked upload complete with authorization success
- **Test Code:** [TC005_post_chunked_upload_complete_with_authorization_success.py](./TC005_post_chunked_upload_complete_with_authorization_success.py)
- **Test Error:** Traceback (most recent call last):
  File "/var/task/handler.py", line 258, in run_with_retry
    exec(code, exec_env)
  File "<string>", line 80, in <module>
  File "<string>", line 32, in test_post_chunked_upload_complete_with_authorization_success
AssertionError: Expected 201 on upload init, got 404

- **Test Visualization and Result:** https://www.testsprite.com/dashboard/mcp/tests/e33a7e0c-7776-4ab2-bbd1-e91340bb35cc/4ea82001-ee4a-410c-b976-f6f58f0a5872
- **Status:** ❌ Failed
- **Analysis / Findings:** {{TODO:AI_ANALYSIS}}.
---

#### Test TC006 post api upload tdlib service unavailable
- **Test Code:** [TC006_post_api_upload_tdlib_service_unavailable.py](./TC006_post_api_upload_tdlib_service_unavailable.py)
- **Test Visualization and Result:** https://www.testsprite.com/dashboard/mcp/tests/e33a7e0c-7776-4ab2-bbd1-e91340bb35cc/514256a1-96f7-4afa-9b91-ec3f7e2075f1
- **Status:** ✅ Passed
- **Analysis / Findings:** {{TODO:AI_ANALYSIS}}.
---

#### Test TC007 get file public stream with valid token
- **Test Code:** [TC007_get_file_public_stream_with_valid_token.py](./TC007_get_file_public_stream_with_valid_token.py)
- **Test Error:** Traceback (most recent call last):
  File "/var/task/handler.py", line 258, in run_with_retry
    exec(code, exec_env)
  File "<string>", line 55, in <module>
  File "<string>", line 21, in test_get_file_public_stream_with_valid_token
AssertionError: Unexpected upload status: 201

- **Test Visualization and Result:** https://www.testsprite.com/dashboard/mcp/tests/e33a7e0c-7776-4ab2-bbd1-e91340bb35cc/52997e7f-3dd7-49b8-9323-8f7f0ad2dfd3
- **Status:** ❌ Failed
- **Analysis / Findings:** {{TODO:AI_ANALYSIS}}.
---

#### Test TC008 get file public stream with range header partial content
- **Test Code:** [TC008_get_file_public_stream_with_range_header_partial_content.py](./TC008_get_file_public_stream_with_range_header_partial_content.py)
- **Test Visualization and Result:** https://www.testsprite.com/dashboard/mcp/tests/e33a7e0c-7776-4ab2-bbd1-e91340bb35cc/4671302a-6afc-48eb-82cd-02614bac7ed9
- **Status:** ✅ Passed
- **Analysis / Findings:** {{TODO:AI_ANALYSIS}}.
---

#### Test TC009 get api files list with authorization success
- **Test Code:** [TC009_get_api_files_list_with_authorization_success.py](./TC009_get_api_files_list_with_authorization_success.py)
- **Test Visualization and Result:** https://www.testsprite.com/dashboard/mcp/tests/e33a7e0c-7776-4ab2-bbd1-e91340bb35cc/3787c198-7537-4c27-840c-733e9ef90120
- **Status:** ✅ Passed
- **Analysis / Findings:** {{TODO:AI_ANALYSIS}}.
---

#### Test TC010 patch api files update metadata success
- **Test Code:** [TC010_patch_api_files_update_metadata_success.py](./TC010_patch_api_files_update_metadata_success.py)
- **Test Error:** Traceback (most recent call last):
  File "/var/task/handler.py", line 258, in run_with_retry
    exec(code, exec_env)
  File "<string>", line 75, in <module>
  File "<string>", line 31, in test_patch_api_files_update_metadata_success
AssertionError: Unexpected upload status code: 400

- **Test Visualization and Result:** https://www.testsprite.com/dashboard/mcp/tests/e33a7e0c-7776-4ab2-bbd1-e91340bb35cc/d7b680bb-84bc-4f85-bd84-70f89138981e
- **Status:** ❌ Failed
- **Analysis / Findings:** {{TODO:AI_ANALYSIS}}.
---


## 3️⃣ Coverage & Matching Metrics

- **50.00** of tests passed

| Requirement        | Total Tests | ✅ Passed | ❌ Failed  |
|--------------------|-------------|-----------|------------|
| ...                | ...         | ...       | ...        |
---


## 4️⃣ Key Gaps / Risks
{AI_GNERATED_KET_GAPS_AND_RISKS}
---