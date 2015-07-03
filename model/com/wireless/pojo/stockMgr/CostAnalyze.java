package com.wireless.pojo.stockMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class CostAnalyze implements Jsonable{

	private int deptId;
	private String deptName;
	private float primeMoney;
	//private float useMaterialMoney;
	//领料
	private float pickMaterialMoney;
	//退料
	private float stockOutMoney;
	//拨入
	private float stockInTransferMoney;
	//拨出
	private float stockOutTransferMoney;
	private float endMoney;
	private float costMoney;
	private float salesMoney = 0;
	private float profit;
	private float profitRate;
	
	
	public CostAnalyze(){
		
	}
	
	public CostAnalyze(int id){
		this.deptId = id;
	}
	
	public int getDeptId() {
		return deptId;
	}

	public void setDeptId(int deptId) {
		this.deptId = deptId;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}


	public float getPickMaterialMoney() {
		return pickMaterialMoney;
	}

	public void setPickMaterialMoney(float pickMaterialMoney) {
		this.pickMaterialMoney = pickMaterialMoney;
	}

	public float getPrimeMoney() {
		return primeMoney;
	}

	public void setPrimeMoney(float primeMoney) {
		this.primeMoney = primeMoney;
	}

	public float getStockOutMoney() {
		return stockOutMoney;
	}

	public void setStockOutMoney(float stockOutMoney) {
		this.stockOutMoney = stockOutMoney;
	}

	public float getStockOutTransferMoney() {
		return stockOutTransferMoney;
	}

	public void setStockOutTransferMoney(float stockOutTransferMoney) {
		this.stockOutTransferMoney = stockOutTransferMoney;
	}

	public float getEndMoney() {
		return endMoney;
	}

	public void setEndMoney(float endMoney) {
		this.endMoney = endMoney;
	}

	public float getCostMoney() {
		return costMoney;
	}

	public void setCostMoney(float costMoney) {
		this.costMoney = costMoney;
	}

	public float getSalesMoney() {
		return salesMoney;
	}

	public void setSalesMoney(float salesMoney) {
		this.salesMoney = salesMoney;
	}

	public float getProfit() {
		profit = Math.round((this.salesMoney - this.costMoney) * 100) / 100;
		return profit;
	}

	public void setProfit(float profit) {
		this.profit = profit;
	}

	public float getProfitRate() {
		if(this.salesMoney == 0){
			profitRate = 0;
		}else{
			profitRate = (float)Math.round((this.getProfit() / this.salesMoney) * 100) / 100;
		}
		return profitRate;
	}

	public void setProfitRate(float profitRate) {
		this.profitRate = profitRate;
	}
	
	public float getStockInTransferMoney() {
		return stockInTransferMoney;
	}

	public void setStockInTransferMoney(float stockInTransferMoney) {
		this.stockInTransferMoney = stockInTransferMoney;
	}

	@Override
	public String toString(){
		return "";
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CostAnalyze)) {
			return false;
		} else {
			return deptId == ((CostAnalyze) obj).deptId;
					
		}
	}	
	@Override
	public int hashCode() {
		int result = 17;
		result = result * 31 + this.deptId;
		return result;
	}
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		
		jm.putInt("deptId", this.getDeptId());
		jm.putString("deptName", this.getDeptName());
		jm.putFloat("primeMoney", this.getPrimeMoney());
		jm.putFloat("useMaterialMoney", this.getPickMaterialMoney());
		jm.putFloat("stockOutMoney", this.getStockOutMoney());
		jm.putFloat("stockInTransferMoney", this.getStockInTransferMoney());
		jm.putFloat("stockOutTransferMoney", this.getStockOutTransferMoney());
		jm.putFloat("endMoney", this.getEndMoney());
		jm.putFloat("costMoney", this.getCostMoney());
		jm.putFloat("salesMoney", this.getSalesMoney());
		jm.putFloat("profit", this.getProfit());
		jm.putFloat("profitRate", this.getProfitRate());
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

	
}
