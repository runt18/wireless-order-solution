package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

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
	* <Body>
	* table[2] : total_price[4] : pay_type : discount_type : len_member : member_id[len]
	* table[2] - 2-byte indicates the table id
	* total_price[4] - 4-byte indicates the total price
	* 				   total_price[0] indicates the float part
	* 				   total_price[1..3] indicates the fixed part
	* pay_type - one of the values of pay type
	* discount_type - one of the values of discount type
	* pay_manner - one of the values of pay manner
	* len_member - length of the id to member
	* member_id[len] - the id to member
	*******************************************************/
	public ReqPayOrder(short tableID, Order order, byte printType){
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.PAY_ORDER;
		header.reserved = printType;
		
		byte[] memberIDBytes = new byte[0];
		if(order.memberID != null){
			try{
				memberIDBytes = order.memberID.getBytes("UTF-8");
			}catch(UnsupportedEncodingException e){}
		}
		
		int bodyLen = 2 + /* table id takes up 2 bytes */
					  4 + /* actual total price takes up 4 bytes */
					  1 + /* pay type takes up 1 byte */
					  1 + /* discount type takes up 1 byte */
					  1 + /* the pay manner takes up 1 byte */
					  1 + /* the length of member id takes up 1 byte */
					  memberIDBytes.length; /* the member id takes up length bytes */
		
		//assign the length of the body
		header.length[0] = (byte)(bodyLen & 0x000000FF);
		header.length[1] = (byte)((bodyLen >> 8) & 0x000000FF);
		
		body = new byte[bodyLen];
		//assign the table id
		body[0] = (byte)(tableID & 0x00FF);
		body[1] = (byte)((tableID >> 8) & 0x00FF);
		//assign the total price
		body[2] = (byte)(order.totalPrice & 0x000000FF);
		body[3] = (byte)((order.totalPrice >> 8) & 0x000000FF);
		body[4] = (byte)((order.totalPrice >> 16) & 0x000000FF);
		body[5] = (byte)((order.totalPrice >> 24) & 0x000000FF);
		//assign the payment type
		body[6] = (byte)(order.payType & 0x000000FF);
		//assign the discount type
		body[7] = (byte)(order.discountType & 0x000000FF);
		//assign the payment manner
		body[8] = (byte)(order.payManner & 0x000000FF);
		//assign the length of the member id
		body[9] = (byte)(memberIDBytes.length & 0x000000FF);
		//assign the value of the member id
		System.arraycopy(memberIDBytes, 0, body, 10, memberIDBytes.length);
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
