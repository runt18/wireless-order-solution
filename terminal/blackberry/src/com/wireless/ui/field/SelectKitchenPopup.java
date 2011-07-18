package com.wireless.ui.field;

import java.util.Vector;

import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.protocol.Food;
import com.wireless.terminal.WirelessOrder;

public class SelectKitchenPopup extends PopupScreen{
	
	private OrderListField _orderField;
	
	public SelectKitchenPopup(OrderListField orderField){
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR), DEFAULT_CLOSE);
		
		_orderField = orderField;
		

		add(new LabelField("选择菜品的厨房"));
		add(new SeparatorField());
		
		ListField kitchenLF = new ListField(WirelessOrder.foodMenu.kitchens.length){
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
		  		   orderByKitchen();
				   return true;
			   }else{
				   return super.navigationClick(status, time);
			   }
		    }	
		    
		    //Click ENTER to add the selected food to order list
			protected boolean keyChar(char c, int status, int time){
				if(c == Characters.ENTER){
					orderByKitchen();			
					return true;
				}else{
					return super.keyChar(c, status, time);
				}
			}
			
			private void orderByKitchen(){
				int nCount = 0;
				Vector vectFoods = new Vector();
				for(int i = 0; i < WirelessOrder.foodMenu.foods.length; i++){
					if(WirelessOrder.foodMenu.foods[i].kitchen == WirelessOrder.foodMenu.kitchens[getSelectedIndex()].alias_id){
						nCount++;
						vectFoods.addElement(WirelessOrder.foodMenu.foods[i]);
					}
				}
				Food[] foods = new Food[nCount];
				vectFoods.copyInto(foods);
				UiApplication.getUiApplication().pushScreen(new SelectFoodPopup(_orderField, foods));
			}
		};
		kitchenLF.setCallback(new ListFieldCallback(){

			public void drawListRow(ListField listField, Graphics graphics,	int index, int y, int width) {
				graphics.drawText(WirelessOrder.foodMenu.kitchens[index].name, 0, y, 0, width);			
			}

			public Object get(ListField listField, int index) {
				return WirelessOrder.foodMenu.kitchens[index];
			}

			public int getPreferredWidth(ListField listField) {
				return Display.getWidth();
			}

			public int indexOfList(ListField listField, String prefix, int start) {
				return start;
			}
			
		});
		add(kitchenLF);
		
		add(new SeparatorField());
		
		ButtonField cancelBtn = new ButtonField("返回", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		cancelBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				close();
			}	
		});
		add(cancelBtn);
	}
}
