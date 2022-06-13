
#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/pgmspace.h>
#include "uart.h"
#include "Platform/GPIO.h"

/* Define CPU frequency in Hz in Makefile or toolchain compiler configuration */
#ifndef F_CPU
#error "F_CPU undefined, please define CPU frequency in Hz in Makefile or compiler configuration"
#endif


static volatile int UARTGetsCnt = 0;
char NibbleToChar(uint8_t nibble);

char NibbleToChar(uint8_t nibble)
{
	char c = 0;
	if(nibble <= 9)
	{
		c = (char)nibble + '0';
	}
	else
	{
		c = (char)nibble + 'A' - 0x0a;
	}
	return c;
}

void UARTInit(void)
{
	cli();
	
	//No Parity UPM01 UPM00 = 0
	//asynchrony UART UMSEL01 UMSEL00 = 0
	//stop bit USBS0 = 0
	//8 Bit  UCSZ02 = 0 UCSZ01  = 1 UCSZ00 = 1
	//UCSR0A |= (1 << U2X0);
	UCSR0C |= (1 << UCSZ01)|(1 << UCSZ00) ;
	//Baud rate 38.4k 12 0.2% on 8 Mhz
	
	UCSR0B |= (1<<TXEN0) | (1<<RXEN0);
	UBRR0L = 12;
	UBRR0H = 0;
	sei();
}

void UARTLLPutchar(char c)
{
	UDR0 = c;
}

void UARTPutchar(char c)
{
	while((UCSR0A & (1 << UDRE0)) == 0){};
	UDR0 = c;
}

char UARTGetchar(void)
{
	
	if((UCSR0A & (1 << RXC0)) > 0)
	{
		return (char)UDR0;
	}
	return (char)-1;
	
}

int UARTGets(char *str,char endDelemiter)
{
	char c = UARTGetchar();
	
	if(c == (char)-1)
	{
		return -1;
	}
	str[UARTGetsCnt] = c;
	UARTGetsCnt++;
	if(c == endDelemiter || c == 0)
	{
		str[UARTGetsCnt] = 0;
		int tmp = UARTGetsCnt;
		UARTGetsCnt = 0;
		return tmp;
	}
	return 0;
}

void UARTPuts(char *str)
{
	while(*str != 0)
	{
		UARTPutchar(*str);
		str++;
	}
}


void UARTPutsHex8(uint8_t val)
{
	char str[3] = {0,0,0};
	uint8_t nibbleH = val >> 4;
	uint8_t nibbleL = val & 0x0f;
	str[0] = NibbleToChar(nibbleH);
	str[1] = NibbleToChar(nibbleL);
	UARTPuts(str);
}

void UARTPutsHex32(uint32_t val)
{
	char str[9] = {0,0,0,0,0,0,0,0,0};
	str[0] = NibbleToChar((uint8_t)(val >> 28) & 0x0000000f);
	str[1] = NibbleToChar((uint8_t)(val >> 24) & 0x0000000f);
	str[2] = NibbleToChar((uint8_t)(val >> 20) & 0x0000000f);
	str[3] = NibbleToChar((uint8_t)(val >> 16) & 0x0000000f);
	str[4] = NibbleToChar((uint8_t)(val >> 12) & 0x0000000f);
	str[5] = NibbleToChar((uint8_t)(val >> 8) & 0x0000000f);
	str[6] = NibbleToChar((uint8_t)(val >> 4) & 0x0000000f);
	str[7] = NibbleToChar((uint8_t)val & 0x0000000f);
	
	UARTPuts(str);
}
