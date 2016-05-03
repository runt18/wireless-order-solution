package com.wireless.pojo.billStatistics;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.member.IncomeByCharge;
import com.wireless.pojo.util.DateUtil;

public class IncomeByEachDay implements Jsonable{

	private final String date;
	
	private int customerAmount;
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
	
	public void setCustomerAmount(int amount){
		this.customerAmount = amount;
	}
	
	public int getCustomerAmount(){
		return this.customerAmount;
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
	
	public void append(IncomeByEachDay appendIncome){
		//Append the customer amount.
		this.customerAmount += appendIncome.customerAmount;
		
		//Append the income by pay.
		for(IncomeByPay.PaymentIncome payIncome : appendIncome.getIncomeByPay().getPaymentIncomes()){
			if(this.incomeByPay == null){
				this.incomeByPay = new IncomeByPay(0);
			}
			this.incomeByPay.addIncome4Chain(payIncome);
		}
		
		//Append the total & amount to erase price.
		append(appendIncome.getIncomeByErase());
		
		//Append the total & amount to discount price.
		append(appendIncome.getIncomeByDiscount());
		
		//Append the total & amount to gift price.
		append(appendIncome.getIncomeByGift());
		
		//Append the total & amount to cancel price.
		append(appendIncome.getIncomeByCancel());
		
		//Append the total & amount to coupon price.
		append(appendIncome.getIncomeByCoupon());
		
		//Append the total & amount to repaid order.
		append(appendIncome.getIncomeByRepaid());
		
		//Append the total & amount to order with service.
		append(appendIncome.getIncomeByService());
		
		//Append the charge income by both cash and credit card.
		append(appendIncome.getIncomeByCharge());
	}
	
	private void append(IncomeByErase eraseIncome){
		if(this.incomeByErase != null){
			this.incomeByErase.setEraseAmount(this.incomeByErase.getEraseAmount() + eraseIncome.getEraseAmount());
			this.incomeByErase.setErasePrice(this.incomeByErase.getTotalErase() + eraseIncome.getTotalErase());
		}else{
			this.incomeByErase = eraseIncome;
		}
	}
	
	private void append(IncomeByDiscount discountIncome){
		if(this.incomeByDiscount != null){
			this.incomeByDiscount.setDiscountAmount(this.incomeByDiscount.getDiscountAmount() + discountIncome.getDiscountAmount());
			this.incomeByDiscount.setTotalDiscount(this.incomeByDiscount.getTotalDiscount() + discountIncome.getTotalDiscount());
		}else{
			this.incomeByDiscount = discountIncome;
		}
	}
	
	private void append(IncomeByGift giftIncome){
		if(this.incomeByGift != null){
			this.incomeByGift.setGiftAmount(this.incomeByGift.getGiftAmount() + giftIncome.getGiftAmount());
			this.incomeByGift.setTotalGift(this.incomeByGift.getTotalGift() + giftIncome.getTotalGift());
		}else{
			this.incomeByGift = giftIncome;
		}
	}
	
	private void append(IncomeByCancel cancelIncome){
		if(this.incomeByCancel != null){
			this.incomeByCancel.setCancelAmount(this.incomeByCancel.getCancelAmount() + cancelIncome.getCancelAmount());
			this.incomeByCancel.setTotalCancel(this.incomeByCancel.getTotalCancel() + cancelIncome.getTotalCancel());
		}else{
			this.incomeByCancel = cancelIncome;
		}
	}
	
	private void append(IncomeByService serviceIncome){
		if(this.incomeByService != null){
			this.incomeByService.setServiceAmount(this.incomeByService.getServiceAmount() + serviceIncome.getServiceAmount());
			this.incomeByService.setTotalService(this.incomeByService.getTotalService() + serviceIncome.getTotalService());
		}else{
			this.incomeByService = serviceIncome;
		}
	}
	
	private void append(IncomeByRepaid repaidIncome){
		if(this.incomeByRepaid != null){
			this.incomeByRepaid.setRepaidAmount(this.incomeByRepaid.getRepaidAmount() + repaidIncome.getRepaidAmount());
			this.incomeByRepaid.setTotalRepaid(this.incomeByRepaid.getTotalRepaid() + repaidIncome.getTotalRepaid());
		}else{
			this.incomeByRepaid = repaidIncome;
		}
	}
	
	private void append(IncomeByCoupon couponIncome){
		if(this.incomeByCoupon != null){
			this.incomeByCoupon.setCouponAmount(this.incomeByCoupon.getCouponAmount() + couponIncome.getCouponAmount());
			this.incomeByCoupon.setTotalCoupon(this.incomeByCoupon.getTotalCoupon() + couponIncome.getTotalCoupon());
		}else{
			this.incomeByCoupon = couponIncome;
		}
	}
	
	private void append(IncomeByCharge chargeIncome){
		if(this.incomeByCharge != null){
			this.incomeByCharge.setActualCashCharge(this.incomeByCharge.getActualCashCharge() + chargeIncome.getActualCashCharge());
			this.incomeByCharge.setActualCreditCardCharge(this.incomeByCharge.getActualCreditCardCharge() + chargeIncome.getActualCreditCardCharge());
			this.incomeByCharge.setChargeAmount(this.incomeByCharge.getChargeAmount() + chargeIncome.getChargeAmount());
			this.incomeByCharge.setRefundAmount(this.incomeByCharge.getRefundAmount() + chargeIncome.getRefundAmount());
			this.incomeByCharge.setTotalAccountCharge(this.incomeByCharge.getTotalAccountCharge() + chargeIncome.getTotalAccountCharge());
			this.incomeByCharge.setTotalAccountRefund(this.incomeByCharge.getTotalAccountRefund() + chargeIncome.getTotalAccountRefund());
			this.incomeByCharge.setTotalActualRefund(this.incomeByCharge.getTotalActualRefund() + chargeIncome.getTotalActualRefund());
		}else{
			this.incomeByCharge = chargeIncome;
		}
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
		jm.putInt("customerAmount", this.customerAmount);
		
		jm.putJsonable(this.getIncomeByPay(), flag);
		
		jm.putFloat("discountIncome", this.getIncomeByDiscount().getTotalDiscount());
		
		jm.putFloat("giftIncome", this.getIncomeByGift().getTotalGift());
		
		jm.putFloat("cancelIncome", this.getIncomeByCancel().getTotalCancel());
		
		jm.putFloat("eraseIncome", this.getIncomeByErase().getTotalErase());
		
		jm.putFloat("couponIncome", this.getIncomeByCoupon().getTotalCoupon());
		
		jm.putFloat("paidIncome", this.getIncomeByRepaid().getTotalRepaid());
		
		jm.putInt("eraseAmount", this.getIncomeByErase().getEraseAmount());
		jm.putInt("couponAmount", this.getIncomeByCoupon().getCouponAmount());
		jm.putInt("cancelAmount", this.getIncomeByCancel().getCancelAmount());
		jm.putInt("giftAmount", this.getIncomeByGift().getGiftAmount());
		jm.putInt("discountAmount", this.getIncomeByDiscount().getDiscountAmount());
		jm.putFloat("totalActualCharge", this.getIncomeByCharge().getTotalActualCharge());
		jm.putFloat("totalActualRefund", this.getIncomeByCharge().getTotalActualRefund());
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}
