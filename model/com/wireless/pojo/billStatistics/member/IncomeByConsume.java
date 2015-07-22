package com.wireless.pojo.billStatistics.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IncomeByConsume {

	public final static IncomeByConsume DUMMY = new IncomeByConsume();

	public static class IncomeByPay{
		private final String payType;
		private final float income;
		private final int amount;
		
		IncomeByPay(String payType, float income, int amount) {
			this.payType = payType;
			this.income = income;
			this.amount = amount;
		}
		
		public String getPayType(){
			return this.payType;
		}
		
		public float getIncome(){
			return this.income;
		}
		
		public int getAmount(){
			return this.amount;
		}
	}
	
	private final List<IncomeByPay> incomes = new ArrayList<IncomeByPay>();
	
	public void add(String payType, float income, int amount){
		incomes.add(new IncomeByPay(payType, income, amount));
	}
	
	public float getTotalConsume(){
		float total = 0;
		for(IncomeByPay payIncome : incomes){
			total += payIncome.income;
		}
		return total;
	}
	
	public int getTotalAmount(){
		int total = 0;
		for(IncomeByPay payIncome : incomes){
			total += payIncome.amount;
		}
		return total;
	}
	
	public List<IncomeByPay> getIncomes(){
		return Collections.unmodifiableList(this.incomes);
	}
}
