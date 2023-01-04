package com.example.cipedtronicapp.mcu.mcu;

import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.example.cipedtronicapp.mcu.BLE.BLEAdapter;
import com.example.cipedtronicapp.mcu.BLE.BLECipedTronic;
import com.example.cipedtronicapp.mcu.BLE.BLEDevice;
import com.example.cipedtronicapp.mcu.BLE.BLEScannedDevice;
import com.example.cipedtronicapp.mcu.BLE.BLETimeOut;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CipedTronicMCU extends  Thread{
    public interface OnCipedTronicDeviceListener {
        public void onDeviceScanResult(List<BLEScannedDevice> devices);
        public void onDeviceError(String error);
        public void onStateChanged(McuStates state);
        public void onDataUpdate(CipedtronicData data);
    }

    private static volatile CipedTronicMCU INSTANCE = null;
    //States
    public enum McuStates
    {
        StateIdle,
        StateInit,
        StateDeinit,
        StateConnect,
        StateConnectWait,
        StateConnected,
        StateDisconnect,
        StateDisconnectWait,
        StateDisconnected,
        StateRestart,
        StateStartScan,
        StateScanWait,
        StateScanComplete,
        StateStartResetMcu,
        StateResetMcuWait,
        StateResetMcuComplete,
        StateSetId,
        StateSetIdWait,
        StateSetIdComplete,
        StateGetId,
        StateGetIdWait,
        StateGetIdComplete,
        StateError,
        StateNone
    }

    private final long MCU_CMD_RESET_COUNTER = 0x01;

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
    private BLECipedTronic  _BLECipedTronic;
    private String _BluetoothState = "";
    private List<BLEScannedDevice> _ScannedDevices = new ArrayList<>();
    private String _BleAddress = "";


    //others
    private  Context _Context;
    private boolean _deviceReady = false;
    private CipedtronicData mcuData = new CipedtronicData();

    //State machine
    private ArrayDeque<McuStates> _CommandQueue = new ArrayDeque<>();
    private McuStates _LastState = McuStates.StateIdle;
    private McuStates _State = McuStates.StateIdle;
    private McuStates _OldState = McuStates.StateIdle;

    private boolean _StateMachineBusy = false;
    BLETimeOut _RestartTimeOut = new BLETimeOut();
    BLETimeOut _StateMachineTimeOut = new BLETimeOut();
    private boolean _AutomaticRestart = false;
    private  boolean _Connected = false;



    private CipedTronicMCU(Context context)
    {
        _Context = context;
        _BLEAdapter = new BLEAdapter(context);
        _BLEAdapter.setOnScanListener(_OnScanListener);
        this.start();
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

    public void StateMachine()
    {
        switch (_State) {
            case StateIdle: //Idle
            {
                    /*if (_BluetoothState == "STATE_CONNECTED") {
                        _State = McuStates.StateConnected;
                    }
                    if (_BluetoothState == "STATE_DISCONNECTED") {
                        _State = McuStates.StateDisconnected;
                    }*/
                break;
            }
            case StateInit: {
                _StateMachineBusy = true;
                _BLEDevice = _BLEAdapter.getDevice(_BleAddress);
                _BLECipedTronic = new BLECipedTronic(_BLEDevice,CIPEDTRONIC_UUID);
                _BLECipedTronic.setOnReceiveListener(_OnCipedTronicListener);
                if (_BLEDevice == null) {
                    _State = McuStates.StateError;
                    break;
                }

                _State = McuStates.StateConnect;
                break;
            }
            case StateDeinit: {
                _BLEDevice.close();
                return;
            }
            case StateConnect: {
                if(_Connected)
                {
                    _State = McuStates.StateConnected;
                    break;
                }
                _StateMachineBusy = true;
                _AutomaticRestart = true;
                if (!_BLEDevice.connect()) {
                    _State = McuStates.StateError;
                } else {
                    _StateMachineTimeOut.reset();
                    _State = McuStates.StateConnectWait;
                }
                break;
            }
            case StateConnectWait:{
                if (_BluetoothState == "STATE_CONNECTED") {
                    _State = McuStates.StateConnected;
                } else {
                    if (_StateMachineTimeOut.check(2000)) {
                        if(_AutomaticRestart) {
                            _State = McuStates.StateRestart;
                        }
                        else {
                            _State = McuStates.StateError;
                        }
                        break;
                    }
                }
                break;
            }
            case StateConnected: {

                if (_BluetoothState == "STATE_DISCONNECTED") {
                    _Connected = false;
                    _State = McuStates.StateDisconnected;
                    _AutomaticRestart = true;
                    break;
                }
                _AutomaticRestart = false;
                _StateMachineBusy = false;
                _Connected = true;
                break;
            }
            case StateDisconnect: {

                if(!_Connected)
                {
                    _State = McuStates.StateDisconnected;
                    break;
                }
                _StateMachineBusy = true;
                _AutomaticRestart = false;

                _BLECipedTronic.disconnect();
                _StateMachineTimeOut.reset();

                _State = McuStates.StateDisconnectWait;
                break;
            }
            case StateDisconnectWait: {
                //if timoute

                if (_BluetoothState == "STATE_DISCONNECTED") {
                    _State = McuStates.StateDisconnected;
                } else {
                    if (_StateMachineTimeOut.check(2000)) {
                        _State = McuStates.StateError;
                    }
                }
                break;
            }
            case StateDisconnected: {
                _StateMachineBusy = false;
                _Connected = false;
                if(_AutomaticRestart) {
                    _RestartTimeOut.reset();
                    _State = McuStates.StateRestart;
                }

                break;
            }
            case StateRestart: {
                _StateMachineBusy = false;
                if (_RestartTimeOut.check(10000)) {
                    _State = McuStates.StateConnect;
                }
                break;

            }
            case StateStartScan:
            {
                if(_Connected)
                {
                    _State = _LastState;
                    break;
                }
                Log.d("CipedTronicMCU", "start scan");
                _StateMachineBusy = true;
                _ScannedDevices.clear();
                _BLEAdapter.scanDevices("CIPBLA");
                _State = McuStates.StateScanWait;
                break;
            }
            case StateScanComplete:
            {
                Log.d("CipedTronicMCU", "Scan complete");
                _StateMachineBusy = false;
                _Listener.onDeviceScanResult(_ScannedDevices);
                _State = _LastState;
                break;
            }
            case StateStartResetMcu:
            {
                if(!_Connected)
                {
                    _State = McuStates.StateResetMcuComplete;
                    break;
                }
                Log.d("CipedTronicMCU", "StateStartResetMcu");
                _StateMachineBusy = true;
                if(!_BLECipedTronic.writeControl(MCU_CMD_RESET_COUNTER))
                {
                    _StateMachineTimeOut.reset();
                    _State = McuStates.StateError;
                    break;
                }
                _StateMachineTimeOut.reset();
                _State = McuStates.StateResetMcuWait;
                break;
            }
            case StateResetMcuWait:
            {
                if(_StateMachineTimeOut.check(2000))
                {
                    _State = McuStates.StateError;
                    break;
                }
                break;
            }
            case StateResetMcuComplete:
            {
                Log.d("CipedTronicMCU", "StateResetMcuComplete");
                _State = _LastState;
                _StateMachineBusy = false;
                break;
            }
            case StateSetId:
            {
                if(!_Connected)
                {
                    _State = McuStates.StateSetIdComplete;
                    break;
                }
                _StateMachineBusy = true;
                _BLECipedTronic.writeId(_Id);
                _StateMachineTimeOut.reset();
                _State = McuStates.StateSetIdWait;
                break;
            }
            case StateSetIdWait:
            {
                if(_StateMachineTimeOut.check(2000))
                {
                    _State = McuStates.StateError;
                    break;
                }
                break;
            }

            case StateSetIdComplete:
            {
                _State = _LastState;
                _StateMachineBusy = false;
                break;
            }
            case StateGetId:
            {
                if(!_Connected)
                {
                    _State = McuStates.StateGetIdComplete;
                    break;
                }
                _StateMachineBusy = true;
                _BLECipedTronic.readId();
                _StateMachineTimeOut.reset();
                _State = McuStates.StateGetIdWait;
                break;
            }
            case StateGetIdWait:
            {
                if(_StateMachineTimeOut.check(2000))
                {
                    _State = McuStates.StateError;
                    break;
                }
                break;
            }

            case StateGetIdComplete:
            {
                _State = _LastState;
                _StateMachineBusy = false;
                break;
            }
            case StateError:
            {
                _StateMachineBusy = false;
                _Listener.onDeviceError("Error");
                _State = McuStates.StateIdle;
                break;
            }
        }
    }

    @Override
    public void run(){
        while (true) {

            if (!_CommandQueue.isEmpty()&& !_StateMachineBusy) {
                _LastState = _State;
                _State = _CommandQueue.poll();
            }

            StateMachine();


            if(_State != _OldState)
            {
                Log.d("CipedTronicMCU", _State.name());
                if(_Listener != null) {
                    _Listener.onStateChanged(_State);
                }
            }
            _OldState = _State;
        }//while true


    }




    BLEAdapter.OnScanListener _OnScanListener = new BLEAdapter.OnScanListener() {
        @Override
        public void onScanResult(List<BLEScannedDevice> devices, String State) {
            if(_State == McuStates.StateScanWait)
            {
                _ScannedDevices = devices;
                _State = McuStates.StateScanComplete;
            }
        }
    };


    BLECipedTronic.OnCipedTronicListener _OnCipedTronicListener = new BLECipedTronic.OnCipedTronicListener() {
        @Override
        public void onReceive(long pulsesPerSecond,long pulsesPerSecondMax,long pulsesPerSecondAvg,long pulses) {
            CalculateDeviceData(pulsesPerSecond,pulsesPerSecondMax,pulsesPerSecondAvg,pulses);
        }

        @Override
        public void onRead(byte[] data) {
            switch(_State)
            {
                case StateGetIdWait:
                {
                    ByteBuffer buffer = ByteBuffer.allocate(4);//
                    buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
                    if(data.length != 4)
                    {
                        _State = McuStates.StateError;
                        return;
                    }
                    buffer.put(data);
                    _IdFromDevice = (long)(buffer.getInt(0) & 0xFFFFFFFFL);
                    _State = McuStates.StateGetIdComplete;
                    break;
                }
                default:
                {
                    _State = McuStates.StateError;
                }
            }
        }

        @Override
        public void onWritten() {
            switch(_State)
            {
                case StateResetMcuWait:
                {
                    _State = McuStates.StateResetMcuComplete;
                    break;
                }
                case StateSetIdWait:
                {
                    _State = McuStates.StateSetIdComplete;
                    break;
                }
                default:
                {
                    _State = McuStates.StateError;
                }
            }
        }

        @Override
        public void onStatusChanged(String state) {
                _BluetoothState = state;
        }

    };

    private void CalculateDeviceData(long pulsesPerSecond,long pulsesPerSecondMax,long pulsesPerSecondAverage,long pulses)
    {
        _PulsesPerSecond = pulsesPerSecond;
        _PulsesPerSecondMax = pulsesPerSecondMax;
        _PulsesPerSecondAverage = pulsesPerSecondAverage;
        _Pulses = pulses;
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
    public boolean getDeviceReady(){return _deviceReady;}
    public String getMcuState(){return _State.toString();}

    public boolean setMCUId(long id)
    {
        _Id = id;
        _CommandQueue.push(McuStates.StateSetId);
       return true;
    }


    public boolean start(String address)
    {
        _BleAddress = address;
        _CommandQueue.add(McuStates.StateInit);
        return true;
    }

    public void close()
    {
        _CommandQueue.add(McuStates.StateDeinit);
    }

    public void StartScan()
    {
        _CommandQueue.add(McuStates.StateDisconnect);
        _CommandQueue.add(McuStates.StateStartScan);
        _CommandQueue.add(McuStates.StateConnect);
    }
    public void ResetMCU()
    {
        _CommandQueue.add(McuStates.StateStartResetMcu);
    }


@Override
    public void finalize()
    {
        close();
    }
}
