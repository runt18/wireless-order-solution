package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

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
public class RespQueryOrder extends RespPackage{

	public RespQueryOrder(ProtocolHeader reqHeader, Order order) throws UnsupportedEncodingException {
		super(reqHeader);
		header.type = Type.ACK;
		
		//calculate the amount of bytes that the food list needs
		int foodLen = 0;
		for(int i = 0; i < order.foods.length; i++){
			if(order.foods[i].isTemporary){
				/* is_temp(1) : food_id[2] : order_amount[2] : unit_price[3] : hang_status : len : food_name[len] */
				foodLen += 1 + /* is_temp */
						   2 + /* food_id[2] */
						   2 + /* order_amount[2] */
						   3 + /* unit_price[3] */
						   1 + /* hang_status */
						   1 + /* length of temporary food value */
						   order.foods[i].name.getBytes("UTF-8").length; /* tmp_food_name */
			}else{
				/* is_temp(0) : food_id[2] : order_amount[2] : status : taste_id[2] : taste_id2[2] : taste_id3[2] : len_tmp_taste : tmp_taste[n] : tmp_taste_price[4] : hang_status */
				foodLen += 1 + /* is_temp */
						   2 + /* food_id[2] */
						   2 + /* order_amount[2] */
						   1 + /* status */
						   2 + /* taste_id[2] */
						   2 + /* taste_id2[2] */
						   2 + /* taste_id3[2] */
						   1 + /* len_tmp_taste */
						   (order.foods[i].tmpTaste == null ? 0 : order.foods[i].tmpTaste.preference.getBytes("UTF-8").length) + /* tmp_taset */
						   2 + /* tmp_taste_alias[2] */
						   4 + /* tmp_taste_price[4] */
						   1 + /* hang_status */
						   8 + /* order_date[4] */
						   1 + /* nWaiter */
						   (order.foods[i].waiter == null ? 0 : order.foods[i].waiter.getBytes("UTF-8").length); /* the value of waiter */
			}
		}
		
		//calculate the body's length
		int bodyLen = 2 + /* table id takes up 2-byte */
					2 + /* 2nd table id takes up 2-byte */
					4 + /* minimum cost takes up 4-byte */
					1 + /* category takes up 1-byte */
					1 + /* custom number takes up 1-byte */ 
					4 + /* price takes up 4-byte */ 
					1 + /* food number takes up 1-byte */
					foodLen;  /* the amount of bytes that food list needs */
		
		//assign the body length to header's length field
		header.length[0] = (byte)(bodyLen & 0x000000FF);
		header.length[1] = (byte)((bodyLen & 0x0000FF00) >> 8);
		
		//allocate the memory for body
		body = new byte[bodyLen];
		
		//assign the table id
		body[0] = (byte)(order.destTbl.aliasID & 0x00FF);
		body[1] = (byte)((order.destTbl.aliasID & 0xFF00) >> 8);
		
		//assign the 2nd table id
		body[2] = (byte)(order.destTbl2.aliasID & 0x00FF);
		body[3] = (byte)((order.destTbl2.aliasID & 0xFF00) >> 8);
		
		//assign the minimum cost
		body[4] = (byte)(order.minCost & 0x000000FF);
		body[5] = (byte)((order.minCost & 0x0000FF00) >> 8);
		body[6] = (byte)((order.minCost & 0x00FF0000) >> 16);
		body[7] = (byte)((order.minCost & 0xFF000000) >> 24);
		
		//assign the category
		body[8] = (byte)(order.category & 0x00FF);
		
		//assign the custom number
		body[9] = (byte)(order.custom_num & 0x000000FF);
		
		//assign the total price
		body[10] = (byte)(order.cashIncome & 0x000000FF);
		body[11] = (byte)((order.cashIncome & 0x0000FF00) >> 8);
		body[12] = (byte)((order.cashIncome & 0x00FF0000) >> 16);
		body[13] = (byte)((order.cashIncome & 0xFF000000) >> 24);
		
		//assign the food number
		body[14] = (byte)(order.foods.length & 0x000000FF);
		
		int offset = 15;
		
		//assign each food information, including food'id and order number
		for(int i = 0; i < order.foods.length; i++){
			if(order.foods[i].isTemporary){
				byte[] nameBytes = order.foods[i].name.getBytes("UTF-8");
				
				/* is_temp(1) : food_id[2] : order_amount[2] : unit_price[3] : hang_status : len : food_name[len] */
				
				//assign the temporary flag 
				body[offset] = 1;
				//assign the food id
				body[offset + 1] = (byte)(order.foods[i].aliasID & 0x000000FF);
				body[offset + 2] = (byte)((order.foods[i].aliasID & 0x0000FF00) >> 8);
				//assign the order amount
				body[offset + 3] = (byte)(order.foods[i].count & 0x000000FF);
				body[offset + 4] = (byte)((order.foods[i].count & 0x0000FF00) >> 8);
				//assign the unit price
				body[offset + 5] = (byte)(order.foods[i].price & 0x000000FF);
				body[offset + 6] = (byte)((order.foods[i].price & 0x0000FF00) >> 8);
				body[offset + 7] = (byte)((order.foods[i].price & 0x00FF0000) >> 16);
				//assign the hang status
				body[offset + 8] = (byte)(order.foods[i].hangStatus);
				//assign the amount of food name's byte
				body[offset + 9] = (byte)(nameBytes.length);
				//assign the value of food name
				for(int cnt = 0; cnt < nameBytes.length; cnt++){
					body[offset + 10 + cnt] = nameBytes[cnt];
				}
				
				offset += 1 + 2 + 2 + 3 + 1 + 1 + nameBytes.length;
				
			}else{
				/** 
				 * is_temp(0) : food_id[2] : order_amount[2] : status : taste_id[2] : taste_id2[2] : taste_id3[2] : 
				 * len_tmp_taste : tmp_taste[n] : tmp_taste_alias[2] : tmp_taste_price[4] : hang_status 
				 * order_date[8] : nWaiter : waiter 
				 */
				//assign the temporary flag
				body[offset] = 0;
				//assign the food alias id
				body[offset + 1] = (byte)(order.foods[i].aliasID & 0x000000FF);
				body[offset + 2] = (byte)((order.foods[i].aliasID & 0x0000FF00) >> 8);
				//assign the order amount
				body[offset + 3] = (byte)(order.foods[i].count & 0x000000FF);
				body[offset + 4] = (byte)((order.foods[i].count & 0x0000FF00) >> 8);
				//assign the food status
				body[offset + 5] = (byte)(order.foods[i].status);
				//assign the 1st taste id
				body[offset + 6] = (byte)(order.foods[i].tastes[0].aliasID & 0x00FF);
				body[offset + 7] = (byte)((order.foods[i].tastes[0].aliasID & 0xFF00) >> 8);
				//assign the 2nd taste id
				body[offset + 8] = (byte)(order.foods[i].tastes[1].aliasID & 0x00FF);
				body[offset + 9] = (byte)((order.foods[i].tastes[1].aliasID & 0xFF00) >> 8);
				//assign the 3rd taste id
				body[offset + 10] = (byte)(order.foods[i].tastes[2].aliasID & 0x00FF);
				body[offset + 11] = (byte)((order.foods[i].tastes[2].aliasID & 0xFF00) >> 8);
				
				offset += 12;
				
				if(order.foods[i].tmpTaste != null){
					byte[] tmpTasteBytes = order.foods[i].tmpTaste.preference.getBytes("UTF-8");
					//assign the length of temporary taste value
					body[offset] = (byte)(tmpTasteBytes.length);
					offset++;
					
					//assign the value of temporary taste value
					for(int cnt = 0; cnt < tmpTasteBytes.length; cnt++){
						body[offset + cnt] = tmpTasteBytes[cnt];
					}
					offset += tmpTasteBytes.length;
					
					//assign the alias id to temporary taste
					body[offset] = (byte)(order.foods[i].tmpTaste.aliasID & 0x00FF);
					body[offset + 1] = (byte)((order.foods[i].tmpTaste.aliasID & 0xFF00) >> 8);
					offset += 2;
					
					//assign the price to temporary taste
					body[offset] = (byte)(order.foods[i].tmpTaste.price & 0x000000FF);
					body[offset + 1] = (byte)((order.foods[i].tmpTaste.price & 0x0000FF00) >> 8);
					body[offset + 2] = (byte)((order.foods[i].tmpTaste.price & 0x00FF0000) >> 16);
					body[offset + 3] = (byte)((order.foods[i].tmpTaste.price & 0xFF000000) >> 24);
					
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
				body[offset] = (byte)(order.foods[i].orderDate & 0x00000000000000FF);
				body[offset + 1] = (byte)((order.foods[i].orderDate & 0x000000000000FF00L) >> 8);
				body[offset + 2] = (byte)((order.foods[i].orderDate & 0x0000000000FF0000L) >> 16);
				body[offset + 3] = (byte)((order.foods[i].orderDate & 0x00000000FF000000L) >> 24);
				body[offset + 4] = (byte)((order.foods[i].orderDate & 0x000000FF00000000L) >> 32);
				body[offset + 5] = (byte)((order.foods[i].orderDate & 0x0000FF0000000000L) >> 40);
				body[offset + 6] = (byte)((order.foods[i].orderDate & 0x00FF000000000000L) >> 48);
				body[offset + 7] = (byte)((order.foods[i].orderDate & 0xFF00000000000000L) >> 56);
				offset += 8;
				
				if(order.foods[i].waiter != null){
					byte[] waiterBytes = order.foods[i].waiter.getBytes("UTF-8");
					//assign the length of waiter
					body[offset] = (byte)(waiterBytes.length);
					offset++;
					//assign the value of waiter
					for(int cnt = 0; cnt < waiterBytes.length; cnt++){
						body[offset + cnt] = waiterBytes[cnt];
					}
					offset += waiterBytes.length;
				}else{
					//assign the length of waiter to zero
					body[offset] = 0x00;
					offset++;
				}
			}
		}
	}

}
