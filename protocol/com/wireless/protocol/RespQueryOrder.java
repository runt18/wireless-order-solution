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
public class RespQueryOrder extends RespPackage{

	public RespQueryOrder(ProtocolHeader reqHeader, Order order) throws UnsupportedEncodingException {
		super(reqHeader);
		header.type = Type.ACK;
		
		//calculate the amount of bytes that the food list needs
		int foodLen = 0;
		for(int i = 0; i < order.foods.length; i++){
			if(order.foods[i].isTemporary){
				/* is_temp(1) : food_id[2] : order_amount[2] : unit_price[3] : hang_status : len : food_name[len] */
				foodLen += 1 + 2 + 2 + 3 + 1 + 1 + order.foods[i].name.getBytes("UTF-8").length;
			}else{
				/* is_temp(0) : food_id[2] : order_amount[2] : status : taste_id[2] : taste_id2[2] : taste_id3[2] : hang_status */
				foodLen += 13; 
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
		body[0] = (byte)(order.table.aliasID & 0x00FF);
		body[1] = (byte)((order.table.aliasID & 0xFF00) >> 8);
		
		//assign the 2nd table id
		body[2] = (byte)(order.table2.aliasID & 0x00FF);
		body[3] = (byte)((order.table2.aliasID & 0xFF00) >> 8);
		
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
				/* is_temp(0) : food_id[2] : order_amount[2] : status : taste_id[2] : taste_id2[2] : taste_id3[2] : hang_status */
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
				//assign the hang status
				body[offset + 12] = (byte)(order.foods[i].hangStatus);
				offset += 13;
			}

		}
	}

}
