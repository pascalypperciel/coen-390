package com.example.minicapapp;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ControllerFragment extends Fragment {

    public static class Record {
        public String distance;
        public String temperature;
        public String timestamp;
        public String sessionID;
        public String pressure;
        public String valid;
    }
    public static class Session {
        public String sessionName;
        public float initialLength;
        public float initialArea;
    }
    // Internal Attributes
    protected TextView textViewMotorControls, textViewDistance, textViewPressure, textViewTemperature, textViewConnectBluetoothMessage;
    private LinearLayout mainContent;
    protected EditText editTextSessionName, editTextInitialLength, editTextInitialArea;
    protected Button buttonMotorForward, buttonMotorBackward, buttonStartStop, buttonBluetoothStatus;
    private boolean testFinished = false;

    //definitions from old controller activity
    private static final int BATCH_SIZE = 10;
    private static final long BATCH_TIMEOUT_MS = 3000;
    ArrayList<ControllerFragment.Record> recordList = new ArrayList<>();
    private long lastBatchSentTime = System.currentTimeMillis();
    private double youngModulus = -1;
    private static final double GRAVITY = 9.81;
    private float initialLength = 0.0f;
    private float initialArea = 0.0f;
    private volatile boolean isListening = false;

    // The UI elements present in the Controller Fragment

    //////////////////////////////////////////////////////
    protected ImageButton imageButtonHelpController;

    public ControllerFragment() {
        // Required empty public constructor
    }


    private void updateBluetoothStatusButton() {
        BluetoothManager btManager = BluetoothManager.getInstance();
        if (btManager.isConnected()) {
            buttonBluetoothStatus.setText("Connected");
        } else {
            buttonBluetoothStatus.setText("Disconnected");
        }
        buttonBluetoothStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_bluetooth_24, 0, 0, 0);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_controller, container, false);

        mainContent = view.findViewById(R.id.mainContent);
        textViewConnectBluetoothMessage = view.findViewById(R.id.textViewConnectBluetoothMessage);

        // Define the Bluetooth Status button
        buttonBluetoothStatus = view.findViewById(R.id.buttonBluetoothStatus);
        updateBluetoothStatusButton();

        buttonBluetoothStatus.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameLayoutActivityContent, new BluetoothFragment())
                        .addToBackStack(null)
                        .commit();

                ((BottomNavigationBarActivity) requireActivity()).setBottomNavSelectedItemWithoutTriggering(R.id.action_load_settings);
            }
        });

        // Define and set the behaviour of the UI elements in ths fragment
        imageButtonHelpController = view.findViewById(R.id.imageButtonHelpController);
        imageButtonHelpController.setOnClickListener(v -> {
            HelpFragment helpFragment = HelpFragment.newInstance("Controller");
            helpFragment.show(requireActivity().getSupportFragmentManager(), "HelpDialogue");
        });

        // Session Parameters
        // Session Name
        editTextSessionName = view.findViewById(R.id.editTextSessionName);
        editTextSessionName.setTextColor(getResources().getColor(R.color.black, null));
        // Initial Length of the Material Object
        editTextInitialLength = view.findViewById(R.id.editTextInitialLength);
        editTextInitialLength.setTextColor(getResources().getColor(R.color.black, null));
        // Initial Cross-Sectional Area of the Material Object
        editTextInitialArea = view.findViewById(R.id.editTextInitialArea);
        editTextInitialArea.setTextColor(getResources().getColor(R.color.black, null));

        // Motor Control Elements
        textViewMotorControls = view.findViewById(R.id.textViewMotorControls);
        textViewMotorControls.setText(R.string.motor_controls);

        buttonMotorForward = view.findViewById(R.id.buttonMotorForward);
        buttonMotorForward.setText(R.string.move_forward);
        buttonMotorForward.setEnabled(false);
        buttonMotorForward.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                BluetoothManager btManager = BluetoothManager.getInstance();
                if (btManager.isConnected()) {
                    btManager.sendCommand("Motor_FWD");
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {

                BluetoothManager btManager = BluetoothManager.getInstance();
                if (btManager.isConnected()) {
                    btManager.sendCommand("Motor_OFF");
                }
            }
            return true;
        });

        buttonMotorBackward = view.findViewById(R.id.buttonMotorBackward);
        buttonMotorBackward.setText(R.string.move_backward);
        buttonMotorBackward.setEnabled(false);
        buttonMotorBackward.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                BluetoothManager btManager = BluetoothManager.getInstance();
                if (btManager.isConnected()) {
                    btManager.sendCommand("Motor_BWD");
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                BluetoothManager btManager = BluetoothManager.getInstance();
                if (btManager.isConnected()) {
                    btManager.sendCommand("Motor_OFF");
                }
            }
            return true;
        });

        buttonStartStop = view.findViewById(R.id.buttonStartStop);

        buttonStartStop.setOnClickListener(v -> {
            if (!isListening) { //Stop if Started
                isListening = true;
                buttonStartStop.setText(R.string.stop);
                buttonStartStop.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));

                BluetoothManager btManager = BluetoothManager.getInstance();
                if(!btManager.isConnected()) {
                    Toast.makeText(requireContext(), "Bluetooth has not been enabled", Toast.LENGTH_SHORT).show();
                } else {
                    // Check the parameters input by the user.
                    checkSessionParameters(editTextSessionName.getText().toString(), editTextInitialLength.getText().toString(), editTextInitialArea.getText().toString());
                }

            } else { //Start if Stopped
                isListening = false;
                buttonStartStop.setText(R.string.start_new_session);
                buttonStartStop.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));

                // Re-disable the session input fields
                editTextSessionName.setEnabled(true);
                editTextInitialLength.setEnabled(true);
                editTextInitialArea.setEnabled(true);

                // Reset the text fields
                editTextSessionName.setText("");
                editTextInitialLength.setText("");
                editTextInitialArea.setText("");

                // Reset the Live Data Displays
                textViewDistance.setText("-");
                textViewPressure.setText("-");
                textViewTemperature.setText("-");

                BluetoothManager btManager = BluetoothManager.getInstance();
                if (btManager.isConnected()) {
                    btManager.sendCommand("Motor_OFF");
                }

                // If any records left, send them to db
                if (!recordList.isEmpty()) {
                    try {
                        sendBatchData(recordList);
                    } catch (JSONException e) {
                        Log.e("StopButton", "Failed to send final batch", e);
                    }
                }

                disableInputStream();
            }
        });

        // Real-Time Session Sensor Data
        textViewDistance = view.findViewById(R.id.textDistanceValue);
        textViewPressure = view.findViewById(R.id.textPressureValue);
        textViewTemperature = view.findViewById(R.id.textTemperatureValue);

        showSessionInputsIfConnected();

        return view;
    }

    private void showSessionInputsIfConnected() {
        BluetoothManager btManager = BluetoothManager.getInstance();
        boolean isConnected = btManager.isConnected();

        if (isConnected) {
            mainContent.setAlpha(0f);
            mainContent.setVisibility(View.VISIBLE);
            mainContent.animate().alpha(1f).setDuration(300).start();

            textViewConnectBluetoothMessage.setVisibility(View.GONE);
        } else {
            mainContent.setVisibility(View.GONE);
            textViewConnectBluetoothMessage.setVisibility(View.VISIBLE);
        }
    }


    private void startBluetoothDataListener(String sessionID) {
        BluetoothManager btManager = BluetoothManager.getInstance();
        InputStream inputStream = btManager.getInputStream();

        if (inputStream == null) {
            Log.e("BluetoothData", "InputStream is null. Cannot start data listener.");
            return;
        }

        new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                int bytes;
                StringBuilder dataBuffer = new StringBuilder();

                while (btManager.isConnected() && isListening) {
                    bytes = inputStream.read(buffer);
                    String incomingData = new String(buffer, 0, bytes);
                    dataBuffer.append(incomingData);

                    int endOfLineIndex;
                    while ((endOfLineIndex = dataBuffer.indexOf("\n")) >= 0) {
                        String fullMessage = dataBuffer.substring(0, endOfLineIndex).trim();
                        dataBuffer.delete(0, endOfLineIndex + 1);

                        try {
                            processBluetoothData(fullMessage, sessionID);
                        } catch (JSONException e) {
                            Log.e("BluetoothData", "Error parsing Bluetooth data", e);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e("BluetoothData", "Error reading from input stream", e);
            }
        }).start();
    }



    private void processBluetoothData(String incoming, String sessionID) throws JSONException {
        String[] values = incoming.split(";");
        Record record = new Record();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String timestamp = sdf.format(new Date());

        if (values.length == 3) {
            String valid = "True";
            for (String value : values) {
                if (value.equalsIgnoreCase("nan")) {
                    valid = "False";
                    break;
                }
            }

            record.valid = valid;
            record.distance = values[0];
            record.temperature = values[1];
            record.pressure = values[2];
            record.timestamp = timestamp;
            record.sessionID = sessionID;
            recordList.add(record);

            // Update the UI on the main thread
            if (isAdded()) { // Ensure the fragment is attached to an activity
                requireActivity().runOnUiThread(() -> displayRecord(record));
            }
        }

        long currentTime = System.currentTimeMillis();

        if (recordList.size() >= BATCH_SIZE || (currentTime - lastBatchSentTime) >= BATCH_TIMEOUT_MS) {
            sendBatchData(recordList);
            lastBatchSentTime = currentTime;

            // Analyze the last batch of data
            analyzeStopData();
            monitorYoungModulus();

            // Clear the record list for the next batch
            recordList.clear();
        }
    }

    private void sendBatchData(ArrayList<Record> recordList) throws JSONException {
        // Create a JSONArray to hold all the records
        JSONArray jsonArray = new JSONArray();

        for (Record record : recordList) {
            // Convert each Record object to a JSONObject
            JSONObject jsonRecord = new JSONObject();
            jsonRecord.put("Distance", record.distance);
            jsonRecord.put("Temperature", record.temperature);
            jsonRecord.put("Pressure", record.pressure);
            jsonRecord.put("SessionID", record.sessionID);
            jsonRecord.put("Timestamp", record.timestamp);
            jsonRecord.put("Valid", record.valid);

            // Add the JSONObject to the JSONArray
            jsonArray.put(jsonRecord);
        }

        new Thread(() -> {
            try {
                URL url = new URL("https://cat-tester-api.azurewebsites.net/batch-process-records");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                Log.d("JSON_PAYLOAD", "Sending: " + jsonArray);

                OutputStream os = conn.getOutputStream();
                os.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                    Log.d("BatchProcessing", "Batch processed successfully: " + responseCode);
                } else {
                    Log.e("BatchProcessing", "Batch processing failed: " + responseCode + " - " + conn.getResponseMessage());
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Batch processing failed: " + responseCode, Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            } catch (Exception e) {
                Log.e("BatchProcessing", "Error during batch processing", e);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error during batch processing: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).start();
    }
    private void disableInputStream() {
        // Update the UI on the main thread
        testFinished = false;
    }

    private void displayRecord(Record newMessage) {
        // Update the UI on the main thread
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                textViewDistance.setText(newMessage.distance + " cm");
                textViewPressure.setText(newMessage.pressure + " kg");
                textViewTemperature.setText(newMessage.temperature + "°C");
            });
        }

        // Stop if the sensor detects "too close" or "too far"
        try {
            float distance = Float.parseFloat(newMessage.distance.trim());
            if ((distance < 2.0) && !testFinished) {
                testFinished = true;

                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Test Finished", Toast.LENGTH_LONG).show()
                    );
                }

                BluetoothManager btManager = BluetoothManager.getInstance();
                if (btManager.isConnected()) {
                    btManager.sendCommand("Motor_OFF");
                }

                disableInputStream();
            }
        } catch (NumberFormatException e) {
            Log.e("BT", "Error parsing distance: " + e.getMessage());
        }
    }

    private void analyzeStopData() {
        if (recordList.size() < 10) {
            return; // Not enough data to analyze
        }

        // Extract the last 10 records
        List<Record> lastTenRecords = recordList.subList(recordList.size() - 10, recordList.size());

        for (Record r : lastTenRecords) {
            if (r.valid.equals("False")) {
                Log.w("ANALYZE", "Skipping variation analysis due to invalid data");
                return; // One or more of the values were NaN
            }
        }

        double sumDistanceFirstFive = 0.0, sumDistanceLastFive = 0.0;
        double sumPressureFirstFive = 0.0, sumPressureLastFive = 0.0;

        for (int i = 0; i < 5; i++) {
            try {
                // Get distance and pressure for the first five records
                sumDistanceFirstFive += Double.parseDouble(lastTenRecords.get(i).distance.trim());
                sumPressureFirstFive += Double.parseDouble(lastTenRecords.get(i).pressure.trim());

                // Get distance and pressure for the last five records
                sumDistanceLastFive += Double.parseDouble(lastTenRecords.get(i + 5).distance.trim());
                sumPressureLastFive += Double.parseDouble(lastTenRecords.get(i + 5).pressure.trim());
            } catch (NumberFormatException e) {
                Log.e("ANALYZE", "Invalid number format in record: " + e.getMessage());
                return; // Exit if invalid data is encountered
            }
        }

        // Calculate averages & differences
        double avgDistanceFirstFive = sumDistanceFirstFive / 5;
        double avgDistanceLastFive = sumDistanceLastFive / 5;
        double avgPressureFirstFive = sumPressureFirstFive / 5;
        double avgPressureLastFive = sumPressureLastFive / 5;

        double distanceDifference = Math.abs(avgDistanceLastFive - avgDistanceFirstFive);
        double pressureDifference = Math.abs(avgPressureLastFive - avgPressureFirstFive);

        // Define thresholds for stopping the test
        double distanceThreshold = 5.0;
        double pressureThreshold = 100.0;

        // Check if differences exceed thresholds
        if (distanceDifference > distanceThreshold || pressureDifference > pressureThreshold) {
            Toast.makeText(requireContext(), "Test stopped due to large data variation", Toast.LENGTH_LONG).show();

            BluetoothManager btManager = BluetoothManager.getInstance();
            if (btManager.isConnected()) {
                btManager.sendCommand("Motor_OFF");
            }
            disableInputStream();
        }
    }
    private void monitorYoungModulus() {
        if (recordList.size() < 10) {
            return; // Not enough data to calculate Young's modulus
        }

        List<Record> recentRecords = recordList.subList(recordList.size() - 10, recordList.size());

        for (Record r : recentRecords) {
            if (r.valid.equals("False")) {
                Log.w("YOUNG_MODULUS", "Skipping Young Modulus calc due to invalid data");
                return; // One or more of the values were NaN
            }
        }

        // Calculate the initial Young's modulus if not already calculated
        if (youngModulus == -1) {
            youngModulus = calculateYoungModulus(recordList);
            if (youngModulus == -1) {
                Log.e("YOUNG_MODULUS", "Invalid Young Modulus value");
                return;
            }
        }

        // Calculate the current Young's modulus
        double currentYoungModulus = calculateYoungModulus(recordList.subList(recordList.size() - 10, recordList.size()));
        if (currentYoungModulus == -1) {
            Log.e("YOUNG_MODULUS", "Invalid Young Modulus value");
            return;
        }

        // Calculate the percentage difference
        double percDiff = Math.abs((youngModulus - currentYoungModulus) / youngModulus) * 100;
        if (percDiff > 5) {
            Toast.makeText(requireContext(), "Test stopped due to large Young Modulus variation", Toast.LENGTH_LONG).show();

            BluetoothManager btManager = BluetoothManager.getInstance();
            if (btManager.isConnected()) {
                btManager.sendCommand("Motor_OFF");
            }
            disableInputStream();
        }
    }
    private double calculateYoungModulus(List<ControllerFragment.Record> records) {
        try {
            double totalStress = 0.0;
            double totalStrain = 0.0;

            // Calculate total stress and strain
            for (ControllerFragment.Record record : records) {
                double pressure = Double.parseDouble(record.pressure);
                double distance = Double.parseDouble(record.distance);
                double force = pressure / 1000 * GRAVITY;

                double stress = force / initialArea; // Stress = Force / Area
                double strain = (distance - initialLength) / initialLength; // Strain = ΔL / L₀
                totalStress += stress;
                totalStrain += strain;
            }

            // Calculate averages
            double avgStress = totalStress / records.size();
            double avgStrain = totalStrain / records.size();

            // Check for division by zero
            if (avgStrain == 0) {
                Log.e("YOUNG_MODULUS", "Strain is zero, cannot calculate Young's modulus.");
                return -1; // Return -1 to indicate failure
            }

            // Calculate Young's modulus (Stress / Strain)
            return avgStress / avgStrain;
        } catch (Exception e) {
            Log.e("YOUNG_MODULUS", "Error calculating Young's modulus: " + e.getMessage());
            return -1; // Return -1 to indicate failure
        }
    }

    private void createSession(long sessionID, String sessionName, float initialLength, float initialArea, Runnable onSuccess) {
        new Thread(() -> {
            JSONObject initialData = new JSONObject();
            try {
                // Prepare the JSON payload
                initialData.put("SessionID", sessionID);
                initialData.put("SessionName", sessionName);
                initialData.put("InitialLength", initialLength);
                initialData.put("InitialArea", initialArea);
            } catch (JSONException e) {
                Log.e("CreateSession", "Error creating JSON payload", e);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error creating session data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
                return;
            }

            try {
                // Open the connection
                URL url = new URL("https://cat-tester-api.azurewebsites.net/initial-session-info");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                Log.d("JSON_PAYLOAD", "Sending: " + initialData);

                // Send the JSON payload
                OutputStream os = conn.getOutputStream();
                os.write(initialData.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                // Handle the response
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("CreateSession", "Session created successfully: " + responseCode);
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Session created successfully!", Toast.LENGTH_SHORT).show();
                            onSuccess.run();
                        });
                    }
                } else {
                    Log.e("CreateSession", "Session creation failed: " + responseCode + " - " + conn.getResponseMessage());
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Session creation failed: " + responseCode, Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            } catch (Exception e) {
                Log.e("CreateSession", "Error during session creation", e);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error during session creation: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).start();
    }


    private void checkSessionParameters(String nameString, String lengthString, String areaString) {
        if (!(nameString.isBlank() || lengthString.isBlank() || areaString.isBlank())) {
            try {
                // Convert the length and area parameters to float variables.
                float length = Float.parseFloat(lengthString);
                float area = Float.parseFloat(areaString);

                // Verify that the conditions for the length and area variables are met.
                if ((length > 0.0f) && (area > 0.0f)) {
                    // If this value is true, the motor controls will appear below the preliminary session parameters.
                    initialLength = length;
                    initialArea = area;

                    // Make the motor controls section visible.
                    textViewMotorControls.setEnabled(true);
                    buttonMotorForward.setEnabled(true);
                    buttonMotorBackward.setEnabled(true);
                    buttonStartStop.setEnabled(true);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                    String sessionID = sdf.format(new Date());

                    BluetoothManager btManager = BluetoothManager.getInstance();
                    if (btManager.isConnected()) {
                        btManager.sendCommand("Motor_BWD");
                    }

                    Session session = new Session();
                    session.initialArea = Float.parseFloat(editTextInitialArea.getText().toString());
                    session.initialLength = Float.parseFloat(editTextInitialLength.getText().toString());
                    session.sessionName = editTextSessionName.getText().toString();

                    // Create the session
                    createSession(Long.parseLong(sessionID), session.sessionName, session.initialLength, session.initialArea, () -> {
                        // Disable session inputs after successful session creation
                        requireActivity().runOnUiThread(() -> {
                            editTextSessionName.setEnabled(false);
                            editTextInitialLength.setEnabled(false);
                            editTextInitialArea.setEnabled(false);
                        });

                        // Start Bluetooth listener for batch records data
                        startBluetoothDataListener(sessionID);
                    });

                } else {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Length and Area cannot be 0", Toast.LENGTH_LONG).show()
                        );
                    }
                }
            } catch (NumberFormatException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Invalid Input", Toast.LENGTH_LONG).show()
                    );
                }
            }
        } else {
            if (isAdded()) {
                requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "All fields must be filled", Toast.LENGTH_LONG).show()
                );
            }
        }
    }
}
