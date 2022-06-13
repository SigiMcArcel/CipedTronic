/*
 * ADC.h
 *
 * Created: 18.04.2022 11:40:37
 *  Author: Siegwart
 */ 
#ifndef ADC_H_
#define ADC_H_
#include <avr/io.h>
#include <avr/interrupt.h>


void ADCInit(void);
uint16_t ADCGet(void);
void ADCHandler(void);

#endif /* ADC_H_ */