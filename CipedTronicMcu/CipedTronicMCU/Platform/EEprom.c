/*
 * EEprom.c
 *
 * Created: 25.06.2022 12:55:40
 *  Author: Siegwart
 */ 

#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/eeprom.h>
#include "EEProm.h"


void EEPROMWrite(uint16_t address, uint8_t data)
{
	eeprom_write_byte((uint8_t *)address, data);
}

uint8_t EEPROMRead(uint16_t address)
{
	return eeprom_read_byte ((const uint8_t*)address);	
}

void EEPROMWrite32(uint16_t address, uint32_t data)
{
	eeprom_write_dword((uint32_t *)address, data);
}

uint32_t EEPROMRead32(uint16_t address)
{
	return eeprom_read_dword ((const uint32_t*)address);	
}

void EEPROMWriteChar(uint16_t address, char* data,int32_t len)
{
	int16_t count = 0;
	for(count = address;count < len + address;count++)
	{
		EEPROMWrite(address,data[count]);
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