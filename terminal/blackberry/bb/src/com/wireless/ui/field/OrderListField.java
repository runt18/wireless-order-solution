package com.wireless.ui.field;

import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FocusChangeListener;
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

public class OrderListField extends ListField implements FocusChangeListener, ListFieldCallback{
	
	private static final int PRICE_WIDTH = 85;
	
	private Vector _orderFoods = new Vector();	
	private boolean _isFocused = false;
	private byte _type = Type.INSERT_ORDER;
	
	private String _focusedFoodInfo;
	private int _scrollCount = 0;
	private int _currentChar = 0;
    private Timer _scrollTimer;  
    private TimerTask _scrollTimerTask;
	
	
	public OrderListField(){
		super(0, Field.EDITABLE);
		init(null, Type.INSERT_ORDER);
	}
	
	public OrderListField(Food[] foods, byte type){
		super(0, Field.EDITABLE);
		init(foods, type);
	}

	public OrderListField(Food[] foods, long style, byte type){
		super(0, style);
		init(foods, type);
	}
	
	private void init(Food[] foods, byte type){
		if(foods != null){
			for(int i = 0; i < foods.length; i++){
				_orderFoods.addElement(foods[i]);
			}
		}
		setSize(_orderFoods.size());	
		_type = type;
		setCallback(this);
		setFocusListener(this);
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
     * Turn off the timer task while the field is obscured.
     */
    protected void onObscured(){
    	stopScroll();
    }
    
    /**
     * Tow shortcut to order list field
     * 1 - 'Del' for removing the selected food
     * 2 - 'Space' for adding the taste preference
     */
    protected boolean keyChar(char key, int status, int time){
    	if(key == Characters.BACKSPACE && getSelectedIndex() != -1 && isEditable()){
    		
    		int resp;

    		if(_type == Type.UPDATE_ORDER){
    			/**
    			 * In the case of "改单"，ask operator for permission before cancel a food
    			 */
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
    			/**
    			 * In the case of "下单"，cancel a food directly
    			 */
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
    		
    	}else if((key == Characters.SPACE) && getSelectedIndex() != -1 && isEditable()){
    		/**
    		 * Change taste only in insert order screen
    		 */
    		if(_type == Type.INSERT_ORDER){
    			UiApplication.getUiApplication().pushScreen(new SelectTTypePopup(this, (Food)_orderFoods.elementAt(getSelectedIndex())));
    		}
    		return true;
    		
    	}else if((key == Characters.LATIN_SMALL_LETTER_G || key == Characters.LATIN_CAPITAL_LETTER_G) && getSelectedIndex() != -1 && isEditable()){
    		/**
    		 * Remove the taste only in insert order screen
    		 */
    		if(_type == Type.INSERT_ORDER){
    			UiApplication.getUiApplication().pushScreen(new RemoveTastePopup(this, (Food)_orderFoods.elementAt(getSelectedIndex())));
    		}
    		return true;
    		
    	}else if((key == Characters.LATIN_SMALL_LETTER_D || key == Characters.LATIN_CAPITAL_LETTER_D) && getSelectedIndex() != -1){
    		/**
    		 * Show up the food detail screen. 
    		 */
    		UiApplication.getUiApplication().pushScreen(new FoodDetailPopup((Food)_orderFoods.elementAt(getSelectedIndex())));
    		return true;
    		
    	}else if((key == Characters.LATIN_SMALL_LETTER_C || key == Characters.LATIN_CAPITAL_LETTER_C) && getSelectedIndex() != -1){
    		/**
    		 * Hurry food only in update order screen
    		 */
    		if(_type == Type.UPDATE_ORDER){
    			Food food = (Food)_orderFoods.elementAt(getSelectedIndex());
    			if(food.isHurried){
    				int resp = Dialog.ask(Dialog.D_YES_NO, "确认取消催菜" + food.name + "?");
    				if(resp == Dialog.YES){
        				food.isHurried = false;    					
    				}
    			}else{
    				int resp = Dialog.ask(Dialog.D_YES_NO, "确认催菜" + food.name + "?");
    				if(resp == Dialog.YES){
        				food.isHurried = true;    					
    				}
    			}
    		}
    		invalidate(getSelectedIndex());
    		return true;
    		
    	}else if((key == Characters.LATIN_SMALL_LETTER_J || key == Characters.LATIN_CAPITAL_LETTER_J) && getSelectedIndex() != -1){
 
    		if(_type == Type.INSERT_ORDER){
    	   		/**
        		 * There are two hang statuses to switch in the case of "下单",
        		 * - 叫起
        		 * - 取消叫起
        		 */
    			Food food = (Food)_orderFoods.elementAt(getSelectedIndex());
    			if(food.hangStatus == Food.FOOD_NORMAL){
    				int resp = Dialog.ask(Dialog.D_YES_NO, "确认叫起" + food.name + "?");
    				if(resp == Dialog.YES){
    					food.hangStatus = Food.FOOD_HANG_UP;
    				}
    			}else if(food.hangStatus == Food.FOOD_HANG_UP){
    				int resp = Dialog.ask(Dialog.D_YES_NO, "确认取消叫起" + food.name + "?");
    				if(resp == Dialog.YES){
    					food.hangStatus = Food.FOOD_NORMAL;
    				}
    			}
    			
    		}else if(_type == Type.UPDATE_ORDER){
    	   		/**
        		 * There are two hang statuses to switch in the case of "下单",
        		 * - 叫起
        		 * - 即起
        		 */
    			Food food = (Food)_orderFoods.elementAt(getSelectedIndex());
    			if(food.hangStatus == Food.FOOD_HANG_UP){
    				int resp = Dialog.ask(Dialog.D_YES_NO, "确认即起" + food.name + "?");
    				if(resp == Dialog.YES){
    					food.hangStatus = Food.FOOD_IMMEDIATE;
    				}
    			}else if(food.hangStatus == Food.FOOD_IMMEDIATE){
    				int resp = Dialog.ask(Dialog.D_YES_NO, "确认重新叫起" + food.name + "?");
    				if(resp == Dialog.YES){
    					food.hangStatus = Food.FOOD_HANG_UP;
    				}
    			}
    		}
    		invalidate(getSelectedIndex());
    		return true;
    	}else{
    		return false;
    	}
    	
    }

    
    //when click the any ordered food, prompt operator whether to cancel this food 
    protected boolean navigationClick(int status, int time){
    	if(!isEditable()){
    		return true;
    		
    	}else if(getSelectedIndex() != -1 && isFocus()){
  		    //stopScroll();
  		    UiApplication.getUiApplication().pushScreen(new OpeFoodPopup(this, _type));
		    return true;
		    
	    }else{
		    return super.navigationClick(status, time);
	    }
    }		

    /**
     * Accumulate the amount of order if both food and taste alias id is matched,
     * otherwise, add the food to the order list.
     * @param newFood the food to add
     */
    public void addFood(Food newFood){
    	boolean isExist = false;
    	
    	Enumeration e = _orderFoods.elements();
		
    	/**
    	 * Enumerate to check whether the food to be ordered exist in the original foods 
    	 */
    	while(e.hasMoreElements()){
    		Food food = (Food)e.nextElement();
    		if(newFood.equals(food)){    	
   				
   	   			int count = Util.float2Int(food.getCount()) + Util.float2Int(newFood.getCount());
       			if((count / 100) > 255){
       				Dialog.alert(newFood.name + "-最多只能点255份");
       				food.setCount(new Float(255));
       			}else{
           			food.setCount(Util.int2Float(count));        				
       			}
 
    			isExist = true;
    			break;
    		}
    	}
    	/**
    	 * If the food to be ordered does NOT exist in original food list,
    	 * means to add a new food
    	 */
    	if(!isExist){
    		_orderFoods.addElement(newFood);
    	}
		setSize(_orderFoods.size(), _orderFoods.size());
		stopScroll();
    }
    
    /**
     * Remove the food from the list field
     * @param food the food to delete
     */
    public void delFood(Food food){
    	int index = _orderFoods.indexOf(food);
    	if(index != -1){
    		_orderFoods.removeElement(food);
    		delete(index);
    		stopScroll();
    	}
    }
    
    /**
     * Remove the food according to the index.
     * @param index
     */
    public void delFood(int index){
    	if(index >= 0 && index < getSize()){
    		_orderFoods.removeElementAt(index);
    		delete(index);
    		stopScroll();
    	}
    }
    
    /**
     * Remove all the food from the list field
     */
    public void removeAll(){
    	int size = getSize();
    	for(int i = 0; i < size; i++){
    		delete(0);
    	}
    	stopScroll();
    	_orderFoods.removeAllElements();
    }
    
    /**
     * Get the food according to the index.
     * @param index the index
     * @return the food 
     */
    public Food getFood(int index){
    	return (Food)_orderFoods.elementAt(index);
    }
    
    /**
     * Get the selected food.
     * @return return food selected on list field,
     * 		   return null if no food selected
     */
    public Food getSelectedFood(){
    	if(getSelectedIndex() != -1){
    		return (Food)_orderFoods.elementAt(getSelectedIndex());
    	}else{
    		return null;
    	}
    }

    private String genFoodInfo(Food orderFood){
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
		
		String hangStatus = null;
		if(orderFood.hangStatus == Food.FOOD_HANG_UP){
			hangStatus = "叫";
		}else if(orderFood.hangStatus == Food.FOOD_IMMEDIATE){
			hangStatus = "即";
		}else{
			hangStatus = "";
		}
		if(hangStatus.length() != 0){
			hangStatus = "(" + hangStatus + ")";
		}
		
		String hurriedStatus = null;
		if(orderFood.isHurried){
			hurriedStatus = "(催)";
		}else{
			hurriedStatus = "";
		}
		
		return hangStatus + hurriedStatus + nameAndCount + status;
    }
    
    private void startScroll(){
    	if(isFocus()){
			_focusedFoodInfo = genFoodInfo((Food)_orderFoods.elementAt(getSelectedIndex()));
			if(getFont().getAdvance(_focusedFoodInfo) > getPreferredWidth() - PRICE_WIDTH){
				if(_scrollTimer == null && _scrollTimerTask == null){
				    _scrollTimer = new Timer();  
				    _scrollTimerTask = new TimerTask(){
						public void run() {
							_currentChar = _currentChar + 1; 
						    if(_currentChar > _focusedFoodInfo.length()) {  
						    	_scrollCount++;
				                _currentChar = 0;  
				            } 
						    if(_scrollCount < 3){ 
						    	invalidate();
						    }else{
						    	stopScroll();
						    }
						}    	
				    };
				    _scrollCount = 0;
					_currentChar = 0;
					_scrollTimer.scheduleAtFixedRate(_scrollTimerTask, 500, 500);
				}else{
					_scrollCount = 0;
					_currentChar = 0;
				}
			}
    	}
    }
    
    private void stopScroll(){
		if(_scrollTimer != null){
			_scrollTimerTask.cancel();
			_scrollTimer.cancel();
			_scrollTimerTask = null;
			_scrollTimer = null;
			_focusedFoodInfo = null;
			_scrollCount = 0;
			_currentChar = 0;
			invalidate();
		}
    }
    
    /**
     * The call back function to FocusChangeListener
     */
	public void focusChanged(Field field, int eventType) {
		if(eventType == FOCUS_GAINED || eventType == FOCUS_CHANGED){
			if(_orderFoods.size() != 0){
				String focusedFoodInfo = genFoodInfo((Food)_orderFoods.elementAt(getSelectedIndex()));
				if(getFont().getAdvance(focusedFoodInfo) > getPreferredWidth() - PRICE_WIDTH){
					startScroll();
				}else{
					stopScroll();
				}
			}			
		}else if(eventType == FOCUS_LOST){
			stopScroll();
		}
	}
    
	/**
	 * The call back function to interface ListFieldCallback
	 */
    public void drawListRow(ListField list, Graphics g, int index, int y, int w){
	
		Food orderFood = (Food)_orderFoods.elementAt(index);	

		if(list.getSelectedIndex() == index){
			String currentText = genFoodInfo(orderFood);
	        if(_currentChar < currentText.length()) {  
	            currentText = currentText.substring(_currentChar);  
	        }  
	        g.drawText(currentText, 0, y, DrawStyle.LEFT, w - PRICE_WIDTH); 

		}else{
			g.drawText(genFoodInfo(orderFood), 0, y, DrawStyle.LEFT, w - PRICE_WIDTH);
		}
		
		g.drawText(Util.CURRENCY_SIGN + Util.float2String(orderFood.totalPrice2()), w - PRICE_WIDTH, y, DrawStyle.RIGHT, PRICE_WIDTH);		
    }
    
    /**
     * The call back function to ListFieldCallback.
     * Get the selected index from the correct Vector
     */
    public Object get(ListField list, int index) {
    	return _orderFoods.elementAt(index);
    }
    
    /**
     * The call back function to ListFieldCallback.
     * Get the screen width as the preferred width.
     */
    public int getPreferredWidth(ListField list) {
        return Display.getWidth();
    }
    
    /**
     * The call back function to ListFieldCallback
     */
    public int indexOfList(ListField listField, String prefix, int start) {
        // Not a correct implementation - this is really just commented out
        return start;
    }


	
}



