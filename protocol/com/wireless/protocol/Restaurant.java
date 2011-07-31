package com.wireless.protocol;

public class Restaurant {
	public int id;
	public String name;
	public String tele_1;
	public String tele_2;
	public String addr;
	public String info;
	public String owner;
	public String pwd;
	public String pwd2;
	public String pwd3;
	public Setting setting = new Setting();
	
	public Restaurant(){
		this.name = "";
		this.info = "";
		this.owner = "";
	}
	
	public Restaurant(String name){
		this.name = name;
		this.info = "";
		this.owner = "";
	}
	
	public Restaurant(String name, String info, String owner){
		this.name = name;
		this.info = info;
		this.owner = owner;
	}
	/**
	 * The reserved restaurant id
	 */
	public static final int ADMIN = 1;
	public static final int IDLE = 2;
	public static final int DISCARD = 3;
	public static final int RESERVED_1 = 4;
	public static final int RESERVED_2 = 5;
	public static final int RESERVED_3 = 6;
	public static final int RESERVED_4 = 7;
	public static final int RESERVED_5 = 8;
	public static final int RESERVED_6 = 9;
	public static final int RESERVED_7 = 10;
	
}


