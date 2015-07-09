package com.wireless.pojo.billStatistics;

import java.util.Collections;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class ShiftDetail implements Jsonable{
	private final String onDuty;				//开始时间
	private final String offDuty;				//结束时间
	
	private IncomeByPay incomeByPay;	//各种收款方式数据
	
	private IncomeByDiscount incomeByDiscount;	//折扣数据
	
	private IncomeByGift incomeByGift;			//赠送数据
	
	private IncomeByCancel incomeByCancel;		//退菜数据
	
	private IncomeByCoupon incomeByCoupon;		//优惠券数据
	
	private IncomeByService incomeByService;	//服务费数据
	
	private IncomeByRepaid incomeByRepaid;		//反结账数据
	
	private IncomeByErase incomeByErase;		//抹数数据
	
	private IncomeByCharge incomeByCharge; 		//会员充值信息
	
	private IncomeByBook incomeByBook;			//预订订金
	
	private List<IncomeByDept> deptIncome;		//所有部门营业额

	public ShiftDetail(DutyRange range){
		this.onDuty = range.getOnDutyFormat();
		this.offDuty = range.getOffDutyFormat();
	}
	
	public String getOnDuty() {
		return onDuty;
	}
	
	public String getOffDuty() {
		return offDuty;
	}
	
	public IncomeByPay getIncomeByPay(){
		if(this.incomeByPay != null){
			return this.incomeByPay;
		}else{
			return IncomeByPay.DUMMY;
		}
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
		if(deptIncome == null){
			return Collections.emptyList();
		}
		return deptIncome;
	}
	
	public void setDeptIncome(List<IncomeByDept> deptIncome) {
		this.deptIncome = deptIncome;
	}

	public void setIncomeByCharge(IncomeByCharge incomeByCharge){
		this.incomeByCharge = incomeByCharge;
	}

	public IncomeByCharge getIncomeByCharge(){
		if(this.incomeByCharge == null){
			return IncomeByCharge.DUMMY;
		}
		return this.incomeByCharge;
	}
	
	public float getTotalIncome() {
		if(incomeByPay != null){
			return incomeByPay.getTotalIncome();
		}else{
			return 0;
		}
	}

	public void setIncomeByBook(IncomeByBook bookIncome){
		this.incomeByBook = bookIncome;
	}
	
	public IncomeByBook getIncomeByBook(){
		if(this.incomeByBook == null){
			return IncomeByBook.DUMMY;
		}
		return this.incomeByBook;
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
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("paramsOnDuty", this.onDuty);
		jm.putString("paramsOffDuty", this.offDuty);
		jm.putFloat("totalActual", this.getTotalActual());
		jm.putFloat("totalIncome", this.getTotalIncome());
		jm.putInt("orderAmount", this.getOrderAmount());
		
		//FIXME
//		jm.putFloat("cashIncome2", this.getCashActualIncome());
//		jm.putInt("cashAmount", this.getCashAmount());
//		
//		jm.putFloat("creditCardIncome2", this.getCreditActualIncome());
//		jm.putInt("creditCardAmount", this.getCreditCardAmount());
//		
//		jm.putFloat("hangIncome2", this.getHangActualIncome());
//		jm.putInt("hangAmount", this.getHangAmount());
//		
//		jm.putFloat("signIncome2", this.getSignActualIncome());
//		jm.putInt("signAmount", this.getSignAmount());
//		
//		jm.putInt("memberAmount", this.getMemberCardAmount());
//		jm.putFloat("memberActual", this.getMemberActualIncome());
		
		jm.putFloat("discountIncome", this.getDiscountIncome());
		
		jm.putFloat("giftIncome", this.getGiftIncome());
		
		jm.putFloat("couponIncome", this.getCouponIncome());
		
		jm.putFloat("cancelIncome", this.getCancelIncome());
		
		jm.putFloat("eraseIncome", this.getEraseIncome());
		
		jm.putFloat("paidIncome", this.getPaidIncome());
		jm.putInt("serviceAmount", this.getServiceAmount());
		jm.putFloat("serviceIncome", this.getServiceIncome());
		
		jm.putFloat("bookIncome", this.getIncomeByBook().getIncome());
		
		jm.putJsonableList("deptStat", this.getDeptIncome(), 0);
		jm.putInt("paidAmount", this.getPaidAmount());
		jm.putInt("eraseAmount", this.getEraseAmount());
		jm.putInt("couponAmount", this.getCouponAmount());
		jm.putInt("cancelAmount", this.getCancelAmount());
		jm.putInt("giftAmount", this.getGiftAmount());
		jm.putInt("discountAmount", this.getDiscountAmount());
		
		jm.putJsonable(this.getIncomeByPay(), flag);
		
		jm.putFloat("memberChargeByCash", this.getIncomeByCharge().getActualCashCharge());
		jm.putFloat("memberChargeByCard", this.getIncomeByCharge().getActualCreditCardCharge());
		jm.putFloat("memberAccountCharge", this.getIncomeByCharge().getTotalAccountCharge());
		jm.putFloat("memberRefund", this.getIncomeByCharge().getTotalActualRefund());
		jm.putFloat("memberAccountRefund", this.getIncomeByCharge().getTotalAccountRefund());
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}