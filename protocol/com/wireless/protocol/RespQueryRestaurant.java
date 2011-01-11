package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

public class RespQueryRestaurant extends RespPackage{
	/******************************************************
	 * In the case query restaurant successfully, 
	 * design the query restaurant response looks like below
	 * mode : type : seq : reserved : pin[6] : len[2] : <Body>
	 * <Header>
	 * mode - ORDER_BUSSINESS
	 * type - ACK
	 * seq - same as request
	 * reserved : 0x00
	 * pin[6] : same as request
	 * len[2] -  length of the <Body>
	 * <Body>
	 * len_1 : restaurant_name : len_2 : restaurant_info 
	 * len_1 - 1-byte indicates the length of the restaurant name
	 * restaurant_name - restaurant name whose length equals "len_1"
	 * len_2 - 1-byte indicates the length of the restaurant info
	 * restaurant_info - restaurant info whose length equals "len_2"
	 * len_3 - 1-byte indicates the length of the terminal's owner name
	 * owner - the owner name of terminal
	 *******************************************************/
	public RespQueryRestaurant(ProtocolHeader reqHeader, Restaurant restaurant) throws UnsupportedEncodingException{
		super(reqHeader);
		header.type = Type.ACK;
		byte[] name = restaurant.name.getBytes("UTF-16BE");	
		byte[] info = restaurant.info.getBytes("UTF-16BE");
		byte[] owner = restaurant.owner.getBytes("UTF-16BE");
		//calculate the length of the body
		int bodyLen = 1 +	/* length of the name takes up 1 byte */
				      name.length + /* the name's length */
				      1 + /* length of the info takes up 1 byte */
				      info.length + /* the info length */
					  1 + /* length of the owner takes up 1 byte */
					  owner.length; /* the owner name length */
		
		//assign the body length to header's length field
		header.length[0] = (byte)(bodyLen & 0x000000FF);
		header.length[1] = (byte)((bodyLen & 0x0000FF00) >> 8);
		
		//allocate the memory for body
		body = new byte[bodyLen];
		int offset = 0;
		//assign the length of the restaurant name
		body[offset] = (byte)(name.length & 0x000000FF);
		//assign the restaurant name
		for(int i = 0; i < name.length; i++){
			body[++offset] = name[i];
		}
		//assign the length of the restaurant info
		body[++offset] = (byte)(info.length & 0x000000FF);
		//assign the restaurant info
		for(int i = 0; i < info.length; i++){
			body[++offset] = info[i];
		}
		//assign the length of owner name
		body[++offset] = (byte)(owner.length & 0x000000FF);
		for(int i = 0; i < owner.length; i++){
			body[++offset] = owner[i];
		}
	}
}
