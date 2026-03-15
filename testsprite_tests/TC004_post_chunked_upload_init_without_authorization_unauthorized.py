import requests

def test_post_chunked_upload_init_without_authorization_unauthorized():
    base_url = "http://localhost:3000"
    url = f"{base_url}/chunked-upload/init"
    headers = {
        "Content-Type": "application/json"
    }
    payload = {}

    try:
        response = requests.post(url, json=payload, headers=headers, timeout=30)
    except requests.RequestException as e:
        assert False, f"Request failed: {e}"

    assert response.status_code == 401, f"Expected status 401 Unauthorized but got {response.status_code}"
    
    try:
        json_resp = response.json()
    except Exception:
        json_resp = {}

    err_msg = json_resp.get("error") or json_resp.get("message") or ""
    assert isinstance(err_msg, str) and "unauthorized" in err_msg.lower(), f"Expected unauthorized error message, got: {err_msg}"

test_post_chunked_upload_init_without_authorization_unauthorized()
