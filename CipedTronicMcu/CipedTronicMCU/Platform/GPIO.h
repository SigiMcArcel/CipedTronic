/*
 * GPIO.h
 *
 * Created: 17.04.2022 10:59:11
 *  Author: Siegwart
 */ 


#ifndef GPIO_H_
#define GPIO_H_

#define GPIO_DIR_OUT 1
#define GPIO_DIR_IN 0


void GPIOInit(void);
void GPIOSetDirection(uint8_t pin,volatile uint8_t*,uint8_t direction);

void GPIOSet(uint8_t pin,volatile uint8_t* port);
void GPIOReset(uint8_t pin,volatile uint8_t* port);
uint8_t GPIOGet(uint8_t pin,volatile uint8_t* port);
void GPIOToggle(uint8_t pin,volatile uint8_t* port);



#endif /* GPIO_H_ */