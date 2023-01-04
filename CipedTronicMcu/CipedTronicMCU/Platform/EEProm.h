/*
 * EEProm.h
 *
 * Created: 25.06.2022 12:55:15
 *  Author: Siegwart
 */ 


#ifndef EEPROM_H_
#define EEPROM_H_

#define EEPROM_ADR_COUNTER 0
#define EEPROM_LEN_COUNTER 4
#define EEPROM_ADR_COUNTER_MAX 4
#define EEPROM_LEN_COUNTER_MAX 4
#define EEPROM_ADR_COUNTER_AVG 8
#define EEPROM_LEN_COUNTER_AVG 4
#define EEPROM_ADR_SYSTEM_ID 12
#define EEPROM_LEN_SYSTEM_ID 12


void EEPROMWrite32(uint32_t address, uint32_t data);
uint32_t EEPROMRead32(uint32_t address);
void EEPROMReadChar(uint16_t address, char* data,int32_t len);
void EEPROMWriteChar(uint16_t address, char* data,int32_t len);
#endif /* EEPROM_H_ */