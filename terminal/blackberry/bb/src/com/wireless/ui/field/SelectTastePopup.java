package com.wireless.ui.field;

import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.TasteGroup;
import com.wireless.protocol.Util;
import com.wireless.terminal.PlatformInfo;
import com.wireless.terminal.WirelessOrder;

public class SelectTastePopup extends PopupScreen{
	
	private Screen _self = this;
	private OrderListField _orderListField = null;
	private EditField _tasteID = null;
	private Taste[] _tastes = null;
	private OrderFood _selectedFood = null;
	private ListField _tasteList = null;
	int[] _tasteMatchedIdx = null;
	String _cate = null;
	
	public SelectTastePopup(OrderListField orderField, Taste[] tastes, OrderFood food){		
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR), DEFAULT_CLOSE);
		_orderListField = orderField;
		_tastes = tastes;
		_selectedFood = food;

		
		//initialize the taste matched index
		_tasteMatchedIdx = new int[_tastes.length];
		for(int i = 0; i < _tastes.length; i++){
			_tasteMatchedIdx[i] = i;
		}
	
		if(tastes[0].category == Taste.CATE_TASTE){
			_cate = "口味";
		}else if(tastes[0].category == Taste.CATE_STYLE){
			_cate = "做法";
		}else if(tastes[0].category == Taste.CATE_SPEC){
			_cate = "规格";
		}else{
			_cate = "口味";
		}
		
		//the label to show the info about the food and taste preference
		add(new LabelField("请选择\"" + food.name + "\"的" + _cate, DrawStyle.ELLIPSIS));
		//the edit field to select the taste
		_tasteID = new EditField(_cate + "编号: ", "", 20, TextField.NO_NEWLINE | EditField.FILTER_NUMERIC);
		
		//Set the taste ID call back listener
		//the available taste field would filter the taste matching the id
		_tasteID.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				int matchNum = 0;
				for(int i = 0; i < _tastes.length; i++){
					Taste taste = (Taste)_tastes[i];
					if(Integer.toString(taste.aliasID).startsWith(_tasteID.getText())){
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
		_tasteList = new ListField(_tastes.length){
			private boolean _isFocused = false;
		    //Invoked when this field receives the focus.
		    public void onFocus(int direction){
		    	WirelessOrder.pfInfo.setVKeyBoard(_self, PlatformInfo.HIDE);
		        _isFocused = true;
		        super.onFocus(direction);
		    }

		    //Invoked when a field loses the focus.
		    public void onUnfocus(){
		    	WirelessOrder.pfInfo.setVKeyBoard(_self, PlatformInfo.SHOW);
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
					int resp = Dialog.ask(Dialog.D_YES_NO, "确认" + _cate + "-" + _tastes[_tasteMatchedIdx[getSelectedIndex()]].getPreference() + " ?", Dialog.YES);
					if(resp == Dialog.YES){
						if(!_selectedFood.hasTaste()){
							_selectedFood.tasteGroup = new TasteGroup();
						}
						_selectedFood.tasteGroup.addTaste(_tastes[_tasteMatchedIdx[getSelectedIndex()]]);
						//_orderListField.setSize(_orderListField.getSize(), _orderListField.getSelectedIndex());
						_orderListField.invalid(_selectedFood);
					}
				}
				
			}
		};
		
		//set the call back function to taste list field so as to show taste preference which has been filtered
		_tasteList.setCallback(new ListFieldCallback(){
			// draw the current row
		    public void drawListRow(ListField list, Graphics g, int index, int y, int w){
	    		Taste taste = (Taste)_tastes[_tasteMatchedIdx[index]];
	    		int priceWidth = 85;
		    	g.drawText(taste.getPreference(), 0, y, 0, w - priceWidth);
		    	if(taste.calc == Taste.CALC_PRICE){
		    		g.drawText(Util.CURRENCY_SIGN + Util.float2String(taste.getPrice()), w - priceWidth, y, DrawStyle.RIGHT, priceWidth);
		    		
		    	}else if(taste.calc == Taste.CALC_RATE){
		    		g.drawText(Util.float2Int(taste.getRate()) + "%", w - priceWidth, y, DrawStyle.RIGHT, priceWidth);
		    		
		    	}else{
		    		g.drawText(Util.CURRENCY_SIGN + Util.float2String(taste.getPrice()), w - priceWidth, y, DrawStyle.RIGHT, priceWidth);
		    	}
		    }
		    
		    // get the selected index from the correct Vector
		    public Object get(ListField list, int index) {
	    		return _tastes[_tasteMatchedIdx[index]];
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
