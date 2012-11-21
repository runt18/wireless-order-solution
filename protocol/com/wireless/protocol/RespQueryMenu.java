package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

public class RespQueryMenu extends RespPackage{
	
	public RespQueryMenu(ProtocolHeader reqHeader, FoodMenu foodMenu) throws UnsupportedEncodingException{
		
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
		 * discount_amount : <Discount_1> : <Discount_2>...
		 * 
		 * food_amount[2] - 2-byte indicating the amount of the foods listed in the menu
		 * <Food>
		 * food_id[2] : price[3] : kitchen : status : order_cnt[4] :  
		 * name_len : name[name_len] : 
		 * pinyin_len : pinyin[pinyin_len] : 
		 * img_len : image[img_len] : 
		 * pop_taste_amount : pop_taste_1[2] : pop_taste_2[2]... : 
		 * child_foods_amount : child_food_1[2] : child_food_2[2] 
		 * food_id[2] - 2-byte indicating the food's id
		 * price[3] - 3-byte indicating the food's price
		 * 			  price[0] 1-byte indicating the float point
		 * 			  price[1..2] 2-byte indicating the fixed point
		 * kitchen - the kitchen id to this food
		 * status - the status to this food
		 * order_cnt[4] - 4-byte indicates the order count
		 * name_len - the length of the food's name
		 * name[name_len] - the food's name whose length equals "len1"
		 * pinyin_len - the length of the pinyin
		 * pinyin[pinyin_len] - the pinyin whose length equals "len2"
		 * img_len : the length to image
		 * image : the image file name
		 * pop_taste_amount - the amount of popular taste to this food
		 * pop_taste[2] - 2-byte indicating the alias id to each popular taste  
		 * child_foods_amount - the amount of child to this food
		 * child_food[2] - 2-byte indicating the alias id to each child food
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
		 * kitchen_amount[] - 1 byte indicates the amount of kitchen
		 * <Kitchen>
		 * kitchen_alias : is_allow_temp : dept_id : len : kitchen_name[len]
		 * kitchen_alias : the alias id to this kitchen
		 * is_allow_temp : flag to indicate whether allow temporary food
		 * dept_id : the id to department which this kitchen belong to
		 * len : the length of the kitchen name
		 * kitchen_name[len] : the name to this kitchen
		 * 
		 * dept_amount - 1 byte indicates the amount of department
		 * <Department>
		 * dept_id : len : dept_name[len]
		 * dept_id : the id to this department
		 * len : the length of the department name
		 * dept_name[len] : the name to this department
		 * 
		 * discount_amount - 1 byte indicates the amount of discount
		 * <Discount>
		 * discount_id[4] : len : dist_name[len] : level[2] : status : dist_plan_amount : <DiscountPlan_1> : <DiscountPlan_2> : ...
		 * discount_id[4] - 4 bytes indicates the discount id
		 * len - 1 byes indicates the length of discount name
		 * dist_name[len] - the name of discount
		 * level[2] - the level of discount
		 * status - the status of discount
		 * <DiscountPlan>
		 * kitchen_alias : rate
		 * kitchen_alias : 1 bytes indicates the kitchen alias 
		 * rate - 1 byte indicates the discount rate		  
		 *******************************************************/
		
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
					   4 + 				/* order count(4-byte) */
					   1 + 				/* the length to food name(1-byte) */
					   name.length + 	/* the value to food name  */
					   1 + 				/* the length to pinyin(1-byte) */	
					   pinyin.length + 	/* the value to pinyin */
					   1 +				/* the length to image */
					   image.length +	/* the value to image name */
					   1 + 				/* the amount to popular taste(1-byte) */
					   (foodMenu.foods[i].popTastes == null ? 0 : (foodMenu.foods[i].popTastes.length * 2)) + /* all the alias id to popular taste */
					   1 +				/* the amount of child food(1-byte) */
					   (foodMenu.foods[i].childFoods == null ? 0 : (foodMenu.foods[i].childFoods.length * 2));/* all the alias id to child food */
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
					   1 +					/* is_allow_temp(1-byte) */
					   1 + 					/* dept_id(1-byte) */
					   1 + 					/* length to kitchen name(1-byte) */
					   kitchenName.length;	/* the value to kitchen */
		}
		
		
		// the amount of department takes up 1-byte 
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
		
		//the amount of discount takes up 1-byte
		bodyLen += 1;
		for(int i = 0; i < foodMenu.discounts.length; i++){
			byte[] distName = foodMenu.discounts[i].name.getBytes("UTF-16BE");
			//each discount consist of the staff below
			bodyLen += 4 + 											/* discount_id(4-byte) */
					   1 +											/* length of discount name(1-byte) */
					   distName.length + 							/* discount name */
					   2 +											/* level to discount(2-byte) */
					   1 +											/* status to discount(1-byte) */
					   1 +											/* amount of discount plan(1-byte) */
					   foodMenu.discounts[i].plans.length * 2;		/* each discount plan(2-byte) */
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
			
			//assign the order count to this food
			body[offset] = (byte)(foodMenu.foods[i].statistics.orderCnt & 0x000000FF);
			body[offset + 1] = (byte)((foodMenu.foods[i].statistics.orderCnt & 0x0000FF00) >> 8);
			body[offset + 2] = (byte)((foodMenu.foods[i].statistics.orderCnt & 0x00FF0000) >> 16);
			body[offset + 3] = (byte)((foodMenu.foods[i].statistics.orderCnt & 0xFF000000) >> 24);
			offset += 4;
			
			//assign the length of food's name
			byte[] name = foodMenu.foods[i].name.getBytes("UTF-16BE");
			body[offset] = (byte)(name.length & 0x000000FF);
			offset++;
			
			//assign the food name
			System.arraycopy(name, 0, body, offset, name.length);
			offset += name.length;
			
			byte[] pinyin;
			if(foodMenu.foods[i].pinyin != null){
				pinyin = foodMenu.foods[i].pinyin.getBytes();
			}else{
				pinyin = new byte[0];
			}
			
			//assign the length of the food's pinyin
			body[offset] = (byte)(pinyin.length & 0x000000FF);
			offset++;
			
			//assign the food's pinyin
			System.arraycopy(pinyin, 0, body, offset, pinyin.length);
			offset += pinyin.length;
			
			byte[] image;
			if(foodMenu.foods[i].image != null){
				image = foodMenu.foods[i].image.getBytes();
			}else{
				image = new byte[0];
			}
			//assign the length of food's image
			body[offset] = (byte)(image.length & 0x000000FF);
			offset++;
			
			//assign the value of food's image
			System.arraycopy(image, 0, body, offset, image.length);
			offset += image.length;
			
			int lenOfPopTaste = 0;
			if(foodMenu.foods[i].popTastes == null){
				//assign the amount of popular taste
				body[offset] = 0; 
			}else{
				//assign the amount of popular taste
				body[offset] = (byte)foodMenu.foods[i].popTastes.length;
				//assign each popular taste alias id to this food
				for(int cnt = 0; cnt < foodMenu.foods[i].popTastes.length; cnt++){
					body[offset + 1 + lenOfPopTaste] = (byte)(foodMenu.foods[i].popTastes[cnt].aliasID & 0x00FF);
					body[offset + 2 + lenOfPopTaste] = (byte)((foodMenu.foods[i].popTastes[cnt].aliasID & 0xFF00) >> 8);
					lenOfPopTaste += 2;
				}
			}
			offset += 1 + lenOfPopTaste;
			
			int lenOfChildFood = 0;
			if(foodMenu.foods[i].childFoods == null){
				//assign the amount of child food
				body[offset] = 0;
			}else{
				//assign the amount of child food
				body[offset] = (byte)foodMenu.foods[i].childFoods.length;
				//assign each child food alias id to this food
				for(int cnt = 0; cnt < foodMenu.foods[i].childFoods.length; cnt++){
					body[offset + 1 + lenOfChildFood] = (byte)(foodMenu.foods[i].childFoods[cnt].aliasID & 0x00FF);
					body[offset + 2 + lenOfChildFood] = (byte)((foodMenu.foods[i].childFoods[cnt].aliasID & 0xFF00) >> 8);
					lenOfChildFood += 2;
				}
			}
			offset += 1 + lenOfChildFood;
			
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
			//assign the kitchen alias
			body[offset] = (byte)(foodMenu.kitchens[i].aliasID & 0x00FF);
			offset++;
			
			//assign the flag to indicate whether allow temporary food
			body[offset] = (byte)(foodMenu.kitchens[i].isAllowTemp ? 1 : 0);
			offset++;
			
			//assign the department alias that this kitchen belong to 
			body[offset] = (byte)(foodMenu.kitchens[i].dept.deptID & 0x00FF);
			offset++;
			
			byte[] kitchenName = foodMenu.kitchens[i].name.getBytes("UTF-16BE");
			//assign the length of the kitchen name
			body[offset] = (byte)(kitchenName.length & 0x000000FF);		
			offset++;
			//assign the kitchen name
			System.arraycopy(kitchenName, 0, body, offset, kitchenName.length);
			offset += kitchenName.length;

		}
		
		//assign the amount of departments
		body[offset] = (byte)(foodMenu.depts.length);
		offset++;
		
		//assign each department to the body
		for(int i = 0; i < foodMenu.depts.length; i++){
			//assign the department's alias
			body[offset] = (byte)(foodMenu.depts[i].deptID & 0x00FF);
			offset++;
			
			//assign the length of the department name
			byte[] deptName = foodMenu.depts[i].name.getBytes("UTF-16BE");
			body[offset] = (byte)(deptName.length & 0x000000FF);
			offset++;
			
			//assign the department name
			System.arraycopy(deptName, 0, body, offset, deptName.length);
			offset += deptName.length;
		}
		
		//assign the amount of discounts
		body[offset] = (byte)(foodMenu.discounts.length);
		offset++;
				
		//assign each discount
		for(int i = 0; i < foodMenu.discounts.length; i++){
			//assign the discount id
			body[offset] = (byte)(foodMenu.discounts[i].discountID & 0x000000FF);
			body[offset + 1] = (byte)((foodMenu.discounts[i].discountID & 0x0000FF00) >> 8);
			body[offset + 2] = (byte)((foodMenu.discounts[i].discountID & 0x00FF0000) >> 16);
			body[offset + 3] = (byte)((foodMenu.discounts[i].discountID & 0xFF000000) >> 24);
			offset += 4;
			
			//assign the length of discount name
			byte[] distName = foodMenu.discounts[i].name.getBytes("UTF-16BE");
			body[offset] = (byte)(distName.length & 0x000000FF);
			offset++;
			
			//assign the discount name
			System.arraycopy(distName, 0, body, offset, distName.length);
			offset += distName.length;
			
			//assign the level of discount
			body[offset] = (byte)(foodMenu.discounts[i].level & 0x000000FF);
			body[offset + 1] = (byte)(foodMenu.discounts[i].level & 0x0000FF00);
			offset += 2;
			
			body[offset] = (byte)(foodMenu.discounts[i].mStatus);
			offset++;
			
			//assign the amount of discount plan
			body[offset] = (byte)(foodMenu.discounts[i].plans.length & 0x000000FF);
			offset++;
			
			//assign each discount plan
			for(int j = 0; j < foodMenu.discounts[i].plans.length; j++){
				//assign the kitchen alias associated with discount plan
				body[offset] = (byte)(foodMenu.discounts[i].plans[j].kitchen.aliasID & 0x000000FF);
				offset++;
				
				//assign the rate associated with discount plan
				body[offset] = (byte)(foodMenu.discounts[i].plans[j].rate & 0x000000FF);
				offset++;
			}
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
			offset++;
			
			//assign the preference string
			System.arraycopy(preference, 0, body, offset, preference.length);
			offset += preference.length;

		}	
		
		return offset;
	}
}
