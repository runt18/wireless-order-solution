package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

public final class ReqParser {
	
	/******************************************************
	* Design the query table status request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode - ORDER_BUSSINESS
	* type - QUERY_TABLE_STATUS
	* seq - auto calculated and filled in
	* reserved - 0x00
	* pin[6] - auto calculated and filled in
	* len[2] - 0x02, 0x00
	* <Body>
	* table[2]
	* table[2] - 2-byte indicates the table id
	*******************************************************/
	public static int parseQueryTblStatus(ProtocolPackage req){
		//return the table to query
		return ((req.body[0] & 0x000000FF) | ((req.body[1] & 0x000000FF) << 8));
	}
	
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
	 * print_type[4] : order_id[4] : ori_tbl[2] : new_tbl[2] : on_duty[8] : off_duty[8]
	 * print_type[4] - 4-byte indicates the print type
	 * order_id[4] - 4-byte indicating the order id to print
	 * ori_tbl[2] - 2-byte indicating the original table id
	 * new_tbl[2] - 2-byte indicating the new table id
	 * on_duty[8] - 8-byte indicating the on duty
	 * off_duty[8] - 8-byte indicating the off duty
	 *******************************************************/
	public static ReqPrintOrder2.ReqParam parsePrintReq(ProtocolPackage req){
		//get the print type
		int printConf = (req.body[0] & 0x000000FF) | 
						((req.body[1] & 0x000000FF) << 8) |
						((req.body[2] & 0x000000FF) << 16) |
						((req.body[3] & 0x000000FF) << 24);
		
		//get the order id
		int orderID = (req.body[4] & 0x000000FF) |
	   	   			  ((req.body[5] & 0x000000FF) << 8) |
	   	   			  ((req.body[6] & 0x000000FF) << 16) |
	   	   			  ((req.body[7] & 0x000000FF) << 24); 
		
		//get the original table id
		int oriTblID = (req.body[8] & 0x000000FF) |
					   ((req.body[9] & 0x000000FF) << 8);
		
		//get the new table id
		int newTblID = (req.body[10] & 0x000000FF) |
		   			   ((req.body[11] & 0x000000FF) << 8);
		
		//get the on duty
		long onDuty = (req.body[12] & 0x00000000000000FFL) |
					  ((req.body[13] & 0x00000000000000FFL) << 8) |
					  ((req.body[14] & 0x00000000000000FFL) << 16) |
					  ((req.body[15] & 0x00000000000000FFL) << 24) |
					  ((req.body[16] & 0x00000000000000FFL) << 32) |
					  ((req.body[17] & 0x00000000000000FFL) << 40) |
					  ((req.body[18] & 0x00000000000000FFL) << 48) |
					  ((req.body[19] & 0x00000000000000FFL) << 56);

		//get the off duty
		long offDuty = (req.body[20] & 0x00000000000000FFL) |
					  ((req.body[21] & 0x00000000000000FFL) << 8) |
					  ((req.body[22] & 0x00000000000000FFL) << 16) |
					  ((req.body[23] & 0x00000000000000FFL) << 24) |
					  ((req.body[24] & 0x00000000000000FFL) << 32) |
					  ((req.body[25] & 0x00000000000000FFL) << 40) |
					  ((req.body[26] & 0x00000000000000FFL) << 48) |
					  ((req.body[27] & 0x00000000000000FFL) << 56);


		ReqPrintOrder2.ReqParam reqParam = new ReqPrintOrder2.ReqParam();
		reqParam.printConf = printConf;
		reqParam.orderID = orderID;
		reqParam.srcTblID = oriTblID;
		reqParam.destTblID = newTblID;
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
	* print_type[4] : table[2] : cash_income[4] : gift_price[4] : pay_type : discount_type : pay_manner : service_rate : len_member : member_id[len] : len_comment : comment[len]
	* print_type[4] - 4-byte indicates the print type
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
		int printType = ((req.body[0] & 0x000000FF) | 
						((req.body[1] & 0x000000FF) << 8)) |
						((req.body[2] & 0x000000FF) << 16) |
						((req.body[3] & 0x000000FF) << 24);
		
		//get the table id
		int tableToPay = ((req.body[4] & 0x000000FF) | ((req.body[5] & 0x000000FF) << 8));
		//get the actual total price
		int cashIncome = (req.body[6] & 0x000000FF) | 
						 ((req.body[7] & 0x000000FF) << 8) | 
						 ((req.body[8] & 0x000000FF) << 16) |
						 ((req.body[9] & 0x000000FF) << 24);
		//get the gift price
		int giftPrice = (req.body[10] & 0x000000FF) | 
		 				((req.body[11] & 0x000000FF) << 8) | 
		 				((req.body[12] & 0x000000FF) << 16) |
		 				((req.body[13] & 0x000000FF) << 24);
		//get the payment type
		int payType = req.body[14];
		//get the discount type
		int discountType = req.body[15];
		//get the payment manner
		int payManner = req.body[16];
		//get the service rate
		int serviceRate = req.body[17];
		//get the the length to member id
		int lenMember = req.body[18];
		//get the value to member id
		String memberID = null;
		//get the member id if exist
		if(lenMember > 0){
			byte[] memberIDBytes = new byte[lenMember];
			System.arraycopy(req.body, 19, memberIDBytes, 0, lenMember);
			try{
				memberID = new String(memberIDBytes, "UTF-8");
			}catch(UnsupportedEncodingException e){}
		}
		//get the length of comment
		int lenComment = req.body[19 + lenMember];
		String comment = null;
		//get the comment if exist
		if(lenComment > 0){
			byte[] commentBytes = new byte[lenComment];
			System.arraycopy(req.body, 20 + lenMember, commentBytes, 0, lenComment);
			try{
				comment = new String(commentBytes, "UTF-8");
			}catch(UnsupportedEncodingException e){}
		}
		Order orderToPay = new Order();
		orderToPay.print_type = printType;
		orderToPay.destTbl.aliasID = tableToPay;
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
	
	/******************************************************
	* Design the table transfer request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode - ORDER_BUSSINESS
	* type - TRANS_TABLE
	* seq - auto calculated and filled in
	* reserved - 0x00
	* pin[6] - auto calculated and filled in
	* len[2] - 0x02, 0x00
	* <Table>
	* srcTbl[2] : destTbl[2]
	* srcTbl[2] - 2-byte indicating table alias to source table
	* destTbl[2] - 2-byte indicating table alias to destination table
	*******************************************************/
	public static Table[] parseTransTbl(ProtocolPackage req){
		Table[] tables = new Table[2];
		tables[0] = new Table();
		tables[1] = new Table();
		tables[0].aliasID = ((req.body[0] & 0x000000FF) | ((req.body[1] & 0x000000FF) << 8));
		tables[1].aliasID = ((req.body[2] & 0x000000FF) | ((req.body[3] & 0x000000FF) << 8));
		return tables;		
	}
}


