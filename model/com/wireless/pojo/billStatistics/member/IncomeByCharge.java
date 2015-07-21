package com.wireless.pojo.billStatistics.member;

public class IncomeByCharge {

	public final static IncomeByCharge DUMMY = new IncomeByCharge();
	
	private int chargeAmount;				//充值次数
	
	private float actualCashCharge;			//现金充值实收额
	private float actualCreditCardCharge;	//刷卡刷卡实收额
	
	private float totalAccountCharge;		//充值账户冲额
	
	private float totalActualRefund;		//实际退款总额
	private float totalAccountRefund;		//账户扣款总额
	
	private int refundAmount;				//退款次数
	
	public void setActualCashCharge(float cash){
		this.actualCashCharge = cash;
	}
	
	public float getActualCashCharge(){
		return this.actualCashCharge;
	}
	
	public void setActualCreditCardCharge(float creditCard){
		this.actualCreditCardCharge = creditCard;
	}
	
	public float getActualCreditCardCharge(){
		return this.actualCreditCardCharge;
	}

	public float getTotalActualCharge() {
		return getActualCashCharge() + getActualCreditCardCharge();
	}

	public float getTotalAccountCharge() {
		return totalAccountCharge;
	}

	public void setTotalAccountCharge(float totalCharge) {
		this.totalAccountCharge = totalCharge;
	}

	public float getTotalActualRefund() {
		return totalActualRefund;
	}

	public void setTotalActualRefund(float totalActualRefund) {
		this.totalActualRefund = totalActualRefund;
	}

	public float getTotalAccountRefund() {
		return totalAccountRefund;
	}

	public void setTotalAccountRefund(float totalRefund) {
		this.totalAccountRefund = totalRefund;
	}

	public int getChargeAmount() {
		return chargeAmount;
	}

	public void setChargeAmount(int chargeAmount) {
		this.chargeAmount = chargeAmount;
	}

	public int getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(int refundAmount) {
		this.refundAmount = refundAmount;
	}
}
