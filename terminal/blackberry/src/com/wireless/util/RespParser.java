package com.wireless.util;

import java.io.UnsupportedEncodingException;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Type;
import com.wireless.terminal.WirelessOrder;

public class RespParser {
	
	/**
	 * Parse the response associated with query order request.
	 * The order food doesn't have the name and price.
	 * @param response 
	 * the protocol package return from ProtocolConnector's ask() function
	 * @return
	 * the vector containing the food instance
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
		 * table[2] : custom_num : price[2] : food_num : <Food1> : <Food2>...
		 * table[2] - 2-byte indicates the table id 
		 * custom_num - 1-byte indicating the number of the custom for this order
		 * price[4] - 4-byte indicating the total price to this order
		 * 			  price[0] - 1-byte indicating the float-point
		 * 			  price[1..3] - 3-byte indicating the fixed-point
		 * food_num - 1-byte indicating the number of ordered food
		 * <Food>
		 * food_id[2] : order_num[2] : taste_id
		 * food_id[2] - 2-byte indicating the food's id
		 * order_num[2] - 2-byte indicating how many this foods are ordered
		 * 			   order_num[0] - 1-byte indicates the float-point
		 * 			   order_num[1] - 1-byte indicates the fixed-point
		 * taste_id - 1-byte indicates the taste preference id
		 *******************************************************/
		if(response.header.type == Type.ACK){
			//get the table id
			order.tableID = (short)((response.body[0] & 0x00FF) | ((response.body[1] & 0x00FF) << 8));

			//get the custom number
			order.customNum = (int)response.body[2];

			//get the total price
			order.setTotalPrice( (response.body[3] & 0x000000FF) | 
								((response.body[4] & 0x000000FF ) << 8) |
								((response.body[5] & 0x000000FF ) << 16) |
								((response.body[6] & 0x000000FF ) << 24));

			//get order food's number
			int foodNum = response.body[7];
			Food[] orderFoods = new Food[foodNum]; 
			
			//get every order food's id and number
			int index = 8;
			for(int i = 0; i < orderFoods.length; i++){
				int foodID = (response.body[index] & 0x000000FF) |
							((response.body[index + 1] & 0x000000FF) << 8);
				int orderNum = (response.body[index + 2] & 0x000000FF) |
								((response.body[index + 3] & 0x000000FF) << 8);
				short tasteID = response.body[index + 4];
				//each food information takes up 5-byte
				index += 5;
				orderFoods[i] = new Food();
				orderFoods[i].alias_id = foodID;
				orderFoods[i].count = orderNum;
				orderFoods[i].taste.alias_id = tasteID;
				if(tasteID != Taste.NO_TASTE){
					try{
						orderFoods[i].taste.preference = WirelessOrder.foodMenu.tastes[tasteID].preference;
					}catch(ArrayIndexOutOfBoundsException e){}
				}
			}
			order.foods = orderFoods;
		}
		return order;
	}

	/**
	 * Parse the response associated with query order request.
	 * The order food has the name and price.
	 * @param response 
	 * the protocol package return from ProtocolConnector's ask() function
	 * @return
	 * the vector containing the food instance
	 */
	public static Order parseQueryOrderEx(ProtocolPackage response){
		Order order = parseQueryOrder(response);
		for(int i = 0; i < order.foods.length; i++){
			for(int j = 0; j < WirelessOrder.foodMenu.foods.length; j++){
				if(order.foods[i].alias_id == WirelessOrder.foodMenu.foods[j].alias_id){
					order.foods[i].name = WirelessOrder.foodMenu.foods[j].name;
					order.foods[i].price = WirelessOrder.foodMenu.foods[j].price;
					break;					
				}
			}
			
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
		 * food_amount[2] : <Food1> : <Food2>... : taste_amount : <Taste1> : <Taste2> ...
		 * food_amount[2] - 2-byte indicating the amount of the foods listed in the menu
		 * <Food>
		 * food_id[2] : price[3] : len : name[len]
		 * food_id[2] - 2-byte indicating the food's id
		 * price[3] - 3-byte indicating the food's price
		 * 			  price[0] 1-byte indicating the float point
		 * 			  price[1..2] 2-byte indicating the fixed point
		 * len - 1-byte indicating the length of the food's name
		 * name[len] - the food's name whose length equals "len"
		 * 
		 * taste_amount - 1-byte indicates the amount of the taste preference
		 * <Taste>
		 * taste_id : price[3] : len : preference[len]
		 * taste_id - 1-byte indicating the alias id to this taste preference
		 * price[3] - 3-byte indicating the food's price
		 * 			  price[0] 1-byte indicating the float point
		 * 			  price[1..2] 2-byte indicating the fixed point
		 * len - 1-byte indicating the length of the preference
		 * preference[len] - the string to preference whose length is "len"
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
				
				//get the length of the food's name
				int length = response.body[index + 5];
				
				//get the name value 
				try{
					food.name = new String(response.body, index + 6, length, "UTF-16BE");
				}catch(UnsupportedEncodingException e){

				}
				
				index += 6 + length;
				
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
				short alias_id = response.body[index];
				
				//get the price to taste preference
				int price = ((response.body[index + 2] & 0x000000FF) |
							((response.body[index + 3] & 0x000000FF) << 8) |
							((response.body[index + 4] & 0x000000FF) << 16)) & 0x00FFFFFF ;
				
				//get the length to taste preference string
				int length = response.body[index + 5];
				
				String preference = null;
				//get the taste preference string
				try{
					preference = new String(response.body, index + 6, length, "UTF-16BE");
				}catch(UnsupportedEncodingException e){}
				
				index += 6 + length;
				
				//add the taste
				tastes[i] = new Taste(alias_id, preference, price);
			}
			
			return new FoodMenu(foods, tastes);
			
		}else{
			return new FoodMenu(new Food[0], new Taste[0]);
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
		 * len_1 : restaurant_name : len_2 : restaurant_info 
		 * len_1 - 1-byte indicates the length of the restaurant name
		 * restaurant_name - restaurant name whose length equals "len_1"
		 * len_2 - 1-byte indicating the length of the restaurant info
		 * restaurant_info - restaurant info whose length equals "len_2"
		 * len_3 - 1-byte indicates the length of the terminal's owner name
		 * owner - the owner name of terminal
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
		}catch(UnsupportedEncodingException e){

		}
		return restaurant;
	}
}
