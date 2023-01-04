package com.example.cipedtronicapp.mcu.BLE;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.example.cipedtronicapp.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BLEService {

    BluetoothGattService _Service;
    BLEDevice _BleDevice;
    List<UUID> _NotificationUuids = new ArrayList<>();

    public BLEService()
    {

    }

    public BLEService(BluetoothGattService service,BLEDevice device)
    {
        _Service = service;
        _BleDevice = device;
    }

    public boolean addNotificationUuid(UUID characteristicUuid)
    {
        if(_Service == null)
        {
            return false;
        }
        BluetoothGattCharacteristic characteristic = _Service.getCharacteristic(characteristicUuid);
        if (characteristic == null) {
            return false;
        }

        return true;
    }


    public boolean writeCharacteristic(byte[] value, UUID characteristicUuid)
    {
        if (_Service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = _Service.getCharacteristic(characteristicUuid);
        if (characteristic == null) {
            return false;
        }
        characteristic.setValue(value);
        return _BleDevice.writeCharacteristic(characteristic);
    }

    public boolean readCharacteristic(byte[] value, UUID characteristicUuid)
    {
        if (_Service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = _Service.getCharacteristic(characteristicUuid);
        if (characteristic == null) {
            return false;
        }
        characteristic.setValue(value);
        return _BleDevice.readCharacteristic(characteristic);
    }


    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

        if (newState == BluetoothProfile.STATE_CONNECTED)
        {

        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

        }
    }

    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {

        } else {
            Log.w("BLEService", "onServicesDiscovered received: " + status);
        }
    }

    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic,
                                     int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {

            // _IBLEListener.onDataReceived(characteristic.getValue());
            Log.i("BLEService", Utilities.bytesToString(characteristic.getValue()));
        }
    }

    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic,byte[] value) {


    }

}
