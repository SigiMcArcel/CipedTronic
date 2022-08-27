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
	//set Direction
	DDRB = 0x00;
	DDRC = 0x00;
	DDRD = 0x00;
	DDRE = 0x00;
	DDRF = 0x00;
}

void GPIOSetDirection(uint8_t pin,volatile uint8_t* port,uint8_t direction)
{
	if(direction == GPIO_DIR_IN)
	{
		*port &= ~(1<<pin);
	}
	else
	{
		*port |= (1<<pin);
	}
	
}
void GPIOSet(uint8_t pin,volatile uint8_t* port)
{
	
	*port |= (1<<pin);
}
void GPIOReset(uint8_t pin,volatile uint8_t* port)
{

	*port &= ~(1<<pin);
}
void GPIOToggle(uint8_t pin,volatile uint8_t* port)
{
	if(toggleState)
	{
		GPIOSet(pin,port);
		toggleState = 0;
	}
	else
	{
		GPIOReset(pin,port);
		toggleState = 1;
	}
}

uint8_t GPIOGet(uint8_t pin,volatile uint8_t*  port)
{
	
	return *port &(1<<pin);
}