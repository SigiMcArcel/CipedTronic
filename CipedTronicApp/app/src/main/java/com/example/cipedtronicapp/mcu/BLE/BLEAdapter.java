package com.example.cipedtronicapp.mcu.BLE;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class BLEAdapter {

    public interface OnScanListener {
        void onScanResult(List<BLEScannedDevice> devices, String State);
    }

    private static volatile BLEAdapter _Instance = null;

    private BluetoothManager _BluetoothManager;
    private BluetoothAdapter _BluetoothAdapter;
    private String _BluetoothDeviceAddress;
    private BluetoothGatt _BluetoothGatt;
    private BluetoothLeScanner _Scanner;

    private boolean _Valid;
    private OnScanListener _Listener;
    private List<BLEScannedDevice> _ScannedDeviceList = new ArrayList<>();
    private Context _Context;

    public BLEAdapter(Context context) {
        _Context = context;
        _Valid = false;
        _BluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (_BluetoothManager == null) {
            Log.e("BLEAdapter", "Unable to initialize BluetoothManager");
            return;
        } else {
            _BluetoothAdapter = _BluetoothManager.getAdapter();
            if (_BluetoothAdapter == null) {
                Log.e("BLEAdapter", "Unable to initialize BluetoothAdapter");
                return;
            }
        }
        _Scanner = _BluetoothAdapter.getBluetoothLeScanner();
        _Valid = true;
    }

    //privates
    private boolean startScan(List<ScanFilter> filters,ScanSettings scanSettings)
    {
        try {
            _Scanner.startScan(filters, scanSettings, _LeScanCallback);
        }
        catch(SecurityException exp){
            return false;
        }
        return true;
    }

    private boolean stopScan()
    {
        try {
            _Scanner.stopScan(_LeScanCallback);
        }
        catch(SecurityException exp){
            return false;
        }
        return true;
    }
    //get set
    public boolean Valid() {
        return _Valid;
    }

    public void setOnScanListener(OnScanListener listener) {
        _Listener = listener;
    }

    public BLEDevice getDevice(String address) {
        BluetoothDevice blDev = _BluetoothAdapter.getRemoteDevice(address);
        if (blDev == null) {
            return null;
        }
        return new BLEDevice(_Context,blDev);
    }

    //public
    public boolean scanDevices(String name) {

        if (_Scanner == null) {
            return false;
        }
        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder()
                .setDeviceName(name)
                .build();
        filters.add(filter);
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build();
        startScan(filters, scanSettings);

        new Thread(() -> {
            try {
                Thread.sleep(5000);
                stopScan();

                if(_Listener != null) {
                    _Listener.onScanResult(_ScannedDeviceList,"ok");
                }
            } catch (InterruptedException e) {
                    stopScan();
                if(_Listener != null) {
                    _Listener.onScanResult(_ScannedDeviceList,"Could not start");
                }
                e.printStackTrace();
            }

        }).start();
        return true;
    }

    //callback
    private ScanCallback _LeScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if(result != null)
                    {
                        String deviceName = "";
                        String deviceAddress = "";

                        BLEScannedDevice dev = new BLEScannedDevice();
                        try
                        {
                            dev.Address = result.getDevice().getAddress();
                            dev.Name = result.getDevice().getName();
                        }
                        catch(SecurityException exp)
                        {
                            Log.i("BLEAdapter", " _LeScanCallback" + exp.getMessage());
                        }

                        if(!_ScannedDeviceList.contains(dev) && dev.Name != null)
                        {
                            Log.i("BLEAdapter", " Scan ." + deviceName);
                            _ScannedDeviceList.add(dev);
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


}
