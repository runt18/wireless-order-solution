package com.wireless.protocol;

public class Restaurant {
	public String name;
	public String info;
	public String owner;
	
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
}
