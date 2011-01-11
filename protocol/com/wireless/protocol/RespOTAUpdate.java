package com.wireless.protocol;

/******************************************************
 * In the case request OTA update successfully, 
 * design the response looks like below
 * mode : type : seq : reserved : pin[6] : len[2] : item1 : item2...
 * <Header>
 * mode - OTA
 * type - ACK
 * seq - same as request
 * reserved - 0x00
 * pin[6] - same as request
 * len[2] -  0x06,0x00
 * <Body>
 * ip_addr[4] - 4 bytes indicating the ip address
 * port[2] - 2 bytes indicating the port
 ********************************************************/
public class RespOTAUpdate extends RespPackage{
	public RespOTAUpdate(ProtocolHeader reqHeader, String ipAddr, String port){
		super(reqHeader);
		header.type = Type.ACK;
		header.length[0] = 0x06;
		header.length[1] = 0x00;
		//allocate the memory for the body
		body = new byte[6];
		//extract the ip address
		String[] addr = ipAddr.split("\\.");
		body[0] = (byte)Integer.parseInt(addr[0]);
		body[1] = (byte)Integer.parseInt(addr[1]);
		body[2] = (byte)Integer.parseInt(addr[2]);
		body[3] = (byte)Integer.parseInt(addr[3]);
		//extract the port
		int portVal = Integer.parseInt(port);
		body[4] = (byte)(portVal & 0x000000FF);
		body[5] = (byte)((portVal & 0x0000FF00) >> 8);
	}
}
