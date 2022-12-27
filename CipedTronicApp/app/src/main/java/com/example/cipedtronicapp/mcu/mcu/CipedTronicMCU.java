package com.example.cipedtronicapp.mcu.mcu;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.cipedtronicapp.Utilities;
import com.example.cipedtronicapp.mcu.SerialBLE.BLEAdapter;
import com.example.cipedtronicapp.mcu.SerialBLE.BLEDevice;
import com.example.cipedtronicapp.mcu.SerialBLE.BLEScannedDevice;
import com.example.cipedtronicapp.mcu.SerialBLE.BLEUart;
import com.example.cipedtronicapp.mcu.SerialBLE.SerialBLE;
import com.example.cipedtronicapp.mcu.SerialBLE.OnBLESerialListener;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CipedTronicMCU {
    public interface OnCipedTronicDeviceListener {
        public void onDeviceScanResult(List<BLEScannedDevice> devices);
        public void onDeviceError(String error);
        public void onStateChanged(String state);
        public void onDataUpdate(CipedtronicData data);
    }

    private static volatile CipedTronicMCU INSTANCE = null;

    BLEAdapter _BLEAdapter;
    BLEDevice _BLEDevice;
    BLEUart _BLEUart;
    DeviceProtocol _Protocoll;
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");

    OnCipedTronicDeviceListener _Listener;
    
    
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
    long _PulsesPerSecondAverage = 0;

    String rxBuffer = "";
    boolean BufferSync = false;

    Context _Context;
    boolean _deviceReady = false;
    boolean _stopscan = false;
    String _BleAddress = "";
    String _Id = "";

    private CipedTronicMCU(Context context)
    {
        _Context = context;
        _BLEAdapter = new BLEAdapter(context);
        _BLEAdapter.setOnScanListener(_OnScanListener);
    }

    public void setOnCipedTronicDeviceListener(OnCipedTronicDeviceListener listener)
    {
        _Listener = listener;
    }
    public static CipedTronicMCU createInstance(Context context)
    {
        if(INSTANCE == null) {
            synchronized (CipedTronicMCU.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CipedTronicMCU(context);
                }
            }
        }
        return INSTANCE;
    }

    public static CipedTronicMCU getInstance()
    {
        return INSTANCE;
    }


    public boolean start(String address)
    {
        _BleAddress = address;
        _BLEDevice = _BLEAdapter.getDevice(_BleAddress);
        if(_BLEDevice == null)
        {
            return false;
        }
        _BLEUart = (BLEUart) _BLEDevice.getService(RX_SERVICE_UUID);
        if(_BLEUart == null)
        {
            return false;
        }
        _Protocoll = new DeviceProtocol(_BLEUart);
        _BLEUart.open();
        _Protocoll.setOnDeviceProtcolListener(_OnDeviceProtcolListener);
        return true;
    }

    public void close()
    {
        _BLEUart.close();
    }

    BLEAdapter.OnScanListener _OnScanListener = new BLEAdapter.OnScanListener() {
        @Override
        public void onScanResult(List<BLEScannedDevice> devices, String State) {
            _Listener.onDeviceScanResult(devices);
        }
    };

    public DeviceProtocol.OnDeviceProtcolListener _OnDeviceProtcolListener = new DeviceProtocol.OnDeviceProtcolListener() {
        @Override
        public void OnErrorReceived(long error) {
            if(_Listener != null)
            {
                _Listener.onDeviceError("UART Error 0x" + Long.toHexString(error));
            }
        }

        @Override
        public void OnDataReceived(CipedTronicProtokollPackage message) {
            CalculateDeviceData(message);
        }

        @Override
        public void OnConnectedStateChange(String state) {
            if(state == "STATE_CONNECTED")
            {
                _Protocoll.startTransmition(_Id,30,4,500);
            }
            if(state == "STATE_DISCONNECTED")
            {

            }

        }
    };

    private void CalculateDeviceData(CipedTronicProtokollPackage message)
    {
        _PulsesPerSecond = message._Data[0];
        _PulsesPerSecondMax = message._Data[1];
        _PulsesPerSecondAverage = message._Data[2];
        _Pulses = message._Data[3];
        if(_PulsesPerRevolution == 0) {_PulsesPerRevolution = 1;}
        _Velocity = (double) _PulsesPerSecond * _RadiusOfWheelMM / 1000 * 2 * pi * 3600 / _PulsesPerRevolution / 1000;
        _VelocityMax = (double) _PulsesPerSecondMax * _RadiusOfWheelMM / 1000 * 2 * pi * 3600 / _PulsesPerRevolution / 1000;
        _Distance = (double) _Pulses * _RadiusOfWheelMM / 1000 * 2 * pi / _PulsesPerRevolution / 1000;
        _Revolutions = _Pulses / _PulsesPerRevolution;
        _deviceReady = true;
        CipedtronicData mcuData = new CipedtronicData();
        mcuData.Distance = String.format("%.2f",_Distance);
        mcuData.MaxVelocity = String.format("%.2f",_VelocityMax);
        mcuData.Pulses = String.format("%d",_Pulses);
        mcuData.PulsesPerSecond = String.format("%d",_PulsesPerSecond);
        mcuData.Velocity = String.format("%.2f",_Velocity);
        _Listener.onDataUpdate(mcuData);
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

    public boolean setMCUId(String id)
    {
        long[] param = new long[]{0,0};
        byte[] data = id.getBytes();
        if(data.length - 1 > 8)
        {
            return false;
        }
        ByteBuffer bb = ByteBuffer.wrap(data,0,4);
        param[0] = bb.getLong();
        bb = ByteBuffer.wrap(data,4,4);
        param[1] = bb.getLong();
       _Protocoll.dataWrite(_Id,10,param.length,param);
       return true;
    }


    public void StartScan()
    {
         _BLEAdapter.scanDevices("ADA_BLE");
    }
    public void ResetMCU()
    {
        long[] param = new long[]{0,0,0,0};
        _Protocoll.dataWrite(_Id,30,param.length,param);
    }



    public void destroy()
    {
        _BLEUart.close();
    }
}
