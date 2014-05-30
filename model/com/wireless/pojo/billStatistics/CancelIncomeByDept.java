package com.wireless.pojo.billStatistics;

import java.util.ArrayList;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.menuMgr.Department;

public class CancelIncomeByDept{
	public static class IncomeByEachReason implements Jsonable{
		
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

		@Override
		public JsonMap toJsonMap(int flag) {
			JsonMap jm = new JsonMap();
			jm.putJsonable("reason", this.getReason(), 0);
			jm.putFloat("amount", this.getAmount());
			jm.putFloat("price", this.getPrice());
			return jm;
		}

		@Override
		public void fromJsonMap(JsonMap jsonMap, int flag) {
			
		}
		
	};
	
	private Department mDept;								//部门信息
	private List<IncomeByEachReason> mIncomeByEachReason;	//在这个部门下每个退菜原因的退菜信息
	
	public CancelIncomeByDept(){
		mIncomeByEachReason = new ArrayList<IncomeByEachReason>();
	}
	
	public CancelIncomeByDept(Department dept, List<IncomeByEachReason> incomeByEachReason){
		this.mDept = dept;
		this.mIncomeByEachReason = incomeByEachReason;
	}
	
	public Department getDept() {
		return mDept;
	}
	
	public void setDept(Department dept) {
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
