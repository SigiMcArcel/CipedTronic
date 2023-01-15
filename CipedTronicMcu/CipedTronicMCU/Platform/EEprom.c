/* Copyright 2023 Marcel Siegwart
* 
* Permission is hereby granted, free of charge, 
* to any person obtaining a copy of this software and associated documentation files (the "Software"), 
* to deal in the Software without restriction, including without limitation the rights to use, copy, 
* modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, 
* and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included 
* in all copies or substantial portions of the Software.
*
* Neither the name of the copyright holders nor the names of
* contributors may be used to endorse or promote products derived
* from this software without specific prior written permission.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
* OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

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

int8_t EEPROMWrite(uint16_t address, uint8_t data)
{
	if(address >= EEPROM_BYTE_SIZE)
	{
		return -1;
	}
	eeprom_write_byte((uint8_t *)address, data);
	return 0;
}

uint8_t EEPROMRead(uint16_t address)
{
	if(address >= EEPROM_BYTE_SIZE)
	{
		return 0;
	}
	return eeprom_read_byte ((const uint8_t*)address);	
}

int8_t EEPROMWrite32(uint16_t address, uint32_t data)
{
	if(address + sizeof(uint32_t) >= EEPROM_BYTE_SIZE)
	{
		return -1;
	}
	eeprom_write_dword((uint32_t *)address, data);
	return 0;
}

uint32_t EEPROMRead32(uint16_t address)
{
	if(address + sizeof(uint32_t) >= EEPROM_BYTE_SIZE)
	{
		return -1;
	}
	return eeprom_read_dword ((const uint32_t*)address);	
}

int8_t EEPROMWriteChar(uint16_t address, char* data,int32_t len)
{
	int16_t count = 0;
	if(address + len >= EEPROM_BYTE_SIZE)
	{
		return -1;
	}
	for(count = address;count < len + address;count++)
	{
		EEPROMWrite(address,data[count]);
	}
	return 0;
}

int8_t EEPROMReadChar(uint16_t address, char* data,int32_t len)
{
	int16_t count = 0;
	if(address + len >= EEPROM_BYTE_SIZE)
	{
		return -1;
	}
	for(count = address;count < len + address;count++)
	{
		data[count] = (char)EEPROMRead(address);
	}
	return 0;
}