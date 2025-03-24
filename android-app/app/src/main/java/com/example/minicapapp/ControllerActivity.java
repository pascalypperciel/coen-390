package com.example.minicapapp;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
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
    // Internal attributes
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

    // The UI elements present on the Controller Activity.
    protected Toolbar toolbarController;
    protected Button buttonMotorForward, buttonMotorBackward, buttonStop, buttonEstablishBluetoothConnection, buttonRecord;
    protected TextView textViewBluetoothStatus, textViewBluetoothData;
    protected EditText editTextInitialLength, editTextInitialArea, editTextSessionName;

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

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // The "finish()" will navigate back to the previous activity.
        return true;
    }

    // Setup Functions for the Appbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Load the main_appbar_resource as an object
        getMenuInflater().inflate(R.menu.menu_appbar_resource, menu);

        // Define the Toolbar Items and change their colour
        MenuItem settingsItem = menu.findItem(R.id.action_settings);
        settingsItem.getIcon().setColorFilter(getResources().getColor(R.color.white, null), PorterDuff.Mode.SRC_IN);

        MenuItem helpItem = menu.findItem(R.id.action_help);
        helpItem.getIcon().setColorFilter(getResources().getColor(R.color.white, null), PorterDuff.Mode.SRC_IN);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (R.id.action_settings == item.getItemId()) {
            goToSettingsActivity();
            return true;
        } else if (R.id.action_help == item.getItemId()) {
            HelpFrag helpDialogueFragment = new HelpFrag();
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Bluetooth Elements
        textViewBluetoothData = findViewById(R.id.showsbtmessages);
        textViewBluetoothStatus = findViewById(R.id.connectionstatus);
        buttonEstablishBluetoothConnection = findViewById(R.id.buttonEstablishBluetoothConnection);
        buttonEstablishBluetoothConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupBluetooth();
            }
        });

        // Controller Elements
        buttonMotorBackward = findViewById(R.id.motorbwd);
        buttonMotorBackward.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sendBluetoothCommand("Motor_BWD");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                sendBluetoothCommand("Motor_OFF");
            }
            return true;
        });

        buttonMotorForward = findViewById(R.id.motorfwd);
        buttonMotorForward.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sendBluetoothCommand("Motor_FWD");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                sendBluetoothCommand("Motor_OFF");
            }
            return true;
        });

//        editTextInitialLength = findViewById(R.id.??????); //Tyler, you can put the text fields and stuff here
//        editTextInitialArea = findViewById(R.id.??????);
//        editTextSessionName = findViewById(R.id.??????);

        buttonRecord = findViewById(R.id.buttonRecord);
        buttonRecord.setVisibility(View.INVISIBLE);
        buttonRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String sessionID = sdf.format(new Date());

                sendBluetoothCommand("Motor_BWD");

                Session session = new Session();
                session.initialArea = Float.parseFloat(editTextInitialArea.getText().toString());
                session.initialLength = Float.parseFloat(editTextInitialLength.getText().toString());
                session.sessionName = editTextSessionName.getText().toString();
                createSession(Long.parseLong(sessionID), session.sessionName, session.initialLength, session.initialArea);
                connectInputStream(sessionID);
            }
        });

        buttonStop = findViewById(R.id.stop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand("Motor_OFF");
                disableInputStream();

            }
        });
    }

    // This method will allow the Settings Activity to be accessed from the Recorded Data Activity
    private void goToSettingsActivity() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void setupBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            textViewBluetoothData.setText(R.string.bluetooth_not_work);
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
        buttonRecord.setVisibility(View.VISIBLE);
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
            buttonRecord.setVisibility(View.VISIBLE);

            inStream = bluetoothSocket.getInputStream();

        } catch (SecurityException se) {
            se.printStackTrace();
            runOnUiThread(() -> textViewBluetoothStatus.setText("SecurityException: " + se.getMessage()));
            buttonRecord.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> textViewBluetoothStatus.setText("Error: " + e.getMessage()));
            buttonRecord.setVisibility(View.INVISIBLE);
        }

    }

    private void connectInputStream(String sessionID) {
        if (!stopInThread) {
            return;
        }
        stopInThread = false;

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

                            //
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
                buttonRecord.setVisibility(View.VISIBLE);
            }
        });

        inThread.start();
        runOnUiThread(() -> textViewBluetoothStatus.setText(R.string.bluetooth_connected));
        buttonRecord.setVisibility(View.INVISIBLE);

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
            recordList.clear();
            // Right here do you check Jason
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
        buttonRecord.setVisibility(View.VISIBLE);
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
        textViewBluetoothData.setText(displayText);

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
}