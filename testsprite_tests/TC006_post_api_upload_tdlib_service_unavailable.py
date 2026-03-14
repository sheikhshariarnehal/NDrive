import requests

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

def test_post_api_upload_without_file_should_return_400_with_clear_validation_error():
    url = f"{BASE_URL}/api/upload"
    try:
        response = requests.post(url, files={}, timeout=TIMEOUT)
    except requests.RequestException as e:
        assert False, f"Request to {url} failed with exception: {e}"

    assert response.status_code == 400, f"Expected status 400, got {response.status_code}"

    try:
        json_data = response.json()
    except ValueError:
        assert False, "Response is not valid JSON"

    # The error response should be a clear validation error without extra confusing data
    # Check JSON has keys related to validation error, typically an 'error' or 'message' key
    error_keys = {'error', 'message', 'details', 'validationErrors'}
    assert any(key in json_data for key in error_keys), "Response JSON does not include clear validation error keys"

    # Assert that the response JSON does not include a file or other success data keys
    assert 'file' not in json_data, "Response JSON should not include 'file' key on validation error"


test_post_api_upload_without_file_should_return_400_with_clear_validation_error()