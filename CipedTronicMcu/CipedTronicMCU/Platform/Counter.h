/*
 * Counter.h
 *
 * Created: 11.06.2022 09:33:27
 *  Author: Siegwart
 */ 


#ifndef COUNTER_H_
#define COUNTER_H_
#ifdef __cplusplus
extern "C" {
	#endif
void CounterInit();
uint32_t CounterGetCounter();
uint32_t CounterGetCounterPerSecond();
uint32_t CounterGetMaxCounterPerSecond();
uint32_t CounterGetAvgCounterPerSecond();
void CounterResetCounter();
void CounterHandler();
#ifdef __cplusplus
}
#endif

#endif /* COUNTER_H_ */