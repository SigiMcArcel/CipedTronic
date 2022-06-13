/*
 * I2C.c
 *
 * Created: 18.04.2022 12:25:27
 *  Author: Siegwart
 */ 
#include <util/twi.h>
#include <avr/io.h>
#include <avr/interrupt.h>

#include "I2C.h"
#include "UART.h"
#include "Register.h"

#define TWCR_ACK TWCR = (1<<TWEN)|(1<<TWIE)|(1<<TWINT)|(1<<TWEA)|(0<<TWSTA)|(0<<TWSTO)|(0<<TWWC); 

static volatile I2CSlave I2CSlaves[MAX_SLAVES] = {0};
static volatile uint8_t StartAddress = 0;
static volatile uint8_t RegisterIndex = 0; 
static volatile uint8_t ActAddress = 0; 
static volatile uint8_t SelectedNumber = 0; 
static volatile uint8_t SelectedRegister = 0; 
extern uint8_t Registers[];

void I2CInit(void)
{
	cli();
	// load address into TWI address register
	ActAddress = 0;
	
	TWAMR = (0x0f << 1);
	// set the TWCR to enable address matching and enable TWI, clear TWINT, enable TWI interrupt
	TWCR = (1<<TWIE) | (1<<TWEA) | (1<<TWINT) | (1<<TWEN);
	sei();
	
}

int I2CCeateSlaves()
{
	UARTPuts("StartAddress = ");
	UARTPutsHex(StartAddress);
	TWAR = StartAddress << 1;
	I2CInit();
	return 0;
}

ISR(TWI_vect)
{
	switch(TW_STATUS)
	{
		case TW_SR_SLA_ACK: //slave adr && master Writes request received 
		RegisterIndex = 0;
		ActAddress = TWDR;
		TWCR_ACK
		break;
		case TW_SR_DATA_ACK:
		//UARTPuts("WRITE ADR = ");
		//UARTPutsHex(ActAddress >> 1);
		//UARTPuts(" RegisterIndex = ");
		//UARTPutsHex(RegisterIndex);
		I2CSlaves[(ActAddress >> 1) & 0x0f].rxRegisters[RegisterIndex] = TWDR;
		//UARTPuts(" VAL = ");
		//UARTPutsHex(I2CSlaves[ActAddress & 0x0f].rxRegisters[RegisterIndex]);
		//UARTPuts("\r\n");
		RegisterIndex++;
		TWCR_ACK
		// received data from master, call the receive callback
		break;
		
		
		case TW_ST_SLA_ACK:
		RegisterIndex = 0;
		ActAddress = TWDR;
		
		
		case TW_ST_DATA_ACK:
		//UARTPuts("READ ADR = ");
		//UARTPutsHex(ActAddress >> 1);
		//UARTPuts(" RegisterIndex = ");
		//UARTPutsHex(RegisterIndex);
		//UARTPuts(" VAL = ");
		//UARTPutsHex(I2CSlaves[ActAddress & 0x0f].registers[RegisterIndex]);
		//UARTPuts("\r\n");
		TWDR = I2CSlaves[(ActAddress >> 1) & 0x0f].txRegisters[RegisterIndex];
		TWCR_ACK
		RegisterIndex++;
		// master is requesting data, call the request callback
		
		break;
		case TW_BUS_ERROR:
		// some sort of erroneous state, prepare TWI to be readdressed
		TWCR = 0;
		TWCR_ACK
		break;
		default:
		TWCR = (1<<TWIE) | (1<<TWINT) | (1<<TWEA) | (1<<TWEN);
		break;
	}
}

void I2CHandler(void)
{
	SelectedNumber = Registers[REGISTER_I2C_SLAVE_SELECT];
	SelectedRegister = Registers[REGISTER_I2C_SLAVE_REGISTER_SELECT];
	
	if(StartAddress != Registers[REGISTER_I2C_SLAVE_START_ADR])
	{
		StartAddress = Registers[REGISTER_I2C_SLAVE_START_ADR];
		I2CCeateSlaves();
	}
	else
	{
		if(StartAddress != 0)
		{
			Registers[REGISTER_I2C_RXREGISTER] = I2CSlaves[SelectedNumber].rxRegisters[SelectedRegister];
			I2CSlaves[SelectedNumber].txRegisters[SelectedRegister] = Registers[REGISTER_I2C_TXREGISTER];
		} 
	}
}