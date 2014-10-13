package com.wireless.pojo.stockMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class StockInGeneral implements Jsonable{

	private String name;
	private float avgPrice;
	private float count;
	private float totalMoney;
	private float referencePrice;
	private float maxPrice;
	private float minPrice;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public float getAvgPrice() {
		return avgPrice;
	}
	public void setAvgPrice(float avgPrice) {
		this.avgPrice = avgPrice;
	}
	public float getCount() {
		return count;
	}
	public void setCount(float count) {
		this.count = count;
	}
	public float getTotalMoney() {
		return totalMoney;
	}
	public void setTotalMoney(float totalMoney) {
		this.totalMoney = totalMoney;
	}
	public float getReferencePrice() {
		return referencePrice;
	}
	public void setReferencePrice(float referencePrice) {
		this.referencePrice = referencePrice;
	}
	public float getMaxPrice() {
		return maxPrice;
	}
	public void setMaxPrice(float maxPrice) {
		this.maxPrice = maxPrice;
	}
	public float getMinPrice() {
		return minPrice;
	}
	public void setMinPrice(float minPrice) {
		this.minPrice = minPrice;
	}
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("name", this.name);
		jm.putFloat("referencePrice", this.referencePrice);
		jm.putFloat("count", this.count);
		jm.putFloat("totalMoney", this.totalMoney);
		jm.putFloat("avgPrice", this.avgPrice);
		jm.putFloat("maxPrice", this.maxPrice);
		jm.putFloat("minPrice", this.minPrice);
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	

}
