/*
 * GPIO.h
 *
 * Created: 17.04.2022 10:59:11
 *  Author: Siegwart
 */ 


#ifndef GPIO_H_
#define GPIO_H_

void GPIOInit(void);
void GPIOBSet(uint8_t pin);
void GPIOBReset(uint8_t pin);
uint8_t GPIOBGet(uint8_t pin);
void GPIOBToggle(uint8_t pin);



#endif /* GPIO_H_ */