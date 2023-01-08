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
package CipedTronic.products.cipedtronicapp.mcu.BLE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
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
        public BLEDeviceCommand(
                BLEDeviceCommandStates command,
                Object param1,
                Object param2,
                Object param3,
                Object param4,
                Object param5
        )
        {
            Param1 = param1;
            Param2 = param2;
            Param3 = param3;
            Param4 = param4;
            Param5 = param5;
            Command = command;
        }
        public BLEDeviceCommandStates Command;
        public Object Param1;
        public Object Param2;
        public Object Param3;
        public Object Param4;
        public Object Param5;
    }

    public interface OnBLEDeviceListener
    {
        void OnStatusChanged(BLEDeviceStates state);
        void onCharacteristicRead(UUID characteristicUUID,byte[] value);
        void onCharacteristicNotification(UUID characteristicUUID,byte[] value);
        void onScanResult(List<BLEScannedDevice> devices, BLEDeviceStates state);
        void onError(BLEDeviceErrors error);
    }

    public enum BLEDeviceStates {
        None,
        NotInitalized,
        Disconnected,
        Connected,
    }

    public enum BLEDeviceCommandStates
    {
        Idle,
        Initialize,
        Close,
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
        ScanDevices,
        ScanDevicesStart,
        ScanningDevices,
        ScanDevicesFinished,
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
        CouldNotInitializeBLEManager,
        CouldNotInitializeBLEAdapter,
        CouldNotInitializeBLEScanner,
        CouldNotConnectGatt,
        CouldNotGetDevice,
        NotInitialized,
        AlreadyInitialized,
        NotConnected,
        AlreadyConnected,
        ConnectTimeout,
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
        NoScannerFound,
        ScanningFailed,
        UnkownCommand
    }



    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final int _RESTARTTIMEOUT = 5000;

    private BluetoothManager _BluetoothManager;
    private BluetoothAdapter _BluetoothAdapter;
    private String _BluetoothDeviceAddress;
    private BluetoothLeScanner _Scanner;

    private boolean _Valid;
    private List<BLEScannedDevice> _ScannedDeviceList = new ArrayList<>();
    private Context _Context;
    private BluetoothDevice _BluetoothDevice;
    private BluetoothGatt _BluetoothGatt;

    private BLEDeviceStates _State = BLEDeviceStates.NotInitalized;
    private BLEDeviceStates _LastConnectedState = BLEDeviceStates.None;

    private boolean _StateMachineStop = false;
    private BLEDeviceCommandStates _CommandState = BLEDeviceCommandStates.Idle;
    private BLEDeviceCommandStates _LastCommandState = BLEDeviceCommandStates.Idle;

    private  BLEDeviceCommand _ActualCommand = null;
    private BLEDeviceErrors _Error = BLEDeviceErrors.Ok;
    private boolean _ScanActive = false;

    String _Name = "";
    BLETimeOut _RestartTimeOut = new BLETimeOut();
    BLETimeOut _ScanTimeOut = new BLETimeOut();
    BLETimeOut _ConnectTimeout = new BLETimeOut();

    private OnBLEDeviceListener _Listener;
    private List<UUID> _NotificationCharacteristics = new ArrayList<>();
    private int _NotificationListCounter = 0;
    private UUID _ServiceUIID = null;
    private BluetoothGattService _Service = null;
    private ArrayDeque<BLEDeviceCommand> _CommandQueue = new ArrayDeque<>();
    private boolean _AutoConnect = false;


    public BLEDevice()
    {
        start();
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
        _NotificationCharacteristics.add(characteristic);
    }


    public void createDevice(Context context,String address,String name,UUID service,boolean autoConnect) {
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.Initialize,context,address,name,service,autoConnect);
        _CommandQueue.push(cmd);
    }
    public void closeDevice() {
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.Close);
        _CommandQueue.push(cmd);
    }
    public void connectDevice() {
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.Connect);
        _CommandQueue.push(cmd);
    }
    public void disconnectDevice() {
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.Disconnect);
        _CommandQueue.push(cmd);
    }

    public void scanDevices()
    {
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.ScanDevices);
        _CommandQueue.push(cmd);
    }
    public void writeDescription(UUID characteristicUUID,UUID descriptionUUID,byte[] value){
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.WriteDescriptor,characteristicUUID,descriptionUUID,value,null);
        _CommandQueue.push(cmd);
    }
    public void readDescription(UUID characteristicUUID,UUID descriptionUUID) {
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.ReadDescriptor,characteristicUUID,descriptionUUID,null,null);
        _CommandQueue.push(cmd);
    }
    public void writeCharacteristic(UUID characteristicUUID,byte[] value){
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.WriteDescriptor,characteristicUUID,value,null,null);
        _CommandQueue.push(cmd);
    }
    public void readCharacteristic(UUID characteristicUUID) {
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.ReadDescriptor,characteristicUUID,null,null,null);
        _CommandQueue.push(cmd);
    }

    //privates
    private BLEDeviceErrors initializeAdapter() {
        _BluetoothManager = (BluetoothManager) _Context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (_BluetoothManager == null) {
            return BLEDeviceErrors.CouldNotInitializeBLEManager;
        } else {
            _BluetoothAdapter = _BluetoothManager.getAdapter();
            if (_BluetoothAdapter == null) {
                return BLEDeviceErrors.CouldNotInitializeBLEAdapter;
            }
        }

        _Scanner = _BluetoothAdapter.getBluetoothLeScanner();
        if(_Scanner == null) {
            return BLEDeviceErrors.CouldNotInitializeBLEScanner;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors create(Context context,String address,String name, UUID service,boolean autoConnect) {
       BLEDeviceErrors result = BLEDeviceErrors.Ok;
        _Context = context;
        _BluetoothDeviceAddress = address;
        _AutoConnect = autoConnect;
        _ServiceUIID = service;
        _Name = name;
        try {
            result = initializeAdapter();
            if(result != BLEDeviceErrors.Ok) {
                return result;
            }
            _BluetoothDevice = _BluetoothAdapter.getRemoteDevice(_BluetoothDeviceAddress);
            if(_BluetoothDevice == null){
                return BLEDeviceErrors.CouldNotGetDevice;
            }
            _BluetoothGatt = _BluetoothDevice.connectGatt(_Context, false, _GattCallback);
            if(_BluetoothGatt == null)
            {
                return BLEDeviceErrors.CouldNotConnectGatt;
            }

        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "connect " + exp.getMessage());
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors connect() {
        Log.e("BLEDevice", "connect() ");
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

    private BLEDeviceErrors getService()
    {
        try {
            _Service =_BluetoothGatt.getService(_ServiceUIID);
            if(_Service == null)
            {
                return BLEDeviceErrors.ServiceNotFound;
            }
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "discoverServices " + exp.getMessage());
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }
    private BLEDeviceErrors setNotificationEvent(UUID characteristicUUID)
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
            if(!_BluetoothGatt.setCharacteristicNotification(characteristic, true))
            {
                return BLEDeviceErrors.CouldNotEnableNotification;
            }
        }
             catch(SecurityException exp){
                return BLEDeviceErrors.PermissionError;
            }
            return BLEDeviceErrors.Ok;

    }
    private BLEDeviceErrors enableNotification(UUID characteristicUUID)
    {
        BLEDeviceErrors result = BLEDeviceErrors.Ok;
        Log.e("BLEDevice", "enableNotification : " + characteristicUUID.toString());
        result = setNotificationEvent(characteristicUUID);
        if(result != BLEDeviceErrors.Ok)
        {
            return result;
        }
        if(writeDesc(characteristicUUID,CCCD,BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) != BLEDeviceErrors.Ok){
            return BLEDeviceErrors.CouldNotEnableNotification;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors enableNotificationFromQueue()
    {
        BLEDeviceErrors result = BLEDeviceErrors.Ok;
        if(_NotificationListCounter >= _NotificationCharacteristics.size())
        {
            _NotificationListCounter = 0;
            return BLEDeviceErrors.Ok;
        }
        UUID ch = _NotificationCharacteristics.get(_NotificationListCounter);
        _NotificationListCounter++;
        result = enableNotification(ch);
        if(result == BLEDeviceErrors.Ok)
        {
            result =  BLEDeviceErrors.NotificationInQueue;
        }
       return result;
    }

    private BLEDeviceErrors startScan()
    {
        if (_Scanner == null) {
            return BLEDeviceErrors.NoScannerFound;
        }
        try {

            List<ScanFilter> filters = new ArrayList<>();
            ScanFilter filter = new ScanFilter.Builder()
                    .setDeviceName(_Name)
                    .build();
            filters.add(filter);
            ScanSettings scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .build();
            _Scanner.startScan(filters, scanSettings, _LeScanCallback);
        }
        catch(SecurityException exp){
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors stopScan()
    {
        try {
            _Scanner.stopScan(_LeScanCallback);
        }
         catch(SecurityException exp){
                return BLEDeviceErrors.PermissionError;
            }
            return BLEDeviceErrors.Ok;
    }

    private boolean StateMachine() {
        BLEDeviceErrors result = BLEDeviceErrors.Ok;
        switch (_CommandState) {
            case Idle: {
                if(_CommandQueue.size() > 0)
                {
                    _ActualCommand = _CommandQueue.poll();
                    if((_State == BLEDeviceStates.NotInitalized) && (_ActualCommand.Command != BLEDeviceCommandStates.Initialize)){
                        _Error = BLEDeviceErrors.NotInitialized;
                        _CommandState = BLEDeviceCommandStates.Error;
                        break;
                    }

                   /* if((_State != BLEDeviceStates.NotInitalized) && (_ActualCommand.Command == BLEDeviceCommandStates.Initialize)){
                        _Error = BLEDeviceErrors.AlreadyInitialized;
                        _CommandState = BLEDeviceCommandStates.Error;
                        break;
                    }*/

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
            case Initialize: {
                _Error = create(
                        (Context) _ActualCommand.Param1,
                        (String)_ActualCommand.Param2,
                        (String)_ActualCommand.Param3,
                        (UUID) _ActualCommand.Param4,
                        (boolean) _ActualCommand.Param5);
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _State = BLEDeviceStates.Disconnected;
                _CommandState = BLEDeviceCommandStates.Connect;
                break;
            }
            case Close:
            {
                return true;
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
            case Connecting:
            {
                if(_ConnectTimeout.check(5000))
                {
                    if(_AutoConnect)
                    {
                        _CommandState = BLEDeviceCommandStates.Connect;
                        break;
                    }
                    else
                    {
                        _Error = BLEDeviceErrors.ConnectTimeout;
                        _CommandState = BLEDeviceCommandStates.Error;
                    }
                }
                break;
            }
            case Connected: {
                _CommandState = BLEDeviceCommandStates.DiscoverServices;
                Log.e("BLEDevice", "Connected ");
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
            case Disconnecting:
            {
                break;
            }
            case DisconnectedThruClient: {
                _State = BLEDeviceStates.Disconnected;
                if(_AutoConnect)
                {
                    if(_RestartTimeOut.check(_RESTARTTIMEOUT))
                    {
                        Log.d("BLEDevice", "Restart ");
                        _CommandState = BLEDeviceCommandStates.Connect;
                    }
                    break;
                }
                _CommandState = BLEDeviceCommandStates.Disconnected;
                break;
            }
            case Disconnected: {
                _State = BLEDeviceStates.Disconnected;
                if(_ScanActive)
                {
                    _CommandState = BLEDeviceCommandStates.ScanDevicesStart;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.Idle;
                break;
            }
            case DiscoverServices: {
                _Error = discoverServices();
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.DiscoveringServices;
                break;
            }
            case DiscoveringServices:
            {
                break;
            }
            case DiscoverServicesFinish: {
                _Error = getService();
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.EnableNotificiction;
                break;
            }
            case EnableNotificiction: {
                _Error = enableNotificationFromQueue();
                if(_Error == BLEDeviceErrors.Ok)
                {
                    _CommandState = BLEDeviceCommandStates.Ready;
                    break;
                }
                if(_Error == BLEDeviceErrors.CouldNotEnableNotification){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.EnablingNotification;
                break;
            }
            case EnablingNotification:
            {
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
                break;
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
            case ReadingCharacteristic:
            {
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
            case ReadingDescriptor:
            {
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
            case WritingCharacteristic:
            {
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
            case WritingDescriptor:
            {
                break;
            }
            case ScanDevices:
            {
                _ScanActive = true;
                _LastConnectedState = _State;
                if(_State == BLEDeviceStates.Connected)
                {
                    _CommandState = BLEDeviceCommandStates.Disconnect;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.ScanDevicesStart;
                break;
            }
            case ScanDevicesStart:
            {
                _Error = startScan();
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.ScanningDevices;
                break;
            }
            case ScanningDevices:
            {
                if(_ScanTimeOut.check(5000))
                {
                    stopScan();
                    _CommandState = BLEDeviceCommandStates.ScanDevicesFinished;
                    break;
                }
                break;
            }
            case ScanDevicesFinished:
            {
                _ScanActive = false;
                if(_Listener != null){
                    _Listener.onScanResult(_ScannedDeviceList,_State);
                }
                if(_LastConnectedState == BLEDeviceStates.Connected)
                {
                    _CommandState = BLEDeviceCommandStates.Connect;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.Idle;
                break;
            }
            case Error:{
                Log.e("BLEDevice", "Error " + _Error.name());
                if(_Listener != null){
                    _Listener.onError(_Error);
                }
                _Error = BLEDeviceErrors.Ok;
                _CommandState = BLEDeviceCommandStates.Idle;
                break;
            }
            default:{
                _Error = BLEDeviceErrors.UnkownCommand;
                Log.e("BLEDevice", "UnkownCommand " + _CommandState.name());
                _CommandState = BLEDeviceCommandStates.Error;

                break;
            }
        }//switch Command
        return false;
    }

    @Override
    public void close()
    {
        closeDevice();
    }
    @Override
    public void run(){
        while (true) {
            if(StateMachine())
            {
               return;
            }
            if(_LastCommandState != _CommandState){
                Log.d("BLEDevice", "StatetChanged " + _CommandState.name());
                _LastCommandState = _CommandState;
            }
        }//while true
    }

    //callBacks
    private final BluetoothGattCallback _GattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
           if(newState == BluetoothProfile.STATE_CONNECTED){
               if(_CommandState == BLEDeviceCommandStates.Connecting) {
                   _CommandState = BLEDeviceCommandStates.Connected;
               }
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
                if(_CommandState == BLEDeviceCommandStates.EnablingNotification)
                {
                    _Error = enableNotificationFromQueue();
                    if(_Error == BLEDeviceErrors.Ok){
                        _CommandState = BLEDeviceCommandStates.EnableNotificictionFinish;
                    }
                    else if(_Error == BLEDeviceErrors.NotificationInQueue){
                        ;
                    }
                    else{
                        _CommandState = BLEDeviceCommandStates.Error;
                    }
                }
                else {
                    _CommandState = BLEDeviceCommandStates.Idle;
                }
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

    private ScanCallback _LeScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if(result != null)
                    {
                        String deviceName = "";
                        String deviceAddress = "";

                        BLEScannedDevice dev = new BLEScannedDevice();
                        try
                        {
                            dev.Address = result.getDevice().getAddress();
                            dev.Name = result.getDevice().getName();
                        }
                        catch(SecurityException exp)
                        {
                            Log.i("BLEAdapter", " _LeScanCallback" + exp.getMessage());
                            _Error = BLEDeviceErrors.ScanningFailed;
                            _CommandState = BLEDeviceCommandStates.Error;
                        }

                        if(!_ScannedDeviceList.contains(dev) && dev.Name != null)
                        {
                            Log.i("BLEAdapter", " Scan ." + deviceName);
                            _ScannedDeviceList.add(dev);
                        }

                    }
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);

                }

                @Override
                public void onScanFailed(int errorCode)
                {
                    super.onScanFailed(errorCode);
                    _Error = BLEDeviceErrors.ScanningFailed;
                    _CommandState = BLEDeviceCommandStates.Error;
                }
            };



}
