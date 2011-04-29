package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

/******************************************************
 * In the case printer login successfully, 
 * design the response looks like below
 * mode : type : seq : reserved : pin[6] : len[2] : nKitchen : <kitchen_1> : ... : <kitchen_n>
 * <Header>
 * mode - PRINT
 * type - ACK
 * seq - same as request
 * reserved - 0x00
 * pin[6] - same as request
 * len[2] -  length of the <Body>
 * <Body>
 * nKitchen : <kitchen_1> : ... : <kitchen_n>
 * nKitchen - the number of kitchens
 * <kitchen_x>
 * len_name : name
 * len_name - the length of the kitchen name
 * name - the name to kitchen
 *******************************************************/
public class RespPrintLogin extends RespPackage{
	public RespPrintLogin(ProtocolHeader reqHeader, Kitchen[] kitchens){
		super(reqHeader);
		header.mode = Mode.PRINT;
		header.type = Type.ACK;

		//calculate the length of body
		int len = 0;
		//the number of kitchens takes up 1-byte
		len += 1;
		
		//the byte array to hold the name to kitchen
		byte[][] nameBytes = new byte[kitchens.length][];
		
		for(int i = 0; i < nameBytes.length; i++){
			//the length of the name takes up 1-byte
			len += 1;
			try{
				nameBytes[i] = kitchens[i].name.getBytes("GBK");
			}catch(UnsupportedEncodingException e){
				
			}
			//the name of kitchen takes up the byte arrays' length
			len += nameBytes[i].length;
		}
		//allocate the memory for the body
		body = new byte[len];
		
		int offset = 0;
		//assign the number of kitchens
		body[offset] = (byte)kitchens.length;
		offset++;
		
		for(int i = 0; i < nameBytes.length; i++){
			//assign the length to kitchen name
			body[offset] = (byte)nameBytes[i].length;
			//assign the name of kitchen
			System.arraycopy(nameBytes[i], 0, body, offset + 1, nameBytes[i].length);
			offset += 1 + /* the length of name takes up 1-byte */
					  nameBytes[i].length; /* the name of kitchen takes up the byte arrays' length */
		}
		
		header.length[0] = (byte)(body.length & 0x000000FF);
		header.length[1] = (byte)((body.length & 0x0000FF00) >> 8);
	}
}
