
#include "Ciped.h"
#include "CipedService.h"
#include <BLEDevice.h>
#include <BLEServer.h>

using namespace CipedTronic;
BLEServer* _Server;
Ciped* _Ciped = NULL;
CipedService* _CipedService = NULL;


class CipedEvents: public CipedTronic::CipedServiceEvents
{
public:
  uint8_t ControlPointWritten(CipedTronic::CipedControlPointOpCodes_e opCode,CipedControlPoint* data)
  {
    if(_Ciped == NULL)
    {
      return (uint8_t)CipedTronic::CipedControlPointResultCodes_e::OperationFailed;
    }
    Serial.printf("ControlPointWritten %d\r\n",opCode);
    switch(opCode)
    {
      case CipedTronic::CipedControlPointOpCodes_e::SetAlarm:
      {
        Serial.printf("SetAlarm %d\r\n", data->Parameter);
        if(data->Parameter > 0)
        {
          _Ciped->activateAlarm(true);
        }
        else
        {
           _Ciped->activateAlarm(false);
        }
        
        return (uint8_t)CipedTronic::CipedControlPointResultCodes_e::Success;
      } 
      case CipedTronic::CipedControlPointOpCodes_e::SetLoadEnable:
      {
         Serial.printf("Setlight %d\r\n",data->Parameter);
        if(data->Parameter > 0)
        {
          _Ciped->setLoadEnable(true);
        }
        else
        {
           _Ciped->setLoadEnable(false);
        }
        return (uint8_t)CipedTronic::CipedControlPointResultCodes_e::Success;
      }      
       case CipedTronic::CipedControlPointOpCodes_e::ResetCounter:
      {
         Serial.printf("reset %d\r\n",data->Parameter);
        _Ciped->clear();
       return (uint8_t)CipedTronic::CipedControlPointResultCodes_e::Success;
      } 
       case CipedTronic::CipedControlPointOpCodes_e::GetRadius:
      {
         data->Parameter = (uint32_t)_Ciped->getRadius();
         Serial.printf("GetRadius %d\r\n",data->Parameter);
        return (uint8_t)CipedTronic::CipedControlPointResultCodes_e::Success;
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