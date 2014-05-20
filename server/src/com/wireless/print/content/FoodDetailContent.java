package com.wireless.print.content;

import java.util.Comparator;

import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.pojo.util.SortedList;

public class FoodDetailContent extends ConcreteContent {
	
	public static enum DisplayItem{
		WEIGHT(1, "(備)"),
		TEMP(2, "(還)"),
		HANG(3, "(請)"),
		HURRIED(4, "(殼)"),
		NAME(5, "粕靡"),
		AMOUNT(6, "杅講"),
		TASTE(7, "諳庤"),
		COMBO(8, "(杶)"),
		DISCOUNT(9, "殏諶"),
		STATUS(10, "袨怓");
		
		private final int id;
		private final String desc;
		
		DisplayItem(int id, String desc){
			this.id = id;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return desc;
		}
		
	}
	
	public final static DisplayConfig DISPLAY_CONFIG_4_RECEIPT = new DisplayConfig(new DisplayItem[]{ DisplayItem.WEIGHT, DisplayItem.HANG, DisplayItem.HURRIED }){
		@Override
		public DisplayConfig mask(DisplayItem item){
			throw new UnsupportedOperationException();
		}
	};
	
	public final static DisplayConfig DISPLAY_CONFIG_NO_DISCOUNT = new DisplayConfig(new DisplayItem[]{ DisplayItem.DISCOUNT }){
		@Override
		public DisplayConfig mask(DisplayItem item){
			throw new UnsupportedOperationException();
		}
	}; 
	
	public static class DisplayConfig{
		private final SortedList<DisplayItem> displayItems = SortedList.newInstance(new Comparator<DisplayItem>(){
			@Override
			public int compare(DisplayItem o1, DisplayItem o2) {
				if(o1.id < o2.id){
					return -1; 
				}else if(o1.id > o2.id){
					return 1;
				}else{
					return 0;
				}
			}
			
		});
		
		public DisplayConfig(){
			for(DisplayItem item : DisplayItem.values()){
				displayItems.add(item);
			}
		}
		
		private DisplayConfig(DisplayItem[] masks){
			this();
			for(DisplayItem item : masks){
				displayItems.removeElement(item);
			}
		}
		
		public DisplayConfig(DisplayConfig config){
			this.displayItems.addAll(config.displayItems);
		}
		
		public DisplayConfig mask(DisplayItem item){
			displayItems.removeElement(item);
			return this;
		}
		
		public boolean contains(DisplayItem item){
			return displayItems.containsElement(item);
		}
	}
	
	private final DisplayConfig _displayConfig;
	private final OrderFood _of;
	
	public FoodDetailContent(DisplayConfig config, OrderFood of, PType printType, PStyle style){
		super(printType, style);
		_displayConfig = config;
		_of = of;
	}
	
	/**
	 * Generate a single line of order food to print.
	 * The style to this food list is like below.<br>
	 * -----------------------------------------------------<br>
	 * (備)(還)(請)Food-Taste(1)(杶)(8.5殏)(杻,崌)  $32.00<br>
	 * -----------------------------------------------------<br>
	 */
	@Override
	public String toString(){
		
		StringBuilder detail = new StringBuilder();

		if(_displayConfig.contains(DisplayItem.WEIGHT) && _of.asFood().isWeigh()){
			if(mPrintType == PType.PRINT_EXTRA_FOOD_DETAIL || mPrintType == PType.PRINT_ALL_EXTRA_FOOD){
				detail.append("(備+)");
			}
		}
		
		if(_displayConfig.contains(DisplayItem.HURRIED) && _of.isHurried()){
			detail.append("(殼)");
		}
		
		if(_displayConfig.contains(DisplayItem.TEMP) && _of.isTemp()){
			detail.append("(還)");
		}
		
		if(_displayConfig.contains(DisplayItem.HANG) && _of.isHangup()){
			detail.append("(請)");
		}
		
		detail.append(_of.getName());
		
		if(_displayConfig.contains(DisplayItem.AMOUNT)){
			detail.append("(" + NumericUtil.float2String2(_of.getCount()) + ")");
		}
		
		if(_displayConfig.contains(DisplayItem.TASTE) && _of.hasTasteGroup()){
			detail.append("-" + _of.getTasteGroup().getPreference());
		}
		
		if(_displayConfig.contains(DisplayItem.DISCOUNT)){
			if(!_of.asFood().isSpecial() && _of.getDiscount() != 1){
				detail.append("(" + Float.toString(_of.getDiscount() * 10) + "殏)");
			}
		}
			
		if(_displayConfig.contains(DisplayItem.COMBO) && _of.asFood().isCombo()){
			detail.append("(杶)");
		}
		
		if(_displayConfig.contains(DisplayItem.STATUS)){
			StringBuilder status = new StringBuilder();
			if(_of.asFood().isSpecial()){
				if(status.length() != 0){
					status.append(",");
				}
				status.append("杻");
			}
	//		if(_of.asFood().isRecommend()){
	//			if(status.length() != 0){
	//				status.append(",");
	//			}
	//			status.append("熱");
	//		}
			if(_of.asFood().isGift()){
				if(status.length() != 0){
					status.append(",");
				}
				status.append("崌");
			}
			if(status.length() != 0){
				status.insert(0, "(").append(")");
			}
			detail.append(status);
		}
		//--------------------------------------------------------
		
//		_format = _format.replace(PVar.FOOD_NAME, _of.getName());
//		_format = _format.replace(PVar.FOOD_AMOUNT, "(" + NumericUtil.float2String2(_of.getCount()) + ")");
//		
//		String taste = null;
//		if(_of.hasTasteGroup()){
//			taste = "-" + _of.getTasteGroup().getPreference();
//		}
//		_format = _format.replace(PVar.FOOD_TASTE, taste == null ? "" : taste);				
//
//		String combo;
//		if(_of.asFood().isCombo()){
//			combo = "(杶)";
//		}else{
//			combo = "";
//		}
//		_format = _format.replace(PVar.FOOD_COMBO, combo);
//		
//		String discount;
//		if(!_of.asFood().isSpecial() && _of.getDiscount() != 1){
//			discount = "(" + Float.toString(_of.getDiscount() * 10) + "殏)";
//		}else{
//			discount = "";
//		}
//		_format = _format.replace(PVar.FOOD_DISCOUNT, discount);
//		
//		StringBuilder status = new StringBuilder();
//		if(_of.asFood().isSpecial()){
//			if(status.length() != 0){
//				status.append(",");
//			}
//			status.append("杻");
//		}
//		if(_of.asFood().isRecommend()){
//			if(status.length() != 0){
//				status.append(",");
//			}
//			status.append("熱");
//		}
//		if(_of.asFood().isGift()){
//			if(status.length() != 0){
//				status.append(",");
//			}
//			status.append("崌");
//		}
//		if(_of.asFood().isWeigh()){
//			if(status.length() != 0){
//				status.append(",");
//			}
//			status.append("備");
//		}
//		if(status.length() != 0){
//			status.insert(0, "(").append(")");
//		}
//		
//		_format = _format.replace(PVar.FOOD_STATUS, status);
//		
//		String hangStatus;
//		if(_of.isHangup()){
//			hangStatus = "(請)";
//		}else{
//			hangStatus = "";
//		}
//		_format = _format.replace(PVar.HANG_STATUS, hangStatus);
//		
//		String tempStatus;
//		if(_of.isTemp()){
//			tempStatus = "(還)";
//		}else{
//			tempStatus = "";
//		}
//		_format = _format.replace(PVar.TEMP_STATUS, tempStatus);		
	
		//---------------------------------------------------------
		
		String foodPrice;
		if(_displayConfig.contains(DisplayItem.DISCOUNT)){
			foodPrice = NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(_of.calcPriceWithTaste());
		}else{
			foodPrice = NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(_of.calcPriceBeforeDiscount());
		}
		
		return new Grid2ItemsContent(detail.toString(), foodPrice, getStyle()).toString();

	}
	
}
