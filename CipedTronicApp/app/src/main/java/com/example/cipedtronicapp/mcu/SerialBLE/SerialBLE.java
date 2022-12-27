
/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.example.cipedtronicapp.mcu.SerialBLE;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;

import java.lang.Thread;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.os.ParcelUuid;

import androidx.core.app.ActivityCompat;

import com.example.cipedtronicapp.Utilities;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class SerialBLE {
    private final static String TAG = SerialBLE.class.getSimpleName();

    private OnBLESerialListener _IBLEListener;
    private BluetoothManager _BluetoothManager;
    private BluetoothAdapter _BluetoothAdapter;
    private String _BluetoothDeviceAddress;
    private BluetoothGatt _BluetoothGatt;
    private Context _Context;
    private int _ConnectionState = STATE_DISCONNECTED;
    private Handler _ScanHandler;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.nordicsemi.nrfUART.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.nordicsemi.nrfUART.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.nordicsemi.nrfUART.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.nordicsemi.nrfUART.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.nordicsemi.nrfUART.EXTRA_DATA";
    public final static String DEVICE_DOES_NOT_SUPPORT_UART =
            "com.nordicsemi.nrfUART.DEVICE_DOES_NOT_SUPPORT_UART";

    public static final UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    private boolean scanning;
    private BluetoothLeScanner scanner;
    // Stops scanning after 10 seconds.
    static final long SCAN_PERIOD = 10000;
    private List<BLEScannedDevice> list = new ArrayList<>();
    private int advCount = 0;


    List<BluetoothDevice> deviceList;

    public SerialBLE(Context context) {
        _Context = context;
        _ScanHandler = new Handler();
    }

    public void setOnBLESerialListener(OnBLESerialListener listener)
    {
        _IBLEListener = listener;
    }
    public void stopCipetTronicDevices() {
        Log.i(TAG, "stop Scan .");
        scanner.stopScan(_LeScanCallback);
    }

    public void searchCipetTronicDevices() {

        _ScanHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scanner.stopScan(_LeScanCallback);
                _IBLEListener.onScanResult(list);
            }
        },5000);
            List<ScanFilter> filters = new ArrayList<>();
            ScanFilter filter = new ScanFilter.Builder()
                    .setDeviceName("ADA_BLE")
                    .setServiceUuid(new ParcelUuid(RX_SERVICE_UUID))
                    .build();
            filters.add(filter);
            ScanSettings scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .build();


            Log.i(TAG, "start Scan .");
            advCount = 0;
            if (ActivityCompat.checkSelfPermission(_Context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                Log.i(TAG, "No Permission for Scan");

            }
            scanner.startScan(filters, scanSettings, _LeScanCallback);

    }

    private ScanCallback _LeScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if(result != null)
                    {
                        BLEScannedDevice dev = new BLEScannedDevice();
                        dev.Address = result.getDevice().getAddress();
                        dev.Name = result.getDevice().getName();
                        if(!list.contains(dev) && dev.Name != null)
                        {
                            Log.i(TAG, " Scan ." + result.getDevice().getName());
                            list.add(dev);
                        }

                    }
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);

                }

                @Override
                public void onScanFailed(int errorCode)
                {
                    super.onScanFailed(errorCode);
                }
            };


    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback _GattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTED)
            {

                _ConnectionState = STATE_CONNECTED;
                _IBLEListener.onDeviceConnected();
                Log.i(TAG, "Connected to GATT server.");

                Log.i(TAG, "Attempting to start service discovery:" +
                        _BluetoothGatt.discoverServices());


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                _ConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                _IBLEListener.onDeviceDisconnected();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "mBluetoothGatt = " + _BluetoothGatt);
                enableTXNotification();

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

               // _IBLEListener.onDataReceived(characteristic.getValue());
                Log.i(TAG, Utilities.bytesToString(characteristic.getValue()));
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            _IBLEListener.onDataReceived(characteristic.getValue());

        }
    };

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize(OnBLESerialListener IBLEListener)
    {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        this._IBLEListener = IBLEListener;
        if (_BluetoothManager == null)
        {
            _BluetoothManager = (BluetoothManager) _Context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (_BluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        _BluetoothAdapter = _BluetoothManager.getAdapter();
        if (_BluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        scanner = _BluetoothAdapter.getBluetoothLeScanner();
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {

            if (_BluetoothAdapter == null || address == null) {
                Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
                return false;
            }
        new Thread(() -> {
            // Previously connected device.  Try to reconnect.
            if (_BluetoothDeviceAddress != null && address.equals(_BluetoothDeviceAddress)
                    && _BluetoothGatt != null) {
                Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");

                if (_BluetoothGatt.connect()) {
                    _ConnectionState = STATE_CONNECTING;
                    return;
                } else {
                    return;
                }
            }
            final BluetoothDevice device;
            try {
                device = _BluetoothAdapter.getRemoteDevice(address);
            } catch (Exception exp) {
                Log.d(TAG, exp.getMessage());
                return;
            }
            if (device == null) {
                Log.w(TAG, "Device not found.  Unable to connect.");
                return;
            }
            // We want to directly connect to the device, so we are setting the autoConnect
            // parameter to false.
            _BluetoothGatt = device.connectGatt(_Context, false, _GattCallback);
            Log.d(TAG, "Trying to create a new connection.");
            _BluetoothDeviceAddress = address;
            _ConnectionState = STATE_CONNECTING;
        }).start();
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (_BluetoothAdapter == null || _BluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized dissconnect");
            return;
        }
        new Thread(() -> {
            _BluetoothGatt.disconnect();
        }).start();
       // mBluetoothGatt.close();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (_BluetoothGatt == null) {
            return;
        }
        Log.w(TAG, "mBluetoothGatt closed");
        scanner.stopScan(_LeScanCallback);
        _BluetoothDeviceAddress = "";
        _BluetoothGatt.close();
        _BluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (_BluetoothAdapter == null || _BluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized readCharacteristic");
            return;
        }
        _BluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *

    */
    
    /**
     * Enable Notification on TX characteristic
     *
     * @return 
     */
    public void enableTXNotification()
    { 
    	/*
    	if (mBluetoothGatt == null) {
    		showMessage("mBluetoothGatt null" + mBluetoothGatt);
    		broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
    		return;
    	}
    		*/
    	BluetoothGattService RxService = _BluetoothGatt.getService(RX_SERVICE_UUID);
    	if (RxService == null) {
            showMessage("Rx service not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
    	BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            showMessage("Tx charateristic not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        _BluetoothGatt.setCharacteristicNotification(TxChar,true);
        
        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        _BluetoothGatt.writeDescriptor(descriptor);
    	
    }
    
    public void writeRXCharacteristic(byte[] value)
    {
    
    	
    	BluetoothGattService RxService = _BluetoothGatt.getService(RX_SERVICE_UUID);
    	showMessage("mBluetoothGatt null"+ _BluetoothGatt);
    	if (RxService == null) {
            showMessage("Rx service not found!");

            return;
        }
    	BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            showMessage("Rx charateristic not found!");
            return;
        }
        RxChar.setValue(value);
    	boolean status = _BluetoothGatt.writeCharacteristic(RxChar);
    	
        Log.d(TAG, "write TXchar - status=" + status);  
    }
    
    private void showMessage(String msg) {
        Log.e(TAG, msg);
    }
    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (_BluetoothGatt == null) return null;

        return _BluetoothGatt.getServices();
    }
}
