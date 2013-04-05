package com.wireless.protocol.comp;

import java.util.Comparator;

import com.wireless.protocol.Food;

public class FoodComp{

	public final static Comparator<Food> DEFAULT = new Comparator<Food>(){

		@Override
		public int compare(Food o1, Food o2) {
			if(o1.getAliasId() > o2.getAliasId()){
				return 1;
			}else if(o1.getAliasId() < o2.getAliasId()){
				return -1;
			}else{
				return 0;
			}
		}
		
	};
	
	public final static Comparator<Food> BY_KITCHEN = new Comparator<Food>(){

		@Override
		public int compare(Food o1, Food o2) {
			if (o1.getKitchen().getAliasId() > o2.getKitchen().getAliasId()) {
				return 1;
			} else if (o1.getKitchen().getAliasId() < o2.getKitchen().getAliasId()) {
				return -1;
			} else {
				return 0;
			}
		}
		
	};
	
	public final static Comparator<Food> BY_SALES = new Comparator<Food>(){

		@Override
		public int compare(Food o1, Food o2) {
			if(o1.statistics.orderCnt > o2.statistics.orderCnt){
				return -1;
			}else if(o1.statistics.orderCnt < o2.statistics.orderCnt){
				return 1;
			}else{
				return 0;
			}
		}
		
	};
	
	private FoodComp(){
		
	}

}
