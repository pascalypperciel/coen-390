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
            if not all(key in record for key in ["Distance", "Temperature", "Pressure", "MaterialID", "SessionID", "Timestamp", "Valid"]):
                return jsonify({"error": "Missing required field(s)"}), 400 # bad request
            
            records.append((
                record["Distance"],
                record["Temperature"],
                record["Pressure"],
                record["MaterialID"],
                record["SessionID"],
                record["Timestamp"],
                record["Valid"]
            ))

        conn = get_db_connection()
        cur = conn.cursor()
        
        insert_query = """
            INSERT INTO "Record" (Distance, Temperature, Pressure, MaterialID, SessionID, "Timestamp", Valid)
            VALUES %s
            RETURNING RecordID;
        """
        execute_values(cur, insert_query, records) # bulk insertion (batch processing)

        conn.commit()
        cur.close()
        conn.close()

        return jsonify({"message": "Records inserted successfully", "received_count": len(data), "inserted_count": len(records)}), 201 # created
    except Exception as e:
        return jsonify({"error": str(e)}), 500 # internal server error

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
            SELECT RecordID, Distance, Temperature, Pressure, MaterialID, "Timestamp", SessionID, Valid FROM "Record"
            WHERE SessionID = %s AND Valid = TRUE
            ORDER BY "Timestamp" ASC;
        """

        cur.execute(select_query, (session_id,))
        records = cur.fetchall()
        cur.close()
        conn.close()

        result = []
        for row in records:
            result.append({
                "RecordID": row[0],
                "Distance": row[1],
                "Temperature": row[2],
                "Pressure": row[3],
                "MaterialID": row[4],
                "Timestamp": row[5],
                "SessionID": row[6],
                "Valid": row[7]
            })

        return jsonify({"records": result}), 200 # ok
    except Exception as e:
        return jsonify({"error": str(e)}), 500 # internal server error