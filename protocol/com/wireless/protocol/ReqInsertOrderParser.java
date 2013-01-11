package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

public class ReqInsertOrderParser {

	/******************************************************
	 * Design the insert order request looks like below
	 * <Header>
	 * mode : type : seq : reserved : pin[6] : len[2]
	 * mode - ORDER_BUSSINESS
	 * type - INSERT_ORDER
	 * seq - auto calculated and filled in
	 * reserved - 0x00
	 * pin[6] - auto calculated and filled in
	 * len[2] - length of the <Body>
	 * <Body>
	 * print_type[2] : src_tbl[2] : dest_tbl[2] : order_date[8] : category : custom_num : 
	 * food_amount : <Food1> : <Food2>... : <TmpFood1> : <TmpFood2>... : 
	 * 
	 * print_type[2] - 2-byte indicates the print type
	 * src_tbl[2] - 2-byte indicates the alias to source table
	 * dest_tbl[2] - 2-byte indicates the alias to destination table
	 * order_date[8] - 8-byte indicates the last modified order date
	 * category - 1-byte indicates the category to this order  
	 * custom_num - 1-byte indicating the custom number for this table
	 * food_amount - 1-byte indicating the amount of foods
	 * 
	 * <Food>
	 * is_temp(0) : food_alias[2] : order_amount[2] :
	 * normal_taste_amount : <NormalTaste1> : <NormalTaste2>... : 
	 * len_tmp_taste : tmp_taste[n] : tmp_taste_alias[2] : tmp_taste_price[4] : 
	 * kitchen_alias : hang_status : is_hurried : cancel_reason_id[4]
	 * is_temp(0) - "0" means this food is NOT temporary
	 * food_alias[2] - 2-byte indicating the alias id to food
	 * order_amount[2] - 2-byte indicating how many this foods are ordered
	 * normal_taste_amount - 1-byte indicates the amount to normal taste
	 * <NormalTaste>
	 * normal_taste_alias[2] : normal_taste_category
	 * normal_taste_alias[2] - 2-byte indicates the taste alias id
	 * normal_taste_category - 1-byte indicates the category to this normal taste
	 * len_tmp_taste - 1-byte indicates the length of temporary taste
	 * tmp_taste[n] - the temporary taste value
	 * tmp_taste_alias[2] - 2-byte indicates the alias id to this temporary taste
	 * tmp_taste_price[4] - 4-byte indicates the price to this temporary taste
	 * kitchen_alias - the kitchen alias this food belongs to
	 * hang_status - the hang status to the food
	 * is_hurried - indicates whether the food is hurried
	 * cancel_reason_id[4] - 4-byte indicates the id to cancel reason
	 *
	 * <TmpFood>
	 * is_temp(1) : tmp_food_alias[2] : kitchen_alias[2] : order_amount[2] : unit_price[3] : 
	 * hang_status : is_hurried : cancel_reason_id[4] : len : food_name[len] 
	 * is_temp(1) - "1" means this food is temporary
	 * tmp_food_alias[2] - 2-byte indicating the alias to temporary food
	 * kitchen_alias[2] - 2-byte indicating the alias id to kitchen this temporary food belongs to
	 * order_amount[2] - 2-byte indicating how many this foods are ordered
	 * unit_price[3] - 3-byte indicating the unit price to this food
	 * hang_status - the hang status to the food
	 * is_hurried - indicates whether the food is hurried
	 * cancel_reason_id[4] - 4-byte indicates the id to cancel reason
	 * len - the length of food's name
	 * food_name[len] - the value of the food name 
	 *******************************************************/
	public static Order parse(ProtocolPackage req){
		Order order = new Order();
		
		int offset = 0;
		
		//get the print type
		//order.print_type = ((req.body[offset] & 0x000000FF) | ((req.body[offset + 1] & 0x000000FF) << 8));
		offset += 2;
		
		//get the alias to source table
		order.srcTbl.aliasID = ((req.body[offset] & 0x000000FF) | ((req.body[offset + 1] & 0x000000FF) << 8));
		offset += 2;
		
		//get the alias to destination table
		order.destTbl.aliasID = ((req.body[offset] & 0x000000FF) | ((req.body[offset + 1] & 0x000000FF) << 8));
		offset += 2;
		
		//get the last modified order date
		order.orderDate = (req.body[offset] & 0x00000000000000FFL) |
		 				  ((req.body[offset + 1] & 0x00000000000000FFL) << 8) |
		 				  ((req.body[offset + 2] & 0x00000000000000FFL) << 16) |
		 				  ((req.body[offset + 3] & 0x00000000000000FFL) << 24) |
		 				  ((req.body[offset + 4] & 0x00000000000000FFL) << 32) |
		 				  ((req.body[offset + 5] & 0x00000000000000FFL) << 40) |
		 				  ((req.body[offset + 6] & 0x00000000000000FFL) << 48) |
		 				  ((req.body[offset + 7] & 0x00000000000000FFL) << 56);
		offset += 8;
		
		//get the category
		order.mCategory = (short)(req.body[offset] & 0x000000FF);
		offset += 1;
		
		//get the number of customs
		order.mCustomNum = (byte)(req.body[offset] & 0x000000FF);
		offset += 1;
		
		//get the number of foods
		int foodAmount = (byte)(req.body[offset] & 0x000000FF);
		offset += 1;
		
		OrderFood[] orderFoods = new OrderFood[foodAmount];
		
		//assign each order food's information, including the food's id and order number
		for(int i = 0; i < orderFoods.length; i++){
			boolean isTemporary = req.body[offset] == 1 ? true : false;
			offset += 1;
			
			if(isTemporary){

				//get the food alias this temporary food belongs to
				int foodAlias = (req.body[offset] & 0x00FF) | ((req.body[offset + 1] & 0x00FF) << 8);
				offset += 2;
				
				//get the kitchen alias this temporary food belongs to
				int kitchenAlias = (req.body[offset] & 0x00FF) | ((req.body[offset + 1] & 0x00FF) << 8);
				offset += 2;				
		
				//get the order amount
				int orderNum = (req.body[offset] & 0x000000FF) | ((req.body[offset + 1] & 0x000000FF) << 8);
				offset += 2;
				
				//get the unit price
				int unitPrice = (req.body[offset] & 0x000000FF) |
								((req.body[offset + 1] & 0x000000FF) << 8) |
								((req.body[offset + 2] & 0x000000FF) << 16);
				offset += 3;
				
				//get the hang status
				short hangStatus = req.body[offset];
				offset += 1;
				
				//get the hurried flag
				boolean isHurried = req.body[offset] == 1 ? true : false;
				offset += 1;
				
				//get the cancel reason id
				int reasonId = (req.body[offset] & 0x000000FF) |
							   ((req.body[offset + 1] & 0x000000FF) << 8) |
							   ((req.body[offset + 2] & 0x000000FF) << 16) |
							   ((req.body[offset + 3] & 0x000000FF) << 24);
				offset += 4;
				
				//get the amount of food name bytes
				int lenOfTempName = req.body[offset];
				offset += 1;
				
				//get the food name
				String name = null;
				try{
					name = new String(req.body, offset, lenOfTempName, "UTF-8");
				}catch(UnsupportedEncodingException e){}

				offset += lenOfTempName;

				orderFoods[i] = new OrderFood();
				orderFoods[i].isTemporary = true;
				orderFoods[i].mAliasId = foodAlias;
				orderFoods[i].kitchen.aliasID = (short)(kitchenAlias & 0x00FF);
				orderFoods[i].hangStatus = hangStatus;
				orderFoods[i].isHurried = isHurried;
				if(reasonId != CancelReason.NO_REASON){
					orderFoods[i].setCancelReason(new CancelReason(reasonId));
				}
				orderFoods[i].setCountInternal(orderNum);
				orderFoods[i].setPrice(Util.int2Float(unitPrice));
				orderFoods[i].mName = name != null ? name : "";
				
				
			}else{
				
				//get the food id
				int foodAlias = (req.body[offset] & 0x000000FF) | ((req.body[offset + 1] & 0x000000FF) << 8);
				offset += 2;
				
				//get the order amount
				int orderAmount = (req.body[offset] & 0x000000FF) | ((req.body[offset + 1] & 0x000000FF) << 8);
				offset += 2;
				
				//get the amount to normal tastes
				int nNormalTastes = req.body[offset];
				offset += 1;
				
				Taste[] normalTastes = null;
				if(nNormalTastes > 0){
					normalTastes = new Taste[nNormalTastes];
					for(int j = 0; j < normalTastes.length; j++){
						normalTastes[j] = new Taste();
						//get alias id to each normal taste
						normalTastes[j].aliasID = (req.body[offset] & 0x000000FF) | ((req.body[offset + 1] & 0x000000FF) << 8);
						offset += 2;
						//get category to each normal taste
						normalTastes[j].category = req.body[offset];
						offset += 1;
					}
				}
			
				//get the length of temporary taste value
				int lenOfTmpTaste = req.body[offset];
				offset += 1;

				Taste tmpTaste = null;
				if(lenOfTmpTaste != 0){
					//get the temporary taste value
					tmpTaste = new Taste();
					try{
						tmpTaste.preference = new String(req.body, offset, lenOfTmpTaste, "UTF-8");
					}catch(UnsupportedEncodingException e){}
					offset += lenOfTmpTaste;
					
					//get the alias id to temporary taste
					tmpTaste.aliasID = (req.body[offset] & 0x000000FF) | ((req.body[offset + 1] & 0x000000FF) << 8);
					offset += 2;
					
					//get the price of temporary taste
					int tmpTastePrice = (req.body[offset] & 0x000000FF) |
										((req.body[offset + 1] & 0x000000FF) << 8) |
										((req.body[offset + 2] & 0x000000FF) << 16) |
										((req.body[offset + 3] & 0x000000FF) << 24);
					tmpTaste.setPrice(Util.int2Float(tmpTastePrice));
					offset += 4;
					
					//generate the alias id according to preference and price
					tmpTaste.aliasID = Math.abs((tmpTaste.preference.hashCode() + tmpTastePrice) % Integer.MAX_VALUE);
					
				}else{
					offset += 2 + /* alias to this temporary taste takes up 2 bytes */
							  4;  /* price to this temporary taste takes up 4 bytes */
				}
				
				//get the kitchen 
				short kitchen = req.body[offset];
				offset += 1;
				
				//get the hang up status
				short hangStatus = req.body[offset];
				offset += 1;
				
				//get the hurried flag
				boolean isHurried = req.body[offset] == 1 ? true : false;
				offset += 1;				
				
				//get the cancel reason id
				int reasonId = (req.body[offset] & 0x000000FF) |
							   ((req.body[offset + 1] & 0x000000FF) << 8) |
							   ((req.body[offset + 2] & 0x000000FF) << 16) |
							   ((req.body[offset + 3] & 0x000000FF) << 24);
				offset += 4;
				
				orderFoods[i] = new OrderFood();
				orderFoods[i].setAliasId(foodAlias);
				orderFoods[i].setCountInternal(orderAmount);
				
				orderFoods[i].isTemporary = false;
				
				if(normalTastes != null || tmpTaste != null){
					orderFoods[i].mTasteGroup = new TasteGroup(orderFoods[i], normalTastes, tmpTaste);
				}
				
				orderFoods[i].kitchen.aliasID = (short)(kitchen & 0xFF);
				
				orderFoods[i].hangStatus = hangStatus;
				
				orderFoods[i].isHurried = isHurried;
				
				if(reasonId != CancelReason.NO_REASON){
					orderFoods[i].setCancelReason(new CancelReason(reasonId));
				}
			}
		}
		order.foods = orderFoods;
		
		return order;
	}
}
