package com.wireless.pojo.stockMgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;

public class StockActionDetail implements Jsonable{

	private int id;
	private int stockActionId;
	private int materialId;
	private String name;
	private float price;
	private float amount;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getStockInId() {
		return stockActionId;
	}
	
	public void setStockInId(int stockInId) {
		this.stockActionId = stockInId;
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
		return "sdetail:id=" + getId() + "stockId=" + getStockInId() + "materialId" + getMaterialId() + "amount" + getAmount();
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
	
	public StockActionDetail(int materialId, String name, float price, float amount){
		this.materialId = materialId;
		this.name = name;
		this.price = price;
		this.amount = amount;
	}
	
	public StockActionDetail(int id, int stockInId){
		this.id = id;
		this.stockActionId = stockInId;
		
	}
	
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("id", this.getId());
		jm.put("stockInId", this.getStockInId());
		jm.put("materialId", this.getMaterialId());
		jm.put("materialName", this.getName());
		jm.put("price", this.getPrice());
		jm.put("amount", this.getAmount());
		return Collections.unmodifiableMap(jm);
	}
	@Override
	public List<Object> toJsonList(int flag) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
