package com.wireless.pojo.billStatistics;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class IncomeByEachDay implements Jsonable{

	private final String date;
	
	private IncomeByPay incomeByPay;
	private IncomeByErase incomeByErase;
	private IncomeByDiscount incomeByDiscount;
	private IncomeByGift incomeByGift;
	private IncomeByCancel incomeByCancel;
	private IncomeByCoupon incomeByCoupon;
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
	
	public IncomeByCoupon getIncomeByCoupon(){
		if(incomeByCoupon == null){
			return IncomeByCoupon.DUMMY;
		}
		return incomeByCoupon;
	}
	
	public void setIncomeByCoupon(IncomeByCoupon incomeByCoupon){
		this.incomeByCoupon = incomeByCoupon;
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
	
	@Override 
	public String toString(){
		return "IncomeEachDay : " +
				"date " + this.date +
				", totalActual" + this.getTotalActual() +
				", totalAmount" + this.getTotalAmount();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof IncomeByEachDay)){
			return false;
		}else{
			return this.date == ((IncomeByEachDay) obj).date;
		}
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = (int) (result * 31 + DateUtil.parseDate(date));
		return result;
	}
	
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("offDutyToDate", this.date);
		jm.put("totalActual", this.incomeByPay == null ? 0 : this.incomeByPay.getTotalActual());
		jm.put("totalIncome", this.incomeByPay == null ? 0 : this.incomeByPay.getTotalIncome());
		jm.put("orderAmount", this.getTotalAmount());
		
		jm.put("cashIncome2", this.incomeByPay == null ? 0 : this.incomeByPay.getCashActual());
		jm.put("cashAmount", this.incomeByPay == null ? 0 : this.incomeByPay.getCashAmount());
		
		jm.put("creditCardIncome2", this.incomeByPay == null ? 0 : this.incomeByPay.getCreditCardActual());
		jm.put("creditCardAmount", this.incomeByPay == null ? 0 : this.incomeByPay.getCreditCardAmount());
		
		jm.put("hangIncome2", this.incomeByPay == null ? 0 : this.incomeByPay.getHangActual());
		jm.put("hangAmount", this.incomeByPay == null ? 0 : this.incomeByPay.getHangAmount());
		
		jm.put("signIncome2", this.incomeByPay == null ? 0 : this.incomeByPay.getSignActual());
		jm.put("signAmount", this.incomeByPay == null ? 0 : this.incomeByPay.getSignAmount());
		
		jm.put("memberAmount", this.incomeByPay == null ? 0 : this.incomeByPay.getMemberCardAmount());
		jm.put("memberActual", this.incomeByPay == null ? 0 : this.incomeByPay.getMemberCardActual());
		
		jm.put("discountIncome", this.getIncomeByDiscount().getTotalDiscount());
		
		jm.put("giftIncome", this.getIncomeByGift().getTotalGift());
		
		jm.put("cancelIncome", this.getIncomeByCancel().getTotalCancel());
		
		jm.put("eraseIncome", this.getIncomeByErase().getTotalErase());
		
		jm.put("paidIncome", this.getIncomeByRepaid().getTotalRepaid());
		
		if(flag > 0){
			jm.put("paidAmount", this.getIncomeByRepaid().getRepaidAmount());
			jm.put("eraseAmount", this.getIncomeByErase().getEraseAmount());
			jm.put("cancelAmount", this.getIncomeByCancel().getCancelAmount());
			jm.put("giftAmount", this.getIncomeByGift().getGiftAmount());
			jm.put("discountAmount", this.getIncomeByDiscount().getDiscountAmount());
			jm.put("signIncome", this.incomeByPay == null ? 0 : this.incomeByPay.getSignIncome());
			jm.put("hangIncome", this.incomeByPay == null ? 0 : this.incomeByPay.getHangIncome());
			jm.put("creditCardIncome", this.incomeByPay == null ? 0 : this.incomeByPay.getCreditCardIncome());
			jm.put("cashIncome", this.incomeByPay == null ? 0 : this.incomeByPay.getCashIncome());
			jm.put("memberIncome", this.incomeByPay == null ? 0 : this.incomeByPay.getMemberCardIncome());
		}
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
}
