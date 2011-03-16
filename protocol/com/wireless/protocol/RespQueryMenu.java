package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

/******************************************************
 * In the case query menu not successfully
 * <Header>
 * mode : type : seq : reserved : pin[6] : len[2]
 * mode - ORDER_BUSSINESS
 * type - NAK
 * seq - same as request
 * reserved - 0x00
 * pin[6] - same as request
 * len[2] - 0x00, 0x00
 *******************************************************/

/******************************************************
 * In the case query menu successfully, 
 * design the query menu response looks like below
 * mode : type : seq : reserved : pin[6] : len[2] : item1 : item2...
 * <Header>
 * mode - ORDER_BUSSINESS
 * type - ACK
 * seq - same as request
 * reserved - 0x00
 * pin[6] - same as request
 * len[2] -  length of the <Body>
 * <Body>
 * item_amount[2] : <Item1> : <Item2>...
 * item_amount[2] - 2-byte indicating the amount of the foods listed in the menu
 * <Item>
 * food_id[2] : price[2] : len : name[len]
 * food_id[2] - 2-byte indicating the food's id
 * price[3] - 3-byte indicating the food's price
 * 			  price[0] 1-byte indicating the float point
 * 			  price[1..2] 2-byte indicating the fixed point
 * len - 1-byte indicating the length of the food's name
 * name[len] - the food's name whose length equals "len"
 *******************************************************/
public class RespQueryMenu extends RespPackage{
	
	public RespQueryMenu(ProtocolHeader reqHeader, Food[] foodMenu) throws UnsupportedEncodingException{
		super(reqHeader);
		header.type = Type.ACK;
		//calculate the body's length
		int bodyLen = 2; /* the item number takes up 2-byte */
		
		for(int i = 0; i < foodMenu.length; i++){
			byte[] name = foodMenu[i].name.getBytes("UTF-16BE");
			/*
			 * each item of the food menu consist of the stuff below.
			 * food_id(2-byte) + price(3-byte) + length of food name(1-byte) + food's name(len-byte)
			 * */
			bodyLen += 2 + 3 + 1 + name.length; 
		}
		
		//assign the body length to the corresponding header's field
		header.length[0] = (byte)(bodyLen & 0x000000FF);
		header.length[1] = (byte)((bodyLen & 0x0000FF00) >> 8);
		
		//allocate the memory for the body
		body = new byte[bodyLen];
		//assign the item number 
		body[0] = (byte)(foodMenu.length & 0x000000FF);
		body[1] = (byte)((foodMenu.length & 0x0000FF00) >> 8);
		
		//assign each food menu item value to the body 
		int index = 2;
		for(int i = 0; i < foodMenu.length; i++){
			//assign the food's id
			body[index] = (byte)(foodMenu[i].alias_id & 0x000000FF);
			body[index + 1] = (byte)((foodMenu[i].alias_id & 0x0000FF00) >> 8);
			
			//assign the float-point of the price
			body[index + 2] = (byte)(foodMenu[i].getPrice() & 0x000000FF);
			//assign the fixed-point of the price
			body[index + 3] = (byte)((foodMenu[i].getPrice() & 0x0000FF00) >> 8);
			body[index + 4] = (byte)((foodMenu[i].getPrice() & 0x00FF0000) >> 16);
			//assign the length of food's name
			byte[] name = foodMenu[i].name.getBytes("UTF-16BE");
			body[index + 5] = (byte)(name.length & 0x000000FF);
			//assign the food name
			for(int cnt = 0; cnt < name.length; cnt++){
				body[index + 6 + cnt] = name[cnt];
			}
			/* 
			 * food_id(2-byte) + price(3-byte) + length of food's name(1-byte) + food's name 
			 **/
			index += 2 + 3 + 1 + name.length; 
		}
	}
}
