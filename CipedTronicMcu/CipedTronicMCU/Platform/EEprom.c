/*
 * EEprom.c
 *
 * Created: 25.06.2022 12:55:40
 *  Author: Siegwart
 */ 

#include <avr/io.h>
#include <avr/interrupt.h>
#include "EEProm.h"


void EEPROM_write(uint16_t Address, uint8_t data)
{
	/* Wait for completion of previous write */
	while(EECR & (1<<EEPE))
	;
	/* Set up address and Data Registers */
	EEAR = Address;
	EEDR = data;
	/* Write logical one to EEMPE */
	EECR |= (1<<EEMPE);
	/* Start eeprom write by setting EEPE */
	EECR |= (1<<EEPE);
}

uint8_t EEPROM_read(uint16_t Address)
{
	/* Wait for completion of previous write */
	while(EECR & (1<<EEPE))
	;
	/* Set up address register */
	EEAR = Address;
	/* Start eeprom read by writing EERE */
	EECR |= (1<<EERE);
	/* Return data from Data Register */
	return EEDR;
}

void EEPROM_write32(uint16_t Address, uint32_t data)
{
	EEPROM_write(Address,data & 0xff);
	EEPROM_write(Address + 1,(data>>8) & 0xff);
	EEPROM_write(Address + 2,(data >> 16) & 0xff);
	EEPROM_write(Address + 3,(data >> 24) & 0xff);
}

uint32_t EEPROM_read32(uint16_t Address)
{
	uint32_t data = 0;
	data = ((uint32_t)EEPROM_read(Address));
	data |= ((uint32_t)EEPROM_read(Address + 1)) << 8;
	data |= ((uint32_t)EEPROM_read(Address + 2)) << 16;
	data |= ((uint32_t)EEPROM_read(Address + 3)) << 24;
	return data;
}