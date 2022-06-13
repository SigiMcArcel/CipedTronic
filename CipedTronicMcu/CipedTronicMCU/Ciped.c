/*
 * Ciped.c
 *
 * Created: 07.06.2022 19:50:17
 *  Author: Siegwart
 */ 
#include "Ciped.h"
#include "Platform/Comparator.h"

void CipedInit()
{
	CmpInit();
}

void CipedResetPulses()
{
	 CmpResetCounter();
}

uint32_t CipedGetPulses()
{
	return CmpGetCounter();
	
}

uint32_t CipedGetPulsesPersecond()
{
	return CmpGetCounterPerSecond();
}

uint32_t CipedGetMaxPulsesPersecond()
{
	return CmpGetMaxCounterPerSecond();
}

void CipedHandler()
{
	;
}
