package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPackage;

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
	* print_type[4] : order_id[4] : table[2] : custom_num : cash_income[4] : pay_type : discount_id[4] : price_plan_id[4] : erase_price[4] : 
	* pay_manner : service_rate : len_member : member_id[len] : len_comment : comment[len]
	* print_type[4] - 4-byte indicates the print type
	* order_id[4] - 4-byte indicates the order id
	* table[2] - 2-byte indicates the table id
	* custom_num - the custom number
	* cash_income[4] - 4-byte indicates the total price
	* pay_type - one of the values of pay type
	* discount_id[4] - 4-byte indicates the id to discount
	* price_plan_id[4] - 4-byte indicates the id to price plan
	* erase_price[4] - 4-byte indicates the erase price
	* pay_manner - one of the values of pay manner
	* service_rate - the service rate to this order
	* len_member - length of the id to member
	* member_id[len] - the id to member
	* len_comment - length of the comment 
	* comment[len] - the comment this order
	*******************************************************/
	public ReqPayOrder(Order order, int printType){
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.PAY_ORDER;
		
		byte[] bytesToMemberId = new byte[0];
		if(order.memberID != null){
			try{
				bytesToMemberId = order.memberID.getBytes("UTF-8");
			}catch(UnsupportedEncodingException e){}
		}
		
		byte[] bytesToComment = new byte[0];
		if(order.comment != null){
			try{
				bytesToComment = order.comment.getBytes("UTF-8");
			}catch(UnsupportedEncodingException e){}
		}
		
		int bodyLen = 4 + /* print type takes up 4 bytes */
					  4 + /* order id takes up 4 bytes */
					  2 + /* table id takes up 2 bytes */
					  1 + /* the custom number takes up 1 byte */
					  4 + /* actual total price takes up 4 bytes */
					  4 + /* gift price takes up 4 bytes */
					  1 + /* pay type takes up 1 byte */
					  4 + /* discount id takes up 4 bytes */
					  4 + /* price plan id takes up 4 bytes */
					  4 + /* erase price takes up 4 bytes */
					  1 + /* the pay manner takes up 1 byte */
					  1 + /* the service rate takes up 1 byte */
					  1 + /* the length of member id takes up 1 byte */
					  bytesToMemberId.length + /* the member id takes up length bytes */
					  1 + /* the length of comment takes up 1 byte */
					  bytesToComment.length;	 /* the comment takes up length bytes */
		
		
		//assign the length of the body
		header.length[0] = (byte)(bodyLen & 0x000000FF);
		header.length[1] = (byte)((bodyLen >> 8) & 0x000000FF);
		
		int offset = 0;
		body = new byte[bodyLen];
		//assign the print type
		body[offset] = (byte)(printType & 0x000000FF);
		body[offset + 1] = (byte)((printType & 0x0000FF00) >> 8);
		body[offset + 2] = (byte)((printType & 0x00FF0000) >> 16);
		body[offset + 3] = (byte)((printType & 0xFF000000) >> 24);
		offset += 4;
		
		//assign the order id
		body[offset] = (byte)(order.mId & 0x000000FF);
		body[offset + 1] = (byte)((order.mId & 0x0000FF00) >> 8);
		body[offset + 2] = (byte)((order.mId & 0x00FF0000) >> 16);
		body[offset + 3] = (byte)((order.mId & 0xFF000000) >> 24);
		offset += 4;
		
		//assign the table id
		body[offset] = (byte)(order.destTbl.mAliasId & 0x00FF);
		body[offset + 1] = (byte)((order.destTbl.mAliasId >> 8) & 0x00FF);
		offset += 2;
		
		//assign the custom number
		body[offset] = (byte)(order.mCustomNum);
		offset += 1;
		
		//assign the total price
		body[offset] = (byte)(order.cashIncome & 0x000000FF);
		body[offset + 1] = (byte)((order.cashIncome >> 8) & 0x000000FF);
		body[offset + 2] = (byte)((order.cashIncome >> 16) & 0x000000FF);
		body[offset + 3] = (byte)((order.cashIncome >> 24) & 0x000000FF);
		offset += 4;
		
		//assign the payment type
		body[offset] = (byte)(order.payType & 0x000000FF);
		offset += 1;
		
		//assign the discount type
		body[offset] = (byte)(order.mDiscount.mDiscountId & 0x000000FF);
		body[offset + 1] = (byte)((order.mDiscount.mDiscountId >> 8) & 0x000000FF);
		body[offset + 2] = (byte)((order.mDiscount.mDiscountId >> 16) & 0x000000FF);
		body[offset + 3] = (byte)((order.mDiscount.mDiscountId >> 24) & 0x000000FF);
		offset += 4;
		
		//assign the price plan id		
		int pricePlanId;
		if(order.hasPricePlan()){
			pricePlanId = order.getPricePlan().getId();
		}else{
			pricePlanId = PricePlan.INVALID_PRICE_PLAN;
		}
		body[offset] = (byte)(pricePlanId & 0x000000FF);
		body[offset + 1] = (byte)((pricePlanId >> 8) & 0x000000FF);
		body[offset + 2] = (byte)((pricePlanId >> 16) & 0x000000FF);
		body[offset + 3] = (byte)((pricePlanId >> 24) & 0x000000FF);
		offset += 4;
		
		//assign the erase price
		body[offset] = (byte)(order.mErasePrice & 0x000000FF);
		body[offset + 1] = (byte)((order.mErasePrice >> 8) & 0x000000FF);
		body[offset + 2] = (byte)((order.mErasePrice >> 16) & 0x000000FF);
		body[offset + 3] = (byte)((order.mErasePrice >> 24) & 0x000000FF);
		offset += 4;
		
		//assign the payment manner
		body[offset] = (byte)(order.payManner & 0x000000FF);
		offset += 1;
		
		//assign the service rate
		body[offset] = (byte)order.mServiceRate;
		offset += 1;
		
		//assign the length of the member id
		body[offset] = (byte)(bytesToMemberId.length & 0x000000FF);
		offset += 1;
		
		//assign the value of the member id
		System.arraycopy(bytesToMemberId, 0, body, offset, bytesToMemberId.length);
		offset += bytesToMemberId.length;
		
		//assign the length of comment
		body[offset] = (byte)(bytesToComment.length & 0x000000FF);
		offset += 1;		
		
		//assign the value of comment
		System.arraycopy(bytesToComment, 0, body, offset, bytesToComment.length);
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
