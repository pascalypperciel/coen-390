from flask import request, jsonify
from app import app, get_db_connection
import numpy as np
import matplotlib.pyplot as plt
import io
import base64
import psycopg2.errors

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

#find the best linear interval(of defined window_size length) in the data for strainand stress
def find_best_interval(x_data, y_data):
    # Sliding window size
    window_size = 10

    # Loop through the data with a sliding window and calculate the R^2 for each window
    best_r2 = -np.inf
    best_interval = (0, 0)
    best_m=0
    best_b=0

    for start in range(len(x_data) - window_size + 1):
        end = start + window_size
        x_subset = x_data[start:end]
        y_subset = y_data[start:end]
    
        # Perform linear regression on the subset
        #m, b = linear_regression(x_subset, y_subset)
        n = len(x_subset)
        x_mean = np.mean(x_subset)
        y_mean = np.mean(y_subset)
        numerator = np.sum((x_subset - x_mean) * (y_subset - y_mean))
        denominator = np.sum((x_subset - x_mean) ** 2)
        m = numerator / denominator  # Slope
        b = y_mean - m * x_mean     # Intercept

   
        # Calculate R^2 value for the regression line
        y_pred = m * x_subset + b
        ss_total = np.sum((y_subset - np.mean(y_subset)) ** 2)
        ss_residual = np.sum((y_subset - y_pred) ** 2)
        if ss_total == 0:
            r2 = -np.inf
        else:
            r2 = 1 - (ss_residual / ss_total)  # R-squared
    
        if r2 >= best_r2:
            best_r2 = r2
            best_interval = (start, end)
            best_m=m
            best_b=b

    return best_interval, best_m, best_b
    
#create the 4 different graphs

def create_graphs(distances, weights, temperatures, timestamps, session_id, initial_length, initial_area, offset):
    graphs_list = []

    # Displacements and strain/stress calculations
    displacements = np.array([i + distances[0] for i in distances])
    engr_strain = displacements / initial_length
    force_N = np.array([(i * 9.81) / 1000 for i in weights])
    engr_stress = force_N / initial_area
    avg_temperature = np.mean(temperatures)
    time_seconds = [(ts - timestamps[0]).total_seconds() for ts in timestamps]

    # --- Displacement vs Force Plot ---
    fig, ax = plt.subplots()
    ax.plot(displacements, force_N, 'o-', label="Displacement vs Force")
    ax.text(0.95, 0.05, f"Avg Temp: {avg_temperature:.2f}°C", transform=ax.transAxes, fontsize=10, verticalalignment='bottom', horizontalalignment='right', bbox=dict(facecolor='white', alpha=0.5))
    ax.text(0.05, 0.95, f"Session ID: {session_id}", transform=ax.transAxes, fontsize=12, color='green', ha='left', va='top')
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

    #get linear interval of dataset
    (lstart, lend), slope, intercept=find_best_interval(engr_strain,engr_stress)

    strain_subset = engr_strain[lstart:lend]
    stress_subset = engr_stress[lstart:lend]

    actual_offset = offset * (strain_subset.max() - strain_subset.min())
    offset_intercept = intercept + actual_offset

    range = np.linspace(0, engr_strain.max(), len(engr_stress))
    offset_line = slope * range + offset_intercept

    fig, ax = plt.subplots()
    ax.plot(engr_strain, engr_stress, 'o-', label="Stress vs Strain")
    ax.plot(range, offset_line, '--k', label="Offset Line")

    ax.scatter(engr_strain[lend-1],engr_stress[lend-1], color='red',s=200)#youngs modulus
    ax.text(0.05, 0.90, f"Young's Modulus: {engr_strain[lend-1]:.5f}, {engr_stress[lend-1]:.5f}", transform=ax.transAxes, fontsize=12, color='purple', ha='left', va='top')

    if slope<0:
        ax.text(0.05, 0.85, f"Invalid Data", transform=ax.transAxes, fontsize=12, color='red', ha='left', va='top')

    ax.text(0.95, 0.05, f"Avg Temp: {avg_temperature:.2f}°C", transform=ax.transAxes, fontsize=10, verticalalignment='bottom', horizontalalignment='right', bbox=dict(facecolor='white', alpha=0.5))
    ax.text(0.05, 0.95, f"Session ID: {session_id}", transform=ax.transAxes, fontsize=12, color='green', ha='left', va='top')
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
    ax.text(0.05, 0.95, f"Session ID: {session_id}", transform=ax.transAxes, fontsize=12, color='green', ha='left', va='top')
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
    ax.text(0.05, 0.95, f"Session ID: {session_id}", transform=ax.transAxes, fontsize=12, color='green', ha='left', va='top')
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
