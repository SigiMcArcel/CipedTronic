package CipedTronic.products.cipedtronicapp.ui.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;
import androidx.preference.PreferenceManager;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import CipedTronic.products.cipedtronicapp.mcu.BLE.BLEScannedDevice;
import CipedTronic.products.cipedtronicapp.mcu.mcu.CipedTronicConfiguration;
import CipedTronic.products.cipedtronicapp.mcu.mcu.CipedTronicMCU;
import CipedTronic.products.cipedtronicapp.mcu.mcu.CipedtronicData;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PageViewModel extends AndroidViewModel {


    private CipedTronicMCU _CipedTronicMCU = (CipedTronicMCU)CipedTronicMCU.getInstance();
    private MutableLiveData<CipedtronicData> _CipedData = new MutableLiveData<>();
    private MutableLiveData<List<BLEScannedDevice>> _Scanresults = new MutableLiveData<>();
    private MutableLiveData<String> _ErrorString = new MutableLiveData<>();
    private MutableLiveData<String> _StateString = new MutableLiveData<>();
    private MutableLiveData<String> _CipedStateString = new MutableLiveData<>();
    private MutableLiveData<CipedTronicConfiguration> _CipedTronicConfiguration = new MutableLiveData<>();
    private Context _AppContext;


    public PageViewModel(@NonNull Application application)
    {
        super(application);
        _AppContext = application.getApplicationContext();
        _CipedTronicMCU.setOnCipedTronicDeviceListener(_OnCipedTronicDeviceListener);
    }

    public CipedTronicMCU.OnCipedTronicDeviceListener _OnCipedTronicDeviceListener = new CipedTronicMCU.OnCipedTronicDeviceListener() {
        @Override
        public void onStatusChanged(String state) {

            _StateString.postValue(state);
        }

        @Override
        public void onDeviceError(String error) {
            _ErrorString.postValue(error);
        }


        @Override
        public void onDataUpdate(CipedtronicData data)
        {
            _CipedData.postValue(data);
        }

        @Override
        public void onScanResult(List<BLEScannedDevice> devices, String state) {
            _Scanresults.postValue(devices);
        }

        @Override
        public void onGetConfiguration(CipedTronicConfiguration configuration){
            _CipedTronicConfiguration.postValue(configuration);
        }
    };
    public void createDevice(BLEScannedDevice device)
    {
        _CipedTronicMCU.createDevice(_AppContext,device.Address);
        _CipedTronicMCU.connectDevice();
    }
    //device controls
    public void setConfiguration(CipedTronicConfiguration configuration,boolean download)
    {
        _CipedTronicMCU.setConfiguration(configuration,download);
    }
    public void readConfiguration(){_CipedTronicMCU.getConfiguration();}
    public void resetCounters()
    {
        _CipedTronicMCU.resetCounters();
    }
    void setAlarmEnabled(boolean on)
    {
        _CipedTronicMCU.setAlarmEnable(on);
    }

    public void disconnectDevice()
    {
        _CipedTronicMCU.close();
    }

    public void scanDevices()
    {
        _CipedTronicMCU.scanDevices();
    }

    public LiveData<CipedtronicData> getData() {
        return _CipedData;
    }
    public LiveData<List<BLEScannedDevice>> getScanResults() {
        return _Scanresults;
    }
    public LiveData<String> getError() {
        return _ErrorString;
    }
    public LiveData<String> getState() {
        return _StateString;
    }
    public LiveData<CipedTronicConfiguration> getConfiguration() {
        return _CipedTronicConfiguration;
    }


}