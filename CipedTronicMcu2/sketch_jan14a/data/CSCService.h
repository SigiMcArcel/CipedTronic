#include <string>
#include <BLEDevice.h>

enum class CSCMeasurementFlags_e:uint8_t
{
  None = 0,
  WheelRevolutionDataPresent = 1,
  CrankRevolutionDataPresent = 2
}CSCMeasurementFlags;

typedef struct CSCMeasurement_t
{
  uint8_t Flags;
  uint32_t CumulativeWheelRevolutions;
  uint16_t LastWheelEventTime;
}CSCMeasurement;

enum class CSCFeatures_e
{
    WheelRevolutionDataSupported = 0x0001,
    CrankRevolutionDataSupported = 0x0002,
    MultipleSensorLocationsSupported = 0x0004
}CSCFeatures;
  
enum class SensorLocations_e:uint8_t
{
  Other = 0,
  TopOfShoe,
  InShoe,
  Hip,
  FrontWheel,
  LeftCrank,
  RightCrank,
  LeftPedal,
  RightPedal,
  FrontHub,
  RearDropout,
  Chainstay,
  RearWheel,
  RearHub,
  Chest,
  Spider,
  ChainRing,
  Max=255
}SensorLocations;
  
enum class SCControlPointOpModes_e:uint8_t
{
  Reserved = 0,
  SetCumulativeValue,
  StartCalibration,
  UpdateSensorLocation,
  RequestSupportedSensorLocations,
  ResponseCode = 16 
}SCControlPointOpModes;

enum class SCControlPointResponseValues_e:uint8_t
{
  Reserved = 0,
  Success,
  NotSupported,
  InvalidParameter,
  OperationFailed  
}SCControlPointResponseValues;


class CSCService:public BLECharacteristicCallbacks,public BLEDescriptorCallbacks
{
  private:
  BLECharacteristic _CSCMeasurementCharacteristic;
  BLECharacteristic _CSCFeatureCharacteristic;
  BLECharacteristic _CSCSensorLocationCharacteristic;
  BLECharacteristic _CSCControlPointCharacteristic;
  BLEServer *_Server;
  BLEService *_Service;

  uint16_t _LastWheelEventTime;
  uint32_t _CumulativeWheelRevolutions;
  uint8_t _Flags;
  uint16_t _Features;
  bool _DeviceConnected;
  bool _DeviceConnectedOld = false;

  void processSCControlPoint();
  void processSCControlPointSetCumulativeValue();
  void processSCControlPointStartCalibration();
  void processSCControlPointUpdateSensorLocation();
  void processSCControlPointRequestSupportedSensorLocations();

  void sendCSCMeasurementCharacteristicValue(uint16_t lastWheelEventTime,uint32_t cumulativeWheelRevolutions);

public:
  CSCService(uint16_t features, BLEServer *pServer);
  ~CSCService(); 
  void start();
  void stop();
  void setCSCSData(uint16_t LastWheelEventTime,uint32_t CumulativeWheelRevolutions);

//Implemnts
void onWrite(BLECharacteristic *pCharacteristic);
void onStatus(BLECharacteristic* pCharacteristic, Status s, uint32_t code) ;
void onRead(BLECharacteristic* pCharacteristic, esp_ble_gatts_cb_param_t* param);
void onNotify(BLECharacteristic* pCharacteristic);

void onRead(BLEDescriptor* pDescriptor);
void onWrite(BLEDescriptor* pDescriptor);
    
};