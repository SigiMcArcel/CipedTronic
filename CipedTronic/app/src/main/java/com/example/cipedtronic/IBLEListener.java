package com.example.cipedtronic;

import java.util.List;

public interface IBLEListener {
    void onDataReceived(byte[] data);

    void onErrorReceived(String data);

    void onDeviceReady(ResponseStatus responseStatus);

    void onDeviceDisconnected();

    void onScanResult(List<CipedTronicDevice> devices);

}