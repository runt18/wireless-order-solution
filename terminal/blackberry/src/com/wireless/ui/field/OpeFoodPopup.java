package com.wireless.ui.field;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.protocol.Food;
import com.wireless.protocol.Type;
import com.wireless.terminal.WirelessOrder;

class OpeFoodPopup extends PopupScreen{
	
	private OrderListField _orderList = null;
	private Food _selectedFood = null;
	private PopupScreen _self = this;
	private byte _type = Type.INSERT_ORDER;
	
	OpeFoodPopup(OrderListField parent, byte type){
		super(new VerticalFieldManager(Manager.FIELD_HCENTER), DEFAULT_CLOSE);		
		_type = type;
		_orderList = parent;
		_selectedFood = (Food)_orderList._orderFoods.elementAt(_orderList.getSelectedIndex());
		add(new LabelField("请选择\"" + _selectedFood.name + "\"的操作", DrawStyle.ELLIPSIS));
		add(new SeparatorField());

		//the button to remove the food from the order list
		ButtonField delBtn = new ButtonField(type == Type.INSERT_ORDER ? "删除" : "退菜", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		delBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
	    		int resp;
				/**
				 * In the case of "改单"，ask operator for permission before cancel a food
				 */
	    		if(_type == Type.UPDATE_ORDER){
	    			if(WirelessOrder.restaurant.pwd2 != null){
	    				if(WirelessOrder.restaurant.pwd2.length() != 0){
	    					resp = VerifyPwd.ask();
	    				}else{
	    					resp = VerifyPwd.VERIFY_PASS;
	    				}
	    			}else{
	    				resp = VerifyPwd.VERIFY_PASS;
	    			}	    			
	    			
	    		}else if(_type == Type.INSERT_ORDER){
	    			resp = VerifyPwd.VERIFY_PASS;
	    			
	    		}else{
	    			resp = VerifyPwd.VERIFY_PASS;
	    		}
	    		if(resp == VerifyPwd.VERIFY_PASS){

					UiApplication.getUiApplication().pushModalScreen(new RemoveFoodPopup(_orderList, 
																	(Food)_orderList._orderFoods.elementAt(_orderList.getSelectedIndex()), 
																	_type, _self));
					
	    		}else if(resp == VerifyPwd.VERIFY_FAIL){
	    			Dialog.alert("你输入的权限密码不正确");
	    		}

			}
			
		});
		//the button to back to parent screen
		ButtonField cancelBtn = new ButtonField("返回", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		cancelBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				close();			
			}
		});
		
		//the button to add a taste, show up only if taste preferences exist
		if(WirelessOrder.foodMenu.tastes.length != 0){
			ButtonField tasteBtn = new ButtonField("口味", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
			tasteBtn.setChangeListener(new FieldChangeListener(){
				public void fieldChanged(Field field, int context) {
					UiApplication.getUiApplication().pushScreen(new SelectTastePopup(_orderList, _selectedFood, _self));		
				}
			});
			add(tasteBtn);
		}
		
		add(delBtn);
		add(cancelBtn);
	}
	
}

