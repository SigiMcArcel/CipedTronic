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
#include "EEProm.h"

#define PROCESS_CYCLE 1000 //defines the interval in ms, where the speed is calculated
static volatile uint32_t _Counter = 0x0; //Main counter
static volatile uint32_t _LastCounterAdding = 0;//Last counter value at which the ATMEGA counter was added, for handling overflows
static volatile uint32_t _LastCounter = 0; //save last countervalue for calculate velocity
static volatile uint32_t _CounterPerSecond = 0x0; //Velocity
static volatile uint32_t _MaxCounterPerSecond = 0x0; //Max velocity
static volatile uint32_t _AvgCounterPerSecond = 0x0; //Average Velocity
static volatile uint32_t _LastTimerTick = 0; //calculate Cycle

//Timer Callback (1ms) for ATMEGA 32u4 8 Mhz) 
static void CounterTimerCB(void);

//calulate Counter value by adding the value from the ATMEGA hw counter 1 to the main counter.
//Handles counter overflow
void CounterHandler()
{
	uint16_t tmpCount = TCNT1; //get value from HW Counter
	
	if(tmpCount >_LastCounterAdding)
	{
		_Counter += (tmpCount - _LastCounterAdding);
	}
	//overflow 
	else if(tmpCount < _LastCounterAdding)
	{
		_Counter +=  0xffffffff - _LastCounterAdding + tmpCount;
	}
	_LastCounterAdding = tmpCount;
}

void CounterInit()
{
	_LastCounter = _Counter = EEPROMRead32(EEPROM_ADR_COUNTER);
	_MaxCounterPerSecond = EEPROMRead32(EEPROM_ADR_COUNTER_MAX);
	_AvgCounterPerSecond = EEPROMRead32(EEPROM_ADR_COUNTER_AVG);
	/*
		Counter 1  configuration
		- Raising edge ICES1 = 1
		- External clock source on Tn pin. Clock on rising edge
		- Input PD6 
	*/
	TCCR1B = (1<<ICES1) | (1<<CS10) | (1<<CS11) | (1<<CS12);
	TimerSetCallback(CounterTimerCB);//initialize Timer
}

void CounterSetCounter(uint32_t val)
{
	_Counter = val;
	EEPROMWrite32(EEPROM_ADR_COUNTER,EEPROM_LEN_COUNTER);
}


void CounterSetMaxCounterPerSecond(uint32_t val)
{
	_MaxCounterPerSecond = val;
	EEPROMWrite32(EEPROM_ADR_COUNTER_MAX,EEPROM_LEN_COUNTER_MAX);
}

void CounterSetAvgCounterPerSecond(uint32_t val)
{
	_AvgCounterPerSecond = val;
	EEPROMWrite32(EEPROM_ADR_COUNTER_AVG,EEPROM_LEN_COUNTER_AVG);
}

uint32_t CounterGetCounter()
{
	EEPROMWrite32(EEPROM_ADR_COUNTER,_Counter);
	return _Counter;
}

uint32_t CounterGetCounterPerSecond()
{
	return _CounterPerSecond;
}

uint32_t CounterGetMaxCounterPerSecond()
{
	EEPROMWrite32(EEPROM_ADR_COUNTER_MAX,EEPROM_LEN_COUNTER_MAX);
	return _MaxCounterPerSecond;
}

uint32_t CounterGetAvgCounterPerSecond()
{
	EEPROMWrite32(EEPROM_ADR_COUNTER_AVG,EEPROM_LEN_COUNTER_AVG);
	return _AvgCounterPerSecond;
}

void CounterResetCounter()
{
	_Counter = 0;
	_LastCounter = 0;
	_MaxCounterPerSecond = 0;
	_AvgCounterPerSecond = 0;
	EEPROMWrite32(EEPROM_ADR_COUNTER,EEPROM_LEN_COUNTER);
	EEPROMWrite32(EEPROM_ADR_COUNTER_MAX,EEPROM_LEN_COUNTER_MAX);
	EEPROMWrite32(EEPROM_ADR_COUNTER_AVG,EEPROM_LEN_COUNTER_AVG);
}

//Ticks all 1 ms
void CounterTimerCB(void)
{
	uint32_t tick = TimerGetTick();
	uint32_t diff = tick - _LastTimerTick;
	
	//after 1 Second calculate velocity
	if(diff >= PROCESS_CYCLE)
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