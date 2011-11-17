
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

import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.terminal.WirelessOrder;
import com.wireless.ui.field.OrderListField;
import com.wireless.ui.field.SelectFoodPopup;
import com.wireless.ui.field.SelectKitchenPopup;
import com.wireless.ui.field.TopBannerField;

/**
 * The new order screen class.
 */
public class NewOrderScreen extends MainScreen
							implements PostSubmitOrder{

	private OrderListField _orderListField = null;	
	private LabelField _tableTitle = null;
	private EditField _table = null;
	private EditField _customNum = null;
	private LabelField _totalPrice = null;
	private short _tableID = 0;
	private NewOrderScreen _self = this;
	
	private LabelField _listTitle = new LabelField("已点菜", LabelField.USE_ALL_WIDTH | DrawStyle.HCENTER){
		protected void paintBackground(Graphics g) {
			g.clear();
			g.setBackgroundColor(Color.PURPLE);
			super.paintBackground(g);
		} 
		protected void paint(Graphics g){
			g.clear();
			g.setColor(Color.WHITE);		
			super.paint(g);  
		}
	};
	
	// Constructor
	public NewOrderScreen(short tableID, int customNum){
		
		setBanner(new TopBannerField("下单"));
		//setTitle("下单");
	
		//The food has ordered would be listed in here.
		VerticalFieldManager vfm = new VerticalFieldManager();	
		
		vfm.add(new SeparatorField());
		_tableID = tableID;
		_tableTitle = new LabelField(_tableID + "号餐台信息", LabelField.USE_ALL_WIDTH | DrawStyle.HCENTER){
			protected void paintBackground(Graphics g) {
				g.clear();
				g.setBackgroundColor(Color.BLUE);
				super.paintBackground(g);
			} 
			protected void paint(Graphics g){
				g.clear();
				g.setColor(Color.WHITE);		
				super.paint(g);  
			}
		};

		vfm.add(_tableTitle);
		
		//HorizontalFieldManager hfm1 = new HorizontalFieldManager(Field.USE_ALL_WIDTH);
		
		_table = new EditField("台号：", new Short(_tableID).toString(),
							   4, TextField.NO_NEWLINE | EditField.FILTER_NUMERIC);
		
		/**
		 * The id on table title would be changed with the input
		 */
		_table.setChangeListener(new FieldChangeListener(){

			public void fieldChanged(Field field, int context) {
				_tableTitle.setText(_table.getText() + "号餐台信息");
			}
			
		});
		//hfm1.add(_table);
		vfm.add(_table);
		
		_customNum = new EditField("人数：", Integer.toString(customNum), 
									2, TextField.NO_NEWLINE | EditField.FILTER_NUMERIC){
			protected boolean navigationClick(int status, int time){
				return true;
			}
		};		
		//hfm1.add(_customNum);
		vfm.add(_customNum);
		
		//vfm.add(hfm1);
		
		vfm.add(new SeparatorField());
		vfm.add(_listTitle);
		
		_orderListField = new OrderListField();
		_orderListField.setChangeListener(new FieldChangeListener() {
			
			public void fieldChanged(Field field, int context) {
				/**
				 * Change the amount of order food and total price as any changes to the food
				 */
				if(_orderListField.getSize() != 0){
					_listTitle.setText("已点菜" + "(" + _orderListField.getSize() + ")");
				}else{				
					_listTitle.setText("已点菜");
				}
				_totalPrice.setText("小计：" + Util.CURRENCY_SIGN + Util.float2String(_orderListField.calcPrice()));
			}
		});
		
		vfm.add(_orderListField);
		vfm.add(new SeparatorField());
		add(vfm);
		
		_totalPrice = new LabelField("小计：" + Util.CURRENCY_SIGN + "0.00", LabelField.USE_ALL_WIDTH | DrawStyle.RIGHT);
		add(_totalPrice);
		add(new SeparatorField());
		
		
		//Three buttons would be shown in the bottom of the screen
		ButtonField byNoBtn = new ButtonField("编号", ButtonField.CONSUME_CLICK);
		ButtonField byKitchenBtn = new ButtonField("分厨", ButtonField.CONSUME_CLICK);
		ButtonField byPinyin = new ButtonField("拼音", ButtonField.CONSUME_CLICK);
		ButtonField submit = new ButtonField("提交", ButtonField.CONSUME_CLICK);
		HorizontalFieldManager hfm2 = new HorizontalFieldManager(Manager.HORIZONTAL_SCROLL | Manager.FIELD_HCENTER);
		hfm2.add(byNoBtn);
		hfm2.add(new LabelField(""));
		hfm2.add(byKitchenBtn);
		hfm2.add(new LabelField(""));
		hfm2.add(byPinyin);
		hfm2.add(new LabelField(""));
		hfm2.add(submit);
		add(hfm2);
		
		//Set the listener to order by no button
		byNoBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
	            UiApplication.getUiApplication().pushScreen(new SelectFoodPopup(_orderListField, WirelessOrder.foodMenu.foods, SelectFoodPopup.BY_FOOD_ID));
	         }
		});
		
		//Set the listener to order by kitchen button
		byKitchenBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				UiApplication.getUiApplication().pushScreen(new SelectKitchenPopup(_orderListField));
			}
		});
		
		//Set the listener to order by pinyin
		byPinyin.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
	            UiApplication.getUiApplication().pushScreen(new SelectFoodPopup(_orderListField, WirelessOrder.foodMenu.foods, SelectFoodPopup.BY_PINYIN));
	         }
		});
		
		//Set the submit button's listener
		submit.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				if(_orderListField.getSize() == 0){
					Dialog.alert("客人还未点菜，暂时不能下单。");
				}else{

					OrderFood[] foods = new OrderFood[_orderListField.getSize()];
					for(int i = 0; i < _orderListField.getSize(); i++){
						foods[i] = (OrderFood)_orderListField.getCallback().get(null, i);
					}
					
					//FIX ME!!! Just for test
					/*
					Food tmpFood = new Food();
					tmpFood.isTemporary = true;
					tmpFood.alias_id = Util.genTempFoodID();
					//tmpFood.alias_id = 1100;
					tmpFood.name = "临时菜";
					tmpFood.hangStatus = Food.FOOD_HANG_UP;
					tmpFood.setPrice(new Float(20.55));
					tmpFood.setCount(new Float(2.35));
					foods[_orderListField.getSize()] = tmpFood;
					*/
					
					Order reqOrder = new Order(foods, 
											   Short.parseShort(_table.getText()), 
											   Integer.parseInt(_customNum.getText()));
					UiApplication.getUiApplication().pushScreen(new SubmitNewPopup(reqOrder, _self));
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
		if(_orderListField.getSize() == 0){
			return super.onClose();
		}else{
			int resp = Dialog.ask(Dialog.D_YES_NO, "点菜单还未提交，确认退出?", Dialog.NO);
			if(resp == Dialog.YES){
				return super.onClose();
			}else{
				return false;
			}
		}		
	}
	
	public void submitOrderPass(){
		UiApplication.getUiApplication().popScreen(_self);
	}
	
	public void submitOrderFail(){
		
	}
}

