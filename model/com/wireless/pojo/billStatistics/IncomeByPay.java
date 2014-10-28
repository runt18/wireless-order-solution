package com.wireless.pojo.billStatistics;

import java.util.Collections;
import java.util.List;

import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.pojo.util.SortedList;


public class IncomeByPay{
	
	public final static IncomeByPay DUMMY = new IncomeByPay(0);

	public static class PaymentIncome implements Comparable<PaymentIncome>{
		private final PayType payType;
		private final int amount;
		private final float total;
		private final float actual;
		
		public PaymentIncome(PayType payType, int amount, float total, float actual){
			this.payType = payType;
			this.amount = amount;
			this.total = total;
			this.actual = actual;
		}

		public PayType getPayType() {
			return payType;
		}

		public int getAmount() {
			return amount;
		}

		public float getTotal() {
			return total;
		}

		public float getActual() {
			return actual;
		}

		@Override
		public int compareTo(PaymentIncome o) {
			return payType.compareTo(o.payType);
		}
	}
	
	private final int orderAmount;
	private final List<PaymentIncome> paymentIncomes = SortedList.newInstance();
	
	public IncomeByPay(int orderAmount){
		this.orderAmount = orderAmount;
	}
	
	public List<PaymentIncome> getPaymentIncomes(){
		return Collections.unmodifiableList(paymentIncomes);
	}
	
	public void addPaymentIncome(PaymentIncome paymentIncome){
		if(paymentIncome != null){
			int index = paymentIncomes.indexOf(paymentIncome);
			if(index < 0){
				paymentIncomes.add(paymentIncome);
			}else{
				PaymentIncome original = paymentIncomes.get(index);
				paymentIncomes.set(index, new PaymentIncome(paymentIncome.payType, 
														    original.amount + paymentIncome.amount, 
														    original.total + paymentIncome.total, 
														    original.actual + paymentIncome.actual));
			}
		}
	}
	
	public int getOrderAmount(){
		return orderAmount;
	}
	
	public float getTotalActual() {
		float actual = 0;
		for(PaymentIncome eachIncome : paymentIncomes){
			actual += eachIncome.actual;
		}
		return NumericUtil.roundFloat(actual);
	}

	public float getTotalIncome() {
		float total = 0;
		for(PaymentIncome eachIncome : paymentIncomes){
			total += eachIncome.total;
		}
		return NumericUtil.roundFloat(total);
	}

}
