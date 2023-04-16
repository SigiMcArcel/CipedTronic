#include "PowerManagement.h"


CipedTronic::PowerManagement::PowerManagement(int32_t pin,int32_t type, PowerManagementCallback* callback)
:_Pin(pin)
,_Type(type)
,_Callback(callback)

{
 
}

void CipedTronic::PowerManagement::start()
{
  gpio_deep_sleep_hold_dis();
  esp_err_t errPD = esp_sleep_pd_config(ESP_PD_DOMAIN_RTC_PERIPH, ESP_PD_OPTION_ON);
  esp_err_t errGPIO = esp_deep_sleep_enable_gpio_wakeup(1<<_Pin, ESP_GPIO_WAKEUP_GPIO_HIGH);
  if(_Callback)
  {
	  _Callback->beforeSleep();
  }
  delay(500);
  esp_deep_sleep_start();
}

