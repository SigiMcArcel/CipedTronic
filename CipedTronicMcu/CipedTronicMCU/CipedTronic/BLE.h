/*
 * BLE.h
 *
 * Created: 10.07.2022 15:16:53
 *  Author: Siegwart
 */ 


#ifndef BLE_H_
#define BLE_H_

#include "utility/hal_platform.h"
#include "utility/aci.h"
#include "utility/aci_evts.h"
#include "utility/services/CipedTronicServices.h"

#ifdef __cplusplus
extern "C" {
#endif


/// <summary>
/// Callback for received data
/// </summary>
/// <param name="characteristic">Characteristis zhat is receiving data</param>
/// <param name="value">reeived value</param>
/// <returns> </returns>
typedef void (*BLERxCallback_t)(uint8_t characteristic, uint32_t value);

/// <summary>
/// Initialization
/// </summary>
/// <param name="advTimeout">advertisement Timeout</param>
/// <param name="advInterval">advertisement interval </param>
/// <param name="deviceName">sets the Bluetooth LE device name </param>
/// <param name="deviceName">sets the Software Version </param>
/// <param name="rxCallback">sets the rx Callback function</param>
/// <returns></returns>
void BLEInit(uint16_t advTimeout, uint16_t advInterval,const char * deviceName,const char* softwareVersion,BLERxCallback_t rxCallback);

/// <summary>
/// Process all needed bluetooth le functions. Must called cyclic.
/// </summary>

/// <returns></returns>
void BLEProcess(void);

/// <summary>
/// Send a value to host
/// </summary>
/// <param name="characteristic">characteristic for send</param>
/// <param name="value for send</param>
/// <returns>True is ok</returns>
int8_t BLERegisterPipeValue(uint8_t characteristic, uint32_t* value);

/// <summary>
/// Send a the notification values once
/// </summary>

/// <returns>True is ok</returns>
void BLESendNotification();
	
/// <summary>
/// gets the actual state of ble stack
/// </summary>
/// <returns>aci_evt_opcode_t</returns>
aci_evt_opcode_t BLEGetState(void);

/// <summary>
/// gets is ble is connected
/// </summary>
/// <returns>aci_evt_opcode_t</returns>
uint8_t BLEIsReady();

#ifdef __cplusplus
}
#endif

#endif /* BLE_H_ */