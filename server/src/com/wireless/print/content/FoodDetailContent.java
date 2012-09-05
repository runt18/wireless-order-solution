package com.wireless.print.content;

import com.wireless.print.PVar;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Util;

public class FoodDetailContent extends ConcreteContent {
	
	private String _format;
	private OrderFood _food;
	
	public FoodDetailContent(String format, OrderFood food, int style){
		super(Reserved.PRINT_UNKNOWN, style);
		_format = format;
		_food = food;
	}
	
	/**
	 * Generate a single line of order food to print.
	 * The style to this food list is like below.<br>
	 * ----------------------------------------------<br>
	 * (還)(請)Food-Taste(1)(杶)(8.5殏)(杻,熱)  $32.00<br>
	 * ----------------------------------------------<br>
	 */
	@Override
	public String toString(){
		
		_format = _format.replace(PVar.FOOD_NAME, _food.name);
		_format = _format.replace(PVar.FOOD_AMOUNT, "(" + Util.float2String2(_food.getCount()) + ")");
		String foodPrice = Util.CURRENCY_SIGN + Util.float2String(_food.calcPriceWithTaste());
		
		String taste = "";
		if(_food.hasTaste()){
			taste = "-" + _food.getTastePref();
			_format = _format.replace(PVar.FOOD_TASTE, taste);				
		}else{
			_format = _format.replace(PVar.FOOD_TASTE, "");
		}

		String combo;
		if(_food.isCombo()){
			combo = "(杶)";
		}else{
			combo = "";
		}
		_format = _format.replace(PVar.FOOD_COMBO, combo);
		
		String discount;
		if(!_food.isSpecial() && _food.getDiscount() != 1){
			discount = "(" + Float.toString(_food.getDiscount() * 10) + "殏)";
		}else{
			discount = "";
		}
		_format = _format.replace(PVar.FOOD_DISCOUNT, discount);
		
		String status = "";
		if(_food.isSpecial()){
			if(status.length() == 0){
				status = "杻";
			}else{
				status = status + ",杻";
			}
		}
		if(_food.isRecommend()){
			if(status.length() == 0){
				status = "熱";
			}else{
				status = status + ",熱";
			}
		}
		if(_food.isGift()){
			if(status.length() == 0){
				status = "崌";
			}else{
				status = status + ",崌";
			}
		}
		if(status.length() != 0){
			status = "(" + status + ")";
		}
		
		_format = _format.replace(PVar.FOOD_STATUS, status);
		
		String hangStatus;
		if(_food.hangStatus == OrderFood.FOOD_HANG_UP){
			hangStatus = "(請)";
		}else if(_food.hangStatus == OrderFood.FOOD_IMMEDIATE){
			hangStatus = "(撈)";
		}else{
			hangStatus = "";
		}
		_format = _format.replace(PVar.HANG_STATUS, hangStatus);
		
		String tempStatus;
		if(_food.isTemporary){
			tempStatus = "(還)";
		}else{
			tempStatus = "";
		}
		_format = _format.replace(PVar.TEMP_STATUS, tempStatus);		
	
		return new Grid2ItemsContent(_format, foodPrice, _printType, _style).toString();

	}
	
}
