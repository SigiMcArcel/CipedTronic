/* Copyright 2023 Marcel Siegwart
* 
* Permission is hereby granted, free of charge, 
* to any person obtaining a copy of this software and associated documentation files (the "Software"), 
* to deal in the Software without restriction, including without limitation the rights to use, copy, 
* modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, 
* and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included 
* in all copies or substantial portions of the Software.
*
* Neither the name of the copyright holders nor the names of
* contributors may be used to endorse or promote products derived
* from this software without specific prior written permission.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
* OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

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