package com.example.cipedtronicapp;

public class CipedTronicDevice {
    public String Name;
    public String Address;

    @Override
    public String toString()
    {
        return Address + " " + Name;
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof CipedTronicDevice) {
            CipedTronicDevice s = (CipedTronicDevice) object;
            return this.Address.equals(s.Address);
        }
        return false;
    }

    public String getAddress()
    {
        return Address;
    }

    @Override
    public int hashCode() {
        return Address.hashCode();
    }
}
