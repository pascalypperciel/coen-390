package com.example.minicapapp;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.view.MotionEvent;
import android.view.View;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.util.Log;

import android.Manifest;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;
import java.util.UUID;

import android.view.Menu;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// For batch processing
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ControllerActivity extends AppCompatActivity {
    // Internal attributes
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
    private static final String ESP32_MAC_ADDRESS = "20:43:A8:64:E6:9E"; //Change this if we change board btw.
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;
    private OutputStream outStream = null;
    private InputStream inStream = null;
    private Thread inThread;
    private volatile boolean stopInThread = true;
    private static final int REQUEST_BT_PERMISSIONS = 100;
    private static final int BATCH_SIZE = 10;
    private static final long BATCH_TIMEOUT_MS = 3000;
    ArrayList<Record> recordList = new ArrayList<>();
    private long lastBatchSentTime = System.currentTimeMillis();
    private boolean showMotorControls = false; // If this value is true, the motor controls will appear below the preliminary session parameters.
    private double youngModulus = -1;
    private static final double GRAVITY = 9.81;
    private float initialLength = 0.0f;
    private float initialArea = 0.0f;
    private boolean bluetoothConnected = false; // This will keep track fo the Bluetooth connection between the CAT Tester and the user's mobile device.

    // The UI elements present on the Controller Activity.
    protected Toolbar toolbarController;
    protected Button buttonEstablishBluetoothConnection, buttonStartSession, buttonMotorForward, buttonMotorBackward, buttonStop;
    protected TextView textViewBluetoothStatus, textViewMotorControls, textViewSensorData;
    protected EditText editTextSessionName, editTextInitialLength, editTextInitialArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_controller);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // This method will be used to set up all of the UI elements in the Main Activity
        setupUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopInThread = true;
        if (inThread != null && inThread.isAlive()) {
            inThread.interrupt();
        }
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (Exception ignored) {
        }
    }

    // Setup Functions for the Appbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Load the main_appbar_resource as an object
        getMenuInflater().inflate(R.menu.menu_controller_appbar_resource, menu);

        // Define the Toolbar Items and change their colour
        MenuItem bluetoothItem = menu.findItem(R.id.state_bluetooth);
        bluetoothItem.getIcon().setColorFilter(getResources().getColor(R.color.red, null), PorterDuff.Mode.SRC_IN);

        MenuItem helpItem = menu.findItem(R.id.action_help);
        helpItem.getIcon().setColorFilter(getResources().getColor(R.color.white, null), PorterDuff.Mode.SRC_IN);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (R.id.state_bluetooth == item.getItemId()) {
            bluetoothConnected = !bluetoothConnected;

            // Change the colour of the Bluetooth icon
            if(bluetoothConnected) { // Bluetooth is connected
                item.getIcon().setColorFilter(getResources().getColor(R.color.green, null), PorterDuff.Mode.SRC_IN);
                Toast.makeText(this, "Bluetooth Connected", Toast.LENGTH_LONG).show();
            } else { // Bluetooth is not connected
                item.getIcon().setColorFilter(getResources().getColor(R.color.red, null), PorterDuff.Mode.SRC_IN);
                Toast.makeText(this, "Bluetooth Disconnected", Toast.LENGTH_LONG).show();
            }

            return true;
        } else if (R.id.action_help == item.getItemId()) {
            HelpFragment helpDialogueFragment = new HelpFragment();
            helpDialogueFragment.show(getSupportFragmentManager(), "Help");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupUI() {
        // Toolbar
        toolbarController = findViewById(R.id.toolbarController);
        setSupportActionBar(toolbarController);
        getSupportActionBar().setTitle("Controller Activity");

        // Bluetooth Elements
        buttonEstablishBluetoothConnection = findViewById(R.id.buttonEstablishBluetoothConnection);
        buttonEstablishBluetoothConnection.setText("Connect Device");
        buttonEstablishBluetoothConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupBluetooth();
            }
        });
        textViewBluetoothStatus = findViewById(R.id.textViewBluetoothConnectionStatus);

        // Session Parameters
        // Session Name
        editTextSessionName = findViewById(R.id.editTextSessionName);
        editTextSessionName.setTextColor(getResources().getColor(R.color.black, null));
        editTextSessionName.setVisibility(View.INVISIBLE);
        // Initial Length of the Material Object
        editTextInitialLength = findViewById(R.id.editTextInitialLength);
        editTextInitialLength.setTextColor(getResources().getColor(R.color.black, null));
        editTextInitialLength.setVisibility(View.INVISIBLE);
        // Initial Cross-Sectional Area of the Material Object
        editTextInitialArea = findViewById(R.id.editTextInitialArea);
        editTextInitialArea.setTextColor(getResources().getColor(R.color.black, null));
        editTextInitialArea.setVisibility(View.INVISIBLE);

        // The button that will allow the session to commence
        buttonStartSession = findViewById(R.id.buttonStartSession);
        buttonStartSession.setText("Start Session");
        buttonStartSession.setVisibility(View.INVISIBLE);
        buttonStartSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bluetoothAdapter.isEnabled()) {
                    Toast.makeText(ControllerActivity.this, "Bluetooth has not been enabled", Toast.LENGTH_SHORT).show();
                } else {
                    // Check the parameters input by the user.
                    checkSessionParameters(editTextSessionName.getText().toString(), editTextInitialLength.getText().toString(), editTextInitialArea.getText().toString());
                }

            }
        });

        // Motor Control Elements
        textViewMotorControls = findViewById(R.id.textViewMotorControls);
        textViewMotorControls.setText("Motor Controls");
        textViewMotorControls.setVisibility(View.INVISIBLE);

        buttonMotorForward = findViewById(R.id.buttonMotorForward);
        buttonMotorForward.setText("Move Forward");
        buttonMotorForward.setVisibility(View.INVISIBLE);
        buttonMotorForward.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sendBluetoothCommand("Motor_FWD");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                sendBluetoothCommand("Motor_OFF");
            }
            return true;
        });

        buttonMotorBackward = findViewById(R.id.buttonMotorBackward);
        buttonMotorBackward.setText("Move Backward");
        buttonMotorBackward.setVisibility(View.INVISIBLE);
        buttonMotorBackward.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sendBluetoothCommand("Motor_BWD");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                sendBluetoothCommand("Motor_OFF");
            }
            return true;
        });

        buttonStop = findViewById(R.id.buttonStop);
        buttonStop.setText("STOP");
        buttonStop.setVisibility(View.INVISIBLE);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand("Motor_OFF");
                disableInputStream();

            }
        });

        // Real-Time Session Sensor Data
        textViewSensorData = findViewById(R.id.textViewSensorData);
        textViewSensorData.setVisibility(View.INVISIBLE);
    }

    private void setupBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            textViewSensorData.setText(R.string.bluetooth_not_work);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            int scanPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
            int connectPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);

            if (scanPerm != PackageManager.PERMISSION_GRANTED || connectPerm != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BT_PERMISSIONS);
            } else {
                startBluetoothOutThread();
            }
        } else {
            startBluetoothOutThread();
        }
    }

    private void startBluetoothOutThread() {
        if (!bluetoothAdapter.isEnabled()) {
            textViewBluetoothStatus.setText(R.string.bluetooth_not_enabled);
            return;
        }

        // Update the Visibility of the Session Parameters
        editTextSessionName.setVisibility(View.VISIBLE);
        editTextInitialLength.setVisibility(View.VISIBLE);
        editTextInitialArea.setVisibility(View.VISIBLE);
        buttonStartSession.setVisibility(View.VISIBLE);

        connectOutputStream();
    }

    private void connectOutputStream() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                Log.e("BT", "Bluetooth permissions aren't allowed");
                runOnUiThread(() -> textViewBluetoothStatus.setText(R.string.bluetooth_permissions_not_allowed));
                return;
            }
        }

        try {
            bluetoothAdapter.cancelDiscovery();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(ESP32_MAC_ADDRESS);
            bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
            bluetoothSocket.connect();

            outStream = bluetoothSocket.getOutputStream();
            runOnUiThread(() -> textViewBluetoothStatus.setText(R.string.bluetooth_connected));
            buttonStartSession.setVisibility(View.VISIBLE);

            inStream = bluetoothSocket.getInputStream();

        } catch (SecurityException se) {
            se.printStackTrace();
            runOnUiThread(() -> textViewBluetoothStatus.setText("SecurityException: " + se.getMessage()));
            buttonStartSession.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> textViewBluetoothStatus.setText("Error: " + e.getMessage()));
            buttonStartSession.setVisibility(View.INVISIBLE);
        }

    }

    private void connectInputStream(String sessionID) {
        synchronized (this ) {
            if (!stopInThread) {
                return;
            }
            stopInThread = false;
        }

        inThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[128];
                int bytesRead;
                clearInputStream();
                while (!stopInThread) {
                    try {
                        bytesRead = inStream.read(buffer);
                        if (bytesRead > 0) {
                            final String incoming = new String(buffer, 0, bytesRead).trim();
                            processBluetoothData(incoming, sessionID);
                            runOnUiThread(() -> textViewBluetoothStatus.setText("Connected and Fetching Data"));
                        }
                    } catch (SecurityException se) {
                        se.printStackTrace();
                        runOnUiThread(() -> textViewBluetoothStatus.setText("SecurityException: " + se.getMessage()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> textViewBluetoothStatus.setText("Error: " + e.getMessage()));
                    }
                }
                runOnUiThread(() -> textViewBluetoothStatus.setText(R.string.bluetooth_connected));
                buttonStartSession.setVisibility(View.VISIBLE);
            }
        });

        inThread.start();
        runOnUiThread(() -> textViewBluetoothStatus.setText(R.string.bluetooth_connected));
        buttonStartSession.setVisibility(View.INVISIBLE);

    }

        private void processBluetoothData(String incoming, String sessionID) throws JSONException {
        String[] values = incoming.split(";");
        Record record = new Record();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String timestamp = sdf.format(new Date());
    
        if (values.length == 3) {
            String valid = "True";
            for (String value : values) {
                if (Objects.equals(value, "NaN")) {
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
            runOnUiThread(() -> displayRecord(record));
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
                if (responseCode != HttpURLConnection.HTTP_CREATED && responseCode != HttpURLConnection.HTTP_OK) {
                    System.err.println("Batch processing failed: " + responseCode + " - " + conn.getResponseMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void disableInputStream() {
        if (stopInThread) {
            return;
        }
        stopInThread = true;
        if (inThread != null && inThread.isAlive()) {
            inThread.interrupt();
        }
        runOnUiThread(() -> textViewBluetoothStatus.setText(R.string.bluetooth_connected));
        buttonStartSession.setVisibility(View.VISIBLE);
    }

    private void sendBluetoothCommand(String command) {
        if (bluetoothSocket != null && outStream != null) {
            try {
                outStream.write((command + "\n").getBytes());
                outStream.flush();
            } catch (Exception e) {
                Log.e("BT", "Error sending command: " + e.getMessage());
            }
        }
    }

    private void displayRecord(Record newMessage) {
        String displayText = "Distance: " + newMessage.distance + "\nPressure: " + newMessage.pressure + "\nTemperature: " + newMessage.temperature;
        textViewSensorData.setText(displayText);

        //stop if us sensor says too close
        if (Float.valueOf(newMessage.distance.trim()) < 2.0 || Float.valueOf(newMessage.distance.trim()) > 10.55) {
            Toast.makeText(getApplicationContext(), "Test Finished", Toast.LENGTH_LONG).show();
            sendBluetoothCommand("Motor_OFF");
            disableInputStream();
        }
    }

    private void clearInputStream() {
        byte[] buffer = new byte[1024];
        try {
            while (inStream.available() > 0) {
                inStream.read(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> textViewBluetoothStatus.setText("Error clearing inStream" + e.getMessage()));
        }
    }
        private void analyzeStopData() {
        if (recordList.size() < 10) {
            return; // Not enough data to analyze
        }
    
        // Extract the last 10 records
        List<Record> lastTenRecords = recordList.subList(recordList.size() - 10, recordList.size());
    
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
            Toast.makeText(getApplicationContext(), "Test stopped due to large data variation", Toast.LENGTH_LONG).show();
            sendBluetoothCommand("Motor_OFF");
            disableInputStream();
        }
    }
        private void monitorYoungModulus() {
        if (recordList.size() < 10) {
            return; // Not enough data to calculate Young's modulus
        }
    
        // Calculate the initial Young's modulus if not already calculated
        if (youngModulus == -1) {
            youngModulus = calculateYoungModulus(recordList.subList(0, 10));
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
            Toast.makeText(getApplicationContext(), "Test stopped due to large Young Modulus variation", Toast.LENGTH_LONG).show();
            sendBluetoothCommand("Motor_OFF");
            disableInputStream();
        }
    }
        private double calculateYoungModulus(List<Record> records) {
        try {
            double totalStress = 0.0;
            double totalStrain = 0.0;
    
            // Calculate total stress and strain
            double originalLength = Double.parseDouble(records.get(0).distance); // Calculate once
            for (Record record : records) {
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

    private void createSession(long sessionID, String sessionName, float initialLength, float initialArea) {
        new Thread(() -> {
            JSONObject initialData = new JSONObject();
            try {
                initialData.put("SessionID", sessionID);
                initialData.put("SessionName", sessionName);
                initialData.put("InitialLength", initialLength);
                initialData.put("InitialArea", initialArea);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            try {
                URL url = new URL("https://cat-tester-api.azurewebsites.net/initial-session-info");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                Log.d("JSON_PAYLOAD", "Sending: " + initialData);

                OutputStream os = conn.getOutputStream();
                os.write(initialData.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_CREATED && responseCode != HttpURLConnection.HTTP_OK) {
                    System.err.println("Batch processing failed: " + responseCode + " - " + conn.getResponseMessage());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // This method will check the parameters entered by the user and throw an error if it is invalid.
    private void checkSessionParameters(String nameString, String lengthString, String areaString) {
        if (!(nameString.isBlank() || lengthString.isBlank() || areaString.isBlank())) {
            try {
                // Convert the length and area parameters to float variables.
                float length = Float.parseFloat(lengthString);
                float area = Float.parseFloat(areaString);

                // Verify that the conditions for the length and area variables are met.
                if ((length > 0.0f) && (area > 0.0f)) {
                    showMotorControls = true;

                    initialLength = length;
                    initialArea = area;

                    // Make the motor controls section visible.
                    textViewMotorControls.setVisibility(View.VISIBLE);
                    buttonMotorForward.setVisibility(View.VISIBLE);
                    buttonMotorBackward.setVisibility(View.VISIBLE);
                    buttonStop.setVisibility(View.VISIBLE);
                    textViewSensorData.setVisibility(View.VISIBLE);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                    String sessionID = sdf.format(new Date());

                    sendBluetoothCommand("Motor_BWD");

                    Session session = new Session();
                    session.initialArea = Float.parseFloat(editTextInitialArea.getText().toString());
                    session.initialLength = Float.parseFloat(editTextInitialLength.getText().toString());
                    session.sessionName = editTextSessionName.getText().toString();
                    createSession(Long.parseLong(sessionID), session.sessionName, session.initialLength, session.initialArea);
                    connectInputStream(sessionID);
                } else {
                    Toast.makeText(this, "Length and Area cannot be 0", Toast.LENGTH_LONG).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid Input", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(ControllerActivity.this, "All fields must be filled", Toast.LENGTH_LONG).show();
        }

    }
}