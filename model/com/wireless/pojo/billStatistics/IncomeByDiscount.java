package com.wireless.pojo.billStatistics;

public class IncomeByDiscount {

	private int mDiscountAmount;		//折扣账单数
	private float mTotalDiscount;		//合计折扣金额
	
	public IncomeByDiscount(){
		
	}
	
	public IncomeByDiscount(int discountAmount, float totalDiscount){
		setDiscountAmount(discountAmount);
		setTotalDiscount(totalDiscount);
	}
	
	public int getDiscountAmount() {
		return mDiscountAmount;
	}

	public void setDiscountAmount(int discountAmount) {
		this.mDiscountAmount = discountAmount;
	}

	public float getTotalDiscount() {
		return mTotalDiscount;
	}

	public void setTotalDiscount(float totalDiscount) {
		this.mTotalDiscount = totalDiscount;
	}
	
}
