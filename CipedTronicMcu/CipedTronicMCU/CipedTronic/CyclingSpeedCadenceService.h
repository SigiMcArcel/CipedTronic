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
 * CyclingSpeedCadenceService.h
 *
 * Created: 09.01.2023 21:34:23
 * Author: Siegwart
 * Implements the Bluetooth standard cyclic and speed cadence service  
 * under construct. Not used
 */ 


#ifndef CYCLINGSPEEDCADENCESERVICE_H_
#define CYCLINGSPEEDCADENCESERVICE_H_
#include <stdint.h>
typedef struct CSCMeasurement_t
{
	uint8_t Flags;
	uint32_t CumulativeWheelRevolutions;
	uint16_t LastWheelEventTime;
	uint16_t CumulativeCrankRevolutions;
	uint16_t LastCrankEventTime;
	
}CSCMeasurement;

typedef struct CSCFeature_t
{
	uint16_t CSCFeature;
}CSCFeature;

#define CSCREQ_SetCumulativeValue 1
#define CSCREQ_StartSensorCalibration 2
#define CSCREQ_UpdateSensorLocation 3
#define CSCREQ_RequestSupportedSensorLocations 4
#define CSCREQ_ResponseCode 16
#define CSCRSP_Successful 1
#define CSCRSP_Unsupported 2
#define CSCRSP_InvalidParameter 3
#define CSCRSP_OperationFailed 4

typedef struct SCControlPoint_t
{
	uint8_t OpCode;
	uint8_t CumulativeValue[8];
	uint8_t SensorLocationValue;
	uint8_t RequestOpCode;
	uint8_t ResponseValue;
	uint8_t ResponseParameter[8];
}SCControlPoint;


void ProcessCSCMeasurement(uint8_t* data,uint8_t len);
void ProcessScControlPoint(uint8_t* data,uint8_t len);

#endif /* CYCLINGSPEEDCADENCESERVICE_H_ */