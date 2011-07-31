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
	 * len_1 : restaurant_name : len_2 : restaurant_info : len_3 : owner : len_4 : pwd : len_5 : pwd2 : len_6 : pwd3
	 * len_1 - 1-byte indicates the length of the restaurant name
	 * restaurant_name - restaurant name whose length equals "len_1"
	 * len_2 - 1-byte indicates the length of the restaurant info
	 * restaurant_info - restaurant info whose length equals "len_2"
	 * len_3 - 1-byte indicates the length of the terminal's owner name
	 * owner - the owner name of terminal
	 * len_4 : 1-byte indicates the length of the password
	 * pwd : the 1st password to this restaurant
	 * len_5 : 1-byte indicates the length of the password2
	 * pwd2 : the 2nd password to this restaurant
	 * len_6 : 1-byte indicates the length of the password3
	 * pwd3 : the 3rd password to this restaurant
	 *******************************************************/
	public RespQueryRestaurant(ProtocolHeader reqHeader, Restaurant restaurant) throws UnsupportedEncodingException{
		super(reqHeader);
		header.type = Type.ACK;
		byte[] name = restaurant.name.getBytes("UTF-16BE");	
		byte[] info = restaurant.info.getBytes("UTF-16BE");
		byte[] owner = restaurant.owner.getBytes("UTF-16BE");
		byte[] pwd = restaurant.pwd != null ? restaurant.pwd.getBytes() : new byte[0];
		byte[] pwd2 = restaurant.pwd2 != null ? restaurant.pwd2.getBytes() : new byte[0];
		byte[] pwd3 = restaurant.pwd3 != null ? restaurant.pwd3.getBytes() : new byte[0];
		//calculate the length of the body
		int bodyLen = 1 +	/* length of the name takes up 1 byte */
				      name.length + /* the name's length */
				      1 + /* length of the info takes up 1 byte */
				      info.length + /* the info length */
					  1 + /* length of the owner takes up 1 byte */
					  owner.length + /* the owner name length */
					  1 + /* length of the 1st password */
					  pwd.length + /* the 1st password */
					  1 + /* length of the 2nd password */
					  pwd2.length + /* the 2nd password */
					  1 + /* length of the 3rd password */
					  pwd3.length; /* the 3rd password */
					  
		
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
		//assign the owner 
		for(int i = 0; i < owner.length; i++){
			body[++offset] = owner[i];
		}
		
		//assign the length of the 1st password
		body[++offset] = (byte)(pwd.length & 0x000000FF);
		//assign the 1st password
		for(int i = 0; i < pwd.length; i++){
			body[++offset] = pwd[i];
		}
		
		//assign the length of the 2nd password
		body[++offset] = (byte)(pwd2.length & 0x000000FF);
		//assign the 2nd password
		for(int i = 0; i < pwd2.length; i++){
			body[++offset] = pwd2[i];
		}
		
		//assign the length of the 3rd password
		body[++offset] = (byte)(pwd3.length & 0x000000FF);
		//assign the 2nd password
		for(int i = 0; i < pwd3.length; i++){
			body[++offset] = pwd3[i];
		}
	}
}
