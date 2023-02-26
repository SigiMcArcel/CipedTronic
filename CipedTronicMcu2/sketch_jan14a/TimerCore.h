#include "esp32-hal-timer.h"
#include <string>
#include "Arduino.h"

namespace CipedTronic
{	
	class TimerCore
	{
	  public:
	  static TimerCore* _Instance;
	  uint32_t _TimerTick;
	  hw_timer_t* _Timer;
	  
	  TimerCore();

	  static void IRAM_ATTR onTimer();
	public:
	 // deleting copy constructor
	  TimerCore(const MSTimer& obj) = delete;
	  static TimerCore* Instance();
	  uint32_t getTick();
	};
}


 

