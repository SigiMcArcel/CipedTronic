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
#include "Platform/Counter.h"
#include "Platform/Serial.h"
#include "CipedTronic/BLE.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>


#define SOFTWAREVERSION "00.00.42"
#define DEVICENAME "CIPBLA"

static volatile uint32_t _LastTimerTick = 0;
static volatile uint32_t _Delay = 0;
static volatile uint32_t _Control = 0;
static volatile uint32_t _Test = 0;

static uint32_t _Counter = 0;
static uint32_t _CounterPerSecond = 0;
static uint32_t _CounterPerSecondAvg = 0;
static uint32_t _CounterPerSecondMax = 0;

static void BLERxCallback(uint8_t characteristic, uint32_t value);

static void BLERxCallback(uint8_t characteristic, uint32_t value)
{
	SerialPuts("BLERxCallback ");
	SerialPutsLong(characteristic);
	SerialPuts(" ");
	SerialPutsLong(value);
	SerialPuts("\r\n");
	switch(characteristic)
	{
		case PIPE_CIPEDTRONIC_BIKE_SERVICE_MAXIMUM_VELOCITY_RX:
		{
			CounterSetMaxCounterPerSecond(value);
			break;
		}
		case PIPE_CIPEDTRONIC_BIKE_SERVICE_AVERAGE_VELOCITY_RX:
		{
			CounterSetAvgCounterPerSecond(value);
			break;
		}
		case PIPE_CIPEDTRONIC_BIKE_SERVICE_DISTANCE_RX:
		{
			CounterSetCounter(value);
			break;
		}
		case PIPE_CIPEDTRONIC_BIKE_SERVICE_CONTROL_RX:
		{
			_Control = value;
			SerialPuts("PIPE_CIPEDTRONIC_BIKE_SERVICE_CONTROL_RX 0x");
			SerialPutsHex32(_Control);
			SerialPuts("\r\n");
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
	GPIOInit();
	TimerInit();
	CounterInit();
	USBSerialInit();

	GPIOSetDirection(5,&DDRD,GPIO_DIR_OUT);//tx
	GPIOSetDirection(0,&DDRB,GPIO_DIR_OUT);//rx
	GPIOSetDirection(7,&DDRC,GPIO_DIR_OUT);//PW led
	
	
	while(!USBSerialConnected()){}

	SerialPuts("CipedTronic C 2022\r\n");
	GPIOSet(5,&PORTD);
	GPIOSet(0,&PORTB);
	GPIOSet(7,&PORTC);
	
	TimerWait(6000);
	
	GPIOReset(5,&PORTD);
	GPIOReset(0,&PORTB);
	GPIOReset(7,&PORTC);
	BLERegisterPipeValue(PIPE_CIPEDTRONIC_BIKE_SERVICE_VELOCITY_TX,&_CounterPerSecond);
	BLERegisterPipeValue(PIPE_CIPEDTRONIC_BIKE_SERVICE_MAXIMUM_VELOCITY_TX,&_CounterPerSecondMax);
	BLERegisterPipeValue(PIPE_CIPEDTRONIC_BIKE_SERVICE_AVERAGE_VELOCITY_TX,&_CounterPerSecondAvg);
	BLERegisterPipeValue(PIPE_CIPEDTRONIC_BIKE_SERVICE_DISTANCE_TX,&_Counter);
	BLEInit(0,100,DEVICENAME,SOFTWAREVERSION,BLERxCallback);
	
	while(1)
	{
		uint32_t tick = TimerGetTick();
		uint32_t diff = tick - _LastTimerTick;
		CounterHandler();
		
		BLEProcess();
		if(diff >= 500)
		{
			GPIOToggle(7,&PORTC);
			_LastTimerTick = tick;
			_CounterPerSecond = CounterGetCounterPerSecond();
			_CounterPerSecondMax = CounterGetMaxCounterPerSecond();
			_CounterPerSecondAvg = CounterGetAvgCounterPerSecond();
			_Counter = CounterGetCounter();
			
			BLESendNotification();
		}
		
	}
}