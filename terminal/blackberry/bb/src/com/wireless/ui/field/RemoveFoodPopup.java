package com.wireless.ui.field;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;

class RemoveFoodPopup extends PopupScreen implements FieldChangeListener{
	
	private EditField _amount;
	private ButtonField _ok;
	private ButtonField _cancel;
	private OrderListField _orderList;
	private PopupScreen _parent;
	private OrderFood _food2Del;
	private byte _type = Type.INSERT_ORDER;
	
	RemoveFoodPopup(OrderListField orderList, OrderFood foodToDel, byte type, PopupScreen parent){
		super(new VerticalFieldManager(), DEFAULT_CLOSE);
		
		_orderList = orderList;
		_food2Del = foodToDel;
		_type = type;
		_parent = parent;
		
		String ope;
		if(_type == Type.INSERT_ORDER){
			ope = "删除";
		}else if(_type == Type.UPDATE_ORDER){
			ope = "退菜";
		}else{
			ope = "删除";
		}
		add(new LabelField("请输入\"" + _food2Del.name + "\"" + ope + "数量", LabelField.USE_ALL_WIDTH | DrawStyle.LEFT));
		add(new SeparatorField());
		_amount = new EditField("", Util.float2String2(_food2Del.getCount()), 6, EditField.FILTER_REAL_NUMERIC);
		add(_amount);
		add(new SeparatorField());
		_ok = new ButtonField("确定", ButtonField.CONSUME_CLICK);
		_ok.setChangeListener(this);
		_cancel = new ButtonField("取消", ButtonField.CONSUME_CLICK);
		_cancel.setChangeListener(this);
		HorizontalFieldManager _hfm = new HorizontalFieldManager(Manager.FIELD_HCENTER);
		_hfm.add(_ok);
		_hfm.add(_cancel);
		add(_hfm);
	}

	private void remove(){
		try{
			int removeAmount = Util.float2Int(Float.valueOf(_amount.getText()));
			int foodAmount = Util.float2Int(_food2Del.getCount());				
			
			if(foodAmount == removeAmount){
				//remove the food if the food amount equals to remove amount
				_orderList.delFood(_orderList.getSelectedIndex());
				close();
				if(_parent != null){
					_parent.close();
				}
			
			}else if(foodAmount < removeAmount){
				//prompt the user if the remove amount greater than food amount
				Dialog.alert("您输入的数超过了已点数量，请重新输入");
				UiApplication.getUiApplication().invokeLater(new Runnable(){
					public void run(){
						_amount.setFocus();
					}
				});
			
			}else{
				//update the remaining amount to the food
				_food2Del.setCount(Util.int2Float(foodAmount - removeAmount));
				_orderList.setSize(_orderList.getSize(), _orderList.getSelectedIndex());
				
				close();
				if(_parent != null){
					_parent.close();
				}
			}
			
		}catch(NumberFormatException e){
			Dialog.alert("您输入的数量格式不正确，请重新确认");
		}
	}
	
	public void fieldChanged(Field field, int context) {

		if(field == _ok){			
			remove();			
		}else if(field == _cancel) {
			close();
		}
	}
	
	protected boolean keyChar(char c, int status, int time) {
		if(c == Characters.ENTER) {
			remove();
			return true;
		}else{
			return super.keyChar(c, status, time);
		}
	}
	
}
