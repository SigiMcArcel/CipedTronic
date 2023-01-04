package com.example.cipedtronicapp.mcu.mcu;

import com.example.cipedtronicapp.mcu.BLE.BLEUart;

public class DeviceProtocol {

    public interface OnDeviceProtcolListener {
        public void OnErrorReceived(long error);
        public void OnDataReceived(CipedTronicProtokollPackage message);
        public void OnConnectedStateChange(String state);
    }

    /*Registers 32Bit
        0 = Version
        1 = State
        2 = Event Register Number
        3 = Event Register Lenght
        4 Event Register Intervall
        5 = Reset Count
        10 = Id first 4 letters
        11 = id last 4 Letters
        30 = Velocity
        31 = Velocity max
        32 = Velocity Average
        33 = Distance
     */

    //Commands
    final String CMD_DR = "DR";      // Data Read. Device to phone       : <CIP [ID] [CMD] [Register Number] [Lengh]>
    final String CMD_DW = "DW";      // Data Write. Phone to device     : <CIP [ID] [CMD] [Register Number] [Lengh] [Data] ....>
    final String CMD_ST = "ST";      // Start data transmission		: <CIP [ID] [CMD] [Register number] [Lenght] [Intervall ms]>
    final String CMD_SP = "SP";      // Stop data transmission		    : <CIP [ID] [CMD]>
    final String CMD_SK = "SK";      // Set Encryption Key

    //Events:
    final String EVENT_DE = "DE";     // Data Event		            : <CIP [ID] [Event] [Register start Number] [Lenght] [data] .. (for CipedTronic) [Velocity] [Velocity max] [Velocity Average] [Distance] >
     final String EVENT_SE = "SE";      // State Event				: <CIP [ID] [Event] [CMD] [Error]

    private OnDeviceProtcolListener _Listener;
    String _RxBuffer = "";
    boolean _BufferSync = false;
    String _EncryptionKey = "";

    private BLEUart _BLEUart;

    //Constructor
    public DeviceProtocol(BLEUart uart)
    {
        uart.setOnReceiveListener(_UartListener);
    }

    private boolean waitMessageComplete() {
        if (_RxBuffer.endsWith(">") && _RxBuffer.startsWith("<CIP")) {
            return true;
        }
        return false;
    }

    public void setOnDeviceProtcolListener(OnDeviceProtcolListener listener)
    {
        this._Listener = listener;
    }
    public String CreateCommand(String command, String id, String[] data) {
        String dataString = "";
        for (int i = 0; i < data.length; i++) {
            dataString += data[i];
            if (i < data.length - 1) {
                dataString += " ";
            }
        }
        return String.format("<CIP %s %s %s>", id, command, dataString);
    }



    public String CreateSetEncryptionKey(String id, String oldKey, String newkey) {
        String[] data = {oldKey, newkey};
        return CreateCommand(CMD_SK, id, data);
    }

    public String[] CreateDataRequest(long registerNumber ,int lenght,long[] data) {
        String[] dataAsString = new String[2 + lenght];
        dataAsString[0] = String.format("%8X",registerNumber);
        dataAsString[1] = String.format("%8X",lenght);
        for(int i = 0;i< lenght;i++)
        {
            dataAsString[i + 2] = String.format("%8X",data[i]);
        }
        return dataAsString;
    }

    public String createDataRead(String id,long registerNumber ,int lenght,long[] data) {
        return CreateCommand(CMD_DR, id, CreateDataRequest(registerNumber,lenght,data));
    }

    public String createDataWrite(String id,long registerNumber ,int lenght,long[] data) {
        return CreateCommand(CMD_DW, id, CreateDataRequest(registerNumber,lenght,data));
    }

    public String createStartTransmition(String id, int registerNumber, int lenght, int intervallMs) {
        String[] data = {String.format("%8X",registerNumber), String.format("%8X",lenght), String.format("%8X",intervallMs)};
        return CreateCommand(CMD_ST, id, data);
    }

    public String createStopTransmition(String id, String registerNumber) {
        String[] data = {String.format("%8X",registerNumber)};
        return CreateCommand(CMD_SP, id, data);
    }

    public void dataRead(String id,long registerNumber ,int lenght,long[] data)
    {
        String p = createDataRead(id,registerNumber,lenght,data);
        _BLEUart.write(p.getBytes());

    }

    public void dataWrite(String id,long registerNumber ,int lenght,long[] data)
    {
        String p = createDataWrite(id,registerNumber,lenght,data);
        _BLEUart.write(p.getBytes());
    }

    public void startTransmition(String id, int registerNumber, int lenght, int intervallMs) {

        String p = createStartTransmition(id,registerNumber,lenght,intervallMs);
        _BLEUart.write(p.getBytes());
    }

    public void stopTransmition(String id, String registerNumber) {
        String p = createStopTransmition(id,registerNumber);
        _BLEUart.write(p.getBytes());
    }

    CipedTronicProtokollPackage ProcessDataEvent(String[] message) {
        CipedTronicProtokollPackage pack = new CipedTronicProtokollPackage();
        pack._Valid = true;
        pack._Id = message[0];
        pack._Cmd = message[1];
        try {
            pack._RegisterStartNumber = Long.parseUnsignedLong(message[2], 16);
            pack._DataLenght = Long.parseUnsignedLong(message[3], 16);
        }
        catch (Exception exp)
        {
            pack._Valid = false;
            return pack;
        }
        if(message.length != pack._DataLenght - 3)
        {
            pack._Valid = false;
            return pack;
        }
        int count  = 0;
        for(int i = 4;i < message.length;i++)
        {
            try {
                pack._Data[count]  = Long.parseUnsignedLong(message[i], 16);
            }
            catch (Exception exp)
            {
                pack._Valid = false;
            }
        }
        return pack;
    }

    CipedTronicProtokollPackage ProcessStateEvent(String[] message) {
        CipedTronicProtokollPackage pack = new CipedTronicProtokollPackage();
        pack._Valid = true;
        pack._Id = message[0];
        pack._Cmd = message[1];
        try {
            pack._Data[0] = Long.parseUnsignedLong(message[2], 16);
            pack._Data[1] = Long.parseUnsignedLong(message[3], 16);
        }
        catch (Exception exp)
        {
            pack._Valid = false;
        }

        return pack;
    }

    CipedTronicProtokollPackage processEvents(String[] message) {
        CipedTronicProtokollPackage result;
        switch (message[1]) {
            case EVENT_DE: {
                result = ProcessDataEvent(message);
                if(result._Valid) {
                    _Listener.OnDataReceived(result);
                }
                else
                {
                    if(_Listener != null) {
                        _Listener.OnErrorReceived(0x10000000);
                    }
                }
                break;
            }
            case EVENT_SE: {
                result = ProcessStateEvent(message);
                if(result._Valid && result._Data[3] > 0)
                {
                    if(_Listener != null) {
                        _Listener.OnErrorReceived(result._Data[3]);
                    }
                }
                break;
            }

            default: {
                result= new CipedTronicProtokollPackage();
                result._Valid = false;
            }
            break;
        }
        return result;
    }


    boolean parse(byte[] data) {

        if (waitMessageComplete()) {
            String formatted = _RxBuffer.replace("<CIP", "").replace(">", "");
            String[] tmp = formatted.split("\\s");
            _RxBuffer = "";
        }
        return true;
    }

    //Event
    public BLEUart.OnUartListener _UartListener = new BLEUart.OnUartListener() {
        @Override
        public void onReceive(byte[] data) {
            parse(data);
        }

        @Override
        public void onStatusChanged(String state) {
            if(_Listener != null) {
                _Listener.OnConnectedStateChange(state);
            }
        }
    };
}
