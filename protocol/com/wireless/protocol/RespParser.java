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
	public static Order parseQueryOrder(ProtocolPackage response){
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
		 * table[2] : table_2[2] : minimum_cost[4] : category : custom_num : price[2] : food_num : <Food1> : <Food2>...
		 * table[2] - 2-byte indicates the table id
		 * table_2[2] - 2-byte indicates the 2nd table id, only used table merger
		 * minimum_cost[4] - 4-byte indicates the minimum cost to this order
		 * category - 1-byte indicates the category to this order 
		 * custom_num - 1-byte indicating the number of the custom for this order
		 * price[4] - 4-byte indicating the total price to this order
		 * 			  price[0] - 1-byte indicating the float-point
		 * 			  price[1..3] - 3-byte indicating the fixed-point
		 * food_num - 1-byte indicating the number of ordered food
		 * <Food>
		 * food_id[2] : order_num[2] : status : taste_id
		 * food_id[2] - 2-byte indicating the food's id
		 * order_num[2] - 2-byte indicating how many this foods are ordered
		 * 			   order_num[0] - 1-byte indicates the float-point
		 * 			   order_num[1] - 1-byte indicates the fixed-point
		 * status - the status to this food
		 * taste_id - 1-byte indicates the taste preference id
		 *******************************************************/
		if(response.header.type == Type.ACK){
			//get the table id
			order.table_id = (short)((response.body[0] & 0x00FF) | ((response.body[1] & 0x00FF) << 8));

			//get the 2nd table id
			order.table2_id = (short)((response.body[2] & 0x00FF) | ((response.body[3] & 0x00FF) << 8));
			
			//get the minimum cost
			order.minimum_cost = (response.body[4] & 0x000000FF) | 
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
			Food[] orderFoods = new Food[foodNum]; 
			
			//get every order food's id and number
			int index = 15;
			for(int i = 0; i < orderFoods.length; i++){
				int foodID = (response.body[index] & 0x000000FF) |
							((response.body[index + 1] & 0x000000FF) << 8);
				int orderNum = (response.body[index + 2] & 0x000000FF) |
								((response.body[index + 3] & 0x000000FF) << 8);				
				short status = response.body[index + 4];				
				short tasteID = response.body[index + 5];
				//each food information takes up 5-byte
				index += 6;
				orderFoods[i] = new Food();
				orderFoods[i].alias_id = foodID;
				orderFoods[i].count = orderNum;
				orderFoods[i].status = status;
				orderFoods[i].taste.alias_id = tasteID;
			}
			order.foods = orderFoods;
		}
		return order;
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
				food.alias_id = (response.body[index] & 0x000000FF) |
							((response.body[index + 1] & 0x000000FF) << 8);
				
				//get the food's price
				food.price = ((response.body[index + 2] & 0x000000FF) |
							 ((response.body[index + 3] & 0x000000FF) << 8) |
							 ((response.body[index + 4] & 0x000000FF) << 16)) &	0x00FFFFFF;
				
				//get the kitchen no to this food
				food.kitchen = response.body[index + 5];
				
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
			int nTastes = response.body[index] & 0x000000FF;
			index++;
			//allocate the memory for taste preferences
			Taste[] tastes = new Taste[nTastes + 1];
			/**
			 * We put the no taste preference to the first element
			 */
			tastes[0] = new Taste(Taste.NO_TASTE, Taste.NO_PREFERENCE);
			//get each taste preference's information
			for(int i = 1; i < tastes.length; i++){
				
				//get the alias id to taste preference
				short alias_id = (short)(response.body[index] & 0x00FF);
				
				//get the price to taste preference
				int price = ((response.body[index + 1] & 0x000000FF) |
							((response.body[index + 2] & 0x000000FF) << 8) |
							((response.body[index + 3] & 0x000000FF) << 16)) & 0x00FFFFFF ;
				
				//get the length to taste preference string
				int length = response.body[index + 4];
				
				String preference = null;
				//get the taste preference string
				try{
					preference = new String(response.body, index + 5, length, "UTF-16BE");
				}catch(UnsupportedEncodingException e){}
				
				index += 5 + length;
				
				//add the taste
				tastes[i] = new Taste(alias_id, preference, price);
			}
			
			//get the amount of kitchens
			int nKitchens = response.body[index] & 0x000000FF;
			index++;
			//allocate the memory for kitchens
			Kitchen[] kitchens = new Kitchen[nKitchens];
			//get each kitchen's information
			for(int i = 0; i < kitchens.length; i++){
				
				//get the kitchen alias id
				short kitchen_id = (short)(response.body[index] & 0x00FF);
				
				//get 3 normal discounts
				byte dist_1 = response.body[index + 1];
				byte dist_2 = response.body[index + 2];
				byte dist_3 = response.body[index + 3];
				
				//get 3 member discounts
				byte mdist_1 = response.body[index + 4];
				byte mdist_2 = response.body[index + 5];
				byte mdist_3 = response.body[index + 6];
				
				//get the length of the kitchen name
				int length = response.body[index + 7];
				String kname = null;
				try{
					kname = new String(response.body, index + 8, length, "UTF-16BE");
				}catch(UnsupportedEncodingException e){}
				
				index += 8 + length;
				
				//add the kitchen
				kitchens[i] = new Kitchen(kname, kitchen_id,
										  dist_1, dist_2, dist_3,
										  mdist_1, mdist_2, mdist_3);
			}
			
			return new FoodMenu(foods, tastes, kitchens);
			
		}else{
			return new FoodMenu(new Food[0], new Taste[0], new Kitchen[0]);
		}
	}
	
	/**
	 * Parse the response associated with the query restaurant. 
	 * @param response The response containing the restaurant info.
	 * @return The restaurant info.
	 */
	public static Restaurant parseQueryRestaurant(ProtocolPackage response){
		Restaurant restaurant = new Restaurant();;
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
		 * len_1 : restaurant_name : len_2 : restaurant_info : len_3 : owner : len_4 : pwd2
		 * len_1 - 1-byte indicates the length of the restaurant name
		 * restaurant_name - restaurant name whose length equals "len_1"
		 * len_2 - 1-byte indicates the length of the restaurant info
		 * restaurant_info - restaurant info whose length equals "len_2"
		 * len_3 - 1-byte indicates the length of the terminal's owner name
		 * owner - the owner name of terminal
		 * len_4 : 1-byte indicates the length of the password2
		 * pwd2 : the 2nd password to this restaurant
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
			
			//calculate the position of the length to 2nd password
			offset = offset + length;
			length = response.body[offset];
			offset++;
			//get the 2nd password
			if(length != 0){
				restaurant.pwd2 = new String(response.body, offset, length);
			}
			
		}catch(UnsupportedEncodingException e){

		}
		return restaurant;
	}
}
