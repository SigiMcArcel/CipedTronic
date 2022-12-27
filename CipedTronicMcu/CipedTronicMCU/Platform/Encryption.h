/*
 * Encryption.h
 *
 * Created: 11.12.2022 18:20:46
 *  Author: Siegwart
 */ 


#ifndef ENCRYPTION_H_
#define ENCRYPTION_H_
#include <stdint.h>

int8_t Encrypt(char* crypt,char* txt,char* key);
int8_t Decrypt(char* crypt,char* txt,char* key);
#endif /* ENCRYPTION_H_ */