package com.wireless.pojo.stockMgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class MonthlyBalanceDetail implements Jsonable{
	private int id;
	private int monthlyBalanceId;
	private int restaurantId;
	private int deptId;
	private String deptName;
	private float openingBalance;
	private float endingBalance;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getMonthlyBalanceId() {
		return monthlyBalanceId;
	}
	public void setMonthlyBalanceId(int monthlyBalanceId) {
		this.monthlyBalanceId = monthlyBalanceId;
	}
	public int getRestaurantId() {
		return restaurantId;
	}
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
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
	public float getOpeningBalance() {
		return openingBalance;
	}
	public void setOpeningBalance(float openingBalance) {
		this.openingBalance = openingBalance;
	}
	public float getEndingBalance() {
		return endingBalance;
	}
	public void setEndingBalance(float endingBalance) {
		this.endingBalance = endingBalance;
	}
	
	
	public static class InsertBuilder{
		private MonthlyBalanceDetail data = new MonthlyBalanceDetail();
		public InsertBuilder(int deptId, float openingBalance, float endingBalance){
			this.data.setDeptId(deptId);
			this.data.setOpeningBalance(openingBalance);
			this.data.setEndingBalance(endingBalance);
		}
		
		public InsertBuilder setMonthlyBalanceId(int monthlyBalanceId){
			this.data.setMonthlyBalanceId(monthlyBalanceId);
			return this;
		}
		
		public InsertBuilder setRestaurantId(int restaurantId){
			this.data.setRestaurantId(restaurantId);
			return this;
		}
		
		public InsertBuilder setDeptName(String deptName){
			this.data.setDeptName(deptName);
			return this;
		}
		
		public MonthlyBalanceDetail build(){
			return this.data;
		}
	}
	
	@Override 
	public int hashCode(){
		return 17 * 31 + this.id;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof MonthlyBalanceDetail)){
			return false;
		}else{
			return this.id == ((MonthlyBalanceDetail)obj).id 
					&& this.deptId == ((MonthlyBalanceDetail)obj).deptId;
		}
	}
	
	@Override
	public String toString(){
		return "MonthlyBalanceDetail( id = " + this.id + 
				",deptName = " + this.deptName +
				",openingBalance = " + this.openingBalance +
				",endingBalance = " + this.endingBalance + ")";
	}
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("id", this.id);
		jm.put("monthlyBalanceId", this.monthlyBalanceId);
		jm.put("restaurantId", this.restaurantId);
		jm.put("deptId", this.deptId);
		jm.put("deptName", this.deptName);
		jm.put("openingBalance", this.openingBalance);
		jm.put("endingBalance", this.endingBalance);
		return Collections.unmodifiableMap(jm);
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	
}
