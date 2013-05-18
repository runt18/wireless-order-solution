package com.wireless.pojo.billStatistics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.wireless.pojo.util.DateUtil;

public class BusinessStatistics {
	private long onDuty;				//开始时间
	private long offDuty;				//结束时间
	
	private int restaurantID;			//餐厅编号
	private int orderAmount;			//总账单数
	
	private int cashAmount;				//现金账单数
	private float cashIncome;			//现金金额
	private float cashIncome2;			//现金实收
	
	private int creditCardAmount;		//刷卡账单数
	private float creditCardIncome;		//刷卡金额
	private float creditCardIncome2;	//刷卡实收
	
	private int memberCardAmount;		//会员卡账单数
	private float memberCardIncome;		//会员卡金额
	private float memberCardIncome2;	//会员卡实收
	
	private int signAmount;				//签单账单数
	private float signIncome;			//签单金额
	private float signIncome2;			//签单实收
	
	private int hangAmount;				//挂账账单数
	private float hangIncome;			//挂账金额
	private float hangIncome2;			//挂账实收
	
	private int discountAmount;			//折扣账单数
	private float discountIncome;		//合计折扣金额
	
	private int giftAmount;				//赠送账单数
	private float giftIncome;			//合计赠送金额
	
	private int cancelAmount;			//退菜账单数
	private float cancelIncome;			//合计退菜金额
	
	private int serviceAmount;			//服务费账单数
	private float serviceIncome;		//服务费金额
	
	private int paidAmount;				//反结帐账单数
	private float paidIncome;			//反结帐金额
	
	private int eraseAmount;			//抹数账单数
	private float eraseIncome;			//抹数金额
	
	private float totalPrice;			//合计金额
	private float totalPrice2;			//实收金额
	
	private List<BusinessStatisticsByDept> deptStat;
	
	public BusinessStatistics(){}
	
	public BusinessStatistics(ShiftDetail res){
		if(res == null)
			return;
		
		this.onDuty = Date.parse(res.getOnDuty().replaceAll("-", "/"));
		this.offDuty = Date.parse(res.getOffDuty().replaceAll("-", "/"));
		
		this.orderAmount = res.getOrderAmount();
		
		this.cashAmount = res.getCashAmount();
		this.cashIncome = res.getCashTotalIncome();
		this.cashIncome2 = res.getCashActualIncome();
		
		this.creditCardAmount = res.getCreditCardAmount();
		this.creditCardIncome = res.getCreditTotalIncome();
		this.creditCardIncome2 = res.getCreditActualIncome();
		
		this.memberCardAmount = res.getMemberCardAmount();	
		this.memberCardIncome = res.getMemberTotalIncome();
		this.memberCardIncome2 = res.getMemberActualIncome();
		
		this.signAmount = res.getSignAmount();	
		this.signIncome = res.getSignTotalIncome();
		this.signIncome2 = res.getSignActualIncome();
		
		this.hangAmount = res.getHangAmount();	
		this.hangIncome = res.getHangTotalIncome();
		this.hangIncome2 = res.getHangActualIncome();
		
		this.discountAmount = res.getDiscountAmount();	
		this.discountIncome = res.getDiscountIncome();
		
		this.giftAmount = res.getGiftAmount();	
		this.giftIncome = res.getGiftIncome();
		
		this.cancelAmount = res.getCancelAmount();	
		this.cancelIncome = res.getCancelIncome();
		
		this.serviceAmount = res.getServiceAmount();	
		this.serviceIncome = res.getServiceIncome();
		
		this.paidAmount = res.getPaidAmount();	
		this.paidIncome = res.getPaidIncome();
		
		this.eraseAmount = res.getEraseAmount();	
		this.eraseIncome = res.getEraseIncome();
		
		this.totalPrice2 = res.getTotalActual();
		
		if(res.getDeptIncome() != null && res.getDeptIncome().size() > 0){
			this.deptStat = new ArrayList<BusinessStatisticsByDept>();
			BusinessStatisticsByDept temp = null;
			for(com.wireless.pojo.billStatistics.IncomeByDept dept : res.getDeptIncome()){
				temp = new BusinessStatisticsByDept(dept);
				this.deptStat.add(temp);
				temp = null;
			}
		}
	}
	
	public long getOnDuty() {
		return onDuty;
	}
	public String getOnDutyToDate() {
		return DateUtil.formatToDate(onDuty);
	}
	public String getOnDutyToSimple() {
		return DateUtil.format(onDuty);
	}
	public void setOnDuty(long onDuty) {
		this.onDuty = onDuty;
	}
	public void setOnDuty(Date onDuty) {
		this.onDuty = onDuty.getTime();
	}
	public long getOffDuty() {
		return offDuty;
	}
	public String getOffDutyToDate() {
		return DateUtil.formatToDate(offDuty);
	}
	public String getOffDutyToSimple() {
		return DateUtil.format(offDuty);
	}
	public void setOffDuty(long offDuty) {
		this.offDuty = offDuty;
	}
	public void setOffDuty(Date offDuty) {
		this.offDuty = offDuty.getTime();
	}
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	public int getOrderAmount() {
		return orderAmount;
	}
	public void setOrderAmount(int orderAmount) {
		this.orderAmount = orderAmount;
	}
	public int getCashAmount() {
		return cashAmount;
	}
	public void setCashAmount(int cashAmount) {
		this.cashAmount = cashAmount;
	}
	public float getCashIncome() {
		return cashIncome;
	}
	public void setCashIncome(float cashIncome) {
		this.cashIncome = cashIncome;
	}
	public float getCashIncome2() {
		return cashIncome2;
	}
	public void setCashIncome2(float cashIncome2) {
		this.cashIncome2 = cashIncome2;
	}
	public int getCreditCardAmount() {
		return creditCardAmount;
	}
	public void setCreditCardAmount(int creditCardAmount) {
		this.creditCardAmount = creditCardAmount;
	}
	public float getCreditCardIncome() {
		return creditCardIncome;
	}
	public void setCreditCardIncome(float creditCardIncome) {
		this.creditCardIncome = creditCardIncome;
	}
	public float getCreditCardIncome2() {
		return creditCardIncome2;
	}
	public void setCreditCardIncome2(float creditCardIncome2) {
		this.creditCardIncome2 = creditCardIncome2;
	}
	public int getMemberCardAmount() {
		return memberCardAmount;
	}
	public void setMemberCardAmount(int memberCardAmount) {
		this.memberCardAmount = memberCardAmount;
	}
	public float getMemberCardIncome() {
		return memberCardIncome;
	}
	public void setMemberCardIncome(float memberCardIncome) {
		this.memberCardIncome = memberCardIncome;
	}
	public float getMemberCardIncome2() {
		return memberCardIncome2;
	}
	public void setMemberCardIncome2(float memberCardIncome2) {
		this.memberCardIncome2 = memberCardIncome2;
	}
	public int getSignAmount() {
		return signAmount;
	}
	public void setSignAmount(int signAmount) {
		this.signAmount = signAmount;
	}
	public float getSignIncome() {
		return signIncome;
	}
	public void setSignIncome(float signIncome) {
		this.signIncome = signIncome;
	}
	public float getSignIncome2() {
		return signIncome2;
	}
	public void setSignIncome2(float signIncome2) {
		this.signIncome2 = signIncome2;
	}
	public int getHangAmount() {
		return hangAmount;
	}
	public void setHangAmount(int hangAmount) {
		this.hangAmount = hangAmount;
	}
	public float getHangIncome() {
		return hangIncome;
	}
	public void setHangIncome(float hangIncome) {
		this.hangIncome = hangIncome;
	}
	public float getHangIncome2() {
		return hangIncome2;
	}
	public void setHangIncome2(float hangIncome2) {
		this.hangIncome2 = hangIncome2;
	}
	public int getDiscountAmount() {
		return discountAmount;
	}
	public void setDiscountAmount(int discountAmount) {
		this.discountAmount = discountAmount;
	}
	public float getDiscountIncome() {
		return discountIncome;
	}
	public void setDiscountIncome(float discountIncome) {
		this.discountIncome = discountIncome;
	}
	public int getGiftAmount() {
		return giftAmount;
	}
	public void setGiftAmount(int giftAmount) {
		this.giftAmount = giftAmount;
	}
	public float getGiftIncome() {
		return giftIncome;
	}
	public void setGiftIncome(float giftIncome) {
		this.giftIncome = giftIncome;
	}
	public int getCancelAmount() {
		return cancelAmount;
	}
	public void setCancelAmount(int cancelAmount) {
		this.cancelAmount = cancelAmount;
	}
	public float getCancelIncome() {
		return cancelIncome;
	}
	public void setCancelIncome(float cancelIncome) {
		this.cancelIncome = cancelIncome;
	}
	public int getServiceAmount() {
		return serviceAmount;
	}
	public void setServiceAmount(int serviceAmount) {
		this.serviceAmount = serviceAmount;
	}
	public float getServiceIncome() {
		return serviceIncome;
	}
	public void setServiceIncome(float serviceIncome) {
		this.serviceIncome = serviceIncome;
	}
	public int getPaidAmount() {
		return paidAmount;
	}
	public void setPaidAmount(int paidAmount) {
		this.paidAmount = paidAmount;
	}
	public float getPaidIncome() {
		return paidIncome;
	}
	public void setPaidIncome(float paidIncome) {
		this.paidIncome = paidIncome;
	}
	public int getEraseAmount() {
		return eraseAmount;
	}
	public void setEraseAmount(int eraseAmount) {
		this.eraseAmount = eraseAmount;
	}
	public float getEraseIncome() {
		return eraseIncome;
	}
	public void setEraseIncome(float eraseIncome) {
		this.eraseIncome = eraseIncome;
	}
	public float getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(float totalPrice) {
		this.totalPrice = totalPrice;
	}
	public float getTotalPrice2() {
		return totalPrice2;
	}
	public void setTotalPrice2(float totalPrice2) {
		this.totalPrice2 = totalPrice2;
	}
	public List<BusinessStatisticsByDept> getDeptStat() {
		return deptStat;
	}
	public void setDeptStat(List<BusinessStatisticsByDept> deptStat) {
		this.deptStat = deptStat;
	}
	
}
