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

import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.terminal.WirelessOrder;

public class SelectTTypePopup extends PopupScreen {
	
	private OrderListField _orderField = null;
	private OrderFood _selectedFood = null;
	private TasteMap[] _tasteMap = null;
	
	public SelectTTypePopup(OrderListField orderField, OrderFood selectedFood){
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR), DEFAULT_CLOSE);
		
		_orderField = orderField;
		_selectedFood = selectedFood;
		
		Vector tasteMap = new Vector();
		if(WirelessOrder.foodMenu.tastes.length != 0){
			tasteMap.addElement(new TasteMap("口味", WirelessOrder.foodMenu.tastes));
		}
		if(WirelessOrder.foodMenu.styles.length != 0){
			tasteMap.addElement(new TasteMap("做法", WirelessOrder.foodMenu.styles));
		}
		if(WirelessOrder.foodMenu.specs.length != 0){
			tasteMap.addElement(new TasteMap("规格", WirelessOrder.foodMenu.specs));
		}
		
		_tasteMap = new TasteMap[tasteMap.size()];
		tasteMap.copyInto(_tasteMap);
		
		add(new LabelField("选择口味的类型"));
		add(new SeparatorField());
		
		ListField tasteTypeLF = new ListField(_tasteMap.length){
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
		    
		    //click to show all the taste to the type selected  
		    protected boolean navigationClick(int status, int time){
		  	   if(getSelectedIndex() != -1 && isFocus()){
		  		   showTastes();
				   return true;
			   }else{
				   return super.navigationClick(status, time);
			   }
		    }	
		    
		    //Click ENTER to show all the taste to the type selected 
			protected boolean keyChar(char c, int status, int time){
				if(c == Characters.ENTER){
					showTastes();			
					return true;
				}else{
					return super.keyChar(c, status, time);
				}
			}
			
			private void showTastes(){
				UiApplication.getUiApplication().pushScreen(new SelectTastePopup(_orderField, 
										_tasteMap[getSelectedIndex()].tastes,
										_selectedFood));
			}
		};
		
		tasteTypeLF.setCallback(new ListFieldCallback(){

			public void drawListRow(ListField listField, Graphics graphics,	int index, int y, int width) {
				graphics.drawText(_tasteMap[index].name, 0, y, 0, width);			
			}

			public Object get(ListField listField, int index) {
				return _tasteMap[index];
			}

			public int getPreferredWidth(ListField listField) {
				return Display.getWidth();
			}

			public int indexOfList(ListField listField, String prefix, int start) {
				return start;
			}
			
		});
		add(tasteTypeLF);
		
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

class TasteMap{
	
	String name;
	Taste[] tastes;
	
	TasteMap(String name, Taste[] tastes){
		this.name = name;
		this.tastes = tastes;
	}
}
