package com.wireless.pojo.billStatistics;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class ShiftDetail implements Jsonable{
	private String onDuty;				//开始时间
	private String offDuty;				//结束时间
	
	private IncomeByPay incomeByPay;	//各种收款方式数据
	
	private IncomeByDiscount incomeByDiscount;	//折扣数据
	
	private IncomeByGift incomeByGift;			//赠送数据
	
	private IncomeByCancel incomeByCancel;		//退菜数据
	
	private IncomeByCoupon incomeByCoupon;		//优惠券数据
	
	private IncomeByService incomeByService;	//服务费数据
	
	private IncomeByRepaid incomeByRepaid;		//反结账数据
	
	private IncomeByErase incomeByErase;		//抹数数据
	
	private IncomeByCharge incomeByCharge; 	//会员充值信息
	
	private List<IncomeByDept> deptIncome;	//所有部门营业额

	public String getOnDuty() {
		return onDuty;
	}
	
	public void setOnDuty(String onDuty) {
		this.onDuty = onDuty;
	}
	
	public String getOffDuty() {
		return offDuty;
	}
	
	public void setOffDuty(String offDuty) {
		this.offDuty = offDuty;
	}
	
	public int getOrderAmount() {
		if(incomeByPay != null){
			return incomeByPay.getOrderAmount();
		}else{
			return 0;
		}
	}
	
	public void setIncomeByPay(IncomeByPay incomeByPay){
		this.incomeByPay = incomeByPay;
	}
	
	public int getCashAmount() {
		if(incomeByPay != null){
			return incomeByPay.getCashAmount();
		}else{
			return 0;
		}
	}
	
	public float getCashTotalIncome() {
		if(incomeByPay != null){
			return incomeByPay.getTotalIncome();
		}else{
			return 0;
		}
	}
	
	public float getCashActualIncome() {
		if(incomeByPay != null){
			return incomeByPay.getTotalActual();
		}else{
			return 0;
		}
	}
	
	public int getCreditCardAmount() {
		if(incomeByPay != null){
			return incomeByPay.getCreditCardAmount();
		}else{
			return 0;
		}
	}
	
	public float getCreditTotalIncome() {
		if(incomeByPay != null){
			return incomeByPay.getCreditCardIncome();
		}else{
			return 0;
		}
	}
	
	public float getCreditActualIncome() {
		if(incomeByPay != null){
			return incomeByPay.getCreditCardActual();
		}else{
			return 0;
		}
	}
	
	public int getMemberCardAmount() {
		if(incomeByPay != null){
			return incomeByPay.getMemberCardAmount();
		}else{
			return 0;
		}
	}
	
	public float getMemberTotalIncome() {
		if(incomeByPay != null){
			return incomeByPay.getMemberCardIncome();
		}else{
			return 0;
		}
	}
	
	public float getMemberActualIncome() {
		if(incomeByPay != null){
			return incomeByPay.getMemberCardActual();
		}else{
			return 0;
		}
	}
	
	public int getSignAmount() {
		if(incomeByPay != null){
			return incomeByPay.getSignAmount();
		}else{
			return 0;
		}
	}
	
	public float getSignTotalIncome() {
		if(incomeByPay != null){
			return incomeByPay.getSignIncome();
		}else{
			return 0;
		}
	}
	
	public float getSignActualIncome() {
		if(incomeByPay != null){
			return incomeByPay.getSignActual();
		}else{
			return 0;
		}
	}
	
	public int getHangAmount() {
		if(incomeByPay != null){
			return incomeByPay.getHangAmount();
		}else{
			return 0;
		}
	}
	
	public float getHangTotalIncome() {
		if(incomeByPay != null){
			return incomeByPay.getHangIncome();
		}else{
			return 0;
		}
	}
	
	public float getHangActualIncome() {
		if(incomeByPay != null){
			return incomeByPay.getHangActual();
		}else{
			return 0;
		}
	}
	
	public float getTotalActual() {
		if(incomeByPay != null){
			return incomeByPay.getTotalActual();
		}else{
			return 0;
		}
	}
	
	public void setDiscountIncome(IncomeByDiscount discountIncome){
		this.incomeByDiscount = discountIncome;
	}
	
	public int getDiscountAmount() {
		if(incomeByDiscount != null){
			return incomeByDiscount.getDiscountAmount();
		}else{
			return 0;
		}
	}
	
	public float getDiscountIncome() {
		if(incomeByDiscount != null){
			return incomeByDiscount.getTotalDiscount();
		}else{
			return 0;
		}
	}
	
	public void setGiftIncome(IncomeByGift giftIncome){
		this.incomeByGift = giftIncome;
	}
	
	public int getGiftAmount() {
		if(incomeByGift != null){
			return incomeByGift.getGiftAmount();
		}else{
			return 0;
		}
	}
	
	public float getGiftIncome() {
		if(incomeByGift != null){
			return incomeByGift.getTotalGift();
		}else{
			return 0;
		}
	}
	
	public void setCancelIncome(IncomeByCancel cancelIncome){
		this.incomeByCancel = cancelIncome;
	}
	
	public int getCancelAmount() {
		if(incomeByCancel != null){
			return incomeByCancel.getCancelAmount();
		}else{
			return 0;
		}
	}
	
	public float getCancelIncome() {
		if(incomeByCancel != null){
			return incomeByCancel.getTotalCancel();
		}else{
			return 0;
		}
	}
	
	public void setCouponIncome(IncomeByCoupon couponIncome){
		this.incomeByCoupon = couponIncome;
	}
	
	public int getCouponAmount(){
		if(incomeByCoupon != null){
			return this.incomeByCoupon.getCouponAmount();
		}else{
			return 0;
		}
	}
	
	public float getCouponIncome(){
		if(incomeByCoupon != null){
			return this.incomeByCoupon.getTotalCoupon();
		}else{
			return 0;
		}
	}
	
	public void setServiceIncome(IncomeByService serviceIncome){
		this.incomeByService = serviceIncome;
	}
	
	public int getServiceAmount() {
		if(incomeByService != null){
			return incomeByService.getServiceAmount();
		}else{
			return 0;
		}
	}
	
	public float getServiceIncome() {
		if(incomeByService != null){
			return incomeByService.getTotalService();
		}else{
			return 0;
		}
	}
	
	public void setRepaidIncome(IncomeByRepaid repaidIncome){
		this.incomeByRepaid = repaidIncome;
	}
	
	public int getPaidAmount() {
		if(incomeByRepaid != null){
			return incomeByRepaid.getRepaidAmount();
		}else{
			return 0;
		}
	}
	
	public float getPaidIncome() {
		if(incomeByRepaid != null){
			return incomeByRepaid.getTotalRepaid();
		}else{
			return 0;
		}
	}
	
	public void setEraseIncome(IncomeByErase eraseIncome){
		this.incomeByErase = eraseIncome;
	}
	
	public int getEraseAmount() {
		if(incomeByErase != null){
			return incomeByErase.getEraseAmount();
		}else{
			return 0;
		}
	}
	
	public float getEraseIncome() {
		if(incomeByErase != null){
			return incomeByErase.getTotalErase();
		}else{
			return 0;
		}
	}
	
	public List<IncomeByDept> getDeptIncome() {
		return deptIncome;
	}
	
	public void setDeptIncome(List<IncomeByDept> deptIncome) {
		this.deptIncome = deptIncome;
	}

	public void setIncomeByCharge(IncomeByCharge incomeByCharge){
		this.incomeByCharge = incomeByCharge;
	}

	public IncomeByCharge getIncomeByCharge(){
		return this.incomeByCharge;
	}
	
	public float getTotalIncome() {
		if(incomeByPay != null){
			return incomeByPay.getTotalIncome();
		}else{
			return 0;
		}
	}

	@Override 
	public String toString(){
		return "ShiftDetail : " +
			   "orderAmount " + this.getOrderAmount() +
			   ",onDuty" + this.onDuty +
			   ",offDuty" + this.offDuty;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof ShiftDetail)){
			return false;
		}else{
			return this.offDuty == ((ShiftDetail) obj).offDuty && this.onDuty == ((ShiftDetail) obj).onDuty;
		}
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = (int) (result * 31 + DateUtil.parseDate(offDuty));
		return result;
	}

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("paramsOnDuty", this.onDuty);
		jm.put("paramsOffDuty", this.offDuty);
		jm.put("totalActual", this.getTotalActual());
		jm.put("totalIncome", this.getTotalIncome());
		jm.put("orderAmount", this.getOrderAmount());
		
		jm.put("cashIncome2", this.getCashActualIncome());
		jm.put("cashAmount", this.getCashAmount());
		
		jm.put("creditCardIncome2", this.getCreditActualIncome());
		jm.put("creditCardAmount", this.getCreditCardAmount());
		
		jm.put("hangIncome2", this.getHangActualIncome());
		jm.put("hangAmount", this.getHangAmount());
		
		jm.put("signIncome2", this.getSignActualIncome());
		jm.put("signAmount", this.getSignAmount());
		
		jm.put("memberAmount", this.getMemberCardAmount());
		jm.put("memberActual", this.getMemberActualIncome());
		
		jm.put("discountIncome", this.getDiscountIncome());
		
		jm.put("giftIncome", this.getGiftIncome());
		
		jm.put("cancelIncome", this.getCancelIncome());
		
		jm.put("eraseIncome", this.getEraseIncome());
		
		jm.put("paidIncome", this.getPaidIncome());
		jm.put("serviceAmount", this.getServiceAmount());
		jm.put("serviceIncome", this.getServiceIncome());
		
		jm.put("deptStat", this.getDeptIncome());
		jm.put("paidAmount", this.getPaidAmount());
		jm.put("eraseAmount", this.getEraseAmount());
		jm.put("cancelAmount", this.getCancelAmount());
		jm.put("giftAmount", this.getGiftAmount());
		jm.put("discountAmount", this.getDiscountAmount());
		jm.put("signIncome", this.getSignTotalIncome());
		jm.put("hangIncome", this.getHangTotalIncome());
		jm.put("creditCardIncome", this.getCreditTotalIncome());
		jm.put("cashIncome", this.getCashTotalIncome());
		jm.put("memberIncome", this.getMemberTotalIncome());
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
}