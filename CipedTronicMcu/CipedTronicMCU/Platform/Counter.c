/*
 * Counter.c
 *
 * Created: 11.06.2022 09:33:41
 *  Author: Siegwart
 */ 
#include <stdint.h>
#include <avr/io.h>
#include <avr/interrupt.h>
#include "Timer.h"
#include "Counter.h"


static volatile uint32_t _Counter = 0;
static volatile uint32_t _LastCounterAdding = 0;
static volatile uint32_t _LastCounterFrequency = 0;
static volatile uint32_t _CounterPerSecond = 0;
static volatile uint32_t _MaxCounterPerSecond = 0;
static volatile uint32_t _LastTimerTick = 0;


static void CounterTimerCB(void);

void CounterInit()
{
	
	TCCR1B = (1<<ICES1) | (1<<CS10) | (1<<CS11) | (1<<CS12);
	TimerSetCallback(CounterTimerCB);
}
uint32_t CounterGetCounter()
{
	uint16_t tmpCount = TCNT1;
	
	if(tmpCount >_LastCounterAdding)
	{
		_Counter += (tmpCount - _LastCounterAdding);
		
	} else if(tmpCount < _LastCounterAdding)
	{
		_Counter +=  0xffffffff - _LastCounterAdding + tmpCount;
	}
	_LastCounterAdding = tmpCount;
	return _Counter;
}

uint32_t CounterGetCounterPerSecond()
{
	return _CounterPerSecond;
}

uint32_t CounterGetMaxCounterPerSecond()
{
	
	return _MaxCounterPerSecond;
}

void CounterResetCounter()
{
	_Counter = 0;
	_LastCounterFrequency = 0;
	_MaxCounterPerSecond = 0;
}


void CounterTimerCB(void)
{
	uint32_t tick = TimerGetTick();
	uint32_t diff = tick - _LastTimerTick;
	//uint8_t aco = (ACSR & (1<<ACO));
	//if(!aco)
	//{
		//UpFlag = 0;
	//}
	//if(aco && !UpFlag)
	//{
		//_Counter++;
		//UpFlag = 1;
	//}
	
	if(diff >= 1000)
	{
		_LastTimerTick = tick;
		_CounterPerSecond = _Counter -_LastCounterFrequency;
		_LastCounterFrequency = _Counter;
		if(_CounterPerSecond > _MaxCounterPerSecond)
		{
			_MaxCounterPerSecond = _CounterPerSecond;
		}
	}
	
	
}