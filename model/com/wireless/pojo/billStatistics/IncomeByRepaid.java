package com.wireless.pojo.billStatistics;

public class IncomeByRepaid {

	public final static IncomeByRepaid DUMMY = new IncomeByRepaid();
	
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
