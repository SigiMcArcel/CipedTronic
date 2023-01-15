/* Copyright 2023 Marcel Siegwart
* 
* Permission is hereby granted, free of charge, 
* to any person obtaining a copy of this software and associated documentation files (the "Software"), 
* to deal in the Software without restriction, including without limitation the rights to use, copy, 
* modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, 
* and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included 
* in all copies or substantial portions of the Software.
*
* Neither the name of the copyright holders nor the names of
* contributors may be used to endorse or promote products derived
* from this software without specific prior written permission.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
* OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

/*
 * main.c
 *
 * Main Entry
 * Created: 18.04.2022 13:50:13
 *  Author: Siegwart
 */ 
#include <avr/io.h>
#include <avr/interrupt.h>
#include "Platform/Timer.h"
#include "Platform/GPIO.h"
#include "Platform/Comparator.h"
#include "Platform/Counter.h"
#include "Platform/Serial.h"
#include "CipedTronic/BLE.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>


#define SOFTWAREVERSION "00.00.42"
#define DEVICENAME "CIPBLA"

//notification intervall
static volatile uint32_t _LastTimerTick = 0;
static volatile uint32_t _Delay = 0;
static volatile uint32_t _Control = 0;
static volatile uint32_t _Test = 0;

//Speedometer data
static uint32_t _Counter = 0;
static uint32_t _CounterPerSecond = 0;
static uint32_t _CounterPerSecondAvg = 0;
static uint32_t _CounterPerSecondMax = 0;

static void BLERxCallback(uint8_t characteristic, uint32_t value);


//BLE stack callback
static void BLERxCallback(uint8_t characteristic, uint32_t value)
{
	SerialPuts("BLERxCallback ");
	SerialPutsLong(characteristic);
	SerialPuts(" ");
	SerialPutsLong(value);
	SerialPuts("\r\n");
	switch(characteristic)
	{
		//sets Counter Per Second maximum (for future use, multiple tours)
		case PIPE_CIPEDTRONIC_BIKE_SERVICE_MAXIMUM_VELOCITY_RX:
		{
			CounterSetMaxCounterPerSecond(value);
			break;
		}
		//sets Counter Per Second average (for future use, multiple tours)
		case PIPE_CIPEDTRONIC_BIKE_SERVICE_AVERAGE_VELOCITY_RX:
		{
			CounterSetAvgCounterPerSecond(value);
			break;
		}
		//sets Distance (for future use, multiple tours)
		case PIPE_CIPEDTRONIC_BIKE_SERVICE_DISTANCE_RX:
		{
			CounterSetCounter(value);
			break;
		}
		
		//Handles control value
		case PIPE_CIPEDTRONIC_BIKE_SERVICE_CONTROL_RX:
		{
			_Control = value;
			SerialPuts("PIPE_CIPEDTRONIC_BIKE_SERVICE_CONTROL_RX 0x");
			SerialPutsHex32(_Control);
			SerialPuts("\r\n");
			//resets all Counters
			if((_Control & 0x0000001) > 0)
			{
				SerialPuts("Counter Reset\r\n");
				CounterResetCounter();
			}
			_Control = 0;
			break;
		}
	}
}

int main (void)
{
	//initialize mcu hardware
	GPIOInit();
	TimerInit();
	CounterInit();
	USBSerialInit();

	GPIOSetDirection(5,&DDRD,GPIO_DIR_OUT);//tx
	GPIOSetDirection(0,&DDRB,GPIO_DIR_OUT);//rx
	GPIOSetDirection(7,&DDRC,GPIO_DIR_OUT);//PW led
	
	//register Notification Values
	BLERegisterNotificationValue(PIPE_CIPEDTRONIC_BIKE_SERVICE_VELOCITY_TX,&_CounterPerSecond);
	BLERegisterNotificationValue(PIPE_CIPEDTRONIC_BIKE_SERVICE_MAXIMUM_VELOCITY_TX,&_CounterPerSecondMax);
	BLERegisterNotificationValue(PIPE_CIPEDTRONIC_BIKE_SERVICE_AVERAGE_VELOCITY_TX,&_CounterPerSecondAvg);
	BLERegisterNotificationValue(PIPE_CIPEDTRONIC_BIKE_SERVICE_DISTANCE_TX,&_Counter);
	BLEInit(0,100,DEVICENAME,SOFTWAREVERSION,BLERxCallback);
	
	while(1)
	{
		uint32_t tick = TimerGetTick();
		uint32_t diff = tick - _LastTimerTick;
		CounterHandler();//Process Counters
		BLEProcess(); //Process BLE
		if(diff >= 500)
		{
			//Toggle LED
			GPIOToggle(7,&PORTC);
			//Update counter values
			_CounterPerSecond = CounterGetCounterPerSecond();
			_CounterPerSecondMax = CounterGetMaxCounterPerSecond();
			_CounterPerSecondAvg = CounterGetAvgCounterPerSecond();
			_Counter = CounterGetCounter();
			//Send registered notification
			BLESendNotification();
			_LastTimerTick = tick;
		}
		
	}
}