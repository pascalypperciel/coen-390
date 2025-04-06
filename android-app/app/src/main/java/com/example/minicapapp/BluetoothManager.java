package com.example.minicapapp;

import android.Manifest;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothManager {
    private static BluetoothManager instance;
    private final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket bluetoothSocket;
    private OutputStream outStream;
    private InputStream inStream;
    private boolean isConnected = false;
    private BluetoothDevice selectedDevice;


    public static BluetoothManager getInstance() {
        if (instance == null) {
            instance = new BluetoothManager();
        }
        return instance;
    }


    public void setSelectedDevice(BluetoothDevice device) {
        this.selectedDevice = device;
    }


    public boolean connect(Context context) {
        if (isConnected || selectedDevice == null) {
            Toast.makeText(context, "No device selected", Toast.LENGTH_SHORT).show();
            Log.e("BluetoothManager", "Connection attempt failed: no selected device or already connected.");
            return false;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show();
            Log.e("BluetoothManager", "Missing BLUETOOTH_CONNECT permission");
            return false;
        }

        try {
            bluetoothSocket = selectedDevice.createRfcommSocketToServiceRecord(SPP_UUID);
            bluetoothSocket.connect();

            outStream = bluetoothSocket.getOutputStream();
            inStream = bluetoothSocket.getInputStream();
            isConnected = true;

            Toast.makeText(context, "Connected to " + selectedDevice.getName(), Toast.LENGTH_SHORT).show();
            Log.i("BluetoothManager", "Connected to " + selectedDevice.getName() + " (" + selectedDevice.getAddress() + ")");
            return true;

        } catch (IOException e) {
            Log.e("BluetoothManager", "Connection failed: IOException - " + e.getMessage(), e);
            Toast.makeText(context, "Connection failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("BluetoothManager", "Connection failed: Unexpected error", e);
            Toast.makeText(context, "Unexpected connection error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        isConnected = false;
        return false;
    }


    public void disconnect(Context context) {
        try {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }

            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
            isConnected = false;
            Toast.makeText(context, "Bluetooth Disconnected", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("BluetoothManager", "Disconnection failed", e);
            Toast.makeText(context, "Bluetooth Disconnection Failed", Toast.LENGTH_SHORT).show();
        }
    }


    public boolean isConnected() {
        return isConnected;
    }


    public void sendCommand(String command) {
        if (bluetoothSocket != null && outStream != null) {
            try {
                outStream.write((command + "\n").getBytes());
                outStream.flush();
            } catch (Exception e) {
                Log.e("BluetoothManager", "Error sending command: " + e.getMessage());
            }
        }
    }


    public InputStream getInputStream() {
        return inStream;
    }


    public BluetoothDevice getSelectedDevice() {
        return selectedDevice;
    }
}
