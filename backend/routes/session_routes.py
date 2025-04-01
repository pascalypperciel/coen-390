from flask import request, jsonify
from app import app, get_db_connection
import numpy as np
import matplotlib.pyplot as plt
import io
import base64
import math

## Routes

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
        
        # Return success response
        return jsonify({"Graph": graphs_list, "message": "Graphs generated successfully"}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500  # Internal Server Error


## Helpers

def create_graphs(distances, weights, temperatures, timestamps, session_id, initial_length, initial_area, offset):
    graphs_list = []

    # Displacements and strain/stress calculations
    displacements = np.array([i + distances[0] for i in distances])
    engineering_strain = displacements / initial_length
    force_N = np.array([(i * 9.81) / 1000 for i in weights])
    engineering_stress = force_N / initial_area
    avg_temperature = np.mean(temperatures)
    time_seconds = [(ts - timestamps[0]).total_seconds() for ts in timestamps]

    # --- Displacement vs Force Plot ---
    fig, ax = plt.subplots()
    ax.plot(displacements, force_N, 'o-', label="Displacement vs Force")
    ax.text(0.95, 0.05, f"Avg Temp: {avg_temperature:.2f}°C", transform=ax.transAxes, fontsize=10, verticalalignment='bottom', horizontalalignment='right', bbox=dict(facecolor='white', alpha=0.5))
    ax.text(0.05, 0.95, f"Session ID: {session_id}", transform=ax.transAxes, fontsize=12, color='red', ha='left', va='top')
    ax.set_xlabel("Displacement (cm)")
    ax.set_ylabel("Force (N)")
    ax.legend()

    # Save to buffer and encode to Base64
    img_buf = io.BytesIO()
    plt.savefig(img_buf, format='png')
    img_buf.seek(0)
    displacement_force = base64.b64encode(img_buf.read()).decode('utf-8')
    plt.close()

    graphs_list.append(displacement_force)

    # --- Stress vs Strain Plot ---

    test_sample_size = int(math.floor(len(engineering_strain)/10))

    strain_subset = engineering_strain[:test_sample_size]
    stress_subset = engineering_stress[:test_sample_size]
    slope, intercept = np.polyfit(strain_subset, stress_subset, 1)

    actual_offset = offset * (strain_subset.max() - strain_subset.min())
    offset_intercept = intercept + actual_offset

    range = np.linspace(0, engineering_strain.max(), len(engineering_stress))

    offset_line = slope * (range - offset_intercept)

    fig, ax = plt.subplots()
    ax.plot(engineering_strain, engineering_stress, 'o-', label="Stress vs Strain")
    ax.plot(range, offset_line, '--k', label="Offset Line")

    ax.text(0.95, 0.05, f"Avg Temp: {avg_temperature:.2f}°C", transform=ax.transAxes, fontsize=10, verticalalignment='bottom', horizontalalignment='right', bbox=dict(facecolor='white', alpha=0.5))
    ax.text(0.05, 0.95, f"Session ID: {session_id}", transform=ax.transAxes, fontsize=12, color='red', ha='left', va='top')
    ax.set_xlabel("Strain")
    ax.set_ylabel("Stress (Pa)")
    ax.legend()

    img_buf = io.BytesIO()
    plt.savefig(img_buf, format='png')
    img_buf.seek(0)
    stress_strain = base64.b64encode(img_buf.read()).decode('utf-8')
    plt.close()

    graphs_list.append(stress_strain)

    # --- Load vs Time Plot ---
    fig, ax = plt.subplots()
    ax.plot(time_seconds, force_N, 'o-', label="Load vs Time", color='g')
    ax.set_xlabel("Time (sec)")
    ax.set_ylabel("Force (N)")
    ax.legend()

    img_buf = io.BytesIO()
    plt.savefig(img_buf, format='png')
    img_buf.seek(0)
    load_time = base64.b64encode(img_buf.read()).decode('utf-8')
    plt.close()

    graphs_list.append(load_time)

    # --- Displacement vs Time Plot ---
    fig, ax = plt.subplots()
    ax.plot(time_seconds, displacements, 'o-', label="Displacement vs Time", color='b')
    ax.set_xlabel("Time (sec)")
    ax.set_ylabel("Displacement (cm)")
    ax.legend()

    img_buf = io.BytesIO()
    plt.savefig(img_buf, format='png')
    img_buf.seek(0)
    displacement_time = base64.b64encode(img_buf.read()).decode('utf-8')
    plt.close()

    graphs_list.append(displacement_time)

    return graphs_list