#include "MSTimer.h"
MSTimer* MSTimer::_Instance = NULL;

void IRAM_ATTR MSTimer::onTimer(){
    _Instance->_TimerTick++;
}



MSTimer::MSTimer()
{
  _Timer = timerBegin(0, 80, true);
  timerAttachInterrupt(_Timer, &onTimer, true);
  timerAlarmWrite(_Timer, 1000, true);
  timerAlarmEnable(_Timer);    
}
  
MSTimer* MSTimer::Instance()
{
  if(_Instance == NULL)
  {
      _Instance = new MSTimer; 
  }
  return _Instance;
}

uint32_t MSTimer::getTick()
{
  return _TimerTick;
}


