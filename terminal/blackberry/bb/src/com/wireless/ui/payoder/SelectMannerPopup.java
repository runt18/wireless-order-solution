package com.wireless.ui.payoder;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.protocol.Order;

/**
 * The popup screen for user to select the pay manner.
 * There are two manners available for use.
 * 1 - cash
 * 2 - credit card 
 *
 */
class SelectMannerPopup extends PopupScreen{
	
	private Order _bill = null;
	private PayOrderScreen _payOrderScreen = null;
	
	SelectMannerPopup(Order bill, PayOrderScreen screen){
		super(new VerticalFieldManager(Manager.FIELD_HCENTER), DEFAULT_CLOSE);
		_bill = bill;
		_payOrderScreen = screen;
		
		add(new LabelField("请选择付款的方式", DrawStyle.ELLIPSIS));
		add(new SeparatorField());
		
		//the button to use cash for payment
		ButtonField cashBtn = new ButtonField("现金", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		cashBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				_bill.setPayManner(Order.MANNER_CASH);
				UiApplication.getUiApplication().pushScreen(new PayOrderPopup2(_bill, _payOrderScreen));
				close();
			}			
		});
		add(cashBtn);
		
		//the button to use credit card for payment
		ButtonField creditCardBtn = new ButtonField("刷卡", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		creditCardBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				_bill.setPayManner(Order.MANNER_CREDIT_CARD);
				UiApplication.getUiApplication().pushScreen(new PayOrderPopup2(_bill, _payOrderScreen));
				close();
			}			
		});
		add(creditCardBtn);
		
		//the button to use credit card for payment
		ButtonField cancelBtn = new ButtonField("返回", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		cancelBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				close();
			}			
		});
		add(cancelBtn);
	}
}
