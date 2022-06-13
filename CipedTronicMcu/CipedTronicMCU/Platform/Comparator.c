/*
 * Comparator.c
 *
 * Created: 06.06.2022 12:08:54
 *  Author: Siegwart
 */ 
#include <avr/io.h>
#include <avr/interrupt.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "UART.h"
#include "Timer.h"
#include "Comparator.h"
#include "GPIO.h"

static volatile uint32_t _Counter = 0;
static volatile uint32_t _LastCounter = 0;
static volatile uint32_t _CounterPerSecond = 0;
static volatile uint32_t _MaxCounterPerSecond = 0;
static volatile int UpFlag = 0;
static volatile uint32_t _LastTimerTick = 0;
static void timerCB(void);

void CmpInit()
{
	ADCSRA &= ~(1<<ADEN);
	ADCSRB = 0;
	ACSR = 0x03;
	DIDR1 = 0x03;
	UpFlag = 0;
	TimerSetCallback(timerCB);
}

uint32_t CmpGetCounter()
{
	return _Counter;
}

uint32_t CmpGetCounterPerSecond()
{
	return _CounterPerSecond;
}

uint32_t CmpGetMaxCounterPerSecond()
{
	return _MaxCounterPerSecond;
}

void CmpResetCounter()
{
	_Counter = 0;
	_LastCounter = 0;
	_MaxCounterPerSecond = 0;
}


void timerCB(void)
{
	uint32_t tick = TimerGetTick();
	uint32_t diff = tick - _LastTimerTick;
	uint8_t aco = (ACSR & (1<<ACO));
	if(!aco)
	{
		UpFlag = 0;
	}
	if(aco && !UpFlag)
	{
		_Counter++;
		UpFlag = 1;
	}
	
	if(diff >= 1000)
	{
		_LastTimerTick = tick;
		_CounterPerSecond = _Counter -_LastCounter;
		_LastCounter = _Counter;
		if(_CounterPerSecond > _MaxCounterPerSecond)
		{
			_MaxCounterPerSecond = _CounterPerSecond;
		}
	}
	
	
}
