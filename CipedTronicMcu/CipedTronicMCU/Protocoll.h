/*
 Copyright (c) 2022 Marcel Siegwart

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be included
 in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */

/*
* Protocoll.h
*
* Created: 25.12.2022 14:14:00
* Author: Siegwart
* 
* A serial protocol for writing and reading registers for any purposes of configuring and control.
* The registers except the Versions Register are Read/write registers
* microcontrollers with a serial other interfaces.
* It's base of a Request <> Event communication and is string based
* Data's are Type of UINT32 as HexString
* 
* <Format:>
* 
* Request:
* <[Prefix] [ID] [Request] [Register number] [Data lenght] [Data] ...>
* 
* Data Event:
* <[Prefix] [ID] [Event] [Register number] [Data lenght] [Data] ...>
* 
* State Event
* <[Prefix] [ID] [Event] [State] [Additional Info]
* 
* Requests:
* DW : Write Register	Writes data to register
* DR : Read Register	Reads data from register
* 
* Events:
* DE : Data Event -> Will send as response of Data Read request or on cyclic register notification
* SE : State Event -> Will send on Write Register request or errors
* 
* Mandatory Registers:
* 0 ->		Protocol Version as UINT32
* 1-2 ->	Protocol ID as String[8]
* 3			Flags Bit 0 = set encrypion keys, Bit 1-7 reserved
* 4			Notification start register
* 5			Notification number of register
* 6			Notification Intervall in milliseconds (notification start when intervall > 0)
* 7-19		Reserved
* 20..		Custom
* 
*/

#ifndef PROTOCOL_H_
#define PROTOCOL_H_
#ifdef __cplusplus
extern "C" {
#endif

//Configurations
#define PROTOCOL_MAX_DATA_SIZE 10	//Max Parameters (Data) per telegramm
#define PROTOCOL_MAX_ID_LEN 9		//Max char for ID + 1
#define PROTOCOL_MAX_CMD_LEN 3		//Max char for Command + 1
#define PROTOCOL_MAX_KEY_LEN 5		//Encryption Key + 1
#define PROTOCOL_MAX_PACKAGE_LEN 63 //Max telegramm lenght inclusive data
#define PROTOCOL_PREFIX "XXX"		//telegramm Prefix
#define PROTOCOL_MAX_REGISTER 50    //Max Register Array of UINT32
#define PROTOCOL_VERSION (uint32_t)0x00000001

//states
#define PROTOCOL_ERROR_OK 0x00
#define PROTOCOL_ERROR_FORMAT 0x01
#define PROTOCOL_ERROR_NOTIMPLEMENTED 0x02
#define PROTOCOL_ERROR_REGISTER_NUMBER 0x03
#define PROTOCOL_ERROR_ID_NOT_MATCH 0x04
#define PROTOCOL_ERROR_ACCESS 0x05
#define PROTOCOL_ERROR_KEY_NOT_MATCH 0x06
#define PROTOCOL_ERROR_DATASIZE 0x07
#define PROTOCOL_ERROR_NO_NOTIFICATION 0xFF 

	typedef enum ProtocolAccessMode_t
	{
		READWRITE = 0,
		READONLY = 1,
		WRITEONLY = 2
	}ProtocolAccessMode;

	typedef struct RegisterEntry_t
	{
		ProtocolAccessMode Access;
		uint32_t Data;
	}RegisterEntry;

#define PROTOCOL_REGISTER_VERSION 0x00
#define PROTOCOL_REGISTER_ID_LOW 0x01
#define PROTOCOL_REGISTER_ID_HIGH 0x02
#define PROTOCOL_REGISTER_FLAGS 0x03
#define PROTOCOL_REGISTER_NOTIFY_START 0x04
#define PROTOCOL_REGISTER_NOTIFY_NUM 0x05
#define PROTOCOL_REGISTER_NOTIFY_INTERVALL 0x06
#define PROTOCOL_REGISTER_KEY_OLD 0x07
#define PROTOCOL_REGISTER_KEY_NEW 0x08

#define PROTOCOL_REGISTER_FLAG_SETKEY 0x01

	static RegisterEntry ProtocolRegisters[PROTOCOL_MAX_REGISTER] =
	{
		{READONLY,PROTOCOL_VERSION},//Version
		{READWRITE,0x00000000},//ID[0][1][2][3]
		{READWRITE,0x00000000},//ID[4][5][6][7]
		{READWRITE,0x00000000},//Flags
		{READWRITE,0x00000000},//Notification start register
		{READWRITE,0x00000000},//Notification number of register
		{READWRITE,0x00000000},//Notification interval in milliseconds(notification start when interval > 0)
		{WRITEONLY,0x00000000},//Encryption KeyOld[0][1][2][3]
		{WRITEONLY,0x00000000},//Encryption KeyNew[4][5][6][7]
	};
	

	//Call cyclic
	void ProtocolHandler();
	

#ifdef __cplusplus
}
#endif
#endif /* PROTOCOLL_H_ */
