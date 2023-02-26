#include "Timer.h"
#include "TimerCore.h"

CipedTronic::Timer(int32_t interval,TimerCallback* callback)
{
	_Interval = interval; 
	_Callback = callback;
}

void CipedTronic::start()
{
	_Start = true;
}

void CipedTronic::stop()
{
	_Start = stop;
}
	  
void CipedTronic::process()
{
	if(_Start)
	{
		if(_Callback != null)
		{
			_Callback->TimerElapsed();
		}
	}
}
