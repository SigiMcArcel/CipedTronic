package com.example.cipedtronicapp.mcu.BLE;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;

import java.util.UUID;

public class BLEUart extends BLEService{

    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    public interface OnUartListener{
        public void onReceive(byte[] data);
        public void onStatusChanged(String state);
    }

    public OnUartListener _Listener;
    public void setOnReceiveListener(OnUartListener listener)
    {
        _Listener = listener;
    }
    public boolean open()
    {
        addNotificationUuid(TX_CHAR_UUID);
        _BleDevice.connect();
        return true;
    }
    public boolean close()
    {
        _BleDevice.disconnect();
        return true;
    }
    public boolean write(byte[] data)
    {
        return this.writeCharacteristic(data,RX_CHAR_UUID);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic,byte[] value) {
        if(_Listener != null)
        {
            _Listener.onReceive(value);
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt,status,newState);
        if (newState == BluetoothProfile.STATE_CONNECTED)
        {
            _Listener.onStatusChanged("STATE_CONNECTED");
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            _Listener.onStatusChanged("STATE_DISCONNECTED");
        }
    }

}
