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
 * table[2] : table_2[2] : minimum_cost[4] : category : custom_num : price[4] : food_num : <Food1> : <Food2>...
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
 * food_id[2] : order_num[2] : status : taste_id : taste_id[2] : taste_id2[2] : taste_id3[2]
 * food_id[2] - 2-byte indicating the food's id
 * order_num[2] - 2-byte indicating how many this foods are ordered
 * 			   order_num[0] - 1-byte indicates the float-point
 * 			   order_num[1] - 1-byte indicates the fixed-point
 * status - the status to this food
 * taste_id[2] - 2-byte indicates the 1st taste preference id
 * taste_id2[2] - 2-byte indicates the 2nd taste preference id
 * taste_id3[3] - 2-byte indicates the 3rd taste preference id
 *******************************************************/
public class RespQueryOrder extends RespPackage{

	public RespQueryOrder(ProtocolHeader reqHeader, Order order) throws UnsupportedEncodingException {
		super(reqHeader);
		header.type = Type.ACK;
		//calculate the body's length
		int bodyLen = 2 + /* table id takes up 2-byte */
					2 + /* 2nd table id takes up 2-byte */
					4 + /* minimum cost takes up 4-byte */
					1 + /* category takes up 1-byte */
					1 + /* custom number takes up 1-byte */ 
					4 + /* price takes up 4-byte */ 
					1 + /* food number takes up 1-byte */
					order.foods.length * 11; /* each food takes up 11-byte*/
		
		//assign the body length to header's length field
		header.length[0] = (byte)(bodyLen & 0x000000FF);
		header.length[1] = (byte)((bodyLen & 0x0000FF00) >> 8);
		
		//allocate the memory for body
		body = new byte[bodyLen];
		
		//assign the table id
		body[0] = (byte)(order.table_id & 0x00FF);
		body[1] = (byte)((order.table_id & 0xFF00) >> 8);
		
		//assign the 2nd table id
		body[2] = (byte)(order.table2_id & 0x00FF);
		body[3] = (byte)((order.table2_id & 0xFF00) >> 8);
		
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
		
		//assign each food information, including food'id and order number
		int index = 15;
		for(int i = 0; i < order.foods.length; i++){
			body[index] = (byte)(order.foods[i].alias_id & 0x000000FF);
			body[index + 1] = (byte)((order.foods[i].alias_id & 0x0000FF00) >> 8);
			body[index + 2] = (byte)(order.foods[i].count & 0x000000FF);
			body[index + 3] = (byte)((order.foods[i].count & 0x0000FF00) >> 8);
			body[index + 4] = (byte)(order.foods[i].status);
			body[index + 5] = (byte)(order.foods[i].tastes[0].alias_id & 0x00FF);
			body[index + 6] = (byte)((order.foods[i].tastes[0].alias_id & 0xFF00) >> 8);
			body[index + 7] = (byte)(order.foods[i].tastes[1].alias_id & 0x00FF);
			body[index + 8] = (byte)((order.foods[i].tastes[1].alias_id & 0xFF00) >> 8);
			body[index + 9] = (byte)(order.foods[i].tastes[2].alias_id & 0x00FF);
			body[index + 10] = (byte)((order.foods[i].tastes[2].alias_id & 0xFF00) >> 8);
			index += 11;
		}
	}

}
