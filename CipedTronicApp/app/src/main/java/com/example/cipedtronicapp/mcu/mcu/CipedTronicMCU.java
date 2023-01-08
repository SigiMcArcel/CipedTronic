package com.example.cipedtronicapp.mcu.mcu;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.example.cipedtronicapp.mcu.BLE.BLEAdapter;
import com.example.cipedtronicapp.mcu.BLE.BLEDevice;
import com.example.cipedtronicapp.mcu.BLE.BLEScannedDevice;
import com.example.cipedtronicapp.mcu.BLE.BLETimeOut;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CipedTronicMCU extends  BLEDevice{

    public interface OnCipedTronicDeviceListener {
        public void onStatusChanged(String state);
        public void onDeviceError(String error);
        public void onDataUpdate(CipedtronicData data);
    }

    private final long MCU_CMD_RESET_COUNTER = 0x01;
    public static final UUID PULSES_PER_SECOND_UUID = UUID.fromString("00001236-0000-1000-8000-00805f9b34fb");
    public static final UUID PULSES_PER_SECOND_MAX_UUID = UUID.fromString("00001237-0000-1000-8000-00805f9b34fb");
    public static final UUID PULSES_PER_SECOND_AVG_UUID = UUID.fromString("00001238-0000-1000-8000-00805f9b34fb");
    public static final UUID PULSES_UUID = UUID.fromString("00001239-0000-1000-8000-00805f9b34fb");
    public static final UUID CONTROL_UUID = UUID.fromString("00001240-0000-1000-8000-00805f9b34fb");
    public static final UUID SYSTEMID_UUID = UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb");

    private OnCipedTronicDeviceListener _Listener;

    private double pi = 3.14159265358979;
    private double _PulsesPerRevolution = 18.0;
    private double _RadiusOfWheelMM = 365.0;
    private double _Velocity = 0.0;
    private double _VelocityMax = 0.0;
    private double _VelocityAvg = 0.0;
    private double _Distance = 0.0;

    //ciped mcu
    private long _Pulses = 0;
    private long _PulsesPerSecond = 0;
    private long _PulsesPerSecondMax = 0;
    private long _PulsesPerSecondAverage = 0;
    private long _Id = 0;
    private long _IdFromDevice = 0;
    //Bluetooth
    public static final UUID CIPEDTRONIC_UUID = UUID.fromString("00001235-0000-1000-8000-00805f9b34fb");
    private BLEAdapter _BLEAdapter;
    private BLEDevice _BLEDevice;
    private String _BluetoothState = "";
    private List<BLEScannedDevice> _ScannedDevices = new ArrayList<>();
    private String _BleAddress = "";


    //others
    private  Context _Context;
    private boolean _deviceReady = false;
    private CipedtronicData mcuData = new CipedtronicData();


    private boolean _StateMachineBusy = false;
    BLETimeOut _RestartTimeOut = new BLETimeOut();
    BLETimeOut _StateMachineTimeOut = new BLETimeOut();
    private boolean _AutomaticRestart = false;
    private  boolean _Connected = false;

    public CipedTronicMCU(Context context, BluetoothDevice device) {
        super(context, device, CIPEDTRONIC_UUID, true);
        addNotifiationCharacteristic(PULSES_PER_SECOND_UUID);
        addNotifiationCharacteristic(PULSES_PER_SECOND_MAX_UUID);
        addNotifiationCharacteristic(PULSES_PER_SECOND_AVG_UUID);
        addNotifiationCharacteristic(PULSES_UUID);
        setOnBLEDeviceListener(_OnBLEDeviceListener);
    }

    public void setOnCipedTronicDeviceListener(OnCipedTronicDeviceListener listener)
    {
        _Listener = listener;
    }



    private void CalculateDeviceData()
    {

        if(_PulsesPerRevolution == 0) {_PulsesPerRevolution = 1;}
        _Velocity = (double) _PulsesPerSecond * _RadiusOfWheelMM / 1000 * 2 * pi * 3600 / _PulsesPerRevolution / 1000;
        _VelocityMax = (double) _PulsesPerSecondMax * _RadiusOfWheelMM / 1000 * 2 * pi * 3600 / _PulsesPerRevolution / 1000;
        _VelocityAvg = (double) _PulsesPerSecondAverage * _RadiusOfWheelMM / 1000 * 2 * pi * 3600 / _PulsesPerRevolution / 1000;
        _Distance = (double) _Pulses * _RadiusOfWheelMM / 1000 * 2 * pi / _PulsesPerRevolution / 1000;
        //_Revolutions = _Pulses / _PulsesPerRevolution;
        _deviceReady = true;

        mcuData.Velocity = String.format("%.2f",_Velocity);
        mcuData.MaxVelocity = String.format("%.2f",_VelocityMax);
        mcuData.AvgVelocity = String.format("%.2f",_VelocityAvg);
        mcuData.Distance = String.format("%.2f",_Distance);
        mcuData.Pulses = String.format("%d",_Pulses);
        mcuData.PulsesPerSecond = String.format("%d",_PulsesPerSecond);

        if(_Listener != null) {

            _Listener.onDataUpdate(mcuData);
        }
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


    public void ResetMCU() {
        long data = 1;
        //dont use ByteBuffer it's crap
        byte[] buffer = new byte[4];
        //send liitle endian
        buffer[0] = (byte)(data & 0xFF);
        buffer[1] = (byte)((data & 0xFF00)>>8);
        buffer[2] = (byte)((data & 0xFF0000)>>16);
        buffer[3] = (byte)((data & 0xFF000000)>>24);
        this.writeCharacteristic(CONTROL_UUID,buffer);
    }

    BLEDevice.OnBLEDeviceListener _OnBLEDeviceListener = new OnBLEDeviceListener() {
        @Override
        public void OnStatusChanged(BLEDeviceStates state) {
            if(_Listener != null)
            {
                _Listener.onStatusChanged(state.name());
            }
        }

        @Override
        public void onCharacteristicRead(UUID characteristicUUID, byte[] value) {

        }

        @Override
        public void onCharacteristicNotification(UUID characteristicUUID, byte[] value) {
            ByteBuffer buffer = ByteBuffer.allocate(4);//   .wrap(value);
            buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
            if(value.length != 4)
            {
                return;
            }

            buffer.put(value);
            if(characteristicUUID.compareTo(PULSES_PER_SECOND_UUID) == 0)
            {
                _PulsesPerSecond = (long)(buffer.getInt(0) & 0xFFFFFFFFL);
            }else if(characteristicUUID.compareTo(PULSES_PER_SECOND_MAX_UUID) == 0)
            {
                _PulsesPerSecondMax = (long)(buffer.getInt(0) & 0xFFFFFFFFL);
            }else if(characteristicUUID.compareTo(PULSES_PER_SECOND_AVG_UUID) == 0)
            {
                _PulsesPerSecondAverage = (long)(buffer.getInt(0) & 0xFFFFFFFFL);
            }else if(characteristicUUID.compareTo(PULSES_UUID) == 0)
            {
                _Pulses = (long)(buffer.getInt(0) & 0xFFFFFFFFL);
            }else{
                return;
            }
            CalculateDeviceData();

        }

        @Override
        public void onError(BLEDeviceErrors error) {
            if(_Listener != null)
            {
                _Listener.onDeviceError(error.name());
            }
        }
    };

@Override
    public void finalize()
    {
        close();
    }


}
