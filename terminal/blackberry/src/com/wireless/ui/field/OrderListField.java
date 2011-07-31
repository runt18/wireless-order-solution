package com.wireless.ui.field;

import java.util.Enumeration;
import java.util.Vector;

import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;

import com.wireless.protocol.Food;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.terminal.WirelessOrder;

public class OrderListField extends ListField{
	
	Vector _orderFoods;	
	private boolean _isFocused = false;
	private byte _type = Type.INSERT_ORDER;
	
	public OrderListField(){
		_orderFoods = new Vector();
		setCallback(new OrderFieldCallback(_orderFoods));
	}
	
	public OrderListField(Food[] foods, byte type){
		_orderFoods = new Vector();
		for(int i = 0; i < foods.length; i++){
			_orderFoods.addElement(foods[i]);
		}
		setCallback(new OrderFieldCallback(_orderFoods));
		setSize(_orderFoods.size());	
		_type = type;
	}

	public OrderListField(Food[] foods, long style, byte type){
		super(0, style);
		_orderFoods = new Vector();
		for(int i = 0; i < foods.length; i++){
			_orderFoods.addElement(foods[i]);
		}
		setCallback(new OrderFieldCallback(_orderFoods));
		setSize(_orderFoods.size());	
		_type = type;
	}
	
    /**
     * Select the first top item if the focus comes from previous field,
     * and select the last item if the focus comes from subsequent field
     */
    public void onFocus(int direction){
        _isFocused = true;

        if(direction == 1){
        	setSelectedIndex(0);
        }else if(direction == -1){
        	setSelectedIndex(_orderFoods.size());
        }
        
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
    
    /**
     * Tow shortcut to order list field
     * 1 - 'Del' for removing the selected food
     * 2 - 'Space' for adding the taste preference
     */
    protected boolean keyChar(char key, int status, int time){
    	if(key == Characters.BACKSPACE && getSelectedIndex() != -1){

    		int resp;
			/**
			 * In the case of "改单"，ask operator for permission before cancel a food
			 */
    		if(_type == Type.UPDATE_ORDER){

    			if(WirelessOrder.restaurant.pwd2 != null){
    				if(WirelessOrder.restaurant.pwd2.length() != 0){
    					resp = VerifyPwd.ask(VerifyPwd.PWD_3);
    				}else{
    					resp = VerifyPwd.VERIFY_PASS;
    				}
    			}else{
    				resp = VerifyPwd.VERIFY_PASS;
    			}
    			
    		}else if(_type == Type.INSERT_ORDER){  
    			resp = VerifyPwd.VERIFY_PASS;
    			
    		}else{
    			resp = VerifyPwd.VERIFY_PASS;
    		}
    		if(resp == VerifyPwd.VERIFY_PASS){
    			UiApplication.getUiApplication().pushModalScreen(new RemoveFoodPopup(this, 
    													(Food)_orderFoods.elementAt(getSelectedIndex()), 
    													_type, null));
    			
    		}else if(resp == VerifyPwd.VERIFY_FAIL){
    			Dialog.alert("你输入的权限密码不正确");
    		}

 		   return true;
    		
    	}else if((key == Characters.SPACE) && getSelectedIndex() != -1){
    		UiApplication.getUiApplication().pushScreen(new SelectTastePopup(this, (Food)_orderFoods.elementAt(getSelectedIndex()), null));
    		return true;
    		
    	}else{
    		return false;
    	}
    	
    }

    
    //when click the any ordered food, prompt operator whether to cancel this food 
    protected boolean navigationClick(int status, int time){
  	   if(getSelectedIndex() != -1 && isFocus()){
  		   UiApplication.getUiApplication().pushScreen(new OpeFoodPopup(this, _type));
		   return true;
	   }else{
		   return super.navigationClick(status, time);
	   }
    }		

    /**
     * Accumulate the amount of order if both food and taste alias id is matched,
     * otherwise, add the food to the order list.
     * @param orderFood the food to add
     */
    public void addFood(Food orderFood){
    	boolean isExist = false;
    	Enumeration e = _orderFoods.elements();
    	while(e.hasMoreElements()){
    		Food food = (Food)e.nextElement();
    		if(orderFood.alias_id == food.alias_id && orderFood.tastes[0].alias_id == food.tastes[0].alias_id){    			
    			int count = Util.float2Int(food.getCount()) + Util.float2Int(orderFood.getCount());
    			int integer = count / 100;
    			if(integer > 255){
    				integer = 255;
    				Dialog.alert(orderFood.name + "-最多只能点255份");
    			}
    			food.setCount(Util.int2Float(count));
    			isExist = true;
    			break;
    		}
    	}
    	if(!isExist){
    		_orderFoods.addElement(orderFood);
    	}
		setSize(_orderFoods.size(), _orderFoods.size());
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
		int priceWidth = 85;
		Food orderFood = (Food)_orderList.elementAt(index);	
		String taste = null;
		if(!orderFood.tastePref.equals(Taste.NO_PREFERENCE)){
			taste = "-" + orderFood.tastePref;
		}else{
			taste = "";
		}
		String nameAndCount = orderFood.name + taste + "(" + Util.float2String2(orderFood.getCount()) + ")";		
		String status = "";
		if(orderFood.isSpecial()){
			status = "特";
		}
		if(orderFood.isRecommend()){
			if(status.length() == 0){
				status = "荐";
			}else{
				status = status + ",荐";
			}
		}
		if(orderFood.isGift()){
			if(status.length() == 0){
				status = "赠";
			}else{
				status = status + ",赠";
			}
		}
		if(status.length() != 0){
			status = "(" + status + ")";
		}
		g.drawText(nameAndCount + status, 0, y, 0, w - priceWidth);
		g.drawText(Util.CURRENCY_SIGN + Util.float2String(orderFood.totalPrice2()), w - priceWidth, y, DrawStyle.RIGHT, priceWidth);		
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

