#ifndef CIPEDTRONIC_CIPEDSERVICE_H_
#define CIPEDTRONIC_CIPEDSERVICE_H_

#include <string>
#include <BLEDevice.h>
#include "Timer.h"

namespace CipedTronic
{

//b1fbda81-f607-42a1-827d-f84ae6bdf20a created UUID

#define CIPED_SERVICE_UUID  "b1fb1816-f607-42a1-827d-f84ae6bdf20a"  //2000 Ciped 
#define CSC_CLIENT_CONF_DESC_UUID "b1fb2902-f607-42a1-827d-f84ae6bdf20a" //Client Characteristic Configuration  2902
//CSC Measurement
#define CIPED_BIKEDATA_CHARACTER_UUID "b1fb0001-f607-42a1-827d-f84ae6bdf20a"  // 0001
//SC Control Point
#define CIPED_CONTROL_POINT_CHARACTER_UUID "b1fb0002-f607-42a1-827d-f84ae6bdf20a"  //0002
//SC Control Point
#define CIPED_INFO_CHARACTER_UUID "b1fb0003-f607-42a1-827d-f84ae6bdf20a"  //0003


typedef struct __attribute__((packed, aligned(1))) CipedBikeData_t
{
  uint32_t Pulses;
  uint32_t PulsesPerSecond;
  uint32_t PulsesPerSecondMax;
  uint32_t TimeMoving;
  uint32_t State;
}CipedBikeData ;

typedef struct __attribute__((packed, aligned(1))) CipedInfo_t
{
  float VBatt;
}CipedInfo ;

typedef struct __attribute__((packed, aligned(1))) CipedControlPoint_t
{
  uint8_t OpRequestCode;
  uint8_t OpResultCode;
  uint32_t Parameter;
}CipedControlPoint ;

enum class CipedControlPointOpCodes_e:uint8_t
{
  Reserved,
  SetFlags,
  GetFlags,
  ResetCounter,
  ResetAlarm,
  AlarmEnable,
  Max
};


enum class CipedControlPointResultCodes_e:uint8_t
{
  Reserved = 0,
  Success,
  NotSupported,
  InvalidParameter,
  OperationFailed  
};

class CipedServiceEvents
{
  public:
  virtual uint8_t ControlPointWritten(CipedControlPointOpCodes_e opCode,CipedControlPoint* data);
};

class CipedService:public BLECharacteristicCallbacks,public BLEDescriptorCallbacks,public BLEServerCallbacks,public TimerCallback 
{
  private:
  BLECharacteristic _CipedBikeDataCharacteristic;
  BLECharacteristic _CipedControlPointCharacteristic;
  BLECharacteristic _CipedInfoCharacteristic;
  BLEServer *_Server;
  BLEService *_Service;
  CipedServiceEvents* _Listener;

  Timer _Timer1000ms;
  Timer _Timer500ms;
  Timer _Timer250ms;
  uint32_t _LastTick500;
  uint32_t _LastTick250;
  
  CipedBikeData _CipedBikeData;
  CipedInfo _CipedInfo;
  
  bool _DeviceConnected;
  uint32_t _State;
  bool _Toggle;
  uint32_t _NotifyScheduler;
 

  void Timer1000Elapsed();
  void Timer500Elapsed();
  void Timer250Elapsed();
  void toggleIndicatorLed();

  void processCipedControlPoint(uint8_t* data);
  void processCipedControlPointResponse(CipedControlPoint* data);

  void sendCipedBikeDataCharacteristicValue(CipedBikeData* cipedBikeData);
  void sendCipedInfoCharacteristicValue(CipedInfo* Info);

public:
  CipedService( BLEServer *pServer,CipedServiceEvents* listener);
  ~CipedService(); 
  void start();
  void stop();
  void process();

  void setCipedData(CipedBikeData* cipedBikeData);
  void setCipedInfo(CipedInfo*info);
  

//Implements

void onConnect(BLEServer* pServer);
void onDisconnect(BLEServer* pServer);

void onWrite(BLECharacteristic *pCharacteristic);
void onStatus(BLECharacteristic* pCharacteristic, Status s, uint32_t code) ;
void onRead(BLECharacteristic* pCharacteristic, esp_ble_gatts_cb_param_t* param);
void onNotify(BLECharacteristic* pCharacteristic);

void onRead(BLEDescriptor* pDescriptor);
void onWrite(BLEDescriptor* pDescriptor);

void TimerElapsed(int32_t id);
    
};

}//namespace CipedTronic

#endif //CIPEDTRONIC_CIPEDSERVICE_H_
