#include "Arduino.h"


namespace CipedTronic
{
	class TimerCallback
	{
		public:
		void TimerElapsed() = 0;
	}
	
	class Timer
	{
	private :
		int32_t _Interval;
		TimerCallback* _Callback;
		bool _Start;
		
	public:
	  Timer(int32_t interval,TimerCallback* callback);
	  start();
	  stop();
	 
	  void process();
	};
}


 

