package com.wireless.protocol;

public class PMemberType {
	
	private String name;
	private float exchangeRate;
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		if(this.name == null){
			this.name = "";
		}
		return this.name;
	}
	
	public float getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(float exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public String toString(){
		return "member type(name = " + this.name + ")";
	}
	
}
