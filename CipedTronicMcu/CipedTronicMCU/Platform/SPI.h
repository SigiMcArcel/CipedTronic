/*
 * SPI.h
 *
 * Created: 09.07.2022 20:49:11
 *  Author: Siegwart
 */ 


#ifndef SPI_H_
#define SPI_H_

#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/pgmspace.h>

#define SPI_CPOLNEG 1
#define SPI_CPOLPOS 0
#define SPI_CPHARAISING 1
#define SPI_CPHAFALLING 0
#define SPI_LSB 1
#define SPI_MSB 0
#define SPI_CLKDIV_4 0
#define SPI_CLKDIV_16 1
#define SPI_CLKDIV_64 2
#define SPI_CLKDIV_128 3

void SPIInit(uint8_t cpol,uint8_t cpha,uint8_t clkMhz,uint8_t dataOrder,uint8_t poll);
int16_t SPIWriteByte(uint8_t data);
int16_t SPIReadByte(void);
int16_t SPIWrite(uint8_t *buffer, uint8_t size);
int16_t SPIRead(uint8_t *buffer, uint8_t size);
int16_t SPITransfer(uint8_t *txbuffer,uint8_t *rxbuffer, uint8_t size);




#endif /* SPI_H_ */