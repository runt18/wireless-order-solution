package com.wireless.protocol;

import com.wireless.pack.ProtocolPackage;


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
		tables[0].mAliasId = ((req.body[0] & 0x000000FF) | ((req.body[1] & 0x000000FF) << 8));
		tables[1].mAliasId = ((req.body[2] & 0x000000FF) | ((req.body[3] & 0x000000FF) << 8));
		return tables;		
	}
	
	/******************************************************
	* Design the query associated food request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode : ORDER_BUSSINESS
	* type : QUERY_FOOD_ASSOCIATION
	* seq : auto calculated and filled in
	* reserved : 0x00
	* pin[6] : auto calculated and filled in
	* len[2] - 0x02, 0x00
	* <Food>
	* food_alias[2]
	* food_alias[2] - 2-byte indicating the alias to food
	*******************************************************/
	public static Food parseQueryFoodAssociation(ProtocolPackage req){
		int foodAlias = ((req.body[0] & 0x000000FF) | ((req.body[1] & 0x000000FF) << 8));
		return new Food(0, foodAlias, 0);
	}
}


