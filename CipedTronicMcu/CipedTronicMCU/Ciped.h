/*
 * Ciped.h
 *
 * Created: 07.06.2022 19:49:47
 *  Author: Siegwart
 */ 


#ifndef CIPED_H_
#define CIPED_H_
#include <stdint.h>

void CipedInit();
void CipedResetPulses();
uint32_t CipedGetPulses();
uint32_t CipedGetPulsesPersecond();
uint32_t CipedGetMaxPulsesPersecond();
void CipedHandler();



#endif /* CIPED_H_ */