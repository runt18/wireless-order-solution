package com.wireless.protocol.comp;

import java.util.Comparator;

import com.wireless.pojo.tasteMgr.Taste;

public class TasteComp{

	public final static Comparator<Taste> DEFAULT = new Comparator<Taste>(){
		@Override
		public int compare(Taste o1, Taste o2) {
			return o1.compareTo(o2);
		}
	};
	
	
	private TasteComp(){
		
	}
	

}
