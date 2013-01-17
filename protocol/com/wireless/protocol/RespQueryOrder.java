package com.wireless.protocol;

import java.io.UnsupportedEncodingException;


public class RespQueryOrder extends RespPackage{

	public RespQueryOrder(ProtocolHeader reqHeader, Order order) throws UnsupportedEncodingException {
		super(reqHeader);
		header.type = Type.ACK;
		
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
		 * order_id[4] : table_alias[2] : order_date[8] : category : 
		 * custom_num : price[4] : food_num : 
		 * <Food1> : <Food2>...
		 * <TmpFood1> : <TmpFood2>...
		 * 
		 * order_id[4] - 4-byte indicates the order id
		 * 
		 * table_alias[2] - 2-byte indicates the table alias id 
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
		
		//calculate the amount of bytes that the food list needs
		int foodLen = 0;
		for(int i = 0; i < order.foods.length; i++){
			if(order.foods[i].isTemporary){
				/* is_temp(1) : kitchen_alias[2] : order_amount[2] : unit_price[3] : hang_status : len : food_name[len] */
				foodLen += 1 + /* is_temp */
						   2 + /* tmp_food_alias[2] */
						   2 + /* kitchen_alias[2] */
						   2 + /* order_amount[2] */
						   3 + /* unit_price[3] */
						   1 + /* hang_status */
						   1 + /* length of temporary food value */
						   order.foods[i].mName.getBytes("UTF-8").length; /* tmp_food_name */
			}else{
				foodLen += 1 + /* is_temp */
						   2 + /* food_id[2] */
						   2 + /* order_amount[2] */
						   1 + /* status */
						   1 + /* normal_taste_amount */
						   (order.foods[i].hasNormalTaste() ? order.foods[i].mTasteGroup.getNormalTastes().length * 3 : 0) + /* each alias id and category to normal taste takes up 3-byte */
						   1 + /* len_tmp_taste */
						   (order.foods[i].hasTmpTaste() ? order.foods[i].mTasteGroup.mTmpTaste.preference.getBytes("UTF-8").length : 0) + /* the value to tmp_taste */
						   2 + /* tmp_taste_alias[2] */
						   4 + /* tmp_taste_price[4] */
						   1 + /* hang_status */
						   8 + /* order_date[4] */
						   1 + /* nWaiter */
						   (order.foods[i].mWaiter == null ? 0 : order.foods[i].mWaiter.getBytes("UTF-8").length); /* the value of waiter */
			}
		}
		
		//calculate the body's length
		int bodyLen = 4 + /* order id takes up 4-byte */
					  2 + /* table alias id takes up 2-byte */
					  8 + /* order date time takes up 8-byte */
					  1 + /* category takes up 1-byte */
					  1 + /* custom number takes up 1-byte */ 
					  4 + /* price takes up 4-byte */ 
					  1 + /* food number takes up 1-byte */
					  foodLen;  /* the amount of bytes that food list takes up */
		
		
		
		//assign the body length to header's length field
		header.length[0] = (byte)(bodyLen & 0x000000FF);
		header.length[1] = (byte)((bodyLen & 0x0000FF00) >> 8);
		
		//allocate the memory for body
		body = new byte[bodyLen];
		
		int offset = 0;
		
		//assign the order id
		body[offset] = (byte)(order.mId & 0x000000FF);
		body[offset + 1] = (byte)((order.mId & 0x0000FF00) >> 8);
		body[offset + 2] = (byte)((order.mId & 0x00FF0000) >> 16);
		body[offset + 3] = (byte)((order.mId & 0xFF000000) >> 24);
		offset += 4;
		
		//assign the table alias id
		body[offset] = (byte)(order.destTbl.aliasID & 0x00FF);
		body[offset + 1] = (byte)((order.destTbl.aliasID & 0xFF00) >> 8);
		offset += 2;
		
		//assign the order date time
		body[offset] = (byte)(order.orderDate & 0x00000000000000FFL);
		body[offset + 1] = (byte)((order.orderDate & 0x000000000000FF00L) >> 8);
		body[offset + 2] = (byte)((order.orderDate & 0x0000000000FF0000L) >> 16);
		body[offset + 3] = (byte)((order.orderDate & 0x00000000FF000000L) >> 24);
		body[offset + 4] = (byte)((order.orderDate & 0x000000FF00000000L) >> 32);
		body[offset + 5] = (byte)((order.orderDate & 0x0000FF0000000000L) >> 40);
		body[offset + 6] = (byte)((order.orderDate & 0x00FF000000000000L) >> 48);
		body[offset + 7] = (byte)((order.orderDate & 0xFF00000000000000L) >> 56);
		offset += 8;
		
		//assign the category
		body[offset] = (byte)(order.mCategory & 0x00FF);
		offset += 1;
		
		//assign the custom number
		body[offset] = (byte)(order.mCustomNum & 0x000000FF);
		offset += 1;
		
		//assign the total price
		body[offset] = (byte)(order.cashIncome & 0x000000FF);
		body[offset + 1] = (byte)((order.cashIncome & 0x0000FF00) >> 8);
		body[offset + 2] = (byte)((order.cashIncome & 0x00FF0000) >> 16);
		body[offset + 3] = (byte)((order.cashIncome & 0xFF000000) >> 24);
		offset += 4;
		
		//assign the food number
		body[offset] = (byte)(order.foods.length & 0x000000FF);		
		offset += 1;
		
		//assign each food information, including food'id and order number
		for(int i = 0; i < order.foods.length; i++){
			if(order.foods[i].isTemporary){
				byte[] bytesToTempFood = order.foods[i].mName.getBytes("UTF-8");
				
				/* is_temp(1) : tmp_food_alias[2] : kitchen_alias[2] : order_amount[2] : unit_price[3] : hang_status : len : food_name[len] */
				
				//assign the temporary flag 
				body[offset] = 1;
				offset += 1;
				
				//assign the alias id to this temporary food 
				body[offset] = (byte)(order.foods[i].mAliasId & 0x000000FF);
				body[offset + 1] = (byte)((order.foods[i].mAliasId & 0x0000FF00) >> 8);
				offset += 2;

				
				//assign the kitchen_alias this temporary food belongs to
				body[offset] = (byte)(order.foods[i].mKitchen.mAliasId & 0x000000FF);
				body[offset + 1] = (byte)((order.foods[i].mKitchen.mAliasId & 0x0000FF00) >> 8);
				offset += 2;
				
				//assign the order amount
				body[offset] = (byte)(order.foods[i].getCountInternal() & 0x000000FF);
				body[offset + 1] = (byte)((order.foods[i].getCountInternal() & 0x0000FF00) >> 8);
				offset += 2;
				
				//assign the unit price
				body[offset] = (byte)(order.foods[i].mUnitPrice & 0x000000FF);
				body[offset + 1] = (byte)((order.foods[i].mUnitPrice & 0x0000FF00) >> 8);
				body[offset + 2] = (byte)((order.foods[i].mUnitPrice & 0x00FF0000) >> 16);
				offset += 3;
				
				//assign the hang status
				body[offset] = (byte)(order.foods[i].hangStatus);
				offset += 1;
				
				//assign the amount of temporary food name's byte
				body[offset] = (byte)(bytesToTempFood.length);
				offset += 1;
				
				//assign the value of temporary food name
				for(int j = 0; j < bytesToTempFood.length; j++){
					body[offset + j] = bytesToTempFood[j];
				}
				offset += bytesToTempFood.length;
				
				
			}else{

				//assign the temporary flag
				body[offset] = 0;
				offset += 1;
				
				//assign the food alias id
				body[offset] = (byte)(order.foods[i].mAliasId & 0x000000FF);
				body[offset + 1] = (byte)((order.foods[i].mAliasId & 0x0000FF00) >> 8);
				offset += 2;
				
				//assign the order amount
				body[offset] = (byte)(order.foods[i].getCountInternal() & 0x000000FF);
				body[offset + 1] = (byte)((order.foods[i].getCountInternal() & 0x0000FF00) >> 8);
				offset += 2;
				
				//assign the food status
				body[offset] = (byte)(order.foods[i].mStatus);
				offset += 1;
				
				if(order.foods[i].hasNormalTaste()){
					Taste[] normalTaste = order.foods[i].mTasteGroup.getNormalTastes();
					//assign the normal taste amount
					body[offset] = (byte)(normalTaste.length);
					offset += 1;
					for(int j = 0; j < normalTaste.length; j++){
						//assign alias id to each normal taste 
						body[offset] = (byte)(normalTaste[j].aliasID & 0x00FF);
						body[offset + 1] = (byte)((normalTaste[j].aliasID & 0xFF00) >> 8);
						offset += 2;
						
						//assign category to each normal taste
						body[offset] = (byte)normalTaste[j].category;
						offset += 1;
						
					}					
				}else{
					body[offset] = 0x00;
					offset += 1;
				}
				
				if(order.foods[i].hasTmpTaste()){
					
					byte[] bytesToTmpTaste = order.foods[i].mTasteGroup.mTmpTaste.preference.getBytes("UTF-8");
					//assign the length of temporary taste value
					body[offset] = (byte)(bytesToTmpTaste.length);
					offset++;
					
					//assign the value of temporary taste value
					for(int cnt = 0; cnt < bytesToTmpTaste.length; cnt++){
						body[offset + cnt] = bytesToTmpTaste[cnt];
					}
					offset += bytesToTmpTaste.length;
					
					//assign the alias id to temporary taste
					body[offset] = (byte)(order.foods[i].mTasteGroup.mTmpTaste.aliasID & 0x00FF);
					body[offset + 1] = (byte)((order.foods[i].mTasteGroup.mTmpTaste.aliasID & 0xFF00) >> 8);
					offset += 2;
					
					//assign the price to temporary taste
					body[offset] = (byte)(order.foods[i].mTasteGroup.mTmpTaste.price & 0x000000FF);
					body[offset + 1] = (byte)((order.foods[i].mTasteGroup.mTmpTaste.price & 0x0000FF00) >> 8);
					body[offset + 2] = (byte)((order.foods[i].mTasteGroup.mTmpTaste.price & 0x00FF0000) >> 16);
					body[offset + 3] = (byte)((order.foods[i].mTasteGroup.mTmpTaste.price & 0xFF000000) >> 24);
					
					offset += 4;
					
				}else{
					//assign the length of temporary taste value
					body[offset] = 0x00;
					//assign the temporary taste alias
					body[offset + 1] = 0x00;
					body[offset + 2] = 0x00;
					//assign the temporary taste price
					body[offset + 3] = 0x00;
					body[offset + 4] = 0x00;
					body[offset + 5] = 0x00;
					body[offset + 6] = 0x00;
					
					offset += 7;					
				}
				//assign the hang status
				body[offset] = (byte)(order.foods[i].hangStatus);
				offset++;
				
				//assign the order date
				body[offset] = (byte)(order.foods[i].mOrderDate & 0x00000000000000FF);
				body[offset + 1] = (byte)((order.foods[i].mOrderDate & 0x000000000000FF00L) >> 8);
				body[offset + 2] = (byte)((order.foods[i].mOrderDate & 0x0000000000FF0000L) >> 16);
				body[offset + 3] = (byte)((order.foods[i].mOrderDate & 0x00000000FF000000L) >> 24);
				body[offset + 4] = (byte)((order.foods[i].mOrderDate & 0x000000FF00000000L) >> 32);
				body[offset + 5] = (byte)((order.foods[i].mOrderDate & 0x0000FF0000000000L) >> 40);
				body[offset + 6] = (byte)((order.foods[i].mOrderDate & 0x00FF000000000000L) >> 48);
				body[offset + 7] = (byte)((order.foods[i].mOrderDate & 0xFF00000000000000L) >> 56);
				offset += 8;
				
				if(order.foods[i].mWaiter != null){
					byte[] bytesToWaiter = order.foods[i].mWaiter.getBytes("UTF-8");
					//assign the length of waiter
					body[offset] = (byte)(bytesToWaiter.length);
					offset++;
					//assign the value of waiter
					for(int cnt = 0; cnt < bytesToWaiter.length; cnt++){
						body[offset + cnt] = bytesToWaiter[cnt];
					}
					offset += bytesToWaiter.length;
				}else{
					//assign the length of waiter to zero
					body[offset] = 0x00;
					offset++;
				}
			}
		}
	}

}
