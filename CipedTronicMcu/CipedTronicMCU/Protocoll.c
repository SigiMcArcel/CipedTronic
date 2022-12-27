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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include "Protocol.h"
#include "Encryption.h"
#include "BLE.h"

#ifdef __cplusplus
extern "C" {
#endif
	typedef struct Command_t
	{
		char Id[PROTOCOL_MAX_ID_LEN];
		char Cmd[PROTOCOL_MAX_CMD_LEN];
		uint8_t RegisterNumber;
		uint8_t DataLen;
		uint32_t Data[PROTOCOL_MAX_DATA_SIZE];
	}Command;

#define PROTOCOL_ACCESS_MODE_RO 1
#define PROTOCOL_ACCESS_MODE_RW 2
#define PROTOCOL_ACCESS_MODE_WO 3

	

	static char _Id[10] = "CIPEDEMP";
	static char _Key[10] = "";

	static char _SendString[PROTOCOL_MAX_PACKAGE_LEN] = "";
	static char _ReceiveString[PROTOCOL_MAX_PACKAGE_LEN] = "";
	static Command _ReceivedCommand = { 0 };
	static volatile uint32_t _LastTimerTick = 0;

	
	

	static char ProtocolNibbleToChar(uint8_t nibble);
	static void ProtocolHex32ToA(char* str, uint32_t val);
	static int8_t  ProtocolGets(char* str, char endDelemiter);
	static int8_t ProtocolPuts(const char* str);

	static void ProtocolCreateSendString(char* sendString, Command* command);
	static void ProtocolCreateDataEvent(uint8_t regNumber, uint8_t len);
	static void ProtocolCreateStateEvent(uint8_t stateNumber, const char* stateCmd);
	static int8_t ProtocolGetHeader(const char* token, Command* cmd);
	static int8_t ProcessWriteData(Command* command);
	static int8_t ProcessReadData(Command* command);
	static int8_t ProtocolSetEncryptionKey(const char* txt, Command* command);
	static int8_t ProtocolGetCmd();
	static uint32_t ProtocolTimerGetTick();
	static int8_t ProtocolRegisterWritten(uint8_t startNumber, uint8_t len);
	static int8_t ProtocolProcessFlags(uint32_t flags);
	static void ProtocolRxHandler();

	

	
	//privates


	static char ProtocolNibbleToChar(uint8_t nibble)
	{
		char c = 0;
		if (nibble <= 9)
		{
			c = (char)nibble + '0';
		}
		else
		{
			c = (char)nibble + 'A' - 0x0a;
		}
		return c;
	}

	static void ProtocolHex32ToA(char* str, uint32_t val)
	{
		str[0] = ProtocolNibbleToChar((uint8_t)(val >> 28) & 0x0000000f);
		str[1] = ProtocolNibbleToChar((uint8_t)(val >> 24) & 0x0000000f);
		str[2] = ProtocolNibbleToChar((uint8_t)(val >> 20) & 0x0000000f);
		str[3] = ProtocolNibbleToChar((uint8_t)(val >> 16) & 0x0000000f);
		str[4] = ProtocolNibbleToChar((uint8_t)(val >> 12) & 0x0000000f);
		str[5] = ProtocolNibbleToChar((uint8_t)(val >> 8) & 0x0000000f);
		str[6] = ProtocolNibbleToChar((uint8_t)(val >> 4) & 0x0000000f);
		str[7] = ProtocolNibbleToChar((uint8_t)val & 0x0000000f);
	}

	//Platform call for TimerGetTick;
	static uint32_t ProtocolTimerGetTick()
	{
		return GetTickCount();
	}

	/// <summary>
	/// Platform call for serial get
	/// Received Rx chars till delemiter is received [out]
	/// Non Blocking function returns -1 if no char is received.
	/// </summary>
	/// <param name="str">Buffer for received string's without delemiter</param>
	/// <param name="endDelemiter">delemiter waiting for</param>
	/// <returns>-1 No chars, > 0 string completed </returns>
	static int8_t  ProtocolGets(char* str, char endDelemiter)
	{
		return (int8_t)BLEGets(str, endDelemiter);
	}

	/// <summary>
	/// Platform call for 
	/// </summary>
	/// <param name="str"></param>
	/// <returns></returns>
	static int8_t ProtocolPuts(const char* str)
	{
		return (int8_t)BLEPuts(str);
	}
	

	static void ProtocolCreateSendString(char* sendString, Command* command)
	{
		uint8_t dataCount = 0;
		char enCrypted[9] = "";
		memset(_SendString, 0, PROTOCOL_MAX_PACKAGE_LEN);
		strcpy(sendString, "<");
		strcat(sendString, PROTOCOL_PREFIX);
		strcat(sendString, " ");
		strcat(sendString, command->Id);
		strcat(sendString, " ");
		strcat(sendString, command->Cmd);
		
		for (dataCount = 0; dataCount < command->DataLen; dataCount++)
		{
			char dataString[9] = "";
			strcat(sendString, " ");
			ProtocolHex32ToA(dataString, command->Data[dataCount]);
			
			if (_Key[0] != 0)
			{
				Encrypt(enCrypted, dataString, _Key);
			}
			else
			{
				strcpy(enCrypted, dataString);
			}
			strcat(sendString, enCrypted);
			
		}
		strcat(sendString, ">");
	}

	static void ProtocolCreateDataEvent(uint8_t regNumber, uint8_t len)
	{
		uint8_t paramCount = 0;
		Command cmd = { {""},{"DE"},0,0,{0} };
		strcpy(cmd.Id, _Id);
		cmd.DataLen = len;
		cmd.Data[0] = regNumber;
		cmd.Data[1] = len;
		for (paramCount = 0; paramCount < len; paramCount++)
		{
			cmd.Data[paramCount] = ProtocolRegisters[paramCount + regNumber].Data;
		}
		ProtocolCreateSendString(_SendString, &cmd);
		ProtocolPuts(_SendString);
	}

	static void ProtocolCreateStateEvent(uint8_t stateNumber, const char* stateCmd)
	{
		Command cmd = { {""},{"SE"},0,0,{0} };
		strcpy(cmd.Id, _Id);
		cmd.DataLen = 2;
		cmd.Data[0] = *((uint8_t*)stateCmd);
		cmd.Data[1] = stateNumber;
		ProtocolCreateSendString(_SendString, &cmd);
		ProtocolPuts(_SendString);
	}

	static int8_t ProcessWriteData(Command* command)
	{
		uint8_t paramCount = 0;
		
		if (command->RegisterNumber + command->DataLen > PROTOCOL_MAX_REGISTER)
		{
			return PROTOCOL_ERROR_REGISTER_NUMBER;
		}
		if (ProtocolRegisters[paramCount + command->RegisterNumber].Access == READONLY)
		{
			return PROTOCOL_ERROR_ACCESS;
		}
		for (paramCount = 0; paramCount < command->DataLen; paramCount++)
		{
			ProtocolRegisters[paramCount + command->RegisterNumber].Data = command->Data[paramCount];
		}
		return PROTOCOL_ERROR_OK;
	}

	static int8_t ProcessReadData(Command* command)
	{
		uint8_t paramCount = 0;
		char encrypted[9] = "";

		if (command->RegisterNumber + command->DataLen > PROTOCOL_MAX_REGISTER)
		{
			return PROTOCOL_ERROR_REGISTER_NUMBER;
		}
		if (ProtocolRegisters[paramCount + command->RegisterNumber].Access == WRITEONLY)
		{
			return PROTOCOL_ERROR_ACCESS;
		}
		ProtocolCreateDataEvent(command->RegisterNumber, command->DataLen);
		return PROTOCOL_ERROR_NO_NOTIFICATION;
	}

	static int8_t ProtocolSetEncryptionKey(const char* txt, Command* command)
	{
		uint32_t state = 0;
		char oldkey[9] = "";
		char newkey[9] = "";
		//to implement
		return PROTOCOL_ERROR_FORMAT;
	}

	static int8_t ProtocolGetHeader(const char* delemiter,Command* cmd)
	{
		char* token;
		token = strtok(NULL, delemiter);
		if (token == 0)
		{
			return PROTOCOL_ERROR_FORMAT;
		}
		cmd->RegisterNumber = (uint8_t)strtoul(token, NULL, 16);
		token = strtok(NULL, delemiter);
		if (token == 0)
		{
			return PROTOCOL_ERROR_FORMAT;
		}
		cmd->DataLen = (uint8_t)strtoul(token, NULL, 16);
		return PROTOCOL_ERROR_OK;
	}

	static int8_t ProtocolGetCmd()
	{
		char deCrypted[9] = "";
		int8_t dataCount = 0;
		int8_t result = 0;
		uint8_t count = 0;
		const char delemiter[2] = " ";
		char* token;

		if (ProtocolGets(_ReceiveString, '>') > 0)
		{
			//remove delemiter at the end of string
			_ReceiveString[strlen(_ReceiveString) - 1] = 0;
			//check format
			if (strlen(_ReceiveString) < 5)
			{
				return PROTOCOL_ERROR_FORMAT;
			}
			if (strncmp(_ReceiveString, "<" PROTOCOL_PREFIX " ",5) != 0)
			{
				return PROTOCOL_ERROR_FORMAT;
			}
			
			token = strtok(_ReceiveString, delemiter);

			/* walk through other tokens */
			while (token != NULL)
			{
				count++;
				token = strtok(NULL, delemiter);
				if (count <= 2 && token == 0)
				{
					return PROTOCOL_ERROR_NO_NOTIFICATION;
				}
				switch (count)
				{
				case 1:
				{
					strncpy(_ReceivedCommand.Id, token, 8);
					if (strncmp(_ReceivedCommand.Id, _Id, 8) != 0)
					{
						return PROTOCOL_ERROR_ID_NOT_MATCH;
					}
					break;
				}
				case 2:
				{
					strncpy(_ReceivedCommand.Cmd, token, 2);
				}
				default:
				{
					dataCount = 0;
					result = ProtocolGetHeader(delemiter,&_ReceivedCommand);
					
					if (result != PROTOCOL_ERROR_OK)
					{
						return result;
					}
					if (_ReceivedCommand.DataLen > PROTOCOL_MAX_DATA_SIZE)
					{
						return PROTOCOL_ERROR_DATASIZE;
					}

					if (strcmp(_ReceivedCommand.Cmd, "DW") == 0)
					{
						for (dataCount = 0; dataCount < _ReceivedCommand.DataLen; dataCount++)
						{
							token = strtok(NULL, delemiter);
							if (token == 0)
							{
								return PROTOCOL_ERROR_FORMAT;
							}
							if (dataCount > PROTOCOL_MAX_DATA_SIZE) 
							{
								return PROTOCOL_ERROR_DATASIZE;
							}
							if (_Key[0] == 0)
							{
								strcpy(deCrypted, token);
							}
							else
							{
								Decrypt(deCrypted, token, _Key);
							}
							
							_ReceivedCommand.Data[dataCount] = strtoul(deCrypted, NULL, 16);
						}
						result =  ProcessWriteData(&_ReceivedCommand);
						if (result == 0)
						{
							result = ProtocolRegisterWritten(_ReceivedCommand.RegisterNumber, _ReceivedCommand.DataLen);
						}
						return result;
					}
					else if (strcmp(_ReceivedCommand.Cmd, "DR") == 0)
					{
						return ProcessReadData(&_ReceivedCommand);
					}
				else
				{
					return PROTOCOL_ERROR_FORMAT;
				}

				}//default
				}//switch
			}//while
		}//if (ProtocolGets(_ReceiveString, '>') != -1)
		return -1;
	}

	static void ProtocolRxHandler()
	{
		int32_t result = ProtocolGetCmd();
		if (result > -1)
		{
			ProtocolCreateStateEvent(result, _ReceivedCommand.Cmd);
		}

	}

	static int8_t ProtocolProcessFlags(uint32_t flags)
	{
		if ((flags & PROTOCOL_REGISTER_FLAG_SETKEY) > 0)
		{
			if (strncmp((char*)&ProtocolRegisters[PROTOCOL_REGISTER_KEY_OLD].Data, _Key, 4) != 0)
			{
				return PROTOCOL_ERROR_KEY_NOT_MATCH;
			}
			strncpy(_Key, (char*)&ProtocolRegisters[PROTOCOL_REGISTER_KEY_NEW].Data, 4);
		}
		return PROTOCOL_ERROR_OK;
	}
	static int8_t ProtocolRegisterWritten(uint8_t startNumber, uint8_t len)
	{
		int8_t result = 0;
		switch (startNumber)
		{
		case PROTOCOL_REGISTER_FLAGS:
		{
			result = ProtocolProcessFlags(ProtocolRegisters[PROTOCOL_REGISTER_FLAGS].Data);
			ProtocolRegisters[PROTOCOL_REGISTER_FLAGS].Data = 0;
			return result;
		}
		}
		return result;
	}
	//publics
	void ProtocolHandler()
	{
		ProtocolRxHandler();
		if (ProtocolRegisters[2].Data && ProtocolRegisters[3].Data && ProtocolRegisters[4].Data)
		{
			uint32_t tick = ProtocolTimerGetTick();
			uint32_t diff = tick - _LastTimerTick;
			
			if (diff >= ProtocolRegisters[4].Data)
			{
				ProtocolCreateDataEvent(ProtocolRegisters[2].Data, ProtocolRegisters[3].Data);
				_LastTimerTick = tick;
			}
		}
		if (ProtocolRegisters[5].Data)
		{
			
			ProtocolRegisters[5].Data = 0;
		}

		

	}

	

#ifdef __cplusplus
}
#endif