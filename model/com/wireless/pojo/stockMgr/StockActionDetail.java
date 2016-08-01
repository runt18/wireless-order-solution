package com.wireless.pojo.stockMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class StockActionDetail implements Jsonable{

	private int id;
	private int stockActionId;
	private int materialId;
	private int materialAssociateId;
	private String name;
	private float price;
	private float amount;
	private float remaining;
	private float deptInRemaining;
	private float deptOutRemaining;
	
	public int getMaterialAssociateId() {
		return materialAssociateId;
	}

	public void setMaterialAssociateId(int materialAssociateId) {
		this.materialAssociateId = materialAssociateId;
	}

	public float getRemaining() {
		return remaining;
	}

	public void setRemaining(float remaining) {
		this.remaining = remaining;
	}
	
	public float getDeptInRemaining() {
		return deptInRemaining;
	}

	public void setDeptInRemaining(float deptInRemaining) {
		this.deptInRemaining = deptInRemaining;
	}

	public float getDeptOutRemaining() {
		return deptOutRemaining;
	}

	public void setDeptOutRemaining(float deptOutRemaining) {
		this.deptOutRemaining = deptOutRemaining;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getStockActionId() {
		return stockActionId;
	}

	public void setStockActionId(int stockActionId) {
		this.stockActionId = stockActionId;
	}

	public int getMaterialId() {
		return materialId;
	}
	
	public void setMaterialId(int materialId) {
		this.materialId = materialId;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		if(this.name == null){
			this.name = "";
		}
		return this.name;
	}
	
	public float getPrice() {
		return price;
	}
	
	public void setPrice(float price) {
		this.price = price;
	}
	
	public float getAmount() {
		return amount;
	}
	
	public void setAmount(float amount) {
		this.amount = amount;
	}
	
	@Override
	public String toString(){
		return "stock action detail : " +
			   "id = " + getId() + 
			   ",stockId =" + getStockActionId() + 
			   ",materialId = " + getMaterialId() + 
			   ",amount = " + getAmount();
	}
	
	@Override 
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof StockActionDetail)){
			return false;
		}else{
			return stockActionId == ((StockActionDetail)obj).stockActionId && materialId == ((StockActionDetail)obj).materialId;
		}
		
	}
	
	@Override
	public int hashCode(){
		int result = 17;
		result = result * 31 + stockActionId;
		result = result * 31 + materialId;
		return result;
		
	}
	
	public StockActionDetail(){}
	
	public StockActionDetail(int materialId, float price, float amount){
		this.materialId = materialId;
		this.price = price;
		this.amount = amount;
	}
	
	public StockActionDetail(int id, int stockActionId){
		this.id = id;
		this.stockActionId = stockActionId;
		
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.getId());
		jm.putInt("stockActionId", this.getStockActionId());
		jm.putInt("materialId", this.getMaterialId());
		jm.putString("materialName", this.getName());
		jm.putFloat("price", this.getPrice());
		jm.putFloat("amount", this.getAmount());
		jm.putFloat("remaining", this.getRemaining());
		jm.putInt("associateId", this.materialAssociateId);
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

	
}
