package com.wireless.pack.resp;

import com.wireless.pack.ProtocolHeader;
import com.wireless.pack.Type;

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
	
	public static class OTA{
		private final String mIPAddr;
		private final int mPort;
		
		OTA(String ipAddr, int ipPort){
			this.mIPAddr = ipAddr;
			this.mPort = ipPort;
		}
		
		public String getAddr(){
			return mIPAddr;
		}
		
		public int getPort(){
			return mPort;
		}
	}
	
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
	
	public static OTA parse(byte[] body){
		if(body != null){
			   String addr = Short.valueOf((short)(body[0] & 0xFF)) + "." + 
						  	 Short.valueOf((short)(body[1] & 0xFF)) + "." + 
						  	 Short.valueOf((short)(body[2] & 0xFF)) + "." + 
						  	 Short.valueOf((short)(body[3] & 0xFF));
			   
			   int port = (body[4] & 0x000000FF) | ((body[5] & 0x000000FF ) << 8);
			   
			   return new OTA(addr, port);
		}else{
			throw new NullPointerException();
		}
	}
}
