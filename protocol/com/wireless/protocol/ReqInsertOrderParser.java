package com.wireless.protocol;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Comparator;

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
	 * print_type[2] : src_tbl[2] : dest_tbl[2] : dest_tbl_2[2] : order_date[8] : category : custom_num : 
	 * food_amount : <Food1> : <Food2>... : <TmpFood1> : <TmpFood2>... : 
	 * 
	 * print_type[2] - 2-byte indicates the print type
	 * src_tbl[2] - 2-byte indicates the alias to source table
	 * dest_tbl[2] - 2-byte indicates the alias to destination table
	 * dest_tbl_2[2] - 2-byte indicates the alias to 2nd destination table
	 * order_date[8] - 8-byte indicates the last modified order date
	 * category - 1-byte indicates the category to this order  
	 * custom_num - 1-byte indicating the custom number for this table
	 * food_amount - 1-byte indicating the amount of foods
	 * 
	 * <Food>
	 * is_temp(0) : food_id[2] : order_num[2] : taste_id[2] : taste_id2[2] : taste_id3[2] : 
	 * len_tmp_taste : tmp_taste[n] : tmp_taste_alias[2] : tmp_taste_price[4] : 
	 * kitchen : hang_status : is_hurried
	 * is_temp(0) - "0" means this food is NOT temporary
	 * food_id[2] - 2-byte indicating the food's id
	 * order_num[2] - 2-byte indicating how many this foods are ordered
	 * 			   order_num[0] - 1-byte indicates the float-point
	 * 			   order_num[1] - 1-byte indicates the fixed-point
	 * taste_id[2] - 2-byte indicates the 1st taste preference id
	 * taste_id2[2] - 2-byte indicates the 2nd taste preference id
	 * taste_id3[2] - 2-byte indicates the 3rd taste preference id
	 * len_tmp_taste - 1-byte indicates the length of temporary taste
	 * tmp_taste[n] - the temporary taste value
	 * tmp_taste_alias[2] - 2-byte indicates the alias id to this temporary taste
	 * tmp_taste_price[4] - 4-byte indicates the price to this temporary taste
	 * kitchen - the kitchen to this food
	 * hang_status - the hang status to the food
	 * is_hurried - indicates whether the food is hurried
	 *
	 * <TmpFood>
	 * is_temp(1) : food_id[2] : order_amount[2] : unit_price[3] : hang_status : is_hurried : len : food_name[len] 
	 * is_temp(1) - "1" means this food is temporary
	 * food_id[2] - 2-byte indicating the food's id
	 * order_amount[2] - 2-byte indicating how many this foods are ordered
	 * unit_price[3] - 3-byte indicating the unit price to this food
	 * hang_status - the hang status to the food
	 * is_hurried - indicates whether the food is hurried
	 * len - the length of food's name
	 * food_name[len] - the value of the food name
	 *
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
		
		//get the alias to 2nd destination table
		order.destTbl2.aliasID = ((req.body[offset] & 0x000000FF) | ((req.body[offset + 1] & 0x000000FF) << 8));
		offset += 2;
		
		//get the last modified order date
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
		order.category = (short)(req.body[offset] & 0x000000FF);
		offset += 1;
		
		//get the number of customs
		order.customNum = (byte)(req.body[offset] & 0x000000FF);
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
				/**
				 * is_temp(1) : food_id[2] : order_amount[2] : unit_price[3] : hang_status : is_hurried : len : food_name[len] 
				 */
				//get the food id
				int foodID = (req.body[offset] & 0x000000FF) | ((req.body[offset + 1] & 0x000000FF) << 8);
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
				orderFoods[i].kitchen.aliasID = Kitchen.KITCHEN_TEMP;
				orderFoods[i].kitchen.dept.deptID = Department.DEPT_TEMP;
				orderFoods[i].aliasID = foodID;
				orderFoods[i].hangStatus = hangStatus;
				orderFoods[i].isHurried = isHurried;
				orderFoods[i].count = orderNum;
				orderFoods[i].setPrice(Util.int2Float(unitPrice));
				orderFoods[i].name = name != null ? name : "";
				
				
			}else{
				/**
				 * is_temp(0) : food_id[2] : order_num[2] : taste_id[2] : taste_id2[2] : taste_id3[2] : 
				 * len_tmp_taste : tmp_taste[n] : tmp_taste_alias[2] : tmp_taste_price[4] : 
				 * kitchen : hang_status : is_hurried
				 */
				
				//get the food id
				int foodID = (req.body[offset] & 0x000000FF) | ((req.body[offset + 1] & 0x000000FF) << 8);
				offset += 2;
				
				//get the order amount
				int orderAmount = (req.body[offset] & 0x000000FF) | ((req.body[offset + 1] & 0x000000FF) << 8);
				offset += 2;
				
				//get each taste id
				int[] tasteID = new int[3];
				tasteID[0] = (req.body[offset] & 0x000000FF) | ((req.body[offset + 1] & 0x000000FF) << 8);
				tasteID[1] = (req.body[offset + 2] & 0x000000FF) | ((req.body[offset + 3] & 0x000000FF) << 8);
				tasteID[2] = (req.body[offset + 4] & 0x000000FF) | ((req.body[offset + 5] & 0x000000FF) << 8);
				offset += 6;
			
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
				
				orderFoods[i] = new OrderFood();
				orderFoods[i].aliasID = foodID;
				orderFoods[i].count = orderAmount;
				
				orderFoods[i].isTemporary = false;
				
				orderFoods[i].tastes[0].aliasID = tasteID[0];
				orderFoods[i].tastes[1].aliasID = tasteID[1];
				orderFoods[i].tastes[2].aliasID = tasteID[2];
				
				orderFoods[i].tmpTaste = tmpTaste;
				
				Arrays.sort(orderFoods[i].tastes, new Comparator<Taste>(){

					public int compare(Taste taste1, Taste taste2) {
						return taste1.compare(taste2);
					}
					
				});
				
				orderFoods[i].kitchen.aliasID = (short)(kitchen & 0xFF);
				
				orderFoods[i].hangStatus = hangStatus;
				
				orderFoods[i].isHurried = isHurried;
			}
		}
		order.foods = orderFoods;
		
		return order;
	}
}
