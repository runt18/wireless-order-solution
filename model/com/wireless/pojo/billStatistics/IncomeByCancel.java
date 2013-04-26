package com.wireless.pojo.billStatistics;

public class IncomeByCancel {

	private int mCancelAmount;		//退菜账单数
	private float mTotalCancel;		//合计退菜金额
	
	public IncomeByCancel(){
		
	}

	public IncomeByCancel(int cancelAmount, float totalCancel){
		setCancelAmount(cancelAmount);
		setTotalCancel(totalCancel);
	}
	
	public int getCancelAmount() {
		return mCancelAmount;
	}

	public void setCancelAmount(int mCancelAmount) {
		this.mCancelAmount = mCancelAmount;
	}

	public float getTotalCancel() {
		return mTotalCancel;
	}

	public void setTotalCancel(float totalCancel) {
		this.mTotalCancel = totalCancel;
	}
	
}
