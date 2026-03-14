import requests

def test_post_api_upload_without_file_should_return_400():
    base_url = "http://localhost:3000"
    url = f"{base_url}/api/upload"

    # Send POST request without any file
    try:
        response = requests.post(url, files={}, timeout=30)
    except requests.RequestException as e:
        assert False, f"Request failed: {e}"

    # Validate status code is 400 Bad Request for validation error
    assert response.status_code == 400, f"Expected status 400, got {response.status_code}"

    # Validate response body contains clear validation error message
    # Assuming response is JSON and contains an 'error' or similar field
    try:
        json_data = response.json()
    except ValueError:
        assert False, "Response is not valid JSON"

    # Check presence of validation error or meaningful message
    # Accept keys like 'error', 'message', or similar
    error_fields = ['error', 'message', 'detail', 'validationError']
    has_error_message = any(field in json_data and json_data[field] for field in error_fields)
    assert has_error_message, f"Response JSON does not contain validation error message. Response: {json_data}"

test_post_api_upload_without_file_should_return_400()