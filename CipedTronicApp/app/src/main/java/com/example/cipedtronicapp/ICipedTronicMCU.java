package com.example.cipedtronicapp;

import java.util.List;

public interface ICipedTronicMCU {
    public void onDeviceScanResult(List<CipedTronicDevice> devices);
    public void onDeviceError(String error);
    public void onStateChanged(String state);
    public void onDataUpdate(CipedtronicData data);
}
