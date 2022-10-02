/*
 * SPI.c
 *
 * Created: 09.07.2022 20:48:56
 *  Author: Siegwart
 */ 

#include "SPI.h"


int16_t SPIWriteByte(uint8_t data)
{
	if((SPSR & (1<<SPIF)))
	{
		return 0xffff;
	}
	SPDR = data;
	/* Wait for transmission complete */
	return 0;
}

int16_t SPIReadByte(void)
{
	/* Wait for reception complete */
	if(!(SPSR & (1<<SPIF)))
	{
		return 0xffff;
	}
	
	/* Return Data Register */
	return SPDR;
}

void SPIInit(uint8_t cpol,uint8_t cpha,uint8_t clkDiv,uint8_t dataOrder, uint8_t poll)
{
	SPCR = 0;
	
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

int16_t SPIWrite(uint8_t *buffer, uint8_t size)
{
	uint8_t cnt = 0;
	for(cnt = 0;cnt < size;cnt++)
	{
		if(SPIWriteByte(buffer[cnt]) < 0)
		{
			return -1;
		}
	}
	return cnt;
}
int16_t SPIRead(uint8_t *buffer, uint8_t size)
{
	uint8_t cnt = 0;
	for(cnt = 0;cnt < size;cnt++)
	{
		uint8_t d = SPIReadByte();
		if(d >= 0)
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

int16_t SPITransfer(uint8_t *txbuffer,uint8_t *rxbuffer, uint8_t size)
{
	uint8_t cnt = 0;
	for(cnt = 0;cnt < size;cnt++)
	{
		SPIWriteByte(rxbuffer[cnt]);
		rxbuffer[cnt] = SPIReadByte();
	}
	return cnt;
}
