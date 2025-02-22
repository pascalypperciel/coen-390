package com.example.minicapapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;

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
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private TextView txtResponse;
    private TextView txtBluetoothData;
    private Button btnFetch;
    private Button btnLED;

    private static final String ESP32_MAC_ADDRESS = "20:43:A8:64:E6:9E"; //Change this if we change board btw.
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    private Thread btThread;
    private volatile boolean stopBTThread = false;
    private static final int REQUEST_BT_PERMISSIONS = 100;
    private final List<String> lastThreeMessages = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtBluetoothData = findViewById(R.id.txtBluetoothData);
        txtResponse = findViewById(R.id.txtResponse);
        btnFetch = findViewById(R.id.btnFetch);
        btnLED = findViewById(R.id.btnLED);

        btnFetch.setOnClickListener(v -> new FetchDataTask().execute());

        btnLED.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sendBluetoothCommand("LED_ON");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                sendBluetoothCommand("LED_OFF");
            }
            return true;
        });

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
                startBluetoothThread();
            }
        } else {
            startBluetoothThread();
        }
    }

    private void startBluetoothThread() {
        if (!btAdapter.isEnabled()) {
            txtResponse.setText("Must enable Bluetooth");
            return;
        }
        btThread = new Thread(this::connectAndReadBT);
        btThread.start();
    }

    private void connectAndReadBT() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                Log.e("BT", "Bluetooth permissions aren't allowed");
                runOnUiThread(() -> txtResponse.setText("Bluetooth permissions aren't allowed"));
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
            while (!stopBTThread) {
                int bytesRead = inStream.read(buffer);
                if (bytesRead > 0) {
                    final String incoming = new String(buffer, 0, bytesRead);
                    runOnUiThread(() -> updateBluetoothDisplay(incoming));
                }
            }
        } catch (SecurityException se) {
            se.printStackTrace();
            runOnUiThread(() -> txtResponse.setText("SecurityException: " + se.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> txtResponse.setText("Error: " + e.getMessage()));
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
            String existingText = txtResponse.getText().toString();
            String newText = result + "\n\n" + existingText;
            txtResponse.setText(newText);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBTThread = true;
        if (btThread != null && btThread.isAlive()) {
            btThread.interrupt();
        }
        try {
            if (btSocket != null) {
                btSocket.close();
            }
        } catch (Exception ignored) {}
    }
}
