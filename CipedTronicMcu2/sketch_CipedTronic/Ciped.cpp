#include <Arduino.h>
#include "esp32-hal-gpio.h"
#include "Ciped.h"


#define EEPROM_OFFSET_TOTALPULSES 0
#define EEPROM_OFFSET_PULSESPERSECONDMAX 4
#define EEPROM_OFFSET_TIMEWASMOVING 8
#define EEPROM_OFFSET_FLAGS 12
#define EEPROM_OFFSET_PULSESPERREVOLUTION 16

static RTC_DATA_ATTR CipedTronic::CipedData _SaveData;

static uint32_t _Pulses = 0;
static void IRAM_ATTR isr() {
   _Pulses++;
}


#define GPIO_IN_PULSES D1
#define GPIO_OUT_ENABLELOAD D2
#define GPIO_BATTERY_LOAD A0

#define PROCESSCYCLETIME_MS 1000
#define SENSORCYCLETIME_MS 10
#define SAVE_EEPROM_TIME_MS 15000


CipedTronic::Ciped::Ciped()
:_TotalPulses(0)
,_LastPulses(0)
,_PulsesPerSecond(0)
,_LastPulsesPerSecond(0)
,_PulsesPerSecondMax(0)
,_TimeWasMoving(false)
,_AlarmEnable(false)
,_LoadEnable(false)
,_PowerSave(false)
,_Running(false)
,_Error(false)
,_LowBattery(false)
,_Move(false)
,_Loading(false)
,_AlarmEnabled(false)
,_AlarmActiv(false)
,_LoadEnabled(false)
,_OperatingSpeedReached(false)
,_PowerSaveEnabled(false)
,_State(0)
,_Flags(0)
,_CipedBikeData()
,_CipedInfo()
,_ProcessTimer(PROCESSCYCLETIME_MS,PROCESSCYCLETIME_MS,this)
,_PowerManagement(3,1,NULL)
,_SavingState(0)
,_FirstCycle(false)
,_Preferences()
{
  
}

CipedTronic::Ciped::~Ciped ()
{
 _ProcessTimer.stop();
}

void CipedTronic::Ciped::begin()
{
  //Pulses
  loadFromEeprom();

  pinMode(GPIO_IN_PULSES, INPUT);
  attachInterrupt(GPIO_IN_PULSES, isr, RISING);
  //Load Enable
  pinMode(GPIO_OUT_ENABLELOAD, OUTPUT);
  digitalWrite(GPIO_OUT_ENABLELOAD,LOW);
  //Battery measurement
  pinMode(GPIO_BATTERY_LOAD, INPUT);
  
  
  decodeFlags();
  _ProcessTimer.start();  
  
}

void CipedTronic::Ciped::setFlags(uint32_t flags)
{
    _Flags = flags;
    /*_Preferences.begin("da",false);
     if(_Preferences.putLong("fl",_Flags) == 0)
    {
      Serial.printf("error write eeprom _Flags\r\n");
    }
     _Preferences.end();*/
     _SaveData._Flags = _Flags;    
     
}

uint32_t CipedTronic::Ciped::getFlags()
{
  return _Flags;
}

void CipedTronic::Ciped::getCipedBikeData(CipedBikeData* data)
{
    memcpy(data,&_CipedBikeData,sizeof(CipedBikeData));
}

void CipedTronic::Ciped::getCipedBikeInfo(CipedInfo* data)
{
    memcpy(data,&_CipedInfo,sizeof(CipedInfo));
}

void CipedTronic::Ciped::enableAlarm(bool set)
{
   _AlarmEnabled = set;
   if(!set)
   {
      _AlarmActiv = false;
   }

}


void CipedTronic::Ciped::clear()
{
  _TotalPulses = 0;
  _Pulses = 0;
  _LastPulses = 0;
  _PulsesPerSecond = 0;
  _PulsesPerSecondMax = 0;
  _TimeWasMoving = 0;
  saveToEeprom();
}

//privates
bool CipedTronic::Ciped::getBitOf(uint32_t bit,uint32_t value)
{
  if((value & (1<<bit)) > 0)
  {
    return true;
  }  
  return false;  
}

uint32_t CipedTronic::Ciped::setBitOf(uint32_t bit,uint32_t value,bool set)
{
  uint32_t tmp = 0;
   if(set)
   {
      return value | (1<<bit);
   }
   return value & ~(1<<bit);
}

void CipedTronic::Ciped::saveToEeprom()
{
 /* Serial.printf("write eeprom  %d %d %d %d \r\n"
    ,_TotalPulses
    ,_PulsesPerSecondMax
    ,_Flags
    ,_PulsesPerRevolution);
    _Preferences.begin("da",false);
    if(_Preferences.putLong("tp",_TotalPulses) == 0)
    {
      Serial.printf("error write eeprom\r\n");
    }
    if(_Preferences.putLong("ps",_PulsesPerSecondMax) == 0)
    {
      Serial.printf("error write eeprom\r\n");
    }
    if(_Preferences.putLong("tm",_TimeWasMoving) == 0)
    {
      Serial.printf("error write eeprom\r\n");
    }
    _Preferences.end();*/
    _SaveData._Pulses = _Pulses;    
    _SaveData._TotalPulses = _TotalPulses;
	  _SaveData._PulsesPerSecondMax = _PulsesPerSecondMax;
    _SaveData._TimeWasMoving = _TimeWasMoving;
    _SaveData._Flags = _Flags;    
}


void CipedTronic::Ciped::loadFromEeprom()
{
  /*_Preferences.begin("da",false);
    if(_Preferences.getULong("te",0) != 0xaa55aa55)
    {
        _Preferences.clear();
        _Preferences.putULong("te",0xaa55aa55);
         _Preferences.end();
         Serial.printf("Clear eeprom\r\n");
        return;
    }
    
   _LastPulses =_TotalPulses = _Preferences.getULong("tp",0);
   _PulsesPerSecondMax = _Preferences.getULong("ps",0);
   _TimeWasMoving = _Preferences.getULong("tm",0);
   _Flags = _Preferences.getULong("fl",0);
   _PulsesPerRevolution = _Preferences.getULong("pr",0);
    Serial.printf("test eeprom %x %d %d %d %d \r\n"
    ,_Preferences.getULong("te",0)
    ,_TotalPulses
    ,_PulsesPerSecondMax
    ,_Flags
    ,_PulsesPerRevolution
    );
    _Preferences.end();*/
    _Pulses = _SaveData._Pulses;
  
    _TotalPulses =  _SaveData._TotalPulses;
	  _PulsesPerSecondMax =  _SaveData._PulsesPerSecondMax;
    _TimeWasMoving =  _SaveData._TimeWasMoving;
    _Flags =  _SaveData._Flags;      
   
}

//save pulses 15 second  after the bike stand still or battery is low
 void CipedTronic::Ciped::saveTiming()
 {
    switch(_SavingState)
    {
      case 0:
      {
        if(_Move)
        {
           _SavingState = 10;
           break;
        }  
        break;      
        case 10:
        {
           if(!_Move)
           {
              _SavingState = 20;
              break;             
           }
           break;
        }          
        case 20:
        {
          if(_Move)
          {
              _SavingState = 10;
              _ProcessTimer.timeOutReset();
              break;              
          }
           if(_ProcessTimer.timeOut(SAVE_EEPROM_TIME_MS))
           {
              _ProcessTimer.timeOutReset();
              Serial.printf("SAVE\r\n");
              saveToEeprom();
              if(_PowerSave)
              {
                _PowerManagement.start();
              }
             
              _SavingState = 0;
               
              break; 
           }
           break;
        }
      }      
    }
 }

bool CipedTronic::Ciped::checkBattery()
{
  uint32_t Vbatt = 0;
  for(int i = 0; i < 16; i++) {
    Vbatt = Vbatt + analogReadMilliVolts(GPIO_BATTERY_LOAD); // ADC with correction   
  }
  float Vbattf = 2 * Vbatt / 16 / 1000.0; 
  _CipedInfo.VBatt = Vbattf;
  if(Vbattf <= 3.4)
  {
    return true;    
  } 
  return false; 
}

 void CipedTronic::Ciped::decodeFlags()
 {
	  _LoadEnable = getBitOf((uint32_t)CipedTronic::CipedFlags_e::EnableLoad,_Flags);
    _PowerSave = getBitOf((uint32_t)CipedTronic::CipedFlags_e::PowerSave,_Flags);
 }
 
 void CipedTronic::Ciped::encodeStates()
 {
  _State = setBitOf((uint32_t)CipedTronic::CipedStates_e::Running,_State,_Running);
	_State = setBitOf((uint32_t)CipedTronic::CipedStates_e::Error,_State,_Error);
	_State = setBitOf((uint32_t)CipedTronic::CipedStates_e::LowBat,_State,_LowBattery);
	_State = setBitOf((uint32_t)CipedTronic::CipedStates_e::Move,_State,_Move);
	_State = setBitOf((uint32_t)CipedTronic::CipedStates_e::Loading,_State,_Loading);
	_State = setBitOf((uint32_t)CipedTronic::CipedStates_e::AlarmEnabled,_State,_AlarmEnabled);
	_State = setBitOf((uint32_t)CipedTronic::CipedStates_e::AlarmActive,_State,_AlarmActiv);
	_State = setBitOf((uint32_t)CipedTronic::CipedStates_e::LoadEnabled,_State,_LoadEnabled);
  _State = setBitOf((uint32_t)CipedTronic::CipedStates_e::PowerSaveEnabled,_State,_PowerSave);
 }
    
void CipedTronic::Ciped::calculateBikeData()
{
  _CipedBikeData.Pulses = _TotalPulses;
  _CipedBikeData.PulsesPerSecond = _PulsesPerSecond;
  _CipedBikeData.PulsesPerSecondMax = _PulsesPerSecondMax;
  _CipedBikeData.TimeMoving = _TimeWasMoving;
  _CipedBikeData.State = _State;
}

void CipedTronic::Ciped::process()
{
  _ProcessTimer.process();
}

void CipedTronic::Ciped::TimerMain()
{
  uint32_t pulsesTmp = _TotalPulses;
  uint32_t pulsesPerSecondTmp = 0;
  _TotalPulses += _Pulses - _TotalPulses;
  decodeFlags();

  pulsesPerSecondTmp = pulsesTmp - _LastPulses;
  if(pulsesPerSecondTmp != _LastPulsesPerSecond)
  {
      _PulsesPerSecond = pulsesPerSecondTmp;        
  }
  _LastPulsesPerSecond = pulsesPerSecondTmp;
  
  if(_PulsesPerSecond > _OperatingSpeedVelocity)
  {
    _OperatingSpeedReached = true;
  }
  else
  {
    _OperatingSpeedReached = false;
  }

  _PowerSaveEnabled = _PowerSave;
  _LoadEnabled = _LoadEnable;
  if(_LoadEnable && _OperatingSpeedReached)
  {		
    digitalWrite(GPIO_OUT_ENABLELOAD,HIGH);
    _Loading = true;    
  }
  else
  {
    digitalWrite(GPIO_OUT_ENABLELOAD,LOW);  
    _Loading = false;
  }   
    
  if(_LastPulses != pulsesTmp)
  {
    _Move = true;
    _TimeWasMoving++;
  }
  else
  {
    _Move = false;    
  }
  _LastPulses = pulsesTmp;


  saveTiming();
  
  if(_PulsesPerSecond > _PulsesPerSecondMax)
  {
    _PulsesPerSecondMax = _PulsesPerSecond;
  }
  
  calculateBikeData();
 
  _LowBattery = checkBattery();

  
  if(_Move && _AlarmEnabled)
  {
    _AlarmActiv = true; 
  }
 
  encodeStates();
  if(!_FirstCycle)
  {
    _FirstCycle = true;
    Serial.printf("decodeFlags 0x%x\r\n",_Flags);
    Serial.printf("decodeFlags _LoadEnable = %d\r\n",_LoadEnable);
    Serial.printf("decodeFlags _PowerSave = %d\r\n",_PowerSave);
  }
}

 void CipedTronic::Ciped::TimerElapsed(int32_t id)
 {
   switch(id)
   {
     case 1000:
     {
       TimerMain();
       break;
     }
   }
 }

