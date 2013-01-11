package com.wireless.ui.field;

import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.terminal.PlatformInfo;
import com.wireless.terminal.WirelessOrder;

public class SelectFoodPopup extends PopupScreen{
	
	public final static short BY_FOOD_ID = 0;
	public final static short BY_PINYIN = 1;
	
	private Screen _self = this;
	private OrderListField _orderListField;
	private EditField _orderNum;
	private EditField _filter;
	private Food[] _foods;
	private ListField _foodList;
	private int[] _foodMatchedIdx;
	private short _filterType = BY_FOOD_ID;
	    
	/**
	 * Select food pop-up screen.
	 * @param orderField 
	 * 			the order list field the food select to add
	 * @param foods
	 * 			the foods show on the pop-up screen
	 */
	public SelectFoodPopup(OrderListField orderField, Food[] foods, short filterType){
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR), DEFAULT_CLOSE);
		
		_orderListField = orderField;
		_orderNum = new EditField("数量: ", "1", 5, TextField.NO_NEWLINE | EditField.FILTER_REAL_NUMERIC);
		
		_filterType = filterType;
		if(_filterType == BY_FOOD_ID){
			_filter = new EditField("菜编号: ", "", 20, TextField.NO_NEWLINE | EditField.FILTER_NUMERIC);
		}else if(_filterType == BY_PINYIN){
			_filter = new EditField("拼音: ", "", 20, TextField.NO_NEWLINE | EditField.FILTER_LOWERCASE);
		}else{
			_filter = new EditField("菜编号: ", "", 20, TextField.NO_NEWLINE | EditField.FILTER_NUMERIC);
		}
		
		_foods = foods;
		_foodList = new ListField(_foods.length){
			private boolean _isFocused = false;
		    //Invoked when this field receives the focus.
		    public void onFocus(int direction){
		    	//getVirtualKeyboard().setVisibility(VirtualKeyboard.HIDE);
		    	WirelessOrder.pfInfo.setVKeyBoard(_self, PlatformInfo.HIDE);
		        _isFocused = true;
		        super.onFocus(direction);
		    }

		    //Invoked when a field loses the focus.
		    public void onUnfocus(){
		    	//getVirtualKeyboard().setVisibility(VirtualKeyboard.SHOW);
		    	WirelessOrder.pfInfo.setVKeyBoard(_self, PlatformInfo.SHOW);
		        _isFocused = false;
		        super.onUnfocus();
		    }

		    public boolean isFocus(){
		        return _isFocused;
		    }
		    
		    //click the selected food and add it to ordered food list 
		    protected boolean navigationClick(int status, int time){
		  	   if(getSelectedIndex() != -1 && isFocus()){
		  		   selectFood();
				   return true;
			   }else{
				   return super.navigationClick(status, time);
			   }
		    }	
		    
		    //Click ENTER to add the selected food to order list
			protected boolean keyChar(char c, int status, int time){
				if(c == Characters.ENTER){
					selectFood();			
					return true;
				}else{
					return super.keyChar(c, status, time);
				}
			}
			
			private void selectFood(){
				Food selectedFood = _foods[_foodMatchedIdx[getSelectedIndex()]];
				
				if(selectedFood.isSellOut()){
					Dialog.alert(selectedFood.getName() + "已经售完");
					
				}else{
					OrderFood orderFood = new OrderFood(selectedFood);					
					
					String count = _orderNum.getText();
					int pos = count.indexOf(".");
					int integer = -1;
					int decimal = -1;
					if(pos == -1){
						//check if the null situation
						if(count.length() != 0){
							integer = Integer.parseInt(count);
							decimal = 0;
						}
					}else{ 
						//check if only the "." situation
						if(count.length() != 1){
							if(pos == 0){
								integer = 0;
								decimal = Integer.parseInt(count.substring(count.indexOf(".") + 1));
							}else{
								integer = Integer.parseInt(count.substring(0, count.indexOf(".")));
								decimal = Integer.parseInt(count.substring(count.indexOf(".") + 1));
							}
						}
					}
					
					if((integer >= 0 && integer < 100 && decimal >= 0 && decimal < 100) &&
							!(integer == 0 && decimal == 0)){
						orderFood.setCount(Float.valueOf(count));
						int response = Dialog.ask(Dialog.D_YES_NO, "确认点餐-" + orderFood.getName() + " ?", Dialog.YES);
						if(response == Dialog.YES){
							_orderListField.addFood(orderFood);
							_orderNum.setFocus();
							_filter.setFocus();
							_filter.setText("");
							_orderNum.setText("1");
						}
					}else{
						Dialog.alert("输入数量范围是0.01到99.99");
						_orderNum.setFocus();
					}
				}
			}
		};
		
		_foodMatchedIdx = new int[_foods.length];
		for(int i = 0; i < _foods.length; i++){
			_foodMatchedIdx[i] = i;
		}
		
		//Set the food ID call back listener
		//the available food field would filter the food matching the id
		_filter.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				int matchNum = 0;
				for(int i = 0; i < _foods.length; i++){
					if(isMatched(_foods[i])){
						_foodMatchedIdx[matchNum] = i;
						matchNum++;
					}
				}
				_foodList.setSize(matchNum);
				if(matchNum == 1){
					UiApplication.getUiApplication().invokeLater(new Runnable(){
						public void run() {
							_foodList.setFocus();							
						}						
					});
				}
			}
			
			private boolean isMatched(Food food){
				if(_filterType == BY_FOOD_ID){
					return Integer.toString(food.getAliasId()).startsWith(_filter.getText());
				}else if(_filterType == BY_PINYIN){
					if(food.getPinyin() != null){
						return food.getPinyin().toLowerCase().indexOf(_filter.getText()) != -1;
					}else{
						return true;
					}
				}else{
					return true;
				}
			}
		});
		
		//Set avail food field's listener to show the food's name.
		_foodList.setCallback(new ListFieldCallback(){
			// draw the current row
		    public void drawListRow(ListField list, Graphics g, int index, int y, int w){
	    		Food food = _foods[_foodMatchedIdx[index]];
	    		String status = "";
	    		if(food.isSpecial()){
	    			status = "特";
	    		}
	    		if(food.isRecommend()){
	    			if(status.length() == 0){
	    				status = "荐";
	    			}else{
	    				status = status + ",荐";
	    			}
	    		}
	    		if(food.isGift()){
	    			if(status.length() == 0){
	    				status = "赠";
	    			}else{
	    				status = status + ",赠";
	    			}
	    		}
	    		if(food.isSellOut()){
	    			if(status.length() == 0){
	    				status = "停";
	    			}else{
	    				status = status + ",停";
	    			}
	    		}
	    		if(food.isCurPrice()){
	    			if(status.length() == 0){
	    				status = "时";
	    			}else{
	    				status = status + ",时";
	    			}
	    		}
	    		if(status.length() != 0){
	    			status = "(" + status + ")";
	    		}
		    	g.drawText(food.getName() + status, 0, y, 0, w);
		    }
		    
		    // get the selected index from the correct Vector
		    public Object get(ListField list, int index) {
	    		return _foods[_foodMatchedIdx[index]];
		    }
		    
		    // get the screen width as the preferred width
		    public int getPreferredWidth(ListField list) {
		        return Display.getWidth();
		    }
		    
		    public int indexOfList(ListField listField, String prefix, int start) {
		        // Not a correct implementation - this is really just commented out
		        return start;
		    }
		});
		
		ButtonField cancelBtn = new ButtonField("返回", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		cancelBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				close();
			}	
		});
		
		add(_orderNum);
		add(_filter);
		add(new SeparatorField());
		add(_foodList);
		add(new SeparatorField());
		add(cancelBtn);
		//Focus on foodID button
		_filter.setFocus();
	}

}
