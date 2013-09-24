package com.wireless.print;


final public class PFormat{
	//$(temp_status)$(hang_status)$(name)$(taste)$(count)$(combo)$(discount)$(status)	
	public final static String RECEIPT_FORMAT_DEF = PVar.TEMP_STATUS + 
													PVar.HANG_STATUS + 
													PVar.FOOD_NAME + 
													PVar.FOOD_TASTE + 
													PVar.FOOD_AMOUNT + 
													PVar.FOOD_COMBO + 
													PVar.FOOD_DISCOUNT + 
													PVar.FOOD_STATUS;
	
	//$(temp_status)$(hang_status)$(name)$(taste)$(count)$(combo)$(status)	
	public final static String FROMAT_NO_DISCOUNT = PVar.TEMP_STATUS + 
													PVar.HANG_STATUS + 
													PVar.FOOD_NAME + 
													PVar.FOOD_TASTE + 
													PVar.FOOD_AMOUNT + 
													PVar.FOOD_COMBO + 
													PVar.FOOD_STATUS;
	
	//$(name)($(count))$(unit_price)
	public final static String FORMAT_2 = PVar.FOOD_NAME + 
										  PVar.FOOD_TASTE + "(" + 
										  PVar.FOOD_AMOUNT + ")" + 
										  PVar.SPACE + 
										  PVar.FOOD_UNIT_PRICE;

}
