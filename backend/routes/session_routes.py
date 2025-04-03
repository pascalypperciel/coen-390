from flask import request, jsonify
from app import app, get_db_connection
import psycopg2.errors

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


# Route to delete a session from the db
@app.route("/delete-session", methods=["POST"])
def delete_session():
    try:
        data = request.json
        session_id = data.get("SessionID")
        
        if not (session_id):
            return jsonify({"error": "SessionID is required"}), 400  # Bad request

        conn = get_db_connection()
        cur = conn.cursor()

        query = """
            DELETE FROM Session
            WHERE SessionID = %s;
        """

        cur.execute(query, (session_id,))
        conn.commit()

        deleted = cur.rowcount

        cur.close()
        conn.close()
        
        if deleted == 0:
            return jsonify({"error": "Session not found"}), 404 # not found

        return jsonify({"message": "Session deleted successfully"}), 200 # ok
    
    except (psycopg2.errors.InvalidTextRepresentation, psycopg2.DataError) as e:
        return jsonify({"error": "Invalid input syntax or data type"}), 400
    except Exception as e:
        return jsonify({"error": str(e)}), 500 # internal server error


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

        return jsonify({"message": "Session inserted successfully", "SessionID": inserted_id}), 201 # created
    
    except (psycopg2.errors.InvalidTextRepresentation, psycopg2.DataError) as e:
        return jsonify({"error": "Invalid input syntax or data type"}), 400
    except Exception as e:
        return jsonify({"error": str(e)}), 500 # internal server error