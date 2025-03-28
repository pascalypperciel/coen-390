import datetime

session_id = -1 # This is the sessionid I added in the db dedicated to this

def test_post_batch_valid(client):
    # Good, valid payload
    payload = [
        {
            "Distance": 10.5,
            "Temperature": 36.7,
            "Pressure": 101.3,
            "SessionID": session_id,
            "Timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            "Valid": True
        },
        {
            "Distance": 20.1,
            "Temperature": 38.0,
            "Pressure": 102.2,
            "SessionID": session_id,
            "Timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            "Valid": False
        }
    ]
    response = client.post("/batch-process-records", json=payload)
    assert response.status_code == 201 # expect success
    data = response.get_json()
    assert data["received_count"] == len(payload)
    assert data["inserted_count"] == len(payload)
    assert data["message"] == "Records inserted successfully"

def test_post_batch_empty_payload(client):
    # Empty payload, it shouldn't made anything crash
    payload = []
    response = client.post("/batch-process-records", json=payload)
    assert response.status_code == 201 # expect success
    data = response.get_json()
    assert data["received_count"] == len(payload)
    assert data["inserted_count"] == len(payload)
    assert data["message"] == "Records inserted successfully"

def test_post_batch_missing_field(client):
    # Wrong, missing field (temperature here)
    payload = [
        {
            "Distance": 10.5,
            "Pressure": 101.3,
            "SessionID": session_id,
            "Timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            "Valid": True
        }
    ]
    response = client.post("/batch-process-records", json=payload)
    assert response.status_code == 400 # expect bad request
    data = response.get_json()
    assert data["error"] == "Missing required field(s)"

def test_post_batch_data_type(client):
    # Wrong, wrong data type (temperature here)
    payload = [
        {
            "Distance": 10.5,
            "Temperature": "This should break",
            "Pressure": 101.3,
            "SessionID": session_id,
            "Timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            "Valid": True
        }
    ]
    response = client.post("/batch-process-records", json=payload)
    assert response.status_code == 400 # expect bad request
    data = response.get_json()
    assert "Invalid data type in record" in data["error"]

def test_post_batch_invalid_timestamp(client):
    # Wrong, wrong timestamp format
    payload = [
        {
            "Distance": 10.5,
            "Temperature": 36.7,
            "Pressure": 101.3,
            "SessionID": session_id,
            "Timestamp": "Not a valid date format",
            "Valid": True
        }
    ]
    response = client.post("/batch-process-records", json=payload)
    assert response.status_code == 400 # expect bad request
    data = response.get_json()
    assert "Invalid data type in record" in data["error"]