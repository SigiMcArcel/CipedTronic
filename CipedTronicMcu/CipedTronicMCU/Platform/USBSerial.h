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
 * USBSerial.h
 *
 * Created: 22.06.2022 20:16:13
 *  Author: Siegwart
 * 
 * UART over USB functions base on 
 * authors: J. Fiene & J. Romano
 * https://github.com/PennRobotics/acrobot
 */ 

#ifndef USBSERIAL_H_
#define USBSERIAL_H_
#include <stdint.h>

/// <summary>
/// Initialize
/// </summary>
void USBSerialInit(void);
/// <summary>
/// Gets USB is connected
/// </summary>
/// <param name="data">databuffer</param>
/// <param name="size">size</param>
/// <returns>1 = Connected</returns>
uint8_t USBSerialConnected();
/// <summary>
/// Puts a char (Non Blocking)
/// </summary>
/// <param name="c">the char</param>
void USBSerialPutchar(const char c);
/// <summary>
/// Gets a char from receive buffer
/// </summary>
/// <returns>value</returns>
char USBSerialGetchar(void);
/// <summary>
/// waits till a string ends with a delemiter 
/// the received string will returns without delemiter
/// </summary>
/// <param name="str">receives string</param>
/// <param name="delemiter">delemiter the functions wait for</param>
/// <returns>-1 no data or error,0 waiting for delemiter,>0 lenght of string and delemiter received</returns>
int  USBSerialGets(char *str,char endDelemiter);
/// <summary>
/// Puts a string
/// </summary>
/// <param name="str">string</param>

void USBSerialPuts(const char *str);
/// <summary>
/// Puts a uint8 as hex string
/// </summary>
/// <param name="val">byte</param>
void USBSerialPutsHex8(uint8_t val);
/// <summary>
/// Puts a uint32 as hex string
/// </summary>
/// <param name="val">byte</param>
void USBSerialPutsHex32(uint32_t val);
/// <summary>
/// Puts a long as number string
/// </summary>
/// <param name="val">byte</param>
void USBSerialPutsLong(uint32_t val);
/// <summary>
/// Puts a Byte array as as hex string. Format 0x00 0x00 0x00 ... endl
/// </summary>
/// <param name="val">byte</param>
void USBSerialPutsByteArray(uint8_t* array,uint8_t len );

#endif /* USBSERIAL_H_ */