package com.wireless.protocol;

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
 * print_type[2] : order_id[4] : ori_tbl[2] : new_tbl[2] 
 * print_type[2] - 2-byte indicates the print type
 * order_id[4] - 4-byte indicating the order id to print
 * ori_tbl[2] - 2-byte indicating the original table id
 * new_tbl[2] - 2-byte indicating the new table id
 *******************************************************/
public class ReqPrintOrder2 extends ReqPackage{
	
	/**
	 * Construct the print protocol.
	 * @param printConf
	 * 				the configuration parameter to the print, the meaning to each bit is as below.<br>
	 * 				[0] - Not Used<br>
	 *              [1] - PRINT_SYNC<br>
	 *              [2] - PRINT_ORDER_2<br>
	 *              [3] - PRINT_ORDER_DETAIL_2<br>
	 *              [4] - PRINT_RECEIPT_2<br>
	 *              [5] - PRINT_EXTRA_FOOD_2<br>
	 *              [6] - PRINT_CANCELLED_FOOD_2<br>
	 *              [7] - PRINT_TRANSFER_TABLE_2<br>
	 *              [8] - PRINT_ALL_EXTRA_FOOD_2<br>
	 *              [9] - PRINT_ALL_CANCELLED_FOOD_2<br>
	 *              [10] - PRINT_SHIFT_RECEIPT_2<br>
	 *              [11] - PRINT_TEMP_RECEIPT_2<br>	
	 * @param printConf
	 * 				the print configuration parameter             		
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
		
		int bodyLen = 2 + /* print_type takes up 2 bytes */
					  4 + /* order id takes up 4 bytes */
					  2 + /* original table id takes up 2 bytes */
					  2 ; /* new table id takes up 2 bytes */
		
		//assign the length of the body
		header.length[0] = (byte)(bodyLen & 0x000000FF);
		header.length[1] = (byte)((bodyLen >> 8) & 0x000000FF);
		
		body = new byte[bodyLen];
		
		//assign the print type
		body[0] = (byte)(printConf & 0x00FF);
		body[1] = (byte)((printConf & 0xFF00) >> 8);
		
		//assign the order id
		body[2] = (byte)(orderID & 0x000000FF);
		body[3] = (byte)((orderID & 0x0000FF00) >> 8);
		body[4] = (byte)((orderID & 0x00FF0000) >> 16);
		body[5] = (byte)((orderID & 0xFF000000) >> 24);
		
		//assign the original table id
		body[6] = (byte)(oriTbl & 0x000000FF);
		body[7] = (byte)((oriTbl & 0x0000FF00) >> 8);
		
		//assign the new table id
		body[8] = (byte)(newTbl & 0x000000FF);
		body[9] = (byte)((newTbl & 0x0000FF00) >> 8);
	}
}
