#include "Timer.h"
#include "TimerCore.h"

CipedTronic::Timer::Timer(int32_t interval,int32_t id, TimerCallback* callback)
:_Interval(interval)
,_Id(id)
,_Callback(callback)
,_LastTick(0)
,_TimeoutStartValue(0)
{
	
}

void CipedTronic::Timer::start()
{
	_Start = true;
}

void CipedTronic::Timer::stop()
{
	_Start = false;
}
	  
bool CipedTronic::Timer::timeOut(int32_t timeOutValue)
{
  if(_TimeoutStartValue == 0)
  {
    _TimeoutStartValue = TimerCore::Instance()->getTick();
  }  
  else
  {
      if(timeOutValue > (TimerCore::Instance()->getTick() - _TimeoutStartValue))
      {
         _TimeoutStartValue = 0;
          return true;
      }      
  }
   return false;
}

bool CipedTronic::Timer::timeOutReset()
{
  _TimeoutStartValue = 0;
}
//check interrupt prio
void CipedTronic::Timer::process()
{
	if(_Start)
	{
    uint32_t tick = TimerCore::Instance()->getTick();    
    if(_LastTick != tick)//prevent double entry
    {
        if(tick - _LastTick >= _Interval)
        {
          if(_Callback != NULL)
          {
            _Callback->TimerElapsed(_Id);
          }      
          _LastTick = tick;
        }
    }
    
	}
}
