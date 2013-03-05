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

import com.wireless.pack.Type;
import com.wireless.protocol.OrderFood;
import com.wireless.terminal.WirelessOrder;

class OpeFoodPopup extends PopupScreen{
	
	private OrderListField _orderList = null;
	private OrderFood _selectedFood = null;
	private PopupScreen _self = this;
	private byte _type = Type.INSERT_ORDER;
	
	OpeFoodPopup(OrderListField parent, byte type){
		super(new VerticalFieldManager(Manager.FIELD_HCENTER), DEFAULT_CLOSE);		
		_type = type;
		_orderList = parent;
		//_selectedFood = (Food)_orderList._orderFoods.elementAt(_orderList.getSelectedIndex());
		_selectedFood = _orderList.getSelectedFood();
		add(new LabelField("请选择\"" + _selectedFood.getName() + "\"的操作", DrawStyle.ELLIPSIS));
		add(new SeparatorField());

		HorizontalFieldManager hfm = new HorizontalFieldManager(Manager.FIELD_HCENTER | Manager.HORIZONTAL_SCROLL);

		ButtonField hangBtn = null;
		if(_type == Type.INSERT_ORDER){
			if(!_orderList.getSelectedFood().isHangup()){
				hangBtn = new ButtonField("叫起");
				hangBtn.setChangeListener(new FieldChangeListener() {					
					public void fieldChanged(Field field, int context) {
						int resp = Dialog.ask(Dialog.D_YES_NO, "确认叫起" + _orderList.getSelectedFood().getName() + "?");
						if(resp == Dialog.YES){
							_orderList.getSelectedFood().toggleHangup();
							_orderList.invalidate(_orderList.getSelectedIndex());
							close();
						}
					}
				});
				
			}else if(_orderList.getSelectedFood().isHangup()){
				hangBtn = new ButtonField("即起");
				hangBtn.setChangeListener(new FieldChangeListener() {					
					public void fieldChanged(Field field, int context) {
						int resp = Dialog.ask(Dialog.D_YES_NO, "确认取消叫起" + _orderList.getSelectedFood().getName() + "?");
						if(resp == Dialog.YES){
							_orderList.getSelectedFood().toggleHangup();
							_orderList.invalidate(_orderList.getSelectedIndex());
							close();
						}
					}
				});				
			}
			

		}else if(_type == Type.UPDATE_ORDER){
//			if(_orderList.getSelectedFood().hangStatus == OrderFood.FOOD_HANG_UP){
//				hangBtn = new ButtonField("即起");
//				hangBtn.setChangeListener(new FieldChangeListener() {					
//					public void fieldChanged(Field field, int context) {
//						int resp = Dialog.ask(Dialog.D_YES_NO, "确认即起" + _orderList.getSelectedFood().getName() + "?");
//						if(resp == Dialog.YES){
//							_orderList.getSelectedFood().hangStatus = OrderFood.FOOD_IMMEDIATE;			
//							_orderList.invalidate(_orderList.getSelectedIndex());
//							close();
//						}
//					}
//				});
//				
//			}else if(_orderList.getSelectedFood().hangStatus == OrderFood.FOOD_IMMEDIATE){
//				hangBtn = new ButtonField("叫起");
//				hangBtn.setChangeListener(new FieldChangeListener() {					
//					public void fieldChanged(Field field, int context) {
//						int resp = Dialog.ask(Dialog.D_YES_NO, "确认重新叫起" + _orderList.getSelectedFood().getName() + "?");
//						if(resp == Dialog.YES){
//							_orderList.getSelectedFood().hangStatus = OrderFood.FOOD_HANG_UP;	
//							_orderList.invalidate(_orderList.getSelectedIndex());
//							close();
//						}
//					}
//				});				
//			}
		}

		
		//the button to remove the food from the order list
		ButtonField delBtn = new ButtonField(type == Type.INSERT_ORDER ? "删除" : "退菜", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		delBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
	    		int resp;
				/**
				 * In the case of "改单"，ask operator for permission before cancel a food
				 */
	    		if(_type == Type.UPDATE_ORDER){
	    			resp = VerifyPwd.ask(VerifyPwd.PWD_5);
	    			
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

		//the button to show selected food detail
		ButtonField showDetailBtn = new ButtonField("查看", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		showDetailBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				UiApplication.getUiApplication().pushScreen(new FoodDetailPopup(_selectedFood));	
			}			
		});
		
		//the button to hurried food
		ButtonField hurriedBtn = new ButtonField(_selectedFood.isHurried() ? "催菜-" : "催菜", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		hurriedBtn.setChangeListener(new FieldChangeListener() {			
			public void fieldChanged(Field field, int context) {
    			if(_selectedFood.isHurried()){
    				int resp = Dialog.ask(Dialog.D_YES_NO, "确认取消催菜" + _selectedFood.getName() + "?");
    				if(resp == Dialog.YES){
    					_selectedFood.setHurried(false);    
    					_orderList.invalidate(_orderList.getSelectedIndex());
    					close();
    				}
    			}else{
    				int resp = Dialog.ask(Dialog.D_YES_NO, "确认催菜" + _selectedFood.getName() + "?");
    				if(resp == Dialog.YES){
    					_selectedFood.setHurried(true);    	
    					_orderList.invalidate(_orderList.getSelectedIndex());
    					close();
    				}
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
		
		
		
		//the button to add a taste, show up only if taste preferences exist and insert order
		ButtonField addTasteBtn = null;
		ButtonField delTasteBtn = null;
		if(type == Type.INSERT_ORDER){
			if(WirelessOrder.foodMenu.tastes.length != 0 || WirelessOrder.foodMenu.styles.length != 0 || WirelessOrder.foodMenu.specs.length != 0){
				addTasteBtn = new ButtonField("口味+", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
				addTasteBtn.setChangeListener(new FieldChangeListener(){
					public void fieldChanged(Field field, int context) {
						UiApplication.getUiApplication().pushScreen(new SelectTTypePopup(_orderList, _selectedFood));		
					} 
				});
				
			}	
			
			//the button to remove taste, show up only if the food along with the taste
			if(_selectedFood.hasTaste()){
				delTasteBtn = new ButtonField("口味-", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
				delTasteBtn.setChangeListener(new FieldChangeListener(){
					public void fieldChanged(Field field, int context) {
						UiApplication.getUiApplication().pushScreen(new RemoveTastePopup(_orderList, _selectedFood));		
					} 
				});
			}	
		}

		hfm.add(delBtn);
		
		if(addTasteBtn != null){
			hfm.add(addTasteBtn);
		}
		
		if(delTasteBtn != null){
			hfm.add(delTasteBtn);
		}
		
		if(hangBtn != null){
			if(_type == Type.UPDATE_ORDER && _orderList.getSelectedFood().isHangup()){
				hfm.insert(hangBtn, 0);
			}else{
				hfm.add(hangBtn);
			}
		}
		
		/**
		 * Only show the hurried food button in change order screen.
		 */
		if(_type == Type.UPDATE_ORDER){
			hfm.add(hurriedBtn);
		}
		
		hfm.add(showDetailBtn);
		
		add(hfm);
		
		add(new SeparatorField());
		
		//add(cancelBtn);
	}
	
}

