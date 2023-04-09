#include "TimerCore.h"

CipedTronic::TimerCore* CipedTronic::TimerCore::_Instance = NULL;

void IRAM_ATTR CipedTronic::TimerCore::onTimer(){
   portENTER_CRITICAL_ISR(&_Instance->_TimerMux);
    _Instance->_TimerTick++;
    portEXIT_CRITICAL_ISR(&_Instance->_TimerMux);
}



CipedTronic::TimerCore::TimerCore()
:_TimerMux(portMUX_INITIALIZER_UNLOCKED)
{
  
  _Timer = timerBegin(0, 80, true);
  timerAttachInterrupt(_Timer, &onTimer, true);
  timerAlarmWrite(_Timer, 1000, true);
  timerAlarmEnable(_Timer);    
}
  
CipedTronic::TimerCore* CipedTronic::TimerCore::Instance()
{
  if(CipedTronic::TimerCore::_Instance == NULL)
  {
      CipedTronic::TimerCore::_Instance = new CipedTronic::TimerCore; 
  }
  return CipedTronic::TimerCore::_Instance;
}

uint32_t CipedTronic::TimerCore::getTick()
{
  return _TimerTick;
}


