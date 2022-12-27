package com.example.cipedtronicapp.ui.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;
import androidx.preference.PreferenceManager;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.cipedtronicapp.mcu.SerialBLE.BLEScannedDevice;
import com.example.cipedtronicapp.mcu.mcu.CipedTronicMCU;
import com.example.cipedtronicapp.mcu.mcu.CipedtronicData;
import com.example.cipedtronicapp.mcu.mcu.ICipedTronicMCU;

import java.util.List;

public class PageViewModel extends AndroidViewModel {


    private MutableLiveData<CipedtronicData> _CipedData = new MutableLiveData<>();
    private MutableLiveData<List<BLEScannedDevice>> _Scanresults = new MutableLiveData<>();
    private MutableLiveData<String> _ErrorString = new MutableLiveData<>();
    private MutableLiveData<String> _StateString = new MutableLiveData<>();
    private CipedTronicMCU _CipedTronicMCU;

    private Context _AppContext;
    String _BluetothAddress = "";

    public PageViewModel(@NonNull Application application)
    {
        super(application);
        _AppContext = application.getApplicationContext();
        _CipedTronicMCU = CipedTronicMCU.createInstance(_AppContext);
        if(_CipedTronicMCU != null ) {
            _CipedTronicMCU.setOnCipedTronicDeviceListener(_OnCipedTronicDeviceListener);

        }
        else
        {

        }
    }

    public CipedTronicMCU.OnCipedTronicDeviceListener _OnCipedTronicDeviceListener = new CipedTronicMCU.OnCipedTronicDeviceListener() {
        @Override
        public void onDeviceScanResult(List<BLEScannedDevice> devices) {
            _Scanresults.postValue(devices);
        }

        @Override
        public void onDeviceError(String error) {
            _ErrorString.postValue(error);
        }

        @Override
        public void onStateChanged(String state) {
            _StateString.postValue(state);
        }

        @Override
        public void onDataUpdate(CipedtronicData data) {
            _CipedData.postValue(data);
        }
    };
    public void setDevice(BLEScannedDevice device)
    {
        _BluetothAddress = device.Address;
    }

    public  void connectDevice()
    {
        int pulsesPerRevolution =  0;
        float wheelradius =  0;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_AppContext);
        pulsesPerRevolution = Integer.parseInt(prefs.getString("pulse_per_revolution","1"));
        wheelradius = Float.parseFloat(prefs.getString("wheel_radius","1"));
        _BluetothAddress = prefs.getString("bluetooth_Address", "");
        _CipedTronicMCU.setPulsesPerRevolution(pulsesPerRevolution);
        _CipedTronicMCU.setWheelRadius(wheelradius);
        if(_BluetothAddress != "") {
            _CipedTronicMCU.start(_BluetothAddress);
        }

    }

    public void disconnectDevice()
    {
        _CipedTronicMCU.close();
    }

    public void scanDevices()
    {
        _CipedTronicMCU.StartScan();
    }

    public LiveData<CipedtronicData> getData() {
        return _CipedData;
    }
    public LiveData<List<BLEScannedDevice>> getScanDeResults() {
        return _Scanresults;
    }
    public LiveData<String> getError() {
        return _ErrorString;
    }
    public LiveData<String> getState() {
        return _StateString;
    }


}