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

static volatile uint32_t _Counter = 0x5555AAAA;
static volatile uint32_t _LastCounterAdding = 0;
static volatile uint32_t _LastCounter = 0;
static volatile uint32_t _CounterPerSecond = 0x11112222;
static volatile uint32_t _MaxCounterPerSecond = 0x33334444;
static volatile uint32_t _AvgCounterPerSecond = 0x33334444;
static volatile uint32_t _LastTimerTick = 0;


static void CounterTimerCB(void);

void CounterHandler()
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
	TimerSetCallback(CounterTimerCB);
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

void CounterTimerCB(void)
{
	uint32_t tick = TimerGetTick();
	uint32_t diff = tick - _LastTimerTick;
	
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
	
	CounterHandler();
	
}