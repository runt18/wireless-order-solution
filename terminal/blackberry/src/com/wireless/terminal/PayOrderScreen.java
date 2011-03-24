package com.wireless.terminal;

import com.wireless.protocol.Order;
import com.wireless.ui.field.OrderListField;

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;

public class PayOrderScreen extends MainScreen
							implements PostPayOrder{
	
	private ListField _orderListField;
	private LabelField _customNum;
	private Order _bill;
	private PayOrderScreen _self = this;
	
	// Constructor
	public PayOrderScreen(Order bill){
		_bill = bill;
		setTitle("结帐");
		//The food has ordered would be listed in here.
		VerticalFieldManager _vfm = new VerticalFieldManager();
		_vfm.add(new SeparatorField());
		_vfm.add(new LabelField(_bill.tableID + "号餐台信息", LabelField.USE_ALL_WIDTH | DrawStyle.HCENTER){
			protected void paintBackground(Graphics g) {
				g.clear();
				g.setBackgroundColor(Color.GRAY);
				super.paintBackground(g);
			} 
			protected void paint(Graphics g){
				g.clear();
				g.setColor(Color.WHITE);		
				super.paint(g);  
			}
		});
		_customNum = new LabelField("人数：" + new Integer(_bill.customNum).toString());
		_vfm.add(_customNum);
		_vfm.add(new SeparatorField());
		_vfm.add(new LabelField("已点菜", LabelField.USE_ALL_WIDTH | DrawStyle.HCENTER){
			protected void paintBackground(Graphics g) {
				g.clear();
				g.setBackgroundColor(Color.GRAY);
				super.paintBackground(g);
			} 
			protected void paint(Graphics g){
				g.clear();
				g.setColor(Color.WHITE);		
				super.paint(g);  
			}
		});
		
		_orderListField = new OrderListField(_bill.foods, Field.NON_FOCUSABLE);
		
		_vfm.add(_orderListField);
		_vfm.add(new SeparatorField());
		add(_vfm);
		HorizontalFieldManager _hfm1 = new HorizontalFieldManager(Manager.FIELD_RIGHT);
		_hfm1.add(new LabelField("合计：" + _bill.price2StringEx()));
		add(_hfm1);
		add(new SeparatorField());
		
		ButtonField _submit = new ButtonField("提交", ButtonField.CONSUME_CLICK);
		HorizontalFieldManager _hfm2 = new HorizontalFieldManager(Manager.FIELD_HCENTER);
		_hfm2.add(_submit);
		add(_hfm2);
		
		//Set the submit button's listener
		_submit.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				UiApplication.getUiApplication().pushScreen(new PayOrderPopup2(_bill.tableID, _bill.getTotalPrice(), _self));
	         }
		});
		
		//Focus on order button
		_submit.setFocus();
	}  
	
	protected boolean onSavePrompt(){
		return true;
	}
	
	public boolean onClose(){
		int resp = Dialog.ask(Dialog.D_YES_NO, "还未提交结帐，确认退出?", Dialog.NO);
		if(resp == Dialog.YES){
			return super.onClose();
		}else{
			return false;
		}
	}		

	
	public void payOrderPass(){
		UiApplication.getUiApplication().popScreen(_self);
	}
	
	public void payOrderFail(){
		
	}
}
