/*
 * FIFO.h
 *
 * Created: 03.12.2022 09:31:26
 *  Author: Siegwart
 */ 
#include <string.h>

#ifndef FIFO_H_
#define FIFO_H_
#include <stdint.h>

typedef struct FifoHandler_t
{
	uint8_t* DataBuffer;
	int16_t DataBufferSize;
	int16_t DataItemCount;
	int16_t DataItemSize;
	int16_t EnqueueIndex;
	int16_t DequeueIndex;
	int16_t BufferLenght;
}FifoHandler;

void FifoInit(FifoHandler* h, int16_t itemCount, uint16_t itemSize, uint8_t* buffer);
uint8_t* FifoDequeue(FifoHandler* h);
int8_t FifoEnqueue(FifoHandler* h, uint8_t* data);


#endif /* FIFO_H_ */