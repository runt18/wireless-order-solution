package com.wireless.pojo.billStatistics;

public class IncomeByRound {
	public final static IncomeByRound DUMMY = new IncomeByRound();
	
	private int mAmount;
	private float mTotal;
	
	public IncomeByRound(){
		
	}
	
	public IncomeByRound(int amount, float total){
		this.mAmount = amount;
		this.mTotal = total;
	}

	public int getAmount() {
		return mAmount;
	}

	public void setAmount(int amount) {
		this.mAmount = amount;
	}

	public float getTotal() {
		return mTotal;
	}

	public void setTotal(float total) {
		this.mTotal = total;
	}
	
	@Override
	public String toString(){
		return "amount=" + mAmount + ", total=" + mTotal;
	}
}
