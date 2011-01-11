package com.wireless.server;

import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;

class OrderReqParser {
	/******************************************************
	* Design the query order request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode - ORDER_BUSSINESS
	* type - QUERY_ORDER
	* seq - auto calculated and filled in
	* reserved - 0x00
	* pin[6] - auto calculated and filled in
	* len[2] - 0x02, 0x00
	* <Body>
	* table[2]
	* table[2] - 2-byte indicates the table id
	*******************************************************/
	static short parseQueryOrder(ProtocolPackage req){
		//return the table to query
		return (short)((req.body[0] & 0x00FF) | ((req.body[1] & 0x00FF) << 8));
	}
	
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
	 * table[2] : custom_num : food_num : <Food1> : <Food2>...
	 * table[2] - 2-byte indicates the table id  
	 * custom_num - 1-byte indicating the custom number for this table
	 * food_num - 1-byte indicating the number of foods
	 * <Food>
	 * food_id[2] : order_num[2]
	 * food_id[2] - 2-byte indicating the food's id
	 * order_num[2] - 2-byte indicating how many this foods are ordered
	 * 			   order_num[0] - 1-byte indicates the float-point
	 * 			   order_num[1] - 1-byte indicates the fixed-point
	 *******************************************************/
	static Order parseInsertOrder(ProtocolPackage req){
		Order order = new Order();
		//assign the table id
		order.tableID = (short)((req.body[0] & 0x00FF) | 
							((req.body[1] & 0x00FF) << 8));
		
		//assign the number of customs
		order.customNum = (byte)(req.body[2] & 0x000000FF);
		
		//assign the number of foods
		int foodNum = (byte)(req.body[3] & 0x000000FF);
		
		Food[] orderFoods = new Food[foodNum];
		//table id(2-byte) + custom_num(1-byte) + food_num(1-byte)
		int index = 4;
		//assign each order food's information, including the food's id and order number
		for(int i = 0; i < orderFoods.length; i++){
			int foodID = (req.body[index] & 0x000000FF) | ((req.body[index + 1] & 0x000000FF) << 8);
			int orderNum = (req.body[index + 2] & 0x000000FF) | ((req.body[index + 3] & 0x000000FF) << 8);
			orderFoods[i] = new Food();
			orderFoods[i].alias_id = foodID;
			orderFoods[i].setCount(orderNum);
			index += 4;
		}
		order.foods = orderFoods;
		return order;
	}
	
	/******************************************************
	* Design the cancel order request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode - ORDER_BUSSINESS
	* type - CANCEL_ORDER
	* seq - auto calculated and filled in
	* reserved - 0x00
	* pin[6] - auto calculated and filled in
	* len[2] - 0x02, 0x00
	* <Table>
	* table[2]
	* table[2] - 2-byte indicates the table id
	*******************************************************/
	static short parseCancelOrder(ProtocolPackage req){
		//return the table to cancel
		return (short)((req.body[0] & 0x00FF) | ((req.body[1] & 0x00FF) << 8));
	}
	
	/******************************************************
	* Design the pay order request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode - ORDER_BUSSINESS
	* type - CANCEL_ORDER
	* seq - auto calculated and filled in
	* reserved - 0x00
	* pin[6] - auto calculated and filled in
	* len[2] - 0x06, 0x00
	* <Table>
	* table[2] : total_price[4]
	* table[2] - 2-byte indicates the table id
	* tatal_price[4] - 4-byte indicates the total price
	* 				   total_price[0] indicates the float part
	* 				   total_price[1..3] indicates the fixed part
	*******************************************************/
	static Order parsePayOrder(ProtocolPackage req){
		//return the table to pay
		short tableToPay = (short)((req.body[0] & 0x00FF) | ((req.body[1] & 0x00FF) << 8));
		int totalPrice = (req.body[2] & 0x000000FF) | 
						 ((req.body[3] & 0x000000FF) << 8) | 
						 ((req.body[4] & 0x000000FF) << 16) |
						 ((req.body[5] & 0x000000FF) << 24);
		
		Order orderToPay = new Order();
		orderToPay.tableID = tableToPay;
		orderToPay.setTotalPrice(totalPrice);
		return orderToPay;
	}
}


