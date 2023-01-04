package com.example.cipedtronicapp.mcu.BLE;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.UUID;

public class BLECipedTronic implements BLEDevice.OnBLEGATTListener{
    public static final UUID PULSES_PER_SECOND_UUID = UUID.fromString("00001236-0000-1000-8000-00805f9b34fb");
    public static final UUID PULSES_PER_SECOND_MAX_UUID = UUID.fromString("00001237-0000-1000-8000-00805f9b34fb");
    public static final UUID PULSES_PER_SECOND_AVG_UUID = UUID.fromString("00001238-0000-1000-8000-00805f9b34fb");
    public static final UUID PULSES_UUID = UUID.fromString("00001239-0000-1000-8000-00805f9b34fb");
    public static final UUID CONTROL_UUID = UUID.fromString("00001240-0000-1000-8000-00805f9b34fb");
    public static final UUID SYSTEMID_UUID = UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb");



    public interface OnCipedTronicListener{
    void onReceive(long pulsesPerSecond,long pulsesPerSecondMax,long pulsesPerSecondAvg,long pulses);
    void onRead(byte[] data);
    void onWritten();
    void onStatusChanged(String state);
}
    private long _PulsesPerSecond = 0;
    private long _PulsesPerSecondMax = 0;
    private long _PulsesPerSecondAverage = 0;
    private long _Pulses = 0;

    //maybe super class
    private  BluetoothGattService _Service;
    private BLEDevice _Device;
    private ArrayDeque<BluetoothGattCharacteristic> _Characteristics = new ArrayDeque<>();
    private UUID  _ServiceUUID = null;

    public BLECipedTronic.OnCipedTronicListener _Listener;

    public BLECipedTronic()
    {

    }

    public BLECipedTronic(BLEDevice device,UUID serviceUiid)
    {
        _Device = device;
        _Device.setOnOnBLEDeviceGATTListener(this);
        _ServiceUUID = serviceUiid;
        setNotification();
    }
    public void setOnReceiveListener(BLECipedTronic.OnCipedTronicListener listener)
    {
        _Listener = listener;
    }

    long getUnsignedIntFrom(ByteBuffer bb) {
        return (bb.getInt() & 0xFFFFFFFFL);
    }

    @Override
    public void OnStatusChanged(int Status) {
        if (Status == BluetoothProfile.STATE_CONNECTED)
        {
            _Service = _Device.getService(_ServiceUUID);
            if(_Listener != null){ _Listener.onStatusChanged("STATE_CONNECTED");}
        } else if (Status == BluetoothProfile.STATE_DISCONNECTED) {
            if(_Listener != null){ _Listener.onStatusChanged("STATE_DISCONNECTED");}
            _Service = null;
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, byte[] value) {
        if(_Listener != null){ _Listener.onRead(value);}
    }

    @Override
    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic, byte[] value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);//   .wrap(value);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        if(value.length != 4)
        {
            return;
        }

        buffer.put(value);
        if(characteristic.getUuid().compareTo(PULSES_PER_SECOND_UUID) == 0)
        {
            _PulsesPerSecond = (long)(buffer.getInt(0) & 0xFFFFFFFFL);
        }else if(characteristic.getUuid().compareTo(PULSES_PER_SECOND_MAX_UUID) == 0)
        {
            _PulsesPerSecondMax = (long)(buffer.getInt(0) & 0xFFFFFFFFL);
        }else if(characteristic.getUuid().compareTo(PULSES_PER_SECOND_AVG_UUID) == 0)
        {
            _PulsesPerSecondAverage = (long)(buffer.getInt(0) & 0xFFFFFFFFL);
        }else if(characteristic.getUuid().compareTo(PULSES_UUID) == 0)
        {
            _Pulses = (long)(buffer.getInt(0) & 0xFFFFFFFFL);
        }else{
            return;
        }

        if(_Listener != null)
        {
            _Listener.onReceive(_PulsesPerSecond,_PulsesPerSecondMax,_PulsesPerSecondAverage,_Pulses);
        }
    }

    @Override
    public void onCharacteristicWrite() {
        if(_Listener != null) {
            _Listener.onWritten();
        }
    }

    public boolean addNotificationUuid(UUID characteristicUuid)
    {
        if(_Service == null)
        {
            return false;
        }
        BluetoothGattCharacteristic characteristic = _Service.getCharacteristic(characteristicUuid);
        if (characteristic == null) {
            return false;
        }
        _Characteristics.push(characteristic);
        return true;
    }

    public boolean setNotification()
    {
        addNotificationUuid(PULSES_PER_SECOND_UUID);
        addNotificationUuid(PULSES_PER_SECOND_MAX_UUID);
        addNotificationUuid(PULSES_PER_SECOND_AVG_UUID);
        addNotificationUuid(PULSES_UUID);
        _Device.setCharacteristicNotifications(_Characteristics);
        return true;
    }

    public boolean connect()
    {
        return _Device.connect();
    }


    public boolean disconnect()
    {
        _Device.disconnect();
        return true;
    }

    public boolean writeControl(long data)
    {
        if(_Service == null)
        {
            return false;
        }
        //dont use ByteBuffer it's crap
        byte[] buffer = new byte[4];
        //send liitle endian
        buffer[0] = (byte)(data & 0xFF);
        buffer[1] = (byte)((data & 0xFF00)>>8);
        buffer[2] = (byte)((data & 0xFF0000)>>16);
        buffer[3] = (byte)((data & 0xFF000000)>>24);
        BluetoothGattCharacteristic characteristic = _Service.getCharacteristic(CONTROL_UUID);
        if (characteristic == null) {
            return false;
        }
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        characteristic.setValue(buffer);
        return _Device.writeCharacteristic(characteristic);
    }

    public boolean writeId(long data)
    {
        if(_Service == null)
        {
            return false;
        }
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(data);
        BluetoothGattCharacteristic characteristic = _Service.getCharacteristic(SYSTEMID_UUID);
        if (characteristic == null) {
            return false;
        }
        characteristic.setValue(buffer.array());
        return _Device.writeCharacteristic(characteristic);
    }

    public boolean readId()
    {
        if(_Service == null)
        {
            return false;
        }
        BluetoothGattCharacteristic characteristic = _Service.getCharacteristic(SYSTEMID_UUID);
        if (characteristic == null) {
            return false;
        }
        return _Device.readCharacteristic(characteristic);
    }
}
