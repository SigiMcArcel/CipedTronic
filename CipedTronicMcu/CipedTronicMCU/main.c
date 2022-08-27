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
#include "Platform/Comparator.h"
#include "Protocoll.h"
#include "Platform/Counter.h"
#include "Platform/USBSerial.h"
#include "AdafruitBLE/BLE.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
static volatile uint32_t _LastTimerTick = 0;


int main (void)
{
	GPIOInit();
	TimerInit();
	CounterInit();
	USBSerialInit();
	BLEInit(100,100);
	GPIOSetDirection(5,&DDRD,GPIO_DIR_OUT);
	while(!USBSerialConnected()){}
		
	USBSerialPuts("Hallo\r\n");
	while(1)
	{
		uint32_t tick = TimerGetTick();
		uint32_t diff = tick - _LastTimerTick;
		if(diff >= 500)
		{
			GPIOToggle(5,&PORTD);
			_LastTimerTick = tick;
			//ProtocollHandler();
			BLEProcess();
		}
		
	}
}