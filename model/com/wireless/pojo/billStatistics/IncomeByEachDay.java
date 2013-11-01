package com.wireless.pojo.billStatistics;

public class IncomeByEachDay {

	private final String date;
	
	private IncomeByPay incomeByPay;
	private IncomeByErase incomeByErase;
	private IncomeByDiscount incomeByDiscount;
	private IncomeByGift incomeByGift;
	private IncomeByCancel incomeByCancel;
	private IncomeByRepaid incomeByRepaid;
	private IncomeByService incomeByService;
	private IncomeByCharge incomeByCharge;
	
	public IncomeByEachDay(String date){
		this.date = date;
	}
	
	public String getDate(){
		return date;
	}
	
	public float getTotalActual(){
		return getIncomeByPay().getCashActual() +
			   getIncomeByPay().getCreditCardActual() +
			   getIncomeByPay().getHangActual() +
			   getIncomeByPay().getSignActual() +
			   getIncomeByPay().getMemberCardActual();
	}
	
	public int getTotalAmount(){
		return getIncomeByPay().getCashAmount() +
			   getIncomeByPay().getCreditCardAmount() +
			   getIncomeByPay().getHangAmount() +
			   getIncomeByPay().getSignAmount() +
			   getIncomeByPay().getMemberCardAmount();
	}
	
	public IncomeByPay getIncomeByPay() {
		if(incomeByPay == null){
			return IncomeByPay.DUMMY;
		}
		return incomeByPay;
	}
	
	public void setIncomeByPay(IncomeByPay incomeByPay) {
		this.incomeByPay = incomeByPay;
	}
	
	public IncomeByErase getIncomeByErase() {
		if(incomeByErase == null){
			return IncomeByErase.DUMMY;
		}
		return incomeByErase;
	}
	
	public void setIncomeByErase(IncomeByErase incomeByErase) {
		this.incomeByErase = incomeByErase;
	}
	
	public IncomeByDiscount getIncomeByDiscount() {
		if(incomeByDiscount == null){
			return IncomeByDiscount.DUMMY;
		}
		return incomeByDiscount;
	}
	
	public void setIncomeByDiscount(IncomeByDiscount incomeByDiscount) {
		this.incomeByDiscount = incomeByDiscount;
	}
	
	public IncomeByGift getIncomeByGift() {
		if(incomeByGift == null){
			return IncomeByGift.DUMMY;
		}
		return incomeByGift;
	}
	
	public void setIncomeByGift(IncomeByGift incomeByGift) {
		this.incomeByGift = incomeByGift;
	}
	
	public IncomeByCancel getIncomeByCancel() {
		if(incomeByCancel == null){
			return IncomeByCancel.DUMMY;
		}
		return incomeByCancel;
	}
	
	public void setIncomeByCancel(IncomeByCancel incomeByCancel) {
		this.incomeByCancel = incomeByCancel;
	}
	
	public IncomeByRepaid getIncomeByRepaid() {
		if(incomeByRepaid == null){
			return IncomeByRepaid.DUMMY;
		}
		return incomeByRepaid;
	}
	
	public void setIncomeByRepaid(IncomeByRepaid incomeByRepaid) {
		this.incomeByRepaid = incomeByRepaid;
	}
	
	public IncomeByService getIncomeByService() {
		if(incomeByService == null){
			return IncomeByService.DUMMY;
		}
		return incomeByService;
	}
	
	public void setIncomeByService(IncomeByService incomeByService) {
		this.incomeByService = incomeByService;
	}
	
	public IncomeByCharge getIncomeByCharge() {
		if(incomeByCharge == null){
			return IncomeByCharge.DUMMY;
		}
		return incomeByCharge;
	}
	
	public void setIncomeByCharge(IncomeByCharge incomeByCharge) {
		this.incomeByCharge = incomeByCharge;
	}
}
