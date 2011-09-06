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
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.protocol.Food;
import com.wireless.protocol.Taste;
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
		//_selectedFood = (Food)_orderList._orderFoods.elementAt(_orderList.getSelectedIndex());
		_selectedFood = _orderList.getSelectedFood();
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
	    					resp = VerifyPwd.ask(VerifyPwd.PWD_3);
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
																	_orderList.getSelectedFood(), 
																	_type, _self));
					
	    		}else if(resp == VerifyPwd.VERIFY_FAIL){
	    			Dialog.alert("你输入的权限密码不正确");
	    		}

			}
			
		});
		
		HorizontalFieldManager hfm = new HorizontalFieldManager(Manager.FIELD_HCENTER | Manager.HORIZONTAL_SCROLL);

		
		//the button to show selected food detail
		ButtonField showDetailBtn = new ButtonField("查看", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		showDetailBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				UiApplication.getUiApplication().pushScreen(new ShowFoodPopup(_selectedFood));	
			}			
		});
		
		//the button to back to parent screen
		ButtonField cancelBtn = new ButtonField("返回", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		cancelBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				close();			
			}
		});
		
		hfm.add(delBtn);
		
		//the button to add a taste, show up only if taste preferences exist
		if(WirelessOrder.foodMenu.tastes.length != 0 || WirelessOrder.foodMenu.styles.length != 0 || WirelessOrder.foodMenu.specs.length != 0){
			ButtonField addTasteBtn = new ButtonField("口味+", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
			addTasteBtn.setChangeListener(new FieldChangeListener(){
				public void fieldChanged(Field field, int context) {
					UiApplication.getUiApplication().pushScreen(new SelectTTypePopup(_orderList, _selectedFood));		
				} 
			});
			hfm.add(addTasteBtn);
		}	
		
		//the button to remove taste, show up only if the food along with the taste
		for(int i = 0; i < _selectedFood.tastes.length; i++){
			if(_selectedFood.tastes[i].alias_id != Taste.NO_TASTE){
				ButtonField delTasteBtn = new ButtonField("口味-", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
				delTasteBtn.setChangeListener(new FieldChangeListener(){
					public void fieldChanged(Field field, int context) {
						UiApplication.getUiApplication().pushScreen(new RemoveTastePopup(_orderList, _selectedFood));		
					} 
				});
				hfm.add(delTasteBtn);
				break;
			}
		}	

		hfm.add(showDetailBtn);
		
		add(hfm);
		
		add(new SeparatorField());
		
		//add(cancelBtn);
	}
	
}

