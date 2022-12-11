/*
 * Encyption.c
 *
 * Created: 11.12.2022 18:21:11
 *  Author: Siegwart
 */ 
#include <string.h>
#include "Encryption.h"

int8_t Encrypt(char* crypt,char* txt,char* key)
{
	int32_t keylen = 0;
	int32_t txtlen = 0;
	int32_t cryptlen = 0;
	int32_t txtcount = 0;
	
	if(crypt == 0 || txt == 0 || key == 0)
	{
		return -1;
	}
	keylen = strlen(key);
	txtlen = strlen(txt);
	cryptlen = strlen(crypt);
	if(cryptlen == 0 || txtlen == 0 || keylen == 0)
	{
		return -2;
	}
	if(cryptlen != txtlen)
	{
		return -3;
	}
	
	for(txtcount = 0;txtcount < txtlen;txtcount++)
	{
		int32_t keyCount = txtcount%keylen;
		crypt[txtcount] = txt[txtcount] ^ key[keyCount];
	}
	return txtcount;
}
int8_t Decrypt(char* crypt,char* txt,char* key)
{
	return Encrypt(txt,crypt,key);
}