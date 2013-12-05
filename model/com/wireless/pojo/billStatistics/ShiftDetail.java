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
	
	private int orderAmount;			//总账单数
	
	private int cashAmount;				//现金账单数
	private float cashTotalIncome;		//现金金额
	private float cashActualIncome;		//现金实收
	
	private int creditCardAmount;		//刷卡账单数
	private float creditTotalIncome;	//刷卡金额
	private float creditActualIncome;	//刷卡实收
	
	private int memberCardAmount;		//会员卡账单数
	private float memberTotalIncome;	//会员卡金额
	private float memberActualIncome;	//会员卡实收
	
	private int signAmount;				//签单账单数
	private float signTotalIncome;		//签单金额
	private float signActualIncome;		//签单实收
	
	private int hangAmount;				//挂账账单数
	private float hangTotalIncome;		//挂账金额
	private float hangActualIncome;		//挂账实收
	
	private float totalActual;			//合计实收金额
	private float totalIncome;			//合计实收金额
	
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
	
	public float getCashTotalIncome() {
		return cashTotalIncome;
	}
	
	public void setCashTotalIncome(float cashTotalIncome) {
		this.cashTotalIncome = cashTotalIncome;
	}
	
	public float getCashActualIncome() {
		return cashActualIncome;
	}
	
	public void setCashActualIncome(float cashActualIncome) {
		this.cashActualIncome = cashActualIncome;
	}
	
	public int getCreditCardAmount() {
		return creditCardAmount;
	}
	
	public void setCreditCardAmount(int creditCardAmount) {
		this.creditCardAmount = creditCardAmount;
	}
	
	public float getCreditTotalIncome() {
		return creditTotalIncome;
	}
	
	public void setCreditTotalIncome(float creditTotalIncome) {
		this.creditTotalIncome = creditTotalIncome;
	}
	
	public float getCreditActualIncome() {
		return creditActualIncome;
	}
	
	public void setCreditActualIncome(float creditActualIncome) {
		this.creditActualIncome = creditActualIncome;
	}
	
	public int getMemberCardAmount() {
		return memberCardAmount;
	}
	
	public void setMemberCardAmount(int memberCardAmount) {
		this.memberCardAmount = memberCardAmount;
	}
	
	public float getMemberTotalIncome() {
		return memberTotalIncome;
	}
	
	public void setMemberTotalIncome(float memberTotalIncome) {
		this.memberTotalIncome = memberTotalIncome;
	}
	
	public float getMemberActualIncome() {
		return memberActualIncome;
	}
	
	public void setMemberActualIncome(float memberActualIncome) {
		this.memberActualIncome = memberActualIncome;
	}
	
	public int getSignAmount() {
		return signAmount;
	}
	
	public void setSignAmount(int signAmount) {
		this.signAmount = signAmount;
	}
	
	public float getSignTotalIncome() {
		return signTotalIncome;
	}
	
	public void setSignTotalIncome(float signTotalIncome) {
		this.signTotalIncome = signTotalIncome;
	}
	
	public float getSignActualIncome() {
		return signActualIncome;
	}
	
	public void setSignActualIncome(float signActualIncome) {
		this.signActualIncome = signActualIncome;
	}
	
	public int getHangAmount() {
		return hangAmount;
	}
	
	public void setHangAmount(int hangAmount) {
		this.hangAmount = hangAmount;
	}
	
	public float getHangTotalIncome() {
		return hangTotalIncome;
	}
	
	public void setHangTotalIncome(float hangTotalIncome) {
		this.hangTotalIncome = hangTotalIncome;
	}
	
	public float getHangActualIncome() {
		return hangActualIncome;
	}
	
	public void setHangActualIncome(float hangActualIncome) {
		this.hangActualIncome = hangActualIncome;
	}
	
	public float getTotalActual() {
		return totalActual;
	}
	
	public void setTotalActual(float totalActual) {
		this.totalActual = totalActual;
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
		return totalIncome;
	}

	public void setTotalIncome(float totalIncome) {
		this.totalIncome = totalIncome;
	}

	@Override 
	public String toString(){
		return "ShiftDetail : " +
				"orderAmount " + this.orderAmount +
				"onDuty" + this.onDuty +
				"offDuty" + this.offDuty;
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
		jm.put("orderAmount", this.orderAmount);
		
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