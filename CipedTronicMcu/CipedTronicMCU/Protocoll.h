/*
 * Protocoll.h
 *
 * Created: 07.06.2022 20:27:53
 *  Author: Siegwart
 * Protocoll format
 * <CIP [ID] [CMD] [Command Specific] [Private Key]>
 * ID  : 
		A String 8 characters for recognize the right CIPEDtronic device
 * CMD : 
		Commands
 *		DE Cipedtronic Data Event		: <CIP [Version] [ID] [CMD] [Velocity] [Velocity max] [Velocity Average] [Distance] >
		DR Data Read Device to phone    : <CIP [ID] [CMD] [Register Number] [Lengh] [Data] [Data] [Data] [Data]>
		DW Data Write Phone to device   : <CIP [ID] [CMD] [Register Number] [Lengh] [Data] [Data] [Data] [Data]>
		RS Reset Counter Data			: <CIP [ID] [CMD]>
		SI Set ID						: <CIP [ID] [CMD]>
		SK Set Encryption Key			: <CIP [ID] [CMD] [old Encryption Key] [new Encryption Key]>
		SE Status Event					: <CIP [ID] [CMD] [Status] [Encryption Key]>
		EE Error Event					: <CIP [ID] [CMD] [Error] [Encryption Key]>
	Encryption Key:
		A String Key of 8 Chars only the value are cyrpt
 */ 


#ifndef PROTOCOLL_H_
#define PROTOCOLL_H_
#ifdef __cplusplus
extern "C" {
#endif
void ProtocolInit();
void ProtocolHandler();
void ProtocolRxHandler();

#ifdef __cplusplus
}
#endif
#endif /* PROTOCOLL_H_ */