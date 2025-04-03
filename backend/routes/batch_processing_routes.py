from datetime import datetime
from flask import request, jsonify
from app import app, get_db_connection
from psycopg2.extras import execute_values

# Route to send batched records data to database 
@app.route("/batch-process-records", methods=["POST"])
def batch_process_records():
    try:
        data = request.get_json()

        if isinstance(data, dict):
            data = [data] # dictionary to list when we only receive 1 record
        if not isinstance(data, list):
            return jsonify({"error": "Expected a JSON of a list of records"}), 400 # bad request
        
        records = []
        for record in data:
            if not all(key in record for key in ["Distance", "Temperature", "Pressure", "SessionID", "Timestamp", "Valid"]):
                return jsonify({"error": "Missing required field(s)"}), 400 # bad request
            
            try:
                distance = float(record["Distance"])
                temperature = float(record["Temperature"])
                pressure = float(record["Pressure"])
                session_id = int(record["SessionID"])
                
                parsed_timestamp = datetime.fromisoformat(record["Timestamp"])
                timestamp_str = parsed_timestamp.strftime("%Y-%m-%d %H:%M:%S.%f")[:-3] 
                
                valid = bool(record["Valid"])
            except (ValueError, TypeError) as e:
                return jsonify({"error": f"Invalid data type in record: {str(e)}"}), 400

            records.append((distance, temperature, pressure, session_id, timestamp_str, valid))

        conn = get_db_connection()
        cur = conn.cursor()
        
        insert_query = """
            INSERT INTO "Record" (Distance, Temperature, Pressure, SessionID, "Timestamp", Valid)
            VALUES %s
            RETURNING RecordID;
        """
        execute_values(cur, insert_query, records) # bulk insertion (batch processing)

        conn.commit()
        cur.close()
        conn.close()

        return jsonify({"message": "Records inserted successfully", "received_count": len(data), "inserted_count": len(records)}), 201 # created
    except Exception as e: # pragma: no cover
        return jsonify({"error": str(e)}), 500 # internal server error pragma: no cover

# Route to request data from a whole session (batch)
@app.route("/request-session-records", methods=["GET"])
def request_session_records():
    try:
        session_id = request.args.get("SessionID")

        if not session_id:
            return jsonify({"error": "SessionID is required"}), 400 # bad request
        
        conn = get_db_connection()
        cur = conn.cursor()

        select_query = """
            SELECT RecordID, Distance, Temperature, Pressure, "Timestamp", SessionID, Valid FROM "Record"
            WHERE SessionID = %s AND Valid = TRUE
            ORDER BY "Timestamp" ASC;
        """

        cur.execute(select_query, (session_id,))
        records = cur.fetchall()
        cur.close()
        conn.close()

        if not records:
            return jsonify({"error": "This SessionID doesn't exist or has no records"}), 400 # bad request
        
        result = []
        for row in records:
            result.append({
                "RecordID": row[0],
                "Distance": row[1],
                "Temperature": row[2],
                "Pressure": row[3],
                "Timestamp": row[4],
                "SessionID": row[5],
                "Valid": row[6]
            })

        return jsonify({"records": result}), 200 # ok
    except Exception as e: # pragma: no cover
        return jsonify({"error": str(e)}), 500 # internal server error pragma: no cover