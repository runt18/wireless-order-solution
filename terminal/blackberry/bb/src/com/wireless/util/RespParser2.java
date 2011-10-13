package com.wireless.util;

import com.wireless.protocol.RespParser;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Util;
import com.wireless.terminal.WirelessOrder;

public class RespParser2{
	/**
	 * Parse the response associated with query order request.
	 * And get the food name, price and taste name, price from the food menu.
	 * @param response 
	 * the protocol package return from ProtocolConnector's ask() function
	 * @return the order result
	 */
	public static Order parseQueryOrder2(ProtocolPackage response){
		Order order = RespParser.parseQueryOrder(response);		

		for(int i = 0; i < order.foods.length; i++){
			if(!order.foods[i].isTemporary){
				//get the food name, unit price and attached kitchen
				for(int j = 0; j < WirelessOrder.foodMenu.foods.length; j++){
					if(order.foods[i].alias_id == WirelessOrder.foodMenu.foods[j].alias_id){
						order.foods[i].name = WirelessOrder.foodMenu.foods[j].name;
						order.foods[i].setPrice(WirelessOrder.foodMenu.foods[j].getPrice());
						order.foods[i].kitchen = WirelessOrder.foodMenu.foods[j].kitchen;
						break;
					}			
				}	
				
				for(int j = 0; j < order.foods[i].tastes.length; j++){

					//search and get the taste match the alias id
					Taste taste = srchTaste(order.foods[i].tastes[j].alias_id, WirelessOrder.foodMenu.tastes);
					if(taste != null){
						order.foods[i].tastes[j] = taste;
						continue;
					}
					
					//search and get the style match the alias id
					Taste style = srchTaste(order.foods[i].tastes[j].alias_id, WirelessOrder.foodMenu.styles);
					if(style != null){
						order.foods[i].tastes[j] = style;
						continue;
					}
					
					//search and get the specification match the alias id
					Taste spec = srchTaste(order.foods[i].tastes[j].alias_id, WirelessOrder.foodMenu.specs);
					if(spec != null){
						order.foods[i].tastes[j] = spec;
						continue;
					}
				}
				
				//set the taste preference to this food
				order.foods[i].tastePref = Util.genTastePref(order.foods[i].tastes);
				//set the taste total price to this food
				order.foods[i].setTastePrice(Util.genTastePrice(order.foods[i].tastes, order.foods[i].getPrice()));
			}			
		}		

		return order;
	}
	
	private static Taste srchTaste(int aliasID, Taste[] tasteSrc){
		
		if(aliasID != Taste.NO_TASTE){
			Taste taste = null;
			
			for(int i = 0; i < tasteSrc.length; i++){
				if(aliasID == tasteSrc[i].alias_id){
					
					taste = new Taste();
					
					taste.alias_id = tasteSrc[i].alias_id;
					taste.preference = tasteSrc[i].preference;
					taste.setPrice(tasteSrc[i].getPrice());
					taste.calc = tasteSrc[i].calc;
					taste.category = tasteSrc[i].category;
					taste.setRate(tasteSrc[i].getRate());

					break;
				}
			}
			
			return taste;
			
		}else{
			return null;
		}
	}
}
