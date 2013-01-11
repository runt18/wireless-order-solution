package com.wireless.pojo.billStatistics;

public class IncomeByRepaid {

	private int mRepaidAmount;
	private float mTotalRepaid;
	
	public IncomeByRepaid(){
		
	}
	
	public IncomeByRepaid(int repaidAmount, float totalRepaid){
		setRepaidAmount(repaidAmount);
		setTotalRepaid(totalRepaid);
	}

	public int getRepaidAmount() {
		return mRepaidAmount;
	}

	public void setRepaidAmount(int repaidAmount) {
		this.mRepaidAmount = repaidAmount;
	}

	public float getTotalRepaid() {
		return mTotalRepaid;
	}

	public void setTotalRepaid(float totalRepaid) {
		this.mTotalRepaid = totalRepaid;
	}
	
}
