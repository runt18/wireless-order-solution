package com.wireless.pojo.stockMgr;

public class StockInDetail {

	private int id;
	private int stockInId;
	private int materialId;
	private float price;
	private float amount;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStockInId() {
		return stockInId;
	}
	public void setStockInId(int stockInId) {
		this.stockInId = stockInId;
	}
	public int getMaterialId() {
		return materialId;
	}
	public void setMaterialId(int materialId) {
		this.materialId = materialId;
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
		if(obj == null || !(obj instanceof StockInDetail)){
			return false;
		}else{
			return stockInId == ((StockInDetail)obj).materialId && materialId == ((StockInDetail)obj).materialId;
		}
		
	}
	
	@Override
	public int hashCode(){
		int result = 17;
		result = result * 31 + stockInId;
		result = result * 31 + materialId;
		return result;
		
	}
	
	public StockInDetail(int id, int stockInId, int materialId, Float price, Float amount){
		this.id = id;
		this.stockInId = stockInId;
		this.materialId = materialId;
		this.price = price;
		this.amount = amount;
	}
	
	
}
