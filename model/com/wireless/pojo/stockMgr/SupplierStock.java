package com.wireless.pojo.stockMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.supplierMgr.Supplier;

public class SupplierStock implements Jsonable{
	
	private Supplier supplier;			//供应商
	private float StockInAmount;		//采购次数
	private float StockInActualMoney;	//采购金额
	private float StockOutAmount;		//退货次数
	private float StockOutActualMoney;	//退货金额
	private float totalMoney;			//合计金额
	private float StockInMoney;
	private float StockOutMoney;
	
	
	public float getStockInMoney() {
		return StockInMoney;
	}

	public void setStockInMoney(float stockInMoney) {
		StockInMoney = stockInMoney;
	}

	public float getStockOutMoney() {
		return StockOutMoney;
	}

	public void setStockOutMoney(float stockOutMoney) {
		StockOutMoney = stockOutMoney;
	}

	public float getStockInActualMoney() {
		return StockInActualMoney;
	}

	public void setStockInActualMoney(float stockInActualMoney) {
		StockInActualMoney = stockInActualMoney;
	}

	public float getStockOutActualMoney() {
		return StockOutActualMoney;
	}

	public void setStockOutActualMoney(float stockOutActualMoney) {
		StockOutActualMoney = stockOutActualMoney;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

	public float getStockInAmount() {
		return StockInAmount;
	}

	public void setStockInAmount(float stockInAmount) {
		StockInAmount = stockInAmount;
	}

	public float getStockOutAmount() {
		return StockOutAmount;
	}

	public void setStockOutAmount(float stockOutAmount) {
		StockOutAmount = stockOutAmount;
	}

	public float getTotalMoney() {
		return totalMoney;
	}

	public void setTotalMoney(float totalMoney) {
		this.totalMoney = totalMoney;
	}
	
	

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable("supplier", this.supplier, 0);
		jm.putFloat("stockInAmount", this.StockInAmount);
		jm.putFloat("stockInActualMoney", this.StockInActualMoney);
		jm.putFloat("stockOutAmount", this.StockOutAmount);
		jm.putFloat("stockOutActualMoney", this.StockOutActualMoney);
		jm.putFloat("totalMoney", this.totalMoney);
		jm.putFloat("stockInMoney", this.StockInMoney);
		jm.putFloat("stockOutMoney", this.StockOutMoney);
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		
	}

}
