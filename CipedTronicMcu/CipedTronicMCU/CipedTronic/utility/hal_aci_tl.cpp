/* Copyright (c) 2009 Nordic Semiconductor. All Rights Reserved.
 *
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC
 * SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT. 
 *
 * Licensees are granted free, non-transferable use of the information. NO
 * WARRANTY of ANY KIND is provided. This heading must NOT be removed from
 * the file.
 *
 * $LastChangedRevision: 4808 $
 */ 

/** @file
@brief Implementation of the ACI transport layer module
*/

#include <string.h>
#ifdef __cplusplus
extern "C" {
#endif
#include "../../Platform/SPI.h"
#include "../../Platform/USBSerial.h"
#include "../../Platform/GPIO.h"
#include "../../Platform/Timer.h"
#ifdef __cplusplus
}
#endif
#include "hal/hal_aci_tl.h"
#include "ble_system.h"


static void setRequestLine(uint8_t val);
static uint8_t getReadyLine();
static uint8_t waitReadyLine(uint32_t timeout);

static void ReadyLineOccured(uint8_t num)
{
	if(num == 6)
	{
		hal_aci_tl_poll_rdy_line(0);
	}
}

static void           m_print_aci_data(hal_aci_data_t *p_data);

typedef struct {
 hal_aci_data_t           aci_data[ACI_QUEUE_SIZE];
 uint8_t                  head;
 uint8_t                  tail;
} aci_queue_t;

static hal_aci_data_t received_data;
static uint8_t        spi_readwrite(uint8_t aci_byte);
static bool           aci_debug_print;




static aci_queue_t    aci_tx_q;
static aci_queue_t    aci_rx_q;

static volatile uint32_t start = 0;
static volatile uint8_t txPending = 0;

static void setRequestLine(uint8_t val)
{
	if(val)
	{
		GPIOSet(7,&PORTB);
	}
	else
	{
		GPIOReset(7,&PORTB);
	}
}

static uint8_t getReadyLine()
{
	return GPIOGet(6,&PINB);
}

static uint8_t waitReadyLine(uint32_t timeout)
{
	start = TimerGetTick();
	while(getReadyLine())
	{
		if((TimerGetTick() - start) > timeout)
		{
			USBSerialPuts(("Timeout\r\n"));
			return 0;
		} 
	}
	return 1;
}

static uint8_t waitNotReadyLine(uint32_t timeout)
{
	start = TimerGetTick();
	while(!getReadyLine())
	{
		if((TimerGetTick() - start) > timeout)
		{
			USBSerialPuts(("Timeout\r\n"));
			return false;
		}
	}
	return true;
}

static void m_aci_q_init(aci_queue_t *aci_q)
{
  uint8_t loop;
  aci_q->head = 0;
  aci_q->tail = 0;
  for(loop=0; loop<ACI_QUEUE_SIZE; loop++)
  {
    aci_tx_q.aci_data[loop].buffer[0] = 0x00;
    aci_tx_q.aci_data[loop].buffer[1] = 0x00;
  }
}

void hal_aci_debug_print(bool enable)
{
	aci_debug_print = enable;
}

static bool m_aci_q_enqueue(aci_queue_t *aci_q, hal_aci_data_t *p_data)
{
  const uint8_t next = (aci_q->tail + 1) % ACI_QUEUE_SIZE;
  const uint8_t length = p_data->buffer[0];
  
  if (next == aci_q->head)
  {
    /* full queue */
    return false;
  }
  aci_q->aci_data[aci_q->tail].status_byte = 0;
  
  memcpy((uint8_t *)&(aci_q->aci_data[aci_q->tail].buffer[0]), (uint8_t *)&p_data->buffer[0], length + 1);
  aci_q->tail = next;
  
  return true;
}

//@comment after a port have test for the queue states, esp. the full and the empty states
static bool m_aci_q_dequeue(aci_queue_t *aci_q, hal_aci_data_t *p_data)
{
  if (aci_q->head == aci_q->tail)
  {
    /* empty queue */
    return false;
  }
  
  memcpy((uint8_t *)p_data, (uint8_t *)&(aci_q->aci_data[aci_q->head]), sizeof(hal_aci_data_t));
  aci_q->head = (aci_q->head + 1) % ACI_QUEUE_SIZE;
  
  return true;
}

static bool m_aci_q_is_empty(aci_queue_t *aci_q)
{
  return (aci_q->head == aci_q->tail);
}

static bool m_aci_q_is_full(aci_queue_t *aci_q)
{
  uint8_t next;
  bool state;
  
  //This should be done in a critical section
  cli();
  next = (aci_q->tail + 1) % ACI_QUEUE_SIZE;  
  
  if (next == aci_q->head)
  {
    state = true;
  }
  else
  {
    state = false;
  }
  
  sei();
  //end
  
  return state;
}

void m_print_aci_data(hal_aci_data_t *p_data)
{
  const uint8_t length = p_data->buffer[0];
  uint8_t i;
  
  USBSerialPutsLong(length);
  USBSerialPuts(" :");
  for (i=0; i<=length; i++)
  {
    USBSerialPutsHex8(p_data->buffer[i]);
    USBSerialPuts(", ");
  }
  USBSerialPuts("\r\n");
}


uint8_t hal_aci_tl_poll_rdy_line(uint32_t timeout)
{
	hal_aci_data_t *p_aci_data;
	
	if(timeout > 0)
	{
		if(!waitReadyLine(timeout))
		{
			return 0;
		}
	}
	if(!getReadyLine() || txPending)
	{
		txPending = 0;
		p_aci_data = hal_aci_tl_poll_get();
		 // Check if we received data
		if (p_aci_data->buffer[0] > 0)
		{
			if (!m_aci_q_enqueue(&aci_rx_q, p_aci_data))
			{
				/* Receive Buffer full.
					Should never happen.
					Spin in a while loop.
					*/
				 USBSerialPuts("event buffer full \r\n");
			}
		}
	}
	return 1;
}

bool hal_aci_tl_event_get(hal_aci_data_t *p_aci_data)
{
	
  bool was_full = m_aci_q_is_full(&aci_rx_q);
  
  if (m_aci_q_dequeue(&aci_rx_q, p_aci_data))
  {
	 
	  
	  if (was_full)
	  {
		 m_aci_q_flush();
	  }
	  return true;
  }
  else
  {
	  return false;
  }
}

void hal_aci_tl_init()
{
	USBSerialPuts("Set GPIOs\r\n");
   GPIOSetDirection(6,&DDRB,GPIO_DIR_IN);//Ready line
   GPIOSetDirection(7,&DDRB,GPIO_DIR_OUT);//Request line
   GPIOSetDirection(5,&DDRB,GPIO_DIR_OUT);//reset
  GPIOSet(6,&PORTB);
   USBSerialPuts("Set Reset and Req line to 1\r\n");
    setRequestLine(1);
	GPIOSet(5,&PORTB);
	USBSerialPuts("Init SPI\r\n");
   SPIInit(SPI_CPOLPOS,SPI_CPHAFALLING,SPI_CLKDIV_4,SPI_LSB,1);
   m_aci_q_init(&aci_tx_q);
   m_aci_q_init(&aci_rx_q);
   USBSerialPuts("Reset nrf\r\n");
   USBSerialPuts("Init INT\r\n");
   
    GPIOReset(5,&PORTB);
    TimerWait(1000);
    GPIOSet(5,&PORTB);
    TimerWait(1000);
	 USBSerialPuts(" Wait for readyline\r\n");
}

bool hal_aci_tl_send(hal_aci_data_t *p_aci_cmd)
{
  const uint8_t length = p_aci_cmd->buffer[0];
 
  if (length > HAL_ACI_MAX_LENGTH)
  {
    return false;
  }
  else
  {
	 
	if (!m_aci_q_enqueue(&aci_tx_q,p_aci_cmd))
	{
		USBSerialPuts("tx buffer full \r\n");
		return false;
	}
	txPending = 1;
  }
  return true;
}


hal_aci_data_t * hal_aci_tl_poll_get(void)
{
  uint8_t byte_cnt;
  uint8_t byte_sent_cnt;
  uint8_t max_bytes;
  hal_aci_data_t data_to_send;



  
  setRequestLine(0);

  // Receive from queue
  if (m_aci_q_dequeue(&aci_tx_q, &data_to_send) == false)
  {
    /* queue was empty, nothing to send */
    data_to_send.status_byte = 0;
    data_to_send.buffer[0] = 0;
  }
  else
  {
	   if (true == aci_debug_print)
	   {
		   USBSerialPuts("C");
		   m_print_aci_data(&data_to_send);
	   }
	   for(int i = 0;i < 20000;i++){}
  }
 
  
  //Change this if your mcu has DMA for the master SPI
  
  // Send length, receive header
  byte_sent_cnt = 0;
  received_data.status_byte = spi_readwrite(data_to_send.buffer[byte_sent_cnt++]);
  
  // Send first byte, receive length from slave
  received_data.buffer[0] = spi_readwrite(data_to_send.buffer[byte_sent_cnt++]);
 
 
  if (0 == data_to_send.buffer[0])
  {
    max_bytes = received_data.buffer[0];
  }
  else
  {
    // Set the maximum to the biggest size. One command byte is already sent
    max_bytes = (received_data.buffer[0] > (data_to_send.buffer[0] - 1)) 
      ? received_data.buffer[0] : (data_to_send.buffer[0] - 1);
  }

  if (max_bytes > HAL_ACI_MAX_LENGTH)
  {
    max_bytes = HAL_ACI_MAX_LENGTH;
  }

  // Transmit/receive the rest of the packet 
  for (byte_cnt = 0; byte_cnt < max_bytes; byte_cnt++)
  {
    received_data.buffer[byte_cnt+1] =  spi_readwrite(data_to_send.buffer[byte_sent_cnt++]);
	
  }

	setRequestLine(1);
	GPIOReset(1,&PORTB);
	GPIOReset(2,&PORTB);
	for(int i = 0;i < 20000;i++){}
	if (true == aci_debug_print)
	{
		 USBSerialPuts("E");
		 m_print_aci_data(&received_data);
	}


  
	if (false == m_aci_q_is_empty(&aci_tx_q))
	{
		//Lower the REQN line to start a new ACI transaction         
		setRequestLine(0);
	}

	return (&received_data);
}

static uint8_t spi_readwrite(const uint8_t aci_byte)
{
	SPIWriteByte(aci_byte);
	return SPIReadByte();
}

void m_aci_q_flush(void)
{
  cli();
  /* re-initialize aci cmd queue and aci event queue to flush them*/
  m_aci_q_init(&aci_tx_q);
  m_aci_q_init(&aci_rx_q);
  sei();
}
