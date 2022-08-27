#ifndef UART_H
#define UART_H

#define TX_BUFFER_SIZE 100
#define RX_BUFFER_SIZE 10
#define BAUDRATE 38400

void UARTInit(void);
void UARTPutchar(char c);
char UARTGetchar(void);
int  UARTGets(char *str,char endDelemiter);
void UARTPuts(char *str);
void UARTPutsHex8(uint8_t val);
void UARTPutsHex32(uint32_t val);
void UARTLLPutchar(char c);

#endif // UART_H
