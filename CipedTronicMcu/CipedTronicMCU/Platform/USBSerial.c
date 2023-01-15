/*
 * USBSerial.c
 *
 * Created: 22.06.2022 20:15:54
 *  Author: Siegwart
 */ 

#include "USBSerial.h"
#include "USBCDC.h"

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

void USBSerialInit(void)
{
	m_usb_init();
}

void USBSerialPutchar(const char c)
{
	m_usb_tx_char(c);
}

char USBSerialGetchar(void)
{
	if(m_usb_rx_available())
	{
		return m_usb_rx_char();
	}
	return -1;
}

int  USBSerialGets(char *str,char endDelemiter)
{
	char c = USBSerialGetchar();
	
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

void USBSerialPuts(const char *str)
{
	while(*str != 0)
	{
		USBSerialPutchar(*str);
		str++;
	}
}

void USBSerialPutsHex8(uint8_t val)
{
	char str[3] = {0,0,0};
	uint8_t nibbleH = val >> 4;
	uint8_t nibbleL = val & 0x0f;
	str[0] = NibbleToChar(nibbleH);
	str[1] = NibbleToChar(nibbleL);
	USBSerialPuts(str);
}

void USBSerialPutsHex32(uint32_t val)
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
	
	USBSerialPuts(str);
}

void USBSerialPutsLong(uint32_t val)
{
	char str[20];
	ultoa(val,str,10);
	USBSerialPuts(str);
}

void USBSerialPutsByteArray(uint8_t* array,uint8_t len )
{
	int i = 0;
	if(array == 0)
	{
		return;
	}
	for(i = 0;i < len;i++)
	{
		USBSerialPuts("0x");
		USBSerialPutsHex8(array[i]);
		USBSerialPuts(" ");
	}
	USBSerialPuts("\r\n");
}

uint8_t USBSerialConnected()
{
	return m_usb_isconnected();
}