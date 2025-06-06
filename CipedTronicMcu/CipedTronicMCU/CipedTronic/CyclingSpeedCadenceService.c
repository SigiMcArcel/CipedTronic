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