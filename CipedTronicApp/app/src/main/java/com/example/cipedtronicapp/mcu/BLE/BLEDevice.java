package com.example.cipedtronicapp.mcu.BLE;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class BLEDevice {


    public interface OnBLEGATTListener
    {
        void OnStatusChanged(int Status);
        void onCharacteristicRead(BluetoothGattCharacteristic characteristic,byte[] value);
        void onCharacteristicChanged(BluetoothGattCharacteristic characteristic,byte[] value);
        void onCharacteristicWrite();
    }
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private String _Address = "";
    private BluetoothDevice _BluetoothDevice;
    private BluetoothGatt _BluetoothGatt;
    private Context _Context;
    private Map<UUID,BLEService> _Services = new LinkedHashMap<>();
    private int _Status = 0;
    private OnBLEGATTListener _GattListener;
    private ArrayDeque<BluetoothGattCharacteristic> _Characteristics = new ArrayDeque<>();

    public BLEDevice(String address, BluetoothDevice device,Context context)
    {
        _BluetoothDevice = device;
        _Context = context;
        _Address = address;
    }



    public void setOnOnBLEDeviceGATTListener(OnBLEGATTListener listener)
    {
        _GattListener = listener;
    }

    public boolean connect() {
        try {
            _BluetoothGatt = _BluetoothDevice.connectGatt(_Context, false, _GattCallback);
            if(_BluetoothGatt == null)
            {
                return false;
            }


        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "connect " + exp.getMessage());
            return false;
        }
        return true;
    }

    public void disconnect() {
        try {
            _BluetoothGatt.disconnect();
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "disconnect " + exp.getMessage());
        }
    }
    public void close() {
        try {
        _BluetoothGatt.close();
        }
        catch(SecurityException exp) {
            Log.w("BLEDevice", "close " + exp.getMessage());
        }
    }
    public BluetoothGattService getService(UUID serviceUuid)
    {
        if(_BluetoothGatt == null) {
            return null;
        }
        return _BluetoothGatt.getService(serviceUuid);
    }



    public boolean setCharacteristicNotifications(ArrayDeque<BluetoothGattCharacteristic> characteristics)
    {
        if(characteristics.size() == 0)
        {
            return false;
        }
        _Characteristics = characteristics;
        setNotificationFromQueue();
        return true;
    }

    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if(_BluetoothGatt == null) {
            return false;
        }
        try {
            return _BluetoothGatt.writeCharacteristic(characteristic);
        }
        catch(SecurityException exp) {
            Log.w("BLEDevice", "writeCharacteristic " + exp.getMessage());
            return false;
        }
    }

    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if(_BluetoothGatt == null) {
            return false;
        }
       try{
           return _BluetoothGatt.readCharacteristic(characteristic);
       }
       catch(SecurityException exp) {
           Log.e("BLEDevice", "readCharacteristic " + exp.getMessage());
           return false;
       }

    }

    private void setNotificationFromQueue()
    {
        if(_Characteristics.size() == 0)
        {
            return;
        }
        BluetoothGattCharacteristic ch = _Characteristics.pop();
        if(ch == null)
        {
            return;
        }
        try {
            _BluetoothGatt.setCharacteristicNotification(ch, true);
            BluetoothGattDescriptor descriptor = ch.getDescriptor(CCCD);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            _BluetoothGatt.writeDescriptor(descriptor);
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "writeCharacteristic " + exp.getMessage());
            return;
        }
    }

    //callBack
    private final BluetoothGattCallback _GattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            _Status = newState;
            if(newState == BluetoothProfile.STATE_CONNECTED){
                try {
                    _BluetoothGatt.discoverServices();
                }
                catch(SecurityException exp) {
                    Log.e("BLEDevice", "discoverServices " + exp.getMessage());
                    return;
                }
            }
            else
            {
                if(_GattListener != null)
                {
                    _GattListener.OnStatusChanged(_Status);
                }
            }
        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            setNotificationFromQueue();
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            if(_GattListener != null) {
                _GattListener.onCharacteristicWrite();
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

            } else {
                Log.e("BLEDevice", "onServicesDiscovered received: " + status);
            }

            if(_Status == BluetoothProfile.STATE_CONNECTED) {

                if(_GattListener != null)
                {
                    _GattListener.OnStatusChanged(_Status);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         byte[] value,
                                         int status) {
            if(_GattListener != null)
            {
                _GattListener.onCharacteristicRead(characteristic,value);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic,byte[] value) {
            if(_GattListener != null)
            {
                _GattListener.onCharacteristicChanged(characteristic,value);
            }

        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            if(_GattListener != null)
            {
                _GattListener.onCharacteristicChanged(characteristic,characteristic.getValue());
            }

        }
    };

}
