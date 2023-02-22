#include <string>
#include "MSTimer.h"

enum class CipedStates_e:uint32_t
{
  None = 0x00000000,
  Running = 0x00000001,
  Error = 0x00000002,  
  LowBat = 0x00000004,
  Move = 0x00000008,
  LightOn = 0x000000010,
  AlarmActived = 0x000000020,
  AlarmActive = 0x000000040  
};

class Ciped
{
  private:
  uint32_t _PulsesPerRevolution;
  uint32_t _LastPulses;
  uint32_t _PulsesPerSecond;
  uint32_t _PulsesPerSecondMax;
  uint32_t _PulsesPerSecondAvg;
  uint32_t _LastTick;
  bool _Move;
  bool _Alarm;
  bool _AlarmActivated;
  bool _LightOn;
  bool _LowBattery;
  uint32_t _StandstillTick;
  int _SavingState;
   uint32_t _State;

  void saveTiming();
  void saveToEeprom();
  void loadFromEeprom();
  bool checkBattery();
  bool getBatteryState();
public:
  Ciped();
  ~Ciped(); 

  void setPulsesPerRevolution(uint32_t pulsesPerRevolution);
  uint32_t getPulses();
  uint32_t getPulsesPerSecond();
  uint32_t getPulsesPerSecondMax();
  uint32_t getPulsesPerSecondAvg();
  uint32_t getState();

  void clear();
  void activateAlarm(bool alarm);
  void resetAlarm();
  void setLight(bool light);
 
  void process();
 
};