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
 * food_amount[2] : <Food1> : <Food2>... : 
 * taste_amount : <Taste1> : <Taste2> ... :
 * kitchen_amount : <Kitchen1> : <Kitchen2>...
 * food_amount[2] - 2-byte indicating the amount of the foods listed in the menu
 * <Food>
 * food_id[2] : price[3] : status : kitchen : len : name[len] : len2 : pinyin[len2]
 * food_id[2] - 2-byte indicating the food's id
 * price[3] - 3-byte indicating the food's price
 * 			  price[0] 1-byte indicating the float point
 * 			  price[1..2] 2-byte indicating the fixed point
 * kitchen - the kitchen id to this food
 * len1 - the length of the food's name
 * name[len1] - the food's name whose length equals "len1"
 * len2 - the length of the pinyin
 * pinyin[len2] - the pinyin whose length equals "len2"
 * 
 * taste_amount - 1-byte indicates the amount of the taste preference
 * <Taste>
 * taste_id : price[3] : len : preference[len]
 * taste_id - 1-byte indicating the alias id to this taste preference
 * len - 1-byte indicating the length of the preference
 * preference[len] - the string to preference whose length is "len"
 * 
 * <Kitchen>
 * kitchen_id : dist_1 : dist_2 : dist_3 : mdist_1 : mdist_2 : mdist_3 : len : kname[len]
 * kitchen_id : the id to this kitchen
 * dist_1..3 : 3 normal discounts to this kitchen
 * mdist_1..3 : 3 member discounts to this kitchen
 * len : the length of the kitchen name
 * kname[len] : the name to this kitchen
 *******************************************************/
public class RespQueryMenu extends RespPackage{
	
	public RespQueryMenu(ProtocolHeader reqHeader, FoodMenu foodMenu) throws UnsupportedEncodingException{
		super(reqHeader);
		header.type = Type.ACK;
		//calculate the body's length
		int bodyLen = 2; /* the item number takes up 2-byte */
		
		for(int i = 0; i < foodMenu.foods.length; i++){
			byte[] name = foodMenu.foods[i].name.getBytes("UTF-16BE");
			byte[] pinyin = null;
			if(foodMenu.foods[i].pinyin != null){
				pinyin = foodMenu.foods[i].pinyin.getBytes();				
			}else{
				pinyin = new byte[0];
			}
			/**
			 * each item of the food menu consist of the stuff below.
			 * food_id(2-byte) + price(3-byte) + + kitchen(1-byte) + status(1-byte) + 
			 * length of food name(1-byte) + food's name(len-byte) +
			 * length of the pinyn(1-byte) + food's pinyin(len-byte) 
			 */
			bodyLen += 2 + 3 + 1 + 1 + 1 + name.length + 1 + pinyin.length; 
		}
		
		/* the taste amount takes up 1-byte */
		bodyLen += 1;
		
		for(int i = 0; i < foodMenu.tastes.length; i++){
			byte[] preference = foodMenu.tastes[i].preference.getBytes("UTF-16BE");
			/**
			 * each taste preference consist of the stuff below.
			 * toast_id(1-byte) + price(3-byte) + length of the preference(1-byte) + preference description(len-byte)
			 */
			bodyLen += 1 + 3 + 1 + preference.length;
		}
		
		/* the kitchen amount takes up 1-byte */
		bodyLen += 1;
		
		for(int i = 0; i < foodMenu.kitchens.length; i++){
			byte[] kname = foodMenu.kitchens[i].name.getBytes("UTF-16BE");
			/**
			 * each kitchen consist of the stuff below.
			 * kitchen_id(1-byte) + dist1..3(3-byte) + mdist1..3(3-byte) + length of the kitchen name(1-byte) + kitchen name(len-byte)
			 */
			bodyLen += 1 + 3 + 3 + 1 + kname.length;
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
			body[index + 2] = (byte)(foodMenu.foods[i].price & 0x000000FF);
			//assign the fixed-point of the price
			body[index + 3] = (byte)((foodMenu.foods[i].price & 0x0000FF00) >> 8);
			body[index + 4] = (byte)((foodMenu.foods[i].price & 0x00FF0000) >> 16);
			//assign the kitchen to this food
			body[index + 5] = (byte)foodMenu.foods[i].kitchen;
			//assign the status to this food
			body[index + 6] = (byte)foodMenu.foods[i].status;
			//assign the length of food's name
			byte[] name = foodMenu.foods[i].name.getBytes("UTF-16BE");
			body[index + 7] = (byte)(name.length & 0x000000FF);
			//assign the food name
			for(int cnt = 0; cnt < name.length; cnt++){
				body[index + 8 + cnt] = name[cnt];
			}
			byte[] pinyin;
			if(foodMenu.foods[i].pinyin != null){
				pinyin = foodMenu.foods[i].pinyin.getBytes();
			}else{
				pinyin = new byte[0];
			}
			//assign the length of the food's pinyin
			body[index + 8 + name.length] = (byte)(pinyin.length & 0x000000FF);
			//assign the food's pinyin
			for(int cnt = 0; cnt < pinyin.length; cnt++){
				body[index + 9 + name.length + cnt] = pinyin[cnt];
			}
			/* 
			 * food_id(2-byte) + price(3-byte) + kitchen(1-byte) + status(1-byte)
			 * length of food's name(1-byte) + food's name + 
			 * length of food's pinyin(1-byte) + food's pinyin
			 */
			index += 2 + 3 + 1 + 1 + 1 + name.length + 1 + pinyin.length; 
		}
		
		//assign the taste preference amount
		body[index] = (byte)(foodMenu.tastes.length & 0x000000FF);
		index++;
		
		//assign each taste preference to the body
		for(int i = 0; i < foodMenu.tastes.length; i++){
			//assign the taste preference alias id
			body[index] = (byte)(foodMenu.tastes[i].alias_id & 0x00FF);
			
			//assign the float-point of taste preference price
			body[index + 1] = (byte)(foodMenu.tastes[i].price & 0x000000FF);
			//assign the fixed-point of the taste preference price
			body[index + 2] = (byte)((foodMenu.tastes[i].price & 0x0000FF00) >> 8);
			body[index + 3] = (byte)((foodMenu.tastes[i].price & 0x00FF0000) >> 16);
			
			//assign the length of the preference string
			byte[] preference = foodMenu.tastes[i].preference.getBytes("UTF-16BE");
			body[index + 4] = (byte)(preference.length & 0x000000FF);
			//assign the preference string
			for(int cnt = 0; cnt < preference.length; cnt++){
				body[index + 5 + cnt] = preference[cnt];
			}
			/**
			 * toast_id(1-byte) + price(3-byte) + length of the preference(1-byte) + preference description(len-byte) 
			 */
			index += 1 + 3 + 1 + preference.length;
		}	
		
		//assign the kitchen amount
		body[index] = (byte)(foodMenu.kitchens.length);
		index++;
		
		//assign each kitchen to the body
		for(int i = 0; i < foodMenu.kitchens.length; i++){
			//assign the kitchen id
			body[index] = (byte)(foodMenu.kitchens[i].alias_id & 0x00FF);
			
			//assign 3 normal discounts
			body[index + 1] = foodMenu.kitchens[i].discount;
			body[index + 2] = foodMenu.kitchens[i].discount_2;
			body[index + 3] = foodMenu.kitchens[i].discount_3;
			
			//assign 3 member discounts
			body[index + 4] = foodMenu.kitchens[i].member_discount_1;
			body[index + 5] = foodMenu.kitchens[i].member_discount_2;
			body[index + 6] = foodMenu.kitchens[i].member_discount_3;
			
			byte[] kname = foodMenu.kitchens[i].name.getBytes("UTF-16BE");
			//assign the length of the kitchen name
			body[index + 7] = (byte)(kname.length & 0x000000FF);
			
			//assign the kitchen name
			for(int cnt = 0; cnt < kname.length; cnt++){
				body[index + 8 + cnt] = kname[cnt];
			}
			
			/**
			 * kitchen_id(1-byte) + dist1..3(3-byte) + mdist1..3(3-byte) + length of the kitchen name(1-byte) + kitchen name(len-byte)
			 */
			index += 1 + 3 + 3 + 1 + kname.length;
		}
	}
}
