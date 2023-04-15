package CipedTronic.products.cipedtronicapp.mcu.mcu;

import android.content.Context;

import CipedTronic.products.cipedtronicapp.mcu.BLE.BLEDevice;
import CipedTronic.products.cipedtronicapp.mcu.BLE.BLEScannedDevice;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Abstraction of the cipedTronic device
 * - Configuring, starts and controlling the BLE Service
 * - calculates the recieved data to readable values
 */
public class CipedTronicMCU extends  BLEDevice {

    public enum CipedStates
    {
        CipedStateNone,
        CipedStateRunning,
        CipedStateError,
        CipedStateLowBat,
        CipedStateMove,
        CipedStateLoadEnabled,
        CipedStateLoading,
        CipedStateAlarmEnabled,
        CipedStateAlarmActive,
        PowerSaveEnabled,
    }

    private static volatile CipedTronicMCU _Instance = null;

    public static CipedTronicMCU getInstance() {
        if (_Instance == null) {
            _Instance = new CipedTronicMCU();
        }
        return _Instance;
    }

    public interface OnCipedTronicDeviceListener {
        void onStatusChanged(String state);
        void onDeviceError(String error);
        void onDataUpdate(CipedtronicData data);
        void onGetConfiguration(CipedTronicConfiguration configuration);
        void onScanResult(List<BLEScannedDevice> devices, String state);
    }

    //Bluetooth
    public static final UUID CIPED_SERVICE_UUID = UUID.fromString("b1fb1816-f607-42a1-827d-f84ae6bdf20a");
    public static final UUID CIPED_MEASUREMENT_CHARACTER_UUID = UUID.fromString("b1fb0001-f607-42a1-827d-f84ae6bdf20a");
    public static final UUID CIPED_CONTROL_POINT_CHARACTER_UUID = UUID.fromString("b1fb0002-f607-42a1-827d-f84ae6bdf20a");
    public static final UUID CIPED_INFO_CHARACTER_UUID = UUID.fromString("b1fb0003-f607-42a1-827d-f84ae6bdf20a");


    private List<OnCipedTronicDeviceListener> _Listeners = new ArrayList<>();
    private CipedTronicControlPoint _CipedTronicControlPoint = new CipedTronicControlPoint(this, CIPED_CONTROL_POINT_CHARACTER_UUID);



    private CipedtronicData _McuData = new CipedtronicData();
    private CipedtronicData _LastMcuData = new CipedtronicData();
    private  CipedTronicConfiguration _CipedTronicConfiguration = new CipedTronicConfiguration();
    private int _LastState = 0;


    private static final double PI = 3.14159265358979323;
    private CipedTronicMCU() {
        super();
        addNotificationCharacteristic(CIPED_MEASUREMENT_CHARACTER_UUID);
        addIndicateNotificationCharacteristic(CIPED_CONTROL_POINT_CHARACTER_UUID);
        //addNotificationCharacteristic(CIPED_INFO_CHARACTER_UUID);
        setOnBLEDeviceListener(_OnBLEDeviceListener);
        _CipedTronicControlPoint.setOnCipedTronicControlPointListener(_OnCipedTronicControlPointListener);
    }

    public void setOnCipedTronicDeviceListener(OnCipedTronicDeviceListener listener) {
        _Listeners.add(listener);
        listener.onStatusChanged(getDeviceState());
    }

    CipedTronicControlPoint.OnCipedTronicControlPointListener _OnCipedTronicControlPointListener = new CipedTronicControlPoint.OnCipedTronicControlPointListener() {
        @Override
        public void onResponse(CipedTronicControlPoint.CipedControlPointOpCodes opCode, CipedTronicControlPoint.CipedControlPointResultCodes result, long parameter) {

        }

        @Override
        public void onError(CipedTronicControlPoint.CipedControlPointOpCodes opCode, CipedTronicControlPoint.CipedControlPointResultCodes result) {

        }

        @Override
        public void onReadFlags(int flags) {
            _CipedTronicConfiguration.setFlags(flags);
            for (OnCipedTronicDeviceListener listener:_Listeners) {
                listener.onGetConfiguration(_CipedTronicConfiguration);
            }
        }


        @Override
        public void onEmpty(){
            for (OnCipedTronicDeviceListener listener:_Listeners) {
                listener.onGetConfiguration(_CipedTronicConfiguration);
            }
        }

    };

    private void formatDeviceData(
            long pulses,
            long pulsesPerSecond,
            long pulsesPerSecondMax,
            long timeWasMoving
    ) {
        double PreCalcKm = ((double)2 * PI * (double)_CipedTronicConfiguration.WheelRadius) / (double)_CipedTronicConfiguration.PulsesPerRevolution/ 1000000;
        double distance =  PreCalcKm * (double)pulses ;
        double velocity = PreCalcKm * (double)pulsesPerSecond * 3600;
        double velocityMax = PreCalcKm * (double)pulsesPerSecondMax * 3600;;
        double velocityAvg = 0;


        Duration duration = Duration.ofSeconds((long)timeWasMoving);
        if(timeWasMoving != 0)
        {
            velocityAvg = distance / timeWasMoving * 3600;
        }

        _McuData.Velocity = String.format("%.2f", velocity);
        _McuData.MaxVelocity = String.format("%.2f", velocityMax);
        _McuData.AvgVelocity = String.format("%.2f",velocityAvg);
        _McuData.Distance = String.format("%.2f",distance);
        _McuData.TimeWasMoving = duration.toString();
        _McuData.Pulses = String.format("%d",pulses);
        _McuData.PulsesPerSecond = String.format("%d",pulsesPerSecond);
        _McuData.TimeWasMoving = duration.toString();

    }

    private void formatDeviceInfo(
            double BatteryVoltageMillvolt
    ) {
        _McuData.Vbat = String.format("%2f", BatteryVoltageMillvolt);

    }

    public void decodeCipedState(long state) {
        _McuData.StateRunning = false;
        _McuData.StateError = false;
        _McuData.StateLowBat = false;
        _McuData.StateMove = false;
        _McuData.StateLoadEnabled = false;
        _McuData.StateLoading = false;
        _McuData.StateAlarmEnabled = false;
        _McuData.StateAlarmActive = false;

        _McuData.StateRunning = getStateBit(state,CipedStates.CipedStateRunning);
        _McuData.StateError =  getStateBit(state,CipedStates.CipedStateError);
        _McuData.StateLowBat =  getStateBit(state,CipedStates.CipedStateLowBat);
        _McuData.StateMove = getStateBit(state,CipedStates.CipedStateMove);
        _McuData.StateLoadEnabled = getStateBit(state,CipedStates.CipedStateLoadEnabled);
        _McuData.StateLoading = getStateBit(state,CipedStates.CipedStateLoading);
        _McuData.StateAlarmEnabled = getStateBit(state,CipedStates.CipedStateAlarmEnabled);
        _McuData.StateAlarmActive = getStateBit(state,CipedStates.CipedStateAlarmActive);
        _McuData.StatePowerSaveEnabled = getStateBit(state,CipedStates.PowerSaveEnabled);

    }


    public CipedtronicData getData() {
        return _McuData;
    }
    public void setAlarmEnable(boolean on) {
        _CipedTronicControlPoint.setAlarmEnable(on);
    }
    public void resetCounters() {
        _CipedTronicControlPoint.resetCounter();
    }
    public void setConfiguration(CipedTronicConfiguration configuration,boolean download) {
        _CipedTronicConfiguration = configuration;
        if(download) {
            _CipedTronicControlPoint.writeFlags(_CipedTronicConfiguration.getFlags());
        }
    }

    public void getConfiguration()
    {
        _CipedTronicControlPoint.readFlags();
    }

    public void createDevice(Context context, String address) {

        createDevice(context, address, "CIPED", CIPED_SERVICE_UUID, true);
    }

    private boolean getStateBit(long value,CipedStates state)
    {
        if((value & (1 << state.ordinal())) > 0 )
        {
            return true;
        }
        return false;
    }
    BLEDevice.OnBLEDeviceListener _OnBLEDeviceListener = new OnBLEDeviceListener() {
        @Override
        public void OnStatusChanged(BLEDeviceStates state) {
            for (OnCipedTronicDeviceListener listener:_Listeners) {
                listener.onStatusChanged(state.name());
            }
        }

        @Override
        public void onCharacteristicRead(UUID characteristicUUID, byte[] value) {

        }

        @Override
        public void onDescriptionRead(UUID characteristicUUID, UUID descriptionUUID, byte[] value) {

        }

        @Override
        public void onCharacteristicNotification(UUID characteristicUUID, byte[] value) {

            if (characteristicUUID.compareTo(CIPED_MEASUREMENT_CHARACTER_UUID) == 0) {
                ByteBuffer buffer = ByteBuffer.allocate(20);//   .wrap(value);
                buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
                if (value.length != 20) {
                    return;
                }
                buffer.put(value);
                formatDeviceData(
                        (long) (buffer.getInt(0) & 0xFFFFFFFFL),
                        (long) (buffer.getInt(4) & 0xFFFFFFFFL),
                        (long)(buffer.getInt(8) & 0xFFFFFFFFL),
                        (long) (buffer.getInt(12) & 0xFFFFFFFFL)
                );

                decodeCipedState((buffer.getInt(16) & 0xFFFFFFFFL));
                if(!_McuData.cmp(_LastMcuData)) {
                    for (OnCipedTronicDeviceListener listener : _Listeners) {
                        listener.onDataUpdate(_McuData);
                    }
                }
                _LastMcuData.set(_McuData);
            } else if (characteristicUUID.compareTo(CIPED_CONTROL_POINT_CHARACTER_UUID) == 0) {
                _CipedTronicControlPoint.processResults(value);

            } else if (characteristicUUID.compareTo(CIPED_INFO_CHARACTER_UUID) == 0) {
                ByteBuffer buffer = ByteBuffer.allocate(4);//   .wrap(value);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                if (value.length != 4) {
                    return;
                }
                buffer.put(value);
                formatDeviceInfo(
                        (double)(buffer.getFloat(0))
                );
                for (OnCipedTronicDeviceListener listener:_Listeners) {
                    listener.onDataUpdate(_McuData);
                }
            } else
                return;
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
