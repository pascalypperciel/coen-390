from flask import request, jsonify
from app import app, get_db_connection

from graph_generator import create_graphs

@app.route("/build-graphs", methods=["GET"])
def build_graphs():
    try:
        session_id = request.args.get("SessionID")
        
        offset = 0.05

        if not session_id:
            return jsonify({"error": "Session Name is required"}), 400  # Bad request   
       
        conn = get_db_connection()
        cur = conn.cursor()

        select_query = """
            SELECT RecordID, Distance, Temperature, Pressure, "Timestamp", SessionID, Valid FROM "Record"
            WHERE SessionID = %s AND Valid = TRUE
            ORDER BY "Timestamp" ASC;
        """
        cur.execute(select_query, (session_id,))
        records = cur.fetchall()
        # cur.close()
        # conn.close()

        # cur = conn.cursor()
        # conn = get_db_connection()


        select_query2 = """
            SELECT InitialLength, InitialArea FROM Session 
            WHERE SessionID = %s;
        """
        cur.execute(select_query2, (session_id,))
        (initial_length, initial_area) = cur.fetchall()[0]

        cur.close()
        conn.close()

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

        if not records:
            return jsonify({"error": "No valid records found for this session"}), 404  # Not found
        # Extract relevant data
        distances = [row[1] for row in records]  # row[1] corresponds to Distance
        pressures = [row[3] for row in records]  # row[3] corresponds to Pressure
        temperatures = [row[2] for row in records]  # row[2] corresponds to Temperature
        timestamps = [row[4] for row in records]  # row[5] corresponds to Timestamp
        graphs_list = create_graphs(distances, pressures, temperatures, timestamps, session_id, initial_length, initial_area, offset)
        print("Here3")
        # Return success response
        return jsonify({"Graph": graphs_list, "message": "Graphs generated successfully"}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500  # Internal Server Error
    

# Route to request all test data
@app.route("/get-all-sessions", methods=["GET"])
def get_all_sessions():
    try:
        conn = get_db_connection()
        cur = conn.cursor()

        select_query = """
            SELECT * FROM Session
            ORDER BY SessionID DESC;
        """

        cur.execute(select_query)
        records = cur.fetchall()

        # If you want to return the records as a list of dictionaries, map the column names
        columns = [desc[0] for desc in cur.description]  # Get column names from cursor description
        result = [dict(zip(columns, record)) for record in records]

        cur.close()
        conn.close()

        return jsonify({"list of tests": result}), 200  # ok
    except Exception as e:
        return jsonify({"error": str(e)}), 500  # internal server error


@app.route("/initial-session-info", methods=["POST"])
def initial_session_info():
    try:
        data = request.json

        session_id = data.get("SessionID")
        session_name = data.get("SessionName")
        initial_length = data.get("InitialLength")
        initial_area = data.get("InitialArea")

        if not all([session_id, session_name, initial_length, initial_area]):
            return jsonify({"error": "All fields are required"}), 400  # Bad request
        
        conn = get_db_connection()
        cur = conn.cursor()

        insert_query = """
            INSERT INTO Session (SessionID, SessionName, InitialLength, InitialArea)
            VALUES (%s, %s, %s, %s)
            RETURNING SessionID;
        """

        cur.execute(insert_query, (session_id, session_name, initial_length, initial_area))
        inserted_id = cur.fetchone()[0]
        conn.commit()
        cur.close()
        conn.close()

        return jsonify({"message": "Test inserted successfully", "SessionID": inserted_id}), 201 # created
    except Exception as e:
        return jsonify({"error": str(e)}), 500 # internal server error