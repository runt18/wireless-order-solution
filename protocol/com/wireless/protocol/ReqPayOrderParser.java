package com.wireless.protocol;

import java.io.UnsupportedEncodingException;

import com.wireless.pack.ProtocolPackage;

public final class ReqPayOrderParser {
	
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
	public static Order parse(ProtocolPackage req){
		
		int offset = 0;
		
		//get the print type
		int printType = ((req.body[offset] & 0x000000FF) | 
						((req.body[offset + 1] & 0x000000FF) << 8)) |
						((req.body[offset + 2] & 0x000000FF) << 16) |
						((req.body[offset + 3] & 0x000000FF) << 24);
		offset += 4;
		
		//get the order id
		int orderId = ((req.body[offset] & 0x000000FF) | 
					  ((req.body[offset + 1] & 0x000000FF) << 8)) |
					  ((req.body[offset + 2] & 0x000000FF) << 16) |
					  ((req.body[offset + 3] & 0x000000FF) << 24);
		offset += 4;
		
		//get the table id
		int tableToPay = ((req.body[offset] & 0x000000FF) | ((req.body[offset + 1] & 0x000000FF) << 8));
		offset += 2;
		
		//get the custom number
		int customNum = req.body[offset];
		offset += 1;
		
		//get the actual total price
		int cashIncome = (req.body[offset] & 0x000000FF) | 
						 ((req.body[offset + 1] & 0x000000FF) << 8) | 
						 ((req.body[offset + 2] & 0x000000FF) << 16) |
						 ((req.body[offset + 3] & 0x000000FF) << 24);
		offset += 4;
		
		//get the payment type
		int payType = req.body[offset];
		offset += 1;
		
		//get the discount id
		int discountId = (req.body[offset] & 0x000000FF) | 
 						 ((req.body[offset + 1] & 0x000000FF) << 8) | 
 						 ((req.body[offset + 2] & 0x000000FF) << 16) |
 						 ((req.body[offset + 3] & 0x000000FF) << 24);
		offset += 4;

		//get the price plan id
		int pricePlanId = (req.body[offset] & 0x000000FF) | 
				 ((req.body[offset + 1] & 0x000000FF) << 8) | 
				 ((req.body[offset + 2] & 0x000000FF) << 16) |
				 ((req.body[offset + 3] & 0x000000FF) << 24);
		offset += 4;

		
		//get the erase price
		int erasePrice = (req.body[offset] & 0x000000FF) | 
				 		 ((req.body[offset + 1] & 0x000000FF) << 8) | 
				 		 ((req.body[offset + 2] & 0x000000FF) << 16) |
				 		 ((req.body[offset + 3] & 0x000000FF) << 24);
		offset += 4;
		
		//get the payment manner
		int payManner = req.body[offset];
		offset += 1;
		
		//get the service rate
		int serviceRate = req.body[offset];
		offset += 1;
		
		//get the the length to member id
		int lenOfMember = req.body[offset];
		offset += 1;
		
		//get the value to member id
		String memberID = null;
		//get the member id if exist
		if(lenOfMember > 0){
			byte[] memberIDBytes = new byte[lenOfMember];
			System.arraycopy(req.body, offset, memberIDBytes, 0, lenOfMember);
			try{
				memberID = new String(memberIDBytes, "UTF-8");
			}catch(UnsupportedEncodingException e){}
			offset += lenOfMember;
		}
		
		//get the length of comment
		int lenOfComment = req.body[offset];
		offset += 1;
		
		String comment = null;
		//get the comment if exist
		byte[] commentBytes = new byte[lenOfComment];
		System.arraycopy(req.body, offset, commentBytes, 0, lenOfComment);
		try{
			comment = new String(commentBytes, "UTF-8");
		}catch(UnsupportedEncodingException e){}
		offset += lenOfComment;
		
		Order orderToPay = new Order();
		orderToPay.printType = printType;
		orderToPay.setId(orderId);
		orderToPay.destTbl.mAliasId = tableToPay;
		orderToPay.mCustomNum = customNum;
		orderToPay.cashIncome = cashIncome;
		orderToPay.payType = payType;
		orderToPay.mDiscount.mDiscountId = discountId;
		orderToPay.mErasePrice = erasePrice;
		orderToPay.payManner = payManner;
		orderToPay.mServiceRate = serviceRate;
		orderToPay.memberID = memberID;
		orderToPay.comment = comment;
		if(pricePlanId != PricePlan.INVALID_PRICE_PLAN){
			orderToPay.setPricePlan(new PricePlan(pricePlanId));
		}
		return orderToPay;
	}
	
}
