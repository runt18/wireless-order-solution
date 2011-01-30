package com.wireless.protocol;

public class ReqPayOrder extends ReqOrderPackage{
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
	* <Table>
	* table[2] : total_price[4]
	* table[2] - 2-byte indicates the table id
	* total_price[4] - 4-byte indicates the total price
	* 				   total_price[0] indicates the float part
	* 				   total_price[1..3] indicates the fixed part
	*******************************************************/
	public ReqPayOrder(short tableID, int totalPrice, byte printType){
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.PAY_ORDER;
		header.reserved = printType;
		header.length[0] = 0x06;
		header.length[1] = 0x00;
		body = new byte[6];
		body[0] = (byte)(tableID & 0x00FF);
		body[1] = (byte)((tableID >> 8) & 0x00FF);
		body[2] = (byte)(totalPrice & 0x000000FF);
		body[3] = (byte)((totalPrice >> 8) & 0x000000FF);
		body[4] = (byte)((totalPrice >> 16) & 0x000000FF);
		body[5] = (byte)((totalPrice >> 24) & 0x000000FF);
	} 

	/******************************************************
	 * In the case insert order successfully
	 * <Header>
	 * mode : type : seq : reserved : pin[6] : len[2]
	 * mode - ORDER_BUSSINESS
	 * type - ACK
	 * seq - same as request
	 * reserved - 0x00
	 * pin[6] - same as request
	 * len[2] - 0x00, 0x00
	 *******************************************************/
	
	/******************************************************
	 * In the case insert order not successfully
	 * <Header>
	 * mode : type : seq : reserved : pin[6] : len[2]
	 * mode - ORDER_BUSSINESS
	 * type - NAK
	 * seq - same as request
	 * reserved - 0x00
	 * pin[6] - same as request
	 * len[2] - 0x00, 0x00
	 *******************************************************/
}
