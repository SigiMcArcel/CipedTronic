#include "CipedService.h"
#include <BLEUtils.h>
#include <BLEServer.h>
#include <BLEService.h>
#include <BLE2902.h>
#include "Timer.h"


CipedTronic::CipedService::CipedService(BLEServer *pServer,CipedServiceEvents* listener)
:_CipedBikeDataCharacteristic(CIPED_BIKEDATA_CHARACTER_UUID,BLECharacteristic::PROPERTY_NOTIFY | BLECharacteristic::PROPERTY_READ)
,_CipedControlPointCharacteristic(CIPED_CONTROL_POINT_CHARACTER_UUID,BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_INDICATE)
,_CipedInfoCharacteristic(CIPED_INFO_CHARACTER_UUID,BLECharacteristic::PROPERTY_NOTIFY | BLECharacteristic::PROPERTY_READ)
,_Server(pServer)
,_Service(NULL)
,_Listener(listener)
,_Timer1000ms(500,1000,this)
,_Timer500ms(500,500,this)
,_Timer250ms(100,250,this)
,_CipedBikeData()
,_CipedInfo()
,_DeviceConnected(false)
,_Toggle(false)
,_NotifyScheduler(0)
{
   if(_Server != NULL)
   {
      pinMode(D10, OUTPUT);
      _Server->setCallbacks(this);
      _Service = pServer->createService(CIPED_SERVICE_UUID);
      if(_Service != NULL)
      {
        Serial.println("Service created");
        _CipedBikeDataCharacteristic.addDescriptor(new BLE2902());
        _CipedControlPointCharacteristic.addDescriptor(new BLE2902());
        _CipedInfoCharacteristic.addDescriptor(new BLE2902());
        _Service->addCharacteristic(&_CipedBikeDataCharacteristic);
        _Service->addCharacteristic(&_CipedControlPointCharacteristic);
        _Service->addCharacteristic(&_CipedInfoCharacteristic);
        
        _CipedBikeDataCharacteristic.setCallbacks(this);    
        _CipedControlPointCharacteristic.setCallbacks(this);   
        _CipedInfoCharacteristic.setCallbacks(this); 
         BLEDevice::startAdvertising();    
      }
   }
}

CipedTronic::CipedService::~CipedService ()
{
 
}
 
void CipedTronic::CipedService::start()
{
   if(_Service != NULL)
  {
    Serial.println("Service started");
    _Service->start();
    _Timer1000ms.start();
    _Timer500ms.start();
    _Timer250ms.start();
  }
}

void CipedTronic::CipedService::CipedService::stop()
{
   if(_Service != NULL)
  {
    _Service->stop();
  }
   _Timer1000ms.stop();
  _Timer500ms.stop();
  _Timer250ms.stop();
   digitalWrite(D10,LOW);
}

void CipedTronic::CipedService::process()
{
  _Timer1000ms.process();
  _Timer500ms.process();
  _Timer250ms.process();
}

void CipedTronic::CipedService::toggleIndicatorLed()
{
  if(_Toggle)
  {
    digitalWrite(D10,HIGH);
    _Toggle = false;
  }
  else
  {
    digitalWrite(D10,LOW);
    _Toggle = true;      
  }     
}

void CipedTronic::CipedService::Timer1000Elapsed()
{
  if(!_DeviceConnected)
  {
    return;    
  }
  switch(_NotifyScheduler)
  {
    case 0:
    {
      sendCipedBikeDataCharacteristicValue(&_CipedBikeData);  
      _NotifyScheduler++;
      break;
    }
    case 1:
    {
      sendCipedInfoCharacteristicValue(&_CipedInfo);
      _NotifyScheduler++;      
      break;
    }
    default:
    {
        _NotifyScheduler = 0;
    }
  }
   
	
}

void CipedTronic::CipedService::Timer500Elapsed()
{
  if(!_DeviceConnected)
  {
     toggleIndicatorLed();   
  }
}

void CipedTronic::CipedService::Timer250Elapsed()
{
  if(_DeviceConnected)
  {
     toggleIndicatorLed(); 
  }
}

void CipedTronic::CipedService::setCipedData(CipedBikeData* cipedBikeData)
{
    memcpy(&_CipedBikeData,cipedBikeData,sizeof(CipedBikeData));
}

void CipedTronic::CipedService::setCipedInfo(CipedInfo* info)
{
    memcpy(&_CipedInfo,info,sizeof(CipedInfo));
}

void CipedTronic::CipedService::sendCipedBikeDataCharacteristicValue(CipedBikeData* cipedBikeData)
{
 //  Serial.printf("send Bike data %f %f %f %d %d \r\n",cipedBikeData->Distance,cipedBikeData->Velocity,cipedBikeData->VelocityMax,cipedBikeData->Radius,cipedBikeData->TimeMoving);
  _CipedBikeDataCharacteristic.setValue((uint8_t*)cipedBikeData,sizeof(CipedBikeData));  
  _CipedBikeDataCharacteristic.notify();
}

void CipedTronic::CipedService::sendCipedInfoCharacteristicValue(CipedInfo* info)
{
  // Serial.printf("send info %d %d %d \r\n",info->Pulses,info->PulsesPerSecond,info->Radius);
  _CipedInfoCharacteristic.setValue((uint8_t*)info,sizeof(CipedInfo));  
  _CipedInfoCharacteristic.notify();
}

void CipedTronic::CipedService::processCipedControlPoint(uint8_t* data)
{
  CipedControlPoint* cipedControlPointData = (CipedControlPoint*)data;
  
  if(cipedControlPointData == NULL)
  {
    return;    
  }
  if((CipedControlPointOpCodes_e)cipedControlPointData->OpRequestCode >= CipedControlPointOpCodes_e::Max)
  {
    cipedControlPointData->OpResultCode = static_cast<uint8_t>(CipedControlPointResultCodes_e::NotSupported);
    cipedControlPointData->Parameter = 0;
    processCipedControlPointResponse(cipedControlPointData);
    return;
  }
  if(_Listener)
  {
    cipedControlPointData->OpResultCode = _Listener->ControlPointWritten((CipedControlPointOpCodes_e)cipedControlPointData->OpRequestCode, cipedControlPointData);
  }
  processCipedControlPointResponse(cipedControlPointData);
}


void CipedTronic::CipedService::processCipedControlPointResponse(CipedControlPoint* data)
{
  _CipedControlPointCharacteristic.setValue((uint8_t*)data,sizeof(CipedControlPoint));  
  _CipedControlPointCharacteristic.indicate();
}


//implements

void CipedTronic::CipedService::TimerElapsed(int32_t id)
{
  switch(id)
  {
    case 1000:
    {
       Timer1000Elapsed();
       break;
    }
     case 500:
    {
       Timer500Elapsed();
       break;
    }
     case 250:
    {
       Timer250Elapsed();
       break;
    }
  }
}

void CipedTronic::CipedService::onWrite(BLECharacteristic *pCharacteristic)
{
  if(pCharacteristic->getUUID().equals(_CipedControlPointCharacteristic.getUUID()))
  {
    
    processCipedControlPoint(pCharacteristic->getData());
  }
}

void CipedTronic::CipedService::onStatus(BLECharacteristic* pCharacteristic, Status s, uint32_t code)
{
}

void CipedTronic::CipedService::onRead(BLECharacteristic* pCharacteristic, esp_ble_gatts_cb_param_t* param)
{
}

void CipedTronic::CipedService::onNotify(BLECharacteristic* pCharacteristic)
{
}

void CipedTronic::CipedService::onRead(BLEDescriptor* pDescriptor)
{
}

void CipedTronic::CipedService::onWrite(BLEDescriptor* pDescriptor)
{
}

void CipedTronic::CipedService::onConnect(BLEServer* pServer) 
{
  _DeviceConnected = true;
  Serial.printf("connected\r\n");
};

void CipedTronic::CipedService::onDisconnect(BLEServer* pServer) 
{
  _DeviceConnected = false;
  Serial.println("disconnected");
   BLEDevice::startAdvertising();
}
