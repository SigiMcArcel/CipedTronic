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
static gpiocallback_t cbGPIO = 0;
void GPIOInit(void)
{
	//set Direction
	DDRB = 0x00;
	DDRC = 0x00;
	DDRD = 0x00;
	DDRE = 0x00;
	DDRF = 0x00;
	EICRA = 0;
	EICRB = 0;
	EIMSK = 0;
	PCMSK0  = 0;
	PCICR = 0;
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
	
	return *port & (1<<pin);
}

uint8_t GPIOSetInterrupt(gpiocallback_t cb,uint8_t pin,uint8_t mode)
{
	cli();
	cbGPIO = cb;
	
	if(pin == 6)
	{
		EICRB = (mode << ISC60); 
		
	}
	else if(pin < 4)
	{
		EICRA |= (mode << (pin *2)); 
	}
	else
	{
		return 0;
	}
	
	EIMSK |= (1 << pin);
	
	PCICR = 0;
	sei();
	return 1;
}

ISR (INT0_vect)
{
	if(cbGPIO != 0)
	{
		cbGPIO(0);
	}
}
ISR (INT1_vect)
{
	if(cbGPIO != 0)
	{
		cbGPIO(1);
	}
}
ISR (INT2_vect)
{
	if(cbGPIO != 0)
	{
		cbGPIO(2);
	}
}
ISR (INT3_vect)
{
	if(cbGPIO != 0)
	{
		cbGPIO(3);
	}
}
ISR (INT6_vect)
{
	EIFR = 0;
	if(cbGPIO != 0)
	{
		cli();
		cbGPIO(6);
		sei();
	}
}

ISR (PCINT0_vect)
{
	
}


