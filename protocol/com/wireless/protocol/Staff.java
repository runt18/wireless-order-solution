package com.wireless.protocol;

public class Staff {
	public long id;
	public String name;
	public String pwd;
	public int pin;
	
	public Staff(){
		
	}
	
	public Staff(long sID, String sName, String sPwd, int sPin){
		id = sID;
		name = sName;
		pwd = sPwd;
		pin = sPin;
	}
}
