package com.wireless.print.content;

import java.util.Comparator;

import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.pojo.util.SortedList;

public class FoodDetailContent extends ConcreteContent {
	
	public static enum DisplayItem{
		WEIGHT(1, "(��)"),
		TEMP(2, "(��)"),
		HANG(3, "(��)"),
		HURRIED(4, "(��)"),
		GIFT(5, "(��)"),
		NAME(6, "����"),
		AMOUNT(7, "����"),
		TASTE(8, "��ζ"),
		COMBO(9, "(��)"),
		DISCOUNT(10, "�ۿ�"),
		STATUS(11, "״̬");
		
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
	 * (��)(��)(��)(��)Food-Taste(1)(��)(8.5��)(��)  $32.00<br>
	 * -----------------------------------------------------<br>
	 */
	@Override
	public String toString(){
		
		StringBuilder detail = new StringBuilder();

		if(_displayConfig.contains(DisplayItem.WEIGHT) && _of.asFood().isWeigh()){
			if(mPrintType == PType.PRINT_EXTRA_FOOD_DETAIL || mPrintType == PType.PRINT_ALL_EXTRA_FOOD){
				detail.append("(��+)");
			}
		}
		
		if(_displayConfig.contains(DisplayItem.HURRIED) && _of.isHurried()){
			detail.append("(��)");
		}
		
		if(_displayConfig.contains(DisplayItem.TEMP) && _of.isTemp()){
			detail.append("(��)");
		}
		
		if(_displayConfig.contains(DisplayItem.HANG) && _of.isHangup()){
			detail.append("(��)");
		}
		
		if(_displayConfig.contains(DisplayItem.GIFT) && _of.isGift()){
			detail.append("(��)");
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
				detail.append("(" + Float.toString(NumericUtil.roundFloat(_of.getDiscount() * 10)) + "��)");
			}
		}
			
		if(_displayConfig.contains(DisplayItem.COMBO) && _of.asFood().isCombo()){
			detail.append("(��)");
		}
		
		if(_displayConfig.contains(DisplayItem.STATUS)){
			StringBuilder status = new StringBuilder();
			if(_of.asFood().isSpecial()){
				if(status.length() != 0){
					status.append(",");
				}
				status.append("��");
			}
			if(status.length() != 0){
				status.insert(0, "(").append(")");
			}
			detail.append(status);
		}
		
		String foodPrice;
		if(_displayConfig.contains(DisplayItem.DISCOUNT)){
			foodPrice = NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(_of.calcPrice());
		}else{
			foodPrice = NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(_of.calcPriceBeforeDiscount());
		}
		
		return new Grid2ItemsContent(detail.toString(), foodPrice, getStyle()).toString();

	}
	
}
