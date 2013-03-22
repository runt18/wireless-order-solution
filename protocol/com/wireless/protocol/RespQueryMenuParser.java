package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.util.NumericUtil;

public class RespQueryMenuParser {
	/**
	 * Parse the response associated with query menu request.
	 * @param response the protocol package return from ProtocolConnector's ask() function
	 * @return the vector containing the food instance
	 */
	public static FoodMenu parse(ProtocolPackage response){

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
		 * cancel_reason_amount : <Reason_1> : <Reason_2>...
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
		 * 
		 * discount_amount - 1 byte indicates the amount of discount plan
		 * <DiscountPlan>
		 * kitchen_alias : rate
		 * kitchen_alias : 1 bytes indicates the kitchen alias 
		 * rate - 1 byte indicates the discount rate	
		 * 
		 * cancel_reason_amount - 1 byte indicates the amount of cancel reason
		 * <CancelReason>
		 * reason_id[4] : len : reason[len]
		 * reason_id[4] - 4 bytes indicates the reason id
		 * len - 1 byte indicates the length to reason
		 * reason[len] - the value to reason	  
		 *******************************************************/
		//make sure the response is ACK
		if(response.header.type == Type.ACK){
			
			int offset = 0;
			
			//get the amount of foods
			int foodAmount = (response.body[offset] & 0x000000FF) | ((response.body[offset + 1] & 0x000000FF) << 8);
			
			//allocate the memory for foods
			Food[] foods = new Food[foodAmount];
			
			//the food number takes up 2-byte
			offset += 2; 
			
			//get each food's information 
			for(int i = 0; i < foods.length; i++){
				Food food = new Food();
				//get the food's id
				food.mAliasId = (response.body[offset] & 0x000000FF) |
							((response.body[offset + 1] & 0x000000FF) << 8);
				offset += 2;
				
				//get the food's price
				food.mUnitPrice = ((response.body[offset] & 0x000000FF) |
							 ((response.body[offset + 1] & 0x000000FF) << 8) |
							 ((response.body[offset + 2] & 0x000000FF) << 16)) & 0x00FFFFFF;
				offset += 3;
				
				//get the kitchen no to this food
				food.mKitchen.mAliasId = response.body[offset];
				offset++;
				
				//get the status to this food
				food.mStatus = response.body[offset];
				offset++;
				
				//get the order count to this food
				food.statistics = new FoodStatistics(((response.body[offset] & 0x000000FF) |
		 				   							((response.body[offset + 1] & 0x000000FF) << 8) |
		 				   							((response.body[offset + 2] & 0x000000FF) << 16) |
		 				   							((response.body[offset + 3] & 0x000000FF) << 24)) & 0xFFFFFFFF);
				offset += 4;
				
				//get the length of the food's name
				int lenOfFoodName = response.body[offset];
				offset++;
				
				//get the name value 
				try{
					food.mName = new String(response.body, offset, lenOfFoodName, "UTF-16BE");
				}catch(UnsupportedEncodingException e){

				}
				offset += lenOfFoodName;				
				
				//get the length of the food's pinyin
				int lenOfPinyin = response.body[offset];
				offset++;
				
				//get the food's pinyin
				if(lenOfPinyin > 0){
					food.mPinyin = new String(response.body, offset, lenOfPinyin);
					offset += lenOfPinyin;
				}
				
				//get the length of food's image
				int lenOfImage = response.body[offset];
				offset++;
				
				//get the value of food's image
				if(lenOfImage > 0){
					food.image = new String(response.body, offset, lenOfImage);
					offset += lenOfImage;
				}				
				
				//get the amount of taste reference to this food
				int nPopTaste = response.body[offset];
				offset++;
				
				//get each alias id to popular taste
				int lenOfPopTaste = 0;
				if(nPopTaste != 0){
					food.mPopTastes = new Taste[nPopTaste];
					for(int j = 0; j < food.mPopTastes.length; j++){
						food.mPopTastes[j] = new Taste();
						food.mPopTastes[j].aliasId = (response.body[offset + lenOfPopTaste] & 0x000000FF) | 
													((response.body[offset + 1 + lenOfPopTaste] & 0x000000FF) << 8);
						lenOfPopTaste += 2;
					}
				}else{
					food.mPopTastes = new Taste[0];
				}
				offset += lenOfPopTaste;
				
				//get the amount of child food
				int nChildFood = response.body[offset];
				offset++;
				
				//get alias id to each child food
				int lenOfChildFood = 0;
				if(nChildFood != 0){
					food.mChildFoods = new Food[nChildFood];
					for(int j = 0; j < food.mChildFoods.length; j++){
						food.mChildFoods[j] = new Food();
						food.mChildFoods[j].mAliasId = (response.body[offset + lenOfChildFood] & 0x000000FF) | 
													 ((response.body[offset + 1 + lenOfChildFood] & 0x000000FF) << 8);
						lenOfChildFood += 2;
					}
				}else{
					food.mChildFoods = new Food[0];
				}
				offset += lenOfChildFood;
				
				//add to foods
				foods[i] = food;
			}
			

			//get the amount of taste preferences
			int nCount = (response.body[offset] & 0x000000FF) | ((response.body[offset + 1] & 0x000000FF) << 8);
			offset += 2;
			//allocate the memory for taste preferences
			Taste[] tastes = new Taste[nCount];
			//get the taste preferences
			offset = genTaste(response, offset, tastes, 0, tastes.length);			

			//get the amount of taste styles
			nCount = (response.body[offset] & 0x000000FF) | ((response.body[offset + 1] & 0x000000FF) << 8);
			offset += 2;
			//allocate the memory for taste preferences
			Taste[] styles = new Taste[nCount];
			//get the taste styles
			offset = genTaste(response, offset, styles, 0, styles.length);

			//get the amount of taste specifications
			nCount = (response.body[offset] & 0x000000FF) | ((response.body[offset + 1] & 0x000000FF) << 8);
			offset += 2;
			//allocate the memory for taste preferences
			Taste[] specs = new Taste[nCount];
			//get the taste specifications
			offset = genTaste(response, offset, specs, 0, specs.length);
					
			//get the amount of kitchens
			int nKitchens = response.body[offset] & 0x000000FF;
			offset++;
			//allocate the memory for kitchens
			PKitchen[] kitchens = new PKitchen[nKitchens];
			//get each kitchen's information
			for(int i = 0; i < kitchens.length; i++){
				
				//get the kitchen alias id
				short kitchenAlias = (short)(response.body[offset] & 0x00FF);
				offset++;
				
				//get the flag to indicate whether allow temporary food
				boolean isAllowTemp = response.body[offset] == 1 ? true : false;
				offset++;
				
				//get the department alias id that the kitchen belong to
				short deptAlias = (short)(response.body[offset] & 0x00FF);
				offset++;
				
				//get the length of kitchen name
				int lenOfKitchenName = response.body[offset];
				offset++;
				//get the value of kitchen name
				String kitchenName = null;
				try{
					kitchenName = new String(response.body, offset, lenOfKitchenName, "UTF-16BE");
				}catch(UnsupportedEncodingException e){}
				offset += lenOfKitchenName;
				
				//add the kitchen
				kitchens[i] = new PKitchen(0, kitchenName, 0, kitchenAlias, isAllowTemp, PKitchen.TYPE_NORMAL, 
										  new PDepartment(null, deptAlias, 0, PDepartment.TYPE_NORMAL));
			}
			
			//get the amount of departments
			int nDept = response.body[offset] & 0x000000FF;
			offset++;
			//allocate the memory for departments
			PDepartment[] depts = new PDepartment[nDept];
			//get each department's information
			for(int i = 0; i < depts.length; i++){
				//get the alias id to department
				short deptID = (short)(response.body[offset] & 0x00FF);
				offset++;
				
				//get the length of department name
				int lenOfDeptName = response.body[offset];
				offset++;
				
				//get the value of department name
				String deptName = null;
				try{
					deptName = new String(response.body, offset, lenOfDeptName, "UTF-16BE");
				}catch(UnsupportedEncodingException e){}
				offset += lenOfDeptName;				
				
				depts[i] = new PDepartment(deptName, deptID, 0, PDepartment.TYPE_NORMAL);
			}	
			
			//get the amount of discount
			int nDiscount = response.body[offset] & 0x000000FF;
			offset++;
			//allocate the memory for discounts
			PDiscount[] discounts = new PDiscount[nDiscount];
			//get each discount's information
			for(int i = 0; i < discounts.length; i++){
				discounts[i] = new PDiscount();
				//get the discount id
				discounts[i].setId(((response.body[offset] & 0x000000FF) |
						 				  ((response.body[offset + 1] & 0x000000FF) << 8) |
						 				  ((response.body[offset + 2] & 0x000000FF) << 16)|
						 				  ((response.body[offset + 3] & 0x000000FF) << 24)));
				offset += 4;
				
				//get the length of discount name
				int lenOfDistName = response.body[offset];
				offset++;
				
				//get the value of discount name
				try{
					discounts[i].mName = new String(response.body, offset, lenOfDistName, "UTF-16BE");
				}catch(UnsupportedEncodingException e){}
				offset += lenOfDistName;
				
				//get the level to this discount
				discounts[i].mLevel = ((response.body[offset] & 0x000000FF) |
		 				  			 ((response.body[offset + 1] & 0x000000FF) << 8)) & 0x0000FFFF;
				offset += 2;
				
				//get the status to this discount
				discounts[i].mStatus = response.body[offset];
				offset++;
				
				//get the amount of discount plan
				int nDistPlan = response.body[offset];
				offset++;
				
				//allocate the memory for discount plan
				discounts[i].mPlans = new PDiscountPlan[nDistPlan];
				
				//get value to each discount plan
				for(int j = 0; j < discounts[i].mPlans.length; j++){
					
					//get the kitchen alias
					PKitchen kitchen = new PKitchen();
					kitchen.mAliasId = response.body[offset];
					offset++;
					
					//get the discount rate associated with this kitchen
					int rate = response.body[offset];
					offset++;
					
					discounts[i].mPlans[j] = new PDiscountPlan(kitchen, rate);
				}				
			}
			
			//get the amount of cancel reason
			int nReason = response.body[offset] & 0x000000FF;
			offset++;
			//allocate the memory for cancel reasons
			CancelReason[] reasons = new CancelReason[nReason];
			//get details to each cancel reason
			for(int i = 0; i < reasons.length; i++){
				reasons[i] = new CancelReason();
				//get the id to reason
				reasons[i].mId = ((response.body[offset] & 0x000000FF) |
		 				  		 ((response.body[offset + 1] & 0x000000FF) << 8) |
		 				  		 ((response.body[offset + 2] & 0x000000FF) << 16)|
		 				  		 ((response.body[offset + 3] & 0x000000FF) << 24));
				offset += 4;
				
				//get the length of reason name
				int lenOfReason = response.body[offset];
				offset++;
				
				//get the value of reason name
				try{
					reasons[i].mReason = new String(response.body, offset, lenOfReason, "UTF-16BE");
				}catch(UnsupportedEncodingException e){}
				offset += lenOfReason;
			}
			
			return new FoodMenu(foods, tastes, styles, specs, kitchens, depts, discounts, reasons);
			
		}else{
			return new FoodMenu(new Food[0], new Taste[0], new Taste[0], new Taste[0], new PKitchen[0], new PDepartment[0], new PDiscount[0], new CancelReason[0]);
		}
	}
	
	private static int genTaste(ProtocolPackage response, int offset, Taste[] tastes, int begPos, int endPos){
		//get each taste preference's information
		for(int i = begPos; i < endPos; i++){
			
			//get the alias id to taste preference
			int alias_id = (response.body[offset] & 0x000000FF) |
						   ((response.body[offset + 1] & 0x000000FF) << 8);
			offset += 2;
			
			//get the category to taste preference
			short category = response.body[offset];
			offset++;
			
			//get the calculate type to taste preference
			short calcType = response.body[offset];
			offset++;
			
			//get the price to taste preference
			int price = ((response.body[offset] & 0x000000FF) |
						((response.body[offset + 1] & 0x000000FF) << 8) |
						((response.body[offset + 2] & 0x000000FF) << 16)) & 0x00FFFFFF ;
			offset += 3;
			
			//get the rate to taste preference
			int rate = response.body[offset] & 0x000000FF | 
					   ((response.body[offset + 1] & 0x000000FF) << 8);
			offset += 2;
			
			//get the length to taste preference string
			int lenOfTaste = response.body[offset];
			offset++;
			
			String preference = null;
			//get the taste preference string
			try{
				preference = new String(response.body, offset, lenOfTaste, "UTF-16BE");
			}catch(UnsupportedEncodingException e){}
			offset += lenOfTaste;
			
			//add the taste
			tastes[i] = new Taste(0,
								  alias_id, 
								  0,
								  preference, 
								  category, 
								  calcType, 
								  NumericUtil.int2Float(rate),
								  NumericUtil.int2Float(price),
							      Taste.TYPE_NORMAL);
		}
		
		return offset;
	}
}
