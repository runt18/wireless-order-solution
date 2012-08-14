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
 * food_amount[2] : <Food_1> : <Food_2>... 
 * taste_amount[2] : <Taste_1> : <Taste_2> ... 
 * style_amount[2] : <Style_1> : <Style_2>... 
 * spec_amount[2] : <Spec_1> : <Spec_2>... 
 * kitchen_amount : <Kitchen_1> : <Kitchen_2>...
 * dept_amount : <Dept_1> : <Dept_2>...
 * 
 * food_amount[2] - 2-byte indicating the amount of the foods listed in the menu
 * <Food>
 * food_id[2] : price[3] : status : kitchen : name_len : name[name_len] : pinyin_len : pinyin[pinyin_len] : 
 * img_len : image : taste_ref_amount : taste_alias_1[2] : tase_alias_2[2]...
 * food_id[2] - 2-byte indicating the food's id
 * price[3] - 3-byte indicating the food's price
 * 			  price[0] 1-byte indicating the float point
 * 			  price[1..2] 2-byte indicating the fixed point
 * kitchen - the kitchen id to this food
 * name_len - the length of the food's name
 * name[name_len] - the food's name whose length equals "len1"
 * pinyin_len - the length of the pinyin
 * pinyin[pinyin_len] - the pinyin whose length equals "len2"
 * img_len : the length to image
 * image : the image file name
 * taste_ref_amount - the amount to taste reference
 * taste_alias_n[2] - 2-byte indicating the alias id to each taste reference 
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
 * kitchen_id : dept_id : dist_1 : dist_2 : dist_3 : mdist_1 : mdist_2 : mdist_3 : len : kname[len]
 * kitchen_id : the id to this kitchen
 * dept_id : the id to department which this kitchen belong to
 * dist_1..3 : 3 normal discounts to this kitchen
 * mdist_1..3 : 3 member discounts to this kitchen
 * len : the length of the kitchen name
 * kname[len] : the name to this kitchen
 * 
 * <Department>
 * dept_id : len : dept_name[len]
 * dept_id : the id to this department
 * len : the length of the department name
 * dept_name[len] : the name to this department
 *******************************************************/
public class RespQueryMenu extends RespPackage{
	
	public RespQueryMenu(ProtocolHeader reqHeader, FoodMenu foodMenu) throws UnsupportedEncodingException{
		super(reqHeader);
		header.type = Type.ACK;
		
		//calculate the body's length
		
		int bodyLen = 2; /* the item number takes up 2-byte */
		
		for(int i = 0; i < foodMenu.foods.length; i++){
			byte[] name = foodMenu.foods[i].name.getBytes("UTF-16BE");
			byte[] pinyin;
			if(foodMenu.foods[i].pinyin != null){
				pinyin = foodMenu.foods[i].pinyin.getBytes();				
			}else{
				pinyin = new byte[0];
			}
			byte[] image;
			if(foodMenu.foods[i].image != null){
				image = foodMenu.foods[i].image.getBytes();
			}else{
				image = new byte[0];
			}
			/**
			 * Each item of the food menu consist of the stuff below.
			 */
			bodyLen += 2 + 				/* food_alias(2-byte) */
					   3 + 				/* price(3-byte) */ 
					   1 + 				/* kitchen id to this food(1-byte) */
					   1 + 				/* status(1-byte) */ 
					   1 + 				/* the length to food name(1-byte) */
					   name.length + 	/* the value to food name  */
					   1 + 				/* the length to pinyin(1-byte) */	
					   pinyin.length + 	/* the value to pinyin */
					   1 +				/* the length to image */
					   image.length +	/* the value to image name */
					   1 + 				/* the amount to taste reference(1-byte) */
					   (foodMenu.foods[i].popTastes == null ? 0 : (foodMenu.foods[i].popTastes.length * 2)); /* all the alias id to taste reference */ 
		}
		
		//add the length to taste preferences
		bodyLen += calcTasteLen(foodMenu.tastes);
		
		//add the length to taste styles
		bodyLen += calcTasteLen(foodMenu.styles);
		
		//add the length to taste specifications
		bodyLen += calcTasteLen(foodMenu.specs);
		
		/* the amount of kitchen takes up 1-byte */
		bodyLen += 1;
		
		for(int i = 0; i < foodMenu.kitchens.length; i++){
			byte[] kitchenName = foodMenu.kitchens[i].name.getBytes("UTF-16BE");
			/**
			 * Each kitchen consist of the stuff below.
			 */
			bodyLen += 1 + 					/* kitchen_alias(1-byte) */
					   1 + 					/* dept_id(1-byte) */
					   3 + 					/* normal discount 1..3(3-byte) */
					   3 + 					/* member discount 1..3(3-byte) */
					   1 + 					/* length to kitchen name */
					   kitchenName.length;	/* the value to kitchen */
		}
		
		
		/* the amount of super kitchen takes up 1-byte */
		bodyLen += 1;
		
		for(int i = 0; i < foodMenu.depts.length; i++){
			byte[] deptName = foodMenu.depts[i].name.getBytes("UTF-16BE");
			/**
			 * Each department consist of the stuff below.
			 */
			bodyLen += 1 + 				/* dept_id(1-byte) */
					   1 + 				/* length to department name */
					   deptName.length;	/* the value to department name */
		}
		
		//assign the body length to the corresponding header's field
		header.length[0] = (byte)(bodyLen & 0x000000FF);
		header.length[1] = (byte)((bodyLen & 0x0000FF00) >> 8);
		
		//allocate the memory for the body
		body = new byte[bodyLen];
		
		int offset = 0;
		//assign the food amount 
		body[offset] = (byte)(foodMenu.foods.length & 0x000000FF);
		body[offset + 1] = (byte)((foodMenu.foods.length & 0x0000FF00) >> 8);
		
		//assign each food menu item value to the body 
		offset += 2;
		for(int i = 0; i < foodMenu.foods.length; i++){
			//assign the food's id
			body[offset] = (byte)(foodMenu.foods[i].aliasID & 0x000000FF);
			body[offset + 1] = (byte)((foodMenu.foods[i].aliasID & 0x0000FF00) >> 8);
			offset += 2;			
			
			//assign the unit price to this food
			body[offset] = (byte)(foodMenu.foods[i].price & 0x000000FF);
			body[offset + 1] = (byte)((foodMenu.foods[i].price & 0x0000FF00) >> 8);
			body[offset + 2] = (byte)((foodMenu.foods[i].price & 0x00FF0000) >> 16);
			offset += 3;
			
			//assign the kitchen to this food
			body[offset] = (byte)foodMenu.foods[i].kitchen.aliasID;
			offset += 1;
			
			//assign the status to this food
			body[offset] = (byte)foodMenu.foods[i].status;
			offset += 1;
			
			//assign the length of food's name
			byte[] name = foodMenu.foods[i].name.getBytes("UTF-16BE");
			body[offset] = (byte)(name.length & 0x000000FF);
			//assign the food name
			for(int cnt = 0; cnt < name.length; cnt++){
				body[offset + 1 + cnt] = name[cnt];
			}
			offset += 1 + name.length;
			
			byte[] pinyin;
			if(foodMenu.foods[i].pinyin != null){
				pinyin = foodMenu.foods[i].pinyin.getBytes();
			}else{
				pinyin = new byte[0];
			}
			//assign the length of the food's pinyin
			body[offset] = (byte)(pinyin.length & 0x000000FF);
			//assign the food's pinyin
			for(int cnt = 0; cnt < pinyin.length; cnt++){
				body[offset + 1 + cnt] = pinyin[cnt];
			}
			offset += 1 + pinyin.length;
			
			byte[] image;
			if(foodMenu.foods[i].image != null){
				image = foodMenu.foods[i].image.getBytes();
			}else{
				image = new byte[0];
			}
			//assign the length of food's image
			body[offset] = (byte)(image.length & 0x000000FF);
			//assign the value to food's image
			for(int cnt = 0; cnt < image.length; cnt++){
				body[offset + 1 + cnt] = image[cnt];
			}
			offset += 1 + image.length;
			
			int lenOfPopTaste = 0;
			if(foodMenu.foods[i].popTastes == null){
				//assign the amount taste reference
				body[offset] = 0; 
			}else{
				//assign the amount taste reference
				body[offset] = (byte)foodMenu.foods[i].popTastes.length;
				//assign each taste reference alias id to this food
				for(int cnt = 0; cnt < foodMenu.foods[i].popTastes.length; cnt++){
					body[offset + 1 + lenOfPopTaste] = (byte)(foodMenu.foods[i].popTastes[cnt].aliasID & 0x00FF);
					body[offset + 2 + lenOfPopTaste] = (byte)((foodMenu.foods[i].popTastes[cnt].aliasID & 0xFF00) >> 8);
					lenOfPopTaste += 2;
				}
			}
			offset += 1 + lenOfPopTaste;
			/**
			 * Each item of the food menu consist of the stuff below.
			 */
//			offset += 2 + 				/* food_alias(2-byte) */
//					  3 + 				/* price(3-byte) */ 
//					  1 + 				/* kitchen id to this food(1-byte) */
//					  1 + 				/* status(1-byte) */ 
//					  1 + 				/* the length to food name(1-byte) */
//					  name.length + 	/* the value to food name  */
//					  1 + 				/* the length to pinyin(1-byte) */	
//					  pinyin.length + 	/* the value to pinyin */
//					  1 +				/* the length to image */
//					  image.length +	/* the value to image */		
//					  1 + 				/* the amount to taste reference(1-byte) */
//					  lenOfPopTaste; 	/* all the alias id to taste reference */ 
		}
		
		//assign the taste preferences
		offset = fillTaste(foodMenu.tastes, offset);
		
		//assign the taste styles
		offset = fillTaste(foodMenu.styles, offset);
		
		//assign the taste specifications
		offset = fillTaste(foodMenu.specs, offset);
		
		//assign the kitchen amount
		body[offset] = (byte)(foodMenu.kitchens.length);
		offset++;
		
		//assign each kitchen to the body
		for(int i = 0; i < foodMenu.kitchens.length; i++){
			//assign the kitchen id
			body[offset] = (byte)(foodMenu.kitchens[i].aliasID & 0x00FF);
			offset++;
			
			//assign the super kitchen id that this kitchen belong to 
			body[offset] = (byte)(foodMenu.kitchens[i].dept.deptID & 0x00FF);
			offset++;
			
			//assign 3 normal discounts
			body[offset] = (byte)(foodMenu.kitchens[i].discount_1 & 0x000000FF);
			body[offset + 1] = (byte)(foodMenu.kitchens[i].discount_2 & 0x000000FF);
			body[offset + 2] = (byte)(foodMenu.kitchens[i].discount_3 & 0x000000FF);
			offset += 3;
			
			//assign 3 member discounts
			body[offset] = (byte)(foodMenu.kitchens[i].memberDist_1 & 0x000000FF);
			body[offset + 1] = (byte)(foodMenu.kitchens[i].memberDist_2 & 0x000000FF);
			body[offset + 2] = (byte)(foodMenu.kitchens[i].memberDist_3 & 0x000000FF);
			offset += 3;
			
			byte[] kname = foodMenu.kitchens[i].name.getBytes("UTF-16BE");
			//assign the length of the kitchen name
			body[offset] = (byte)(kname.length & 0x000000FF);			
			//assign the kitchen name
			for(int cnt = 0; cnt < kname.length; cnt++){
				body[offset + 1 + cnt] = kname[cnt];
			}
			offset += 1 + kname.length;
			
			/**
			 * Each kitchen consist of the stuff below.
			 */
//			offset += 1 + 					/* kitchen_alias(1-byte) */
//					  1 + 					/* dept_id(1-byte) */
//					  3 + 					/* normal discount 1..3(3-byte) */
//					  3 + 					/* member discount 1..3(3-byte) */
//					  1 + 					/* length to kitchen name */
//					  kname.length;			/* the value to kitchen */
		}
		
		//assign the amount of super kitchen
		body[offset] = (byte)(foodMenu.depts.length);
		offset++;
		
		//assign each super kitchen to the body
		for(int i = 0; i < foodMenu.depts.length; i++){
			//assign the super kitchen id
			body[offset] = (byte)(foodMenu.depts[i].deptID & 0x00FF);
			offset++;
			
			//assign the length of the super kitchen name
			byte[] deptName = foodMenu.depts[i].name.getBytes("UTF-16BE");
			body[offset] = (byte)(deptName.length & 0x000000FF);
			offset++;
			
			//assign the department name
			for(int cnt = 0; cnt < deptName.length; cnt++){
				body[offset + cnt] = deptName[cnt];
			}			
			offset += deptName.length;

			/**
			 * Each department consist of the stuff below.
			 */
//			offset += 1 + 				/* dept_id(1-byte) */
//					  1 + 				/* length to department name */
//					  deptName.length;	/* the value to department name */
		}
	}
	
	private int calcTasteLen(Taste[] tastes) throws UnsupportedEncodingException{
		int bodyLen = 0;
		/* the taste amount takes up 2-byte */
		bodyLen += 2;
		
		for(int i = 0; i < tastes.length; i++){
			byte[] preference = tastes[i].preference.getBytes("UTF-16BE");
			/**
			 * Each taste preference consist of the stuff below.
			 */
			bodyLen += 2 + 					/* toast_alias(2-byte) */
					   1 + 					/* category(1-byte) */
					   1 + 					/* calculate type(1-byte) */
					   3 + 					/* price(3-byte) */
					   2 + 					/* rate(2-byte) */
					   1 + 					/* the length to taste preference */
					   preference.length;	/* the value to taste preference */
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
			body[offset] = (byte)(tastes[i].aliasID & 0x00FF);
			body[offset + 1] = (byte)((tastes[i].aliasID & 0xFF00) >> 8);
			offset += 2;     
			
			//assign the category of taste preference
			body[offset] = (byte)(tastes[i].category & 0x00FF);
			offset++;
			
			//assign the calculate type of taste preference
			body[offset] = (byte)(tastes[i].calc & 0x00FF);
			offset++;
			
			//assign the float-point of taste preference price
			body[offset] = (byte)(tastes[i].price & 0x000000FF);
			//assign the fixed-point of the taste preference price
			body[offset + 1] = (byte)((tastes[i].price & 0x0000FF00) >> 8);
			body[offset + 2] = (byte)((tastes[i].price & 0x00FF0000) >> 16);
			offset += 3;
			
			//assign the rate of the taste preference
			body[offset] = (byte)(tastes[i].rate & 0x00FF);
			body[offset + 1] = (byte)((tastes[i].rate & 0xFF00) >> 8);
			offset += 2;
			
			//assign the length of the preference string
			byte[] preference = tastes[i].preference.getBytes("UTF-16BE");
			body[offset] = (byte)(preference.length & 0x000000FF);
			//assign the preference string
			for(int cnt = 0; cnt < preference.length; cnt++){
				body[offset + 1 + cnt] = preference[cnt];
			}
			offset += 1 + preference.length;

			/**
			 * Each taste preference consist of the stuff below.
			 */
//			offset += 2 + 					/* toast_alias(2-byte) */
//					  1 + 					/* category(1-byte) */
//					  1 + 					/* calculate type(1-byte) */
//					  3 + 					/* price(3-byte) */
//					  2 + 					/* rate(2-byte) */
//					  1 + 					/* the length to taste preference */
//					  preference.length;	/* the value to taste preference */
		}	
		
		return offset;
	}
}
