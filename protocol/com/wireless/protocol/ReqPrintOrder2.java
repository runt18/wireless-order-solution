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
 * 			  [4..7] - Not Used
 * pin[6] - auto calculated and filled in
 * len[2] - length of the <Body>
 * <Body>
 * order_id[4] - 4-byte indicating the order id to print
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
	 *              [4..7] - Not Used			
	 * @param orderID
	 * 			    the order id to print
	 */
	public ReqPrintOrder2(byte printConf, int orderID){
		header.mode = Mode.PRINT;
		header.type = Type.PRINT_BILL_2;
		header.reserved = printConf;
		header.length[0] = 4;
		header.length[1] = 0;
		body = new byte[4];
		
		body[0] = (byte)(orderID & 0x000000FF);
		body[1] = (byte)((orderID & 0x0000FF00) >> 8);
		body[2] = (byte)((orderID & 0x00FF0000) >> 16);
		body[3] = (byte)((orderID & 0xFF000000) >> 24);
	}
}
