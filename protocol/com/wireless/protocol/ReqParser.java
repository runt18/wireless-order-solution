package com.wireless.protocol;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Comparator;

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
	public static int parseQueryOrder(ProtocolPackage req){
		//return the table to query
		return ((req.body[0] & 0x000000FF) | ((req.body[1] & 0x000000FF) << 8));
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
	 * print_type[2] : table[2] : table2[2] : category : custom_num : food_num : 
	 * <Food1> : <Food2>... :
	 * <TmpFood1> : <TmpFood2>... : 
	 * original_table[2] 
	 * 
	 * print_type[2] - 2-byte indicates the print type
	 * table[2] - 2-byte indicates the table id
	 * table_2[2] - 2-byte indicates the 2nd table id  
	 * category - 1-byte indicates the category to this order  
	 * custom_num - 1-byte indicating the custom number for this table
	 * food_num - 1-byte indicating the number of foods
	 * 
	 * <Food>
	 * is_temp(0) : food_id[2] : order_num[2] : taste_id[2] : taste_id2[2] : taste_id3[2] : kitchen : hang_status : is_hurried
	 * is_temp(0) - "0" means this food is NOT temporary
	 * food_id[2] - 2-byte indicating the food's id
	 * order_num[2] - 2-byte indicating how many this foods are ordered
	 * 			   order_num[0] - 1-byte indicates the float-point
	 * 			   order_num[1] - 1-byte indicates the fixed-point
	 * taste_id[2] - 2-byte indicates the 1st taste preference id
	 * taste_id2[2] - 2-byte indicates the 2nd taste preference id
	 * taste_id3[2] - 2-byte indicates the 3rd taste preference id
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
	 * origianal_table[2] - 2-bytes indicates the original table id,
	 *                      These two bytes are used for table transferred
	 *******************************************************/
	public static Order parseInsertOrder(ProtocolPackage req){
		Order order = new Order();
		
		//get the print type
		order.print_type = ((req.body[0] & 0x000000FF) | ((req.body[1] & 0x000000FF) << 8));
		
		//get the table id
		order.table.aliasID = ((req.body[2] & 0x000000FF) | ((req.body[3] & 0x000000FF) << 8));
		
		//get the 2nd table id
		order.table2.aliasID = ((req.body[4] & 0x000000FF) | ((req.body[5] & 0x000000FF) << 8));
		
		//get the category
		order.category = (short)(req.body[6] & 0x000000FF);
		
		//get the number of customs
		order.custom_num = (byte)(req.body[7] & 0x000000FF);
		
		//get the number of foods
		int foodNum = (byte)(req.body[8] & 0x000000FF);
		
		OrderFood[] orderFoods = new OrderFood[foodNum];
		//table id(2-byte) + 2nd table id(2-byte) + category(1-byte) + custom_num(1-byte) + food_num(1-byte)
		int offset = 9;
		//assign each order food's information, including the food's id and order number
		for(int i = 0; i < orderFoods.length; i++){
			boolean isTemporary = req.body[offset] == 1 ? true : false;
			
			if(isTemporary){
				/**
				 * is_temp(1) : food_id[2] : order_amount[2] : unit_price[3] : hang_status : is_hurried : len : food_name[len] 
				 */
				//get the food id
				int foodID = (req.body[offset + 1] & 0x000000FF) | 
							 ((req.body[offset + 2] & 0x000000FF) << 8);
				
				//get the order amount
				int orderNum = (req.body[offset + 3] & 0x000000FF) | 
								((req.body[offset + 4] & 0x000000FF) << 8);
				
				//get the unit price
				int unitPrice = (req.body[offset + 5] & 0x000000FF) |
								((req.body[offset + 6] & 0x000000FF) << 8) |
								((req.body[offset + 7] & 0x000000FF) << 16);
				
				//get the hang status
				short hangStatus = req.body[offset + 8];
				
				//get the hurried flag
				boolean isHurried = req.body[offset + 9] == 1 ? true : false;
				
				//get the amount of food name bytes
				int len = req.body[offset + 10];
				
				//get the food name
				String name = null;
				try{
					name = new String(req.body, offset + 11, len, "UTF-8");
				}catch(UnsupportedEncodingException e){}
				
				orderFoods[i] = new OrderFood();
				orderFoods[i].isTemporary = true;
				orderFoods[i].kitchen.aliasID = Kitchen.KITCHEN_TEMP;
				orderFoods[i].aliasID = foodID;
				orderFoods[i].hangStatus = hangStatus;
				orderFoods[i].isHurried = isHurried;
				orderFoods[i].count = orderNum;
				orderFoods[i].setPrice(Util.int2Float(unitPrice));
				orderFoods[i].name = name != null ? name : "";
				
				offset += 1 + 2 + 2 + 3 + 1 + 1 + 1 + len;
				
			}else{
				/**
				 * is_temp(0) : food_id[2] : order_num[2] : taste_id[2] : taste_id2[2] : taste_id3[2] : kitchen : hang_status : is_hurried
				 */
				
				//get the food id
				int foodID = (req.body[offset + 1] & 0x000000FF) | ((req.body[offset + 2] & 0x000000FF) << 8);
				
				//get the order amount
				int orderNum = (req.body[offset + 3] & 0x000000FF) | ((req.body[offset + 4] & 0x000000FF) << 8);
				
				//get each taste id
				int[] tasteID = new int[3];
				tasteID[0] = (req.body[offset + 5] & 0x000000FF) | 
								((req.body[offset + 6] & 0x000000FF) << 8);
				tasteID[1] = (req.body[offset + 7] & 0x000000FF) | 
								((req.body[offset + 8] & 0x000000FF) << 8);
				tasteID[2] = (req.body[offset + 9] & 0x000000FF) | 
								((req.body[offset + 10] & 0x000000FF) << 8);
				
				//get the kitchen 
				short kitchen = req.body[offset + 11];
				
				//get the hang up status
				short hangStatus = req.body[offset + 12];
				
				//get the hurried flag
				boolean isHurried = req.body[offset + 13] == 1 ? true : false;
				
				offset += 14;
				
				orderFoods[i] = new OrderFood();
				orderFoods[i].aliasID = foodID;
				orderFoods[i].count = orderNum;
				
				orderFoods[i].isTemporary = false;
				
				orderFoods[i].tastes[0].aliasID = tasteID[0];
				orderFoods[i].tastes[1].aliasID = tasteID[1];
				orderFoods[i].tastes[2].aliasID = tasteID[2];
				
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
		
		//assign the original table id
		order.oriTbl.aliasID = ((req.body[offset] & 0x000000FF) | 
									((req.body[offset + 1] & 0x000000FF) << 8));
		return order;
	}
	
	/******************************************************
	 * Design the print order 2 request looks like below
	 * <Header>
	 * mode : type : seq : reserved : pin[6] : len[2] : print_content
	 * mode - PRINT
	 * type - PRINT_BILL_2
	 * seq - auto calculated and filled in
	 * reserved - 0x00	
	 * pin[6] - auto calculated and filled in
	 * len[2] - length of the <Body>
	 * <Body>
	 * print_type[2] : order_id[4] : ori_tbl[2] : new_tbl[2] : on_duty[8] : off_duty[8]
	 * print_type[2] - 2-byte indicates the print type
	 * order_id[4] - 4-byte indicating the order id to print
	 * ori_tbl[2] - 2-byte indicating the original table id
	 * new_tbl[2] - 2-byte indicating the new table id
	 * on_duty[8] - 8-byte indicating the on duty
	 * off_duty[8] - 8-byte indicating the off duty
	 *******************************************************/
	public static ReqPrintOrder2.ReqParam parsePrintReq(ProtocolPackage req){
		//get the print type
		int printConf = (req.body[0] & 0x000000FF) | ((req.body[1] & 0x000000FF) << 8);
		
		//get the order id
		int orderID = (req.body[2] & 0x000000FF) |
	   	   			  ((req.body[3] & 0x000000FF) << 8) |
	   	   			  ((req.body[4] & 0x000000FF) << 16) |
	   	   			  ((req.body[5] & 0x000000FF) << 24); 
		
		//get the original table id
		int oriTblID = (req.body[6] & 0x000000FF) |
					   ((req.body[7] & 0x000000FF) << 8);
		
		//get the new table id
		int newTblID = (req.body[8] & 0x000000FF) |
		   			   ((req.body[9] & 0x000000FF) << 8);
		
		//get the on duty
		long onDuty = (req.body[10] & 0x00000000000000FFL) |
					  ((req.body[11] & 0x00000000000000FFL) << 8) |
					  ((req.body[12] & 0x00000000000000FFL) << 16) |
					  ((req.body[13] & 0x00000000000000FFL) << 24) |
					  ((req.body[14] & 0x00000000000000FFL) << 32) |
					  ((req.body[15] & 0x00000000000000FFL) << 40) |
					  ((req.body[16] & 0x00000000000000FFL) << 48) |
					  ((req.body[17] & 0x00000000000000FFL) << 56);

		//get the off duty
		long offDuty = (req.body[18] & 0x00000000000000FFL) |
					  ((req.body[19] & 0x00000000000000FFL) << 8) |
					  ((req.body[20] & 0x00000000000000FFL) << 16) |
					  ((req.body[21] & 0x00000000000000FFL) << 24) |
					  ((req.body[22] & 0x00000000000000FFL) << 32) |
					  ((req.body[23] & 0x00000000000000FFL) << 40) |
					  ((req.body[24] & 0x00000000000000FFL) << 48) |
					  ((req.body[25] & 0x00000000000000FFL) << 56);


		ReqPrintOrder2.ReqParam reqParam = new ReqPrintOrder2.ReqParam();
		reqParam.printConf = printConf;
		reqParam.orderID = orderID;
		reqParam.oriTblID = oriTblID;
		reqParam.newTblID = newTblID;
		reqParam.onDuty = onDuty;
		reqParam.offDuty = offDuty;
		
		return reqParam;
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
	public static int parseCancelOrder(ProtocolPackage req){
		//return the table to cancel
		return ((req.body[0] & 0x000000FF) | ((req.body[1] & 0x000000FF) << 8));
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
	* print_type[2] : table[2] : cash_income[4] : gift_price[4] : pay_type : discount_type : pay_manner : service_rate : len_member : member_id[len] : len_comment : comment[len]
	* print_type[2] - 2-byte indicates the print type
	* table[2] - 2-byte indicates the table id
	* cash_income[4] - 4-byte indicates the total price
	* gift_price[4] - 4-byte indicates the gift price
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
		
		//get the print type
		int printType = ((req.body[0] & 0x000000FF) | ((req.body[1] & 0x000000FF) << 8));
		
		//get the table id
		int tableToPay = ((req.body[2] & 0x000000FF) | ((req.body[3] & 0x000000FF) << 8));
		//get the actual total price
		int cashIncome = (req.body[4] & 0x000000FF) | 
						 ((req.body[5] & 0x000000FF) << 8) | 
						 ((req.body[6] & 0x000000FF) << 16) |
						 ((req.body[7] & 0x000000FF) << 24);
		//get the gift price
		int giftPrice = (req.body[8] & 0x000000FF) | 
		 				((req.body[9] & 0x000000FF) << 8) | 
		 				((req.body[10] & 0x000000FF) << 16) |
		 				((req.body[11] & 0x000000FF) << 24);
		//get the payment type
		int payType = req.body[12];
		//get the discount type
		int discountType = req.body[13];
		//get the payment manner
		int payManner = req.body[14];
		//get the service rate
		int serviceRate = req.body[15];
		//get the the length to member id
		int lenMember = req.body[16];
		//get the value to member id
		String memberID = null;
		//get the member id if exist
		if(lenMember > 0){
			byte[] memberIDBytes = new byte[lenMember];
			System.arraycopy(req.body, 17, memberIDBytes, 0, lenMember);
			try{
				memberID = new String(memberIDBytes, "UTF-8");
			}catch(UnsupportedEncodingException e){}
		}
		//get the length of comment
		int lenComment = req.body[17 + lenMember];
		String comment = null;
		//get the comment if exist
		if(lenComment > 0){
			byte[] commentBytes = new byte[lenComment];
			System.arraycopy(req.body, 18 + lenMember, commentBytes, 0, lenComment);
			try{
				comment = new String(commentBytes, "UTF-8");
			}catch(UnsupportedEncodingException e){}
		}
		Order orderToPay = new Order();
		orderToPay.print_type = printType;
		orderToPay.table.aliasID = tableToPay;
		orderToPay.cashIncome = cashIncome;
		orderToPay.giftPrice = giftPrice;
		orderToPay.pay_type = payType;
		orderToPay.discount_type = discountType;
		orderToPay.pay_manner = payManner;
		orderToPay.serviceRate = serviceRate;
		orderToPay.memberID = memberID;
		orderToPay.comment = comment;
		return orderToPay;
	}
}


