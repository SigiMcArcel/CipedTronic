#include "TimerCore.h"

TimerCore* CipedTronic::TimerCore::_Instance = NULL;

void IRAM_ATTR CipedTronic::TimerCore::onTimer(){
    _Instance->_TimerTick++;
}



CipedTronic::TimerCore::TimerCore()
{
  _Timer = timerBegin(0, 80, true);
  timerAttachInterrupt(_Timer, &onTimer, true);
  timerAlarmWrite(_Timer, 1000, true);
  timerAlarmEnable(_Timer);    
}
  
CipedTronic::TimerCore* TimerCore::Instance()
{
  if(_Instance == NULL)
  {
      _Instance = new CipedTronic::TimerCore; 
  }
  return _Instance;
}

uint32_t CipedTronic::TimerCore::getTick()
{
  return _TimerTick;
}


