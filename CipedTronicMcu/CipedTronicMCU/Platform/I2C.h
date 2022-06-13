/*
 * I2C.h
 *
 * Created: 18.04.2022 12:25:44
 *  Author: Siegwart
 */ 


#ifndef I2C_H_
#define I2C_H_

#define MAX_SLAVES 15
#define MAX_SLAVE_REGISTER 4

typedef struct I2CSlave_t
{
	volatile uint8_t txRegisters[MAX_SLAVE_REGISTER];
	volatile uint8_t rxRegisters[MAX_SLAVE_REGISTER];

}I2CSlave;

void I2CInit(void);
int I2CCeateSlaves();
void I2CHandler(void);

#endif /* I2C_H_ */