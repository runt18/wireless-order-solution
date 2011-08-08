// protocol.cpp : Defines the exported functions for the DLL application.
//

#include "stdafx.h"
#include "../inc/Protocol.h"
#include <Windows.h>
#include <boost/shared_ptr.hpp>

/*******************************************************************************
* Function Name  : send
* Description    : Send the protocol package via the socket.
* Input          : socket - the socket connection
*                  pack - the protocol package to be sent
* Output         : None
* Return         : If no error occurs, send returns the total number of bytes sent, 
*                  which can be less than the number requested to be sent in the len parameter. 
*                  Otherwise, a value of SOCKET_ERROR is returned
*******************************************************************************/
int Protocol::send(SOCKET socket, const ProtocolPackage& pack){
	//calculate the length of body
	unsigned int body_len = (unsigned char)pack.header.length[0] | (unsigned char)pack.header.length[1] << 8;
	//calculate the length of entire package
	int len = ProtocolHeader::SIZE + body_len + strlen(ProtocolPackage::EOP);
	//allocate the memory
	boost::shared_ptr<char> send_buf(new char[len], boost::checked_array_deleter<char>());
	char* pSendBuf = send_buf.get();
	//assign the header
	pSendBuf[0] = pack.header.mode;
	pSendBuf[1] = pack.header.type;
	pSendBuf[2] = pack.header.seq;
	pSendBuf[3] = pack.header.reserved[0];
	pSendBuf[4] = pack.header.reserved[1];
	pSendBuf[5] = pack.header.reserved[2];
	pSendBuf[6] = pack.header.reserved[3];
	pSendBuf[7] = pack.header.pin[0];
	pSendBuf[8] = pack.header.pin[1];
	pSendBuf[9] = pack.header.pin[2];
	pSendBuf[10] = pack.header.pin[3];
	pSendBuf[11] = pack.header.pin[4];
	pSendBuf[12] = pack.header.pin[5];
	pSendBuf[13] = pack.header.length[0];
	pSendBuf[14] = pack.header.length[1];
	//assign the body
	for(unsigned int i = 0; i < body_len; i++){
		pSendBuf[ProtocolHeader::SIZE + i] = pack.body[i];
	}
	//assign the EOP
	for(unsigned int i = 0; i < strlen(ProtocolPackage::EOP); i++){
		pSendBuf[ProtocolHeader::SIZE + body_len + i] = *(ProtocolPackage::EOP + i);
	}
	//send the printer login buffer
	return ::send(socket, pSendBuf, len, 0);
}

/*******************************************************************************
* Function Name  : recv
* Description    : Receive the data and parse the data into package according to
                   the protocol.
* Input          : socket - the socket connection
                   rec_size - the size of the receive buffer
* Output         : pack - the result parsed from data read from socket
* Return         : If no error occurs, recv returns the number of bytes received 
*                  and the buffer pointed to by the buf parameter will contain this data received. 
*                  If the connection has been gracefully closed, the return value is zero
*******************************************************************************/
int Protocol::recv(SOCKET socket, int rec_size, ProtocolPackage& pack){
	//receive the header to package
	boost::shared_ptr<char> header_buf(new char[ProtocolHeader::SIZE], boost::checked_array_deleter<char>());
	int iResult = ::recv(socket, header_buf.get(), ProtocolHeader::SIZE, 0);
	if(iResult >= ProtocolHeader::SIZE){
		//assign the header
		pack.header.mode = header_buf.get()[0];
		pack.header.type = header_buf.get()[1];
		pack.header.seq = header_buf.get()[2];
		pack.header.reserved[0] = header_buf.get()[3];
		pack.header.reserved[1] = header_buf.get()[4];
		pack.header.reserved[2] = header_buf.get()[5];
		pack.header.reserved[3] = header_buf.get()[6];
		pack.header.pin[0] = header_buf.get()[7];
		pack.header.pin[1] = header_buf.get()[8];
		pack.header.pin[2] = header_buf.get()[9];
		pack.header.pin[3] = header_buf.get()[10];
		pack.header.pin[4] = header_buf.get()[11];
		pack.header.pin[5] = header_buf.get()[12];
		pack.header.length[0] = header_buf.get()[13];
		pack.header.length[1] = header_buf.get()[14];

		//calculate the length of the body
		unsigned int body_len = (unsigned char)pack.header.length[0] | 
							   (unsigned char)pack.header.length[1] << 8;

		body_len += strlen(ProtocolPackage::EOP);

		//allocate the memory for receiving body
		boost::shared_ptr<char> body_buf(new char[body_len], boost::checked_array_deleter<char>());
		unsigned int offset = 0;
		do{
			//receive the body to package
			iResult = ::recv(socket, body_buf.get() + offset, body_len - offset, 0);
			//In order to receive all the data blocks, 
			//the function "recv" might be invoked for some times if the data is huge.
			//So here loop to receive the data until the length of received data is equal to the header,
			//and sleep a short time before the data available.
			if(iResult > 0){
				offset += iResult;
				if(offset < body_len){
					//Sleep(200);
				}
			}else{
				break;
			}
		}while(offset < body_len);

		if(offset >= body_len){
			//allocate the memory for the body
			pack.body = new char[body_len];
			//assign the body
			for(unsigned int i = 0; i < body_len; i++){
				pack.body[i] = body_buf.get()[i];
			}
		}
	}
	return iResult;
}