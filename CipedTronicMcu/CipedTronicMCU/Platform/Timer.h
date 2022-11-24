/*
 * Timer.h
 *
 * Created: 17.04.2022 10:37:46
 *  Author: Siegwart
 */ 


#ifndef TIMER_H_
#define TIMER_H_


typedef void (*timercallback_t)(void);

void TimerInit(void);
int TimerElapsed(uint32_t millsecond);
uint32_t TimerGetTick(void);
void TimerSetCallback(timercallback_t cb);
void TimerWait(uint32_t ms);


#endif /* TIMER_H_ */