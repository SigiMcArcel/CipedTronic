/*
 * GPIO.h
 *
 * GPIO Functions for ATMEGA 32U4
 * Created: 17.04.2022 10:59:11
 *  Author: Siegwart
 */ 


#ifndef GPIO_H_
#define GPIO_H_
#include <stdint.h>

//Pin direction
#define GPIO_DIR_OUT 1
#define GPIO_DIR_IN 0

//Interrupt Mode
#define GPIO_INT_LOW_LEVEL 0
#define GPIO_INT_ANY_EDGE 1
#define GPIO_INT_FALLING_EDGE 2
#define GPIO_INT_RAISING_EDGE 3

/// <summary>
/// The callback type
/// </summary
/// <param name="num">callback function</param>
typedef void (*gpiocallback_t)(uint8_t num);

/// <summary>
/// Initialize
/// </summary
void GPIOInit(void);
/// <summary>
/// Sets External Interrupt and callback function
/// </summary>
/// <param name="cb">callback function</param>
/// <param name="pin">
/// The number corresponds to the external interrupt inputs of the Atmega 32u4.
/// INT0,1,2,3 PORTD and 6 PORTE
/// </param>
/// <param name="mode">See defines</param>
/// <returns>1 = OK,0 = Pin number invalid </returns>
uint8_t GPIOSetInterrupt(gpiocallback_t cb,uint8_t pin,uint8_t mode);
/// <summary>
/// Sets PinChange interrupt and callback function PORTB
/// </summary>
/// <param name="cb">callback function</param>
/// <param name="mask">Pin Bit Mask for enabling</param>
/// <returns>1 = OK,0 = Pin number invalid </returns>
void GPIOSetPinChangeInterrupt(gpiocallback_t cb,uint8_t mask);
/// <summary>
/// Sets GPIO Pin Direction
/// </summary>
/// <param name="pin">Pin number 0...7</param>
/// <param name="port">Port address, like "&PORTB"</param>
/// <param name="direction">Pin direction </param>
void GPIOSetDirection(uint8_t pin,volatile uint8_t* port,uint8_t direction);
/// <summary>
/// Sets GPIO Pin Value to TRUE
/// </summary>
/// <param name="pin">Pin number 0...7</param>
/// <param name="port">Port address, like "&PORTB"</param>
void GPIOSet(uint8_t pin,volatile uint8_t* port);
/// <summary>
/// Sets GPIO Pin Value to FALSE
/// </summary>
/// <param name="pin">Pin number 0...7</param>
/// <param name="port">Port address, like "&PORTB"</param>
void GPIOReset(uint8_t pin,volatile uint8_t* port);
/// <summary>
/// Gets The GPIO Pin Value
/// </summary>
/// <param name="pin">Pin number 0...7</param>
/// <param name="port">Port address, like "&PORTB"</param>
/// <returns>Value </returns>
uint8_t GPIOGet(uint8_t pin,volatile uint8_t* port);
/// <summary>
/// Toggles GPIO Pin Value
/// </summary>
/// <param name="pin">Pin number 0...7</param>
/// <param name="port">Port address, like "&PORTB"</param>
void GPIOToggle(uint8_t pin,volatile uint8_t* port);

#endif /* GPIO_H_ */