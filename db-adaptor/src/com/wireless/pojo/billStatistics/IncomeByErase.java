package com.wireless.pojo.billStatistics;

public class IncomeByErase {
	
	private float mErasePrice;		//抹数账单数
	private int mEraseAmount;		//抹数账单数		
	
	public IncomeByErase(){
		
	}
	
	public IncomeByErase(float erasePrice, int eraseAmount){
		setErasePrice(erasePrice);
		setEraseAmount(eraseAmount);
	}
	
	public void setErasePrice(float erasePrice){
		this.mErasePrice = erasePrice;
	}
	
	public float getTotalErase(){
		return this.mErasePrice;
	}
	
	public void setEraseAmount(int eraseAmount){
		this.mEraseAmount = eraseAmount;
	}
	
	public int getEraseAmount(){
		return this.mEraseAmount;
	}
}
