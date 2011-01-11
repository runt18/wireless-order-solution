package com.wireless.terminal;

import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.*;

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
  		   Food _food = (Food)_orderFoods.elementAt(getSelectedIndex());
		   int response = Dialog.ask(Dialog.D_DELETE, "确认删除-" + _food.name.toString(), Dialog.CANCEL);
		   if(response == Dialog.DELETE){
			   _orderFoods.removeElementAt(getSelectedIndex());
			   delete(getSelectedIndex());
		   }
		   return true;
	   }else{
		   return super.navigationClick(status, time);
	   }
    }		

    public void insertFood(Food orderFood){
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
		Food _orderFood = (Food)_orderList.elementAt(index);	
		String nameAndCount = _orderFood.name + "(" + _orderFood.count2String() + ")";		
		g.drawText(nameAndCount, 0, y, 0, w - lenUnitPrice);
		g.drawText(_orderFood.price2StringEx(), w - lenUnitPrice, y, DrawStyle.RIGHT, lenUnitPrice);		
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
