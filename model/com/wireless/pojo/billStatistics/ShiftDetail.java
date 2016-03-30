package com.wireless.pojo.billStatistics;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.member.IncomeByCharge;
import com.wireless.pojo.util.DateUtil;

public class ShiftDetail implements Jsonable{
	private final String onDuty;				//开始时间
	private final String offDuty;				//结束时间
	
	private int customerAmount;					//客流量
	
	private IncomeByPay incomeByPay;	//各种收款方式数据
	
	private IncomeByDiscount incomeByDiscount;	//折扣数据
	
	private IncomeByGift incomeByGift;			//赠送数据
	
	private IncomeByCancel incomeByCancel;		//退菜数据
	
	private IncomeByCoupon incomeByCoupon;		//优惠券数据
	
	private IncomeByService incomeByService;	//服务费数据
	
	private IncomeByRepaid incomeByRepaid;		//反结账数据
	
	private IncomeByErase incomeByErase;		//抹数数据
	
	private IncomeByRound incomeByRound;		//尾数数据
	
	//private IncomeByMemberPrice incomeByMemberPrice;	//会员价数据
	
	private IncomeByCharge incomeByCharge; 		//会员充值信息
	
	private IncomeByBook incomeByBook;			//预订订金
	
	private CouponUsage couponUsage;			//优惠券使用情况
	
	private List<IncomeByDept> deptIncome;		//所有部门营业额

	public ShiftDetail(DutyRange range){
		this.onDuty = range.getOnDutyFormat();
		this.offDuty = range.getOffDutyFormat();
	}
	
	public void append(ShiftDetail appendDetail){
		//Append the customer amount.
		this.customerAmount += appendDetail.customerAmount;
		
		//Append the income by pay.
		for(IncomeByPay.PaymentIncome payIncome : appendDetail.getIncomeByPay().getPaymentIncomes()){
			this.incomeByPay.addIncome4Chain(payIncome);
		}
		
		//Append the total & amount to erase price.
		append(appendDetail.incomeByErase);
		
		//Append the total & amount to discount price.
		append(appendDetail.incomeByDiscount);
		
		//Append the total & amount to gift price.
		append(appendDetail.incomeByGift);
		
		//Append the total & amount to cancel price.
		append(appendDetail.incomeByCancel);
		
		//Append the total & amount to coupon price.
		append(appendDetail.incomeByCoupon);
		
		//Append the total & amount to repaid order.
		append(appendDetail.incomeByRepaid);
		
		//Append the total & amount to order with service
		append(appendDetail.incomeByService);
		
		//Append the total & amount to round price.
		append(appendDetail.incomeByRound);
		
		//Append the income by charge.
		append(appendDetail.incomeByCharge);
		
		//Append the income by book.
		append(appendDetail.incomeByBook);
		
		//Append the coupon usage.
		append(appendDetail.couponUsage);
		
		//Append the gift, discount & total to each department during this period.
		append(appendDetail.deptIncome);
	}
	
	private void append(List<IncomeByDept> appendDeptIncome){
		if(this.deptIncome != null){
			//Group by the department name.
			Map<String, IncomeByDept> result = new HashMap<String, IncomeByDept>();
			for(IncomeByDept eachIncome : this.deptIncome){
				result.put(eachIncome.getDept().getName(), eachIncome);
			}
			for(IncomeByDept eachAppendDeptIncome : appendDeptIncome){
				if(result.containsKey(eachAppendDeptIncome.getDept().getName())){
					IncomeByDept income = result.get(eachAppendDeptIncome.getDept().getName());
					income.setDiscount(income.getDiscount() + eachAppendDeptIncome.getDiscount());
					income.setGift(income.getGift() + eachAppendDeptIncome.getGift());
					income.setIncome(income.getIncome() + eachAppendDeptIncome.getIncome());
				}else{
					result.put(eachAppendDeptIncome.getDept().getName(), eachAppendDeptIncome);
				}
			}
			this.deptIncome.clear();
			this.deptIncome.addAll(result.values());
		}else{
			this.deptIncome = appendDeptIncome;
		}
	}
	
	private void append(CouponUsage couponUsage){
		if(this.couponUsage != null){
			for(CouponUsage.Usage used : couponUsage.getUsed()){
				this.couponUsage.addUse(used.getName(), used.getAmount(), used.getPrice());
			}
			for(CouponUsage.Usage issued : couponUsage.getIssued()){
				this.couponUsage.addIssue(issued.getName(), issued.getAmount(), issued.getPrice());
			}
		}else{
			this.couponUsage = couponUsage;
		}
	}
	
	private void append(IncomeByBook bookIncome){
		if(this.incomeByBook != null){
			this.incomeByBook.setAmount(this.incomeByBook.getAmount() + bookIncome.getAmount());
			this.incomeByBook.setIncome(this.incomeByBook.getIncome() + bookIncome.getIncome());
		}else{
			this.incomeByBook = bookIncome;
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
	
	private void append(IncomeByRound roundIncome){
		if(this.incomeByRound != null){
			this.incomeByRound.setAmount(this.incomeByRound.getAmount() + roundIncome.getAmount());
			this.incomeByRound.setTotal(this.incomeByRound.getTotal() + roundIncome.getTotal());
		}else{
			this.incomeByRound = roundIncome;
		}
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
	
	public String getOnDuty() {
		return onDuty;
	}
	
	public String getOffDuty() {
		return offDuty;
	}
	
	public int getCustomerAmount(){
		return this.customerAmount;
	}
	
	public void setCustomerAmount(int amount){
		this.customerAmount = amount;
	}
	
	public void setCouponUsage(CouponUsage usage){
		this.couponUsage = usage;
	}
	
	public CouponUsage getCouponUsage(){
		return this.couponUsage;
	}
	
	public boolean hasCouponUsage(){
		if(this.couponUsage != null){
			return !this.couponUsage.getIssued().isEmpty() || !this.couponUsage.getUsed().isEmpty();
		}else{
			return false;
		}
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
	
	public void setRoundIncome(IncomeByRound roundIncome){
		this.incomeByRound = roundIncome;
	}
	
	public int getRoundAmount(){
		if(this.incomeByRound == null){
			return 0;
		}
		return this.incomeByRound.getAmount();
	}
	
	public float getRoundIncome(){
		if(this.incomeByRound == null){
			return 0;
		}
		return this.incomeByRound.getTotal();
	}
	
//	public void setMemberPriceIncome(IncomeByMemberPrice memberPriceIncome){
//		this.incomeByMemberPrice = memberPriceIncome;
//	}
//	
//	public int getMemberPriceAmount(){
//		if(this.incomeByMemberPrice != null){
//			return this.incomeByMemberPrice.getMemberPriceAmount();
//		}else{
//			return 0;
//		}
//	}
//	
//	public float getMemberPriceIncome(){
//		if(this.incomeByMemberPrice != null){
//			return this.incomeByMemberPrice.getMemberPrice();
//		}else{
//			return 0;
//		}
//	}
	
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
		jm.putInt("customerAmount", this.customerAmount);
		
		//各付款方式营收
		jm.putJsonable(this.getIncomeByPay(), flag);
		
		//折扣
		jm.putFloat("discountIncome", this.getDiscountIncome());
		jm.putInt("discountAmount", this.getDiscountAmount());
		
		//赠送
		jm.putFloat("giftIncome", this.getGiftIncome());
		jm.putInt("giftAmount", this.getGiftAmount());
		
		//优惠券
		jm.putFloat("couponIncome", this.getCouponIncome());
		jm.putInt("couponAmount", this.getCouponAmount());
		
		//退菜
		jm.putFloat("cancelIncome", this.getCancelIncome());
		jm.putInt("cancelAmount", this.getCancelAmount());
		
		//抹数
		jm.putFloat("eraseIncome", this.getEraseIncome());
		jm.putInt("eraseAmount", this.getEraseAmount());
		
		//反结账
		jm.putFloat("paidIncome", this.getPaidIncome());
		jm.putInt("paidAmount", this.getPaidAmount());
		
		//服务费
		jm.putInt("serviceAmount", this.getServiceAmount());
		jm.putFloat("serviceIncome", this.getServiceIncome());
		
		//尾数处理
		jm.putInt("roundAmount", this.getRoundAmount());
		jm.putFloat("roundIncome", this.getRoundIncome());
		
		//会员价
		//jm.putInt("memberPriceAmount", this.getMemberPriceAmount());
		//jm.putFloat("memberPriceIncome", this.getMemberPriceIncome());
		
		//预订金额
		jm.putFloat("bookIncome", this.getIncomeByBook().getIncome());
		
		//优惠券使用情况
		if(hasCouponUsage()){
			jm.putJsonable("couponUsage", this.getCouponUsage(), 0);
		}
		
		//部门营收
		jm.putJsonableList("deptStat", this.getDeptIncome(), 0);
		
		//会员现金实冲
		jm.putFloat("memberChargeByCash", this.getIncomeByCharge().getActualCashCharge());
		//会员刷卡实冲
		jm.putFloat("memberChargeByCard", this.getIncomeByCharge().getActualCreditCardCharge());
		//会员冲额
		jm.putFloat("memberAccountCharge", this.getIncomeByCharge().getTotalAccountCharge());
		//会员实退金额
		jm.putFloat("memberRefund", this.getIncomeByCharge().getTotalActualRefund());
		//会员账户扣额
		jm.putFloat("memberAccountRefund", this.getIncomeByCharge().getTotalAccountRefund());
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}