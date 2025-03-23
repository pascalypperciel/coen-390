package com.example.minicapapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Build;
import android.os.Bundle;
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
import android.view.MenuInflater;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

// For batch processing
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ControllerActivity extends AppCompatActivity {
    protected Button btnMotorFwd, btnMotorBwd, btnStopB, btnEstablishConnectionBluetooth, btnRecordB;
    protected Toolbar toolbar;
    protected TextView txtStatusBluetooth, txtBluetoothData;
    protected Spinner spnC, spnT;
    protected EditText etInput;
    protected String selectedTest, selectedMaterial;
    private static final String ESP32_MAC_ADDRESS = "20:43:A8:64:E6:9E"; //Change this if we change board btw.
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inStream= null;
    private Thread inThread;
    private volatile boolean stopInThread = true;
    private static final int REQUEST_BT_PERMISSIONS = 100;
    private static final int BATCH_SIZE = 10;
    private static final long BATCH_TIMEOUT_MS = 3000;
    ArrayList<Record> recordList = new ArrayList<>();
    private long lastBatchSentTime = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnMotorBwd = findViewById(R.id.motorbwd);
        btnMotorFwd = findViewById(R.id.motorfwd);
        btnStopB = findViewById(R.id.stop);
        btnRecordB = findViewById(R.id.record);
        btnEstablishConnectionBluetooth = findViewById(R.id.establishc);

        etInput = findViewById(R.id.thinput);
        etInput.setVisibility(View.INVISIBLE);

        txtBluetoothData= findViewById(R.id.showsbtmessages);
        txtStatusBluetooth = findViewById(R.id.connectionstatus);

        spnC = findViewById(R.id.cspinner);
        ArrayAdapter<CharSequence>adapter=ArrayAdapter.createFromResource(this, R.array.materials, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spnC.setAdapter(adapter);

        spnT = findViewById(R.id.tspinner);
        ArrayAdapter<CharSequence>testAdapter = ArrayAdapter.createFromResource(this, R.array.tests, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spnT.setAdapter(testAdapter);

        spnC.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                selectedMaterial = spnC.getSelectedItem().toString();
                if("New material(manually set threshold)".equals(selectedMaterial)){
                    etInput.setVisibility(View.VISIBLE);

                }else{
                    etInput.setVisibility(View.INVISIBLE);
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        spnT.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                selectedTest = spnT.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
        
        btnRecordB.setVisibility(View.INVISIBLE);

        btnMotorBwd.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sendBluetoothCommand("Motor_BWD");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                sendBluetoothCommand("Motor_OFF");
            }
            return true;
        });

        btnMotorFwd.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sendBluetoothCommand("Motor_FWD");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                sendBluetoothCommand("Motor_OFF");
            }
            return true;
        });

        btnEstablishConnectionBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupBluetoothStuff();
            }
        });

        btnStopB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand("Motor_OFF");
                disableInputStream();

            }
        });
        
        btnRecordB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedTest.equals("Tensile(stretching)")) {
                    sendBluetoothCommand("Motor_FWD");
                    connectInputStream(sessionID);
                }else if(selectedTest.equals("Compression")){
                    sendBluetoothCommand("Motor_BWD");
                }
                
                String sessionID = sdf.format(new Date());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

                createSession(session_id, session_name, initial_length, initial_area, test_type);
                connectInputStream(session_id);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // The "finish()" will navigate back to the previous activity.
        return true;
    }

    createSession(session_id, session_name, initial_length, initial_area, test_type) {
        JSONObject initialData = new JSONObject();
        initialData.put("SessionID", session_id);
        initialData.put("SessionName", session_name);
        initialData.put("InitialLength", initial_length);
        initialData.put("InitialArea", initial_area);
        initialData.put("TestType", test_type);
        
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
    }

    private void setupBluetoothStuff(){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            txtBluetoothData.setText(R.string.bluetooth_not_work);
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
        if (!btAdapter.isEnabled()) {
            txtStatusBluetooth.setText(R.string.bluetooth_not_enabled);
            return;
        }
        btnRecordB.setVisibility(View.VISIBLE);
        connectOutputStream();
    }

    private void connectOutputStream(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                Log.e("BT", "Bluetooth permissions aren't allowed");
                runOnUiThread(() -> txtStatusBluetooth.setText(R.string.bluetooth_permissions_not_allowed));
                return;
            }
        }

        try {
            btAdapter.cancelDiscovery();
            BluetoothDevice device = btAdapter.getRemoteDevice(ESP32_MAC_ADDRESS);
            btSocket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
            btSocket.connect();

            outStream = btSocket.getOutputStream();
            runOnUiThread(() -> txtStatusBluetooth.setText(R.string.bluetooth_connected));
            btnRecordB.setVisibility(View.VISIBLE);

            inStream = btSocket.getInputStream();

        } catch (SecurityException se) {
            se.printStackTrace();
            runOnUiThread(() -> txtStatusBluetooth.setText("SecurityException: " + se.getMessage()));
            btnRecordB.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> txtStatusBluetooth.setText("Error: " + e.getMessage()));
            btnRecordB.setVisibility(View.INVISIBLE);
        }

    }

    private void connectInputStream(String sessionID){
        if(!stopInThread){
            return;
        }
        stopInThread =false;

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
                            runOnUiThread(() -> txtStatusBluetooth.setText("Connected and Fetching Data"));

                            //
                        }
                    }catch (SecurityException se) {
                        se.printStackTrace();
                        runOnUiThread(() -> txtStatusBluetooth.setText("SecurityException: " + se.getMessage()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> txtStatusBluetooth.setText("Error: " + e.getMessage()));
                    }
                }
                runOnUiThread(() -> txtStatusBluetooth.setText(R.string.bluetooth_connected));
                btnRecordB.setVisibility(View.VISIBLE);
            }
        });

        inThread.start();
        runOnUiThread(() -> txtStatusBluetooth.setText(R.string.bluetooth_connected));
        btnRecordB.setVisibility(View.INVISIBLE);

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
            // Material ID to be implemented later
            record.materialID = String.valueOf(1);
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
            jsonRecord.put("MaterialID", record.materialID);
            jsonRecord.put("SessionID", record.sessionID);
            jsonRecord.put("Timestamp", record.timestamp);
            jsonRecord.put("Valid", record.valid);

            // Add the JSONObject to the JSONArray
            jsonArray.put(jsonRecord);
        }
        
        new Thread(new Runnable() {
            @Override
            public void run() {
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
            }
        }).start();
    }
    
    private void disableInputStream(){
        if(stopInThread){
            return;
        }
        stopInThread =true;
        if (inThread != null && inThread.isAlive()) {
            inThread.interrupt();
        }
        runOnUiThread(() -> txtStatusBluetooth.setText(R.string.bluetooth_connected));
        btnRecordB.setVisibility(View.VISIBLE);
    }

    private void sendBluetoothCommand(String command) {
        if (btSocket != null && outStream != null) {
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
        txtBluetoothData.setText(displayText);

        //stop if us sensor says too close
        if(Float.valueOf(newMessage.distance.trim())<2.0 || Float.valueOf(newMessage.distance.trim())>10.55){
            Toast.makeText(getApplicationContext(), "Test Finished", Toast.LENGTH_LONG).show();
            sendBluetoothCommand("Motor_OFF");
            disableInputStream();
        }
    }

    private void clearInputStream(){
        byte[] buffer=new byte[1024];
        try{
            while(inStream.available()>0){
                inStream.read(buffer);
            }
        }catch(Exception e){
            e.printStackTrace();
            runOnUiThread(()-> txtStatusBluetooth.setText("Error clearing inStream" + e.getMessage()));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopInThread = true;
        if (inThread != null && inThread.isAlive()) {
            inThread.interrupt();
        }
        try {
            if (btSocket != null) {
                btSocket.close();
            }
        } catch (Exception ignored) {}
    }

    //for menu icon in the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_appbar_resource, menu);

        return true;
    }
}

class Record {
    public String distance;
    public String temperature;
    public String timestamp;
    public String sessionID;
    public String pressure;
    public String valid;
    public String materialID;
}