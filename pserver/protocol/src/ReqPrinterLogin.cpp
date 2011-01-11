#include "stdafx.h"
#include "../inc/ReqPrinterLogin.h"
#include "../inc/Mode.h"
#include "../inc/Type.h"

/****************************************************************************
 * The login request is as below
 * mode : type : seq : reserved : pin[6] : len[2] : len1 : user : len2 : pwd
 * <Header>
 * mode - PRINT
 * type - ACK
 * seq - auto calculated and filled in
 * reserved - 0x00
 * pin[6] - 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
 * len[2] -  length of the <Body>
 * <Body>
 * len1 - 1-byte indicates the length of the user name
 * user - the user name
 * len2 - 1-byte indicates the length of the password
 * pwd - the password related to the user 
 ****************************************************************************/	
ReqPrinterLogin::ReqPrinterLogin(const char* user, const char* pwd){
	header.mode = Mode::PRINT;
	header.type = Type::PRINTER_LOGIN;
	//calculate the length of body
	int len = strlen(user) + strlen(pwd) + 2;
	header.length[0] = (unsigned char)(len & 0x000000FF);
	header.length[1] = (unsigned char)((len & 0x0000FF00) >> 8);
	//allocate the memory for the body
	body = new char[len];
	int offset = 0;
	//set the length of user name
	body[offset] = (char)(strlen(user) & 0x000000FF);
	offset++;
	//set the user name value
	for(unsigned int i = 0; i < strlen(user); i++){
		body[offset] = *(user + i);
		offset++;
	}
	//set the length of password
	body[offset] = (char)(strlen(pwd) & 0x000000FF);
	offset++;
	//set the password value
	for(unsigned int i = 0; i < strlen(pwd); i++){
		body[offset] = *(pwd + i);
		offset++;
	}
}

ReqPrinterLogin::~ReqPrinterLogin(){

}