#include "CipedService.h"
#include <BLEUtils.h>
#include <BLEServer.h>
#include <BLEService.h>
#include <BLE2902.h>
#include "MSTimer.h"


CipedService::CipedService(BLEServer *pServer,CipedServiceEvents* listener)
:_CipedMeasurementCharacteristic(CIPED_MEASUREMENT_CHARACTER_UUID,BLECharacteristic::PROPERTY_NOTIFY | BLECharacteristic::PROPERTY_READ)
,_CipedControlPointCharacteristic(CIPED_CONTROL_POINT_CHARACTER_UUID,BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_INDICATE)
,_Server(pServer)
,_Service(NULL)
,_Listener(listener)
,_LastTick(0)
,_LastTick250(0)
,_LastTick500(0)
,_CipedMeasurement()
,_DeviceConnected(false)
,_Toggle(false)
{
   if(_Server != NULL)
   {
      pinMode(D10, OUTPUT);
      _Server->setCallbacks(this);
      _Service = pServer->createService(CIPED_SERVICE_UUID);
      if(_Service != NULL)
      {
        Serial.println("Service created");
        _CipedMeasurementCharacteristic.addDescriptor(new BLE2902());
        _CipedControlPointCharacteristic.addDescriptor(new BLE2902());
        _Service->addCharacteristic(&_CipedMeasurementCharacteristic);
        _Service->addCharacteristic(&_CipedControlPointCharacteristic);
        
        _CipedMeasurementCharacteristic.setCallbacks(this);    
        _CipedControlPointCharacteristic.setCallbacks(this);    
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
  }
}

void CipedTronic::CipedTronic::CipedService::stop()
{
   if(_Service != NULL)
  {
    _Service->stop();
  }
}

void CipedTronic::CipedService::process()
{
  uint32_t tick = MSTimer::Instance()->getTick();
  if(tick - _LastTick > 1000)
  {
      sendCipedMeasurementCharacteristicValue(&_CipedMeasurement);    
      _LastTick = tick;
  }

  if(_DeviceConnected)
  {
    if(tick - _LastTick250 > 125)
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
      _LastTick250 = tick;        
    }
  }
  else
  {
    if(tick - _LastTick500 > 500)
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
       _LastTick500 = tick;          
    }    
  }
}

void CipedTronic::CipedService::setCipedData(CipedMeasurement* cipedMeasurement)
{
    memcpy(&_CipedMeasurement,cipedMeasurement,sizeof(CipedMeasurement));
}

void CipedTronic::CipedService::sendCipedMeasurementCharacteristicValue(CipedMeasurement* cipedMeasurement)
{
  if(!_DeviceConnected)
  {
    return;    
  }
   //Serial.printf("send %d %d %d %d 0x%x \r\n",cipedMeasurement->Pulses,cipedMeasurement->PulsesPerSecond,cipedMeasurement->PulsesPerSecondMax,cipedMeasurement->PulsesPerSecondAvg,cipedMeasurement->State);
  _CipedMeasurementCharacteristic.setValue((uint8_t*)cipedMeasurement,sizeof(CipedMeasurement));  
  _CipedMeasurementCharacteristic.notify();
}

void CipedTronic::CipedService::processCipedControlPoint(uint8_t* data)
{
  CipedControlPoint* cipedControlPointData = (CipedControlPoint*)data;
  if(cipedControlPointData == NULL)
  {
    return;    
  }
  switch(static_cast<int>(cipedControlPointData->OpResultCode))
  {
    case static_cast<int>(CipedControlPointOpCodes_e::SetPulsesPerRevolution):
    {
      processCipedControlPointSetPulsesPerRevolution(cipedControlPointData);
      break;
    } 
    case static_cast<int>(CipedControlPointOpCodes_e::ResetCounter):
    {
      processCipedControlResetCounter(cipedControlPointData);
      break;
    } 
    case static_cast<int>(CipedControlPointOpCodes_e::SetLight):
    {
      processCipedControlPointSetLight(cipedControlPointData);
      break;
    } 
    case static_cast<int>(CipedControlPointOpCodes_e::SetAlarm):
    {
      processCipedControlPointSetAlarm(cipedControlPointData);
      break;
    } 
    default:
    {
      cipedControlPointData->OpResultCode = static_cast<uint8_t>(CipedControlPointResultCodes_e::NotSupported);
      cipedControlPointData->Parameter = 0;
      processCipedControlPointResponse(cipedControlPointData);
      break;
    }           
  }
}


void CipedTronic::CipedService::processCipedControlPointSetPulsesPerRevolution(CipedControlPoint* data)
{
  if(_Listener)
  {
    data->OpResultCode = _Listener->ControlPointWritten((CipedControlPointOpCodes_e)data->OpResultCode, data->Parameter);
  }
  processCipedControlPointResponse(data);
}

void CipedTronic::CipedService::processCipedControlResetCounter(CipedControlPoint* data)
{
  if(_Listener)
  {
    data->OpResultCode = _Listener->ControlPointWritten((CipedControlPointOpCodes_e)data->OpResultCode, data->Parameter);
  }
  processCipedControlPointResponse(data);
}

void CipedTronic::CipedService::processCipedControlPointSetLight(CipedControlPoint* data)
{
  if(_Listener)
  {
    data->OpResultCode = _Listener->ControlPointWritten((CipedControlPointOpCodes_e)data->OpResultCode, data->Parameter);
  }
  processCipedControlPointResponse(data);
}

void CipedTronic::CipedService::processCipedControlPointSetAlarm(CipedControlPoint* data)
{
  if(_Listener)
  {
    data->OpResultCode = _Listener->ControlPointWritten((CipedControlPointOpCodes_e )data->OpResultCode, data->Parameter);
  }
  processCipedControlPointResponse(data);
}

void CipedTronic::CipedService::processCipedControlPointResponse(CipedControlPoint* data)
{
  _CipedControlPointCharacteristic.setValue((uint8_t*)data,sizeof(CipedControlPoint));  
  _CipedControlPointCharacteristic.indicate();
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
  Serial.println("connected");
};

void CipedTronic::CipedService::onDisconnect(BLEServer* pServer) 
{
  _DeviceConnected = false;
  Serial.println("disconnected");
   BLEDevice::startAdvertising();
}
