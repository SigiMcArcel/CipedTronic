package com.example.cipedtronicapp.mcu.mcu;

import com.example.cipedtronicapp.mcu.BLE.BLEScannedDevice;

import java.util.List;

public interface ICipedTronicMCU {
    public void onDeviceScanResult(List<BLEScannedDevice> devices);
    public void onDeviceError(String error);
    public void onStateChanged(String state);
    public void onDataUpdate(CipedtronicData data);
}
