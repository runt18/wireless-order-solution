package com.wireless.pojo.billStatistics;

public class IncomeByMemberPrice {
	public final static IncomeByMemberPrice DUMMY = new IncomeByMemberPrice();
	
	private int mAmount;
	private float mTotal;
	
	public IncomeByMemberPrice(){
		
	}
	
	public IncomeByMemberPrice(int amount, float total){
		this.mAmount = amount;
		this.mTotal = total;
	}

	public int getMemberPriceAmount() {
		return mAmount;
	}

	public void setMemberPriceAmount(int amount) {
		this.mAmount = amount;
	}

	public float getMemberPrice() {
		return mTotal;
	}

	public void setMemberPrice(float total) {
		this.mTotal = total;
	}
}
