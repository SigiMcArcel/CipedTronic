#include <string>
#include <BLEDevice.h>



//b1fbda81-f607-42a1-827d-f84ae6bdf20a created UUID

#define CIPED_SERVICE_UUID  "b1fb1816-f607-42a1-827d-f84ae6bdf20a"  //2000 Ciped 
#define CSC_CLIENT_CONF_DESC_UUID "b1fb2902-f607-42a1-827d-f84ae6bdf20a" //Client Characteristic Configuration  2902
//CSC Measurement
#define CIPED_MEASUREMENT_CHARACTER_UUID "b1fb0001-f607-42a1-827d-f84ae6bdf20a"  // 0001
//SC Control Point
#define CIPED_CONTROL_POINT_CHARACTER_UUID "b1fb0002-f607-42a1-827d-f84ae6bdf20a"  //0001


typedef struct CipedMeasurement_t
{
  uint32_t Pulses;
  uint32_t PulsesPerSecond;
  uint32_t PulsesPerSecondMax;
  uint32_t PulsesPerSecondAvg;
  uint32_t State;
}CipedMeasurement;

typedef struct CipedControlPoint_t
{
  uint8_t OpResultCode;
  uint32_t Parameter;
}CipedControlPoint;

enum class CipedControlPointOpCodes_e:uint8_t
{
  Reserved = 0,
  SetPulsesPerRevolution,
  ResetCounter,  
  SetLight,
  SetAlarm,
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
  virtual uint8_t ControlPointWritten(CipedControlPointOpCodes_e opCode,uint32_t parameter);
};

class CipedService:public BLECharacteristicCallbacks,public BLEDescriptorCallbacks,public BLEServerCallbacks
{
  private:
  BLECharacteristic _CipedMeasurementCharacteristic;
  BLECharacteristic _CipedControlPointCharacteristic;
  BLEServer *_Server;
  BLEService *_Service;
  CipedServiceEvents* _Listener;

  uint32_t _LastTick;
  uint32_t _LastTick500;
  uint32_t _LastTick250;
  CipedMeasurement _CipedMeasurement;
  bool _DeviceConnected;
  uint32_t _State;
  bool _Toggle;

  
  void processCipedControlPoint(uint8_t* data);
  void processCipedControlPointSetPulsesPerRevolution(CipedControlPoint* data);
  void processCipedControlResetCounter(CipedControlPoint* data);
  void processCipedControlPointSetLight(CipedControlPoint* data);
  void processCipedControlPointSetAlarm(CipedControlPoint* data);
  void processCipedControlPointResponse(CipedControlPoint* data);

  void sendCipedMeasurementCharacteristicValue(CipedMeasurement* cipedMeasurement);

public:
  CipedService( BLEServer *pServer,CipedServiceEvents* listener);
  ~CipedService(); 
  void start();
  void stop();
  void process();

  void setCipedData(CipedMeasurement* cipedMeasurement);
  

//Implements

void onConnect(BLEServer* pServer);
void onDisconnect(BLEServer* pServer);

void onWrite(BLECharacteristic *pCharacteristic);
void onStatus(BLECharacteristic* pCharacteristic, Status s, uint32_t code) ;
void onRead(BLECharacteristic* pCharacteristic, esp_ble_gatts_cb_param_t* param);
void onNotify(BLECharacteristic* pCharacteristic);

void onRead(BLEDescriptor* pDescriptor);
void onWrite(BLEDescriptor* pDescriptor);
    
};