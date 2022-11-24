/*
 * BLE.c
 *
 * Created: 10.07.2022 15:17:13
 *  Author: Siegwart
 */ 

#include "BLE.h"
#include <avr/pgmspace.h>
#include <string.h>
#include "utility/ble_system.h"
#include "utility/lib_aci.h"
#include "utility/aci_setup.h"


#ifdef __cplusplus
extern "C" {
	#endif
#include "../Platform/USBSerial.h"
#include "../Platform/Timer.h"
#ifdef __cplusplus
}
#endif

bool         debugMode = 1;
uint16_t     adv_timeout;
uint16_t     adv_interval = 80;
char         device_name[8];
aci_evt_opcode_t currentStatus;
uint8_t isReady = 0;

#ifdef SERVICES_PIPE_TYPE_MAPPING_CONTENT
static services_pipe_type_mapping_t
services_pipe_type_mapping[NUMBER_OF_PIPES] = SERVICES_PIPE_TYPE_MAPPING_CONTENT;
#else
#define NUMBER_OF_PIPES 0
static services_pipe_type_mapping_t * services_pipe_type_mapping = 0;
#endif

/* Length of the buffer used to store flash strings temporarily when printing. */
#define PRINT_BUFFER_SIZE 20

/* Store the setup for the nRF8001 in the flash of the AVR to save on RAM */
static const hal_aci_data_t setup_msgs[NB_SETUP_MESSAGES] PROGMEM = SETUP_MESSAGES_CONTENT;

static struct aci_state_t aci_state;            /* ACI state data */
static hal_aci_evt_t  aci_data;                 /* Command buffer */
static bool timing_change_done = false;

// This is the Uart RX buffer, which we manage internally when data is available!
#define ADAFRUIT_BLE_UART_RXBUFFER_SIZE 64
uint8_t adafruit_ble_rx_buffer[ADAFRUIT_BLE_UART_RXBUFFER_SIZE];
volatile uint16_t adafruit_ble_rx_head;
volatile uint16_t adafruit_ble_rx_tail;

void BLEInit(uint16_t advTimeout, uint16_t advInterval)
{
	if (0 != services_pipe_type_mapping)
	{
		aci_state.aci_setup_info.services_pipe_type_mapping = &services_pipe_type_mapping[0];
	}
	else
	{
		aci_state.aci_setup_info.services_pipe_type_mapping = 0;
	}
	aci_state.aci_setup_info.number_of_pipes    = NUMBER_OF_PIPES;
	aci_state.aci_setup_info.setup_msgs         = (hal_aci_data_t*)setup_msgs;
	aci_state.aci_setup_info.num_setup_msgs     = NB_SETUP_MESSAGES;

lib_aci_debug_print(false);
	/* Pass the service data into the appropriate struct in the ACI */
	lib_aci_init(&aci_state);
	
}
void BLEProcess(void)
{
	hal_aci_tl_poll_rdy_line(0);
	if (lib_aci_event_get(&aci_state, &aci_data))
	{
		aci_evt_t * aci_evt;

		aci_evt = &aci_data.evt;
		switch(aci_evt->evt_opcode)
		{
			/* As soon as you reset the nRF8001 you will get an ACI Device Started Event */
			case ACI_EVT_DEVICE_STARTED:
			{
				USBSerialPuts("ACI_EVT_DEVICE_STARTED\r\n");
				aci_state.data_credit_total = aci_evt->params.device_started.credit_available;
				switch(aci_evt->params.device_started.device_mode)
				{
					case ACI_DEVICE_SETUP:
					USBSerialPuts("ACI_DEVICE_SETUP\r\n");
					/* Device is in setup mode! */
					if (ACI_STATUS_TRANSACTION_COMPLETE != do_aci_setup(&aci_state))
					{
						if (debugMode) {
							USBSerialPuts("Error in ACI Setup\r\n");
						}
					}
					
					break;

					case ACI_DEVICE_STANDBY:
					/* Start advertising ... first value is advertising time in seconds, the */
					/* second value is the advertising interval in 0.625ms units */
					if (device_name[0] != 0x00)
					{
						USBSerialPuts("ACI_DEVICE_STANDBY\r\n");
						USBSerialPuts(device_name);
						/* Update the device name */
						lib_aci_set_local_data(&aci_state, PIPE_GAP_DEVICE_NAME_SET , (uint8_t *)&device_name, strlen(device_name));
					}
					lib_aci_connect(adv_timeout, adv_interval);
					isReady = 1;
					//USBSerialPuts("ACI_EVT_DEVICE_STARTED\r\n");
					//defaultACICallback(ACI_EVT_DEVICE_STARTED);
					//if (aci_event)
					//aci_event(ACI_EVT_DEVICE_STARTED);
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
				if (debugMode) {
					USBSerialPuts("ACI Command ");
					USBSerialPutsHex8(aci_evt->params.cmd_rsp.cmd_opcode);
					USBSerialPuts(" Evt Cmd respone: Error. Arduino is in an while(1); loop\r\n");
				}
				while (1);
			}
			if (ACI_CMD_GET_DEVICE_VERSION == aci_evt->params.cmd_rsp.cmd_opcode)
			{
				USBSerialPuts("ACI_CMD_GET_DEVICE_VERSION\r\n");
				// Store the version and configuration information of the nRF8001 in the Hardware Revision String Characteristic
				lib_aci_set_local_data(&aci_state, PIPE_DEVICE_INFORMATION_HARDWARE_REVISION_STRING_SET,
				(uint8_t *)&(aci_evt->params.cmd_rsp.params.get_device_version), sizeof(aci_evt_cmd_rsp_params_get_device_version_t));
			}
			break;

			case ACI_EVT_CONNECTED:
			aci_state.data_credit_available = aci_state.data_credit_total;
			/* Get the device version of the nRF8001 and store it in the Hardware Revision String */
			lib_aci_device_version();
			USBSerialPuts("ACI_EVT_CONNECTED\r\n");
			//defaultACICallback(ACI_EVT_CONNECTED);
			//if (aci_event)
			//aci_event(ACI_EVT_CONNECTED);

			case ACI_EVT_PIPE_STATUS:
			if (lib_aci_is_pipe_available(&aci_state, PIPE_UART_OVER_BTLE_UART_TX_TX) && (false == timing_change_done))
			{
				lib_aci_change_timing_GAP_PPCP(); // change the timing on the link as specified in the nRFgo studio -> nRF8001 conf. -> GAP.
				// Used to increase or decrease bandwidth
				timing_change_done = true;
			}
			USBSerialPuts("ACI_EVT_PIPE_STATUS\r\n");
			break;
			
			case ACI_EVT_TIMING:
			/* Link connection interval changed */
			break;

			case ACI_EVT_DISCONNECTED:
			/* Restart advertising ... first value is advertising time in seconds, the */
			/* second value is the advertising interval in 0.625ms units */

			USBSerialPuts("ACI_EVT_DISCONNECTED\r\n");
		
			lib_aci_connect(adv_timeout, adv_interval);

			
			break;

			case ACI_EVT_DATA_RECEIVED:
			//defaultRX(aci_evt->params.data_received.rx_data.aci_data, aci_evt->len - 2);
			//if (rx_event)
			//rx_event(aci_evt->params.data_received.rx_data.aci_data, aci_evt->len - 2);
			break;

			case ACI_EVT_DATA_CREDIT:
			aci_state.data_credit_available = aci_state.data_credit_available + aci_evt->params.data_credit.credit;
			break;

			case ACI_EVT_PIPE_ERROR:
			/* See the appendix in the nRF8001 Product Specication for details on the error codes */
			if (debugMode) {
				USBSerialPuts("ACI Evt Pipe Error: Pipe #:");
				USBSerialPutsLong(aci_evt->params.pipe_error.pipe_number);
				USBSerialPuts("  Pipe Error Code: 0x");
				USBSerialPutsHex8(aci_evt->params.pipe_error.error_code);
				USBSerialPuts("\r\n");
			}

			/* Increment the credit available as the data packet was not sent */
			aci_state.data_credit_available++;
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
}

int BLEAvailable(void)
{
	return (uint16_t)(ADAFRUIT_BLE_UART_RXBUFFER_SIZE + adafruit_ble_rx_head - adafruit_ble_rx_tail)
	% ADAFRUIT_BLE_UART_RXBUFFER_SIZE;
}

int BLERead(void)
{
	// if the head isn't ahead of the tail, we don't have any characters
	if (adafruit_ble_rx_head == adafruit_ble_rx_tail) {
		return -1;
		} else {
		unsigned char c = adafruit_ble_rx_buffer[adafruit_ble_rx_tail];
		adafruit_ble_rx_tail ++;
		adafruit_ble_rx_tail %= ADAFRUIT_BLE_UART_RXBUFFER_SIZE;
		return c;
	}
}

int BLEPeek(void)
{
	if (adafruit_ble_rx_head == adafruit_ble_rx_tail) {
		return -1;
		} else {
		return adafruit_ble_rx_buffer[adafruit_ble_rx_tail];
	}
}

int8_t BLEPuts(const char* str)
{
	int8_t bytesThisPass, sent = 0;
	int8_t len = strlen(str) + 1;
	/* Blocking delay waiting for available credit(s) */
	while (0 == aci_state.data_credit_available)
	{
		BLEProcess();
		TimerWait(10);
	}

	

	while(len) { // Parcelize into chunks
		bytesThisPass = len;
		if(bytesThisPass > ACI_PIPE_TX_DATA_MAX_LEN)
		bytesThisPass = ACI_PIPE_TX_DATA_MAX_LEN;

		if(!lib_aci_is_pipe_available(&aci_state, PIPE_UART_OVER_BTLE_UART_TX_TX))
		{
			BLEProcess();
			continue;
		}

		lib_aci_send_data(PIPE_UART_OVER_BTLE_UART_TX_TX,(uint8_t*) &str[sent],
		bytesThisPass);
		aci_state.data_credit_available--;

		TimerWait(35); // required delay between sends

		if(!(len -= bytesThisPass)) break;
		sent += bytesThisPass;
	}

	return sent;
}
int  BLEGets(char *str,char endDelemiter)
{
	
}

aci_evt_opcode_t BLEGetState(void)
{
	return currentStatus;
}

void BLESetDeviceName(const char * deviceName)
{
	if (strlen(deviceName) > 7)
	{
		/* String too long! */
		return;
	}
	else
	{
		memcpy(device_name, deviceName, strlen(deviceName)+1);
	}
}

uint8_t BLEIsReady()
{
	return isReady;
}