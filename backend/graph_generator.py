import numpy as np
import matplotlib.pyplot as plt
import io
import base64

def create_graphs(distances, weights, temperatures, timestamps, session_id, initial_length, initial_area, offset):
    graphs_list = []

    displacements = np.array([i + distances[0] for i in distances])

    engineering_strain = displacements/initial_length # Engineering strain is the change in length relative to initial length

    force_N = np.array([(i*9.81)/1000 for i in weights])

    engineering_stress = force_N / initial_area # Engineering stress is the pressure relative to initial area
        
    avg_temperature = np.mean(temperatures)
        
    time_seconds = [(ts - timestamps[0]).total_seconds() for ts in timestamps]

    ## ----------------------------- DISPLACEMENT VS FORCE PLOT ----------------------------- ##
    plt.plot(displacements, force_N, 'o-', label="Displacement vs Force")
        
    # Display average temperature
    plt.text(0.95, 0.05, f"Avg Temp: {avg_temperature:.2f}°C", transform=plt.gca().transAxes, fontsize=10,
                 verticalalignment='bottom', horizontalalignment='right', bbox=dict(facecolor='white', alpha=0.5))
        
    # Display SessionID
    plt.text(0.05, 0.95, f"Session ID: {session_id}", transform=plt.gca().transAxes,
    fontsize=12, color='red', ha='left', va='top')
        
    plt.xlabel("Displacement (cm)")
    plt.ylabel("Force (N)")
    plt.legend()
    plt.tight_layout()

    img_buf = io.BytesIO()
    plt.savefig(img_buf, format='png')
    img_buf.seek(0)  # Rewind the buffer to the beginning

    # Encode the image to base64 and put it in the list
    displacement_force = base64.b64encode(img_buf.read()).decode('utf-8')

    plt.close()  # Close the plot to avoid memory issues
    graphs_list.append[displacement_force]

    ## ----------------------------- STRESS VS STRAIN PLOT ----------------------------- ##
    # Fit a line
    coef = np.polyfit(engineering_strain, engineering_stress, 1)
    poly1d_fn = np.poly1d(coef)

    strain_subset = engineering_strain[:25]  # Slice the first 50 values
    offset_line_coef = offset * (strain_subset.max() - strain_subset.min())  # offset% of the range
        
    strain_offset = np.array([i + offset_line_coef for i in engineering_strain])
        
    stress_offset = poly1d_fn(strain_offset)

    plt.plot(engineering_strain, engineering_stress, 'o-', label="Stress vs Strain")

        # Plot the offset line
    plt.plot(strain_offset, stress_offset, '--k', label="Offset Line")  # Offset line (black dashed)
        
        # Find the intersection point
        # Calculate the difference between the original stress and the offset stress
    stress_diff = engineering_stress - stress_offset[:len(engineering_stress)]  # Ensure same length
    intersection_idx = np.argmin(np.abs(stress_diff))  # Find index where the difference is minimal

        # Intersection strain and stress
    intersection_strain = engineering_strain[intersection_idx]
    intersection_stress = engineering_stress[intersection_idx]

        # Mark the intersection point on the plot
    plt.plot(intersection_strain, intersection_stress, 'ro', label="Intersection Point")

        # Display Temperature
    plt.text(0.95, 0.05, f"Avg Temp: {avg_temperature:.2f}°C", transform=plt.gca().transAxes, fontsize=10,
                 verticalalignment='bottom', horizontalalignment='right', bbox=dict(facecolor='white', alpha=0.5))        
        # Display Session ID
    plt.text(0.05, 0.95, f"Session ID: {session_id}", transform=plt.gca().transAxes,
                fontsize=12, color='red', ha='left', va='top')
    plt.xlabel("Strain")
    plt.ylabel("Stress (Pa)")
    plt.legend()
    plt.tight_layout()

    img_buf = io.BytesIO()
    plt.savefig(img_buf, format='png')
    img_buf.seek(0)  # Rewind the buffer to the beginning

        # Encode the image to base64 and put it in the list
    stress_strain = base64.b64encode(img_buf.read()).decode('utf-8')

    plt.close()
    graphs_list.append(stress_strain)

    ## ----------------------------- LOAD VS TIME PLOT ----------------------------- ##
    plt.plot(time_seconds, force_N, 'o-', label="Load vs Time", color='g')
    plt.xlabel("Time (sec)")
    plt.ylabel("Force (N)")
    plt.legend()

    plt.tight_layout()

    img_buf = io.BytesIO()
    plt.savefig(img_buf, format='png')
    img_buf.seek(0)  # Rewind the buffer to the beginning

    # Encode the image to base64 and put it in the list
    load_time = base64.b64encode(img_buf.read()).decode('utf-8')
        
    plt.close() 
    graphs_list.append(load_time)
    
    ## ----------------------------- DISPLACEMENT VS TIME PLOT ----------------------------- ##
    plt.plot(time_seconds, displacements, 'o-', label="Load vs Time", color='g')
    plt.xlabel("Time (sec)")
    plt.ylabel("Displacement (cm)")
    plt.legend()

    plt.tight_layout()

    img_buf = io.BytesIO()
    plt.savefig(img_buf, format='png')
    img_buf.seek(0)  # Rewind the buffer to the beginning

        # Encode the image to base64 and put it in the list
    load_time = base64.b64encode(img_buf.read()).decode('utf-8')
        
    plt.close()
    graphs_list.append(load_time)

    return graphs_list
        # Still need: true stress/strain, stress/strain vs prev. tests, stress/strain vs. temperature, 
        # stress/strain vs. other material, etc.

