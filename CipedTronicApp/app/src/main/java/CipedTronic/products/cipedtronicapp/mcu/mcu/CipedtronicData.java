package CipedTronic.products.cipedtronicapp.mcu.mcu;

public class CipedtronicData {
    public String Velocity = "";
    public String MaxVelocity = "";
    public String AvgVelocity = "";
    public String Distance = "";
    public String Pulses = "";
    public String PulsesPerSecond = "";
    public String Radius = "";
    public String TimeWasMoving = "";
    public String Vbat = "";

    public boolean StateRunning = false;
    public boolean StateError = false;
    public boolean StateLowBat = false;
    public boolean StateMove = false;
    public boolean StateLoadEnabled = false;
    public boolean StateLoading = false;
    public boolean StateAlarmEnabled = false;
    public boolean StateAlarmActive = false;
    public boolean StatePowerSaveEnabled = false;

    void set (CipedtronicData other) {
        Velocity = other.Velocity;
        MaxVelocity = other.MaxVelocity;
        AvgVelocity = other.AvgVelocity;
        Distance = other.Distance;
        Pulses = other.Pulses;
        PulsesPerSecond = other.PulsesPerSecond;
        Radius = other.Radius;
        TimeWasMoving = other.TimeWasMoving;
        Vbat = other.Vbat;

        StateRunning = other.StateRunning;
        StateError = other.StateError;
        StateLowBat = other.StateLowBat;
        StateMove = other.StateMove;
        StateLoadEnabled = other.StateLoadEnabled;
        StateLoading = other.StateLoading;
        StateAlarmEnabled = other.StateAlarmEnabled;
        StateAlarmActive = other.StateAlarmActive;
        StatePowerSaveEnabled = other.StatePowerSaveEnabled;
    }
    boolean cmp (CipedtronicData other) {

        if (null == other) return false;

        if (!Velocity.equals(other.Velocity)) {return false;}
        if (!MaxVelocity.equals(other.MaxVelocity)) {return false;}
        if (!AvgVelocity.equals(other.AvgVelocity)) {return false;}
        if (!Distance.equals(other.Distance)) {return false;}
        if (!Pulses.equals(other.Pulses)) {return false;}
        if (!PulsesPerSecond.equals(other.PulsesPerSecond)) {return false;}
        if (!Radius.equals(other.Radius)) {return false;}
        if (!TimeWasMoving.equals(other.TimeWasMoving)) {return false;}
        if (!Vbat .equals(other.Vbat)) {return false;}
        if (StateRunning != other.StateRunning) {return false;}
        if (StateError != other.StateError) {return false;}
        if (StateLowBat != other.StateLowBat) {return false;}
        if (StateLoadEnabled != other.StateLoadEnabled) {return false;}
        if (StateLoading != other.StateLoading) {return false;}
        if (StateAlarmEnabled != other.StateAlarmEnabled) {return false;}
        if (StateAlarmActive != other.StateAlarmActive) {return false;}
        if (StatePowerSaveEnabled != other.StatePowerSaveEnabled) {return false;}

        return true;

    }
}
