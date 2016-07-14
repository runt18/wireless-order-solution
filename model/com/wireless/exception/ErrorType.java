package com.wireless.exception;

public enum ErrorType {
	/**
	 *  Code Range to each type as below.
	 * 	SYSTEM : 9900 - 9999
	 * 	FOOD : 9800 - 9899
	 * 	MEMBER : 9600 - 9799
	 * 	CLIENT : 9500 - 9599
	 * 	DISCOUNT : 9300 - 9499 
	 * 	RESTAURANT : 9200 - 9299
	 * 	FRONT_BUSINESS : 9000 - 9199
	 * 	MATERIAL : 8900 - 8999
	 * 	TASTE : 8800 - 8899
	 * 	ORDER_MGR : 
	 * 	DEPARTMENT : 8000 - 8100 
	 * 	STOCK : 7750 - 7999
	 * 	SUPPLIER : 7700 - 7749
	 *  PRINT_SCHEME : 7650 - 7699
	 *  STAFF : 7600 - 7649 
	 *  DEVICE : 7550 - 7599 
	 *  WEIXIN_FINANCE : 7500 - 7549 
	 *  WEIXIN_RESTAURANT : 7550 - 7599
	 *  WEIXIN_MEMBER : 7400 - 7449
	 *  SMS : 7450 - 7499
	 *  MODULE : 7350 - 7399
	 *  REGION : 7300 - 7349
	 *  SERVICE_RATE : 7250 - 7299
	 *  PROMOTION : 7200 - 7249
	 *  OSS_IMAGE : 7150 - 7199
	 *  PRICE_PLAN : 7100 - 7149
	 *  TABLE : 7050 - 7099
	 *  PAY_TYPE : 7000 - 7049
	 *  WX_ORDER : 6959 - 6999
	 *  token_error : 6900 - 6949
	 *  bill_board : 6859 - 6899
	 *  book : 6760 - 6799
	 *  weixin_menu : 6700 -6759 
	 */
	UNKNOWN(1, "unknown"),
	PROTOCOL(2, "protocol"),
	SYSTEM(3, "system"),
	FOOD(4, "food"),
	MEMBER(5, "member"),
	CLIENT(6, "client"),
	DISCOUNT(7, "discount"),
	RESTAURANT(8, "restaurant"),
	FRONT_BUSINESS(9, "font_business"),
	MATERIAL(10, "material"),
	TASTE(11, "taste"),
	ORDER_MGR(12, "order management"),
	DEPARTMENT(13, "department"),
	STOCK(14, "stock"),
	SUPPLIER(15, "supplier"),
	PRINT_SCHEME(16, "print scheme"),
	STAFF(17, "staff"),
	DEVICE(18, "device"),
	WX_FINANCE(19, "weixin_finance"),
	WX_RESTAURANT(20, "weixin_restaurant"),
	WX_MEMBER(21, "weixin_member"),
	SMS(22, "sms"),
	MODULE(23, "module"),
	REGION(24, "region"),
	SERVICE_RATE(25, "service_rate"),
	PROMOTION(26, "promotion"),
	OSS_IMAGE(27, "oss_image"),
	PRICE_PLAN(28, "price_plan"),
	TABLE(29, "table"),
	PAY_TYPE(30, "pay_type"),
	WX_ORDER(31, "wx_order"),
	IO_ERROR(32, "io_error"),
	TOKEN_ERROR(33, "token_error"),
	BILL_BOARD(34, "bill_board_error"),
	BOOK(35, "book_error"),
	WX_MENU(36, "weixin_menu_error"),
	DISTRIBUTION(37, "distribution_error");
	
	private final String desc;
	
	private final int val;
	
	ErrorType(int val, String desc){
		this.val = val;
		this.desc = desc;
	}
	
	@Override
	public String toString(){
		return "error type(val = " + val + ",desc = " + desc + ")";
	}
	
	public static ErrorType valueOf(int val){
		for(ErrorType type : values()){
			if(type.val == val){
				return type;
			}
		}
		throw new IllegalArgumentException("The error type(val = " + val + ") is invalid.");
	}
	
	public int getVal(){
		return this.val;
	}
	
	public String getDesc(){
		return this.desc;
	}
}
