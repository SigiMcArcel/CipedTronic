/*
 * UARTSPI.c
 *
 * Created: 10.07.2022 21:08:14
 *  Author: Siegwart
 */ 
#include "UARTSPI.h"

#define UCPHA1 1
#define UDORD1 2
void UARTSPIInit(uint8_t cpol,uint8_t cpha,uint8_t clkDiv,uint8_t dataOrder)
{
	UBRR1 = 0;
	/* Setting the XCKn port pin as output, enables
	master mode. */
	DDRD |= (1<<PIND4);
	/* Set MSPI mode of operation and SPI data mode 0.
	*/
	UCSR1C = (1<<UMSEL11)|(1<<UMSEL10)|(1<<UDORD1)|(0<<UCPHA1)|(0<<UCPOL1);
	/* Enable receiver and transmitter. */
	UCSR1B = (1<<RXEN1)|(1<<TXEN1);
	/* Set baud rate. */
	/* IMPORTANT: The Baud Rate must be set after the
	transmitter is enabled */
	UBRR1 = 0;
}

void UARTSPIWriteByte(uint8_t data)
{
	
}

uint8_t UARTSPIReadByte()
{
	
}
