package com.example.cipedtronicapp.ui.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;
import androidx.preference.PreferenceManager;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.cipedtronicapp.mcu.BLE.BLEAdapter;
import com.example.cipedtronicapp.mcu.BLE.BLEScannedDevice;
import com.example.cipedtronicapp.mcu.mcu.CipedTronicMCU;
import com.example.cipedtronicapp.mcu.mcu.CipedtronicData;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PageViewModel extends AndroidViewModel {
    public interface OnTickListener
    {
        void OnTick();
    }
    public class refreshTask extends TimerTask
    {
       private OnTickListener _Listener;
       public void setOnTickListener(OnTickListener listener)
       {
           _Listener = listener;
       }
        @Override
        public void run()
        {
            if(_Listener != null)
            {
                _Listener.OnTick();
            }
        }
    }

    private MutableLiveData<CipedtronicData> _CipedData = new MutableLiveData<>();
    private MutableLiveData<List<BLEScannedDevice>> _Scanresults = new MutableLiveData<>();
    private MutableLiveData<String> _ErrorString = new MutableLiveData<>();
    private MutableLiveData<String> _StateString = new MutableLiveData<>();
    private MutableLiveData<String> _CipedStateString = new MutableLiveData<>();
    private CipedTronicMCU _CipedTronicMCU;

    private Context _AppContext;
    String _BluetothAddress = "";
    BLEAdapter _BLEAdapter;

    refreshTask _ref = new refreshTask();
    Timer timer = new Timer(true);


    double dist = 0;



    public PageViewModel(@NonNull Application application)
    {
        super(application);

        _ref.setOnTickListener(_OnTickListener);
        _AppContext = application.getApplicationContext();
        _BLEAdapter = new BLEAdapter(_AppContext);
        _CipedTronicMCU = (CipedTronicMCU) _BLEAdapter.getDevice(_BluetothAddress);
        if(_CipedTronicMCU != null ) {
            _CipedTronicMCU.setOnCipedTronicDeviceListener(_OnCipedTronicDeviceListener);
            timer.scheduleAtFixedRate(_ref,0,1000);

        }
        else
        {

        }
    }
    OnTickListener _OnTickListener = new OnTickListener() {
        @Override
        public void OnTick() {
            CipedtronicData cipedTronicData = new CipedtronicData();
            String state = _CipedTronicMCU.getDeviceState();
            cipedTronicData.Distance = _CipedTronicMCU.getDistance();
            cipedTronicData.MaxVelocity = _CipedTronicMCU.getMaxVelocity();
            cipedTronicData.Pulses = _CipedTronicMCU.getPulses();
            cipedTronicData.PulsesPerSecond = _CipedTronicMCU.getPulsesPerSecond();
            cipedTronicData.Velocity = _CipedTronicMCU.getVelocity();


            _CipedData.postValue(cipedTronicData);
            _StateString.postValue(state);
        }
    } ;


    public CipedTronicMCU.OnCipedTronicDeviceListener _OnCipedTronicDeviceListener = new CipedTronicMCU.OnCipedTronicDeviceListener() {
        @Override
        public void onStatusChanged(String state) {

        }

        @Override
        public void onDeviceError(String error) {
            _ErrorString.postValue(error);
        }


        @Override
        public void onDataUpdate(CipedtronicData data)
        {

        }
    };
    public void setDevice(BLEScannedDevice device)
    {
        _BluetothAddress = device.Address;
    }

    public void resetDevice()
    {
        _CipedTronicMCU.ResetMCU();
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
        _CipedTronicMCU.connectDevice();

    }

    public void disconnectDevice()
    {
        _CipedTronicMCU.close();
    }

    public void scanDevices()
    {
        _BLEAdapter.scanDevices("CIPED");
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