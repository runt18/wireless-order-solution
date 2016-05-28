package com.wireless.pojo.stockMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class CostAnalyze implements Jsonable{
/*
	private int deptId;						//部门id
	private String deptName;				//部门名
	private float primeMoney;				//期初金额
	private float pickMaterialMoney;		//领料金额
	private float stockOutMoney;			//退料金额
	private float stockInTransferMoney;		//拨入金额
	private float stockOutTransferMoney;	//拨出金额
	private float endMoney;					//期末金额
	private float costMoney;				//成本金额
	private float salesMoney = 0;			//销售金额
	private float profit;					//毛利额
	private float profitRate;				//毛利率
*/
	
	private int deptId;						//部门id
	private String deptName;				//部门名
	private float primeMoney;				//期初金额
	private float pickMaterialMoney;		//采购金额
	private float stockOutMoney;			//退货金额
	private float stockInTransferMoney;		//领料金额
	private float stockOutTransferMoney;	//退料金额
	private float endMoney;					//期末金额
	private float costMoney;				//成本金额
	private float salesMoney = 0;			//销售金额
	private float profit;					//毛利额
	private float profitRate;				//毛利率

	
	private float stockSpillMoney;			//其他入库额
	private float stockDamageMoney;			//其他出库额
	private float stockTakeMoreMoney;		//盘盈金额
	private float stockTakeLessMoney;		//盘亏金额	
	
	public CostAnalyze(){}
	
	public CostAnalyze(int id){
		this.deptId = id;
	}
	
	public float getStockSpillMoney() {
		return stockSpillMoney;
	}

	public void setStockSpillMoney(float stockSpillMoney) {
		this.stockSpillMoney = stockSpillMoney;
	}

	public float getStockDamageMoney() {
		return stockDamageMoney;
	}

	public void setStockDamageMoney(float stockDamageMoney) {
		this.stockDamageMoney = stockDamageMoney;
	}

	public float getStockTakeMoreMoney() {
		return stockTakeMoreMoney;
	}

	public void setStockTakeMoreMoney(float stockTakeMoreMoney) {
		this.stockTakeMoreMoney = stockTakeMoreMoney;
	}

	public float getStockTakeLessMoney() {
		return stockTakeLessMoney;
	}

	public void setStockTakeLessMoney(float stockTakeLessMoney) {
		this.stockTakeLessMoney = stockTakeLessMoney;
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
		jm.putFloat("stockSpillMoney", this.getStockSpillMoney());
		jm.putFloat("stockDamageMoney", this.getStockDamageMoney());
		jm.putFloat("stockTakeMoreMoney", this.getStockTakeMoreMoney());
		jm.putFloat("stockTakeLessMoney", this.getStockTakeLessMoney());
//		private float stockSpillMoney;			//其他入库额
//		private float stockDamageMoney;			//其他出库额
//		private float stockTakeMoreMoney;		//盘盈金额
//		private float stockTakeLessMoney;		//盘亏金额	
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

	
}
