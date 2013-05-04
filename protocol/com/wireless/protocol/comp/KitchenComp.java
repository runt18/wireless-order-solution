package com.wireless.protocol.comp;

import java.util.Comparator;

import com.wireless.pojo.menuMgr.Kitchen;

/**
 * @deprecated
 * @author Ying.Zhang
 *
 */
public class KitchenComp {

	public final static Comparator<Kitchen> DEFAULT = new Comparator<Kitchen>(){

		@Override
		public int compare(Kitchen o1, Kitchen o2) {
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
