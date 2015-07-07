package com.wireless.pojo.billStatistics;

public class IncomeByBook {
	
	public final static IncomeByBook DUMMY = new IncomeByBook(0, 0);
	
	private int amount;
	private float income;
	
	public IncomeByBook(float income, int amount){
		this.income = income;
		this.amount = amount;
	}
	
	public float getIncome(){
		return this.income;
	}
	
	public int getAmount(){
		return this.amount;
	}
}
