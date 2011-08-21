package com.wireless.ui.neworder;

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
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.terminal.WirelessOrder;
import com.wireless.ui.field.OrderListField;
import com.wireless.ui.field.SelectFoodPopup;
import com.wireless.ui.field.SelectKitchenPopup;
import com.wireless.ui.field.TopBannerField;



public class ChangeOrderScreen extends MainScreen
								implements PostSubmitOrder{
	private OrderListField _orderListField;
	private LabelField _tableTitle = null;
	private EditField _table = null;
	private EditField _customNum;
	private final Order _originalOrder;
	private ChangeOrderScreen _self = this;
	
	// Constructor
	public ChangeOrderScreen(Order bill){
		_originalOrder = bill;
		
		setBanner(new TopBannerField("改单"));
		//setTitle("改单");
		//The food has ordered would be listed in here.
		VerticalFieldManager vfm = new VerticalFieldManager();
		vfm.add(new SeparatorField());
		
		String category;
		if(bill.category == Order.CATE_JOIN_TABLE){
			category = "(并台)";
		}else if(bill.category == Order.CATE_MERGER_TABLE){
			category = "(拼台)";
		}else if(bill.category == Order.CATE_TAKE_OUT){
			category = "(外卖)";
		}else{
			category = "";
		}
		
		_tableTitle = new LabelField(_originalOrder.table_id + "号餐台信息" + category, LabelField.USE_ALL_WIDTH | DrawStyle.HCENTER){
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
		};

		vfm.add(_tableTitle);
		
		_table = new EditField("台号：", Integer.toString(_originalOrder.table_id),
			   	   			   4, TextField.NO_NEWLINE | EditField.FILTER_NUMERIC);
		if(bill.category == Order.CATE_NORMAL){
			vfm.add(_table);			
		}

		_customNum = new EditField("人数：", Integer.toString(_originalOrder.custom_num), 2,
								   TextField.NO_NEWLINE | EditField.FILTER_NUMERIC) {
			protected boolean navigationClick(int status, int time) {
				return true;
			}
		};		

		vfm.add(_customNum);
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
		
		_orderListField = new OrderListField(_originalOrder.foods, Type.UPDATE_ORDER);
		
		vfm.add(_orderListField);
		vfm.add(new SeparatorField());
		add(vfm);
		
		//Three buttons would be shown in the bottom of the screen
		ButtonField byNoBtn = new ButtonField("编号", ButtonField.CONSUME_CLICK);
		ButtonField byKitchenBtn = new ButtonField("分厨", ButtonField.CONSUME_CLICK);
		ButtonField submit = new ButtonField("提交", ButtonField.CONSUME_CLICK);
		HorizontalFieldManager hfm = new HorizontalFieldManager(Manager.FIELD_HCENTER);
		hfm.add(byNoBtn);
		hfm.add(new LabelField("  "));
		hfm.add(byKitchenBtn);
		hfm.add(new LabelField("  "));
		hfm.add(submit);
		add(hfm);
		
		//Set the listener to order button
		byNoBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
	             UiApplication.getUiApplication().pushScreen(new SelectFoodPopup(_orderListField, WirelessOrder.foodMenu.foods));
	         }
		});
		
		//Set the listener to order by kitchen button
		byKitchenBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				UiApplication.getUiApplication().pushScreen(new SelectKitchenPopup(_orderListField));
			}
		});
		
		//Set the submit button's listener
		submit.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				if(_orderListField.getSize() == 0){
					Dialog.alert("点菜单为空，暂时不能改单。");
				}else{
					Food[] foods = new Food[_orderListField.getSize()];
					/**
					 * Since in the change order screen, the count to food is divided in two parts.
					 * - original count
					 * - difference count
					 * Plus both of them to make the order count finally.
					 */
					for(int i = 0; i < _orderListField.getSize(); i++){
						foods[i] = (Food)_orderListField.getCallback().get(null, i);
						//Get the difference count
						int diffCount = Util.float2Int(foods[i].getDiffCount());
						diffCount = foods[i].diffPositive ? diffCount : -diffCount;
						//Plus the original and difference count
						int count = Util.float2Int(foods[i].getCount()) + diffCount;
						//Set the order count to this food
						foods[i].setCount(Util.int2Float(count));
					}
					Order reqOrder = new Order(foods, 
											   Short.parseShort(_table.getText()), 
											   Integer.parseInt(_customNum.getText()));
					reqOrder.originalTableID = _originalOrder.table_id;
					
					UiApplication.getUiApplication().pushScreen(new SubmitChangePopup(reqOrder, _self));
				}
	         }
		});
		
		//Focus on order button
		byNoBtn.setFocus();
	}  
	
	protected boolean onSavePrompt(){
		return true;
	}
	
	public boolean onClose(){
		int resp = Dialog.ask(Dialog.D_YES_NO, "还未提交改单，确认退出?", Dialog.NO);
		if(resp == Dialog.YES){
			return super.onClose();
		}else{
			return false;
		}
	}
	
	public void submitOrderPass(){
		UiApplication.getUiApplication().popScreen(_self);
	}
	
	public void submitOrderFail(){
		
	}
}
