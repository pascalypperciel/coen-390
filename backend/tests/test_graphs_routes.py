from datetime import datetime, timedelta

session_id = -1 # This is the sessionid I added in the db dedicated to this
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
    base_time = datetime.now()
    records = []
    for i in range(20):
        records.append({
            "RecordID": f"rec-{i}",
            "Distance": i * 0.1,
            "Temperature": 25.0,
            "Pressure": 50.0,  # this will always be 50
            "Timestamp": (base_time + timedelta(seconds=i)).isoformat(),
            "SessionID": session_id,
            "Valid": True
        })

    response = client.post("/batch-process-records", json=records)
    assert response.status_code == 201

    response = client.get(f"/build-graphs?SessionID={session_id}")
    
    assert response.status_code == 200
    data = response.get_json()
    assert data["message"] == "Graphs generated successfully"
    assert len(data["Graph"]) == 4
