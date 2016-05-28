package com.wireless.pojo.stockMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class MonthlyCost implements Jsonable{
	
	public static class InsertBuilder{
		private int materialId;
		private int monthlyBalanceId;
		private float cost;
		
		public InsertBuilder(){}
		
		public int getMaterialId() {
			return materialId;
		}
		
		public InsertBuilder setMaterialId(int materialId) {
			this.materialId = materialId;
			return this;
		}
		
		public int getMonthlyBalanceId() {
			return monthlyBalanceId;
		}
		
		public InsertBuilder setMonthlyBalanceId(int monthlyBalanceId) {
			this.monthlyBalanceId = monthlyBalanceId;
			return this;
		}
		
		public float getCost() {
			return cost;
		}
		
		public InsertBuilder setCost(float cost) {
			this.cost = cost;
			return this;
		}
		
		public MonthlyCost builder(){
			return new MonthlyCost(this);
		}
	}
	
	private int id;
	private int materialId;
	private int monthlyBalanceId;
	private float cost;
	
	public MonthlyCost(){}
	
	public MonthlyCost(InsertBuilder builder){
		this.materialId = builder.materialId;
		this.monthlyBalanceId = builder.monthlyBalanceId;
		this.cost = builder.cost;
	}
	
	public int getId() {
		return id;
	}
	
	public MonthlyCost setId(int id) {
		this.id = id;
		return this;
	}
	
	public int getMaterialId() {
		return materialId;
	}
	
	public MonthlyCost setMaterialId(int materialId) {
		this.materialId = materialId;
		return this;
	}
	
	public int getMonthlyBalanceId() {
		return monthlyBalanceId;
	}
	
	public MonthlyCost setMonthlyBalanceId(int monthlyBalanceId) {
		this.monthlyBalanceId = monthlyBalanceId;
		return this;
	}
	
	public float getCost() {
		return cost;
	}
	
	public MonthlyCost setCost(float cost) {
		this.cost = cost;
		return this;
	}
	
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putInt("materialId", this.materialId);
		jm.putInt("monthlyBalanceId", this.monthlyBalanceId);
		jm.putFloat("cost", this.cost);
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		// TODO Auto-generated method stub
		
	}
}
