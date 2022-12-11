/*
 * EEprom.c
 *
 * Created: 25.06.2022 12:55:40
 *  Author: Siegwart
 */ 

#include <avr/io.h>
#include <avr/interrupt.h>
#include "EEProm.h"


void EEPROMWrite(uint16_t address, uint8_t data)
{
	/* Wait for completion of previous write */
	while(EECR & (1<<EEPE))
	;
	/* Set up address and Data Registers */
	EEAR = address;
	EEDR = data;
	/* Write logical one to EEMPE */
	EECR |= (1<<EEMPE);
	/* Start eeprom write by setting EEPE */
	EECR |= (1<<EEPE);
}

uint8_t EEPROMRead(uint16_t address)
{
	/* Wait for completion of previous write */
	while(EECR & (1<<EEPE))
	;
	/* Set up address register */
	EEAR = address;
	/* Start eeprom read by writing EERE */
	EECR |= (1<<EERE);
	/* Return data from Data Register */
	return EEDR;
}

void EEPROMWrite32(uint16_t address, uint32_t data)
{
	EEPROMWrite(address,data & 0xff);
	EEPROMWrite(address + 1,(data>>8) & 0xff);
	EEPROMWrite(address + 2,(data >> 16) & 0xff);
	EEPROMWrite(address + 3,(data >> 24) & 0xff);
}

uint32_t EEPROMRead32(uint16_t address)
{
	uint32_t data = 0;
	data = ((uint32_t)EEPROMRead(address));
	data |= ((uint32_t)EEPROMRead(address + 1)) << 8;
	data |= ((uint32_t)EEPROMRead(address + 2)) << 16;
	data |= ((uint32_t)EEPROMRead(address + 3)) << 24;
	return data;
}

void EEPROMWriteChar(uint16_t address, char* data,int32_t len)
{
	int16_t count = 0;
	for(count = address;count < len + address;count++)
	{
		EEPROMWrite(address,(uint8_t)data[count]);
	}
}

void EEPROMReadChar(uint16_t address, char* data,int32_t len)
{
	int16_t count = 0;
	for(count = address;count < len + address;count++)
	{
		data[count] = (char)EEPROMRead(address);
	}
	
}