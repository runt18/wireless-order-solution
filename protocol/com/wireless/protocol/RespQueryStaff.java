package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

public class RespQueryStaff extends RespPackage{
	/******************************************************
	 * In the case query staff successfully, 
	 * design the query staff response looks like below
	 * mode : type : seq : reserved : pin[6] : len[2] : <Body>
	 * <Header>
	 * mode - ORDER_BUSSINESS
	 * type - ACK
	 * seq - same as request
	 * reserved : 0x00
	 * pin[6] : same as request
	 * len[2] -  length of the <Body>
	 * <Body>
	 * nStaff : <staff_1> : ... : <staff_n>
	 * nStaff - the amount of staff
	 * <staff_n>
	 * len_1 : name : len_2 : pwd : pin[4]
	 * len_1 - the length to the staff name
	 * name - the value to staff name
	 * len_2 - the length to the staff password
	 * pwd - the value to the staff password
	 * pin[4] - 4-byte indicating the pin
	 *******************************************************/	
	public RespQueryStaff(ProtocolHeader reqHeader, StaffTerminal[] staffs) throws UnsupportedEncodingException{
		super(reqHeader);
		header.type = Type.ACK;
		
		/**
		 * calculate the body length
		 */		
		int bodyLen = 1;	/* the amount of staff takes up 1-byte */
		
		for(int i = 0; i < staffs.length; i++){
			byte[] name = staffs[i].name.getBytes("UTF-16BE");
			byte[] pwd = staffs[i].pwd.getBytes("UTF-16BE");
			bodyLen += 1 +				/* the length to staff name takes up 1-byte */
					   name.length + 	/* the name to staff */
					   1 + 				/* the length to staff password takes up 1-byte */
					   pwd.length + 	/* the password to staff */
					   4;				/* the pin takes up 4-byte */
		}
		
		//allocate the memory for the body
		body = new byte[bodyLen];
		
		//assign the amount of staff
		body[0] = (byte)(staffs.length & 0x000000FF);
		
		int offset = 1;
		for(int i = 0; i < staffs.length; i++){
			byte[] name = staffs[i].name.getBytes("UTF-16BE");
			//assign the length of staff name to body
			body[offset] = (byte)name.length;
			offset++;
			//assign the staff name to body
			System.arraycopy(name, 0, body, offset, name.length);
			offset += name.length;
			
			byte[] pwd = staffs[i].pwd.getBytes();
			//assign the length of password to body
			body[offset] = (byte)pwd.length; 
			offset++;
			
			//assign the staff password to body
			System.arraycopy(pwd, 0, body, offset, pwd.length);
			offset += pwd.length;
			
			//assign the pin to body
			body[offset] = (byte)(staffs[i].pin & 0x00000000000000FF);
			body[offset + 1] = (byte)((staffs[i].pin & 0x000000000000FF00) >> 8);
			body[offset + 2] = (byte)((staffs[i].pin & 0x0000000000FF0000) >> 16);
			body[offset + 3] = (byte)((staffs[i].pin & 0x00000000FF000000) >> 24);
			offset += 4;
		}
		
	}
}
