package com.wireless.protocol;


public class ReqParser {
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
	public static short parseQueryOrder(ProtocolPackage req){
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
	 * food_id[2] : order_num[2] : taste_id
	 * food_id[2] - 2-byte indicating the food's id
	 * order_num[2] - 2-byte indicating how many this foods are ordered
	 * 			   order_num[0] - 1-byte indicates the float-point
	 * 			   order_num[1] - 1-byte indicates the fixed-point
	 * taste_id - 1-byte indicates the taste preference id
	 *******************************************************/
	public static Order parseInsertOrder(ProtocolPackage req){
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
			short taste_id = req.body[index + 4];
			orderFoods[i] = new Food();
			orderFoods[i].alias_id = foodID;
			orderFoods[i].count = orderNum;
			orderFoods[i].taste.alias_id = taste_id;
			index += 5;
		}
		order.foods = orderFoods;
		return order;
	}
	
	/******************************************************
	 * Design the print order 2 request looks like below
	 * <Header>
	 * mode : type : seq : reserved : pin[6] : len[2]
	 * mode - PRINT
	 * type - PRINT_BILL_2
	 * seq - auto calculated and filled in
	 * reserved - the meaning to each bit is as below
	 * 			  [0] - PRINT_SYNC
	 * 		      [1] - PRINT_ORDER_2
	 * 			  [2] - PRINT_ORDER_DETAIL_2
	 * 			  [3] - PRINT_RECEIPT_2
	 * 			  [4..7] - Not Used
	 * pin[6] - auto calculated and filled in
	 * len[2] - length of the <Body>
	 * <Body>
	 * total_price[4] : restaurant[4] : table[2] : custom_num : food_num : <Food1> : <Food2>...
	 * tatal_price[4] - 4-byte indicates the total price
	 * 				   total_price[0] indicates the float part
	 * 				   total_price[1..3] indicates the fixed part
	 * restaurant[4] - 4-byte indicates the restaurant id
	 * table[2] - 2-byte indicates the table id  
	 * custom_num - 1-byte indicating the custom number for this table
	 * food_num - 1-byte indicating the number of foods
	 * <Food>
	 * order_num[2] : food_len : food[len] : taste_len : taste[len]
	 * order_num[2] - 2-byte indicating how many this foods are ordered
	 * 			   order_num[0] - 1-byte indicates the float-point
	 * 			   order_num[1] - 1-byte indicates the fixed-point
	 * food_len : 1-byte indicates the length of food name
	 * food[len] : the food name value
	 * taste_len : 1-byte indicates the length of taste preference
	 * taste[len] : the taste preference   
	 *******************************************************/
	public static Order parsePrintReq(ProtocolPackage req){
		Order order = new Order();
		
		//assign the total price
		int totalPrice = (req.body[0] & 0x000000FF) | 
		 				((req.body[1] & 0x000000FF) << 8) | 
		 				((req.body[2] & 0x000000FF) << 16) |
		 				((req.body[3] & 0x000000FF) << 24);
		order.totalPrice = totalPrice;
		
		int restaurantID = (req.body[4] & 0x000000FF) | 
						((req.body[5] & 0x000000FF) << 8) | 
						((req.body[6] & 0x000000FF) << 16) |
						((req.body[7] & 0x000000FF) << 24);
		
		//assign the table id
		order.tableID = (short)((req.body[8] & 0x00FF) | 
							((req.body[9] & 0x00FF) << 8));
		
		//assign the number of customs
		order.customNum = (byte)(req.body[10] & 0x000000FF);
		
		//assign the number of foods
		int foodNum = (byte)(req.body[11] & 0x000000FF);
		
		Food[] orderFoods = new Food[foodNum];
		//total_price(4-byte) + restaurant(4-byte) + table id(2-byte) + custom_num(1-byte) + food_num(1-byte)
		int index = 12;
		//assign each order food's information, including the food's id and order number
		for(int i = 0; i < orderFoods.length; i++){
			//get the order count
			int orderNum = (req.body[index] & 0x000000FF) | ((req.body[index + 1] & 0x000000FF) << 8);
			index += 2;
			
			//get the length of food name bytes
			int nNameBytes = req.body[index];
			index++;
			
			//get the food name bytes
			byte[] foodName = new byte[nNameBytes];
			System.arraycopy(req.body, index, foodName, 0, nNameBytes);
			index += nNameBytes;
			
			//get the length of food taste
			int nTasteBytes = req.body[index];
			index++;
			
			//get the food taste bytes
			byte[] foodTaste = new byte[nTasteBytes];
			System.arraycopy(req.body, index, foodTaste, 0, nTasteBytes);
			index += nTasteBytes;
			
			orderFoods[i].count = orderNum;
			orderFoods[i].name = new String(foodName);
			orderFoods[i].taste.preference = new String(foodTaste);
		}
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
	public static short parseCancelOrder(ProtocolPackage req){
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
	public static Order parsePayOrder(ProtocolPackage req){
		//return the table to pay
		short tableToPay = (short)((req.body[0] & 0x00FF) | ((req.body[1] & 0x00FF) << 8));
		int totalPrice = (req.body[2] & 0x000000FF) | 
						 ((req.body[3] & 0x000000FF) << 8) | 
						 ((req.body[4] & 0x000000FF) << 16) |
						 ((req.body[5] & 0x000000FF) << 24);
		
		Order orderToPay = new Order();
		orderToPay.tableID = tableToPay;
		orderToPay.totalPrice = totalPrice;
		return orderToPay;
	}
}


