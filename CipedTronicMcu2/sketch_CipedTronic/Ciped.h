#ifndef CIPEDTRONIC_CIPED_H_
#define CIPEDTRONIC_CIPED_H_
#include <string>
#include "Timer.h"
#include "CipedService.h"
#include "PowerManagement.h"
#include <Preferences.h>

namespace CipedTronic
{
  //to do: mask or bit number for all 
	enum class CipedStates_e:uint32_t
	{
	  None,
	  Running,
	  Error,  
	  LowBat,
	  Move,
	  LoadEnabled,
	  Loading,
	  AlarmEnabled,
	  AlarmActive,
    PowerSaveEnabled,
	};

  enum class CipedFlags_e:uint32_t
	{
	  None,
	  EnableLoad,  
	  PowerSave
	};

typedef struct  CipedData_t
{
    uint32_t _Pulses;
    uint32_t _TotalPulses;
    uint32_t _PulsesPerSecondMax;
    uint32_t _TimeWasMoving;
    uint32_t _Flags;
} CipedData;

class CipedEvents
{
  public:
  virtual void BevorSaveData();
};

	class Ciped : public TimerCallback
	{
	  private:
    uint32_t _TotalPulses;
	  uint32_t _LastPulses;
	  uint32_t _PulsesPerSecond;
	  uint32_t _LastPulsesPerSecond;
	  uint32_t _PulsesPerSecondMax;
    uint32_t _TimeWasMoving;
  
    //Control
    bool _AlarmEnable;
	  bool _LoadEnable;
    bool _PowerSave;
  
    //States
    bool _Running;
    bool _Error;
    bool _LowBattery;
	  bool _Move;
    bool _Loading;
	  bool _AlarmEnabled;
    bool _AlarmActiv;
    bool _LoadEnabled;
	  bool _OperatingSpeedReached;
    bool _PowerSaveEnabled;
    

	  
    //data
    uint32_t _State;
    uint32_t _Flags;
    CipedBikeData _CipedBikeData;
    CipedInfo _CipedInfo;
    
    //intern    
    Timer _ProcessTimer;
    PowerManagement _PowerManagement;
    int _SavingState;
    bool _FirstCycle;
    Preferences _Preferences; 
  

    
    //const
    const uint32_t _OperatingSpeedVelocity = 10; 
   

      //functions
	  void saveTiming();
	  void saveToEeprom();
	  void loadFromEeprom();
	  bool checkBattery();
	  bool getBatteryState();
    void calculateBikeData();    
    bool getBitOf(uint32_t bit,uint32_t value);
    uint32_t setBitOf(uint32_t bit,uint32_t value,bool set);
    void decodeFlags();
    void encodeStates();
	public:
	  Ciped();
	  ~Ciped(); 

    void begin();
    void setFlags(uint32_t flags);
    uint32_t getFlags();
    void getCipedBikeData(CipedBikeData* data);    
    void getCipedBikeInfo(CipedInfo* data);
   

	  void clear();
    void enableAlarm(bool set);

	  void process();
    void TimerMain();

    //Implements
    void TimerElapsed(int32_t id);
	 
	};
} //namespace CipedTronic
#endif //CIPEDTRONIC_CIPED_H_