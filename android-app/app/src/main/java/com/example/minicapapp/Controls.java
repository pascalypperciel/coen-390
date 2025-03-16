package com.example.minicapapp;

import static java.lang.Float.isNaN;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.View;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.util.Log;

import android.Manifest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.i18n.DateTimeFormatter;

// For batch processing
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Controls extends AppCompatActivity {
    //private TextView txtResponse;
    //private TextView txtBluetoothData;
    private Button btnFetch;
    protected Button bmotorfwd, bmotorbwd,stopb,establishcb;
    protected static Button recordb;
    protected Toolbar toolbar;

    protected TextView  statusbth, txtBluetoothData;
    protected Spinner cspinner, tspinner;
    protected EditText thinput;

    protected String selectedTest, selectedMaterial;

    private static final String ESP32_MAC_ADDRESS = "20:43:A8:64:E6:9E"; //Change this if we change board btw.
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inStream= null;

    //private boolean isReadingInputStream=false;

    private Thread inThread;
    private volatile boolean stopinThread = true;
    private static final int REQUEST_BT_PERMISSIONS = 100;
    private final List<String> lastThreeMessages = new LinkedList<>();

    private static final int BATCH_SIZE = 10;
    private static final long BATCH_TIMEOUT_MS = 3000;
    private final List<JSONObject> batchRecords = new ArrayList<>();
    private long lastBatchSentTime = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bmotorbwd = findViewById(R.id.motorbwd);
        bmotorfwd = findViewById(R.id.motorfwd);
        stopb = findViewById(R.id.stop);
        recordb= findViewById(R.id.record);
        establishcb= findViewById(R.id.establishc);

        thinput= findViewById(R.id.thinput);
        thinput.setVisibility(View.INVISIBLE);

        txtBluetoothData= findViewById(R.id.showsbtmessages);
        statusbth= findViewById(R.id.connectionstatus);

        cspinner= findViewById(R.id.cspinner);
        ArrayAdapter<CharSequence>adapter=ArrayAdapter.createFromResource(this, R.array.materials, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        cspinner.setAdapter(adapter);

        tspinner= findViewById(R.id.tspinner);
        ArrayAdapter<CharSequence>testadapter=ArrayAdapter.createFromResource(this, R.array.tests, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        tspinner.setAdapter(testadapter);

        cspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                selectedMaterial = cspinner.getSelectedItem().toString();
                Toast.makeText(getApplicationContext(), "you selected: " + selectedMaterial, Toast.LENGTH_LONG).show();
                if("New material(manually set threshold)".equals(selectedMaterial)){
                    Toast.makeText(getApplicationContext(), "HAPPY BIRTHDAY TO NEW MATERIAL", Toast.LENGTH_LONG).show();
                    thinput.setVisibility(View.VISIBLE);

                }else{
                    thinput.setVisibility(View.INVISIBLE);
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        tspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                selectedTest = tspinner.getSelectedItem().toString();
                Toast.makeText(getApplicationContext(), "you selected: " + selectedTest, Toast.LENGTH_LONG).show();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });



        //btnFetch.setOnClickListener(v -> new FetchDataTask().execute());

        //setupbtstuff();
        recordb.setVisibility(View.INVISIBLE);



        bmotorbwd.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sendBluetoothCommand("LED_ON");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                sendBluetoothCommand("LED_OFF");
            }
            return true;
        });

        bmotorfwd.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sendBluetoothCommand("LED_ON");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                sendBluetoothCommand("LED_OFF");
            }
            return true;
        });

        establishcb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupbtstuff();
            }
        });

        stopb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand("LED_OFF");
                disableinputstream();

            }
        });
        recordb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendBluetoothCommand("LED_OFF");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String sessionID = sdf.format(new Date());
                connectinputstream(sessionID);
            }
        });
    }

    private void setupbtstuff(){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            txtBluetoothData.setText("Bluetooth don't work");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            int scanPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
            int connectPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);

            if (scanPerm != PackageManager.PERMISSION_GRANTED || connectPerm != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BT_PERMISSIONS);
            } else {
                startBluetoothoutThread();
            }
        } else {
            startBluetoothoutThread();
        }
    }
    private void startBluetoothoutThread() {
        if (!btAdapter.isEnabled()) {
            statusbth.setText("Must enable Bluetooth");
            return;
        }
        recordb.setVisibility(View.VISIBLE);
        connectoutputstream();
        //btThread = new Thread(this::connectoutputstream);
        //btThread.start();
    }

    private void connectoutputstream(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                Log.e("BT", "Bluetooth permissions aren't allowed");
                runOnUiThread(() -> statusbth.setText("Bluetooth permissions aren't allowed"));
                return;
            }
        }

        try {
            btAdapter.cancelDiscovery();
            BluetoothDevice device = btAdapter.getRemoteDevice(ESP32_MAC_ADDRESS);
            btSocket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
            btSocket.connect();

            outStream = btSocket.getOutputStream();
            runOnUiThread(() -> statusbth.setText("connected to CAT tester"));
            recordb.setVisibility(View.VISIBLE);

            inStream = btSocket.getInputStream();

        } catch (SecurityException se) {
            se.printStackTrace();
            runOnUiThread(() -> statusbth.setText("SecurityException: " + se.getMessage()));
            recordb.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> statusbth.setText("Error: " + e.getMessage()));
            recordb.setVisibility(View.INVISIBLE);
        }

    }

    private void connectinputstream(String sessionID){
        if(!stopinThread){
            return;
        }
        stopinThread=false;

        inThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[128];
                int bytesRead;
                clearInputStream();
                while (!stopinThread) {
                    try {
                        bytesRead = inStream.read(buffer);
                        if (bytesRead > 0) {
                            final String incoming = new String(buffer, 0, bytesRead).trim();
                            processBluetoothData(incoming, sessionID);

                            //
                        }
                    }catch (SecurityException se) {
                        se.printStackTrace();
                        runOnUiThread(() -> statusbth.setText("SecurityException: " + se.getMessage()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> statusbth.setText("Error: " + e.getMessage()));
                    }
                }
                runOnUiThread(() -> statusbth.setText("Connected to CAT Tester"));
                recordb.setVisibility(View.VISIBLE);
            }
        });

        inThread.start();
        runOnUiThread(() -> statusbth.setText("Connected to CAT Tester"));
        recordb.setVisibility(View.INVISIBLE);

    }

    private void processBluetoothData(String incoming, String sessionID) throws JSONException {
        String[] values = incoming.split(";");
        Record record = new Record();
        ArrayList<Record> recordList = new ArrayList<Record>();
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
        }

        long currentTime = System.currentTimeMillis();

        if (recordList.size() >= BATCH_SIZE || (currentTime - lastBatchSentTime) >= BATCH_TIMEOUT_MS) {
            sendBatchData(recordList);
            lastBatchSentTime = currentTime;
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
                    URL url = new URL("http://10.0.2.2:5000/batch-process-records");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    os.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_CREATED && responseCode != HttpURLConnection.HTTP_OK) {
                        System.err.println("Batch processing failed: " + responseCode);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void disableinputstream(){
        if(stopinThread){
            return;
        }
        stopinThread=true;
        if (inThread != null && inThread.isAlive()) {
            inThread.interrupt();
        }
        runOnUiThread(() -> statusbth.setText("Connected to CAT Tester"));
        recordb.setVisibility(View.VISIBLE);
    }
    private void connectAndReadBT() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                Log.e("BT", "Bluetooth permissions aren't allowed");
                runOnUiThread(() -> statusbth.setText("Bluetooth permissions aren't allowed"));
                return;
            }
        }

        try {
            btAdapter.cancelDiscovery();
            BluetoothDevice device = btAdapter.getRemoteDevice(ESP32_MAC_ADDRESS);
            btSocket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
            btSocket.connect();

            outStream = btSocket.getOutputStream();

            InputStream inStream = btSocket.getInputStream();
            byte[] buffer = new byte[1024];
            while (!stopinThread) {
                int bytesRead = inStream.read(buffer);
                if (bytesRead > 0) {
                    final String incoming = new String(buffer, 0, bytesRead);
                    runOnUiThread(() -> statusbth.setText("connected and getting data"));
                    runOnUiThread(() -> updateBluetoothDisplay(incoming));
                }
                runOnUiThread(() ->statusbth.setText("Connected to CAT Tester"));
                recordb.setVisibility(View.VISIBLE);
            }
        } catch (SecurityException se) {
            se.printStackTrace();
            runOnUiThread(() -> statusbth.setText("SecurityException: " + se.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> statusbth.setText("Error: " + e.getMessage()));
        }
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

    private void updateBluetoothDisplay(String newMessage) {
        if (lastThreeMessages.size() >= 3) {
            lastThreeMessages.remove(0);
        }
        lastThreeMessages.add(newMessage);

        String displayedText = String.join("\n", lastThreeMessages);
        txtBluetoothData.setText(displayedText);
    }

    private void clearInputStream(){
        byte[] buffer=new byte[1024];
        int bytesRead;
        try{
            while(inStream.available()>0){
                bytesRead=inStream.read(buffer);
            }
        }catch(Exception e){
            e.printStackTrace();
            runOnUiThread(()->statusbth.setText("Error clearing inputstream" + e.getMessage()));
        }
    }

    private class FetchDataTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL("http://10.0.2.2:5000/get-all");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                return result.toString();
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            String existingText = statusbth.getText().toString();
            String newText = result + "\n\n" + existingText;
            statusbth.setText(newText);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopinThread = true;
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
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Menu menu=toolbar.getMenu();

        MenuItem mbhelp = menu.findItem(R.id.mbhelp);
        MenuItem mbsettings = menu.findItem(R.id.mbsettings);
        int id=item.getItemId();
        if (mbhelp.getItemId()==id) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new HelpFrag()).commit();
            //Toast.makeText(getApplicationContext(), "clicked on go to help", Toast.LENGTH_LONG).show();

        }
        else if(mbsettings.getItemId()==id) {
            Intent sintent= new Intent(this, Settings.class);
            startActivity(sintent);
            //Toast.makeText(getApplicationContext(), "clicked on go to settings", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
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