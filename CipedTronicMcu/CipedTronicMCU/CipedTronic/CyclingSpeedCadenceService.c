/*
 * CyclingSpeedCadenceService.c
 *
 * Created: 09.01.2023 21:34:11
 *  Author: Siegwart
 */ 
#include "CyclingSpeedCadenceService.h"

void ProcessCSCMeasurement(uint8_t* data,uint8_t len)
{
	
	
}
void ProcessCscFeature(uint8_t* data,uint8_t len)
{
	
	
}
void ProcessScControlPoint(uint8_t* data,uint8_t len)
{
	SCControlPoint* d = (SCControlPoint*
	)data;
	switch(d->OpCode)
	{
		case CSCREQ_SetCumulativeValue:
		{
			break;
		}
		case CSCREQ_ResponseCode:
		{
			break;
		}
		default:
		{
			
			
		}
		
	}
}