package com.wireless.pojo.stockMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.inventoryMgr.Material;

public class StockTakeDetail implements Jsonable {

	/**
	 * The helper class to create the StockIn object to perform insert
	 */
	public static class InsertStockTakeDetail{
		private int stockTakeId;
		private Material material = new Material();
		private float actualAmount;
		private float expectAmount;		
		
		public int getStockTakeId() {
			return stockTakeId;
		}

		public InsertStockTakeDetail setStockTakeId(int stockTakeId) {
			this.stockTakeId = stockTakeId;
			return this;
		}

		public Material getMaterial() {
			return material;
		}		

		public InsertStockTakeDetail setMaterial(Material material) {
			this.material = material;
			return this;
		}

		public InsertStockTakeDetail setMaterialId(int id){
			this.material.setId(id);
			return this;
		}
		public InsertStockTakeDetail setMaterialName(String name){
			this.material.setName(name);
			return this;
		}
		
		public float getActualAmount() {
			return actualAmount;
		}
		public InsertStockTakeDetail setActualAmount(float actualAmount) {
			this.actualAmount = actualAmount;
			return this;
		}
		public float getExpectAmount() {
			return expectAmount;
		}
		public InsertStockTakeDetail setExpectAmount(float expectAmount) {
			this.expectAmount = expectAmount;
			return this;
		}
		
		public StockTakeDetail build(){
			return new StockTakeDetail(this);
		}
		
	}
	
	private int id;
	private int stockTakeId;
	private Material material = new Material();
	private float actualAmount;
	private float expectAmount;
	private float deltaAmount;
	
	//消耗差异表字段
	private float primeAmount;
	private float endAmount;
	private float stockInTotal;
	private float stockOutTotal;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStockTakeId() {
		return stockTakeId;
	}
	public void setStockTakeId(int stockTakeId) {
		this.stockTakeId = stockTakeId;
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterialId(int id){
		this.material.setId(id);
	}
	public void setMaterialName(String name){
		this.material.setName(name);
	}
	
	public void setMaterial(Material material) {
		this.material = material;
	}
	public float getActualAmount() {
		return actualAmount;
	}
	public void setActualAmount(float actualAmount) {
		this.actualAmount = actualAmount;
	}
	public float getExpectAmount() {
		return expectAmount;
	}
	public void setExpectAmount(float expectAmount) {
		this.expectAmount = expectAmount;
	}
	public float getDeltaAmount() {
		return deltaAmount;
	}
	public void setDeltaAmount(float deltaAmount) {
		this.deltaAmount = deltaAmount;
	}	
	
	
	
	public float getPrimeAmount() {
		return primeAmount;
	}
	public void setPrimeAmount(float primeAmount) {
		this.primeAmount = primeAmount;
	}
	public float getEndAmount() {
		return endAmount;
	}
	public void setEndAmount(float endAmount) {
		this.endAmount = endAmount;
	}
	public float getStockInTotal() {
		return stockInTotal;
	}
	public void setStockInTotal(float stockInTotal) {
		this.stockInTotal = stockInTotal;
	}
	public float getStockOutTotal() {
		return stockOutTotal;
	}
	public void setStockOutTotal(float stockOutTotal) {
		this.stockOutTotal = stockOutTotal;
	}
	
	
	
	public float getDeltaRate(){
		return this.deltaAmount / this.actualAmount;
	}
	public float getDeltaMoney(){
		return this.deltaAmount * this.material.getPrice();
	}
	
	public StockTakeDetail(){}
	
	public StockTakeDetail(float actualAmount){
		this.actualAmount = actualAmount;
	}
	
	public StockTakeDetail(InsertStockTakeDetail builder){
		setStockTakeId(builder.getStockTakeId());
		setMaterial(builder.getMaterial());
		setActualAmount(builder.getActualAmount());
		setExpectAmount(builder.getExpectAmount());
	}
	
	public float getTotalDelta(){
		return this.actualAmount - this.expectAmount;
	}
	
	@Override
	public String toString(){
		return "StockTakeDetail : id = " + id +
				", stockTakeId = " + stockTakeId +
				", materialId = " + material.getId() +
				", name = " + material.getName() +
				", actualAmount = " + actualAmount +
				", expectAmount = " + expectAmount +
				", delta = " +deltaAmount;
	}
	
	@Override 
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof StockTakeDetail)){
			return false;
		}else{
			return  stockTakeId == ((StockTakeDetail)obj).stockTakeId
					&& material.getId() == ((StockTakeDetail)obj).material.getId();
		}
	}
	
	@Override
	public int hashCode(){
		int result = 17;
		result = result * 31 + id;
		result = result * 31 + stockTakeId;
		return result;
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.getId());
		jm.putJsonable("material", this.material, 0);
		jm.putFloat("actualAmount", this.actualAmount);
		jm.putFloat("expectAmount", this.expectAmount);
		jm.putFloat("deltaAmount", this.deltaAmount);
		jm.putFloat("primeAmount", this.primeAmount);
		jm.putFloat("endAmount", this.endAmount);
		jm.putFloat("stockInTotal", this.stockInTotal);
		jm.putFloat("stockOutTotal", this.stockOutTotal);
		return jm;
	}
	
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	
	
	
}
