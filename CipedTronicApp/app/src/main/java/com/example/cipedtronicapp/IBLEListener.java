package com.example.cipedtronicapp;

import java.util.List;

public interface IBLEListener {
    void onDataReceived(byte[] data);

    void onErrorReceived(String data);

    void onDeviceConnected();

    void onDeviceDisconnected();

    void onScanResult(List<CipedTronicDevice> devices);

}