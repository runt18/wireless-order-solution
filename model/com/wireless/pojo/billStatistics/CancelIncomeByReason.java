package com.wireless.pojo.billStatistics;

import java.util.ArrayList;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.menuMgr.Department;

public class CancelIncomeByReason {
	
	public static class IncomeByEachDept implements Jsonable{
		
		Department mDept;		//部门信息
		float mCancelAmount;	//退菜数量
		float mCancelPrice;		//退菜金额
		
		public IncomeByEachDept(){
			
		}
		
		public IncomeByEachDept(Department dept, float cancelAmount, float cancelPrice){
			this.mDept = dept;
			this.mCancelAmount = cancelAmount;
			this.mCancelPrice = cancelPrice;
		}
		
		public void getDept(Department dept){
			this.mDept = dept;
		}
		
		public Department getDept(){
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

		@Override
		public JsonMap toJsonMap(int flag) {
			JsonMap jm = new JsonMap();
			jm.putJsonable("dept", this.getDept(), Department.DEPT_JSONABLE_COMPLEX);
			jm.putFloat("amount", this.getAmount());
			jm.putFloat("price", this.getPrice());
			return jm;
		}

		@Override
		public void fromJsonMap(JsonMap jsonMap, int flag) {
			
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
