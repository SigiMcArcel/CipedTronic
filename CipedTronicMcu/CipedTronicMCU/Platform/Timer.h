/*
 * Timer.h
 *
 * Created: 17.04.2022 10:37:46
 *  Author: Siegwart
 * Timer functions for ATMEGA 32u4
 */ 

#ifndef TIMER_H_
#define TIMER_H_

/// <summary>
/// Timer callback
/// After register, The callback will called in 1 millsecond interval
/// </summary>
typedef void (*timercallback_t)(void);

/// <summary>
/// Timer callback
/// </summary>
void TimerInit(void);
/// <summary>
/// Returns 1, if the time specified in the parameter has expired.
/// The functions resets automatically after the time has elapsed
/// </summary>
int TimerElapsed(uint32_t millsecond);
/// <summary>
/// Gets the millisecond tick of the timer (Not overflow save) 
/// <returns>ticks in milliseconds</returns>
uint32_t TimerGetTick(void);
/// <summary>
/// Sets the millsecond callback
/// </summary>
void TimerSetCallback(timercallback_t cb);
/// <summary>
/// Blocking waiter
/// </summary>
/// <param name="millsecond">time in millseconds</param>
void TimerWait(uint32_t ms);
#endif /* TIMER_H_ */