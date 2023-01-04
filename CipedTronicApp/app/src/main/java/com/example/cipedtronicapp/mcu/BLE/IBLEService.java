package com.example.cipedtronicapp.mcu.BLE;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

public interface IBLEService {
    boolean addNotificationUuid(UUID characteristicUuid);
    boolean writeCharacteristic(byte[] value);
    boolean readCharacteristic(byte[] value, UUID characteristicUuid);
    void onConnectionStateChange(int newState);
    void onCharacteristicRead(byte[] value,UUID characteristicUuid);
    void onCharacteristicChanged(byte[] value,UUID characteristicUuid);
}
