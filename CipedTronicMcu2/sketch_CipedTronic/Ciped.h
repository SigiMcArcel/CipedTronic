#ifndef CIPEDTRONIC_CIPED_H_
#define CIPEDTRONIC_CIPED_H_
#include <string>
#include "Timer.h"
#include "Adafruit_VL53L0X.h"

namespace CipedTronic
{
	enum class CipedStates_e:uint32_t
	{
	  None = 0x00000000,
	  Running = 0x00000001,
	  Error = 0x00000002,  
	  LowBat = 0x00000004,
	  Move = 0x00000008,
	  LoadEnabled = 0x000000010,
	  AlarmEnabled = 0x000000020,
	  AlarmActive = 0x000000040  
	};

	class Ciped : public TimerCallback
	{
	  private:
	  uint32_t _PulsesPerRevolution;
	  uint32_t _LastPulses;
	  uint32_t _PulsesPerSecond;
	  uint32_t _LastPulsesPerSecond;
	  uint32_t _PulsesPerSecondMax;
	  uint32_t _PulsesPerSecondAvg;
	  uint32_t _LastTick;

      //Control
      bool _AlarmEnable;
	  bool _LoadEnable;
    
      //States
	  bool _Move;
	  bool _Alarm;
	  bool _Loading;
	  bool _LowBattery;
	  
      //data
      uint32_t _State;

      //intern    
      Timer _ProcessTimer;
      Adafruit_VL53L0X _Lox;
      int _SavingState;
     
      const uint32_t _VelocityForLoad = 60; 

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
      uint16_t getRadius();

	  void clear();
	  void activateAlarm(bool alarm);
	  void resetAlarm();
	  void setLoadEnable(bool enable);	 

	  void process();

    //Implements
    void TimerElapsed(int32_t id);
	 
	};
} //namespace CipedTronic
#endif //CIPEDTRONIC_CIPED_H_