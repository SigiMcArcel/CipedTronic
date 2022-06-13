/*
 * main.c
 *
 * Created: 18.04.2022 13:50:13
 *  Author: Siegwart
 */ 
#include <avr/io.h>
#include <avr/interrupt.h>
#include "Platform/Timer.h"
#include "Platform/GPIO.h"
//#include "Platform/Register.h"
#include "Platform/UART.h"
#include "Platform/Comparator.h"
#include "Protocoll.h"
#include "Platform/Counter.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
static volatile uint32_t _LastTimerTick = 0;


int main (void)
{
	TimerInit();
	UARTInit();
	CounterInit();
	
	
	while(1)
	{
		uint32_t tick = TimerGetTick();
		uint32_t diff = tick - _LastTimerTick;
		if(diff >= 500)
		{
			_LastTimerTick = tick;
			ProtocollHandler();
		}
	}
}