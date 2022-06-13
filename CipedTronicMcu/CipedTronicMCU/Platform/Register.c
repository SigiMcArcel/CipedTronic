/*
 * Register.c
 *
 * Created: 17.04.2022 21:52:56
 *  Author: Siegwart
 */ 
#include <stdint.h>

#include "Register.h"
#include "UART.h"

//<[Access][Address][Lenght][Values]>
//'<W000100> ,<R0001> 


#define MINCOMMAMDLEN 6
#define MAXCOMMAMDLEN 40
#define MAXTMPLEN 5
#define  MAXVALUELEN
#include <stdio.h>
#include <stdlib.h>

volatile uint8_t Registers[MAXREGISTER] = {0};
static char str[MAXCOMMAMDLEN + 1] = "";
static char strTmp[MAXTMPLEN] = "";
static volatile char cmd = 0;
static volatile uint8_t adr = 0;
static volatile uint8_t len = 0;
uint8_t getLen(void);
uint8_t getValue(uint8_t index);
uint8_t getAdr(void);





uint8_t getAdr(void)
{
	strTmp[0] = str[2];
	strTmp[1] = str[3];
	strTmp[2] = 0;
	return (uint8_t)strtoul(strTmp,NULL,16);
}

uint8_t getLenght(void)
{
	strTmp[0] = str[4];
	strTmp[1] = str[5];
	strTmp[2] = 0;
	return (uint8_t)strtoul(strTmp,NULL,16);
}

uint8_t getValue(uint8_t index)
{
	int i = index << 1;
	strTmp[0] = str[6 + i];
	strTmp[1] = str[7  + i];
	strTmp[3] = 0;
	return (uint8_t)strtoul(strTmp,NULL,16);
}

void RegisterInit(void)
{
	Registers[REGISTER_VERSION] = 0x01;
	Registers[REGISTER_MAX] = MAXREGISTER;
}

void RegisterHandler(void)
{
	int index = 0;
	int result = UARTGets(str,'>');
	
	if((result > 0) && (result > MINCOMMAMDLEN))
	{
		cmd = str[1];
		adr = getAdr();
		len = getLenght();
		
		if(len > MAXREGISTER)
		{
			UARTPuts("<E000004>");
			return;
		}
		
		switch(cmd)
		{
			case 'W':
			{
				for(index = 0;index < len;index++)
				{
					Registers[adr + index] = getValue(index);
				}
				UARTPuts("<W");
				UARTPutsHex(adr);
				UARTPutsHex(index);
				UARTPuts(">");
				break;
			}
			case 'R':
			{
				UARTPuts("<R");
				UARTPutsHex(adr);
				UARTPutsHex(len);
				for(index = 0;index < len;index++)
				{
					UARTPutsHex(Registers[adr + index]);
				}
				UARTPuts(">");
				break;
			}
			default:
			{
				UARTPuts("<E000002>");
			}
		}
	}
	else
	{
		if((result > 0) && (result < MINCOMMAMDLEN))
		{
			
			UARTPuts("<E000003>");
		}
	}
}