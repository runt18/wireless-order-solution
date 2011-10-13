package com.wireless.ui.neworder;

import java.util.Vector;

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



public class ChangeOrderScreen extends MainScreen implements PostSubmitOrder{
	
	private OrderListField _oriListField;
	private OrderListField _newListField;
	private LabelField _tableTitle = null;
	private EditField _table = null;
	private EditField _customNum = null;
	private LabelField _totalPrice = null;
	private final Order _originalOrder;
	private ChangeOrderScreen _self = this;
	
	private LabelField _oriListTitle = new LabelField("已点菜", LabelField.USE_ALL_WIDTH | DrawStyle.HCENTER){
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
	
	private LabelField _newListTitle = new LabelField("新点菜", LabelField.USE_ALL_WIDTH | DrawStyle.HCENTER){
		protected void paintBackground(Graphics g) {
			g.clear();
			g.setBackgroundColor(Color.GREEN);
			super.paintBackground(g);
		} 
		protected void paint(Graphics g){
			g.clear();
			g.setColor(Color.WHITE);		
			super.paint(g);  
		}
	};
	
	// Constructor
	public ChangeOrderScreen(Order bill){
		_originalOrder = bill;
		
		setBanner(new TopBannerField("改单"));

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
		if(_originalOrder.foods.length != 0){
			_oriListTitle.setText("已点菜" + "(" + _originalOrder.foods.length + ")");
		}
		vfm.add(_oriListTitle);
		
		_oriListField = new OrderListField(_originalOrder.foods, Type.UPDATE_ORDER);	
		_oriListField.setChangeListener(new FieldChangeListener() {
			
			public void fieldChanged(Field field, int context) {
				/**
				 * Change the amount of ordered food and the total price as any changes to the food
				 */
				if(_oriListField.getSize() != 0){
					_oriListTitle.setText("已点菜" + "(" + _oriListField.getSize() + ")");
				}else{				
					_oriListTitle.setText("已点菜");
				}
				int total = Util.float2Int(_oriListField.calcPrice()) + Util.float2Int(_newListField.calcPrice());
				_totalPrice.setText("小计：" + Util.CURRENCY_SIGN + Util.float2String(Util.int2Float(total)));
			}
		});	
		vfm.add(_oriListField);
		
		vfm.add(new SeparatorField());
		vfm.add(_newListTitle);
		
		_newListField = new OrderListField();
		_newListField.setChangeListener(new FieldChangeListener() {
			
			public void fieldChanged(Field field, int context) {
				/**
				 * Change amount of new ordered food and the total price as any changes to the food
				 */
				if(_newListField.getSize() != 0){
					_newListTitle.setText("新点菜" + "(" + _newListField.getSize() + ")");
				}else{				
					_newListTitle.setText("新点菜");
				}
				int total = Util.float2Int(_oriListField.calcPrice()) + Util.float2Int(_newListField.calcPrice());
				_totalPrice.setText("小计：" + Util.CURRENCY_SIGN + Util.float2String(Util.int2Float(total)));
			}
		});
		vfm.add(_newListField);
		
		vfm.add(new SeparatorField());
		add(vfm);
		
		_totalPrice = new LabelField("小计：" + Util.CURRENCY_SIGN + Util.float2String(_oriListField.calcPrice()), 
									LabelField.USE_ALL_WIDTH | DrawStyle.RIGHT);
		add(_totalPrice);
		add(new SeparatorField());		
		
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
	             UiApplication.getUiApplication().pushScreen(new SelectFoodPopup(_newListField, WirelessOrder.foodMenu.foods));
	         }
		});
		
		//Set the listener to order by kitchen button
		byKitchenBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				UiApplication.getUiApplication().pushScreen(new SelectKitchenPopup(_newListField));
			}
		});
		
		//Set the submit button's listener
		submit.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				if(_oriListField.getSize() == 0 && _newListField.getSize() == 0){
					Dialog.alert("点菜单为空，暂时不能改单。");
				}else{
					
					Vector vectFoods = new Vector();					
					
					/**
					 * Combine the foods of original and new list fields. 
					 */
					for(int i = 0; i < _oriListField.getSize(); i++){
						Food originalFood = (Food)_oriListField.getCallback().get(null, i);
						for(int j = 0; j < _newListField.getSize(); j++){
							Food newFood = (Food)_newListField.getCallback().get(null, j);
							if(originalFood.equals(newFood)){
								int count = Util.float2Int(originalFood.getCount()) + Util.float2Int(newFood.getCount());
								originalFood.setCount(Util.int2Float(count));
								break;
							}
						}
						vectFoods.addElement(originalFood);
					}
					
					for(int i = 0; i < _newListField.getSize(); i++){
						Food newFood = (Food)_newListField.getCallback().get(null, i);
						if(!vectFoods.contains(newFood)){
							vectFoods.addElement(newFood);
						}
					}
					
					Food[] foods = new Food[vectFoods.size()];
					vectFoods.copyInto(foods);
					
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
