/*
 * UARTSPI.h
 *
 * Created: 10.07.2022 21:07:49
 *  Author: Siegwart
 */ 


#ifndef UARTSPI_H_
#define UARTSPI_H_

void UARTSPIInit(uint8_t cpol,uint8_t cpha,uint8_t clkDiv,uint8_t dataOrder);
void UARTSPIWriteByte(uint8_t data);
uint8_t UARTSPIReadByte();



#endif /* UARTSPI_H_ */