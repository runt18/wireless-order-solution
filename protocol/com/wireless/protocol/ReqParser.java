package com.wireless.protocol;

import java.io.UnsupportedEncodingException;


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
	 * table[2] : table_2[2] : category : custom_num : food_num : <Food1> : <Food2>... : original_table[2]
	 * table[2] - 2-byte indicates the table id
	 * table_2[2] - 2-byte indicates the 2nd table id  
	 * categroy - 1-byte indicates the category to this order  
	 * custom_num - 1-byte indicating the custom number for this table
	 * food_num - 1-byte indicating the number of foods
	 * <Food>
	 * food_id[2] : order_num[2] : taste_id : kitchen
	 * food_id[2] - 2-byte indicating the food's id
	 * order_num[2] - 2-byte indicating how many this foods are ordered
	 * 			   order_num[0] - 1-byte indicates the float-point
	 * 			   order_num[1] - 1-byte indicates the fixed-point
	 * taste_id - 1-byte indicates the taste preference id
	 * kitchen - the kitchen to this food
	 *
	 * origianal_table[2] - 2-bytes indicates the original table id,
	 *                      These two bytes are used for table transferred
	 *******************************************************/
	public static Order parseInsertOrder(ProtocolPackage req){
		Order order = new Order();
		//get the table id
		order.table_id = (short)((req.body[0] & 0x00FF) | 
							((req.body[1] & 0x00FF) << 8));
		
		//get the 2nd table id
		order.table2_id = (short)((req.body[2] & 0x00FF) | 
							((req.body[3] & 0x00FF) << 8));
		
		//get the category
		order.category = (short)(req.body[4] & 0x000000FF);
		
		//get the number of customs
		order.custom_num = (byte)(req.body[5] & 0x000000FF);
		
		//get the number of foods
		int foodNum = (byte)(req.body[6] & 0x000000FF);
		
		Food[] orderFoods = new Food[foodNum];
		//table id(2-byte) + 2nd table id(2-byte) + category(1-byte) + custom_num(1-byte) + food_num(1-byte)
		int offset = 7;
		//assign each order food's information, including the food's id and order number
		for(int i = 0; i < orderFoods.length; i++){
			int foodID = (req.body[offset] & 0x000000FF) | ((req.body[offset + 1] & 0x000000FF) << 8);
			int orderNum = (req.body[offset + 2] & 0x000000FF) | ((req.body[offset + 3] & 0x000000FF) << 8);
			short taste_id = (short)(req.body[offset + 4] & 0x00FF);
			short kitchen = req.body[offset + 5];
			orderFoods[i] = new Food();
			orderFoods[i].alias_id = foodID;
			orderFoods[i].count = orderNum;
			orderFoods[i].taste.alias_id = taste_id;
			orderFoods[i].kitchen = (short)(kitchen & 0xFF);
			offset += 6;
		}
		order.foods = orderFoods;
		
		//assign the original table id
		order.originalTableID = (short)((req.body[offset] & 0x00FF) | 
									((req.body[offset + 1] & 0x00FF) << 8));
		return order;
	}
	
	/******************************************************
	 * Design the print order request looks like below
	 * <Header>
	 * mode : type : seq : reserved : pin[6] : len[2] : print_content
	 * mode - PRINT
	 * type - PRINT_BILL
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
	 * order_id[4] - 4-byte indicating the order id to print
	 *******************************************************/
	public static int parsePrintReq(ProtocolPackage req){
		int orderID = (req.body[0] & 0x000000FF) |
	   	   			  ((req.body[1] & 0x000000FF) << 8) |
	   	   			  ((req.body[2] & 0x000000FF) << 16) |
	   	   			  ((req.body[3] & 0x000000FF) << 24); 
		return orderID;
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
	* reserved - PRINT_SYNC or PRINT_ASYNC
	* pin[6] - auto calculated and filled in
	* len[2] - 0x06, 0x00
	* <Body>
	* table[2] : cash_income[4] : pay_type : discount_type : pay_manner : service_rate : len_member : member_id[len] : len_comment : comment[len]
	* table[2] - 2-byte indicates the table id
	* cash_income[4] - 4-byte indicates the total price
	* 				   total_price[0] indicates the float part
	* 				   total_price[1..3] indicates the fixed part
	* pay_type - one of the values of pay type
	* discount_type - one of the values of discount type
	* pay_manner - one of the values of pay manner
	* service_rate - the service rate to this order
	* len_member - length of the id to member
	* member_id[len] - the id to member
	* len_comment - length of the comment 
	* comment[len] - the comment this order
	*******************************************************/
	public static Order parsePayOrder(ProtocolPackage req){
		//get the table id
		short tableToPay = (short)((req.body[0] & 0x00FF) | ((req.body[1] & 0x00FF) << 8));
		//get the actual total price
		int cashIncome = (req.body[2] & 0x000000FF) | 
						 ((req.body[3] & 0x000000FF) << 8) | 
						 ((req.body[4] & 0x000000FF) << 16) |
						 ((req.body[5] & 0x000000FF) << 24);
		
		//get the payment type
		int payType = req.body[6];
		//get the discount type
		int discountType = req.body[7];
		//get the payment manner
		int payManner = req.body[8];
		//get the service rate
		byte serviceRate = req.body[9];
		//get the the length to member id
		int lenMember = req.body[10];
		//get the value to member id
		String memberID = null;
		//get the member id if exist
		if(lenMember > 0){
			byte[] memberIDBytes = new byte[lenMember];
			System.arraycopy(req.body, 11, memberIDBytes, 0, lenMember);
			try{
				memberID = new String(memberIDBytes, "UTF-8");
			}catch(UnsupportedEncodingException e){}
		}
		//get the length of comment
		int lenComment = req.body[11 + lenMember];
		String comment = null;
		//get the comment if exist
		if(lenComment > 0){
			byte[] commentBytes = new byte[lenComment];
			System.arraycopy(req.body, 12 + lenMember, commentBytes, 0, lenComment);
			try{
				comment = new String(commentBytes, "UTF-8");
			}catch(UnsupportedEncodingException e){}
		}
		Order orderToPay = new Order();
		orderToPay.table_id = tableToPay;
		orderToPay.cashIncome = cashIncome;
		orderToPay.pay_type = payType;
		orderToPay.discount_type = discountType;
		orderToPay.pay_manner = payManner;
		orderToPay.service_rate = serviceRate;
		orderToPay.member_id = memberID;
		orderToPay.comment = comment;
		return orderToPay;
	}
}


