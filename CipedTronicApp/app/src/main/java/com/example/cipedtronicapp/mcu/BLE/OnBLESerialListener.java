package com.example.cipedtronicapp.mcu.BLE;

import java.util.List;

public interface OnBLESerialListener {
    void onDataReceived(byte[] data);

    void onErrorReceived(String data);

    void onDeviceConnected();

    void onDeviceDisconnected();

    void onScanResult(List<BLEScannedDevice> devices);

}