package com.wireless.protocol;

import java.util.Arrays;
import java.util.Comparator;

public class RespParserEx {
	/**
	 * Parse the response associated with query menu request.
	 * @param response the protocol package return from ProtocolConnector's ask() function
	 * @return the vector containing the food instance
	 */
	public static FoodMenu parseQueryMenu(ProtocolPackage response){
		FoodMenu foodMenu = RespParser.parseQueryMenu(response);
		
		Comparator<Taste> tasteComp = new Comparator<Taste>(){
			@Override
			public int compare(Taste taste1, Taste taste2) {
				return taste1.compare(taste2);
			}
		};
		
		Arrays.sort(foodMenu.tastes, tasteComp);
		Arrays.sort(foodMenu.styles, tasteComp);
		Arrays.sort(foodMenu.specs, tasteComp);
		
		for(int i = 0; i < foodMenu.foods.length; i++){
			if(foodMenu.foods[i].popTastes != null){
				for(int j = 0; j < foodMenu.foods[i].popTastes.length; j++){
					int index;
					index = Arrays.binarySearch(foodMenu.tastes, foodMenu.foods[i].popTastes[j], tasteComp);
					if(index >= 0){
						foodMenu.foods[i].popTastes[j] = foodMenu.tastes[index];
						continue;
					}
					
					index = Arrays.binarySearch(foodMenu.styles, foodMenu.foods[i].popTastes[j], tasteComp);
					if(index >= 0){
						foodMenu.foods[i].popTastes[j] = foodMenu.styles[index];
						continue;
					}
					
					index = Arrays.binarySearch(foodMenu.specs, foodMenu.foods[i].popTastes[j], tasteComp);
					if(index >= 0){
						foodMenu.foods[i].popTastes[j] = foodMenu.specs[index];
						continue;
					}
				}
			}
		}		
		
		return foodMenu;
		
	}

}
