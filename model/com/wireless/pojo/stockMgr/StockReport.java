package com.wireless.pojo.stockMgr;

public class StockReport {

	private int materialId;
	private float primeAmount;
	private float stockIn;
	private float stockInTransfer;
	private float stockTakeMore;
	private float stockSpill;
	private float stockInAmount;
	private float stockOut;
	private float stockOutTransfer;
	private float stockOutLess;
	private float stockDamage;
	private float useUp;
	private float stockOutAmount;
	private float finalAmount;
	private float finalPrice;
	private float finalMoney;
	
	
	
	public int getMaterialId() {
		return materialId;
	}
	public void setMaterialId(int materialId) {
		this.materialId = materialId;
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
	public float getStockInAmount() {
		return stockInAmount;
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
	public float getStockOutLess() {
		return stockOutLess;
	}
	public void setStockOutLess(float stockOutLess) {
		this.stockOutLess = stockOutLess;
	}
	public float getStockDamage() {
		return stockDamage;
	}
	public void setStockDamage(float stockDamage) {
		this.stockDamage = stockDamage;
	}
	public float getStockOutAmount() {
		return stockOutAmount;
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
	public float getFinalMoney() {
		return finalMoney;
	}
	public void setFinalMoney(float finalMoney) {
		this.finalMoney = finalMoney;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null && !(obj instanceof StockReport)){
			return false;
		}else{
			return materialId == ((StockReport)obj).materialId;
		}
	}
	@Override
	public int hashCode(){
		return 17 * 31 + materialId;
	}
	
}
