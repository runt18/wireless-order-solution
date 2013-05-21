package com.wireless.print.content;

import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.print.PVar;

public class FoodDetailContent extends ConcreteContent {
	
	private String _format;
	private OrderFood _food;
	
	public FoodDetailContent(String format, OrderFood food, PStyle style){
		super(PType.PRINT_UNKNOWN, style);
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
		
		_format = _format.replace(PVar.FOOD_NAME, _food.getName());
		_format = _format.replace(PVar.FOOD_AMOUNT, "(" + NumericUtil.float2String2(_food.getCount()) + ")");
		String foodPrice = NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(_food.calcPriceWithTaste());
		
		String taste = null;
		if(_food.hasTaste()){
			taste = "-" + _food.getTasteGroup().getTastePref();
		}
		_format = _format.replace(PVar.FOOD_TASTE, taste == null ? "" : taste);				
//		if(_food.hasTaste()){
//			taste = "-" + _food.getTastePref();
//			_format = _format.replace(PVar.FOOD_TASTE, taste);				
//		}else{
//			_format = _format.replace(PVar.FOOD_TASTE, "");
//		}

		String combo;
		if(_food.asFood().isCombo()){
			combo = "(杶)";
		}else{
			combo = "";
		}
		_format = _format.replace(PVar.FOOD_COMBO, combo);
		
		String discount;
		if(!_food.asFood().isSpecial() && _food.getDiscount() != 1){
			discount = "(" + Float.toString(_food.getDiscount() * 10) + "殏)";
		}else{
			discount = "";
		}
		_format = _format.replace(PVar.FOOD_DISCOUNT, discount);
		
		StringBuilder status = new StringBuilder();
		if(_food.asFood().isSpecial()){
			if(status.length() != 0){
				status.append(",");
			}
			status.append("杻");
		}
		if(_food.asFood().isRecommend()){
			if(status.length() != 0){
				status.append(",");
			}
			status.append("熱");
		}
		if(_food.asFood().isGift()){
			if(status.length() != 0){
				status.append(",");
			}
			status.append("崌");
		}
		if(_food.asFood().isWeigh()){
			if(status.length() != 0){
				status.append(",");
			}
			status.append("備");
		}
		if(status.length() != 0){
			status.insert(0, "(").append(")");
		}
		
		_format = _format.replace(PVar.FOOD_STATUS, status);
		
		String hangStatus;
		if(_food.isHangup()){
			hangStatus = "(請)";
		}else{
			hangStatus = "";
		}
		_format = _format.replace(PVar.HANG_STATUS, hangStatus);
		
		String tempStatus;
		if(_food.isTemp()){
			tempStatus = "(還)";
		}else{
			tempStatus = "";
		}
		_format = _format.replace(PVar.TEMP_STATUS, tempStatus);		
	
		return new Grid2ItemsContent(_format, foodPrice, getStyle()).toString();

	}
	
}
