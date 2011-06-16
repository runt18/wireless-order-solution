package com.wireless.ui.neworder;

import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.ui.field.OrderListField;
import com.wireless.ui.field.SelectFoodPopup;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;



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
		setTitle("改单");
		//The food has ordered would be listed in here.
		VerticalFieldManager _vfm = new VerticalFieldManager();
		_vfm.add(new SeparatorField());
		
		_tableTitle = new LabelField(_originalOrder.table_id + "号餐台信息", LabelField.USE_ALL_WIDTH | DrawStyle.HCENTER){
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

		_vfm.add(_tableTitle);
		
		_table = new EditField("台号：", Integer.toString(_originalOrder.table_id),
							   4, TextField.NO_NEWLINE | EditField.FILTER_NUMERIC);
		
		/**
		 * The id on table title would be changed with the input
		 */
//		_table.setChangeListener(new FieldChangeListener(){
//
//			public void fieldChanged(Field field, int context) {
//				_tableTitle.setText(_table.getText() + "号餐台信息");
//			}
//			
//		});
		_vfm.add(_table);
		
		_customNum = new EditField("人数：", new Integer(_originalOrder.custom_num).toString(), 
									2, TextField.NO_NEWLINE | EditField.FILTER_NUMERIC){
			protected boolean navigationClick(int status, int time){
				return true;
			}
		};
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
		
		_orderListField = new OrderListField(_originalOrder.foods);
		
		_vfm.add(_orderListField);
		_vfm.add(new SeparatorField());
		add(_vfm);
		
		//Three buttons would be shown in the bottom of the screen
		ButtonField _addOrder = new ButtonField("加菜", ButtonField.CONSUME_CLICK);
		ButtonField _submit = new ButtonField("提交", ButtonField.CONSUME_CLICK);
		ButtonField _reset = new ButtonField("清空", ButtonField.CONSUME_CLICK);
		HorizontalFieldManager _hfm = new HorizontalFieldManager(Manager.FIELD_HCENTER);
		_hfm.add(_addOrder);
		_hfm.add(new LabelField("    "));
		_hfm.add(_submit);
		//_hfm.add(new LabelField("    "));
		//_hfm.add(_reset);
		add(_hfm);
		
		//Set the listener to order button
		_addOrder.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
	             UiApplication.getUiApplication().pushScreen(new SelectFoodPopup(_orderListField));
	         }
		});
		//Set the submit button's listener
		_submit.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				if(_orderListField.getSize() == 0){
					Dialog.alert("点菜单为空，暂时不能改单。");
				}else{
					Food[] foods = new Food[_orderListField.getSize()];
					for(int i = 0; i < _orderListField.getSize(); i++){
						foods[i] = (Food)_orderListField.getCallback().get(null, i);
					}
					Order reqOrder = new Order(foods, 
											   Short.parseShort(_table.getText()), 
											   Integer.parseInt(_customNum.getText()));
					reqOrder.originalTableID = _originalOrder.table_id;
					
					UiApplication.getUiApplication().pushScreen(new SubmitChangePopup(reqOrder, _self));
				}
	         }
		});
		//Set the reset button's listener
		_reset.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				if(!_orderListField.isEmpty()){
					int response = Dialog.ask(Dialog.D_YES_NO, "确认清空?");
					if(response == Dialog.YES){
						 _orderListField.removeAll();
					}
				}
	         }
		});		
		
		//Focus on order button
		_addOrder.setFocus();
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
