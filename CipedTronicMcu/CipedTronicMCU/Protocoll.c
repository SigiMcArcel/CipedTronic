/*
 * Protocoll.c
 *
 * Created: 07.06.2022 20:29:16
 *  Author: Siegwart
 */ 
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "CipedTronic/BLE.h"
#include "Platform/Counter.h"
#include "Platform/USBSerial.h"
#include "Platform/EEProm.h"
#ifdef __cplusplus
extern "C" {
#endif
	
#define PROTOKOLL_MAX_PARAMETER 5
#define PROTOKOLL_MAX_ID_LEN 8
#define PROTOKOLL_MAX_CMD_LEN 5
#define PROTOKOLL_MAX_KEY_LEN 8
#define PROTOKOLL_MAX_PACKAGE_LEN 63

typedef struct Command_t
{
	char Id[PROTOKOLL_MAX_ID_LEN];
	char Cmd[PROTOKOLL_MAX_CMD_LEN];
	int32_t DataLen;
	uint32_t Data[PROTOKOLL_MAX_PARAMETER];
}Command;

static char _Id[10] = "";
static char _Key[10] = "";
static int _State = 10;
static char _SendString[PROTOKOLL_MAX_PACKAGE_LEN] = "";

static void CreateSendString(char*sendString,Command* command)
{
	int16_t dataCount = 0;
	memset(_SendString,0,PROTOKOLL_MAX_PACKAGE_LEN);
	strcpy(sendString,"<CIP ");
	strcat(sendString,command->Id); 
	strcat(sendString," "); 
	strcat(sendString,command->Cmd);
	
	for(dataCount = 0;dataCount < command->DataLen;dataCount++)
	{
		strcat(sendString," ");
		utoa(command->Data[dataCount],sendString + strlen(sendString),16);	
	}
	strcat(sendString,">");
}

static void ProtokollCreateCipedDataEvent(uint32_t velocity,uint32_t velocityMax,uint32_t velocityAvg,uint32_t distance)
{
	Command cmd = {{"DE"},{""},0,{0}};
	strcpy(cmd.Id,_Id);
	cmd.Data[0] = velocity;
	cmd.Data[1] = velocityMax;
	cmd.Data[2] = velocityAvg;
	cmd.Data[3] = distance;
	CreateSendString(_SendString,&cmd);
}

char getCmd(uint32_t* value)
{
	//if(BLEGets(txt,>) != -1)
	//{
		//
	//}
	return  '0';
}

void ProtocolInit()
{
	EEPROMReadChar(EEPROM_ADR_PROT_ID,_Id,EEPROM_LEN_PROT_ID);
	EEPROMReadChar(EEPROM_ADR_PROT_KEY,_Key,EEPROM_LEN_PROT_KEY);
}

void ProtocolHandler()
{
	//sprintf(txt,"<CIP D %08lX %08lX %08lX>",Counter,CounterPerSecond,MaxCounterPerSecond);
	ProtokollCreateCipedDataEvent(CounterGetCounterPerSecond(),CounterGetMaxCounterPerSecond(),CounterGetAvgCounterPerSecond(),CounterGetCounter());
	BLEPuts(_SendString);
}

void ProtocolRxHandler()
{
	char c = BLEGetChar();
	if(c == (char)-1)
	{
		return;
	}
	
	switch(_State)
	{
		case 10: 
		{
			if(c == '>')
			{
				_State = 20;
			}
			else
			{
				BLEPuts("<CIP E 00000001>");
			}
			break;
		}
		case 20: 
		{
			switch(c)
			{
				case 'A':
				{
					CounterResetCounter();
					BLEPuts("<CIP A 00000000>");
					_State = 10;
					break;
				}
				case 'S':
				{
					
					BLEPuts("<CIP A 00000000>");
					_State = 10;
					break;
				}
				default:
				{
					BLEPuts("<CIP E 00000002>");
					_State = 10;
				}
			}//switch c
			break;
		
		}
	}
}

#ifdef __cplusplus
}
#endif