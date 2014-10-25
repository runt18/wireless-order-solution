package com.wireless.pojo.billStatistics;

import java.util.Collections;
import java.util.List;

import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.pojo.util.SortedList;


public class IncomeByPay{
	
	public final static IncomeByPay DUMMY = new IncomeByPay();

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
	
	private final List<PaymentIncome> paymentIncomes = SortedList.newInstance();
	
//	private int mCashAmount;			//现金账单数
//	private float mCashIncome;			//现金金额
//	private float mCashActual;			//现金实收
//	
//	private int mCreditCardAmount;		//刷卡账单数
//	private float mCreditCardIncome;	//刷卡金额
//	private float mCreditCardActual;	//刷卡实收
//	
//	private int mMemeberCardAmount;		//会员卡账单数
//	private float mMemberCardIncome;	//会员卡金额
//	private float mMemberCardActual;	//会员卡实收
//	
//	private int mSignAmount;			//签单账单数
//	private float mSignIncome;			//签单金额
//	private float mSignActual;			//签单实收
//	
//	private int mHangAmount;			//挂账账单数
//	private float mHangIncome;			//挂账金额
//	private float mHangActual;			//挂账实收
	
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
		int amount = 0;
		for(PaymentIncome eachIncome : paymentIncomes){
			amount += eachIncome.amount;
		}
		return amount;
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
