/*
 * GPIO.c
 *
 * Created: 17.04.2022 10:58:57
 *  Author: Siegwart
 */ 
#include <avr/io.h>
#include <avr/interrupt.h>
#include "GPIO.h"

static int toggleState = 0;
void GPIOInit(void)
{
	DDRB = 0x01;
	DDRC = 0x00;
	DDRD = 0x20;
}
void GPIOBSet(uint8_t pin)
{
	PORTD |= (1<<pin);
}
void GPIOBReset(uint8_t pin)
{
	PORTD &= ~(1<<pin);
}
void GPIOBToggle(uint8_t pin)
{
	if(toggleState)
	{
		GPIOBSet(pin);
		toggleState = 0;
	}
	else
	{
		GPIOBReset(pin);
		toggleState = 1;
	}
}

uint8_t GPIOBGet(uint8_t pin)
{
	return PORTB &(1<<pin);
}