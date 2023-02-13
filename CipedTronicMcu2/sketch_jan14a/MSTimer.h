#include "esp32-hal-timer.h"
#include <string>
#include "Arduino.h"

class MSTimer
{
  public:
  static MSTimer* _Instance;
  uint32_t _TimerTick;
  hw_timer_t* _Timer;
  
  MSTimer();

  static void IRAM_ATTR onTimer();
public:
 // deleting copy constructor
  MSTimer(const MSTimer& obj) = delete;
  static MSTimer* Instance();
  uint32_t getTick();
};


 

