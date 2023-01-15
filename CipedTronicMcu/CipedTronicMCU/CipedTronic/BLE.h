/*
 * BLE.h
 *
 * Abstraction class for BLE Stack
 * Handles all needed stuff for BLE.
 * Because the BLE stack is written in C++, BLE 
 * also works as a separation from the rest of the project, which is written in C
 * Author: Marcel Siegwart
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
/// Callback type for received data
/// </summary>
/// <param name="pipeNumber">Pipenumer as defined in ...sService.h that is receiving data</param>
/// <param name="value">reeived value</param>
/// <returns> </returns>
typedef void (*BLERxCallback_t)(uint8_t pipeNumber, uint32_t value);

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
/// Register a value for sending over Notification to the client.
/// The notifications will send when  BLESendNotification is called 
/// A maximum of 10 values ??can be registered
/// </summary>
/// <param name="pipeNumber">characteristic for send</param>
/// <param name="value for send</param>
/// <returns>0 = OK,-1 = value pointer not valid,-2 max registered values exceeded, -3 pipeNumber not exists,</returns>
int8_t BLERegisterNotificationValue(uint8_t pipeNumber, uint32_t* value);

/// <summary>
/// Send a the notification with all registered values once
/// </summary>
/// <returns></returns>
void BLESendNotification();
	
/// <summary>
/// gets the actual state of BLE stack
/// </summary>
/// <returns>aci_evt_opcode_t</returns>
aci_evt_opcode_t BLEGetState(void);

/// <summary>
/// gets is ble is connected
/// </summary>
/// <returns>1 for connected</returns>
uint8_t BLEIsReady();

#ifdef __cplusplus
}
#endif

#endif /* BLE_H_ */