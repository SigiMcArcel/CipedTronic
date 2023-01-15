/*
 * EEProm.h
 *
 * Reading and writing EEPROM
 * Created: 25.06.2022 12:55:15
 *  Author: Siegwart
 */ 


#ifndef EEPROM_H_
#define EEPROM_H_

#define EEPROM_BYTE_SIZE 1024
//EPPROM Mapping
#define EEPROM_ADR_COUNTER 0
#define EEPROM_LEN_COUNTER 4
#define EEPROM_ADR_COUNTER_MAX 4
#define EEPROM_LEN_COUNTER_MAX 4
#define EEPROM_ADR_COUNTER_AVG 8
#define EEPROM_LEN_COUNTER_AVG 4
#define EEPROM_ADR_SYSTEM_ID 12
#define EEPROM_LEN_SYSTEM_ID 12

/// <summary>
/// Writes uint32 to EEPROM
/// </summary>
/// <param name="address">byte address</param>
/// <param name="val"<value>/param>
/// <return>-1 = invalid address</return>
int8_t EEPROMWrite32(uint16_t address, uint32_t data);

/// <summary>
/// Reads uint32 from EEPROM
/// </summary>
/// <param name="address">byte address</param>
/// <param name="val">value</param>
/// <return>The value or 0 on invlaid address</return>
uint32_t EEPROMRead32(uint16_t address);

/// <summary>
/// Reads chars from EEPROM
/// </summary>
/// <param name="address">byte address</param>
/// <param name="data">the string</param>
/// <param name="len">lenght</param>
/// <return>-1 = invalid address</return>
int8_t EEPROMReadChar(uint16_t address, char* data,int32_t len);

/// <summary>
/// Writes char to EEPROM
/// </summary>
/// <param name="address">byte address</param>
/// <param name="data">the string</param>
/// <param name="len">lenght</param>
/// <return>-1 = invalid address</return>
int8_t EEPROMWriteChar(uint16_t address, char* data,int32_t len);
#endif /* EEPROM_H_ */