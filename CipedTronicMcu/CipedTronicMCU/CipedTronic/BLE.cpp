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
 * BLE.c
 *
 * Handles all needed stuff for BLE.
 * Because the BLE stack is written in C++, BLE
 * also works as a separation from the rest of the project, which is written in C
 * Author: Marcel Siegwart
 * Created: 10.07.2022 15:17:13
 *  Author: Siegwart
 */ 

#include "BLE.h"
#include <stdint.h>
#include <stdlib.h>
#include <avr/pgmspace.h>
#include <string.h>
#include "utility/ble_system.h"
#include "utility/lib_aci.h"
#include "utility/aci_setup.h"


#ifdef __cplusplus
extern "C" {
	#endif
#include "../Platform/Serial.h"
#include "../Platform/Timer.h"
#include "../Platform/EEProm.h"
#ifdef __cplusplus
}
#endif

static bool				_DebugMode = 1;
static uint16_t			_AdvTimeout = 0;
static uint16_t			_AdvInterval = 80;
static char				_DeviceName[8] = "";
static char				_SystemID[12] = "CIPEDTRONIC";
static char				_SoftwareVersion[10] = "";
static aci_evt_opcode_t _CurrentStatus = ACI_EVT_DISCONNECTED;
static int8_t			_IsConnected = 0;
static BLERxCallback_t	_RxCallback = NULL;

//Initialize Nordic BLE Stack
#ifdef SERVICES_PIPE_TYPE_MAPPING_CONTENT
static services_pipe_type_mapping_t
services_pipe_type_mapping[NUMBER_OF_PIPES] = SERVICES_PIPE_TYPE_MAPPING_CONTENT;
#else
#define NUMBER_OF_PIPES 0
static services_pipe_type_mapping_t * services_pipe_type_mapping = 0;
#endif

// Store the setup for the nRF8001 in the flash of the AVR to save on RAM 
static const hal_aci_data_t setup_msgs[NB_SETUP_MESSAGES] PROGMEM = SETUP_MESSAGES_CONTENT;

static struct aci_state_t _AciState;            // ACI state data 
static hal_aci_evt_t  _AciData;                 // Command buffer 
static bool _TimingChangeDone = false;


//Notification handling

#define MAX_PIPE_COUNT 10

typedef struct TxDataPipe_T //Max registered Values for notifications
{
	uint32_t* Value;
	uint8_t Pipe;
}TxDataPipe;

typedef struct TxHandling_t
{
	int8_t SendBusy;
	int8_t Start;
	int8_t PipeCount;
	int8_t PipeCountSend;
	TxDataPipe Pipes[MAX_PIPE_COUNT];
}TxHandling;
	
static TxHandling _TxHandling = {0};
	
static void BLETxHandler(); //Handles Notifications
static void BLERxHandler(uint8_t characteristic, uint8_t* value,uint8_t len); //Handles received data

//publics
int8_t BLERegisterNotificationValue(uint8_t characteristic, uint32_t* value)
{
	if(value == 0)
	{
		return -1;
		
	}
	if(_TxHandling.PipeCount >= MAX_PIPE_COUNT) 
	{
		return -2;
	}
	if(characteristic > NUMBER_OF_PIPES) //defined in ...service.h
	{
		return -3;
	}
	_TxHandling.Pipes[_TxHandling.PipeCount].Value = value;
	_TxHandling.Pipes[_TxHandling.PipeCount].Pipe = characteristic;
	_TxHandling.PipeCount++;
	
}


void BLESendNotification()
{
	_TxHandling.Start = 1;
	
}

void BLEInit(uint16_t advTimeout, uint16_t advInterval,const char * deviceName,const char* softwareVersion,BLERxCallback_t rxCallback)
{
	char systemID[12] = "";
	_AdvTimeout = advTimeout;
	_AdvInterval = _AdvInterval;
	_RxCallback = rxCallback;
	//set theh devicename
	memcpy(_DeviceName, deviceName,strlen(deviceName)+1);
	_DeviceName[7] = 0; 
	strcpy(_SoftwareVersion, softwareVersion);
	
	//BLE Stack Setup
	if (0 != services_pipe_type_mapping)
	{
		_AciState.aci_setup_info.services_pipe_type_mapping = &services_pipe_type_mapping[0];
	}
	else
	{
		_AciState.aci_setup_info.services_pipe_type_mapping = 0;
	}
	_AciState.aci_setup_info.number_of_pipes    = NUMBER_OF_PIPES;
	_AciState.aci_setup_info.setup_msgs         = (hal_aci_data_t*)setup_msgs;
	_AciState.aci_setup_info.num_setup_msgs     = NB_SETUP_MESSAGES;

	lib_aci_debug_print(false);
	/* Pass the service data into the appropriate struct in the ACI */
	lib_aci_init(&_AciState);
}

void BLEProcess(void)
{
	hal_aci_tl_poll_rdy_line(0); //get nordic chip comm
	if (lib_aci_event_get(&_AciState, &_AciData))
	{
		aci_evt_t * aci_evt;

		aci_evt = &_AciData.evt;
		switch(aci_evt->evt_opcode)
		{
			case ACI_EVT_DEVICE_STARTED:
			{
				SerialPuts("ACI_EVT_DEVICE_STARTED\r\n");
				_AciState.data_credit_total = aci_evt->params.device_started.credit_available;
				switch(aci_evt->params.device_started.device_mode)
				{
					case ACI_DEVICE_SETUP:
					SerialPuts("ACI_DEVICE_SETUP\r\n");
					/* Device is in setup mode! */
					if (ACI_STATUS_TRANSACTION_COMPLETE != do_aci_setup(&_AciState))
					{
						if (_DebugMode) {
							SerialPuts("Error in ACI Setup\r\n");
						}
					}
					break;

					case ACI_DEVICE_STANDBY:
					/* Start advertising ... first value is advertising time in seconds, the */
					/* second value is the advertising interval in 0.625ms units */
					SerialPuts("ACI_DEVICE_STANDBY\r\n");
					if (_SoftwareVersion[0] != 0x00)
					{
						lib_aci_set_local_data(&_AciState, PIPE_DEVICE_INFORMATION_SOFTWARE_REVISION_STRING_SET , (uint8_t *)&_SoftwareVersion, strlen(_SoftwareVersion));
					}
					if (_DeviceName[0] != 0x00)
					{
						SerialPuts("Bluetooth Name: ");
						SerialPuts(_DeviceName);
						SerialPuts("\r\n");
						lib_aci_set_local_data(&_AciState, PIPE_GAP_DEVICE_NAME_SET , (uint8_t *)&_DeviceName, strlen(_DeviceName));
					}
					lib_aci_connect(_AdvTimeout, _AdvInterval);
					
				}
			}
			break;

			case ACI_EVT_CMD_RSP:
			/* If an ACI command response event comes with an error -> stop */
			if (ACI_STATUS_SUCCESS != aci_evt->params.cmd_rsp.cmd_status)
			{
				// ACI ReadDynamicData and ACI WriteDynamicData will have status codes of
				// TRANSACTION_CONTINUE and TRANSACTION_COMPLETE
				// all other ACI commands will have status code of ACI_STATUS_SUCCESS for a successful command
				if (_DebugMode) {
					SerialPuts("ACI Command ");
					SerialPutsHex8(aci_evt->params.cmd_rsp.cmd_opcode);
					SerialPuts(" Evt Cmd respone: Error. Arduino is in an while(1); loop\r\n");
				}
				while (1);
			}
			if (ACI_CMD_GET_DEVICE_VERSION == aci_evt->params.cmd_rsp.cmd_opcode)
			{
				SerialPuts("ACI_CMD_GET_DEVICE_VERSION\r\n");
				// Store the version and configuration information of the nRF8001 in the Hardware Revision String Characteristic
				lib_aci_set_local_data(&_AciState, PIPE_DEVICE_INFORMATION_HARDWARE_REVISION_STRING_SET,
				(uint8_t *)&(aci_evt->params.cmd_rsp.params.get_device_version), sizeof(aci_evt_cmd_rsp_params_get_device_version_t));
			}
			break;

			case ACI_EVT_CONNECTED:
			_AciState.data_credit_available = _AciState.data_credit_total;
			_IsConnected = 1;
			
			lib_aci_device_version();
			SerialPuts("ACI_EVT_CONNECTED ");
			SerialPutsLong(_AciState.data_credit_available);
			SerialPuts("\r\n");
			case ACI_EVT_PIPE_STATUS:
			
			if (lib_aci_is_pipe_available(&_AciState, PIPE_CIPEDTRONIC_BIKE_SERVICE_VELOCITY_TX) && (false == _TimingChangeDone))
			{
				lib_aci_change_timing_GAP_PPCP(); // change the timing on the link as specified in the nRFgo studio -> nRF8001 conf. -> GAP.
				// Used to increase or decrease bandwidth
				_TimingChangeDone = true;
			}
			SerialPuts("ACI_EVT_PIPE_STATUS ");
			SerialPutsHex8(_AciState.pipes_open_bitmap[0]);
			SerialPuts("\r\n");
			break;
			
			case ACI_EVT_TIMING:
			/* Link connection interval changed */
			SerialPuts("ACI_EVT_TIMING\r\n");
			break;

			case ACI_EVT_DISCONNECTED:
			/* Restart advertising ... first value is advertising time in seconds, the */
			/* second value is the advertising interval in 0.625ms units */
			_IsConnected = 0;
			SerialPuts("ACI_EVT_DISCONNECTED\r\n");
		
			lib_aci_connect(_AdvTimeout, _AdvInterval);
			break;

			case ACI_EVT_DATA_RECEIVED:
			SerialPuts("ACI_EVT_DATA_RECEIVED len = ");
			SerialPutsLong(aci_evt->len);
			SerialPuts(" data = ");
			SerialPutsByteArray(aci_evt->params.data_received.rx_data.aciData,aci_evt->len);
			BLERxHandler(aci_evt->params.data_received.rx_data.pipe_number,aci_evt->params.data_received.rx_data.aciData,aci_evt->len -2);
			break;
			
			case ACI_EVT_BOND_STATUS:
			SerialPuts("ACI_EVT_BOND_STATUS\r\n");
			
			break;
			
			case ACI_EVT_DATA_ACK:
			SerialPuts("ACI_EVT_DATA_ACK\r\n");
			
			break;

			case ACI_EVT_DATA_CREDIT:
			_TxHandling.SendBusy = 0;
			_AciState.data_credit_available = _AciState.data_credit_available + aci_evt->params.data_credit.credit;
			break;

			case ACI_EVT_PIPE_ERROR:
			/* See the appendix in the nRF8001 Product Specication for details on the error codes */
			if (_DebugMode) {
				SerialPuts("ACI Evt Pipe Error: Pipe #:");
				SerialPutsLong(aci_evt->params.pipe_error.pipe_number);
				SerialPuts("  Pipe Error Code: 0x");
				SerialPutsHex8(aci_evt->params.pipe_error.error_code);
				SerialPuts("\r\n");
			}

			/* Increment the credit available as the data packet was not sent */
			_AciState.data_credit_available++;
			break;
		}
	}
	else
	{
		// Serial.println(F("No ACI Events available"));
		// No event in the ACI Event queue and if there is no event in the ACI command queue the arduino can go to sleep
		// Arduino can go to sleep now
		// Wakeup from sleep from the RDYN line
	}
	BLETxHandler();
}



aci_evt_opcode_t BLEGetState(void)
{
	return _CurrentStatus;
}

uint8_t BLEIsReady()
{
	return _IsConnected;
}

//private
static void BLETxHandler()
{
	//send queue works not properly, so wait till ACI_EVT_DATA_CREDIT says message sent (Send busy)
	if(!_IsConnected)
	{
		_TxHandling.PipeCountSend = 0;
		_TxHandling.Start = 0;
		_TxHandling.SendBusy = 0;
		return;
	}
	if ((_AciState.data_credit_available == 0) ||  _TxHandling.SendBusy)
	{
		return;
	}
	
	if(!_TxHandling.Start)
	{
		return;
	}
	
	if(lib_aci_is_pipe_available(&_AciState,_TxHandling.Pipes[_TxHandling.PipeCountSend].Pipe))
	{
		_TxHandling.SendBusy = 1;
		lib_aci_send_data(_TxHandling.Pipes[_TxHandling.PipeCountSend].Pipe,(uint8_t*) _TxHandling.Pipes[_TxHandling.PipeCountSend].Value,4);
		_AciState.data_credit_available--;
	}
	
	_TxHandling.PipeCountSend += 1;
	if( _TxHandling.PipeCountSend >= _TxHandling.PipeCount)
	{
		_TxHandling.PipeCountSend = 0;
		_TxHandling.Start = 0;
	}
}

static void BLERxHandler(uint8_t characteristic, uint8_t* value,uint8_t len)
{
	switch(characteristic)
	{
		case PIPE_DEVICE_INFORMATION_SYSTEM_ID_RX:
		{
			memcpy(_SystemID,value,11);
			EEPROMWriteChar(EEPROM_ADR_SYSTEM_ID,_SystemID,EEPROM_LEN_SYSTEM_ID);
			break;
		}
		default:
		{	
			if(_RxCallback != NULL)
			{
				if(len == sizeof(uint32_t))
				{
					_RxCallback(characteristic, *((uint32_t*)value));
				}
			}
		}
	}
}

