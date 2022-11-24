/*
 * Protocoll.c
 *
 * Created: 07.06.2022 20:29:16
 *  Author: Siegwart
 */ 
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "Platform/USBSerial.h"
#include "Platform/Comparator.h"
#include "Platform/Counter.h"


static volatile int _State = 10;
static void ProtocolRxHandler();

void ProtocollHandler()
{
	uint32_t Counter = CounterGetCounter();
	uint32_t CounterPerSecond = CounterGetCounterPerSecond();
	uint32_t MaxCounterPerSecond = CounterGetMaxCounterPerSecond();
	
	USBSerialPuts("<CIP");
	USBSerialPutsHex32(Counter);
	USBSerialPuts(" ");
	USBSerialPutsHex32(CounterPerSecond);
	USBSerialPuts(" ");
	USBSerialPutsHex32(MaxCounterPerSecond);
	USBSerialPuts(">\n");
	ProtocolRxHandler();
}

void ProtocolRxHandler()
{
	char c = USBSerialGetchar();
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
				USBSerialPuts("<Z>\n");
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
					USBSerialPuts("<A>\n");
					_State = 10;
					break;
				}
				default:
				{
					USBSerialPuts("<Z>\n");
					_State = 10;
				}
			}//switch c
			break;
		
		}
	}
}