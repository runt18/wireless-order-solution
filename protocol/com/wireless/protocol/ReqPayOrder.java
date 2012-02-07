package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

public class ReqPayOrder extends ReqPackage{
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
	public ReqPayOrder(Order order, short printType){
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.PAY_ORDER;
		
		byte[] memberIDBytes = new byte[0];
		if(order.memberID != null){
			try{
				memberIDBytes = order.memberID.getBytes("UTF-8");
			}catch(UnsupportedEncodingException e){}
		}
		
		byte[] commentBytes = new byte[0];
		if(order.comment != null){
			try{
				commentBytes = order.comment.getBytes("UTF-8");
			}catch(UnsupportedEncodingException e){}
		}
		
		int bodyLen = 2 + /* print type takes up 2 bytes */
					  2 + /* table id takes up 2 bytes */
					  4 + /* actual total price takes up 4 bytes */
					  4 + /* gift price takes up 4 bytes */
					  1 + /* pay type takes up 1 byte */
					  1 + /* discount type takes up 1 byte */
					  1 + /* the pay manner takes up 1 byte */
					  1 + /* the service rate takes up 1 byte */
					  1 + /* the length of member id takes up 1 byte */
					  memberIDBytes.length + /* the member id takes up length bytes */
					  1 + /* the length of comment takes up 1 byte */
					  commentBytes.length;	 /* the comment takes up length bytes */
		
		//assign the length of the body
		header.length[0] = (byte)(bodyLen & 0x000000FF);
		header.length[1] = (byte)((bodyLen >> 8) & 0x000000FF);
		
		body = new byte[bodyLen];
		//assign the print type
		body[0] = (byte)(printType & 0x00FF);
		body[1] = (byte)((printType & 0xFF00) >> 8);
		
		//assign the table id
		body[2] = (byte)(order.table_id & 0x00FF);
		body[3] = (byte)((order.table_id >> 8) & 0x00FF);
		
		//assign the total price
		body[4] = (byte)(order.cashIncome & 0x000000FF);
		body[5] = (byte)((order.cashIncome >> 8) & 0x000000FF);
		body[6] = (byte)((order.cashIncome >> 16) & 0x000000FF);
		body[7] = (byte)((order.cashIncome >> 24) & 0x000000FF);
		
		//assign the gift price
		body[8] = (byte)(order.giftPrice & 0x000000FF);
		body[9] = (byte)((order.giftPrice >> 8) & 0x000000FF);
		body[10] = (byte)((order.giftPrice >> 16) & 0x000000FF);
		body[11] = (byte)((order.giftPrice >> 24) & 0x000000FF);
		
		//assign the payment type
		body[12] = (byte)(order.pay_type & 0x000000FF);
		
		//assign the discount type
		body[13] = (byte)(order.discount_type & 0x000000FF);
		
		//assign the payment manner
		body[14] = (byte)(order.pay_manner & 0x000000FF);
		
		//assign the service rate
		body[15] = order.service_rate;
		
		//assign the length of the member id
		body[16] = (byte)(memberIDBytes.length & 0x000000FF);
		
		//assign the value of the member id
		System.arraycopy(memberIDBytes, 0, body, 17, memberIDBytes.length);
		
		//assign the length of comment
		body[17 + memberIDBytes.length] = (byte)(commentBytes.length & 0x000000FF);
		
		//assign the value of comment
		System.arraycopy(commentBytes, 0, body, 18 + memberIDBytes.length, commentBytes.length);
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
