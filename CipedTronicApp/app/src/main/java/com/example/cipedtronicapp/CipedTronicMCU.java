package com.example.cipedtronicapp;

import android.app.Activity;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CipedTronicMCU implements IBLEListener {
    private static volatile CipedTronicMCU INSTANCE = null;



    BLEService _BLEService;
    List<ICipedTronicMCU> _ICipedTronicMCUs = new ArrayList<>();
    
    
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
    boolean BufferSync = false;

    Context _Context;
    boolean _deviceReady = false;
    boolean _stopscan = false;

    private CipedTronicMCU(Context context)
    {
        _Context = context;
        _BLEService = new BLEService(context,this);
    }

    public static CipedTronicMCU createInstance(Context contetxt)
    {
        if(INSTANCE == null) {
            synchronized (CipedTronicMCU.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CipedTronicMCU(contetxt);
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
            rxBuffer += Utilities.bytesToString(data);
              //start if start sequence is received
              //find start sequence
              int startPos = rxBuffer.indexOf ("<CIP");
              if(!BufferSync) {
                  if (startPos == 0) {
                      BufferSync = true;
                  } else if (startPos == -1) {
                      rxBuffer = "";
                  } else {
                      rxBuffer = rxBuffer.substring(startPos);
                      BufferSync = true;
                  }
              }

              if (rxBuffer.endsWith(">") && rxBuffer.startsWith("<CIP")) {
                String formatted = rxBuffer.replace("<CIP", "").replace(">", "");
                String[] tmp = formatted.split("\\s");
                  rxBuffer = "";
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
                for (ICipedTronicMCU mcu: _ICipedTronicMCUs ) {
                    CipedtronicData mcuData = new CipedtronicData();
                    mcuData.Distance = String.format("%.2f",_Distance);
                    mcuData.MaxVelocity = String.format("%.2f",_VelocityMax);
                    mcuData.Pulses = String.format("%d",_Pulses);
                    mcuData.PulsesPerSecond = String.format("%d",_PulsesPerSecond);
                    mcuData.Velocity = String.format("%.2f",_Velocity);
                    mcu.onDataUpdate(mcuData);
                }
                }
                catch(Exception exp)
                {
                    Log.i("MCU", exp.getMessage());
                }
            }
        }
    }

    @Override
    public void onErrorReceived(String data) {
        Toast.makeText(_Context, data,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeviceConnected() {
        BufferSync = false;
        for (ICipedTronicMCU mcu: _ICipedTronicMCUs ) {
            mcu.onStateChanged("connected");
        }

    }

    @Override
    public void onDeviceDisconnected() {
        for (ICipedTronicMCU mcu: _ICipedTronicMCUs ) {
            mcu.onStateChanged("disconnected");
        }
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

    public void StartScan()
    {
         _BLEService.searchCipetTronicDevices();
    }
    public void ResetMCU()
    {
        String command = ">A";
        if(_deviceReady) {
            _BLEService.writeRXCharacteristic(command.getBytes());
        }
    }

    private void checkPermissions()
    {

    }
    public void initialize()
    {
        _BLEService.initialize(this);
    }

    public void connect(String address)
    {
        BufferSync = false;
        _BLEService.connect(address);
    }
    public void disconnect()
    {
        _BLEService.disconnect();
    }

    public void Pause()
    {

    }

    public void destroy()
    {
        _BLEService.disconnect();
        _BLEService.close();
    }
}
