package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.util.NumericUtil;

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
		 * mode : type : seq : reserved : pin[6] : len[2] : <Body>
		 * <Header>
		 * mode - ORDER_BUSSINESS
		 * type - ACK
		 * seq - same as request
		 * reserved : 0x00
		 * pin[6] : same as request
		 * len[2] -  length of the <Body>
		 * <Body>
		 * order_id[4] : table[2] : order_date[8] : category : 
		 * custom_num : price[4] : food_num : 
		 * <Food1> : <Food2>...
		 * <TmpFood1> : <TmpFood2>...
		 * 
		 * order_id[4] - 4-byte indicates the order id
		 * 
		 * table[2] - 2-byte indicates the table id 
		 * 
		 * order_date[8] - 8-byte indicates the order date time
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
		 * is_temp(0) : food_id[2] : order_amount[2] : status : 
		 * normal_taste_amount : <NormalTaste1> : <NormalTaste2>... : 
		 * len_tmp_taste : tmp_taste[n] : tmp_taste_alias[2] : tmp_taste_price[4] : hang_status : 
		 * order_date[8] : nWaiter : waiter 
		 * is_temp : "0" means this food is NOT temporary
		 * food_id[2] - 2-byte indicating the food's id
		 * order_amount[2] - 2-byte indicating how many this foods are ordered
		 * status - the status to this food
		 * <NormalTaste>
		 * normal_taste_alias[2] : normal_taste_category
		 * normal_taste_alias[2] - 2-byte indicates the taste alias id
		 * normal_taste_category - 1-byte indicates the category to this normal taste		 
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
		 * is_temp(1) : tmp_food_alias :kitchen_alias[2] : order_amount[2] : unit_price[3] : hang_status : len : food_name[len] 
		 * is_temp(1) - "1" means this food is temporary
		 * tmp_food_alias[2] - 2-byte indicating the alias to this temporary food
		 * kitchen_alias[2] - 2-byte indicating the kitchen alias this temporary food belongs to
		 * order_amount[2] - 2-byte indicating how many this foods are ordered
		 * unit_price[3] - 3-byte indicating the unit price to this food
		 * hang_status - indicates the hang status to the food
		 * len - the length of food's name
		 * food_name[len] - the value of the food name
		 *******************************************************/
		if(resp.header.type == Type.ACK){
			int offset = 0;
			
			//get the order id
			order.mId = (resp.body[offset] & 0x000000FF) | 
						((resp.body[offset + 1] & 0x000000FF ) << 8) |
						((resp.body[offset + 2] & 0x000000FF ) << 16) |
						((resp.body[offset + 3] & 0x000000FF ) << 24);
			offset += 4;
			
			//get the table id
			//order.getSrcTbl().mAliasId = order.getDestTbl().mAliasId = ((resp.body[offset] & 0x000000FF) | ((resp.body[offset + 1] & 0x000000FF) << 8));
			offset += 2;
			
			//get the last modified order date
			order.mOrderDate = (resp.body[offset] & 0x00000000000000FFL) |
			 				  ((resp.body[offset + 1] & 0x00000000000000FFL) << 8) |
			 				  ((resp.body[offset + 2] & 0x00000000000000FFL) << 16) |
			 				  ((resp.body[offset + 3] & 0x00000000000000FFL) << 24) |
			 				  ((resp.body[offset + 4] & 0x00000000000000FFL) << 32) |
			 				  ((resp.body[offset + 5] & 0x00000000000000FFL) << 40) |
			 				  ((resp.body[offset + 6] & 0x00000000000000FFL) << 48) |
			 				  ((resp.body[offset + 7] & 0x00000000000000FFL) << 56);
			offset += 8;

			//get the category
			order.mCategory = (short)(resp.body[offset] & 0x00FF);
			offset += 1;
			
			//get the custom number
			order.mCustomNum = (int)resp.body[offset];
			offset += 1;

			//get the total price
			order.mTotalPrice =  (resp.body[offset] & 0x000000FF) | 
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
				
				offset++;
				
				if(isTemporary){
					/**
					 * is_temp(1) : tmp_food_alias[2] : kitchen_alias[2] : order_amount[2] : unit_price[3] : hang_status : len : food_name[len]
					 */
					
					//get the alias id to this temporary food
					int foodAlias = (resp.body[offset] & 0x000000FF) | ((resp.body[offset + 1] & 0x000000FF) << 8);
					offset += 2;
					
					//get the kitchen alias this temporary food belongs to
					int kitchenAlias = (resp.body[offset] & 0x000000FF) | ((resp.body[offset + 1] & 0x000000FF) << 8);
					offset += 2;
					
					//get the order amount
					int orderAmount = (resp.body[offset] & 0x000000FF) |
									((resp.body[offset + 1] & 0x000000FF) << 8);
					offset += 2;
					
					//get the unit price
					int unitPrice = (resp.body[offset] & 0x000000FF) |
									((resp.body[offset + 1] & 0x000000FF) << 8) |
									((resp.body[offset + 2] & 0x000000FF) << 16);
					offset += 3;
					
					//get the hang status
					//short hangStatus = resp.body[offset];
					offset += 1;
					
					//get the amount of food name bytes
					int lenOfTempFood = resp.body[offset];
					offset += 1;
					
					//get the food name
					String name = null;
					try{
						name = new String(resp.body, offset, lenOfTempFood, "UTF-8");
					}catch(UnsupportedEncodingException e){}
					offset += lenOfTempFood;
					
					orderFoods[i] = new OrderFood();
					orderFoods[i].isTemporary = true;
					orderFoods[i].setAliasId(foodAlias);
					orderFoods[i].mKitchen.mAliasId = (short)(kitchenAlias & 0x00FF);
					//orderFoods[i].hangStatus = hangStatus;
					orderFoods[i].setCountInternal(orderAmount);
					orderFoods[i].setPrice(NumericUtil.int2Float(unitPrice));
					orderFoods[i].mName = (name != null ? name : "");
					
				}else{

					//get the food alias id
					int foodAliasID = (resp.body[offset] & 0x000000FF) |
								((resp.body[offset + 1] & 0x000000FF) << 8);
					offset += 2;
					
					//get the detail to food from food menu
					for(int j = 0; j < foodMenu.foods.length; j++){
						if(foodAliasID == foodMenu.foods[j].mAliasId){
							orderFoods[i] = new OrderFood();
							orderFoods[i].copyFrom(foodMenu.foods[j]);
							break;
						}			
					}
					
					if(orderFoods[i] == null){
						orderFoods[i] = new OrderFood();
					}
					
					//get the order amount
					int orderAmount = (resp.body[offset] & 0x000000FF) |
									((resp.body[offset + 1] & 0x000000FF) << 8);
					offset += 2;
					
					//get the food status
					short status = resp.body[offset];
					offset += 1;
					
					//get the amount to normal tastes
					int nNormalTastes = resp.body[offset];
					offset += 1;
					
					Taste[] normalTastes = null;
					if(nNormalTastes > 0){
						
						normalTastes = new Taste[nNormalTastes];
						for(int j = 0; j < normalTastes.length; j++){
							//get alias id to each normal taste
							int tasteAliasID = (resp.body[offset] & 0x000000FF) | ((resp.body[offset + 1] & 0x000000FF) << 8);
							offset += 2;
							
							//get category to each normal taste
							//int tasteCate = resp.body[offset];
							offset += 1;
							
							//get the details from tastes in food menu
							Taste taste = srchTaste(tasteAliasID, foodMenu.tastes);
							if(taste != null){
								normalTastes[j] = taste;
								continue;
							}
							
							//get the details from styles in food menu
							Taste style = srchTaste(tasteAliasID, foodMenu.styles);
							if(style != null){
								normalTastes[j] = style;
								continue;
							}
							
							//get the details from specs in food menu
							Taste spec = srchTaste(tasteAliasID, foodMenu.specs);
							if(spec != null){
								normalTastes[j] = spec;
								continue;
							}
						}					
					}
					
					//get the length of temporary taste
					int lenOfTmpTaste = resp.body[offset];
					offset += 1;
					
					String tmpPref = null;
					//get the temporary taste value
					if(lenOfTmpTaste > 0){
						try{
							tmpPref = new String(resp.body, offset, lenOfTmpTaste, "UTF-8");
						}catch(UnsupportedEncodingException e){}
					}else{
						tmpPref = "";
					}
					offset += lenOfTmpTaste;
					
					//get the alias id to temporary taste
					int tmpAliasId = (resp.body[offset] & 0x000000FF) | ((resp.body[offset + 1] & 0x000000FF) << 8);
					offset += 2;
					
					//get the price of temporary taste
					int tmpTastePrice = (resp.body[offset] & 0x000000FF) |
										((resp.body[offset + 1] & 0x000000FF) << 8) |
										((resp.body[offset + 2] & 0x000000FF) << 16) |
										((resp.body[offset + 3] & 0x000000FF) << 24);
					offset += 4;
					
					Taste tmpTaste = null;					
					if(tmpPref.length() != 0 || tmpTastePrice != 0){
						tmpTaste = new Taste();
						tmpTaste.preference = tmpPref;
						tmpTaste.aliasId = tmpAliasId;
						tmpTaste.setPrice(NumericUtil.int2Float(tmpTastePrice));
					}
					
					//get the hang status
					//short hangStatus = resp.body[offset];					
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
					
					orderFoods[i].isTemporary = false;
					orderFoods[i].setAliasId(foodAliasID);
					orderFoods[i].setCountInternal(orderAmount);
					orderFoods[i].setStatus(status);
					orderFoods[i].mOrderDate = orderDate;
					orderFoods[i].mWaiter = waiter;					
					
					if(normalTastes != null || tmpTaste != null){
						orderFoods[i].makeTasetGroup(normalTastes, tmpTaste);
					}
					
					//orderFoods[i].hangStatus = hangStatus;
				}
			}
			order.mOrderFoods = orderFoods;
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
		
		Taste taste = null;
		
		for(int i = 0; i < tasteSrc.length; i++){
			if(aliasID == tasteSrc[i].aliasId){
				
				taste = new Taste(tasteSrc[i]);

				break;
			}
		}
		
		return taste;
		
	}
	
	
}
