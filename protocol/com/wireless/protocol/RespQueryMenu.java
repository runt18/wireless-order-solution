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
 * food_amount[2] : <Food1> : <Food2>... : taste_amount : <Taste1> : <Taste2> ...
 * food_amount[2] - 2-byte indicating the amount of the foods listed in the menu
 * <Food>
 * food_id[2] : price[2] : len : name[len]
 * food_id[2] - 2-byte indicating the food's id
 * price[3] - 3-byte indicating the food's price
 * 			  price[0] 1-byte indicating the float point
 * 			  price[1..2] 2-byte indicating the fixed point
 * len - 1-byte indicating the length of the food's name
 * name[len] - the food's name whose length equals "len"
 * 
 * taste_amount - 1-byte indicates the amount of the taste preference
 * <Taste>
 * taste_id : len : preference[len]
 * taste_id - 1-byte indicating the alias id to this taste preference
 * len - 1-byte indicating the length of the preference
 * preference[len] - the string to preference whose length is "len"
 *******************************************************/
public class RespQueryMenu extends RespPackage{
	
	public RespQueryMenu(ProtocolHeader reqHeader, FoodMenu foodMenu) throws UnsupportedEncodingException{
		super(reqHeader);
		header.type = Type.ACK;
		//calculate the body's length
		int bodyLen = 2; /* the item number takes up 2-byte */
		
		for(int i = 0; i < foodMenu.foods.length; i++){
			byte[] name = foodMenu.foods[i].name.getBytes("UTF-16BE");
			/*
			 * each item of the food menu consist of the stuff below.
			 * food_id(2-byte) + price(3-byte) + length of food name(1-byte) + food's name(len-byte)
			 */
			bodyLen += 2 + 3 + 1 + name.length; 
		}
		
		/* the taste amount takes up 1-byte */
		bodyLen += 1;
		
		for(int i = 0; i < foodMenu.tastes.length; i++){
			byte[] preference = foodMenu.tastes[i].preference.getBytes("UTF-16BE");
			/*
			 * each taste preference consist of the stuff below.
			 * toast_id(1-byte) + length of the preference(1-byte) + preference description(len-byte)
			 */
			bodyLen += 1 + 1 + preference.length;
		}
		
		//assign the body length to the corresponding header's field
		header.length[0] = (byte)(bodyLen & 0x000000FF);
		header.length[1] = (byte)((bodyLen & 0x0000FF00) >> 8);
		
		//allocate the memory for the body
		body = new byte[bodyLen];
		
		//assign the food amount 
		body[0] = (byte)(foodMenu.foods.length & 0x000000FF);
		body[1] = (byte)((foodMenu.foods.length & 0x0000FF00) >> 8);
		
		//assign each food menu item value to the body 
		int index = 2;
		for(int i = 0; i < foodMenu.foods.length; i++){
			//assign the food's id
			body[index] = (byte)(foodMenu.foods[i].alias_id & 0x000000FF);
			body[index + 1] = (byte)((foodMenu.foods[i].alias_id & 0x0000FF00) >> 8);
			
			//assign the float-point of the price
			body[index + 2] = (byte)(foodMenu.foods[i].getPrice() & 0x000000FF);
			//assign the fixed-point of the price
			body[index + 3] = (byte)((foodMenu.foods[i].getPrice() & 0x0000FF00) >> 8);
			body[index + 4] = (byte)((foodMenu.foods[i].getPrice() & 0x00FF0000) >> 16);
			//assign the length of food's name
			byte[] name = foodMenu.foods[i].name.getBytes("UTF-16BE");
			body[index + 5] = (byte)(name.length & 0x000000FF);
			//assign the food name
			for(int cnt = 0; cnt < name.length; cnt++){
				body[index + 6 + cnt] = name[cnt];
			}
			/* 
			 * food_id(2-byte) + price(3-byte) + length of food's name(1-byte) + food's name 
			 */
			index += 2 + 3 + 1 + name.length; 
		}
		
		//assign the taste preference amount
		body[index] = (byte)(foodMenu.tastes.length & 0x000000FF);
		index++;
		
		//assign each taste preference to the body
		for(int i = 0; i < foodMenu.tastes.length; i++){
			//assign the taste preference alias id
			body[index] = (byte)(foodMenu.tastes[i].alias_id & 0x00FF);
			//assign the length of the preference string
			byte[] preference = foodMenu.tastes[i].preference.getBytes("UTF-16BE");
			body[index + 1] = (byte)(preference.length & 0x000000FF);
			//assign the preference string
			for(int cnt = 0; cnt < preference.length; cnt++){
				body[index + 2 + cnt] = preference[cnt];
			}
			/*
			 * toast_id(1-byte) + length of the preference(1-byte) + preference description(len-byte) 
			 */
			index += 1 + 1 + preference.length;
		}		
	}
}
