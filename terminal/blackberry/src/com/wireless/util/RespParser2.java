package com.wireless.util;

import com.wireless.protocol.RespParser;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.terminal.WirelessOrder;

public class RespParser2{
	/**
	 * Parse the response associated with query order request.
	 * And get the food and taste detail from the food menu.
	 * @param response 
	 * the protocol package return from ProtocolConnector's ask() function
	 * @return the order result
	 */
	public static Order parseQueryOrder2(ProtocolPackage response){
		Order order = RespParser.parseQueryOrder(response);
		
		for(int i = 0; i < order.foods.length; i++){
			for(int j = 0; j < WirelessOrder.foodMenu.foods.length; j++){
				if(order.foods[i].alias_id == WirelessOrder.foodMenu.foods[j].alias_id){
					order.foods[i].name = WirelessOrder.foodMenu.foods[j].name;
					order.foods[i].price = WirelessOrder.foodMenu.foods[j].price;
				}
				
				if(order.foods[i].taste.alias_id == WirelessOrder.foodMenu.foods[j].taste.alias_id){
					order.foods[i].taste.preference = WirelessOrder.foodMenu.foods[j].taste.preference;
					order.foods[i].taste.price = WirelessOrder.foodMenu.foods[j].taste.price;
				}				
			}			
		}
		return order;
	}
}
