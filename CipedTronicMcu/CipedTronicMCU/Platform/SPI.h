/*
 * SPI.h
 *
 * Created: 09.07.2022 20:49:11
 * Author: Siegwart
 * 
 * SPI functions for ATMEGA 32u4
 * SPI will handled with polling
 */ 


#ifndef SPI_H_
#define SPI_H_
#include <stdint.h>

//Polarity
#define SPI_CPOLNEG 1
#define SPI_CPOLPOS 0
//Phase
#define SPI_CPHARAISING 1
#define SPI_CPHAFALLING 0
//Data order
#define SPI_LSB 1
#define SPI_MSB 0
//clock
#define SPI_CLKDIV_4 0
#define SPI_CLKDIV_16 1
#define SPI_CLKDIV_64 2
#define SPI_CLKDIV_128 3


/// <summary>
/// Setup SPI
/// </summary>
/// <param name="polarity">see defines</param>
/// <param name="phase">see defines</param>
/// <param name="clkMhz">see defines</param>
/// <param name="dataOrder">see defines</param>
/// <param name="dataOrder">see defines</param>
void SPIInit(uint8_t polarity,uint8_t phase,uint8_t clkMhz,uint8_t dataOrder);
/// <summary>
/// Writes a byte blocking
/// </summary>
/// <param name="data"></param>
void SPIWriteByte(uint8_t data);
/// <summary>
/// Reads a byte blocking
/// </summary>
/// <param name="data"></param>
/// <returns>Value</returns>
uint8_t SPIReadByte(void);
/// <summary>
/// Writes bytes blocking
/// </summary>
/// <param name="data">databuffer</param>
/// <param name="size">size</param>
/// <returns>Value</returns>
int8_t SPIWrite(uint8_t *buffer, uint16_t size);
/// <summary>
/// Writes bytes blocking
/// </summary>
/// <param name="data">databuffer</param>
/// <param name="size">size</param>
/// <returns>Count</returns>
int8_t SPIRead(uint8_t *buffer, uint16_t size);
/// <summary>
/// Transfer bytes blocking
/// </summary>
/// <param name="txbuffer">datab for send</param>
/// <param name="txbuffer">received data</param>
/// <param name="size">size</param>
/// <returns>Count</returns>
int8_t SPITransfer(uint8_t *txbuffer,uint8_t *rxbuffer, uint16_t size);
#endif /* SPI_H_ */