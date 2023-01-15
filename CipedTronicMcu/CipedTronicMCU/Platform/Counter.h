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
 * Counter.h
 *
 * Counts and evaluates the impulses from the bicycle dynamo.
 * the values will saved in EEPROM at reading the values
 * Created: 11.06.2022 09:33:27
 *  Author: Siegwart
 */ 


#ifndef COUNTER_H_
#define COUNTER_H_
#ifdef __cplusplus
extern "C" {
	#endif

/// <summary>
/// Initializes atmega 32u4 Counter1 and reads the saved counter values from EEPROM
/// </summary>
void CounterInit();
/// <summary>
/// Sets the  counter value and saves in the EEPROM
/// </summary>
/// <param name="val">Counter value</param>
void CounterSetCounter(uint32_t val);
/// <summary>
/// Sets the  max counter value per second and saves in the EEPROM
/// </summary>
/// <param name="val">Counter value per second</param>
void CounterSetMaxCounterPerSecond(uint32_t val);
/// <summary>
/// Sets the average counter value per second and saves in the EEPROM
/// </summary>
/// <param name="val">Counter value</param>
void CounterSetAvgCounterPerSecond(uint32_t val);
/// <summary>
/// Gets the Counter
/// </summary>
/// <returns>Counter value</returns>
uint32_t CounterGetCounter();
/// <summary>
/// Gets the Counter
/// </summary>
/// <returns>Gets the Counter per second</returns>
uint32_t CounterGetCounterPerSecond();
/// <summary>
/// Gets the Counter per second max
/// </summary>
/// <returns>Counter value</returns>
uint32_t CounterGetMaxCounterPerSecond();
/// <summary>
/// Gets the Counter per second average
/// </summary>
/// <returns>Counter value</returns>
uint32_t CounterGetAvgCounterPerSecond();
/// <summary>
/// Sets all counters to 0
/// </summary>
void CounterResetCounter();
/// <summary>
/// Handles the Counter. Must called cyclic in main
/// </summary>
void CounterHandler();
#ifdef __cplusplus
}
#endif

#endif /* COUNTER_H_ */