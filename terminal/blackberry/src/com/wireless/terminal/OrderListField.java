package com.wireless.terminal;

import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import java.util.*;

import com.wireless.protocol.Food;

public class OrderListField extends ListField{
	
	Vector _orderFoods;	
	private boolean _isFocused = false;
	
	public OrderListField(){
		_orderFoods = new Vector();
		setCallback(new OrderFieldCallback(_orderFoods));
	}
	
	public OrderListField(Food[] foods){
		_orderFoods = new Vector();
		for(int i = 0; i < foods.length; i++){
			_orderFoods.addElement(foods[i]);
		}
		setCallback(new OrderFieldCallback(_orderFoods));
		setSize(_orderFoods.size());		
	}

	public OrderListField(Food[] foods, long style){
		super(0, style);
		_orderFoods = new Vector();
		for(int i = 0; i < foods.length; i++){
			_orderFoods.addElement(foods[i]);
		}
		setCallback(new OrderFieldCallback(_orderFoods));
		setSize(_orderFoods.size());		
	}
	
    //Invoked when this field receives the focus.
    public void onFocus(int direction){
        _isFocused = true;
        super.onFocus(direction);
    }

    //Invoked when a field loses the focus.
    public void onUnfocus(){
        _isFocused = false;
        super.onUnfocus();
    }

    public boolean isFocus(){
        return _isFocused;
    }
    
    //when click the any ordered food, prompt operator whether to cancel this food 
    protected boolean navigationClick(int status, int time){
  	   if(getSelectedIndex() != -1 && isFocus()){
//  		   Food food = (Food)_orderFoods.elementAt(getSelectedIndex());
//		   int response = Dialog.ask(Dialog.D_DELETE, "确认删除-" + food.name, Dialog.CANCEL);
//		   if(response == Dialog.DELETE){
//			   _orderFoods.removeElementAt(getSelectedIndex());
//			   delete(getSelectedIndex());
//		   }
  		   UiApplication.getUiApplication().pushScreen(new OpeFoodPopup(this));
		   return true;
	   }else{
		   return super.navigationClick(status, time);
	   }
    }		

    /**
     * Change the food's taste if the food has been exist.
     * @param orderFood
     */
    public void addFoodByTaste(Food orderFood){
    	Enumeration e = _orderFoods.elements();
    	while(e.hasMoreElements()){
    		Food food = (Food)e.nextElement();
    		if(orderFood.alias_id == food.alias_id){
    			food.taste_id = orderFood.taste_id;
    			break;
    		}
    	}
    }
    
    /**
     * Accumulate the amount of order if the food has been exist,
     * otherwise, add the food to the order list.
     * @param orderFood the food to add
     */
    public void addFoodByCount(Food orderFood){
    	boolean isExist = false;
    	Enumeration e = _orderFoods.elements();
    	while(e.hasMoreElements()){
    		Food food = (Food)e.nextElement();
    		if(orderFood.alias_id == food.alias_id){
    			int decimal = (food.getCount() & 0x000000FF) + (orderFood.getCount() & 0x000000FF);
    			int integer = ((food.getCount() & 0x0000FF00) >> 8) +
    						((orderFood.getCount() & 0x0000FF00) >> 8) +
    						 decimal / 100;
    			if(integer > 255){
    				integer = 255;
    				Dialog.alert(orderFood.name + "-最多只能点255份");
    			}
    			int count = ((integer & 0x000000FF) << 8) | ((decimal & 0x000000FF) % 100);
    			food.setCount(count);
    			isExist = true;
    			break;
    		}
    	}
    	if(!isExist){
    		_orderFoods.addElement(orderFood);
    	}
		setSize(_orderFoods.size());
    }
    
    /**
     * Remove all the food from the list field
     */
    public void removeAll(){
    	int size = getSize();
    	for(int i = 0; i < size; i++){
    		delete(0);
    	}
    	_orderFoods.removeAllElements();
    }
}

class OrderFieldCallback implements ListFieldCallback{
	
	private Vector _orderList;
	
	//Constructor
	OrderFieldCallback(Vector list){
		_orderList = list;		
	}
	
	// draw the current row
    public void drawListRow(ListField list, Graphics g, int index, int y, int w){
		int lenUnitPrice = 85;
		Food orderFood = (Food)_orderList.elementAt(index);	
		String taste = "";
		if(orderFood.taste_id != 0){
			try{
				taste = "-" + WirelessOrder.foodMenu.tastes[orderFood.taste_id - 1].preference;
			}catch(ArrayIndexOutOfBoundsException e){}
		}
		String nameAndCount = orderFood.name + taste + "(" + orderFood.count2String() + ")";		
		g.drawText(nameAndCount, 0, y, 0, w - lenUnitPrice);
		g.drawText(orderFood.price2StringEx(), w - lenUnitPrice, y, DrawStyle.RIGHT, lenUnitPrice);		
    }
    
    // get the selected index from the correct Vector
    public Object get(ListField list, int index) {
    	return _orderList.elementAt(index);
    }
    
    // get the screen width as the preferred width
    public int getPreferredWidth(ListField list) {
        return Display.getWidth();
    }
    
    public int indexOfList(ListField listField, String prefix, int start) {
        // Not a correct implementation - this is really just commented out
        return start;
    }
}

class OpeFoodPopup extends PopupScreen{
	
	private OrderListField _orderList = null;
	private Food _selectedFood = null;
	private PopupScreen _self = this;
	
	OpeFoodPopup(OrderListField parent){
		super(new VerticalFieldManager(Manager.FIELD_HCENTER), DEFAULT_CLOSE);		
		_orderList = parent;
		_selectedFood = (Food)_orderList._orderFoods.elementAt(_orderList.getSelectedIndex());
		add(new LabelField("请选择\"" + _selectedFood.name + "\"的操作", DrawStyle.ELLIPSIS));
		add(new SeparatorField());

		//the button to remove the food from the order list
		ButtonField delBtn = new ButtonField("删除", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		delBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context) {
				   int response = Dialog.ask(Dialog.D_YES_NO, "确认删除-" + _selectedFood.name, Dialog.NO);
				   if(response == Dialog.YES){
					   _orderList._orderFoods.removeElementAt(_orderList.getSelectedIndex());
					   _orderList.delete(_orderList.getSelectedIndex());			
					   close();
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
		
		//the button to add a taste, show up only if taste preferences exist
		if(WirelessOrder.foodMenu.tastes.length != 0){
			ButtonField tasteBtn = new ButtonField("口味", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
			tasteBtn.setChangeListener(new FieldChangeListener(){
				public void fieldChanged(Field field, int context) {
					UiApplication.getUiApplication().pushScreen(new SelectTastePopup(_orderList, _selectedFood, _self));		
				}
			});
			add(tasteBtn);
		}
		
		add(delBtn);
		add(cancelBtn);
	}
	
}
