import numpy as np
import matplotlib.pyplot as plt
import io
import base64
import math

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