from flask import request, jsonify
from app import app, get_db_connection

# Route to Get All Data from test_table
@app.route("/get-all", methods=["GET"])
def get_all():
    try:
        conn = get_db_connection()
        cur = conn.cursor()

        cur.execute("SELECT * FROM test_table;")
        rows = cur.fetchall()
        cur.close()
        conn.close()

        return jsonify({"data": rows}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# Route to Add Data to test_table
@app.route("/add-item", methods=["POST"])
def add_item():
    try:
        conn = get_db_connection()
        cur = conn.cursor()

        data = request.json
        name = data.get("name")

        cur.execute("INSERT INTO test_table (name) VALUES (%s) RETURNING id;", (name,))
        inserted_id = cur.fetchone()[0]
        conn.commit()
        cur.close()
        conn.close()

        return jsonify({"message": "Item added successfully", "id": inserted_id}), 201
    except Exception as e:
        return jsonify({"error": str(e)}), 500
