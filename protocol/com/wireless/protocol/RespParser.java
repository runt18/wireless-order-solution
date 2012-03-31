package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

public class RespParser {
	
	/**
	 * Parse the response associated with query order request.
	 * The result only doesn't contain the detail info like food's name 
	 * , price and taste's name and price 
	 * @param response 
	 * the protocol package return from ProtocolConnector's ask() function
	 * @return
	 * the order result
	 */
	public static Order parseQueryOrder(ProtocolPackage response, FoodMenu foodMenu){
		Order order = new Order();
		/******************************************************
		 * In the case query order successfully, 
		 * design the query order response looks like below
		 * mode : type : seq : reserved : pin[6] : len[2] : <Order>
		 * <Header>
		 * mode - ORDER_BUSSINESS
		 * type - ACK
		 * seq - same as request
		 * reserved : 0x00
		 * pin[6] : same as request
		 * len[2] -  length of the <Body>
		 * <Body>
		 * table[2] : table_2[2] : minimum_cost[4] : category : 
		 * custom_num : price[4] : food_num : 
		 * <Food1> : <Food2>...
		 * <TmpFood1> : <TmpFood2>...
		 * 
		 * table[2] - 2-byte indicates the table id 
		 * 
		 * table_2[2] - 2-byte indicates the 2nd table id, only used table merger
		 * 
		 * minimum_cost[4] - 4-byte indicates the minimum cost to this order
		 * 
		 * category - 1-byte indicates the category to this order
		 * 
		 * custom_num - 1-byte indicating the number of the custom for this order
		 * 
		 * price[4] - 4-byte indicating the total price to this order
		 * 			  price[0] - 1-byte indicating the float-point
		 * 			  price[1..3] - 3-byte indicating the fixed-point
		 * 
		 * food_num - 1-byte indicating the number of ordered food
		 * 
		 * <Food>
		 * is_temp(0) : food_id[2] : order_amount[2] : status : taste_id[2] : taste_id2[2] : taste_id3[2] : hang_status
		 * is_temp : "0" means this food is NOT temporary
		 * food_id[2] - 2-byte indicating the food's id
		 * order_amount[2] - 2-byte indicating how many this foods are ordered
		 * 			   order_num[0] - 1-byte indicates the float-point
		 * 			   order_num[1] - 1-byte indicates the fixed-point
		 * status - the status to this food
		 * taste_id[2] - 2-byte indicates the 1st taste preference id
		 * taste_id2[2] - 2-byte indicates the 2nd taste preference id
		 * taste_id3[2] - 2-byte indicates the 3rd taste preference id
		 * hang_status - indicates the hang status to the food
		 * 
		 * <TmpFood>
		 * is_temp(1) : food_id[2] : order_amount[2] : unit_price[3] : hang_status : len : food_name[len] 
		 * is_temp(1) - "1" means this food is temporary
		 * food_id[2] - 2-byte indicating the food's id
		 * order_amount[2] - 2-byte indicating how many this foods are ordered
		 * unit_price[3] - 3-byte indicating the unit price to this food
		 * hang_status - indicates the hang status to the food
		 * len - the length of food's name
		 * food_name[len] - the value of the food name
		 *******************************************************/
		if(response.header.type == Type.ACK){
			//get the table id
			order.table.aliasID = ((response.body[0] & 0x000000FF) | ((response.body[1] & 0x000000FF) << 8));

			//get the 2nd table id
			order.table2.aliasID = ((response.body[2] & 0x000000FF) | ((response.body[3] & 0x000000FF) << 8));
			
			//get the minimum cost
			order.minCost = (response.body[4] & 0x000000FF) | 
								 ((response.body[5] & 0x000000FF ) << 8) |
								 ((response.body[6] & 0x000000FF ) << 16) |
								 ((response.body[7] & 0x000000FF ) << 24);
			
			//get the category
			order.category = (short)(response.body[8] & 0x00FF);
			
			//get the custom number
			order.custom_num = (int)response.body[9];

			//get the total price
			order.totalPrice =  (response.body[10] & 0x000000FF) | 
								((response.body[11] & 0x000000FF ) << 8) |
								((response.body[12] & 0x000000FF ) << 16) |
								((response.body[13] & 0x000000FF ) << 24);

			//get order food's number
			int foodNum = response.body[14];
			OrderFood[] orderFoods = new OrderFood[foodNum]; 
			
			//get every order food's id and number
			int offset = 15;
			for(int i = 0; i < orderFoods.length; i++){
				//get the temporary flag
				boolean isTemporary = response.body[offset] == 1 ? true : false;
				
				if(isTemporary){
					/**
					 * is_temp(1) : food_id[2] : order_amount[2] : unit_price[3] : hang_status : len : food_name[len]
					 */
					//get the food alias id
					int foodID = (response.body[offset + 1] & 0x000000FF) |
								((response.body[offset + 2] & 0x000000FF) << 8);
					//get the order amount
					int orderAmount = (response.body[offset + 3] & 0x000000FF) |
									((response.body[offset + 4] & 0x000000FF) << 8);
					//get the unit price
					int unitPrice = (response.body[offset + 5] & 0x000000FF) |
									((response.body[offset + 6] & 0x000000FF) << 8) |
									((response.body[offset + 7] & 0x000000FF) << 16);
					//get the hang status
					short hangStatus = response.body[offset + 8];
					//get the amount of food name bytes
					int len = response.body[offset + 9];
					//get the food name
					String name = null;
					try{
						name = new String(response.body, offset + 10, len, "UTF-8");
					}catch(UnsupportedEncodingException e){}
					
					orderFoods[i] = new OrderFood();
					orderFoods[i].isTemporary = true;
					orderFoods[i].aliasID = foodID;
					orderFoods[i].hangStatus = hangStatus;
					orderFoods[i].count = orderAmount;
					orderFoods[i].setPrice(Util.int2Float(unitPrice));
					orderFoods[i].name = (name != null ? name : "");
					
					offset += 1 + 2 + 2 + 3 + 1 + 1 + len;
					
				}else{
					/**
					 * is_temp(0) : food_id[2] : order_amount[2] : status : taste_id[2] : taste_id2[2] : taste_id3[2] : hang_status
					 */
					//get the food alias id
					int foodID = (response.body[offset + 1] & 0x000000FF) |
								((response.body[offset + 2] & 0x000000FF) << 8);
					//get the order amount
					int orderAmount = (response.body[offset + 3] & 0x000000FF) |
									((response.body[offset + 4] & 0x000000FF) << 8);
					//get the food status
					short status = response.body[offset + 5];
					
					int[] tasteID = new int[3];
					//get the 1st taste id
					tasteID[0] = (response.body[offset + 6] & 0x000000FF) | 
									((response.body[offset + 7] & 0x000000FF) << 8);
					//get the 2nd taste id
					tasteID[1] = (response.body[offset + 8] & 0x000000FF) | 
									((response.body[offset + 9] & 0x000000FF) << 8);
					//get the 3rd taste id
					tasteID[2] = (response.body[offset + 10] & 0x000000FF) | 
									((response.body[offset + 11] & 0x000000FF) << 8);
					//get the hang status
					short hangStatus = response.body[offset + 12];
					
					offset += 13;
					
					orderFoods[i] = new OrderFood();
					orderFoods[i].isTemporary = false;
					orderFoods[i].aliasID = foodID;
					orderFoods[i].count = orderAmount;
					orderFoods[i].status = status;
					
					//Arrays.sort(tasteID, 0, tasteID.length);
					orderFoods[i].tastes[0].aliasID = tasteID[0];
					orderFoods[i].tastes[1].aliasID = tasteID[1];
					orderFoods[i].tastes[2].aliasID = tasteID[2];
					
					orderFoods[i].hangStatus = hangStatus;
				}
			}
			order.foods = orderFoods;
		}
		
		/**
		 * Since the food information from response only has the food_id, taste_id, taste_id1, taste_id2,
		 * we get the corresponding food and taste name from the food menu. 
		 */
		for(int i = 0; i < order.foods.length; i++){
			if(!order.foods[i].isTemporary){
				//get the food name, unit price and attached kitchen
				for(int j = 0; j < foodMenu.foods.length; j++){
					if(order.foods[i].aliasID == foodMenu.foods[j].aliasID){
						order.foods[i].name = foodMenu.foods[j].name;
						order.foods[i].setPrice(foodMenu.foods[j].getPrice());
						order.foods[i].kitchen = foodMenu.foods[j].kitchen;
						break;
					}			
				}	
				
				for(int j = 0; j < order.foods[i].tastes.length; j++){

					//search and get the taste match the alias id
					Taste taste = srchTaste(order.foods[i].tastes[j].aliasID, foodMenu.tastes);
					if(taste != null){
						order.foods[i].tastes[j] = taste;
						continue;
					}
					
					//search and get the style match the alias id
					Taste style = srchTaste(order.foods[i].tastes[j].aliasID, foodMenu.styles);
					if(style != null){
						order.foods[i].tastes[j] = style;
						continue;
					}
					
					//search and get the specification match the alias id
					Taste spec = srchTaste(order.foods[i].tastes[j].aliasID, foodMenu.specs);
					if(spec != null){
						order.foods[i].tastes[j] = spec;
						continue;
					}
				}
				
				//set the taste preference to this food
				order.foods[i].tastePref = Util.genTastePref(order.foods[i].tastes);
				//set the taste total price to this food
				order.foods[i].setTastePrice(Util.genTastePrice(order.foods[i].tastes, order.foods[i].getPrice()));
			}			
		}		

		return order;
	}

	/**
	 * Get the taste detail according to the specific taste id
	 * @param aliasID the taste id
	 * @param tasteSrc the taste detail information to be searched
	 * @return a taste if found, null if NOT found
	 */
	private static Taste srchTaste(int aliasID, Taste[] tasteSrc){
		
		if(aliasID != Taste.NO_TASTE){
			Taste taste = null;
			
			for(int i = 0; i < tasteSrc.length; i++){
				if(aliasID == tasteSrc[i].aliasID){
					
					taste = new Taste();
					
					taste.aliasID = tasteSrc[i].aliasID;
					taste.preference = tasteSrc[i].preference;
					taste.setPrice(tasteSrc[i].getPrice());
					taste.calc = tasteSrc[i].calc;
					taste.category = tasteSrc[i].category;
					taste.setRate(tasteSrc[i].getRate());

					break;
				}
			}
			
			return taste;
			
		}else{
			return null;
		}
	}
	
	/**
	 * Parse the response associated with query menu request.
	 * @param response the protocol package return from ProtocolConnector's ask() function
	 * @return the vector containing the food instance
	 */
	public static FoodMenu parseQueryMenu(ProtocolPackage response){

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
		//make sure the response is ACK
		if(response.header.type == Type.ACK){
			//get the amount of foods
			int nFoods = (response.body[0] & 0x000000FF) | ((response.body[1] & 0x000000FF) << 8);
			
			//allocate the memory for foods
			Food[] foods = new Food[nFoods];
			
			int index = 2; /* the food number takes up 2-byte */
			
			//get each food's information 
			for(int i = 0; i < nFoods; i++){
				Food food = new Food();
				//get the food's id
				food.aliasID = (response.body[index] & 0x000000FF) |
							((response.body[index + 1] & 0x000000FF) << 8);
				
				//get the food's price
				food.price = ((response.body[index + 2] & 0x000000FF) |
							 ((response.body[index + 3] & 0x000000FF) << 8) |
							 ((response.body[index + 4] & 0x000000FF) << 16)) &	0x00FFFFFF;
				
				//get the kitchen no to this food
				food.kitchen.aliasID = response.body[index + 5];
				
				//get the status to this food
				food.status = response.body[index + 6];
				
				//get the length of the food's name
				int len1 = response.body[index + 7];
				
				//get the name value 
				try{
					food.name = new String(response.body, index + 8, len1, "UTF-16BE");
				}catch(UnsupportedEncodingException e){

				}
				
				//get the length of the food's pinyin
				int len2 = response.body[index + 8 + len1];
				
				//get the food's pinyin
				if(len2 != 0){
					food.pinyin = new String(response.body, index + 8 + len1 + 1, len2);
				}
				
				index += 8 + len1 + 1 + len2;
				
				//add to foods
				foods[i] = food;
			}
			

			//get the amount of taste preferences
			int nCount = (response.body[index] & 0x000000FF) | ((response.body[index + 1] & 0x000000FF) << 8);
			index += 2;
			//allocate the memory for taste preferences
			Taste[] tastes = new Taste[nCount];
			//get the taste preferences
			index = genTaste(response, index, tastes, 0, tastes.length);			

			//get the amount of taste styles
			nCount = (response.body[index] & 0x000000FF) | ((response.body[index + 1] & 0x000000FF) << 8);
			index += 2;
			//allocate the memory for taste preferences
			Taste[] styles = new Taste[nCount];
			//get the taste styles
			index = genTaste(response, index, styles, 0, styles.length);

			//get the amount of taste specifications
			nCount = (response.body[index] & 0x000000FF) | ((response.body[index + 1] & 0x000000FF) << 8);
			index += 2;
			//allocate the memory for taste preferences
			Taste[] specs = new Taste[nCount];
			//get the taste specifications
			index = genTaste(response, index, specs, 0, specs.length);
					
			//get the amount of kitchens
			int nKitchens = response.body[index] & 0x000000FF;
			index++;
			//allocate the memory for kitchens
			Kitchen[] kitchens = new Kitchen[nKitchens];
			//get each kitchen's information
			for(int i = 0; i < kitchens.length; i++){
				
				//get the kitchen alias id
				short kitchenAlias = (short)(response.body[index] & 0x00FF);
				
				//get the department alias id that the kitchen belong to
				short deptAlias = (short)(response.body[index + 1] & 0x00FF);
				
				//get 3 normal discounts
				byte dist_1 = response.body[index + 2];
				byte dist_2 = response.body[index + 3];
				byte dist_3 = response.body[index + 4];
				
				//get 3 member discounts
				byte mdist_1 = response.body[index + 5];
				byte mdist_2 = response.body[index + 6];
				byte mdist_3 = response.body[index + 7];
				
				//get the length of the kitchen name
				int length = response.body[index + 8];
				//get the value of super kitchen name
				String kitchenName = null;
				try{
					kitchenName = new String(response.body, index + 9, length, "UTF-16BE");
				}catch(UnsupportedEncodingException e){}
				
				index += 9 + length;
				
				//add the kitchen
				kitchens[i] = new Kitchen(kitchenName, 0, kitchenAlias, deptAlias,
										  dist_1, dist_2, dist_3,
										  mdist_1, mdist_2, mdist_3);
			}
			
			//get the amount of super kitchens
			int nDept = response.body[index] & 0x000000FF;
			index++;
			//allocate the memory for super kitchens
			Department[] depts = new Department[nDept];
			//get each super kitchen's information
			for(int i = 0; i < depts.length; i++){
				//get the alias id to department
				short deptID = (short)(response.body[index] & 0x00FF);
				
				//get the length of the department name
				int length = response.body[index + 1];
				
				//get the value of super department name
				String deptName = null;
				try{
					deptName = new String(response.body, index + 2, length, "UTF-16BE");
				}catch(UnsupportedEncodingException e){}
				
				index += 2 + length;
				
				depts[i] = new Department(deptName, deptID, 0);
			}
			
			return new FoodMenu(foods, tastes, styles, specs, kitchens, depts);
			
		}else{
			return new FoodMenu(new Food[0], new Taste[0], new Taste[0], new Taste[0], new Kitchen[0], new Department[0]);
		}
	}
	
	private static int genTaste(ProtocolPackage response, int offset, Taste[] tastes, int begPos, int endPos){
		//get each taste preference's information
		for(int i = begPos; i < endPos; i++){
			
			//get the alias id to taste preference
			int alias_id = (response.body[offset] & 0x000000FF) |
						   ((response.body[offset + 1] & 0x000000FF) << 8);
			
			//get the category to taste preference
			short category = response.body[offset + 2];
			
			//get the calculate type to taste preference
			short calcType = response.body[offset + 3];
			
			//get the price to taste preference
			int price = ((response.body[offset + 4] & 0x000000FF) |
						((response.body[offset + 5] & 0x000000FF) << 8) |
						((response.body[offset + 6] & 0x000000FF) << 16)) & 0x00FFFFFF ;
			
			//get the rate to taste preference
			int rate = response.body[offset + 7] & 0x000000FF | 
					   ((response.body[offset + 8] & 0x000000FF) << 8);
			
			//get the length to taste preference string
			int length = response.body[offset + 9];
			
			String preference = null;
			//get the taste preference string
			try{
				preference = new String(response.body, offset + 10, length, "UTF-16BE");
			}catch(UnsupportedEncodingException e){}
			
			/**
			 * each taste preference consist of the stuff below.
			 * toast_id(2-byte) + category(1-byte) + calc(1-byte) + 
			 * price(3-byte) + rate(2-byte) + length of the preference(1-byte) + preference description(len-byte)
			 */
			offset += 2 + 1 + 1 + 3 + 2 + 1 + length;
			
			//add the taste
			tastes[i] = new Taste(0,
								  alias_id, 
								  preference, 
								  category, 
								  calcType, 
							      Util.int2Float(rate),
							      Util.int2Float(price));
		}
		
		return offset;
	}
	
	/**
	 * Parse the response associated with the query restaurant. 
	 * @param response The response containing the restaurant info.
	 * @return The restaurant info.
	 */
	public static Restaurant parseQueryRestaurant(ProtocolPackage response){
		Restaurant restaurant = new Restaurant();
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

		int offset = 0;
		try{
			//get the length of the restaurant name
			int length = response.body[0];
			offset++;
			//get the restaurant name
			if(length != 0){
				restaurant.name = new String(response.body, offset, length, "UTF-16BE");
			}
			
			//calculate the position of the length to restaurant info
			offset = offset + length;
			length = response.body[offset];
			offset++;
			//get the restaurant info
			if(length != 0){
				restaurant.info = new String(response.body, offset, length, "UTF-16BE");
			}
			
			//calculate the position of the length to owner
			offset = offset + length;
			length = response.body[offset];
			offset++;
			//get the owner name of the terminal 
			if(length != 0){
				restaurant.owner = new String(response.body, offset, length, "UTF-16BE");
			}
			
			//calculate the position of the length to 1st password
			offset = offset + length;
			length = response.body[offset];
			offset++;
			//get the 1st password
			if(length != 0){
				restaurant.pwd = new String(response.body, offset, length);
			}
			
			//calculate the position of the length to 2nd password
			offset = offset + length;
			length = response.body[offset];
			offset++;
			//get the 2nd password
			if(length != 0){
				restaurant.pwd2 = new String(response.body, offset, length);
			}
			
			//calculate the position of the length to 3rd password
			offset = offset + length;
			length = response.body[offset];
			offset++;
			//get the 3rd password
			if(length != 0){
				restaurant.pwd3 = new String(response.body, offset, length);
			}
			
		}catch(UnsupportedEncodingException e){

		}
		return restaurant;
	}
	
	/**
	 * Parse the response associated with the query staff. 
	 * @param response The response containing the staff info.
	 * @return Return the array of staff info, null if no staff info.
	 */
	public static StaffTerminal[] parseQueryStaff(ProtocolPackage response){
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
		 * nStaff[2] : <staff_1> : ... : <staff_n>
		 * nStaff[2] - the amount of staff
		 * <staff_n>
		 * len_1 : name : len_2 : pwd : pin[4]
		 * len_1 - the length to the staff name
		 * name - the value to staff name
		 * len_2 - the length to the staff password
		 * pwd - the value to the staff password
		 * pin[4] - 4-byte indicating the pin
		 *******************************************************/	
		StaffTerminal[] staffs = null;
		try{
			//get the amount to staff
			int nStaff = (response.body[0] & 0x000000FF) |
						 ((response.body[1] & 0x000000FF) << 8);
			
			if(nStaff > 0){
				staffs = new StaffTerminal[nStaff];
				
				int offset = 2;
				for(int i = 0; i < nStaff; i++){
					
					staffs[i] = new StaffTerminal();
					
					//get the length to this staff name
					int nameLen = response.body[offset];
					offset++;
					
					//get the value to this staff name
					if(nameLen > 0){
						staffs[i].name = new String(response.body, offset, nameLen, "UTF-16BE");
						offset += nameLen;
					}
					
					//get the length to staff password
					int pwdLen = response.body[offset];
					offset++;
					
					//get the value to this staff name
					if(pwdLen > 0){
						staffs[i].pwd = new String(response.body, offset, pwdLen);
						offset += pwdLen;
					}
					
					//get the pin to this staff
					staffs[i].pin = ((long)response.body[offset] & 0x00000000000000FF) |
						   	   		(((long)response.body[offset + 1] & 0x00000000000000FF) << 8) |
						   	   		(((long)response.body[offset + 2] & 0x00000000000000FF) << 16) |
						   	   		(((long)response.body[offset + 3] & 0x00000000000000FF) << 24);
					offset += 4;
				}		

			}			
			
		}catch(UnsupportedEncodingException e){
			
		}
		
		return staffs;
	}
	
	/**
	 * Parse the response associated with the query table. 
	 * @param response The response containing the table info.
	 * @return Return the array of table info, null if no table info.
	 */
	public static Table[] parseQueryTable(ProtocolPackage response){
		/******************************************************
		 * In the case query table successfully, 
		 * design the query table response looks like below
		 * mode : type : seq : reserved : pin[6] : len[2] : <Body>
		 * <Header>
		 * mode - ORDER_BUSSINESS
		 * type - ACK
		 * seq - same as request
		 * reserved : 0x00
		 * pin[6] : same as request
		 * len[2] -  length of the <Body>
		 * <Body>
		 * nTable[2] : <table_1> : ... : <table_2>
		 * nTable[2] - the amount of tables
		 * <table_n>
		 * len_1 : table_name : table_alias[2] : region : service_rate[2] : minimum_cost[4] : status : category : custom_num
		 * len_1 - the length to the table name
		 * table_name - the value to table name
		 * table_alias[2] - the alias id to this table
		 * region - the region alias id to this table
		 * service_rate[2] - the service rate to this table
		 * minimum_cost[4] - the minimum cost to this table
		 * status - the status to this table
		 * category - the category to this table
		 * custom_num - the custom number to this table
		 *******************************************************/
		Table[] tables = null;
		try{
			//get the amount to staff
			int nTable = (response.body[0] & 0x000000FF) |
						 ((response.body[1] & 0x000000FF) << 8);
			
			if(nTable > 0){
				tables = new Table[nTable];
				
				int offset = 2;
				for(int i = 0; i < tables.length; i++){
					
					tables[i] = new Table();
					
					//get the length to this table name
					int nameLen = response.body[offset];
					offset++;
					
					//get the value to this table name
					if(nameLen > 0){
						tables[i].name = new String(response.body, offset, nameLen, "UTF-16BE");
						offset += nameLen;
					}
					
					//get the table alias
					tables[i].aliasID = ((int)(response.body[offset] & 0x000000FF)) | 
										((int)(response.body[offset + 1] & 0x000000FF) << 8);
					offset += 2;
					
					//get the region alias
					tables[i].regionID = response.body[offset];
					offset++;
					
					//get the service rate
					tables[i].serviceRate = (response.body[offset] & 0x000000FF) |
										    ((response.body[offset + 1] & 0x000000FF) << 8);
					offset += 2;
					
					//get the minimum cost
					tables[i].minimumCost = (response.body[offset] & 0x000000FF) | 
											((response.body[offset + 1] & 0x000000FF) << 8) |
											((response.body[offset + 2] & 0x000000FF) << 16) |
											((response.body[offset + 3] & 0x000000FF) << 24);
					offset += 4;
					
					//get the status
					tables[i].status = response.body[offset];
					offset++;
					
					//get the category
					tables[i].category = response.body[offset];
					offset++;
					
					//get the custom number;
					tables[i].custom_num = response.body[offset];
					offset++;
				}
			}
			
		}catch(UnsupportedEncodingException e){
			
		}
		return tables;
	}
	
	/**
	 * Parse the response associated with the query region. 
	 * @param response The response containing the region info.
	 * @return Return the array of region info, null if no region info.
	 */
	public static Region[] parseQueryRegion(ProtocolPackage response){
		/******************************************************
		 * In the case query region successfully, 
		 * design the query region response looks like below
		 * mode : type : seq : reserved : pin[6] : len[2] : <Body>
		 * <Header>
		 * mode - ORDER_BUSSINESS
		 * type - ACK
		 * seq - same as request
		 * reserved : 0x00
		 * pin[6] : same as request
		 * len[2] -  length of the <Body>
		 * <Body>
		 * nRegion : <region_1> : ... : <region_n>
		 * nRegion - the amount of regions
		 * <region_n>
		 * len_1 : region_name : region_alias[2] 
		 * len_1 - the length to the region name
		 * region_name - the value to region name
		 * region_alias[2] - the alias id to this region
		 *******************************************************/
		Region[] regions = null;
		try{
			//get the amount to staff
			int nRegion = response.body[0];
			
			if(nRegion > 0){
				regions = new Region[nRegion];
				
				int offset = 1;
				for(int i = 0; i < regions.length; i++){
					
					regions[i] = new Region();
					
					//get the length to this region name
					int nameLen = response.body[offset];
					offset++;
					
					//get the value to this region name
					if(nameLen > 0){
						regions[i].name = new String(response.body, offset, nameLen, "UTF-16BE");
						offset += nameLen;
					}
					
					//get the region alias
					regions[i].regionID = (short)(((int)(response.body[offset] & 0x0000000FF)) | 
										((int)(response.body[offset + 1] & 0x0000000FF) << 8));
					offset += 2;				
				}
			}			
		}catch(UnsupportedEncodingException e){
			
		}
		return regions;
	}
}
