package com.wireless.ui.field;

import com.wireless.protocol.Food;
import com.wireless.protocol.Util;
import com.wireless.terminal.WirelessOrder;

import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;

public class SelectFoodPopup extends PopupScreen{
	private OrderListField _orderListField;
	private EditField _orderNum;
	private EditField _foodID;
	ListField _foodList;
	int[] _foodMatchedIdx;
	    
	public SelectFoodPopup(OrderListField orderField){
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR), DEFAULT_CLOSE);
		_orderListField = orderField;
		_orderNum = new EditField("数量: ", "1", 5, TextField.NO_NEWLINE | EditField.FILTER_REAL_NUMERIC);
		_foodID = new EditField("菜编号: ", "", 20, TextField.NO_NEWLINE | EditField.FILTER_NUMERIC);
		
		_foodList = new ListField(WirelessOrder.foodMenu.foods.length){
			private boolean _isFocused = false;
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
		    
		    //click the selected food and add it to ordered food list 
		    protected boolean navigationClick(int status, int time){
		  	   if(getSelectedIndex() != -1 && isFocus()){
		  		   orderFood();
				   return true;
			   }else{
				   return super.navigationClick(status, time);
			   }
		    }	
		    
		    //Click ENTER to add the selected food to order list
			protected boolean keyChar(char c, int status, int time){
				if(c == Characters.ENTER){
					orderFood();			
					return true;
				}else{
					return super.keyChar(c, status, time);
				}
			}
			
			private void orderFood(){
				Food selectedFood = (Food)WirelessOrder.foodMenu.foods[_foodMatchedIdx[getSelectedIndex()]];
				Food orderFood = new Food(selectedFood.alias_id, selectedFood.name, Util.price2Float(selectedFood.price, Util.INT_MASK_2));
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
					int response = Dialog.ask(Dialog.D_YES_NO, "确认点餐-" + orderFood.name + " ?", Dialog.YES);
					if(response == Dialog.YES){
						_orderListField.addFood(orderFood);
						_orderNum.setFocus();
						_foodID.setFocus();
						_foodID.setText("");
						_orderNum.setText("1");
					}
				}else{
					Dialog.alert("输入数量范围是0.01到99.99");
					_orderNum.setFocus();
				}
			}
		};
		_foodMatchedIdx = new int[WirelessOrder.foodMenu.foods.length];
		for(int i = 0; i < WirelessOrder.foodMenu.foods.length; i++){
			_foodMatchedIdx[i] = i;
		}
		
		//Set the food ID call back listener
		//the available food field would filter the food matching the id
		_foodID.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				int matchNum = 0;
				for(int i = 0; i < WirelessOrder.foodMenu.foods.length; i++){
					Food _food = (Food)WirelessOrder.foodMenu.foods[i];
					if(new Integer(_food.alias_id).toString().indexOf(_foodID.getText()) == 0){
						_foodMatchedIdx[matchNum] = i;
						matchNum++;
					}
				}
				_foodList.setSize(matchNum);
				if(matchNum == 1){
					_foodList.setFocus();
				}
			}
		});
		
		//Set avail food field's listener to show the food's name.
		_foodList.setCallback(new ListFieldCallback(){
			// draw the current row
		    public void drawListRow(ListField list, Graphics g, int index, int y, int w){
	    		Food _orderFood = (Food)WirelessOrder.foodMenu.foods[_foodMatchedIdx[index]];	
		    	g.drawText(_orderFood.name, 0, y, 0, w);
		    }
		    
		    // get the selected index from the correct Vector
		    public Object get(ListField list, int index) {
	    		return WirelessOrder.foodMenu.foods[_foodMatchedIdx[index]];
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
		add(_foodID);
		add(new SeparatorField());
		add(_foodList);
		add(new SeparatorField());
		add(cancelBtn);
		//Focus on foodID button
		_foodID.setFocus();
	}

}
