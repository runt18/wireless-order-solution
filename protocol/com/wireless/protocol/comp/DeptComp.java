package com.wireless.protocol.comp;

import java.util.Comparator;

import com.wireless.pojo.menuMgr.Department;

/**
 * @deprecated
 * @author Ying.Zhang
 *
 */
public class DeptComp {

	public final static Comparator<Department> DEFAULT = new Comparator<Department>(){

		@Override
		public int compare(Department o1, Department o2) {
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
