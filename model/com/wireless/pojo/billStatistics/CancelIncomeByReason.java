package com.wireless.pojo.billStatistics;

import java.util.ArrayList;
import java.util.List;

import com.wireless.protocol.CancelReason;
import com.wireless.protocol.PDepartment;

public class CancelIncomeByReason {
	
	public static class IncomeByEachDept{
		
		PDepartment mDept;		//部门信息
		float mCancelAmount;	//退菜数量
		float mCancelPrice;		//退菜金额
		
		public IncomeByEachDept(){
			
		}
		
		public IncomeByEachDept(PDepartment dept, float cancelAmount, float cancelPrice){
			this.mDept = dept;
			this.mCancelAmount = cancelAmount;
			this.mCancelPrice = cancelPrice;
		}
		
		public void getDept(PDepartment dept){
			this.mDept = dept;
		}
		
		public PDepartment getDept(){
			return mDept;
		}
		
		public void setAmount(float amount){
			this.mCancelAmount = amount;
		}
		
		public float getAmount(){
			return this.mCancelAmount;
		}
		
		public void setPrice(float price){
			this.mCancelPrice = price;
		}
		
		public float getPrice(){
			return this.mCancelPrice;
		}
	}
	
	private CancelReason mCancelReason;					//退菜原因
	private List<IncomeByEachDept> mIncomeByEachDept;	//在这个退菜原因下每个部门的退菜信息
	
	public CancelIncomeByReason(){
		 mIncomeByEachDept = new ArrayList<IncomeByEachDept>();
	}
	
	public CancelIncomeByReason(CancelReason reason, List<IncomeByEachDept> incomeByEachDept){
		this.mCancelReason = reason;
		this.mIncomeByEachDept = incomeByEachDept;
	}
	
	public CancelReason getCancelReason() {
		return mCancelReason;
	}
	
	public void setCancelReason(CancelReason cancelReason) {
		this.mCancelReason = cancelReason;
	}
	
	public List<IncomeByEachDept> getIncomeByEachDept() {
		return mIncomeByEachDept;
	}

	public void setIncomeByEachDept(List<IncomeByEachDept> incomeByEachDept) {
		this.mIncomeByEachDept = incomeByEachDept;
	}

	public float getTotalCancelAmount() {
		float cancelAmount = 0;
		for(IncomeByEachDept income : mIncomeByEachDept){
			cancelAmount += income.mCancelAmount;
		}
		return cancelAmount;
	}
	
	public float getTotalCancelPrice() {
		float cancelPrice = 0;
		for(IncomeByEachDept income : mIncomeByEachDept){
			cancelPrice += income.mCancelPrice;
		}
		return cancelPrice;
	}
}
