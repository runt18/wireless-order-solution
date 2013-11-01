package com.wireless.pojo.billStatistics;

public class IncomeByCharge {

	public final static IncomeByCharge DUMMY = new IncomeByCharge();
	
	private float cash;
	private float creditCard;
	
	public void setCash(float cash){
		this.cash = cash;
	}
	
	public float getCash(){
		return this.cash;
	}
	
	public void setCreditCard(float creditCard){
		this.creditCard = creditCard;
	}
	
	public float getCreditCard(){
		return this.creditCard;
	}
}
