package com.example.cipedtronic;

import android.app.Activity;

import com.example.cipedtronic.ResponseStatus;
import com.example.cipedtronic.USBSerialConnector;
import com.example.cipedtronic.USBSerialListener;
import com.example.cipedtronic.Utilities;

import android.widget.Toast;

public class CipedTronicMCU implements USBSerialListener{
    USBSerialConnector _UsbConnector;
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

    public CipedTronicMCU(Activity Activity)
    {

        _Activity = Activity;
        _UsbConnector = USBSerialConnector.getInstance();
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
        _UsbConnector.writeAsync(command.getBytes());
    }

    public void ResetMCU()
    {
        String command = ">A";
        if(_deviceReady) {
            USBSerialConnector.getInstance().writeAsync(command.getBytes());
        }
    }
    public void Resume()
    {
        _UsbConnector.setUsbSerialListener(this);
        _UsbConnector.init(_Activity,115200);

    }

    public void Pause()
    {
        _UsbConnector.pausedActivity();
    }
}
