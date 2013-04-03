package com.wireless.protocol.comp;

import java.util.Comparator;

import com.wireless.protocol.PDepartment;

public class DeptComp {

	public final static Comparator<PDepartment> DEFAULT = new Comparator<PDepartment>(){

		@Override
		public int compare(PDepartment o1, PDepartment o2) {
			if(o1.getId() > o2.getId()){
				return 1;
			}else if(o1.getId() < o2.getId()){
				return -1;
			}else{
				return 0;
			}
		}
		
	};
	
	private DeptComp(){
		
	}
}
