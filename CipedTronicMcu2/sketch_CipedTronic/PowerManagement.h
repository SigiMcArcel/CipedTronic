#ifndef CIPEDTRONIC_POWERMANAGEMENT_H_
#define CIPEDTRONIC_POWERMANAGEMENT_H_

#include "Arduino.h"

namespace CipedTronic
{
	class PowerManagementCallback
	{
		public:
		virtual void beforeSleep() = 0;
	};
	
	class PowerManagement
	{
	private :
	  int32_t _Pin;
	  uint32_t _Type;
	  PowerManagementCallback* _Callback;
	 
 
	public:
	  PowerManagement(int32_t pin,int32_t type, PowerManagementCallback* callback);
	  void start();
	  
  };
}
#endif //CIPEDTRONIC_POWERMANAGEMENT_H_

 

