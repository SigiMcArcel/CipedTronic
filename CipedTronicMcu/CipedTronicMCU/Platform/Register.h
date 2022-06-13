/*
 * Register.h
 *
 * Created: 17.04.2022 21:53:15
 *  Author: Siegwart
 *
 *
 *
 *
 *
 *
 
 */ 


#ifndef REGISTER_H_
#define REGISTER_H_

#define REGISTER_VERSION 0x00
#define REGISTER_MAX 0x01
#define REGISTER_ADC 0x02
#define REGISTER_MODE 0x03
#define REGISTER_I2C_SLAVE_START_ADR 0x04 //
#define REGISTER_I2C_SLAVE_SELECT 0x05  //0..15
#define REGISTER_I2C_SLAVE_REGISTER_SELECT 0x06  //0..15
#define REGISTER_I2C_ADDRESS  0x07
#define REGISTER_I2C_RXREGISTER  0x08
#define REGISTER_I2C_TXREGISTER  0x09
#define MAXREGISTER 0x0a

void RegisterInit(void);
void RegisterHandler(void);




#endif /* REGISTER_H_ */