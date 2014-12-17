package com.wireless.pojo.member;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class MOSummary implements Jsonable {
	
	private long startDate;		// 统计开始时间
	private long endDate;		// 统计结束时间
	private Member member;		// 会员信息
	private float chargeMoney;	// 充值金额
	private int consumeAmount;	// 消费次数
	private float payMoney;		// 消费金额
	private float consumePoint;	// 消费赠送积分
	private float pointConsume;	// 积分消费
	private float pointAdjust;	// 积分调整
	private float moneyAdjust;	// 金额调整
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("startDateFormat", DateUtil.format(this.startDate));
		jm.putString("endDateFormat", DateUtil.format(this.endDate));
		jm.putFloat("chargeMoney", this.chargeMoney);
		jm.putInt("consumeAmount", this.consumeAmount);
		jm.putFloat("payMoney", this.payMoney);
		jm.putFloat("consumePoint", this.consumePoint);
		jm.putFloat("pointConsume", this.pointConsume);
		jm.putFloat("pointAdjust", this.pointAdjust);
		jm.putFloat("moneyAdjust", this.moneyAdjust);
		jm.putJsonable("member", this.member, 0);			
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

	public long getStartDate() {
		return startDate;
	}
	public void setStartDate(long startDate) {
		this.startDate = startDate;
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
	public float getChargeMoney() {
		return chargeMoney;
	}
	public void setChargeMoney(float chargeMoney) {
		this.chargeMoney = chargeMoney;
	}
	public int getConsumeAmount() {
		return consumeAmount;
	}
	public void setConsumeAmount(int consumeAmount) {
		this.consumeAmount = consumeAmount;
	}
	public float getPayMoney() {
		return payMoney;
	}
	public void setPayMoney(float payMoney) {
		this.payMoney = payMoney;
	}
	public float getConsumePoint() {
		return consumePoint;
	}
	public void setConsumePoint(float consumePoint) {
		this.consumePoint = consumePoint;
	}
	public float getPointConsume() {
		return pointConsume;
	}
	public void setPointConsume(float pointConsume) {
		this.pointConsume = pointConsume;
	}
	public float getPointAdjust() {
		return pointAdjust;
	}
	public void setPointAdjust(float pointAdjust) {
		this.pointAdjust = pointAdjust;
	}
	public float getMoneyAdjust() {
		return moneyAdjust;
	}
	public void setMoneyAdjust(float moneyAdjust) {
		this.moneyAdjust = moneyAdjust;
	}
	

}
