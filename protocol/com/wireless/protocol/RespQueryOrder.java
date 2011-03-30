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
 * table[2] : custom_num : price[4] : food_num : <Food1> : <Food2>...
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
public class RespQueryOrder extends RespPackage{

	public RespQueryOrder(ProtocolHeader reqHeader, Order order) throws UnsupportedEncodingException {
		super(reqHeader);
		header.type = Type.ACK;
		//calculate the body's length
		int bodyLen = 2 + /* table id takes up 2-byte */
					1 + /* custom number takes up 1-byte */ 
					4 + /* price takes up 4-byte */ 
					1 + /* food number takes up 1-byte */
					order.foods.length * 5; /* each food takes up 5-byte*/
		
		//assign the body length to header's length field
		header.length[0] = (byte)(bodyLen & 0x000000FF);
		header.length[1] = (byte)((bodyLen & 0x0000FF00) >> 8);
		
		//allocate the memory for body
		body = new byte[bodyLen];
		
		//assign the table id
		body[0] = (byte)(order.tableID & 0x00FF);
		body[1] = (byte)((order.tableID & 0xFF00) >> 8);
		
		//assign the custom number
		body[2] = (byte)(order.customNum & 0x000000FF);
		
		//assign the total price
		body[3] = (byte)(order.getTotalPrice() & 0x000000FF);
		body[4] = (byte)((order.getTotalPrice() & 0x0000FF00) >> 8);
		body[5] = (byte)((order.getTotalPrice() & 0x00FF0000) >> 16);
		body[6] = (byte)((order.getTotalPrice() & 0xFF000000) >> 24);
		
		//assign the food number
		body[7] = (byte)(order.foods.length & 0x000000FF);
		
		//assign each food information, including food'id and order number
		int index = 8;
		for(int i = 0; i < order.foods.length; i++){
			body[index] = (byte)(order.foods[i].alias_id & 0x000000FF);
			body[index + 1] = (byte)((order.foods[i].alias_id & 0x0000FF00) >> 8);
			body[index + 2] = (byte)(order.foods[i].count & 0x000000FF);
			body[index + 3] = (byte)((order.foods[i].count & 0x0000FF00) >> 8);
			body[index + 4] = (byte)(order.foods[i].taste.alias_id & 0x00FF);
			index += 5;
		}
	}

}
