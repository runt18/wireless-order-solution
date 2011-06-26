package com.wireless.ui.payoder;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.protocol.Order;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.ui.field.OrderListField;

public class PayOrderScreen extends MainScreen
							implements PostPayOrder{
	
	private ListField _orderListField = null;
	private LabelField _customNum = null;
	private EditField _cashIncome = null;
	private Order _bill = null;
	private PayOrderScreen _self = this;
	
	// Constructor
	public PayOrderScreen(Order bill){
		_bill = bill;
		setTitle("结帐");
		//The food has ordered would be listed in here.
		VerticalFieldManager vfm = new VerticalFieldManager();
		vfm.add(new SeparatorField());
		vfm.add(new LabelField(_bill.table_id + "号餐台信息", LabelField.USE_ALL_WIDTH | DrawStyle.HCENTER){
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
		
		HorizontalFieldManager hfm = new HorizontalFieldManager(Field.USE_ALL_WIDTH);
		_customNum = new LabelField("人数：" + new Integer(_bill.custom_num).toString());
		hfm.add(_customNum);
		
		if(Util.float2Int(bill.getMinimumCost()) != 0){
			LabelField minimumCost = new LabelField("最低消:￥" + Util.float2String2(bill.getMinimumCost()), LabelField.USE_ALL_WIDTH | DrawStyle.RIGHT);
			hfm.add(minimumCost);
		}
		
		vfm.add(hfm);
		
		vfm.add(new SeparatorField());
		vfm.add(new LabelField("已点菜", LabelField.USE_ALL_WIDTH | DrawStyle.HCENTER){
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
		
		_orderListField = new OrderListField(_bill.foods, Field.NON_FOCUSABLE, Type.PAY_ORDER);
		
		vfm.add(_orderListField);
		vfm.add(new SeparatorField());
		add(vfm);
		HorizontalFieldManager hfm1 = new HorizontalFieldManager(Manager.FIELD_RIGHT);
		hfm1.add(new LabelField("合计：" + Util.CURRENCY_SIGN + Util.float2String(_bill.totalPrice2())));
		add(hfm1);
		
		HorizontalFieldManager hfm2 = new HorizontalFieldManager(Field.FIELD_RIGHT);
		_cashIncome = new EditField("实收：￥", 
									  Util.float2String(_bill.totalPrice2()), 
									  7, 
									  Field.HIGHLIGHT_SELECT | Field.FIELD_RIGHT | EditField.NO_LEARNING | EditField.NO_NEWLINE | EditField.FILTER_REAL_NUMERIC){
		    public void layout(int width, int height) {
		        super.layout(getPreferredWidth(), height);
		        setExtent(getPreferredWidth(), getHeight());
		    }
		    
		    public int getPreferredWidth() {
		        int maxChars = this.getTextLength(); // + 1 to allow some visible extra space
		        int textSpace = this.getFont().getAdvance(Characters.DIGIT_ZERO) * maxChars +
		                        this.getFont().getAdvance(this.getLabel()); 
		        return textSpace;
		    }	    
		    
			protected boolean keyChar(char key, int status, int time) {
				boolean result = super.keyChar(key, status, time);
				// changes in the field's text require a new layout (width change)
				layout(getPreferredWidth(), getHeight());
				return result;
			}
		};
		
		hfm2.add(_cashIncome);
		//add(hfm2);
		
		add(new SeparatorField());
		
		ButtonField submitNormal = new ButtonField("一般", ButtonField.CONSUME_CLICK);
		HorizontalFieldManager hfm3 = new HorizontalFieldManager(Manager.FIELD_HCENTER);
		hfm3.add(submitNormal);
		hfm3.add(new LabelField("    "));
		ButtonField submitDiscount = new ButtonField("折扣", ButtonField.CONSUME_CLICK);
		hfm3.add(submitDiscount);
		add(hfm3);
		
		//Set the submit button's listener
		submitNormal.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				payOrder(Order.PAY_NORMAL, Order.DISCOUNT_1);
	         }
		});
		
		//Set the submit discount's listener
		submitDiscount.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				payOrder(Order.PAY_NORMAL, Order.DISCOUNT_2);
			}			
		});
		
		//Focus on order button
		submitNormal.setFocus();
	}  
	
	private void payOrder(int payType, int distType){
		try{
			int totalPrice = Util.float2Int(_bill.totalPrice2());
			int minimumCost = Util.float2Int(_bill.getMinimumCost());
			//check to see whether the total price reach the minimum cost
			if(totalPrice < minimumCost){
				Dialog.alert("消费额还没到最低消费,暂不能结帐");
			}else{
				//_bill.setCashIncome(new Float(Float.parseFloat(_cashIncome.getText())));
				
				_bill.pay_type = payType;
				_bill.discount_type = distType;
				UiApplication.getUiApplication().pushScreen(new SelectMannerPopup(_bill, _self));
			}
		}catch(NumberFormatException e){
			Dialog.alert("实收数字不正确，请重新输入");
			_cashIncome.setFocus();
		}
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
