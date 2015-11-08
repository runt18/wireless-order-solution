package com.wireless.pojo.billStatistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.NumericUtil;

public class CouponUsage implements Jsonable {
	
	public static class Usage implements Jsonable{
		private final String name;
		private final int amount;
		private final float price;
		
		Usage(String name, int amount, float price){
			this.name = name;
			this.amount = amount;
			this.price = price;
		}
		
		public int getAmount(){
			return this.amount;
		}
		
		public float getPrice(){
			return NumericUtil.roundFloat(this.price);
		}
		
		public String getName(){
			if(this.name == null){
				return "";
			}
			return this.name;
		}
		
		@Override
		public String toString(){
			return this.name + ", " + this.amount + ", " + this.price;
		}

		@Override
		public JsonMap toJsonMap(int flag) {
			JsonMap jm = new JsonMap();
			jm.putString("name", this.name);
			jm.putInt("amount", this.amount);
			jm.putFloat("price", this.price);
			return jm;
		}

		@Override
		public void fromJsonMap(JsonMap jm, int flag) {
			
		}
	}
	
	private final List<Usage> used = new ArrayList<Usage>();	
	private final List<Usage> issued = new ArrayList<Usage>();
	
	public void addUse(String name, int amount, float price){
		used.add(new Usage(name, amount, price));
	}
	
	public void addIssue(String name, int amount, float price){
		issued.add(new Usage(name, amount, price));
	}
	
	public List<Usage> getUsed(){
		return Collections.unmodifiableList(this.used);
	}
	
	public List<Usage> getIssued(){
		return Collections.unmodifiableList(this.issued);
	}
	
	public int getUsedAmount(){
		int amount = 0;
		for(Usage use : used){
			amount += use.amount;
		}
		return amount;
	}
	
	public float getUsedPrice(){
		float price = 0;
		for(Usage use : used){
			price += use.price;
		}
		return NumericUtil.roundFloat(price);
	}
	
	public int getIssuedAmount(){
		int amount = 0;
		for(Usage issue : issued){
			amount += issue.amount;
		}
		return amount;
	}
	
	public float getIssuedPrice(){
		float price = 0;
		for(Usage issue : issued){
			price += issue.price;
		}
		return NumericUtil.roundFloat(price);
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		if(!this.used.isEmpty()){
			jm.putJsonableList("used", this.used, 0);
		}
		if(!this.issued.isEmpty()){
			jm.putJsonableList("issued", this.issued, 0);
		}
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
	}
	
	@Override
	public String toString(){
		return "used coupons : " + this.used + System.getProperty("line.separator") +
			   "issued coupons : " + this.issued;
	}
}
