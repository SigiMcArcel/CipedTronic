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
import CipedTronic.products.cipedtronicapp.mcu.mcu.CipedTronicMCU;
import CipedTronic.products.cipedtronicapp.mcu.mcu.CipedtronicData;

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

    private CipedTronicMCU _CipedTronicMCU = (CipedTronicMCU)CipedTronicMCU.getInstance();
    private MutableLiveData<CipedtronicData> _CipedData = new MutableLiveData<>();
    private MutableLiveData<List<BLEScannedDevice>> _Scanresults = new MutableLiveData<>();
    private MutableLiveData<String> _ErrorString = new MutableLiveData<>();
    private MutableLiveData<String> _StateString = new MutableLiveData<>();
    private MutableLiveData<String> _CipedStateString = new MutableLiveData<>();


    private Context _AppContext;
    private String _BluetothAddress = "";
    private SharedPreferences _Prefs;

    private refreshTask _ref = new refreshTask();
    private Timer _Timer = new Timer(true);


    double dist = 0;
    Application _Application;


    public PageViewModel(@NonNull Application application)
    {
        super(application);
        _AppContext = application.getApplicationContext();
        init();
    }

    OnTickListener _OnTickListener = new OnTickListener() {
        @Override
        public void OnTick() {
            if(_CipedTronicMCU != null ) {
                CipedtronicData cipedTronicData = new CipedtronicData();
                String state = _CipedTronicMCU.getDeviceState();
                cipedTronicData.Distance = _CipedTronicMCU.getDistance();
                cipedTronicData.MaxVelocity = _CipedTronicMCU.getMaxVelocity();
                cipedTronicData.Pulses = _CipedTronicMCU.getPulses();
                cipedTronicData.PulsesPerSecond = _CipedTronicMCU.getPulsesPerSecond();
                cipedTronicData.Velocity = _CipedTronicMCU.getVelocity();
                cipedTronicData.StateLight = _CipedTronicMCU.getLight();
                cipedTronicData.StateAlarm = _CipedTronicMCU.getAlarm();
                _CipedData.postValue(cipedTronicData);
                _StateString.postValue(state);
            }
        }
    } ;

    public void init()
    {
        _CipedTronicMCU.setOnCipedTronicDeviceListener(_OnCipedTronicDeviceListener);
        _ref.setOnTickListener(_OnTickListener);
        _Prefs = PreferenceManager.getDefaultSharedPreferences(_AppContext);
        _BluetothAddress = _Prefs.getString("bluetooth_Address","");
        _Timer.scheduleAtFixedRate(_ref,0,1000);
    }


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

        @Override
        public void onScanResult(List<BLEScannedDevice> devices, String state) {
            _Scanresults.postValue(devices);
        }
    };
    public void setDeviceAddress(BLEScannedDevice device)
    {
        _BluetothAddress = device.Address;
        initializeMCU(_BluetothAddress);
    }

    public void resetMCU()
    {
        _CipedTronicMCU.ResetMCU();
    }

    void setPulsesPerRevolution(int pulsesPerRevolution)
    {
        _CipedTronicMCU.setPulsesPerRevolution(pulsesPerRevolution);
    }
    void setWheelradius(float wheelradius)
    {
        _CipedTronicMCU.setWheelRadius(wheelradius);
    }

    void setLight(boolean on)
    {
        _CipedTronicMCU.setLight(on);
    }
    void setAlarm(boolean on)
    {
        _CipedTronicMCU.setAlarm(on);
    }
    public  void initializeMCU(String address)
    {
        int pulsesPerRevolution =  Integer.parseInt(_Prefs.getString("pulse_per_revolution","1"));;
        float wheelradius =  Float.parseFloat(_Prefs.getString("wheel_radius","1"));
        _CipedTronicMCU.createDevice(_AppContext,address);
        _CipedTronicMCU.setOnCipedTronicDeviceListener(_OnCipedTronicDeviceListener);
        _CipedTronicMCU.setPulsesPerRevolution(pulsesPerRevolution);
        _CipedTronicMCU.setWheelRadius(wheelradius);

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


}