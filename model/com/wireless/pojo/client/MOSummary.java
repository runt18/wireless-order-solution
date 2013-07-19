package com.wireless.pojo.client;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("startDateFormat", DateUtil.format(this.startDate));
		jm.put("endDateFormat", DateUtil.format(this.endDate));
		jm.put("chargeMoney", this.chargeMoney);
		jm.put("consumeAmount", this.consumeAmount);
		jm.put("payMoney", this.payMoney);
		jm.put("consumePoint", this.consumePoint);
		jm.put("pointConsume", this.pointConsume);
		jm.put("pointAdjust", this.pointAdjust);
		jm.put("moneyAdjust", this.moneyAdjust);
		if(this.member != null){
			jm.put("member", this.member);			
		}
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
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
