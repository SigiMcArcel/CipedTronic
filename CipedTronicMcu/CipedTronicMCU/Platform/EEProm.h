/*
 * EEProm.h
 *
 * Created: 25.06.2022 12:55:15
 *  Author: Siegwart
 */ 


#ifndef EEPROM_H_
#define EEPROM_H_


void EEPROM_write32(uint16_t Address, uint32_t data);
uint32_t EEPROM_read32(uint16_t Address);



#endif /* EEPROM_H_ */