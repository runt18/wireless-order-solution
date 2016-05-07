package com.wireless.pojo.billStatistics;

import java.util.Collections;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.pojo.util.SortedList;


public class IncomeByPay implements Jsonable{
	
	public final static IncomeByPay DUMMY = new IncomeByPay(0);
	//日结, 交班等..
	public static final byte  PAY_TYPE_FOR_DAILY= 0;
	//报表
	public static final byte PAY_TYPE_FOR_STATISTICS = 1;

	public static class PaymentIncome implements Comparable<PaymentIncome>, Jsonable{
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

		@Override
		public JsonMap toJsonMap(int flag) {
			JsonMap jm = new JsonMap();
			jm.putInt("id", this.payType.getId());
			jm.putString("payType", this.payType.getName());
			jm.putFloat("total", this.total);
			jm.putFloat("actual", this.actual);
			jm.putInt("amount", this.amount);
			return jm;
		}

		@Override
		public void fromJsonMap(JsonMap jsonMap, int flag) {
			
		}
		
		@Override
		public String toString(){
			return this.payType + "," + this.total;
		}
	}
	
	private int orderAmount;
	private final List<PaymentIncome> paymentIncomes = SortedList.newInstance();
	
	public IncomeByPay(int orderAmount){
		this.orderAmount = orderAmount;
	}
	
	public List<PaymentIncome> getPaymentIncomes(){
		return Collections.unmodifiableList(paymentIncomes);
	}
	
	public void append(IncomeByPay appendIncome){
		for(IncomeByPay.PaymentIncome payIncome : appendIncome.getPaymentIncomes()){
			addIncome4Chain(payIncome);
		}
		this.orderAmount += appendIncome.orderAmount;
	}
	
	private void addIncome4Chain(PaymentIncome income){
		if(income != null){
			boolean isExist = false;
			for(int i = 0; i < paymentIncomes.size(); i++){
				if(paymentIncomes.get(i).payType.getName().equals(income.getPayType().getName())){
					PaymentIncome original = paymentIncomes.get(i);
					paymentIncomes.set(i, new PaymentIncome(income.payType, 
						    								original.amount + income.amount, 
						    								original.total + income.total, 
						    								original.actual + income.actual));
					isExist = true;
					break;
				}
			}
			if(!isExist){
				paymentIncomes.add(income);
			}
		}
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

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		if(flag == PAY_TYPE_FOR_DAILY){
			jm.putJsonableList("paymentIncomes", this.paymentIncomes, flag);
		}else if(flag == PAY_TYPE_FOR_STATISTICS){
			for (PaymentIncome p : paymentIncomes) {
				jm.putFloat("payType" + p.getPayType().getId(), p.getActual());
			}			
		}
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

}
