#include "CSCService.h"
#include <BLEUtils.h>
#include <BLEServer.h>
#include <BLE2902.h>

//b1fbda81-f607-42a1-827d-f84ae6bdf20a created UUID

#define CSC_SERVICE_UUID  "b1fb1816-f607-42a1-827d-f84ae6bdf20a"  //1816 
#define CSC_CLIENT_CONF_DESC_UUID "b1fb2902-f607-42a1-827d-f84ae6bdf20a" //Client Characteristic Configuration  2902
//CSC Measurement
#define CSC_MEASUREMENT_CHARACTER_UUID "b1fb2a5b-f607-42a1-827d-f84ae6bdf20a"  // 2a5b
//CSC Feature
#define CSC_FEATURE_CHARACTER_UUID "b1fb2a5c-f607-42a1-827d-f84ae6bdf20a"  //2a5c
//Sensor Location
#define CSC_SENSORLOCATION_CHARACTER_UUID "b1fb2a5d-f607-42a1-827d-f84ae6bdf20a"  //2a5d
//SC Control Point
#define SC_CONTROL_POINT_CHARACTER_UUID "b1fb2a55-f607-42a1-827d-f84ae6bdf20a"  //2a55



CSCService::CSCService(uint16_t features, BLEServer *pServer)
:_CSCMeasurementCharacteristic(CSC_MEASUREMENT_CHARACTER_UUID,BLECharacteristic::PROPERTY_NOTIFY)
,_CSCFeatureCharacteristic(CSC_FEATURE_CHARACTER_UUID,BLECharacteristic::PROPERTY_READ)
,_CSCSensorLocationCharacteristic(CSC_SENSORLOCATION_CHARACTER_UUID,BLECharacteristic::PROPERTY_READ)
,_CSCControlPointCharacteristic(SC_CONTROL_POINT_CHARACTER_UUID,BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_INDICATE)
,_Server(pServer)
,_Service(NULL)
,_LastWheelEventTime(0)
,_CumulativeWheelRevolutions(0)
,_Flags()
,_Features()
,_DeviceConnected(false)
,_DeviceConnectedOld(false)
{
   if(_Server != NULL)
   {
      _Service = pServer->createService(CSC_SERVICE_UUID);
      if(_Service != NULL)
      {
        _CSCMeasurementCharacteristic.addDescriptor(new BLE2902());
        _CSCControlPointCharacteristic.addDescriptor(new BLE2902());
        _Service->addCharacteristic(&_CSCMeasurementCharacteristic);
        _Service->addCharacteristic(&_CSCFeatureCharacteristic);
        _Service->addCharacteristic(&_CSCSensorLocationCharacteristic);
        _Service->addCharacteristic(&_CSCControlPointCharacteristic);
   
        _CSCMeasurementCharacteristic.setCallbacks(this);    
        _CSCFeatureCharacteristic.setCallbacks(this);    
        _CSCSensorLocationCharacteristic.setCallbacks(this);    
        _CSCControlPointCharacteristic.setCallbacks(this);    
      }
   }
}

CSCService::~CSCService ()
{
 
}
 
void CSCService::start()
{
   if(_Service != NULL)
  {
    _Service->start();

  }
}

void CSCService::stop()
{
   if(_Service != NULL)
  {
    _Service->stop();

  }
}

void CSCService::setCSCSData(uint16_t lastWheelEventTime,uint32_t cumulativeWheelRevolutions)
{
  _LastWheelEventTime = lastWheelEventTime;
  _CumulativeWheelRevolutions = cumulativeWheelRevolutions;

}



void CSCService::processSCControlPoint()
{
  
}
void CSCService::processSCControlPointSetCumulativeValue()
{
}
void CSCService::processSCControlPointStartCalibration()
{
}
void CSCService::processSCControlPointUpdateSensorLocation()
{
}
void CSCService::processSCControlPointRequestSupportedSensorLocations()
{
}

//this device supports only Wheel Revolution Data Present
void CSCService::sendCSCMeasurementCharacteristicValue(uint16_t lastWheelEventTime,uint32_t cumulativeWheelRevolutions)
{
  CSCMeasurement measurement = {0,0,0};
  measurement.CumulativeWheelRevolutions = cumulativeWheelRevolutions;
  measurement.LastWheelEventTime = lastWheelEventTime;
  measurement.Flags |= (uint8_t)CSCMeasurementFlags_e::WheelRevolutionDataPresent;
  _CSCMeasurementCharacteristic.setValue((uint8_t*)&measurement,sizeof(CSCMeasurement));
}

void CSCService::onWrite(BLECharacteristic *pCharacteristic)
{
}
void CSCService::onStatus(BLECharacteristic* pCharacteristic, Status s, uint32_t code)
{
}
void CSCService::onRead(BLECharacteristic* pCharacteristic, esp_ble_gatts_cb_param_t* param)
{
}
void CSCService::onNotify(BLECharacteristic* pCharacteristic)
{
}

void onRead(BLEDescriptor* pDescriptor)
{
}
void onWrite(BLEDescriptor* pDescriptor)
{
}
