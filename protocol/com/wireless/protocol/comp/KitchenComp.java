package com.wireless.protocol.comp;

import java.util.Comparator;

import com.wireless.protocol.PKitchen;

public class KitchenComp {

	public final static Comparator<PKitchen> DEFAULT = new Comparator<PKitchen>(){

		@Override
		public int compare(PKitchen o1, PKitchen o2) {
			if(o1.getAliasId() > o2.getAliasId()){
				return 1;
			}else if(o1.getAliasId() < o2.getAliasId()){
				return -1;
			}else{
				return 0;
			}
		}
		
	};
	
	private KitchenComp(){
		
	}
}
