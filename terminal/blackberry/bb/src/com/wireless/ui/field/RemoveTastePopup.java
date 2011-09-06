package com.wireless.ui.field;

import java.util.Vector;

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
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.Comparator;

import com.wireless.protocol.Food;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Util;

public class RemoveTastePopup extends PopupScreen {
	
	private OrderListField _orderField = null;
	private Food _selectedFood = null;
	private Taste[] _tastes = null;
	
	public RemoveTastePopup(OrderListField orderField, Food selectedFood){
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR), DEFAULT_CLOSE);
		
		_orderField = orderField;
		_selectedFood = selectedFood;
		
		_tastes = getTastes();

		
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
				int resp = Dialog.ask(Dialog.D_YES_NO, "确认删除-" + _tastes[getSelectedIndex()].preference, Dialog.NO);
				if(resp == Dialog.YES){
					
					/**
					 * Since add a taste to the food means another one different the previous,
					 * we might remove the original food first
					 */
					_orderField.delFood(_selectedFood);
					
					_tastes[getSelectedIndex()] = new Taste();
					
					/**
					 * Reassign the tastes to selected food
					 */
					for(int i = 0; i < _selectedFood.tastes.length; i++){
						if(i < _tastes.length){
							_selectedFood.tastes[i] = _tastes[i];
						}else{
							_selectedFood.tastes[i] = new Taste();
						}
					}
					
					/**
					 * Sort the tastes to selected food
					 */
					Arrays.sort(_tastes, new Comparator(){

						public int compare(Object o1, Object o2) {
							Taste taste1 = (Taste)o1;
							Taste taste2 = (Taste)o2;
							if(taste1.alias_id == taste2.alias_id){
								return 0;
							}else if(taste1.alias_id == Taste.NO_TASTE){
								return 1;
							}else if(taste2.alias_id == Taste.NO_TASTE){
								return -1;
							}else if(taste1.alias_id > taste2.alias_id){
								return 1;
							}else if(taste1.alias_id < taste2.alias_id){
								return -1;
							}else{
								return 0;
							}
						}
						
					});
					
					/**
					 * Redraw the screen after remove the taste
					 */
					_tastes = getTastes();
					setSize(_tastes.length, _tastes.length);
					
					/**
					 * Calculate the taste price and preference
					 */
					_selectedFood.tastePref = Util.genTastePref(_selectedFood.tastes);
					_selectedFood.setTastePrice(Util.genTastePrice(_selectedFood.tastes, _selectedFood.getPrice()));	
					
					_orderField.addFood(_selectedFood);
				}
			}
		};
		
		tastesLF.setCallback(new ListFieldCallback(){

			public void drawListRow(ListField listField, Graphics g, int index, int y, int w) {
	    		int priceWidth = 85;
		    	g.drawText(_tastes[index].preference, 0, y, 0, w - priceWidth);
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
	
	/**
	 * Filter the taste that are NOT no preference.
	 * @return the array with the taste preferences
	 */
	private Taste[] getTastes(){
		Vector vectTaste = new Vector();
		for(int i = 0; i < _selectedFood.tastes.length; i++){
			if(_selectedFood.tastes[i].alias_id != Taste.NO_TASTE){
				vectTaste.addElement(_selectedFood.tastes[i]);
			}
		}
		Taste[] tastes = new Taste[vectTaste.size()];
		vectTaste.copyInto(tastes);
		return tastes;
	}
}
