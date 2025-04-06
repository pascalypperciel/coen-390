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
import android.content.res.ColorStateList;
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


import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Objects;

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
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        String deviceName = device.getName();
                        String deviceAddress = device.getAddress();
                        String connectedAddress = null;

                        // Check if we are connected. If we are, don't display connected device in the scanned list.
                        BluetoothManager btManager = BluetoothManager.getInstance();
                        if (btManager.isConnected()) {
                            BluetoothDevice connectedDevice = btManager.getSelectedDevice();
                            connectedAddress = connectedDevice.getAddress();
                        }

                        if (Objects.equals(deviceName, "CAT_Tester") && !deviceAddress.equals(connectedAddress)) {
                            discoveredDevices.add(device);
                            deviceListAdapter.add(deviceName + "\n" + device.getAddress());
                        }
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Button buttonScan = requireView().findViewById(R.id.buttonScan);
                buttonScan.setText("Scan Devices");
                buttonScan.setEnabled(true);
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            requireActivity().unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            // Already unregistered or not registered — safe to ignore
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        }
    }



    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        ImageView imageViewBluetoothStatus = view.findViewById(R.id.imageViewBluetoothStatus);
        ListView listViewDevices = view.findViewById(R.id.listViewDevices);
        textViewConnectionStatus = view.findViewById(R.id.textViewConnectionStatus);
        Button buttonScan = view.findViewById(R.id.buttonScan);
        Button buttonConnect = view.findViewById(R.id.buttonConnect);

        BluetoothManager btManager = BluetoothManager.getInstance();

        if (btManager.isConnected()) {
            buttonConnect.setText("Disconnect");
            textViewConnectionStatus.setTextColor(ThemeManager.getTextColor(requireContext()));
            BluetoothDevice currentDevice = btManager.getSelectedDevice();
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                String name = currentDevice != null ? currentDevice.getName() : "Device";
                textViewConnectionStatus.setText(getString(R.string.connected_to) + name);
            }
        } else {
            buttonConnect.setText("Connect");
            buttonConnect.setEnabled(false);
            textViewConnectionStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            textViewConnectionStatus.setText(getString(R.string.not_connected));
        }

        if (btManager.isConnected() && btManager.getInputStream() != null) {
            BluetoothDevice currentDevice = btManager.getSelectedDevice();
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                String name = currentDevice != null ? currentDevice.getName() : "Device";
                textViewConnectionStatus.setText("Connected to: " + name);
                imageViewBluetoothStatus.setImageResource(R.drawable.ic_bluetooth);
                imageViewBluetoothStatus.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
            }
        } else {
            textViewConnectionStatus.setText("Not Connected");
            imageViewBluetoothStatus.setImageResource(R.drawable.ic_bluetooth_disabled);
            imageViewBluetoothStatus.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
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

            // Button is loading while scanning/discovery
            buttonScan.setEnabled(false);
            buttonScan.setText("Scanning Devices...");

            deviceListAdapter.clear();
            discoveredDevices.clear();
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            bluetoothAdapter.startDiscovery();
        });

        if (Build.VERSION.SDK_INT >= 31) {
        } else {
            buttonScan.setVisibility(View.GONE);
            listViewDevices.setVisibility(View.GONE);
        }

        listViewDevices.setOnItemClickListener((parent, view1, position, id) -> {
            BluetoothDevice selectedDevice = discoveredDevices.get(position);
            btManager.setSelectedDevice(selectedDevice);
            textViewConnectionStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
            textViewConnectionStatus.setText(getString(R.string.selected) + selectedDevice.getName());
            buttonConnect.setEnabled(true);
        });

        buttonConnect.setOnClickListener(v -> {
            if (!btManager.isConnected()) {
                buttonConnect.setText("Connecting");
                buttonConnect.setEnabled(false);

                buttonConnect.post(() -> {
                    btManager.connect(requireContext());

                    if (bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();
                    }

                    buttonScan.setText("Scan Devices");
                    buttonScan.setEnabled(true);

                    textViewConnectionStatus.setTextColor(ThemeManager.getTextColor(requireContext()));
                    BluetoothDevice currentDevice = btManager.getSelectedDevice();
                    String deviceName = (currentDevice != null) ? currentDevice.getName() : "Unknown";
                    textViewConnectionStatus.setText("Connected to: " + deviceName);

                    buttonConnect.setText("Disconnect");
                    buttonConnect.setEnabled(true);

                    imageViewBluetoothStatus.setImageResource(R.drawable.ic_bluetooth);
                    imageViewBluetoothStatus.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
                });
            } else {
                btManager.disconnect(requireContext());
                textViewConnectionStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                textViewConnectionStatus.setText("Disconnected");
                buttonConnect.setText("Connect");
                buttonConnect.setEnabled(true);
                imageViewBluetoothStatus.setImageResource(R.drawable.ic_bluetooth_disabled);
                imageViewBluetoothStatus.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            }
        });


        // Register BroadcastReceiver for Bluetooth discovery
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        requireActivity().registerReceiver(receiver, filter);

        view.setBackgroundColor(ThemeManager.getBackgroundColor(requireContext()));

        buttonScan.setBackgroundTintList(ColorStateList.valueOf(ThemeManager.getButtonColor(requireContext())));
        buttonScan.setTextColor(ThemeManager.getTextColor(requireContext()));

        buttonConnect.setBackgroundTintList(ColorStateList.valueOf(ThemeManager.getButtonColor(requireContext())));
        buttonConnect.setTextColor(ThemeManager.getTextColor(requireContext()));

        MaterialCardView cardView = view.findViewById(R.id.cardViewBluetooth);
        cardView.setStrokeColor(ThemeManager.getButtonColor(requireContext()));
        cardView.setCardBackgroundColor(ThemeManager.getBackgroundColor(requireContext()));

        textViewConnectionStatus.setTextColor(ThemeManager.getTextColor(requireContext()));

        listViewDevices.setBackgroundColor(ThemeManager.getBackgroundColor(requireContext()));


        return view;
    }
}
