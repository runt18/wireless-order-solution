package com.wireless.pojo.billStatistics;

import com.wireless.protocol.CancelReason;
import com.wireless.protocol.PDepartment;

public class CancelIncomeByDeptAndReason {
	
	private PDepartment mDept;
	private CancelReason mReason;
	private float mCancelAmount;
	private float mCancelPrice;
	
	public CancelIncomeByDeptAndReason(){
		
	}
	
	public CancelIncomeByDeptAndReason(PDepartment dept, CancelReason reason, float cancelAmount, float cancelPrice){
		this.mDept = dept;
		this.mReason = reason;
		this.mCancelAmount = cancelAmount;
		this.mCancelPrice = cancelPrice;
	}
	
	public void setDept(PDepartment dept){
		this.mDept = dept;
	}
	
	public PDepartment getDept(){
		return this.mDept;
	}
	
	public void setCancelAmount(float cancelAmount){
		this.mCancelAmount = cancelAmount;
	}
	
	public CancelReason getReason(){
		return mReason;
	}
	
	public void setCancelReason(CancelReason reason){
		this.mReason = reason;
	}
	
	public float getCancelAmount(){
		return this.mCancelAmount;
	}
	
	public void setCancelPrice(float cancelPrice){
		this.mCancelPrice = cancelPrice;
	}
	
	public float getCancelPrice(){
		return this.mCancelPrice;
	}
	
}
