package com.wireless.pojo.system;

public class Region {
	private int id;
	private String name;
	private int restaurantID;
	
	public Region(){}
	
	public Region(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public Region(int id, String name, int restaurantID){
		this.id = id;
		this.name = name;
		this.restaurantID = restaurantID;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	@Override
	public int hashCode(){
		return id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Region)){
			return false;
		}else{
			return id == ((Region)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return "region(id = " + id + ", name = " + name + ")";
	}
	
}
