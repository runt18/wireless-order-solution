package com.wireless.pojo.billStatistics;


public class IncomeByPay{
	
	public final static IncomeByPay DUMMY = new IncomeByPay();
	
	private int mCashAmount;			//现金账单数
	private float mCashIncome;			//现金金额
	private float mCashActual;			//现金实收
	
	private int mCreditCardAmount;		//刷卡账单数
	private float mCreditCardIncome;	//刷卡金额
	private float mCreditCardActual;	//刷卡实收
	
	private int mMemeberCardAmount;		//会员卡账单数
	private float mMemberCardIncome;	//会员卡金额
	private float mMemberCardActual;	//会员卡实收
	
	private int mSignAmount;			//签单账单数
	private float mSignIncome;			//签单金额
	private float mSignActual;			//签单实收
	
	private int mHangAmount;			//挂账账单数
	private float mHangIncome;			//挂账金额
	private float mHangActual;			//挂账实收
	
	public int getOrderAmount(){
		return getCashAmount() + 
			   getCreditCardAmount() + 
			   getMemberCardAmount() + 
			   getHangAmount() +
			   getSignAmount();
	}
	
	public void setCashAmount(int cashAmount){
		this.mCashAmount = cashAmount;
	}
	
	public int getCashAmount(){
		return this.mCashAmount;
	}
	
	public float getCashActual() {
		return this.mCashActual;
	}

	public void setCashActual(float cashActual) {
		this.mCashActual = cashActual;
	}

	public float getCashIncome() {
		return mCashIncome;
	}

	public void setCashIncome(float cashIncome) {
		this.mCashIncome = cashIncome;
	}
	
	public int getCreditCardAmount() {
		return mCreditCardAmount;
	}

	public void setCreditCardAmount(int creditCardAmount) {
		this.mCreditCardAmount = creditCardAmount;
	}

	public float getCreditCardIncome() {
		return mCreditCardIncome;
	}

	public void setCreditCardIncome(float creditCardIncome) {
		this.mCreditCardIncome = creditCardIncome;
	}

	public float getCreditCardActual(){
		return this.mCreditCardActual;
	}
	
	public void setCreditCardActual(float creditCardActual) {
		this.mCreditCardActual = creditCardActual;
	}

	public int getMemberCardAmount() {
		return mMemeberCardAmount;
	}

	public void setMemeberCardAmount(int memeberCardAmount) {
		this.mMemeberCardAmount = memeberCardAmount;
	}

	public float getMemberCardIncome() {
		return mMemberCardIncome;
	}

	public void setMemberCardIncome(float memberCardIncome) {
		this.mMemberCardIncome = memberCardIncome;
	}

	public float getMemberCardActual() {
		return mMemberCardActual;
	}

	public void setMemberCardActual(float memberCardActual) {
		this.mMemberCardActual = memberCardActual;
	}

	public int getSignAmount() {
		return mSignAmount;
	}

	public void setSignAmount(int signAmount) {
		this.mSignAmount = signAmount;
	}

	public float getSignIncome() {
		return mSignIncome;
	}

	public void setSignIncome(float signIncome) {
		this.mSignIncome = signIncome;
	}

	public float getSignActual() {
		return mSignActual;
	}

	public void setSignActual(float signActual) {
		this.mSignActual = signActual;
	}

	public int getHangAmount() {
		return mHangAmount;
	}

	public void setHangAmount(int hangAmount) {
		this.mHangAmount = hangAmount;
	}

	public float getHangIncome() {
		return mHangIncome;
	}

	public void setHangIncome(float hangIncome) {
		this.mHangIncome = hangIncome;
	}

	public float getHangActual() {
		return mHangActual;
	}

	public void setHangActual(float hangActual) {
		this.mHangActual = hangActual;
	}

	public float getTotalActual() {
		return getCashActual() + 
			   getCreditCardActual() + 
			   getMemberCardActual() + 
			   getSignActual() + 
			   getHangActual();
	}

	public float getTotalIncome() {
		return getTotalIncome() + 
			   getCreditCardIncome() + 
			   getMemberCardIncome() + 
			   getSignIncome() + 
			   getHangIncome();
	}

}
