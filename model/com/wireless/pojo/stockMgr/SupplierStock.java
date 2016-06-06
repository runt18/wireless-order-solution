package com.wireless.pojo.stockMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.supplierMgr.Supplier;

public class SupplierStock implements Jsonable{
	
	private Supplier supplier;		//供应商
	private float StockInAmount;	//采购次数
	private float StockInMoney;		//采购金额
	private float StockOutAmount;	//退货次数
	private float StockOutMoney;	//退货金额
	private float totalMoney;		//合计金额
	
	

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

	public float getStockInMoney() {
		return StockInMoney;
	}

	public void setStockInMoney(float stockInMoney) {
		StockInMoney = stockInMoney;
	}

	public float getStockOutAmount() {
		return StockOutAmount;
	}

	public void setStockOutAmount(float stockOutAmount) {
		StockOutAmount = stockOutAmount;
	}

	public float getStockOutMoney() {
		return StockOutMoney;
	}

	public void setStockOutMoney(float stockOutMoney) {
		StockOutMoney = stockOutMoney;
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
		jm.putFloat("stockInMoney", this.StockInMoney);
		jm.putFloat("stockOutAmount", this.StockOutAmount);
		jm.putFloat("stockOutMoney", this.StockOutMoney);
		jm.putFloat("totalMoney", this.totalMoney);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		
	}

}
