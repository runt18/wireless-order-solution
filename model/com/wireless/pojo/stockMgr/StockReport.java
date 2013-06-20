package com.wireless.pojo.stockMgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.pojo.inventoryMgr.Material;

public class StockReport implements Jsonable{

	private Material material;
	private float primeAmount;
	private float stockIn;
	private float stockInTransfer;
	private float stockTakeMore;
	private float stockSpill;
	private float stockInAmount;
	private float stockOut;
	private float stockOutTransfer;
	private float stockTakeLess;
	private float stockDamage;
	private float useUp;
	private float stockOutAmount;
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
	public float getStockIn() {
		return stockIn;
	}
	
	public float getUseUp() {
		return useUp;
	}
	public void setUseUp(float useUp) {
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

	public void setStockInAmount(float stockInAmount) {
		this.stockInAmount = stockInAmount;
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

	public void setStockOutAmount(float stockOutAmount) {
		this.stockOutAmount = stockOutAmount;
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

	public float getStockInAmount() {
		stockInAmount = this.stockIn + this.stockInTransfer + this.stockTakeMore + this.stockSpill;
		return stockInAmount;
	}
	public float getStockOutAmount() {
		stockOutAmount = this.stockOut + this.stockOutTransfer + this.stockTakeLess + this.stockDamage + this.useUp;
		return stockOutAmount;
	}
	public float getFinalMoney() {
		finalMoney = this.finalPrice * this.finalAmount;
		return finalMoney;
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
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("materialId", this.getMaterial().getId());
		jm.put("materialName", this.getMaterial().getName());
		jm.put("primeAmount", this.getPrimeAmount());
		jm.put("stockIn", this.getStockIn());
		jm.put("stockInTransfer", this.getStockInTransfer());
		jm.put("stockTakeMore", this.getStockTakeMore());
		jm.put("stockSpill", this.getStockSpill());
		jm.put("stockInAmount", this.getStockInAmount());
		jm.put("stockOut", this.getStockOut());
		jm.put("stockOutTransfer", this.getStockOutTransfer());
		jm.put("stockTakeLess", this.getStockTakeLess());
		jm.put("stockDamage", this.getStockDamage());
		jm.put("useUp", this.getUseUp());
		jm.put("stockOutAmount", this.getStockOutAmount());
		jm.put("finalAmount", this.getFinalAmount());
		jm.put("finalPrice", this.getFinalPrice());
		jm.put("finalMoney", this.getFinalMoney());
		return Collections.unmodifiableMap(jm);
	}
	@Override
	public List<Object> toJsonList(int flag) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
