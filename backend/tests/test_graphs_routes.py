from datetime import datetime, timedelta

session_id = -1 # This is the sessionid I added in the db dedicated to this
session_id_variance = -999999999999999901 # Used for a specific test
session_id_temp = 12345678912345 # Dummy test session for testing purposes, will be removed after use
wrong_session_id = -999999999999999999 # This is sessionid isn't in db. (never add it)


def test_get_build_graph(client):
    # Good, valid
    response = client.get(f"/build-graphs?SessionID={session_id}")
    assert response.status_code == 200
    data = response.get_json()
    assert data["message"] == "Graphs generated successfully"


def test_get_build_graph_invalid_sessionid(client):
    # Bad, SessionID doesn't exist
    response = client.get(f"/build-graphs?SessionID={wrong_session_id}")
    assert response.status_code == 404
    data = response.get_json()
    assert data["error"] == "No valid records found for this session"


def test_get_build_graph_missing_param(client):
    # Bad, SessionID wasn't added to the URL
    response = client.get(f"/build-graphs")
    assert response.status_code == 400
    data = response.get_json()
    assert data["error"] == "SessionID is required"

def test_get_build_graphs_zero_variance_stress(client):
    response = client.get(f"/build-graphs?SessionID={session_id_variance}")
    assert response.status_code == 200
    data = response.get_json()
    assert data["message"] == "Graphs generated successfully"
    assert len(data["Graph"]) == 4
