package CipedTronic.products.cipedtronicapp.mcu.mcu;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import CipedTronic.products.cipedtronicapp.mcu.BLE.BLEDevice;

public class CipedTronicControlPoint {
    public enum  CipedControlPointOpCodes
    {
        None,
        SetFlags,
        GetFlags,
        ResetCounter,
        ResetAlarm,
        AlarmEnable,
        Max
    };

    public enum  CipedControlPointResultCodes
    {
        Reserved,
        Success,
        NotSupported,
        InvalidParameter,
        OperationFailed
    };




    public interface OnCipedTronicControlPointListener {
        public void onResponse(CipedControlPointOpCodes opCode,CipedControlPointResultCodes result, long parameter);
        void onError(CipedControlPointOpCodes opCode,CipedControlPointResultCodes result);
        void onReadFlags(int flags);
        void onEmpty();
    }

    BLEDevice _Device = null;
    UUID _UUID = null;
    LinkedList<CipedControlPointOpCodes> PendingRequests = new LinkedList<>() ;


    private OnCipedTronicControlPointListener _Listener = null;

    public CipedTronicControlPoint(BLEDevice device,UUID uuid)
    {
        _Device = device;
        _UUID = uuid;
    }

   void setOnCipedTronicControlPointListener(OnCipedTronicControlPointListener listener)
   {
       _Listener = listener;
   }

   public void  readConfiguration()
   {
       readFlags();
   }



    public void resetCounter()
    {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.clear();
        buffer.put((byte)CipedControlPointOpCodes.ResetCounter.ordinal());
        buffer.put((byte)0);
        buffer.putInt(0);
        PendingRequests.add(CipedControlPointOpCodes.ResetCounter);
        _Device.writeCharacteristic(_UUID,buffer.array());
    }

    public void writeFlags(int flags)
    {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.clear();
        buffer.put((byte)CipedControlPointOpCodes.SetFlags.ordinal());
        buffer.put((byte)0);
        buffer.putInt(flags);
        PendingRequests.add(CipedControlPointOpCodes.SetFlags);
        _Device.writeCharacteristic(_UUID,buffer.array());
    }
    public void readFlags()
    {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.clear();
        buffer.put((byte)CipedControlPointOpCodes.GetFlags.ordinal());
        buffer.put((byte)0);
        buffer.putInt(0);
        PendingRequests.add(CipedControlPointOpCodes.GetFlags);
        _Device.writeCharacteristic(_UUID,buffer.array());
    }

    public void resetAlarm()
    {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.clear();
        buffer.put((byte)CipedControlPointOpCodes.ResetAlarm.ordinal());
        buffer.put((byte)0);
        buffer.putInt(0);
        PendingRequests.add(CipedControlPointOpCodes.ResetAlarm);
        _Device.writeCharacteristic(_UUID,buffer.array());
    }

    public void setAlarmEnable(boolean on) {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.clear();
        buffer.put((byte)CipedControlPointOpCodes.AlarmEnable.ordinal());
        buffer.put((byte)0);
        if(on)
        {
            buffer.putInt(1);
        }
        else {
            buffer.putInt(0);
        }
        PendingRequests.add(CipedControlPointOpCodes.AlarmEnable);
        _Device.writeCharacteristic(_UUID,buffer.array());
    }

    public void processResults(byte[] data)
    {
        Log.d("OnCipedTronicControlPointListener","processResults ");
        int parameter = 0;
        ByteBuffer buffer = ByteBuffer.allocate(6);//   .wrap(value);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        if(PendingRequests.isEmpty())
        {
            _Listener.onError(CipedControlPointOpCodes.None,CipedControlPointResultCodes.OperationFailed);
            return;
        }

        if (data.length != 6) {
            if(_Listener != null) {
                _Listener.onError(CipedControlPointOpCodes.None,CipedControlPointResultCodes.OperationFailed);
                return;
            }
        }
        buffer.put(data);
        CipedTronicControlPoint.CipedControlPointResultCodes OpResultCode = CipedTronicControlPoint.CipedControlPointResultCodes.values()[data[1]];
        CipedTronicControlPoint.CipedControlPointOpCodes OpRequestCode = CipedTronicControlPoint.CipedControlPointOpCodes.values()[data[0]];
        parameter = buffer.getInt(2);

        if(OpResultCode != CipedTronicControlPoint.CipedControlPointResultCodes.Success)
        {

            if(_Listener != null) {
                _Listener.onError(OpRequestCode, OpResultCode);
            }
            Log.e("OnCipedTronicControlPointListener",OpRequestCode.name());
            return;
        }
        if(PendingRequests.contains(OpRequestCode)) {
            PendingRequests.remove(OpRequestCode);
            Log.d("OnCipedTronicControlPointListener","PendingRequests.remove " + OpRequestCode.name());
        }


        switch(OpRequestCode)
        {
            case ResetCounter:
            {
                break;
            }
            case AlarmEnable:
            {
                break;
            }
            case ResetAlarm:
            {
                break;
            }
            case SetFlags:
            {
                break;
            }
            case GetFlags:
            {
                if(_Listener != null) {
                    _Listener.onReadFlags(parameter);
                }
                break;
            }
            default:
            {
                if(_Listener != null) {
                    _Listener.onError(PendingRequests.poll(), OpResultCode);
                }
                Log.e("OnCipedTronicControlPointListener",CipedTronicControlPoint.CipedControlPointResultCodes.OperationFailed.name());
                break;
            }
        }
        if(PendingRequests.isEmpty())
        {
            if(_Listener != null) {
                _Listener.onEmpty();
            }
        }
    }
}
