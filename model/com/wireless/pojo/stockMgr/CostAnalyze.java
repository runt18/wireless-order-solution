package com.wireless.pojo.stockMgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;

public class CostAnalyze implements Jsonable{

	private static final List<Object> IllegalAccessException = null;
	private int deptId;
	private String deptName;
	private float primeMoney;
	private float useMaterialMoney;
	private float stockOutMoney;
	private float stockOutTransferMoney;
	private float endMoney;
	private float costMoney;
	private float salesMoney;
	private float profit;
	private float profitRate;
	
	
	
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

	public float getUseMaterialMoney() {
		return useMaterialMoney;
	}

	public void setUseMaterialMoney(float useMaterialMoney) {
		this.useMaterialMoney = useMaterialMoney;
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
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		
		jm.put("deptId", this.getDeptId());
		jm.put("deptName", this.getDeptName());
		jm.put("primeMoney", this.getPrimeMoney());
		jm.put("useMaterialMoney", this.getUseMaterialMoney());
		jm.put("stockOutMoney", this.getStockOutMoney());
		jm.put("stockOutTransferMoney", this.getStockOutTransferMoney());
		jm.put("endMoney", this.getEndMoney());
		jm.put("costMoney", this.getCostMoney());
		jm.put("salesMoney", this.getSalesMoney());
		jm.put("profit", this.getProfit());
		jm.put("profitRate", this.getProfitRate());
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		
		return  IllegalAccessException;
	}

	
}
