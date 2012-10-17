package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

public final class RespQueryOrderParser {

	/**
	 * Parse the response associated with query order request.
	 * The result only doesn't contain the detail info like food's name 
	 * , price and taste's name and price 
	 * @param resp 
	 * the protocol package return from ProtocolConnector's ask() function
	 * @return
	 * the order result
	 */
	public static Order parse(ProtocolPackage resp, FoodMenu foodMenu){

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
		 * dest_tbl[2] : dest_tbl_2[2] : order_date[8] : minimum_cost[4] : category : 
		 * custom_num : price[4] : food_num : 
		 * <Food1> : <Food2>...
		 * <TmpFood1> : <TmpFood2>...
		 * 
		 * dest_tbl[2] - 2-byte indicates the alias to destination table 
		 * 
		 * dest_tbl_2[2] - 2-byte indicates the alias to 2nd destination table, only used table merger
		 * 
		 * order_date[8] - 8-byte indicates the order date time
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
		 * is_temp(0) : food_id[2] : order_amount[2] : status : taste_id[2] : taste_id2[2] : taste_id3[2] : 
		 * len_tmp_taste : tmp_taste[n] : tmp_taste_alias[2] : tmp_taste_price[4] : hang_status : 
		 * order_date[8] : nWaiter : waiter 
		 * is_temp : "0" means this food is NOT temporary
		 * food_id[2] - 2-byte indicating the food's id
		 * order_amount[2] - 2-byte indicating how many this foods are ordered
		 * 			   order_num[0] - 1-byte indicates the float-point
		 * 			   order_num[1] - 1-byte indicates the fixed-point
		 * status - the status to this food
		 * taste_id[2] - 2-byte indicates the 1st taste preference id
		 * taste_id2[2] - 2-byte indicates the 2nd taste preference id
		 * taste_id3[2] - 2-byte indicates the 3rd taste preference id
		 * len_tmp_taste - indicates the length of temporary taste
		 * tmp_taste[n] - indicates the value of temporary taste
		 * tmp_taste_alias[2] - 2-byte indicates the alias to this temporary taste
		 * tmp_taste_price[4] - 4-byte indicates the price to this temporary taste
		 * hang_status - indicates the hang status to the food
		 * order_date[8] - 8-byte indicates the order date
		 * nWaiter - the length of waiter
		 * waiter[nWaiter] - the waiter value
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
		if(resp.header.type == Type.ACK){
			int offset = 0;
			
			//get the table id
			order.srcTbl.aliasID = order.destTbl.aliasID = ((resp.body[offset] & 0x000000FF) | ((resp.body[offset + 1] & 0x000000FF) << 8));
			offset += 2;

			//get the 2nd table id
			order.destTbl2.aliasID = ((resp.body[offset] & 0x000000FF) | ((resp.body[offset + 1] & 0x000000FF) << 8));
			offset += 2;
			
			//get the last modified order date
			order.orderDate = (resp.body[offset] & 0x00000000000000FFL) |
			 				  ((resp.body[offset + 1] & 0x00000000000000FFL) << 8) |
			 				  ((resp.body[offset + 2] & 0x00000000000000FFL) << 16) |
			 				  ((resp.body[offset + 3] & 0x00000000000000FFL) << 24) |
			 				  ((resp.body[offset + 4] & 0x00000000000000FFL) << 32) |
			 				  ((resp.body[offset + 5] & 0x00000000000000FFL) << 40) |
			 				  ((resp.body[offset + 6] & 0x00000000000000FFL) << 48) |
			 				  ((resp.body[offset + 7] & 0x00000000000000FFL) << 56);
			offset += 8;

			//get the minimum cost
			order.minCost = (resp.body[offset] & 0x000000FF) | 
							((resp.body[offset + 1] & 0x000000FF ) << 8) |
							((resp.body[offset + 2] & 0x000000FF ) << 16) |
							((resp.body[offset + 3] & 0x000000FF ) << 24);
			offset += 4;
			
			//get the category
			order.category = (short)(resp.body[offset] & 0x00FF);
			offset += 1;
			
			//get the custom number
			order.customNum = (int)resp.body[offset];
			offset += 1;

			//get the total price
			order.totalPrice =  (resp.body[offset] & 0x000000FF) | 
								((resp.body[offset + 1] & 0x000000FF ) << 8) |
								((resp.body[offset + 2] & 0x000000FF ) << 16) |
								((resp.body[offset + 3] & 0x000000FF ) << 24);
			offset += 4;

			//get order food's number
			int foodNum = resp.body[offset];
			offset += 1;
			
			OrderFood[] orderFoods = new OrderFood[foodNum]; 
			
			//get every order food's id and number
			for(int i = 0; i < orderFoods.length; i++){
				//get the temporary flag
				boolean isTemporary = resp.body[offset] == 1 ? true : false;
				
				if(isTemporary){
					/**
					 * is_temp(1) : food_id[2] : order_amount[2] : unit_price[3] : hang_status : len : food_name[len]
					 */
					//get the food alias id
					int foodID = (resp.body[offset + 1] & 0x000000FF) |
								((resp.body[offset + 2] & 0x000000FF) << 8);
					//get the order amount
					int orderAmount = (resp.body[offset + 3] & 0x000000FF) |
									((resp.body[offset + 4] & 0x000000FF) << 8);
					//get the unit price
					int unitPrice = (resp.body[offset + 5] & 0x000000FF) |
									((resp.body[offset + 6] & 0x000000FF) << 8) |
									((resp.body[offset + 7] & 0x000000FF) << 16);
					//get the hang status
					short hangStatus = resp.body[offset + 8];
					//get the amount of food name bytes
					int len = resp.body[offset + 9];
					//get the food name
					String name = null;
					try{
						name = new String(resp.body, offset + 10, len, "UTF-8");
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
					 * is_temp(0) : food_id[2] : order_amount[2] : status : taste_id[2] : taste_id2[2] : taste_id3[2] : 
					 * len_tmp_taste : tmp_taste[n] : tmp_taste_alias[2] : tmp_taste_price[4] : hang_status 
					 * order_date[8] : nWaiter : waiter 
					 */
					//get the food alias id
					int foodID = (resp.body[offset + 1] & 0x000000FF) |
								((resp.body[offset + 2] & 0x000000FF) << 8);
					//get the order amount
					int orderAmount = (resp.body[offset + 3] & 0x000000FF) |
									((resp.body[offset + 4] & 0x000000FF) << 8);
					//get the food status
					short status = resp.body[offset + 5];
					
					int[] tasteID = new int[3];
					//get the 1st taste id
					tasteID[0] = (resp.body[offset + 6] & 0x000000FF) | 
									((resp.body[offset + 7] & 0x000000FF) << 8);
					//get the 2nd taste id
					tasteID[1] = (resp.body[offset + 8] & 0x000000FF) | 
									((resp.body[offset + 9] & 0x000000FF) << 8);
					//get the 3rd taste id
					tasteID[2] = (resp.body[offset + 10] & 0x000000FF) | 
									((resp.body[offset + 11] & 0x000000FF) << 8);
					//get the length of temporary taste
					int nTmpTaste = resp.body[offset + 12];
					
					offset += 13;
					
					Taste tmpTaste = null;
					if(nTmpTaste != 0){
						tmpTaste = new Taste();
						//get the temporary taste value
						try{
							tmpTaste.preference = new String(resp.body, offset, nTmpTaste, "UTF-8");
						}catch(UnsupportedEncodingException e){}
						offset += nTmpTaste;
						
						//get the alias id of temporary taste
						tmpTaste.aliasID = (resp.body[offset] & 0x000000FF) |
														((resp.body[offset + 1] & 0x000000FF) << 8);
						offset += 2;
						
						//get the price of temporary taste
						int tmpTastePrice = (resp.body[offset] & 0x000000FF) |
											((resp.body[offset + 1] & 0x000000FF) << 8) |
											((resp.body[offset + 2] & 0x000000FF) << 16) |
											((resp.body[offset + 3] & 0x000000FF) << 24);
						tmpTaste.setPrice(Util.int2Float(tmpTastePrice));
						offset += 4;
						
					}else{
						offset += 2 + /* alias of temporary taste takes up 2 bytes */ 
								  4;  /* price of temporary taste takes up 4 bytes */
					}
					
					//get the hang status
					short hangStatus = resp.body[offset];					
					offset++;
					
					//get the order date
					long orderDate = (resp.body[offset] & 0x00000000000000FFL) |
									 ((resp.body[offset + 1] & 0x00000000000000FFL) << 8) |
									 ((resp.body[offset + 2] & 0x00000000000000FFL) << 16) |
									 ((resp.body[offset + 3] & 0x00000000000000FFL) << 24) |
									 ((resp.body[offset + 4] & 0x00000000000000FFL) << 32) |
									 ((resp.body[offset + 5] & 0x00000000000000FFL) << 40) |
									 ((resp.body[offset + 6] & 0x00000000000000FFL) << 48) |
									 ((resp.body[offset + 7] & 0x00000000000000FFL) << 56);
					offset += 8;
					
					//get the length of waiter value
					int nWaiter = resp.body[offset];
					offset++;
					
					String waiter = null;
					if(nWaiter != 0){
						//get the value of waiter
						try{
							waiter = new String(resp.body, offset, nWaiter, "UTF-8");
						}catch(UnsupportedEncodingException e){}
						offset += nWaiter;
					}else{
						waiter = "";
					}
					
					orderFoods[i] = new OrderFood();
					orderFoods[i].isTemporary = false;
					orderFoods[i].aliasID = foodID;
					orderFoods[i].count = orderAmount;
					orderFoods[i].status = status;
					orderFoods[i].orderDate = orderDate;
					orderFoods[i].waiter = waiter;
					
					//Arrays.sort(tasteID, 0, tasteID.length);
					orderFoods[i].tastes[0].aliasID = tasteID[0];
					orderFoods[i].tastes[1].aliasID = tasteID[1];
					orderFoods[i].tastes[2].aliasID = tasteID[2];
					orderFoods[i].tmpTaste = tmpTaste;
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
				//order.foods[i].tasteNormalPref = Util.genTastePref(order.foods[i].tastes);
				//set the taste total price to this food
				//order.foods[i].setTasteNormalPrice(Util.genTastePrice(order.foods[i].tastes, order.foods[i].getPrice()));
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
	
}
