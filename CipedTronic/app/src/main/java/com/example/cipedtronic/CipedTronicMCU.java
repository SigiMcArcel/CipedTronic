package com.example.cipedtronic;

import android.app.Activity;

import android.widget.Toast;

import java.util.List;

public class CipedTronicMCU implements IBLEListener {
    private static volatile CipedTronicMCU INSTANCE = null;



    BLEService _BLEService;
    List<ICipedTronicMCU> _ICipedTronicMCUs;
    
    
    double pi = 3.14159265358979;
    double _PulsesPerRevolution = 18.0;
    double _RadiusOfWheelMM = 365.0;
    double _Velocity = 0.0;
    double _VelocityMax = 0.0;
    double _Distance = 0.0;
    double _Revolutions = 0;

    //data from ciped mcu
    long _Pulses = 0;
    long _PulsesPerSecond = 0;
    long _PulsesPerSecondMax = 0;

    String rxBuffer = "";

    Activity _Activity;
    boolean _deviceReady = false;

    private CipedTronicMCU(Activity activity)
    {

        _Activity = activity;
        _BLEService = new BLEService(_Activity.getApplicationContext(),this);
    }

    public static CipedTronicMCU createInstance(Activity activity)
    {
        if(INSTANCE == null) {
            synchronized (CipedTronicMCU.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CipedTronicMCU(activity);
                }
            }
        }
        return INSTANCE;
    }

    public static CipedTronicMCU getInstance()
    {
        return INSTANCE;
    }


    @Override
    public void onDataReceived(byte[] data) {
        if (data != null) {
            rxBuffer = Utilities.bytesToString(data);
              if (rxBuffer.endsWith(">") && rxBuffer.startsWith("<CIP")) {
                String formatted = rxBuffer.replace("<CIP", "").replace(">", "");
                String[] tmp = formatted.split("\\s");
                try {
                    _Pulses = Long.parseUnsignedLong(tmp[0], 16);
                    _PulsesPerSecond = Long.parseUnsignedLong(tmp[1], 16);
                    _PulsesPerSecondMax = Long.parseUnsignedLong(tmp[2], 16);

                if(_PulsesPerRevolution == 0) {_PulsesPerRevolution = 1;}
                _Velocity = (double) _PulsesPerSecond * _RadiusOfWheelMM / 1000 * 2 * pi * 3600 / _PulsesPerRevolution / 1000;
                _VelocityMax = (double) _PulsesPerSecondMax * _RadiusOfWheelMM / 1000 * 2 * pi * 3600 / _PulsesPerRevolution / 1000;
                _Distance = (double) _Pulses * _RadiusOfWheelMM / 1000 * 2 * pi / _PulsesPerRevolution / 1000;
                _Revolutions = _Pulses / _PulsesPerRevolution;
                    _deviceReady = true;
                }
                catch(Exception exp)
                {
                    Toast.makeText(_Activity.getApplicationContext(), exp.getMessage() + " " + tmp[0] + " " + tmp[1] + " " + tmp[2],Toast.LENGTH_LONG).show();
                }
            }
            for (ICipedTronicMCU mcu: _ICipedTronicMCUs ) {
                CipedtronicData mcuData = new CipedtronicData();
                mcuData.Distance = String.format("%.2f",_Distance);
                mcuData.MaxVelocity = String.format("%.2f",_VelocityMax);
                mcuData.Pulses = String.format("%.2f",_Pulses);
                mcuData.PulsesPerSecond = String.format("%.2f",_PulsesPerSecond);
                mcuData.Velocity = String.format("%.2f",_Velocity);
                mcu.onDataUpdate(mcuData);
            }
        }
    }

    @Override
    public void onErrorReceived(String data) {
        Toast.makeText(_Activity.getApplicationContext(),data,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeviceReady(ResponseStatus responseStatus) {


    }

    @Override
    public void onDeviceDisconnected() {
        _deviceReady = false;
    }

    @Override
    public void onScanResult(List<CipedTronicDevice> devices)
    {
        for (ICipedTronicMCU mcu: _ICipedTronicMCUs ) {
            mcu.onDeviceScanResult(devices);
        }
    }

    public void RegisterMCUInterface(ICipedTronicMCU iface)
    {
        _ICipedTronicMCUs.add(iface);
    }

    public void DeregisterMCUInterface(ICipedTronicMCU iface)
    {
        _ICipedTronicMCUs.remove(iface);
    }
    
    public String getVelocity()
    {
        return String.format("%.1f",_Velocity);
    }

    public String getMaxVelocity()
    {
        return String.format("%.1f",_VelocityMax);
    }

    public String getDistance()
    {
        return String.format("%.2f",_Distance);
    }

    public String getPulses()
    {
        return String.valueOf(_Pulses);
    }

    public String getPulsesPerSecond()
    {
        return String.valueOf(_PulsesPerSecond);
    }

    public void setWheelRadius(double r)
    {
        _RadiusOfWheelMM = r;
    }

    public void setPulsesPerRevolution(int p)
    {
        _PulsesPerRevolution = p;
    }
    public boolean getDeviceReady(){return _deviceReady;}

    public void setMCUId(Integer id)
    {
        String command = ">I"+Integer.toHexString(id) + "<";
        _BLEService.writeRXCharacteristic(command.getBytes());
    }

    public void ResetMCU()
    {
        String command = ">A";
        if(_deviceReady) {
            _BLEService.writeRXCharacteristic(command.getBytes());
        }
    }
    public void initialize()
    {
        _BLEService.initialize(this);
    }

    public void Pause()
    {

    }
}
