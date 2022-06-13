/*
 * Timer.c
 *
 * Created: 17.04.2022 10:37:32
 *  Author: Siegwart
 */ 

#include <avr/io.h>
#include <avr/interrupt.h>
#include "Timer.h"
#include "GPIO.h"

static volatile uint32_t msTick = 1;
static volatile int timerElapsedStart = 0;
static volatile int timerElapsedState = 0;
static timercallback_t cbTimer = 0;

void TimerSetCallback(timercallback_t cb)
{
	cbTimer = cb;
}

void TimerInit(void)
{
	cli();
	TCCR0A = 0; // set TCCR0A register to 0
	TCCR0B = 0; // set TCCR0B register to 0
	TCNT0  = 0; // set counter value to 0
	//Timer settings for 1 ms CLK8 Fuse not set
	OCR0A = 124; // set compare match register
	TCCR0A |= (1 << WGM01); // turn on CTC mode
	
	
	TCCR0B |= (1 << CS01) | (1 << CS00); // Set CS01 and CS00 bits for 1:64 prescaler

	
	TIMSK0 |= (1 << OCIE0A); // enable timer compare interrupt

	sei();
}

int TimerElapsed(uint32_t millisecond)
{
	uint32_t diff = 0;
	
	
	if(timerElapsedState == 0) //not started
	{
		if(millisecond > 0) //and millsecond is set then set start time
		{
			timerElapsedStart = msTick;
			timerElapsedState = 1;
		}
			
	}
	else //timer is started
	{
		
		diff = msTick- timerElapsedStart;
		if(diff >= millisecond) //elapsed
		{
			timerElapsedState = 0;
			timerElapsedStart = 0;
			return 1;
		}
			
	}
	return 0;
	
}

uint32_t TimerGetTick(void)
{
	return msTick;
	
}

ISR (TIMER0_COMPA_vect)
{
	msTick++;
	if(cbTimer)
	{
		cbTimer();
	}
	
}

