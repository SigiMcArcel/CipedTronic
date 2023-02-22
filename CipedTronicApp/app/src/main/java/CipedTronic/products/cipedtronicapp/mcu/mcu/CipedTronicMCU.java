package CipedTronic.products.cipedtronicapp.mcu.mcu;

import android.content.Context;

import CipedTronic.products.cipedtronicapp.mcu.BLE.BLEDevice;
import CipedTronic.products.cipedtronicapp.mcu.BLE.BLEScannedDevice;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Abstraction of the cipedTronic device
 * - Configuring, starts and controlling the BLE Service
 * - calculates the recieved data to readable values
 */
public class CipedTronicMCU extends  BLEDevice{


        private final long CipedStateRunning = 0x00000001;
        private final long CipedStateError = 0x00000002;
        private final long CipedStateLowBat = 0x00000004;
        private final long CipedStateMove = 0x00000008;
        private final long CipedStateLightOn = 0x000000010;
        private final long CipedStateAlarmActivated = 0x000000020;
        private final long CipedStateAlarmActive = 0x000000040;

    private static volatile CipedTronicMCU _Instance = null;
    public static CipedTronicMCU getInstance()
    {
        if(_Instance == null)
        {
            _Instance = new CipedTronicMCU();
        }
        return _Instance;
    }

    public interface OnCipedTronicDeviceListener {
        public void onStatusChanged(String state);
        public void onDeviceError(String error);
        public void onDataUpdate(CipedtronicData data);
        void onScanResult(List<BLEScannedDevice> devices, String state);
    }

    private final long MCU_CMD_RESET_COUNTER = 0x01;
    public static final UUID CIPED_MEASUREMENT_CHARACTER_UUID = UUID.fromString("b1fb0001-f607-42a1-827d-f84ae6bdf20a");
    public static final UUID CIPED_CONTROL_POINT_CHARACTER_UUID = UUID.fromString("b1fb0002-f607-42a1-827d-f84ae6bdf20a");

    private List<OnCipedTronicDeviceListener> _Listeners = new ArrayList<>();
    private CipedTronicControlPoint _CipedTronicControlPoint = new CipedTronicControlPoint(this,CIPED_CONTROL_POINT_CHARACTER_UUID);

    private double pi = 3.14159265358979;
    private double _PulsesPerRevolution = 18.0;
    private double _RadiusOfWheelMM = 365.0;
    private double _Velocity = 0.0;
    private double _VelocityMax = 0.0;
    private double _VelocityAvg = 0.0;
    private double _Distance = 0.0;
    private double _BatteryVoltage;

    //ciped mcu data
    private long    _Pulses = 0;
    private long    _PulsesPerSecond = 0;
    private long    _PulsesPerSecondMax = 0;
    private long    _PulsesPerSecondAverage = 0;
    private long    _BatteryVoltageMillvolt = 0;
    private long    _CipedState;
    private boolean _LightOn = false;
    private boolean _AlarmOn = false;
    //Bluetooth
    public static final UUID CIPED_SERVICE_UUID = UUID.fromString("b1fb1816-f607-42a1-827d-f84ae6bdf20a");

    private CipedtronicData mcuData = new CipedtronicData();

    public CipedTronicMCU() {
        super();
        addNotifiationCharacteristic(CIPED_MEASUREMENT_CHARACTER_UUID);
        setOnBLEDeviceListener(_OnBLEDeviceListener);
    }

    public void setOnCipedTronicDeviceListener(OnCipedTronicDeviceListener listener)
    {
        _Listeners.add(listener);
    }



    private void CalculateDeviceData()
    {

        if(_PulsesPerRevolution == 0) {_PulsesPerRevolution = 1;}
        _Velocity = (double) _PulsesPerSecond * _RadiusOfWheelMM / 1000 * 2 * pi * 3600 / _PulsesPerRevolution / 1000;
        _VelocityMax = (double) _PulsesPerSecondMax * _RadiusOfWheelMM / 1000 * 2 * pi * 3600 / _PulsesPerRevolution / 1000;
        _VelocityAvg = (double) _PulsesPerSecondAverage * _RadiusOfWheelMM / 1000 * 2 * pi * 3600 / _PulsesPerRevolution / 1000;
        _Distance = (double) _Pulses * _RadiusOfWheelMM / 1000 * 2 * pi / _PulsesPerRevolution / 1000;
        _BatteryVoltage = (double)_BatteryVoltageMillvolt /1000;
        //_Revolutions = _Pulses / _PulsesPerRevolution;


        mcuData.Velocity = String.format("%.2f",_Velocity);
        mcuData.MaxVelocity = String.format("%.2f",_VelocityMax);
        mcuData.AvgVelocity = String.format("%.2f",_VelocityAvg);
        mcuData.Distance = String.format("%.2f",_Distance);
        mcuData.Pulses = String.format("%d",_Pulses);
        mcuData.PulsesPerSecond = String.format("%d",_PulsesPerSecond);
        mcuData.BatteryVoltage = String.format("%.2f",_BatteryVoltage);


    }

    public void decodeCipedState(long state )
    {
        mcuData.StateLight = false;
        mcuData.StateAlarm = false;
        mcuData.StateMove = false;
        mcuData.StateLowBat = false;
        mcuData.StateRunning = false;
        mcuData.StateError = false;
        mcuData.StateAlarmActive = false;

        _LightOn = false;
        _AlarmOn = false;
        if((state & CipedStateLightOn) > 0){
            mcuData.StateLight = true;
            _LightOn = true;
        }
        if((state & CipedStateAlarmActive) >0){
            mcuData.StateAlarm = true;
            _AlarmOn = true;
        }
        if((state & CipedStateAlarmActivated) >0){
            mcuData.StateAlarmActive = true;
        }
        if((state & CipedStateError) >0){
            mcuData.StateError = true;
        }
        if((state & CipedStateMove) >0){
            mcuData.StateMove = true;
        }
        if((state & CipedStateLowBat) >0){
            mcuData.StateLowBat = true;
        }
        if((state & CipedStateRunning) >0){
            mcuData.StateRunning = true;
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
    public String getBatteryVoltage()
    {
        return String.valueOf(_BatteryVoltage);
    }
    public void setWheelRadius(double r)
    {
        _RadiusOfWheelMM = r;
    }
    public void setPulsesPerRevolution(int p)
    {
        _PulsesPerRevolution = p;
    }
    public boolean getLight(){return _LightOn;}
    public boolean getAlarm(){return  _AlarmOn;}
    public CipedtronicData getData()
    {
        return mcuData;
    }


    public void setLight(boolean on){
        _CipedTronicControlPoint.SetLight(on);
    }
    public void setAlarm(boolean on) {
        _CipedTronicControlPoint.SetAlarm(on);
    }


    public void ResetMCU() {
        _CipedTronicControlPoint.ResetCounter();
    }

    public void createDevice(Context context,String address) {

        createDevice(context,address,"CIPED",CIPED_SERVICE_UUID,true);
    }
    BLEDevice.OnBLEDeviceListener _OnBLEDeviceListener = new OnBLEDeviceListener() {
        @Override
        public void OnStatusChanged(BLEDeviceStates state) {

        }

        @Override
        public void onCharacteristicRead(UUID characteristicUUID, byte[] value) {

        }

        @Override
        public void onDescriptionRead(UUID characteristicUUID, UUID descriptionUUID, byte[] value) {

        }

        @Override
        public void onCharacteristicNotification(UUID characteristicUUID, byte[] value) {
            ByteBuffer buffer = ByteBuffer.allocate(20);//   .wrap(value);
            buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
            if(value.length != 20)
            {
                return;
            }

            buffer.put(value);
            if(characteristicUUID.compareTo(CIPED_MEASUREMENT_CHARACTER_UUID) == 0)
            {
                _Pulses = (long)(buffer.getInt(0) & 0xFFFFFFFFL);
                _PulsesPerSecond = (long)(buffer.getInt(4) & 0xFFFFFFFFL);
                _PulsesPerSecondMax = (long)(buffer.getInt(8) & 0xFFFFFFFFL);
                _PulsesPerSecondAverage = (long)(buffer.getInt(12) & 0xFFFFFFFFL);
                _CipedState = (long)(buffer.getInt(16) & 0xFFFFFFFFL);
            }else{
                return;
            }
            decodeCipedState(_CipedState);
            CalculateDeviceData();
            for (OnCipedTronicDeviceListener listener:_Listeners) {
                listener.onDataUpdate(mcuData);
            }

        }

        @Override
        public void onScanResult(List<BLEScannedDevice> devices, BLEDeviceStates state) {

            for (OnCipedTronicDeviceListener listener:_Listeners
                 ) {
                listener.onScanResult(devices,"");
            }

        }

        @Override
        public void onError(BLEDeviceErrors error) {

            for (OnCipedTronicDeviceListener listener:_Listeners
            ) {
                listener.onDeviceError(error.name());
            }
        }
    };

@Override
    public void finalize()
    {
        close();
    }


}
