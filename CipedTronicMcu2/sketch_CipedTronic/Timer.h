#ifndef CIPEDTRONIC_TIMER_H_
#define CIPEDTRONIC_TIMER_H_
#include "Arduino.h"


namespace CipedTronic
{
	class TimerCallback
	{
		public:
		virtual void TimerElapsed(int32_t id) = 0;
	};
	
	class Timer
	{
	private :
		int32_t _Interval;
    int32_t _Id;
		TimerCallback* _Callback;
		bool _Start;
    uint32_t _LastTick;
    uint32_t _TimeoutStartValue;
		
	public:
	  Timer(int32_t interval,int32_t id, TimerCallback* callback);
	  void start();
	  void stop();
    bool timeOut(int32_t timeOutValue);
    bool timeOutReset();
	  void process();
	};
}
#endif //CIPEDTRONIC_TIMER_H_

 

