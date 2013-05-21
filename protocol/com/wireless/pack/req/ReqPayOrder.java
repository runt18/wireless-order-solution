package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.dishesOrder.Order;

public class ReqPayOrder extends RequestPackage{
	
	public final static byte PAY_CATE_NORMAL = 0;
	public final static byte PAY_CATE_TEMP = 1;
	
	/******************************************************
	* Design the pay order request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode - ORDER_BUSSINESS
	* type - PAY_ORDER
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
	public ReqPayOrder(PinGen gen, Order order, byte payCate){
		
		super(gen);
		
		if(payCate == PAY_CATE_NORMAL || payCate == PAY_CATE_TEMP){
		
			header.mode = Mode.ORDER_BUSSINESS;
			header.type = Type.PAY_ORDER;
			header.reserved = payCate;
			
			fillBody(order, Order.ORDER_PARCELABLE_4_PAY);
			
		}else{
			throw new IllegalArgumentException("The pay category is incorrect.");
		}
	}
}
