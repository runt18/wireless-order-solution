package com.wireless.protocol.comp;

import java.util.Comparator;

import com.wireless.protocol.Taste;

public class TasteComp implements Comparator<Taste>{

	private final static TasteComp mInstance = new TasteComp();
	
	public static TasteComp instance(){
		return mInstance;
	}
	
	@Override
	public int compare(Taste o1, Taste o2) {
		return o1.compareTo(o2);
	}

}
