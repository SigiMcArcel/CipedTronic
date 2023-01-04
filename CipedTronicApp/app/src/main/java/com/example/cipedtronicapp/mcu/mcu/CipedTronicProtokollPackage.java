package com.example.cipedtronicapp.mcu.mcu;

public class CipedTronicProtokollPackage{
    public final int MAX_DATA_LENGHT = 5;
    public  String _Id = "";
    public String _Cmd = "";
    public long _RegisterStartNumber;
    public long _DataLenght;
    public long _Data[] = new long[MAX_DATA_LENGHT];
    public boolean _Valid = false;
}
