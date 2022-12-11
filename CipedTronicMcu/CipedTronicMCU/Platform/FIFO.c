#include "FIFO.h"
#include <string.h>

void FifoInit(FifoHandler* h, int16_t itemCount, uint16_t itemSize, uint8_t* buffer)
{
	h->DataItemCount = itemCount;
	h->DataBuffer = buffer;
	h->DataBufferSize = itemCount * itemSize;
	h->DataItemSize = itemSize;
	h->EnqueueIndex = 0;
	h->DequeueIndex = 0;
	h->BufferLenght = 0;
}

uint8_t* FifoDequeue(FifoHandler* h)
{
	uint8_t* tmp = 0;
	if (h->BufferLenght == 0)
	{
		return 0;
	}
	tmp = &h->DataBuffer[h->DequeueIndex];
	h->DequeueIndex += h->DataItemSize;
	h->BufferLenght -= h->DataItemSize;

	if (h->DequeueIndex >= h->DataBufferSize)
	{
		h->DequeueIndex = 0;
	}
	return tmp;
}

int8_t FifoEnqueue(FifoHandler* h, uint8_t* data)
{
	if (h->BufferLenght >= h->DataBufferSize)
	{
		return -1;
	}

	memcpy(&h->DataBuffer[h->EnqueueIndex], data, h->DataItemSize);
	h->EnqueueIndex += h->DataItemSize;
	h->BufferLenght += h->DataItemSize;

	if (h->EnqueueIndex >= h->DataBufferSize)
	{
		h->EnqueueIndex = 0;
	}

	return 0;
}