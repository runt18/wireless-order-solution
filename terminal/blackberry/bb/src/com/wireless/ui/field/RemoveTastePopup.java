package com.wireless.ui.field;

import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Util;

public class RemoveTastePopup extends PopupScreen {
	
	private OrderListField _orderField = null;
	private OrderFood _selectedFood = null;
	private Taste[] _tastes = null;
	
	public RemoveTastePopup(OrderListField orderField, OrderFood selectedFood){
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR), DEFAULT_CLOSE);
		
		_orderField = orderField;
		_selectedFood = selectedFood;
		
		_tastes = _selectedFood.hasTaste() ? _selectedFood.tasteGroup.getNormalTastes() : new Taste[0];

		
		add(new LabelField("选择要删除的口味"));
		add(new SeparatorField());
		
		ListField tastesLF = new ListField(_tastes.length){
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
		  		   delTaste();
				   return true;
			   }else{
				   return super.navigationClick(status, time);
			   }
		    }	
		    
		    //Click ENTER to show all the taste to the type selected 
			protected boolean keyChar(char c, int status, int time){
				if(c == Characters.ENTER || c == Characters.BACKSPACE){
					delTaste();			
					return true;
				}else{
					return super.keyChar(c, status, time);
				}
			}
			
			private void delTaste(){
				int resp = Dialog.ask(Dialog.D_YES_NO, "确认删除-" + _tastes[getSelectedIndex()].getPreference(), Dialog.NO);
				if(resp == Dialog.YES){
					
					_selectedFood.tasteGroup.removeTaste(_tastes[getSelectedIndex()]);
					//_orderField.setSize(_orderField.getSize(), _orderField.getSelectedIndex());
					_orderField.invalid(_selectedFood);
					
					/**
					 * Redraw the remove taste pop up screen after removing the taste
					 */
					_tastes = _selectedFood.hasTaste() ? _selectedFood.tasteGroup.getNormalTastes() : new Taste[0];
					setSize(_tastes.length, _tastes.length);
					

				}
			}
		};
		
		tastesLF.setCallback(new ListFieldCallback(){

			public void drawListRow(ListField listField, Graphics g, int index, int y, int w) {
	    		int priceWidth = 85;
		    	g.drawText(_tastes[index].getPreference(), 0, y, 0, w - priceWidth);
		    	if(_tastes[index].calc == Taste.CALC_PRICE){
		    		g.drawText(Util.CURRENCY_SIGN + Util.float2String(_tastes[index].getPrice()), w - priceWidth, y, DrawStyle.RIGHT, priceWidth);
		    		
		    	}else if(_tastes[index].calc == Taste.CALC_RATE){
		    		g.drawText(Util.float2Int(_tastes[index].getRate()) + "%", w - priceWidth, y, DrawStyle.RIGHT, priceWidth);
		    		
		    	}else{
		    		g.drawText(Util.CURRENCY_SIGN + Util.float2String(_tastes[index].getPrice()), w - priceWidth, y, DrawStyle.RIGHT, priceWidth);
		    	}
			}

			public Object get(ListField listField, int index) {
				return _tastes[index];
			}

			public int getPreferredWidth(ListField listField) {
				return Display.getWidth();
			}

			public int indexOfList(ListField listField, String prefix, int start) {
				return start;
			}
			
		});
		add(tastesLF);
		
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
