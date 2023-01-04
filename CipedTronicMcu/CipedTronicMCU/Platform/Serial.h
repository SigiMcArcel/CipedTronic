/*
 * Serial.h
 *
 * Created: 04.01.2023 22:03:11
 *  Author: Siegwart
 */ 


#ifndef SERIAL_H_
#define SERIAL_H_
#include "USBSerial.h"

#define SerialInit(x) USBSerialInit() 
#define SerialPuts(x) USBSerialPuts(x) 
#define SerialPutsHex8(x) USBSerialPutsHex8(x) 
#define SerialPutsHex32(x)  USBSerialPutsHex32(x)
#define SerialPutsLong(x) USBSerialPutsLong(x)
#define SerialPutsByteArray(x,y) USBSerialPutsByteArray(x,y)




#endif /* SERIAL_H_ */