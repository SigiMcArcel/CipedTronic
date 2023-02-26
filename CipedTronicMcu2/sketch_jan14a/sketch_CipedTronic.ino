
#include "Ciped.h"
#include "CipedService.h"
#include <BLEDevice.h>
#include <BLEServer.h>

BLEServer* _Server;
Ciped* _Ciped = NULL;;
CipedService* _CipedService = NULL;


class CipedEvents: public CipedServiceEvents
{
public:
  uint8_t ControlPointWritten(CipedControlPointOpCodes_e opCode,uint32_t parameter)
  {
    if(_Ciped == NULL)
    {
      return (uint8_t)CipedControlPointResultCodes_e::OperationFailed;
    }
    switch(opCode)
    {
      case CipedControlPointOpCodes_e::SetAlarm:
      {
        Serial.printf("SetAlarm %d\r\n",parameter);
        if(parameter > 0)
        {
          _Ciped->activateAlarm(true);
        }
        else
        {
           _Ciped->activateAlarm(false);
        }
        
        return (uint8_t)CipedControlPointResultCodes_e::Success;
      } 
      case CipedControlPointOpCodes_e::SetLight:
      {
         Serial.printf("Setlight %d\r\n",parameter);
        if(parameter > 0)
        {
          _Ciped->setLight(true);
        }
        else
        {
           _Ciped->setLight(false);
        }
        return (uint8_t)CipedControlPointResultCodes_e::Success;
      }      
       case CipedControlPointOpCodes_e::ResetCounter:
      {
         Serial.printf("reaset %d\r\n",parameter);
        _Ciped->clear();
       return (uint8_t)CipedControlPointResultCodes_e::Success;
      } 
    }
  }
};

void setup() {
  Serial.begin(115200);
  Serial.println("Starting BLE work!");
  

  BLEDevice::init("CIPED");
  _Server = BLEDevice::createServer();
  if(_Server != NULL)
  {
    _Ciped = new Ciped();
    _CipedService = new CipedService(_Server,new CipedEvents());
    _CipedService->start();
    
  }  

}
 
void loop() {
  CipedMeasurement measure = {0};
  
  _Ciped->process();
  measure.Pulses = _Ciped->getPulses();
  measure.PulsesPerSecond = _Ciped->getPulsesPerSecond();
  measure.PulsesPerSecondMax = _Ciped->getPulsesPerSecondMax();
  measure.PulsesPerSecondAvg = _Ciped->getPulsesPerSecondAvg();
  measure.State = _Ciped->getState();
  _CipedService->setCipedData(&measure);
  _CipedService->process();
}