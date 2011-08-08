package com.wireless.protocol;

/******************************************************
 * Design the print order 2 request looks like below
 * <Header>
 * mode : type : seq : reserved : pin[6] : len[2] : print_content
 * mode - PRINT
 * type - PRINT_BILL_2
 * seq - auto calculated and filled in
 * reserved - the meaning to each bit is as below
 * 			  [0] - PRINT_SYNC
 * 		      [1] - PRINT_ORDER_2
 * 			  [2] - PRINT_ORDER_DETAIL_2
 * 			  [3] - PRINT_RECEIPT_2
 *            [4] - PRINT_EXTRA_FOOD_2<br>
 *            [5] - PRINT_CANCELLED_FOOD_2<br>
 *            [6] - PRINT_TRANSFER_TABLE_2<br>
 *            [7] - PRINT_TEMP_RECEIPT_2<br>	
 * pin[6] - auto calculated and filled in
 * len[2] - length of the <Body>
 * <Body>
 * order_id[4] : ori_tbl[2] : new_tbl[2] 
 * order_id[4] - 4-byte indicating the order id to print
 * ori_tbl[2] - 2-byte indicating the original table id
 * new_tbl[2] - 2-byte indicating the new table id
 *******************************************************/
public class ReqPrintOrder2 extends ReqPackage{
	
	/**
	 * Construct the print protocol.
	 * @param printConf
	 * 				the configuration parameter to the print, the meaning to each bit is as below.<br>
	 *              [0] - PRINT_SYNC<br>
	 *              [1] - PRINT_ORDER_2<br>
	 *              [2] - PRINT_ORDER_DETAIL_2<br>
	 *              [3] - PRINT_RECEIPT_2<br>
	 *              [4] - PRINT_EXTRA_FOOD_2<br>
	 *              [5] - PRINT_CANCELLED_FOOD_2<br>
	 *              [6] - PRINT_TRANSFER_TABLE_2<br>
	 *              [7] - PRINT_TEMP_RECEIPT_2<br>			
	 * @param orderID
	 * 			    the order id to print
	 * @param oriTbl
	 * 				the id to original table 
	 * @param newTbl
	 * 				the id to new table 
	 */
	public ReqPrintOrder2(short printConf, int orderID, int oriTbl, int newTbl){
		header.mode = Mode.PRINT;
		header.type = Type.PRINT_BILL_2;
		header.reserved[0] = (byte)(printConf & 0x00FF);
		header.reserved[1] = (byte)((printConf & 0xFF00) >> 8);
		header.length[0] = 8;
		header.length[1] = 0;
		
		int len = (header.length[0] & 0x000000FF) |
				  ((header.length[1] & 0x000000FF) << 8);
		body = new byte[len];
		
		//assign the order id
		body[0] = (byte)(orderID & 0x000000FF);
		body[1] = (byte)((orderID & 0x0000FF00) >> 8);
		body[2] = (byte)((orderID & 0x00FF0000) >> 16);
		body[3] = (byte)((orderID & 0xFF000000) >> 24);
		
		//assign the original table id
		body[4] = (byte)(oriTbl & 0x000000FF);
		body[5] = (byte)((oriTbl & 0x0000FF00) >> 8);
		
		//assign the new table id
		body[6] = (byte)(newTbl & 0x000000FF);
		body[7] = (byte)((newTbl & 0x0000FF00) >> 8);
	}
}
