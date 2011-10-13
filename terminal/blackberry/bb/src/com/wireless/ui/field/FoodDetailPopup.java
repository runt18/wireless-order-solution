package com.wireless.ui.field;

import com.wireless.protocol.Food;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Util;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class FoodDetailPopup extends PopupScreen {
	
	
	public FoodDetailPopup(Food selectedFood){
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR), DEFAULT_CLOSE);
		
		add(new LabelField(selectedFood.name));
		add(new SeparatorField());
		
		add(new LabelField("数量：" + Util.float2String2(selectedFood.getCount())));
		add(new LabelField("价钱：" + Util.CURRENCY_SIGN + Util.float2String(selectedFood.totalPrice2())));
		if(!selectedFood.tastePref.equals(Taste.NO_PREFERENCE)){
			add(new LabelField("口味：" + selectedFood.tastePref));			
		}
		
		String status = null;
		if(selectedFood.hangStatus == Food.FOOD_HANG_UP){
			status = "叫";
		}else if(selectedFood.hangStatus == Food.FOOD_IMMEDIATE){
			status = "即";
		}
		if(selectedFood.isHurried){
			status = (status == null ? "催" : (status + "," + "催"));
		}
		if(selectedFood.isTemporary){
			status = (status == null ? "临" : ("临" + "," + status));
		}
		if(status != null){
			add(new LabelField("状态：" + status));
		}
		
		String foodProperty = null;
		if(selectedFood.isSpecial()){
			foodProperty = "特";
		}
		if(selectedFood.isRecommend()){
			foodProperty = (foodProperty == null ? "荐" : (foodProperty + "," + "荐"));
		}
		if(selectedFood.isGift()){
			foodProperty = (foodProperty == null ? "赠" : (foodProperty + "," + "赠"));
		}
		if(selectedFood.isSellOut()){
			foodProperty = (foodProperty == null ? "停" : (foodProperty + "," + "停"));
		}
		if(foodProperty != null){
			add(new LabelField("属性：" + foodProperty));
		}
		
		add(new SeparatorField());
		
		ButtonField cancelBtn = new ButtonField("确定", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		cancelBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				close();
			}	
		});
		add(cancelBtn);
	}
}
