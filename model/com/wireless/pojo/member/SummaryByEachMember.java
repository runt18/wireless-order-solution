package com.wireless.pojo.member;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class SummaryByEachMember implements Jsonable {
	
	private long startDate;			// 统计开始时间
	private long endDate;			// 统计结束时间
	private Member member;			// 会员信息
	private float chargeActual;		// 充值实收
	private float chargeMoney;		// 充值金充
	private float refundActual;		// 取款实退
	private float refundMoney;		// 取款实扣
	private float consumeBase;		// 基础消费扣额
	private float consumeExtra;		// 赠送消费扣额
	private float consumeTotal;		// 消费金额
	private int consumeAmount;		// 消费次数
	private float remainingBalance;	// 剩余金额
	private int changedPoint;		// 变动积分
	private int remainingPoint;		// 剩余积分
	private float deltaBase;        //基础账户余额
	private float deltaExtra;       //赠送账户余额
	
	public long getStartDate() {
		return startDate;
	}

	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}
	
	public float getDeltaBase() {
		return deltaBase;
	}

	public void setDeltaBase(float deltaBase) {
		this.deltaBase = deltaBase;
	}

	public float getDeltaExtra() {
		return deltaExtra;
	}

	public void setDeltaExtra(float deltaExtra) {
		this.deltaExtra = deltaExtra;
	}

	public long getEndDate() {
		return endDate;
	}

	public void setEndDate(long endDate) {
		this.endDate = endDate;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public float getChargeActual() {
		return chargeActual;
	}

	public void setChargeActual(float chargeActual) {
		this.chargeActual = chargeActual;
	}

	public float getChargeMoney() {
		return chargeMoney;
	}

	public void setChargeMoney(float chargeMoney) {
		this.chargeMoney = chargeMoney;
	}

	public float getRefundActual() {
		return refundActual;
	}

	public void setRefundActual(float refundActual) {
		this.refundActual = refundActual;
	}

	public float getRefundMoney() {
		return refundMoney;
	}

	public void setRefundMoney(float refundMoney) {
		this.refundMoney = refundMoney;
	}

	public float getConsumeBase() {
		return consumeBase;
	}

	public void setConsumeBase(float consumeBase) {
		this.consumeBase = consumeBase;
	}

	public float getConsumeExtra() {
		return consumeExtra;
	}

	public void setConsumeExtra(float consumeExtra) {
		this.consumeExtra = consumeExtra;
	}

	public float getConsumeTotal() {
		return consumeTotal;
	}

	public void setConsumeTotal(float consumeTotal) {
		this.consumeTotal = consumeTotal;
	}

	public int getConsumeAmount() {
		return consumeAmount;
	}

	public void setConsumeAmount(int consumeAmount) {
		this.consumeAmount = consumeAmount;
	}

	public float getRemainingBalance() {
		return remainingBalance;
	}

	public void setRemainingBalance(float remainingBalance) {
		this.remainingBalance = remainingBalance;
	}

	public int getChangedPoint() {
		return changedPoint;
	}

	public void setChangedPoint(int changedPoint) {
		this.changedPoint = changedPoint;
	}

	public int getRemainingPoint() {
		return remainingPoint;
	}

	public void setRemainingPoint(int remainingPoint) {
		this.remainingPoint = remainingPoint;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("startDateFormat", DateUtil.format(this.startDate));
		jm.putString("endDateFormat", DateUtil.format(this.endDate));
		jm.putFloat("chargeMoney", this.chargeMoney);
		jm.putFloat("chargeActual", this.chargeActual);
		jm.putInt("consumeAmount", this.consumeAmount);
		jm.putFloat("refundActual", this.refundActual);
		jm.putFloat("refundMoney", this.refundMoney);
		jm.putFloat("consumeBase", this.consumeBase);
		jm.putFloat("consumeExtra", this.consumeExtra);
		jm.putFloat("consumeTotal", this.consumeTotal);
		jm.putFloat("remainingBalance", this.remainingBalance);
		jm.putFloat("deltaBase", this.deltaBase);
		jm.putFloat("deltaExtra", this.deltaExtra);
		jm.putInt("changedPoint", this.changedPoint);
		jm.putInt("remainingPoint", this.remainingPoint);
		jm.putJsonable("member", this.member, 0);			
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

	@Override
	public String toString(){
		return this.member.getName();
	}

}
