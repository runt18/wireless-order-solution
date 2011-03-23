package com.wireless.terminal;

import com.wireless.protocol.Food;
import com.wireless.protocol.Taste;

import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;

public class SelectTastePopup extends PopupScreen{
	
	private OrderListField _orderListField = null;
	private EditField _tasteID = null;
	private Food _selectedFood = null;
	private ListField _tasteList = null;
	private PopupScreen _parent = null;
	int[] _tasteMatchedIdx = null;
	
	public SelectTastePopup(OrderListField orderField, Food food, PopupScreen parent){		
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR), DEFAULT_CLOSE);
		_orderListField = orderField;
		_selectedFood = food;
		_parent = parent;
		
		//initialize the taste matched index
		_tasteMatchedIdx = new int[WirelessOrder.foodMenu.tastes.length];
		for(int i = 0; i < WirelessOrder.foodMenu.tastes.length; i++){
			_tasteMatchedIdx[i] = i;
		}
		
		//the label to show the info about the food and taste preference
		add(new LabelField(food.name + "-" + (food.taste.alias_id == Taste.NO_TASTE ? "无口味" : WirelessOrder.foodMenu.tastes[food.taste.alias_id - 1].preference)));
		//the edit field to select the taste
		_tasteID = new EditField("口味编号: ", "", 20, TextField.NO_NEWLINE | EditField.FILTER_NUMERIC);
		
		//Set the taste ID call back listener
		//the available taste field would filter the taste matching the id
		_tasteID.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				int matchNum = 0;
				for(int i = 0; i < WirelessOrder.foodMenu.tastes.length; i++){
					Taste taste = (Taste)WirelessOrder.foodMenu.tastes[i];
					if(new Integer(taste.alias_id).toString().indexOf(_tasteID.getText()) == 0){
						_tasteMatchedIdx[matchNum] = i;
						matchNum++;
					}
				}
				_tasteList.setSize(matchNum);
				if(matchNum == 1){
					_tasteList.setFocus();
				}
			}
		});
		
		//the list field to display all the taste preference
		_tasteList = new ListField(WirelessOrder.foodMenu.tastes.length){
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
		    
		    //click to select taste preference
		    protected boolean navigationClick(int status, int time){
		  	   if(getSelectedIndex() != -1 && isFocus()){
		  		   addTaste();
				   return true;
			   }else{
				   return super.navigationClick(status, time);
			   }
		    }	
		    
		    //click ENTER to select taste preference
			protected boolean keyChar(char c, int status, int time){
				if(c == Characters.ENTER){
					addTaste();		
					return true;
				}else{
					return super.keyChar(c, status, time);
				}
			}
			
			private void addTaste(){
				if(getSelectedIndex() != -1){
					int resp = Dialog.ask(Dialog.D_YES_NO, "确认口味-" + WirelessOrder.foodMenu.tastes[getSelectedIndex()].preference + " ?", Dialog.YES);
					if(resp == Dialog.YES){
						/**
						 * Since add a taste to the food means another one different the previous,
						 * we might remove the original food first
						 */
						_orderListField._orderFoods.removeElement(_selectedFood);
						//assign the taste id
						_selectedFood.taste.alias_id = (short)(getSelectedIndex() + 1);
						//assign the taste preference
						try{
							_selectedFood.taste.preference = WirelessOrder.foodMenu.tastes[getSelectedIndex()].preference;
						}catch(ArrayIndexOutOfBoundsException e){}
						_orderListField.addFood(_selectedFood);
						close();
						_parent.close();
					}
				}
				
			}
		};
		
		//set the call back function to taste list field so as to show taste preference which has been filtered
		_tasteList.setCallback(new ListFieldCallback(){
			// draw the current row
		    public void drawListRow(ListField list, Graphics g, int index, int y, int w){
	    		Taste taste = (Taste)WirelessOrder.foodMenu.tastes[_tasteMatchedIdx[index]];	
		    	g.drawText(taste.preference, 0, y, 0, w);
		    }
		    
		    // get the selected index from the correct Vector
		    public Object get(ListField list, int index) {
	    		return WirelessOrder.foodMenu.tastes[_tasteMatchedIdx[index]];
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
		
		
		add(_tasteID);
		add(new SeparatorField());
		add(_tasteList);	
		add(new SeparatorField());
		add(cancelBtn);
		
	}
}
