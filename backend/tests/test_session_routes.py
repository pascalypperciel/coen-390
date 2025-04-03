from app import app, get_db_connection

session_id = -1 # This is the sessionid I added in the db dedicated to this
session_id_temp = 12345678912345 # Dummy test session for testing purposes, will be removed after use
wrong_session_id = -999999999999999999 # This is sessionid isn't in db. (never add it)


def test_get_all_sessions(client):
    # Can we recover all the sessions?
    response = client.get("/get-all-sessions")
    assert response.status_code == 200


def test_post_initial_session_missing_param(client):
    # Bad, missing parameter in the request body (missing InitialArea)
    payload = {
            "SessionID": session_id_temp,
            "SessionName": "This session was generated automatically by pytests",
            "InitialLength": 100
    }
    response = client.post("/initial-session-info", json=payload)
    assert response.status_code == 400 # expect failure
    data = response.get_json()
    assert data["error"] == "All fields are required"


def test_post_initial_session_bad_type(client):
    # Bad, bad data type (SessionID should be int8/bigint)
    payload = {
            "SessionID": "This shouldn't work",
            "SessionName": "This session was generated automatically by pytests",
            "InitialLength": 100,
            "InitialArea": 100
    }
    response = client.post("/initial-session-info", json=payload)
    assert response.status_code == 400 # expect failure
    data = response.get_json()
    assert data["error"] == "Invalid input syntax or data type"


def test_post_initial_session(client):
    # Good, valid payload
    payload = {
        "SessionID": session_id_temp,
        "SessionName": "This session was generated automatically by pytests",
        "InitialLength": 100,
        "InitialArea": 100
    }
    response = client.post("/initial-session-info", json=payload)
    assert response.status_code == 201 # expect success
    data = response.get_json()
    assert data["message"] == "Session inserted successfully"
    assert data["SessionID"] == session_id_temp


def test_post_delete_session(client):
    # Good, delete session we created in previous test
    payload = {
        "SessionID": session_id_temp
    }
    response = client.post("/delete-session", json=payload)
    assert response.status_code == 200 # expect success
    data = response.get_json()
    assert data["message"] == "Session deleted successfully"


def test_post_delete_empty_payload(client):
    # Bad, empty payload
    payload = {}
    response = client.post("/delete-session", json=payload)
    assert response.status_code == 400 # expect failure
    data = response.get_json()
    assert data["error"] == "SessionID is required"


def test_post_delete_bad_type(client):
    # Bad, SessionID wrong format
    payload = {
        "SessionID": "This shouldn't work"
    }
    response = client.post("/delete-session", json=payload)
    assert response.status_code == 400 # expect failure
    data = response.get_json()
    assert data["error"] == "Invalid input syntax or data type"


def test_post_delete_unfound(client):
    # Bad, SessionID isn't in database
    payload = {
        "SessionID": wrong_session_id
    }
    response = client.post("/delete-session", json=payload)
    assert response.status_code == 404 # expect failure
    data = response.get_json()
    assert data["error"] == "Session not found"