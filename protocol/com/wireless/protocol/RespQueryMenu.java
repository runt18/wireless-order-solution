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
 * food_amount[2] : <Food1> : <Food2>... 
 * taste_amount[2] : <Taste1> : <Taste2> ... 
 * style_amount[2] : <Style1> : <Style2>... 
 * spec_amount[2] : <Spec1> : <Spec2>... 
 * kitchen_amount : <Kitchen1> : <Kitchen2>...
 * s_kitchen_amount : <SKitchen1> : <SKitchen2>...
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
 * taste_amount[2] - 2-byte indicates the amount of the taste preference
 * <Taste>
 * taste_id[2] : category : calc : price[3] : rate[2] : len : preference[len]
 * taste_id[2] - 2-byte indicating the alias id to this taste preference
 * category - the category to this taste
 * calc - the calculate type to this taste
 * price[3] - 3-byte indicating the price to this taste
 * rate[2] - 2-byte indicating the rate to this taste
 * len - 1-byte indicating the length of the preference
 * preference[len] - the string to preference whose length is "len"
 * 
 * style_amount[2] - 2-byte indicates the amount of the taste style
 * <Style>
 * The same as <Taste>
 * 
 * spec_amount[2] - 2-byte indicates the specifications of the taste style
 * <Spec>
 * The same as <Taste>
 *  
 * <Kitchen>
 * kitchen_id : s_kitchen_id : dist_1 : dist_2 : dist_3 : mdist_1 : mdist_2 : mdist_3 : len : kname[len]
 * kitchen_id : the id to this kitchen
 * s_kitchen_id : the id to super kitchen which this kitchen belong to
 * dist_1..3 : 3 normal discounts to this kitchen
 * mdist_1..3 : 3 member discounts to this kitchen
 * len : the length of the kitchen name
 * kname[len] : the name to this kitchen
 * 
 * <SKitchen>
 * s_kitchen_id : len : s_kname[len]
 * s_kitchen_id : the id to this super kitchen
 * len : the length of the super kitchen name
 * s_kname[len] : the name to this super kitchen
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
			 * food_id(2-byte) + price(3-byte) + kitchen(1-byte) + status(1-byte) + 
			 * length of food name(1-byte) + food's name(len-byte) +
			 * length of the pinyn(1-byte) + food's pinyin(len-byte) 
			 */
			bodyLen += 2 + 3 + 1 + 1 + 1 + name.length + 1 + pinyin.length; 
		}
		
		//add the length to taste preferences
		bodyLen += tasteLen(foodMenu.tastes);
		
		//add the length to taste styles
		bodyLen += tasteLen(foodMenu.styles);
		
		//add the length to taste specifications
		bodyLen += tasteLen(foodMenu.specs);
		
		/* the amount of kitchen takes up 1-byte */
		bodyLen += 1;
		
		for(int i = 0; i < foodMenu.kitchens.length; i++){
			byte[] kname = foodMenu.kitchens[i].name.getBytes("UTF-16BE");
			/**
			 * each kitchen consist of the stuff below.
			 * kitchen_id(1-byte) + sKitchen_id(1-byte) + dist1..3(3-byte) + mdist1..3(3-byte) + length of the kitchen name(1-byte) + kitchen name(len-byte)
			 */
			bodyLen += 1 + 1 + 3 + 3 + 1 + kname.length;
		}
		
		
		/* the amount of super kitchen takes up 1-byte */
		bodyLen += 1;
		
		for(int i = 0; i < foodMenu.sKitchens.length; i++){
			byte[] sKname = foodMenu.sKitchens[i].name.getBytes("UTF-16BE");
			/**
			 * each super kitchen consist of the stuff below.
			 * skitchen_id(1-byte) + length of the super kitchen name(1-byte) + super kitchen name(len-byte)
			 */
			bodyLen += 1 + 1 + sKname.length;
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
			
			//assign the unit price to this food
			body[index + 2] = (byte)(foodMenu.foods[i].price & 0x000000FF);
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
		
		//assign the taste preferences
		index = fillTaste(foodMenu.tastes, index);
		
		//assign the taste styles
		index = fillTaste(foodMenu.styles, index);
		
		//assign the taste specifications
		index = fillTaste(foodMenu.specs, index);
		
		//assign the kitchen amount
		body[index] = (byte)(foodMenu.kitchens.length);
		index++;
		
		//assign each kitchen to the body
		for(int i = 0; i < foodMenu.kitchens.length; i++){
			//assign the kitchen id
			body[index] = (byte)(foodMenu.kitchens[i].kitchenAlias & 0x00FF);
			
			//assign the super kitchen id that this kitchen belong to 
			body[index + 1] = (byte)(foodMenu.kitchens[i].deptID & 0x00FF);
			
			//assign 3 normal discounts
			body[index + 2] = foodMenu.kitchens[i].discount;
			body[index + 3] = foodMenu.kitchens[i].discount_2;
			body[index + 4] = foodMenu.kitchens[i].discount_3;
			
			//assign 3 member discounts
			body[index + 5] = foodMenu.kitchens[i].member_discount_1;
			body[index + 6] = foodMenu.kitchens[i].member_discount_2;
			body[index + 7] = foodMenu.kitchens[i].member_discount_3;
			
			byte[] kname = foodMenu.kitchens[i].name.getBytes("UTF-16BE");
			//assign the length of the kitchen name
			body[index + 8] = (byte)(kname.length & 0x000000FF);
			
			//assign the kitchen name
			for(int cnt = 0; cnt < kname.length; cnt++){
				body[index + 9 + cnt] = kname[cnt];
			}
			
			/**
			 * kitchen_id(1-byte) + sKitchen_ID(1-byte) + dist1..3(3-byte) + mdist1..3(3-byte) + length of the kitchen name(1-byte) + kitchen name(len-byte)
			 */
			index += 1 + 1 + 3 + 3 + 1 + kname.length;
		}
		
		//assign the amount of super kitchen
		body[index] = (byte)(foodMenu.sKitchens.length);
		index++;
		
		//assign each super kitchen to the body
		for(int i = 0; i < foodMenu.sKitchens.length; i++){
			//assign the super kitchen id
			body[index] = (byte)(foodMenu.sKitchens[i].deptID & 0x00FF);
			
			//assign the length of the super kitchen name
			byte[] sKname = foodMenu.sKitchens[i].name.getBytes("UTF-16BE");
			body[index + 1] = (byte)(sKname.length & 0x000000FF);
			
			//assign the super kitchen name
			for(int cnt = 0; cnt < sKname.length; cnt++){
				body[index + 2 + cnt] = sKname[cnt];
			}
			
			/**
			 * each super kitchen consist of the stuff below.
			 * skitchen_id(1-byte) + length of the super kitchen name(1-byte) + super kitchen name(len-byte)
			 */
			index += 1 + 1 + sKname.length;
		}
	}
	
	private int tasteLen(Taste[] tastes) throws UnsupportedEncodingException{
		int bodyLen = 0;
		/* the taste amount takes up 2-byte */
		bodyLen += 2;
		
		for(int i = 0; i < tastes.length; i++){
			byte[] preference = tastes[i].preference.getBytes("UTF-16BE");
			/**
			 * each taste preference consist of the stuff below.
			 * toast_id(2-byte) + category(1-byte) + calc(1-byte) + 
			 * price(3-byte) + rate(2-byte) + length of the preference(1-byte) + preference description(len-byte)
			 */
			bodyLen += 2 + 1 + 1 + 3 + 2 + 1 + preference.length;
		}
		return bodyLen;
	}
	
	private int fillTaste(Taste[] tastes, int offset) throws UnsupportedEncodingException{
		//assign the taste preference amount
		body[offset] = (byte)(tastes.length & 0x000000FF);
		body[offset + 1] = (byte)((tastes.length & 0x0000FF00) >> 8);
		offset += 2;
		
		//assign each taste preference to the body
		for(int i = 0; i < tastes.length; i++){
			//assign the taste preference alias id
			body[offset] = (byte)(tastes[i].alias_id & 0x00FF);
			body[offset + 1] = (byte)((tastes[i].alias_id & 0xFF00) >> 8);
			     
			//assign the category of taste preference
			body[offset + 2] = (byte)(tastes[i].category & 0x00FF);
			//assign the calculate type of taste preference
			body[offset + 3] = (byte)(tastes[i].calc & 0x00FF);
			//assign the float-point of taste preference price
			body[offset + 4] = (byte)(tastes[i].price & 0x000000FF);
			//assign the fixed-point of the taste preference price
			body[offset + 5] = (byte)((tastes[i].price & 0x0000FF00) >> 8);
			body[offset + 6] = (byte)((tastes[i].price & 0x00FF0000) >> 16);
			//assign the rate of the taste preference
			body[offset + 7] = (byte)(tastes[i].rate & 0x00FF);
			body[offset + 8] = (byte)((tastes[i].rate & 0xFF00) >> 8);
			
			//assign the length of the preference string
			byte[] preference = tastes[i].preference.getBytes("UTF-16BE");
			body[offset + 9] = (byte)(preference.length & 0x000000FF);
			//assign the preference string
			for(int cnt = 0; cnt < preference.length; cnt++){
				body[offset + 10 + cnt] = preference[cnt];
			}
			/**
			 * each taste preference consist of the stuff below.
			 * toast_id(2-byte) + category(1-byte) + calc(1-byte) + 
			 * price(3-byte) + rate(2-byte) + length of the preference(1-byte) + preference description(len-byte)
			 */
			offset += 2 + 1 + 1 + 3 + 2 + 1 + preference.length;
		}	
		
		return offset;
	}
}
