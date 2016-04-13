package com.wireless.pojo.stockMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.inventoryMgr.Material;

public class StockReport implements Jsonable{

	private Material material = new Material();
	private float primeAmount;
	private float primeMoney;
	private float stockIn;
	private float stockInTransfer;
	private float stockTakeMore;
	private float stockSpill;
	private float stockOut;
	private float stockOutTransfer;
	private float stockTakeLess;
	private float stockDamage;
	private float useUp;
	private float finalAmount;
	private float finalPrice;
	private float finalMoney;


	public Material getMaterial() {
		return material;
	}
	public void setMaterial(Material material) {
		this.material = material;
	}
	
	public float getPrimeAmount() {
		return primeAmount;
	}
	public void setPrimeAmount(float primeAmount) {
		this.primeAmount = primeAmount;
	}
	
	public float getPrimeMoney() {
		primeMoney = this.primeAmount * this.finalPrice;
		return primeMoney;
	}
	public void setPrimeMoney(float primeMoney) {
		this.primeMoney = primeMoney;
	}
	public float getStockIn() {
		return stockIn;
	}
	
	public float getUseUp() {
		return useUp;
	}
	public void setConsumption(float useUp) {
		this.useUp = useUp;
	}
	public void setStockIn(float stockIn) {
		this.stockIn = stockIn;
	}
	public float getStockInTransfer() {
		return stockInTransfer;
	}
	public void setStockInTransfer(float stockInTransfer) {
		this.stockInTransfer = stockInTransfer;
	}
	public float getStockTakeMore() {
		return stockTakeMore;
	}
	public void setStockTakeMore(float stockTakeMore) {
		this.stockTakeMore = stockTakeMore;
	}
	public float getStockSpill() {
		return stockSpill;
	}
	public void setStockSpill(float stockSpill) {
		this.stockSpill = stockSpill;
	}

	public float getStockOut() {
		return stockOut;
	}
	public void setStockOut(float stockOut) {
		this.stockOut = stockOut;
	}
	public float getStockOutTransfer() {
		return stockOutTransfer;
	}
	public void setStockOutTransfer(float stockOutTransfer) {
		this.stockOutTransfer = stockOutTransfer;
	}
	public float getStockTakeLess() {
		return stockTakeLess;
	}
	public void setStockTakeLess(float stockOutLess) {
		this.stockTakeLess = stockOutLess;
	}
	public float getStockDamage() {
		return stockDamage;
	}
	public void setStockDamage(float stockDamage) {
		this.stockDamage = stockDamage;
	}

	public float getFinalAmount() {
		return finalAmount;
	}
	
	public void setFinalAmount(float finalAmount) {
		this.finalAmount = finalAmount;
	}
	
	public float getFinalPrice() {
		return finalPrice;
	}
	
	public void setFinalPrice(float finalPrice) {
		this.finalPrice = finalPrice;
	}

	public void setFinalMoney(float finalMoney) {
		this.finalMoney = finalMoney;
	}
	public float getFinalMoney() {
		//finalMoney = this.finalPrice * this.finalAmount;
		return finalMoney;
	}
	
	public float getStockInAmount() {
		return this.stockIn + this.stockInTransfer + this.stockTakeMore + this.stockSpill;
	}
	
	public float getStockOutAmount() {
		return this.stockOut + this.stockOutTransfer + this.stockTakeLess + this.stockDamage + this.useUp;
	}

	
	public float getActualAmount(){
		float actual = this.primeAmount + this.getStockInAmount() - this.getStockOutAmount();
		return actual;
		
	}
	@Override
	public String toString(){
		return "stockReport : materialId = " + material.getId() + " ,primeAmount = " + getPrimeAmount() + ",finalAmount = " + getFinalAmount() + ",finalPrice = " + getFinalPrice();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null && !(obj instanceof StockReport)){
			return false;
		}else{
			return material.getId() == ((StockReport)obj).material.getId();
		}
	}
	@Override
	public int hashCode(){
		return 17 * 31 + material.getId();
	}
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("materialId", this.getMaterial().getId());
		jm.putString("materialName", this.getMaterial().getName());
		jm.putFloat("primeAmount", this.getPrimeAmount());
		jm.putFloat("stockIn", this.getStockIn());
		jm.putFloat("stockInTransfer", this.getStockInTransfer());
		jm.putFloat("stockTakeMore", this.getStockTakeMore());
		jm.putFloat("stockSpill", this.getStockSpill());
		jm.putFloat("stockInAmount", this.getStockInAmount());
		jm.putFloat("stockOut", this.getStockOut());
		jm.putFloat("stockOutTransfer", this.getStockOutTransfer());
		jm.putFloat("stockTakeLess", this.getStockTakeLess());
		jm.putFloat("stockDamage", this.getStockDamage());
		jm.putFloat("useUp", this.getUseUp());
		jm.putFloat("stockOutAmount", this.getStockOutAmount());
		jm.putFloat("finalAmount", this.getFinalAmount());
		jm.putFloat("finalPrice", this.getFinalPrice());
		jm.putFloat("finalMoney", this.getFinalMoney());
		jm.putFloat("primeMoney", this.getPrimeMoney());
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
}
