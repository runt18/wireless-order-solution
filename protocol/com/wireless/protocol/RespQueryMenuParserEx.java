package com.wireless.protocol;

import java.util.Arrays;
import java.util.Comparator;

public class RespQueryMenuParserEx {	
	/**
	 * Parse the response associated with query menu request.
	 * @param response the protocol package return from ProtocolConnector's ask() function
	 * @return the vector containing the food instance
	 */
	public static FoodMenu parse(ProtocolPackage response){
		FoodMenu foodMenu = RespQueryMenuParser.parse(response);
		
		Comparator<Taste> tasteComp = new Comparator<Taste>(){
			@Override
			public int compare(Taste taste1, Taste taste2) {
				return taste1.compare(taste2);
			}
		};
		
		Arrays.sort(foodMenu.tastes, tasteComp);
		Arrays.sort(foodMenu.styles, tasteComp);
		Arrays.sort(foodMenu.specs, tasteComp);
		
		Comparator<Food> foodComp = new Comparator<Food>(){
			@Override
			public int compare(Food arg0, Food arg1) {
				if(arg0.mAliasId > arg1.mAliasId){
					return 1;
				}else if(arg0.mAliasId < arg1.mAliasId){
					return -1;
				}else{
					return 0;
				}
			}			
		};
		
		Arrays.sort(foodMenu.foods, foodComp);
		for(int i = 0; i < foodMenu.foods.length; i++){
	
			//Generate the pinyin to each food
			foodMenu.foods[i].setPinyin(PinyinUtil.cn2Spell(foodMenu.foods[i].mName));
			foodMenu.foods[i].setPinyinShortcut(PinyinUtil.cn2FirstSpell(foodMenu.foods[i].mName));
			
			if(foodMenu.foods[i].isCombo()){
				for(int j = 0; j < foodMenu.foods[i].childFoods.length; j++){
					int index = Arrays.binarySearch(foodMenu.foods, foodMenu.foods[i].childFoods[j], foodComp);
					if(index >= 0){
						foodMenu.foods[i].childFoods[j] = foodMenu.foods[index];
					}
				}
			}
			
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
