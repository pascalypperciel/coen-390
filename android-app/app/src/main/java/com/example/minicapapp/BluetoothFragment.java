package com.example.minicapapp;

import android.Manifest;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;


import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.S)
public class BluetoothFragment extends Fragment {
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> deviceListAdapter;
    private final ArrayList<BluetoothDevice> discoveredDevices = new ArrayList<>();
    private TextView textViewConnectionStatus;


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !discoveredDevices.contains(device)) {
                    discoveredDevices.add(device);
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        String name = device.getName() != null ? device.getName() : "Unnamed Device";
                        deviceListAdapter.add(name + "\n" + device.getAddress());
                    }
                }
            }
        }
    };


    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean scanGranted = result.getOrDefault(Manifest.permission.BLUETOOTH_SCAN, false);
                Boolean connectGranted = result.getOrDefault(Manifest.permission.BLUETOOTH_CONNECT, false);
                Boolean locationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);

                if (Boolean.TRUE.equals(scanGranted) && Boolean.TRUE.equals(connectGranted) && Boolean.TRUE.equals(locationGranted)) {
                    startDiscovery();
                } else {
                    Toast.makeText(requireContext(), "Permissions denied.", Toast.LENGTH_SHORT).show();
                }
            });

    private void startDiscovery() {
        deviceListAdapter.clear();
        discoveredDevices.clear();
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        }
        boolean started = bluetoothAdapter.startDiscovery();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        requireActivity().unregisterReceiver(receiver);
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.cancelDiscovery();
        }
    }


    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        ListView listViewDevices = view.findViewById(R.id.listViewDevices);
        textViewConnectionStatus = view.findViewById(R.id.textViewConnectionStatus);
        Button buttonScan = view.findViewById(R.id.buttonScan);
        Button buttonConnect = view.findViewById(R.id.buttonConnect);

        BluetoothManager btManager = BluetoothManager.getInstance();

        if (btManager.isConnected() && btManager.getInputStream() != null) {
            textViewConnectionStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
            BluetoothDevice currentDevice = btManager.getSelectedDevice();
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                String name = currentDevice != null ? currentDevice.getName() : "Device";
                textViewConnectionStatus.setText(getString(R.string.connected_to) + name);
            }
        } else {
            textViewConnectionStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            textViewConnectionStatus.setText(R.string.not_connected);
        }

        deviceListAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_single_choice);
        listViewDevices.setAdapter(deviceListAdapter);
        listViewDevices.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        buttonScan.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                permissionLauncher.launch(new String[] {
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION
                });
                return;
            }

            deviceListAdapter.clear();
            discoveredDevices.clear();
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            bluetoothAdapter.startDiscovery();
        });


        listViewDevices.setOnItemClickListener((parent, view1, position, id) -> {
            BluetoothDevice selectedDevice = discoveredDevices.get(position);
            btManager.setSelectedDevice(selectedDevice);
            textViewConnectionStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
            textViewConnectionStatus.setText(getString(R.string.selected) + selectedDevice.getName());
        });

        buttonConnect.setOnClickListener(v -> {
            if (!btManager.isConnected()) {
                btManager.connect(requireContext());
                textViewConnectionStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
                BluetoothDevice currentDevice = btManager.getSelectedDevice();
                String deviceName = (currentDevice != null) ? currentDevice.getName() : "Unknown";
                textViewConnectionStatus.setText("Connected to: " + deviceName);

            } else {
                btManager.disconnect(requireContext());
                textViewConnectionStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                textViewConnectionStatus.setText("Disconnected");
            }
        });

        // Register BroadcastReceiver for Bluetooth discovery
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        requireActivity().registerReceiver(receiver, filter);

        return view;
    }
}
