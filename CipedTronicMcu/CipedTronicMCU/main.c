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
#include "Platform/Comparator.h"
#include "Protocoll.h"
#include "Platform/Counter.h"
#include "Platform/USBSerial.h"
#include "CipedTronic/BLE.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
static volatile uint32_t _LastTimerTick = 0;
static volatile uint32_t _Delay = 0;

int main (void)
{
	GPIOInit();
	TimerInit();
	CounterInit();
	USBSerialInit();

	BLESetDeviceName("CIPBLE");
	GPIOSetDirection(5,&DDRD,GPIO_DIR_OUT);//tx
	GPIOSetDirection(0,&DDRB,GPIO_DIR_OUT);//rx
	GPIOSetDirection(7,&DDRC,GPIO_DIR_OUT);//PW led
	
	
	while(!USBSerialConnected()){}

	USBSerialPuts("Hallo\r\n");
	GPIOSet(5,&PORTD);
	GPIOSet(0,&PORTB);
	GPIOSet(7,&PORTC);
	
	TimerWait(6000);
	
	GPIOReset(5,&PORTD);
	GPIOReset(0,&PORTB);
	GPIOReset(7,&PORTC);
	BLEInit(0,100);
	
	while(1)
	{
		uint32_t tick = TimerGetTick();
		uint32_t diff = tick - _LastTimerTick;
		CounterHandler();
		
		ProtocolHandler();
		BLEProcess();
		if(diff >= 500)
		{
			GPIOToggle(7,&PORTC);
			_LastTimerTick = tick;
			
			
		}
		
	}
}