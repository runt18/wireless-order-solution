package com.wireless.pojo.billStatistics;

import java.util.ArrayList;
import java.util.List;

import com.wireless.protocol.CancelReason;
import com.wireless.protocol.PDepartment;

public class CancelIncomeByDept {
	public static class IncomeByEachReason{
		
		CancelReason mReason;		//退菜原因
		float mReasonAmount;		//退菜数量
		float mReasonPrice;			//退菜金额
		
		public IncomeByEachReason(){
			
		}
		
		public IncomeByEachReason(CancelReason reason, float reasonAmount, float reasonPrice){
			this.mReason = reason;
			this.mReasonAmount = reasonAmount;
			this.mReasonPrice = reasonPrice;
		}
		
		public CancelReason getReason(){
			return this.mReason;
		}
		
		public void setAmount(float amount){
			this.mReasonAmount = amount;
		}
		
		public float getAmount(){
			return this.mReasonAmount;
		}
		
		public void setPrice(float price){
			this.mReasonPrice = price;
		}
		
		public float getPrice(){
			return this.mReasonPrice;
		}
		
	};
	
	private PDepartment mDept;								//部门信息
	private List<IncomeByEachReason> mIncomeByEachReason;	//在这个部门下每个退菜原因的退菜信息
	
	public CancelIncomeByDept(){
		mIncomeByEachReason = new ArrayList<IncomeByEachReason>();
	}
	
	public CancelIncomeByDept(PDepartment dept, List<IncomeByEachReason> incomeByEachReason){
		this.mDept = dept;
		this.mIncomeByEachReason = incomeByEachReason;
	}
	
	public PDepartment getDept() {
		return mDept;
	}
	
	public void setDept(PDepartment dept) {
		this.mDept = dept;
	}
	
	public List<IncomeByEachReason> getIncomeByEachReason() {
		return mIncomeByEachReason;
	}

	public void setIncomeByEachReason(List<IncomeByEachReason> incomeByEachReason) {
		this.mIncomeByEachReason = incomeByEachReason;
	}

	public float getTotalCancelAmount() {
		float reasonAmount = 0;
		for(IncomeByEachReason income : mIncomeByEachReason){
			reasonAmount += income.mReasonAmount;
		}
		return reasonAmount;
	}
	
	public float getTotalCancelPrice() {
		float reasonPrice = 0;
		for(IncomeByEachReason income : mIncomeByEachReason){
			reasonPrice += income.mReasonPrice;
		}
		return reasonPrice;
	}
}
