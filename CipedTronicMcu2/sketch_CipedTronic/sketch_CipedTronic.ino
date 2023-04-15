
#include "Ciped.h"
#include "CipedService.h"
#include <BLEDevice.h>
#include <BLEServer.h>

using namespace CipedTronic;
BLEServer* _Server;
Ciped* _Ciped =  NULL;
CipedService* _CipedService = NULL;
uint32_t _LastState = 0;



class CipedControlPointEvents: public CipedTronic::CipedServiceEvents
{
public:
  uint8_t ControlPointWritten(CipedTronic::CipedControlPointOpCodes_e opCode,CipedControlPoint* data)
  {
    if(_Ciped == NULL)
    {
      return (uint8_t)CipedTronic::CipedControlPointResultCodes_e::OperationFailed;
    }
    
    switch(opCode)
    {
      
      case CipedTronic::CipedControlPointOpCodes_e::SetFlags:
      {
        Serial.printf("SetFlags 0x%x\r\n",data->Parameter);
        _Ciped->setFlags(data->Parameter);
        
        return (uint8_t)CipedTronic::CipedControlPointResultCodes_e::Success;
      } 

       case CipedTronic::CipedControlPointOpCodes_e::GetFlags:
      {
  
        data->Parameter = _Ciped->getFlags();
        Serial.printf("GetFlags 0x%x\r\n",data->Parameter);
        return (uint8_t)CipedTronic::CipedControlPointResultCodes_e::Success;
      } 

      case CipedTronic::CipedControlPointOpCodes_e::ResetCounter:
      {
         Serial.printf("ResetCounter 0x%x\r\n",data->Parameter);
        _Ciped->clear();
       return (uint8_t)CipedTronic::CipedControlPointResultCodes_e::Success;
      } 
      
      case CipedTronic::CipedControlPointOpCodes_e::AlarmEnable:
      {
         Serial.printf("AlarmEnable 0x%x\r\n",data->Parameter);
        _Ciped->enableAlarm((bool)data->Parameter);
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
    _Ciped->begin();
    _CipedService = new CipedService(_Server,new CipedControlPointEvents());
    _CipedService->start();
  }  

}
 
void loop() {
  CipedBikeData data = {0};
  CipedInfo info = {0};
  uint32_t state = 0;  
  _Ciped->process();
  _Ciped->getCipedBikeData(&data);
  _Ciped->getCipedBikeInfo(&info);
  
  _CipedService->setCipedData(&data);
  _CipedService->setCipedInfo(&info);
  _CipedService->process();
}