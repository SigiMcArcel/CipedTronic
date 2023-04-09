#include "esp32-hal-gpio.h"
#include "Ciped.h"
#include <EEPROM.h>

static uint32_t _Pulses = 0;

static void IRAM_ATTR isr() {
   _Pulses++;
}


#define GPIO_IN_PULSES D1
#define GPIO_OUT_ENABLELOAD D2
#define GPIO_BATTERY_LOAD A2

#define PROCESSCYCLETIME_MS 1000
#define SAVE_EEPROM_TIME_MS 15000

CipedTronic::Ciped::Ciped()
  :_PulsesPerRevolution(0)
  ,_LastPulses(0)
  ,_PulsesPerSecond(0)
  ,_LastPulsesPerSecond(0)
  ,_PulsesPerSecondMax(0)
  ,_PulsesPerSecondAvg(0)
  ,_LastTick(0)
  ,_AlarmEnable(false)
  ,_LoadEnable(false)
  ,_Move(false)
  ,_Alarm(false)
  ,_Loading(false)
  ,_LowBattery(false)
  ,_State(0)
  ,_ProcessTimer(PROCESSCYCLETIME_MS,PROCESSCYCLETIME_MS,this)
  ,_Lox(Adafruit_VL53L0X())
  ,_SavingState(0)

{
  //Pulses
  pinMode(GPIO_IN_PULSES, INPUT);
  attachInterrupt(GPIO_IN_PULSES, isr, RISING);
  //Load Enable
  pinMode(GPIO_OUT_ENABLELOAD, OUTPUT);
  digitalWrite(GPIO_OUT_ENABLELOAD,LOW);
  //Battery measurement
  pinMode(GPIO_BATTERY_LOAD, INPUT);
  EEPROM.begin(20);
  
  loadFromEeprom();
  _Lox.begin();
  _ProcessTimer.start();
 
}

CipedTronic::Ciped::~Ciped ()
{
 _ProcessTimer.stop();
}

void CipedTronic::Ciped::saveToEeprom()
{
    EEPROM.writeULong(0,_Pulses);
    EEPROM.commit();
    EEPROM.writeULong(4,_PulsesPerSecondMax);
    EEPROM.commit();
    EEPROM.writeULong(8,_PulsesPerSecondAvg);
    EEPROM.commit();
}

void CipedTronic::Ciped::loadFromEeprom()
{
   uint32_t test = EEPROM.readULong(0);
   if(test == 0xFFFFFFFF)
   {
      Serial.printf("eprom init %d\r\n",test);
     saveToEeprom();
      return;
   }
   _LastPulses =_Pulses = EEPROM.readULong(0);
   _PulsesPerSecondMax = EEPROM.readULong(4);
   _PulsesPerSecondAvg = EEPROM.readULong(8);
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
              saveToEeprom();
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
  if(Vbattf <= 3.4)
  {
    return true;    
  } 
  return false; 
}

void CipedTronic::Ciped::setPulsesPerRevolution(uint32_t pulsesPerRevolution)
{
  _PulsesPerRevolution = pulsesPerRevolution;
}

uint32_t CipedTronic::Ciped::getPulses()
{
   return _Pulses;
}

uint32_t CipedTronic::Ciped::getPulsesPerSecond()
{
  return _PulsesPerSecond;
}

uint32_t CipedTronic::Ciped::getPulsesPerSecondMax()
{
  return _PulsesPerSecondMax;
}

uint32_t CipedTronic::Ciped::getPulsesPerSecondAvg()
{
  return _PulsesPerSecondAvg;
}


void  CipedTronic::Ciped::activateAlarm(bool alarm)
{
  _AlarmEnable = alarm;
   if(_AlarmEnable)
    {
      _State |= (uint32_t)CipedStates_e::AlarmEnabled;
    }
    else
    {
      _State &= ~(uint32_t)CipedStates_e::AlarmEnabled;
    }
}
void CipedTronic::Ciped::resetAlarm()
{
   _Alarm = false;
}

void  CipedTronic::Ciped::setLoadEnable(bool enable)
{
   _LoadEnable = enable;
  if(_LoadEnable)
  {
    _State |= (uint32_t)CipedStates_e::LoadEnabled;
  }
  else
  {
    _State &= ~(uint32_t)CipedStates_e::LoadEnabled;
  }
 
}

uint32_t CipedTronic::Ciped::getState()
{
  return _State;
}

uint16_t CipedTronic::Ciped::getRadius()
{
   VL53L0X_RangingMeasurementData_t measure;
  _Lox.rangingTest(&measure, false); 
  if (measure.RangeStatus != 4)
  {
	  return measure.RangeMilliMeter;
  }
  else
  {
	  return -1;
  }
}

void CipedTronic::Ciped::clear()
{
  _Pulses = 0;
  _LastPulses = 0;
  _PulsesPerSecond = 0;
  _PulsesPerSecondMax = 0;
  _PulsesPerSecondAvg = 0;
  saveToEeprom();
}

void CipedTronic::Ciped::process()
{
  _ProcessTimer.process();
}
 

 void CipedTronic::Ciped::TimerElapsed(int32_t id)
 {
    uint32_t pulsesTmp = _Pulses;
    uint32_t pulsesPerSecondTmp = _Pulses;
    pulsesPerSecondTmp = pulsesTmp - _LastPulses;
    if(pulsesPerSecondTmp != _LastPulsesPerSecond)
    {
        _PulsesPerSecond = pulsesPerSecondTmp;        
    }
    _LastPulsesPerSecond = pulsesPerSecondTmp;
   
	if(_LoadEnable)
	{		
		if(_PulsesPerSecond > _VelocityForLoad)
		{
			digitalWrite(GPIO_OUT_ENABLELOAD,HIGH);
		}
		else
		{
			digitalWrite(GPIO_OUT_ENABLELOAD,LOW);  
		}   
	}
	else
	{
		digitalWrite(GPIO_OUT_ENABLELOAD,LOW); 
	}
    
    if(_LastPulses != pulsesTmp)
    {
      _Move = true;
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

    _LowBattery = checkBattery();

     if(_LowBattery)
    {
      _State |= (uint32_t)CipedStates_e::LowBat;
    }
    else
    {
      _State &= ~(uint32_t)CipedStates_e::LowBat;
    }
    
    if(_Move)
    {
      _State |= (uint32_t)CipedStates_e::Move;
    }
    else
    {
      _State &= ~(uint32_t)CipedStates_e::Move;
    }

     if(_Move && _AlarmEnable)
    {
      _State |= (uint32_t)CipedStates_e::AlarmActive;
    }
    else
    {
      _State &= ~(uint32_t)CipedStates_e::AlarmActive;
    }
 }

