#include "esp32-hal-gpio.h"
#include "Ciped.h"
#include <EEPROM.h>

static uint32_t _Pulses = 0;
static void IRAM_ATTR isr() {
	_Pulses++;
  
}

Ciped::Ciped()
  :_LastPulses(0)
  ,_PulsesPerSecond(0)
  ,_PulsesPerSecondMax(0)
  ,_PulsesPerSecondAvg(0)
  ,_Move(false)
  ,_Alarm(false)
  ,_AlarmActivated(false)
  ,_LightOn(false)
  ,_LowBattery(false)
  ,_StandstillTick(0)
  ,_SavingState(0)
  ,_State(0)


{
  pinMode(D1, INPUT_PULLUP);

	attachInterrupt(D1, isr, RISING);
  pinMode(A0, INPUT);
  EEPROM.begin(20);
  
  loadFromEeprom();
 
}

Ciped::~Ciped ()
{
 
}

void Ciped::saveToEeprom()
{
    EEPROM.writeULong(0,_Pulses);
    EEPROM.commit();
    EEPROM.writeULong(4,_PulsesPerSecondMax);
    EEPROM.commit();
    EEPROM.writeULong(8,_PulsesPerSecondAvg);
    EEPROM.commit();
}

void Ciped::loadFromEeprom()
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

//save pulses a 30 second  after the bike stand still or battery is low
 void Ciped::saveTiming()
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
              _StandstillTick = 0;
              break;              
          }
           _StandstillTick++;
           if(_StandstillTick > 10)
           {
              
               saveToEeprom();
               _SavingState = 0;
               _StandstillTick = 0;
              break; 
           }
           break;
        }
      }      
    }
 }

bool Ciped::checkBattery()
{
  uint32_t Vbatt = 0;
  for(int i = 0; i < 16; i++) {
    Vbatt = Vbatt + analogReadMilliVolts(A0); // ADC with correction   
  }
  float Vbattf = 2 * Vbatt / 16 / 1000.0; 
  if(Vbattf <= 3.4)
  {
    return true;    
  } 
  return false; 
}

void Ciped::setPulsesPerRevolution(uint32_t pulsesPerRevolution)
{
  _PulsesPerRevolution = pulsesPerRevolution;
}

uint32_t Ciped::getPulses()
{
   return _Pulses;
}

uint32_t Ciped::getPulsesPerSecond()
{
  return _PulsesPerSecond;
}

uint32_t Ciped::getPulsesPerSecondMax()
{
  return _PulsesPerSecondMax;
}

uint32_t Ciped::getPulsesPerSecondAvg()
{
  return _PulsesPerSecondAvg;
}


void  Ciped::activateAlarm(bool alarm)
{
  _AlarmActivated = alarm;
   if(_AlarmActivated)
    {
      _State |= (uint32_t)CipedStates_e::AlarmActived;
    }
    else
    {
      _State &= ~(uint32_t)CipedStates_e::AlarmActived;
      _AlarmActivated = false;
    }
}
void Ciped::resetAlarm()
{
   _Alarm = false;
}

void  Ciped::setLight(bool light)
{
  if(light)
  {
    _State |= (uint32_t)CipedStates_e::LightOn;
  }
  else
  {
    _State &= ~(uint32_t)CipedStates_e::LightOn;
  }
  _LightOn = light;
}

uint32_t Ciped::getState()
{
    return _State;
}

void Ciped::clear()
{
  _Pulses = 0;
  _LastPulses = 0;
  _PulsesPerSecond = 0;
  _PulsesPerSecondMax = 0;
  _PulsesPerSecondAvg = 0;
  saveToEeprom();
}

void Ciped::process()
{
  uint32_t tick = MSTimer::Instance()->getTick();
  uint32_t pulsesTmp = 0;
  if(tick - _LastTick >= 1000)
  {
    pulsesTmp = _Pulses;

   
    
    _LastTick = tick;
    _PulsesPerSecond = pulsesTmp - _LastPulses;
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

     if(_Move && _AlarmActivated)
    {
      _State |= (uint32_t)CipedStates_e::AlarmActive;
    }
    else
    {
      _State &= ~(uint32_t)CipedStates_e::AlarmActive;
    }
     
  }
}
 

