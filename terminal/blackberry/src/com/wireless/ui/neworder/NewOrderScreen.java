
package com.wireless.ui.neworder;

import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.ui.field.OrderListField;
import com.wireless.ui.field.SelectFoodPopup;

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;

/**
 * The new order screen class.
 */
public class NewOrderScreen extends MainScreen
							implements PostSubmitOrder{

	private OrderListField _orderListField = null;	
	private LabelField _tableTitle = null;
	private EditField _table = null;
	private EditField _customNum = null;
	private short _tableID = 0;
	private NewOrderScreen _self = this;
	
	// Constructor
	public NewOrderScreen(short tableID, int customNum){
		
		setTitle("下单");
	
		//The food has ordered would be listed in here.
		VerticalFieldManager _vfm = new VerticalFieldManager();	
		
		_vfm.add(new SeparatorField());
		_tableID = tableID;
		_tableTitle = new LabelField(_tableID + "号餐台信息", LabelField.USE_ALL_WIDTH | DrawStyle.HCENTER){
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
		_vfm.add(_table);
		
		_customNum = new EditField("人数：", Integer.toString(customNum), 
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
		
		_orderListField = new OrderListField();
		
		_vfm.add(_orderListField);
		_vfm.add(new SeparatorField());
		add(_vfm);
		
		//Three buttons would be shown in the bottom of the screen
		ButtonField _order = new ButtonField("点菜", ButtonField.CONSUME_CLICK);
		ButtonField _submit = new ButtonField("提交", ButtonField.CONSUME_CLICK);
		ButtonField _reset = new ButtonField("清空", ButtonField.CONSUME_CLICK);
		HorizontalFieldManager _hfm = new HorizontalFieldManager(Manager.FIELD_HCENTER);
		_hfm.add(_order);
		_hfm.add(new LabelField("    "));
		_hfm.add(_submit);
		//_hfm.add(new LabelField("    "));
		//_hfm.add(_reset);
		add(_hfm);
		
		//Set the listener to order button
		_order.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
	             UiApplication.getUiApplication().pushScreen(new SelectFoodPopup(_orderListField));
	         }
		});
		//Set the submit button's listener
		_submit.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				if(_orderListField.getSize() == 0){
					Dialog.alert("客人还未点菜，暂时不能下单。");
				}else{
					Food[] foods = new Food[_orderListField.getSize()];
					for(int i = 0; i < _orderListField.getSize(); i++){
						foods[i] = (Food)_orderListField.getCallback().get(null, i);
					}
					Order reqOrder = new Order(foods, 
											   Short.parseShort(_table.getText()), 
											   Integer.parseInt(_customNum.getText()));
					UiApplication.getUiApplication().pushScreen(new SubmitNewPopup(reqOrder, _self));
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
		_order.setFocus();
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

