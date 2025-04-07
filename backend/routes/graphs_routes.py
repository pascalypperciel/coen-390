from flask import request, jsonify
from app import app, get_db_connection
import numpy as np
import matplotlib.pyplot as plt
import io
import base64

@app.route("/build-graphs", methods=["GET"])
def build_graphs():
    try: 
        session_id = request.args.get("SessionID") # this code fragment comes from get-data in batch_processing_routes.py
        
        offset = 0.05 # offset is something we calibrate. Industry standard is 0.02, given a lower precision we need higher offset

        if not session_id:
            return jsonify({"error": "SessionID is required"}), 400  # Bad request   
       
        conn = get_db_connection()
        cur = conn.cursor()

        select_query = """
            SELECT RecordID, Distance, Temperature, Pressure, "Timestamp", SessionID, Valid FROM "Record"
            WHERE SessionID = %s AND Valid = TRUE
            ORDER BY "Timestamp" ASC;
        """
        cur.execute(select_query, (session_id,))
        records = cur.fetchall()
        
        if not records:
            return jsonify({"error": "No valid records found for this session"}), 404  # Not found

        select_query2 = """
            SELECT InitialLength, InitialArea FROM Session 
            WHERE SessionID = %s;
        """ # we need more data this time than just getting a session, since we use the initial length and area in calculations
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

        # extract relevant data
        distances = [row[1] for row in records]  # row[1] corresponds to Distance
        pressures = [row[3] for row in records]  # row[3] corresponds to Pressure
        temperatures = [row[2] for row in records]  # row[2] corresponds to Temperature
        timestamps = [row[4] for row in records]  # row[5] corresponds to Timestamp

        # to make graphs we have a create_graphs function that we call separately, since it's big
        graphs_list = create_graphs(distances, pressures, temperatures, timestamps, session_id, initial_length, initial_area, offset)
        
        # return success response
        return jsonify({"Graph": graphs_list, "message": "Graphs generated successfully"}), 200

    except Exception as e: # pragma: no cover
        return jsonify({"error": str(e)}), 500  # Internal Server Error pragma: no cover


## helpers

# find the best linear interval(of defined window_size length) in the data for strainand stress
def find_best_interval(x_data, y_data):
    # sliding window size
    window_size = 10
    # margin for how much points can deviate from linearity
    margin=20

    # loop through the data with a sliding window and calculate the R^2 for each window
    best_r2 = -np.inf
    best_interval = (0, 0)
    best_m=0
    best_b=0

    for start in range(len(x_data) - window_size + 1):
        end = start + window_size
        x_subset = x_data[start:end]
        y_subset = y_data[start:end]

        # linear regression on the subset
        # m, b = linear_regression(x_subset, y_subset)
        n = len(x_subset)
        x_mean = np.mean(x_subset)
        y_mean = np.mean(y_subset)
        numerator = np.sum((x_subset - x_mean) * (y_subset - y_mean))
        denominator = np.sum((x_subset - x_mean) ** 2)


        if denominator == 0: #prevents division by 0
            return (-1, -1), -1, -1

        m = numerator / denominator  # slope
        b = y_mean - m * x_mean     # intercept

   
        # Calculate R^2 value for the regression line
        y_pred = m * x_subset + b
        ss_total = np.sum((y_subset - np.mean(y_subset)) ** 2)
        ss_residual = np.sum((y_subset - y_pred) ** 2)
        if ss_total == 0:
            r2 = -np.inf
        else:
            r2 = 1 - (ss_residual / ss_total)  # R-squared
    
        if r2 >= best_r2:
            if start>=window_size and abs(y_data[start-2]-y_data[start+2])>margin:
                continue
            else:
                best_r2 = r2
                best_interval = (start, end)
                best_m=m
                best_b=b

    return best_interval, best_m, best_b
    
#create the 5 different graphs

def create_graphs(displacements, weights, temperatures, timestamps, session_id, initial_length, initial_area, offset):
    graphs_list = []

    displacements = np.array(displacements)
    weights = np.array(weights)
    temperatures = np.array(temperatures)
    timestamps = np.array(timestamps)

    # strain/stress calculations
    engr_strain = displacements / initial_length #strain
    force_N = np.array([(i * 9.81) / 1000 for i in weights]) #convert weight to force in kg
    engr_stress = force_N / initial_area  #stress
    avg_temperature = np.mean(temperatures) #average temp
    time_seconds = [(ts - timestamps[0]).total_seconds() for ts in timestamps] #time elapsed since 0 timestamp

    # --- Engr Stress vs Strain Plot ---

    #get linear interval of dataset
    (Elstart, Elend), engr_slope, engr_intercept=find_best_interval(engr_strain,engr_stress)

    if engr_slope != -1 and engr_intercept != -1:

        engr_strain_subset = engr_strain[Elstart:Elend] # a subset of strain for calculating intercept change
        engr_actual_offset = offset * (engr_strain_subset.max() - engr_strain_subset.min())
        engr_offset_intercept = engr_intercept + engr_actual_offset

        engr_range = np.linspace(0, engr_strain.max(), len(engr_stress))
        engr_offset_line = engr_slope * engr_range + engr_offset_intercept # remember y = mx + b?

        fig, ax = plt.subplots()
        ax.plot(engr_strain, engr_stress, 'o-', label="Engineering Stress vs Strain")
        ax.plot(engr_range, engr_offset_line, '--k', label="Offset Line")

        ax.scatter(engr_strain[Elend-1],engr_stress[Elend-1], color='red',s=200)#yield

        if engr_slope<0:
            ax.text(0.02, 0.65, f"Invalid Data", transform=ax.transAxes, fontsize=12, color='red', ha='left', va='top')

        ax.text(0.98, 0.05, f" Yield: {engr_strain[Elend-1]:.5f}, {engr_stress[Elend-1]:.5f} \n Young's Modulus: {engr_slope:.5f} \n Avg Temp: {avg_temperature:.2f}°C", transform=ax.transAxes, fontsize=10, verticalalignment='bottom', horizontalalignment='right', bbox=dict(facecolor='white', alpha=0.5))
        ax.text(0.02, 0.98, f"Session ID: {session_id}", transform=ax.transAxes, fontsize=12, color='green', ha='left', va='top')
        ax.set_xlabel("Strain")
        ax.set_ylabel("Stress (Pa)")
        ax.legend()

        img_buf = io.BytesIO()
        plt.savefig(img_buf, format='png')
        img_buf.seek(0)
        engr_stress_strain = base64.b64encode(img_buf.read()).decode('utf-8')
        plt.close()

        graphs_list.append(engr_stress_strain)

    #------true stress and strain graph-------
    true_strain=np.log(1+engr_strain)    
    true_stress=engr_stress*(1+engr_strain)

    (Tlstart, Tlend), true_slope, true_intercept=find_best_interval(true_strain,true_stress)

    if true_slope != -1 and true_intercept != -1:

        true_strain_subset = true_strain[Tlstart:Tlend]
        true_stress_subset = true_stress[Tlstart:Tlend]

        true_actual_offset = offset * (true_strain_subset.max() - true_strain_subset.min())
        true_offset_intercept = true_intercept + true_actual_offset

        true_range = np.linspace(0, true_strain.max(), len(true_stress))
        true_offset_line = true_slope * true_range + true_offset_intercept
        
        fig, ax = plt.subplots()
        ax.plot(true_strain, true_stress, 'o-', label="True Stress vs Strain")
        ax.plot(true_range, true_offset_line, '--k', label="Offset Line")

        ax.scatter(true_strain[Tlend-1],true_stress[Tlend-1], color='red',s=200)#yield
        
        if true_slope<0:
            ax.text(0.02, 0.65, f"Invalid Data", transform=ax.transAxes, fontsize=12, color='red', ha='left', va='top')

        ax.text(0.98, 0.05, f" Yield: {true_strain[Tlend-1]:.5f}, {true_stress[Tlend-1]:.5f} \n Young's Modulus: {true_slope:.5f} \n Avg Temp: {avg_temperature:.2f}°C", transform=ax.transAxes, fontsize=10, verticalalignment='bottom', horizontalalignment='right', bbox=dict(facecolor='white', alpha=0.5))
        ax.text(0.02, 0.98, f"Session ID: {session_id}", transform=ax.transAxes, fontsize=12, color='green', ha='left', va='top')
        ax.set_xlabel("True Strain")
        ax.set_ylabel("True Stress (Pa)")
        ax.legend()

        img_buf = io.BytesIO()
        plt.savefig(img_buf, format='png')
        img_buf.seek(0)
        true_stress_strain = base64.b64encode(img_buf.read()).decode('utf-8')
        plt.close()

        graphs_list.append(true_stress_strain)

    # --- Displacement vs Force Plot ---
    fig, ax = plt.subplots()
    ax.plot(displacements, force_N, 'o-', label="Displacement vs Force")
    ax.text(0.02, 0.98, f"Session ID: {session_id}", transform=ax.transAxes, fontsize=12, color='green', ha='left', va='top')
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

    # --- Load vs Time Plot ---
    fig, ax = plt.subplots()
    ax.plot(time_seconds, force_N, 'o-', label="Load vs Time", color='g')
    ax.text(0.02, 0.98, f"Session ID: {session_id}", transform=ax.transAxes, fontsize=12, color='green', ha='left', va='top')
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
    ax.text(0.02, 0.98, f"Session ID: {session_id}", transform=ax.transAxes, fontsize=12, color='green', ha='left', va='top')
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