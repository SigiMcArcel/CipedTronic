package CipedTronic.products.cipedtronicapp.mcu.mcu;

public class CipedTronicConfiguration {

    public enum  CipedControlPointFlags
    {
        CipedFlagNone,
        CipedFlagEnableLoad,
        CipedFlagPowerSave,
    }

    public boolean BatteryLoadEnable = false;
    public boolean PowerSave = false;
    public int PulsesPerRevolution = 0;
    public int WheelRadius = 0;

    private int _Flags = 0;

    public CipedTronicConfiguration(int flags)
    {
        _Flags = flags;
        setFlags(_Flags);
    }
    public CipedTronicConfiguration()
    {
    }

    private void clearFlags()
    {
        _Flags = 0;
    }
    private void setFlag(CipedControlPointFlags flag,boolean set)
    {
        if(set)
        {
            _Flags = _Flags | (1 << flag.ordinal());
        }
        else
        {
            _Flags = _Flags & ~(1 << flag.ordinal());
        }
    }
    private boolean getFlag(CipedControlPointFlags flag)
    {
        if((_Flags & (1 << flag.ordinal())) > 0 )
        {
            return true;
        }
        return false;
    }
    public int getFlags()
    {
        clearFlags();
        setFlag(CipedControlPointFlags.CipedFlagEnableLoad,BatteryLoadEnable);
        setFlag(CipedControlPointFlags.CipedFlagPowerSave,PowerSave);
        return _Flags;
    }

    public void setFlags(int flags)
    {
        _Flags = flags;
        BatteryLoadEnable = getFlag(CipedControlPointFlags.CipedFlagEnableLoad);
        PowerSave = getFlag(CipedControlPointFlags.CipedFlagPowerSave);
    }

}
