/*
    Abstracts a BLE device
    Handles one service and several characteristics

   Using:
   Create a Instance with the BLE Address, the service UUID it will handle and the Application Context
   add the characteristic it will notify

   Connect Sequencing:
   - connect->
        onConnectionStateChange(connected)->
            discover Services ->
                onServicesDiscovered->
                    set Notification->
                        ->Ready for use

 */
package com.example.cipedtronicapp.mcu.BLE;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.UUID;


public class BLEDevice extends Thread implements AutoCloseable{

    private class BLEDeviceCommand
    {
        public BLEDeviceCommand(BLEDeviceCommandStates command)
        {
            Command = command;
        }
        public BLEDeviceCommand(
                BLEDeviceCommandStates command,
                Object param1,
                Object param2,
                Object param3,
                Object param4
                )
        {
            Param1 = param1;
            Param2 = param2;
            Param3 = param3;
            Param4 = param4;
            Command = command;
        }
        public BLEDeviceCommandStates Command;
        public Object Param1;
        public Object Param2;
        public Object Param3;
        public Object Param4;
    }

    public interface OnBLEDeviceListener
    {
        void OnStatusChanged(BLEDeviceStates state);
        void onCharacteristicRead(UUID characteristicUUID,byte[] value);
        void onCharacteristicNotification(UUID characteristicUUID,byte[] value);
        void onError(BLEDeviceErrors error);
    }

    public enum BLEDeviceStates {
        None,
        Disconnected,
        Connected,
    }

    public enum BLEDeviceCommandStates
    {
        Idle,
        Connect,
        Connecting,
        Connected,
        Disconnect,
        Disconnecting,
        Disconnected,
        DisconnectedThruClient,
        DiscoverServices,
        DiscoveringServices,
        DiscoverServicesFinish,
        EnableNotificiction,
        EnablingNotification,
        EnableNotificictionFinish,
        Ready,
        WriteDescriptor,
        WritingDescriptor,
        WrittenDescriptor,
        ReadDescriptor,
        ReadingDescriptor,
        ReadDescriptorFinished,
        ReadCharacteristic,
        ReadingCharacteristic,
        ReadCharacteristicFinished,
        WriteCharacteristic,
        WritingCharacteristic,
        WriteCharacteristicFinished,
        Error
    }

    public enum BLEDeviceErrors
    {
        Ok,
        NotConnected,
        AlreadyConnected,
        AlreadyDisconnected,
        ServiceNotFound,
        ServiceNotValid,
        CharacteristicNotFound,
        DescriptionNotFound,
        CouldNotConnectDevice,
        CouldNotReadCharacteristic,
        CouldNotReadDescription,
        CouldNotWriteCharacteristic,
        CouldNotWriteDescription,
        CouldNotDiscoverServices,
        CouldNotEnableNotification,
        NotificationInQueue,
        NoGattAvaiable,
        PermissionError,
        UnkownCommand
    }



    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final int _RESTARTTIMEOUT = 5000;

    private BluetoothDevice _BluetoothDevice;
    private BluetoothGatt _BluetoothGatt;
    private Context _Context;
    private BLEDeviceStates _State = BLEDeviceStates.None;
    private BLEDeviceStates _OldState = BLEDeviceStates.None;

    private boolean _StateMachineStop = false;
    private BLEDeviceCommandStates _CommandState = BLEDeviceCommandStates.Idle;
    private BLEDeviceCommandStates _LastCommandState = BLEDeviceCommandStates.Idle;
    private  BLEDeviceCommand _ActualCommand = null;
    private BLEDeviceErrors _Error = BLEDeviceErrors.Ok;
    BLETimeOut _RestartTimeOut = new BLETimeOut();

    private OnBLEDeviceListener _Listener;
    private ArrayDeque<UUID> _NotificationCharacteristics = new ArrayDeque<>();
    private UUID _ServiceUIID = null;
    private BluetoothGattService _Service = null;
    private ArrayDeque<BLEDeviceCommand> _CommandQueue = new ArrayDeque<>();
    private boolean _AutoConnect = false;

    public BLEDevice(Context context,BluetoothDevice device,UUID service,boolean autoConnect)
    {
        _BluetoothDevice = device;
        _Context = context;
        _ServiceUIID = service;
        _AutoConnect = autoConnect;
        try {
            _BluetoothGatt = _BluetoothDevice.connectGatt(_Context, false, _GattCallback);
            if(_BluetoothGatt == null)
            {
                return ;
            }
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "connect " + exp.getMessage());
            return ;
        }
        this.start();
    }

    public BLEDevice(Context context,BluetoothDevice device)
    {
        _BluetoothDevice = device;
        _Context = context;

        try {
            _BluetoothGatt = _BluetoothDevice.connectGatt(_Context, false, _GattCallback);
            if(_BluetoothGatt == null)
            {
                return ;
            }
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "connect " + exp.getMessage());
            return ;
        }
        this.start();
    }

    public void setOnBLEDeviceListener(OnBLEDeviceListener listener)
    {
        _Listener = listener;
    }

    public void setAutoConnect(boolean auto)
    {
        _AutoConnect = auto;
    }
    public void setService(UUID serviceUUID)
    {
        _ServiceUIID = serviceUUID;
    }

    public String getDeviceState()
    {
        return _State.name();
    }

    public void addNotifiationCharacteristic(UUID characteristic)
    {
        _NotificationCharacteristics.push(characteristic);
    }

    public BLEDeviceErrors closeDevice() {
        _StateMachineStop = true;
        try {
            _BluetoothGatt.close();
        }
        catch(SecurityException exp) {
            Log.w("BLEDevice", "close " + exp.getMessage());
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }

    public boolean connectDevice() {
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.Connect);
        _CommandQueue.push(cmd);
        return true;
    }
    public boolean disconnectDevice() {
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.Disconnect);
        _CommandQueue.push(cmd);
        return false;
    }
    public boolean writeDescription(UUID characteristicUUID,UUID descriptionUUID,byte[] value){
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.WriteDescriptor,characteristicUUID,descriptionUUID,value,null);
        _CommandQueue.push(cmd);
        return true;
    }
    public boolean readDescription(UUID characteristicUUID,UUID descriptionUUID) {
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.ReadDescriptor,characteristicUUID,descriptionUUID,null,null);
        _CommandQueue.push(cmd);
        return true;
    }
    public boolean writeCharacteristic(UUID characteristicUUID,byte[] value){
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.WriteDescriptor,characteristicUUID,value,null,null);
        _CommandQueue.push(cmd);
        return true;
    }

    public boolean readCharacteristic(UUID characteristicUUID) {
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.ReadDescriptor,characteristicUUID,null,null,null);
        _CommandQueue.push(cmd);
        return true;
    }

    //privates
    private BLEDeviceErrors connect() {
        try {
            if(!_BluetoothGatt.connect())
            {
                return BLEDeviceErrors.CouldNotConnectDevice;
            }
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "disconnect " + exp.getMessage());
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors disconnect() {
        try {
            _BluetoothGatt.disconnect();
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "disconnect " + exp.getMessage());
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors writeDesc(UUID characteristicUUID,UUID descriptionUUID,byte[] value)
    {
        if(_Service == null)
        {
            return BLEDeviceErrors.ServiceNotValid;
        }
        BluetoothGattCharacteristic characteristic = _Service.getCharacteristic(characteristicUUID);
        if (characteristic == null) {
            return BLEDeviceErrors.CharacteristicNotFound;
        }
        try {

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptionUUID);
            if(descriptor == null)
            {
                return BLEDeviceErrors.DescriptionNotFound;
            }
            descriptor.setValue(value);
            if(!_BluetoothGatt.writeDescriptor(descriptor))
            {
                return BLEDeviceErrors.CouldNotWriteDescription;
            }
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "writeDesc " + exp.getMessage());
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors readDesc(UUID characteristicUUID,UUID descriptionUUID) {

        if(_Service == null)
        {
            return BLEDeviceErrors.ServiceNotValid;
        }
        BluetoothGattCharacteristic characteristic = _Service.getCharacteristic(characteristicUUID);
        if (characteristic == null) {
            return BLEDeviceErrors.CharacteristicNotFound;
        }
        try{
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptionUUID);
            if(descriptor == null)
            {
                return BLEDeviceErrors.DescriptionNotFound;
            }
             if(!_BluetoothGatt.readDescriptor(descriptor)){
                 return BLEDeviceErrors.CouldNotReadDescription;
             }
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "readDesc " + exp.getMessage());
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors writeCharacter(UUID characteristicUUID,byte[] value)
    {
        if(_Service == null)
        {
            return BLEDeviceErrors.ServiceNotValid;
        }
        BluetoothGattCharacteristic characteristic = _Service.getCharacteristic(characteristicUUID);
        if (characteristic == null) {
            return BLEDeviceErrors.CharacteristicNotFound;
        }
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        characteristic.setValue(value);
        try{
            if(!_BluetoothGatt.writeCharacteristic(characteristic))
            {
                return BLEDeviceErrors.CouldNotWriteCharacteristic;
            }
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "writeCharacter " + exp.getMessage());
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors readCharacter(UUID characteristicUUID) {
        if(_Service == null)
        {
            return BLEDeviceErrors.ServiceNotValid;
        }
        BluetoothGattCharacteristic characteristic = _Service.getCharacteristic(characteristicUUID);
        if (characteristic == null) {
            return BLEDeviceErrors.CharacteristicNotFound;
        }
       try{
           if(!_BluetoothGatt.readCharacteristic(characteristic)){
               return BLEDeviceErrors.CouldNotReadCharacteristic;
           }
       }
       catch(SecurityException exp) {
           Log.e("BLEDevice", "readCharacter " + exp.getMessage());
           return BLEDeviceErrors.PermissionError;
       }
        return BLEDeviceErrors.Ok;
    }
    private BLEDeviceErrors discoverServices()
    {
        try {
           if(!_BluetoothGatt.discoverServices()){
               return BLEDeviceErrors.CouldNotDiscoverServices;
           }
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "discoverServices " + exp.getMessage());
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors enableNotification(UUID characteristicUUID)
    {
        if(writeDesc(characteristicUUID,CCCD,BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) != BLEDeviceErrors.Ok){
            return BLEDeviceErrors.CouldNotEnableNotification;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors enableNotificationFromQueue()
    {
        BLEDeviceErrors result = BLEDeviceErrors.Ok;
        if(_NotificationCharacteristics.size() == 0)
        {
            return BLEDeviceErrors.Ok;
        }
        UUID ch = _NotificationCharacteristics.pop();
        result = enableNotification(ch);
        if(result == BLEDeviceErrors.Ok)
        {
            result =  BLEDeviceErrors.NotificationInQueue;
        }
       return result;
    }

    private void StateMachine() {
        BLEDeviceErrors result = BLEDeviceErrors.Ok;
        switch (_CommandState) {
            case Idle: {
                if(_CommandQueue.size() > 0)
                {
                    if(_BluetoothGatt == null)
                    {
                        _CommandQueue.clear();
                        _Error = BLEDeviceErrors.NoGattAvaiable;
                        _CommandState = BLEDeviceCommandStates.Error;
                        break;
                    }
                    _ActualCommand = _CommandQueue.poll();
                    if((_State == BLEDeviceStates.Disconnected) && (_ActualCommand.Command != BLEDeviceCommandStates.Connect))
                    {
                        _CommandQueue.clear();
                        _Error = BLEDeviceErrors.NotConnected;
                        _CommandState = BLEDeviceCommandStates.Error;
                        break;
                    }
                    if((_State == BLEDeviceStates.Connected) && (_ActualCommand.Command == BLEDeviceCommandStates.Connect))
                    {
                        _CommandQueue.clear();
                        _Error = BLEDeviceErrors.AlreadyConnected;
                        _CommandState = BLEDeviceCommandStates.Error;
                        break;
                    }
                    _CommandState = _ActualCommand.Command;
                }
                break;
            }
            case Connect: {
                _Error = connect();
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.Connecting;
                break;
            }
            case Connected: {
                _CommandState = BLEDeviceCommandStates.DiscoverServices;
                break;
            }
            case Disconnect: {
                _Error = disconnect();
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.Disconnecting;
                break;
            }
            case DisconnectedThruClient: {
                _State = BLEDeviceStates.Disconnected;
                if(_AutoConnect)
                {
                    if(_RestartTimeOut.check(_RESTARTTIMEOUT))
                    {
                        _CommandState = BLEDeviceCommandStates.Connect;
                    }
                    break;
                }
                _CommandState = BLEDeviceCommandStates.Disconnected;
                break;
            }
            case Disconnected: {
                _State = BLEDeviceStates.Disconnected;
                _CommandState = BLEDeviceCommandStates.Idle;
                break;
            }
            case DiscoverServices: {
                _Error = discoverServices();
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                }
                break;
            }
            case DiscoverServicesFinish: {
                _CommandState = BLEDeviceCommandStates.EnableNotificiction;
                break;
            }
            case EnableNotificiction: {
                _Error = enableNotificationFromQueue();
                if(_Error == BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.EnableNotificictionFinish;}
                else{
                    _CommandState = BLEDeviceCommandStates.Error;
                }
                break;
            }
            case EnablingNotification:
            {
                _Error = enableNotificationFromQueue();
                if(_Error == BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.EnableNotificictionFinish;}
                else if(_Error == BLEDeviceErrors.NotificationInQueue){
                    _CommandState = BLEDeviceCommandStates.EnableNotificiction;
                }
                else{
                    _CommandState = BLEDeviceCommandStates.Error;
                }
                break;
            }
            case EnableNotificictionFinish: {
                _CommandState = BLEDeviceCommandStates.Ready;
                break;
            }
            case Ready: {
                _State = BLEDeviceStates.Connected;
                _CommandState = BLEDeviceCommandStates.Idle;
                if(_Listener != null){
                    _Listener.OnStatusChanged(_State);
                }
            }
            case ReadCharacteristic: {
                _Error = readCharacter((UUID)_ActualCommand.Param1);
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.ReadingCharacteristic;
                break;
            }
            case ReadDescriptor: {
                _Error = readDesc((UUID)_ActualCommand.Param1,(UUID)_ActualCommand.Param2);
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.ReadingDescriptor;
                break;
            }
            case WriteCharacteristic: {
                _Error = writeCharacter((UUID)_ActualCommand.Param1,(byte[])_ActualCommand.Param2);
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.WritingCharacteristic;
                break;
            }
            case WriteDescriptor: {
                _Error = writeDesc((UUID)_ActualCommand.Param1,(UUID)_ActualCommand.Param2,(byte[])_ActualCommand.Param3);
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.WritingDescriptor;
                break;
            }
            case Error:{
                if(_Listener != null){
                    _Listener.onError(_Error);
                }
                _Error = BLEDeviceErrors.Ok;
                break;
            }
            default:{
                _Error = BLEDeviceErrors.UnkownCommand;
                _CommandState = BLEDeviceCommandStates.Error;
                break;
            }
        }//switch Command
    }

    @Override
    public void close()
    {
        closeDevice();
    }
    @Override
    public void run(){
        while (!_StateMachineStop) {
            StateMachine();
        }//while true
    }

    //callBack
    private final BluetoothGattCallback _GattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
           if(newState == BluetoothProfile.STATE_CONNECTED){
               _State = BLEDeviceStates.Connected;
           }
           else
           {
               if(_CommandState == BLEDeviceCommandStates.Disconnecting) {
                   _CommandState = BLEDeviceCommandStates.Disconnected;
               }
               else
               {
                   _CommandState = BLEDeviceCommandStates.DisconnectedThruClient;
               }
               _State = BLEDeviceStates.Disconnected;
               if(_Listener != null)
               {
                   _Listener.OnStatusChanged(_State);
               }
           }



        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _CommandState = BLEDeviceCommandStates.Idle;
            } else {
                Log.e("BLEDevice", "onDescriptorWrite : " + status);
                _CommandState = BLEDeviceCommandStates.Error;
            }
        }
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _CommandState = BLEDeviceCommandStates.Idle;
            } else {
                Log.e("BLEDevice", "onDescriptorRead : " + status);
                _CommandState = BLEDeviceCommandStates.Error;
            }
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _CommandState = BLEDeviceCommandStates.Idle;
            } else {
                Log.e("BLEDevice", "onCharacteristicWrite : " + status);
                _CommandState = BLEDeviceCommandStates.Error;
            }
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         byte[] value,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _CommandState = BLEDeviceCommandStates.ReadCharacteristicFinished;
            } else {
                Log.e("BLEDevice", "onCharacteristicRead : " + status);
                _CommandState = BLEDeviceCommandStates.Error;
            }
            if(_Listener != null)
            {
                _Listener.onCharacteristicRead(characteristic.getUuid(),value);
            }
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic,byte[] value) {
            if(_Listener != null)
            {
                _Listener.onCharacteristicNotification(characteristic.getUuid(),value);
            }

        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            if(_Listener != null)
            {
                _Listener.onCharacteristicNotification(characteristic.getUuid(),characteristic.getValue());
            }

        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _CommandState = BLEDeviceCommandStates.DiscoverServicesFinish;
            } else {
                Log.e("BLEDevice", "onServicesDiscovered : " + status);
                _CommandState = BLEDeviceCommandStates.Error;
            }
        }




    };



}
