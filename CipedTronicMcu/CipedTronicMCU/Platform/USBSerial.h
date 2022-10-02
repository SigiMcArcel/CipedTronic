/*
 * USBSerial.h
 *
 * Created: 22.06.2022 20:16:13
 *  Author: Siegwart
 * https://github.com/tewarid/atmega32u4-usb-serial/blob/master/Atmega32u4/USBtoSerial.c
 */ 


#ifndef USBSERIAL_H_
#define USBSERIAL_H_

#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/pgmspace.h>

void USBSerialInit(void);
uint8_t USBSerialConnected();
void USBSerialPutchar(const char c);
char USBSerialGetchar(void);
int  USBSerialGets(char *str,char endDelemiter);
void USBSerialPuts(const char *str);
void USBSerialPutsHex8(uint8_t val);
void USBSerialPutsHex32(uint32_t val);
void USBSerialPutsLong(uint32_t val);


#endif /* USBSERIAL_H_ */