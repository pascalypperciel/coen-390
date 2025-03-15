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
        if isinstance(data, list):
            return jsonify({"error": "Expected a JSON of a list of records"}), 400 # bad request
        
        records = []
        for record in data:
            if not all(key in record for key in ["Distance", "Temperature", "Pressure", "MaterialID", "SessionID"]):
                return jsonify({"error": "Missing required field(s)"}), 400 # bad request
            
            records.append((
                record["Distance"],
                record["Temperature"],
                record["Pressure"],
                record["MaterialID"],
                record["SessionID"]
            ))

        conn = get_db_connection()
        cur = conn.cursor()
        
        insert_query = """
            INSERT INTO "Record" (Distance, Temperature, Pressure, MaterialID, SessionID)
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