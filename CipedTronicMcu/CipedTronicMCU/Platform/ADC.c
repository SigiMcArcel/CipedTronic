/*
 * ADC.c
 *
 * Created: 18.04.2022 11:40:25
 *  Author: Siegwart
 */ 
#include "ADC.h"
#include "Register.h"

#define VREF = 0x3FF;
extern uint16_t Registers[];

void ADCInit(void)
{
	ADMUX = (1<<REFS0); 
	ADCSRA = (1<<ADPS1) | (1<<ADPS0);     // Frequenzvorteiler
	ADCSRA |= (1<<ADEN) ;   // ADC und interrupt aktivieren
	
}

uint16_t ADCGet(void)
{
	uint16_t tmp = 0;
	if(!(ADCSRA & (1<<ADSC) )) //wandlung läuft nicht
	{
		tmp  = 1000/0x3ff*ADCW;
		ADCSRA |= (1<<ADSC);
		return tmp;
	}
	return -1;
}

void ADCHandler(void)
{
	uint16_t val = ADCGet() ;
	if(val != -1)
	{
		Registers[REGISTER_ADC] = val; 
	}
}