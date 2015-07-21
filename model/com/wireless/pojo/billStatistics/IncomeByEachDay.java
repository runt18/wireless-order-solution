package com.wireless.pojo.billStatistics;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.member.IncomeByCharge;
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
		return getIncomeByPay().getTotalActual();
	}
	
	public int getTotalAmount(){
		return getIncomeByPay().getOrderAmount();
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
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("offDutyToDate", this.date);
		jm.putFloat("totalActual", this.incomeByPay == null ? 0 : this.incomeByPay.getTotalActual());
		jm.putFloat("totalIncome", this.incomeByPay == null ? 0 : this.incomeByPay.getTotalIncome());
		jm.putInt("orderAmount", this.getTotalAmount());
		
		jm.putJsonable(this.getIncomeByPay(), flag);
		
		//FIXME
//		jm.putFloat("cashIncome2", this.incomeByPay == null ? 0 : this.incomeByPay.getCashActual());
//		jm.putInt("cashAmount", this.incomeByPay == null ? 0 : this.incomeByPay.getCashAmount());
//		
//		jm.putFloat("creditCardIncome2", this.incomeByPay == null ? 0 : this.incomeByPay.getCreditCardActual());
//		jm.putInt("creditCardAmount", this.incomeByPay == null ? 0 : this.incomeByPay.getCreditCardAmount());
//		
//		jm.putFloat("hangIncome2", this.incomeByPay == null ? 0 : this.incomeByPay.getHangActual());
//		jm.putInt("hangAmount", this.incomeByPay == null ? 0 : this.incomeByPay.getHangAmount());
//		
//		jm.putFloat("signIncome2", this.incomeByPay == null ? 0 : this.incomeByPay.getSignActual());
//		jm.putInt("signAmount", this.incomeByPay == null ? 0 : this.incomeByPay.getSignAmount());
//		
//		jm.putFloat("memberActual", this.incomeByPay == null ? 0 : this.incomeByPay.getMemberCardActual());
//		jm.putInt("memberAmount", this.incomeByPay == null ? 0 : this.incomeByPay.getMemberCardAmount());
		
		jm.putFloat("discountIncome", this.getIncomeByDiscount().getTotalDiscount());
		
		jm.putFloat("giftIncome", this.getIncomeByGift().getTotalGift());
		
		jm.putFloat("cancelIncome", this.getIncomeByCancel().getTotalCancel());
		
		jm.putFloat("eraseIncome", this.getIncomeByErase().getTotalErase());
		
		jm.putFloat("couponIncome", this.getIncomeByCoupon().getTotalCoupon());
		
		jm.putFloat("paidIncome", this.getIncomeByRepaid().getTotalRepaid());
		
//		if(flag > 0){
			jm.putInt("paidAmount", this.getIncomeByRepaid().getRepaidAmount());
			jm.putInt("eraseAmount", this.getIncomeByErase().getEraseAmount());
			jm.putInt("couponAmount", this.getIncomeByCoupon().getCouponAmount());
			jm.putInt("cancelAmount", this.getIncomeByCancel().getCancelAmount());
			jm.putInt("giftAmount", this.getIncomeByGift().getGiftAmount());
			jm.putInt("discountAmount", this.getIncomeByDiscount().getDiscountAmount());
			//FIXME
//			jm.putFloat("signIncome", this.incomeByPay == null ? 0 : this.incomeByPay.getSignIncome());
//			jm.putFloat("hangIncome", this.incomeByPay == null ? 0 : this.incomeByPay.getHangIncome());
//			jm.putFloat("creditCardIncome", this.incomeByPay == null ? 0 : this.incomeByPay.getCreditCardIncome());
//			jm.putFloat("cashIncome", this.incomeByPay == null ? 0 : this.incomeByPay.getCashIncome());
//			jm.putFloat("memberIncome", this.incomeByPay == null ? 0 : this.incomeByPay.getMemberCardIncome());
			jm.putFloat("totalActualCharge", this.getIncomeByCharge().getTotalActualCharge());
			jm.putFloat("totalActualRefund", this.getIncomeByCharge().getTotalActualRefund());
//		}
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}
