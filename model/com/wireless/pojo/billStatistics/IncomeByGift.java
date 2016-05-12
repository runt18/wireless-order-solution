package com.wireless.pojo.billStatistics;

public class IncomeByGift {
	
	private int mGiftAmount;	//赠送账单数
	private float mTotalGift;	//合计赠送金额
	
	public IncomeByGift(){
		
	}

	public IncomeByGift(int giftAmount, float totalGift){
		setGiftAmount(giftAmount);
		setTotalGift(totalGift);
	}
	
	public int getGiftAmount() {
		return mGiftAmount;
	}

	public void setGiftAmount(int giftAmount) {
		this.mGiftAmount = giftAmount;
	}

	public float getTotalGift() {
		return mTotalGift;
	}

	public void setTotalGift(float totalGift) {
		this.mTotalGift = totalGift;
	}
	
	
}	
