package com.wireless.pojo.stockMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.inventoryMgr.Material;

public class StockReport implements Jsonable{

	private Material material = new Material();
	private float primeAmount;		//期初数量
	private float primeMoney;		//期初金额
	private float stockIn;			//入库采购
	private float stockInTransfer;	//领料
	private float stockTakeMore;	//盘盈
	private float stockSpill;		//其他入库
	private float stockOut;			//出库退货
	private float stockOutTransfer; //退料
	private float stockTakeLess;	//盘亏
	private float stockDamage;		//其他出库
	private float consumption;		//消耗
	private float finalAmount;		//期末数量
	private float finalPrice;		//参考成本
	private float finalMoney;		//期末金额
	private float comsumeMoney;		//销售金额
	
	public Material getMaterial() {
		return material;
	}
	
	public float getComsumeMoney(){
		return this.comsumeMoney;
	}
	
	public void setComsumeMoney(float money){
		this.comsumeMoney = money;
	}
	
	/**
	 * 消耗差异(理论消耗{@link #getExpectConsumption} - 实际消耗{@link #getActualConsumption})
	 * @return
	 */
	public float getDeltaAmount() {
		return this.getExpectConsumption() - this.getActualConsumption();
	}
	
	/**
	 * 出库总计 (出库退货 + 出库调拨), 用于【消耗差异表】
	 * @return
	 */
	public float getStockOutTotal() {
		return this.stockOut + this.stockOutTransfer;
	}
	
	/**
	 * 入库总计 (入库采购 + 入库调拨), 用于【消耗差异表】
	 * @return
	 */
	public float getStockInTotal() {
		return this.stockIn + this.stockInTransfer;
	}
	
	/**
	 * 理论消耗(消耗单)
	 * @return
	 */
	public float getExpectConsumption() {
		return this.consumption;
	}
	
	/**
	 * 实际消耗 (期初数量 + 入库采购  + 入库调拨 - 出库退货 - 出库调拨 - 期末数量)
	 * @return
	 */
	public float getActualConsumption() {
		return this.primeAmount + this.stockIn + this.stockInTransfer - this.stockOut - this.stockOutTransfer - this.finalAmount; 
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
		return consumption;
	}
	public void setConsumption(float useUp) {
		this.consumption = useUp;
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
		return this.stockOut + this.stockOutTransfer + this.stockTakeLess + this.stockDamage + this.consumption;
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
		jm.putFloat("actualConsumption", this.getActualConsumption());
		jm.putFloat("expectConsumption", this.getExpectConsumption());
		jm.putFloat("stockInTotal", this.getStockInTotal());
		jm.putFloat("stockOutTotal", this.getStockOutTotal());
		jm.putFloat("deltaAmount", this.getDeltaAmount());
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
}
