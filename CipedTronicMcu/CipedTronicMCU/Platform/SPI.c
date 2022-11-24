/*
 * SPI.c
 *
 * Created: 09.07.2022 20:48:56
 *  Author: Siegwart
 */ 

#include "SPI.h"
#include "Platform/GPIO.h"

void SPIWriteByte(uint8_t data)


{
	SPDR = data;
	while(!(SPSR & (1<<SPIF))){}
}

uint8_t SPIReadByte(void)
{
	return SPDR;
}

void SPIInit(uint8_t cpol,uint8_t cpha,uint8_t clkDiv,uint8_t dataOrder, uint8_t poll)
{
	SPCR = 0;
	GPIOSetDirection(2,&DDRB,GPIO_DIR_OUT);
	GPIOSetDirection(1,&DDRB,GPIO_DIR_OUT);
	GPIOReset(1,&PORTB);
	GPIOReset(2,&PORTB);
	//clockdiv
	SPCR = clkDiv;
	//No Double speed
	SPDR &= ~(1 << SPI2X);
	if(cpha)
	{
		SPCR |= (1 << CPHA);
	}
	
	if(cpol)
	{
		SPCR |= (1 << CPOL);
		
	}
	
	//master
	SPCR |= (1 << MSTR);
	
	
	if(dataOrder)
	{
		SPCR |= (1 << DORD);
	}
	
	//Enable
	SPCR |= (1 << SPE);
	//interrupt Enable
	if(!poll)
	{
		SPCR |= (1 << SPIE);
	}
}

int8_t SPIWrite(uint8_t *buffer, uint16_t size)
{
	uint8_t cnt = 0;
	for(cnt = 0;cnt < size;cnt++)
	{
		SPIWriteByte(buffer[cnt]);
	}
	return 0;
}
int8_t SPIRead(uint8_t *buffer, uint16_t size)
{
	uint8_t result = 0;
	uint16_t cnt = 0;
	for(cnt = 0;cnt < size;cnt++)
	{
		uint8_t d = SPIReadByte();
		if(result >= 0)
		{
			buffer[cnt] = d;
		}
		else
		{
			return -1;
		}
		
	}
	return cnt;
}

int8_t SPITransfer(uint8_t *txbuffer,uint8_t *rxbuffer, uint16_t size)
{
	uint8_t cnt = 0;
	for(cnt = 0;cnt < size;cnt++)
	{
		SPIWriteByte(rxbuffer[cnt]);
		rxbuffer[cnt] = SPIReadByte();
	}
	return cnt;
}
