package com.example.cipedtronicapp.mcu.SerialBLE;

import java.util.List;

public interface OnBLESerialListener {
    void onDataReceived(byte[] data);

    void onErrorReceived(String data);

    void onDeviceConnected();

    void onDeviceDisconnected();

    void onScanResult(List<BLEScannedDevice> devices);

}