/*
 * Comparator.h
 *
 * Created: 06.06.2022 12:09:11
 *  Author: Siegwart
 */ 


#ifndef COMPARATOR_H_
#define COMPARATOR_H_

void CmpInit();
uint32_t CmpGetCounter();
uint32_t CmpGetCounterPerSecond();
uint32_t CmpGetMaxCounterPerSecond();
void CmpResetCounter();



#endif /* COMPARATOR_H_ */