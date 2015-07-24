package com.wireless.pojo.billStatistics.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.member.Member;

public class StatisticsByEachDay implements Jsonable{

	public final static StatisticsByEachDay DUMMY = new StatisticsByEachDay();
	
	private final String date;
	
	private final List<Member> creates = new ArrayList<Member>();
	
	private final IncomeByCharge charge;
	
	private final IncomeByConsume consumption;
	
	private StatisticsByEachDay(){
		this.date = null;
		this.charge = null;
		this.consumption = null;
	}
	
	public StatisticsByEachDay(String date, List<Member> creates, IncomeByCharge charge, IncomeByConsume consumption){
		this.date = date;
		if(creates != null){
			this.creates.addAll(creates);
		}
		this.charge = charge;
		this.consumption = consumption;
	}
	
	public String getDate(){
		return this.date;
	}
	
	public List<Member> getCreateMembers(){
		return Collections.unmodifiableList(this.creates);
	}
	
	public IncomeByCharge getCharge(){
		if(this.charge == null){
			return IncomeByCharge.DUMMY;
		}
		return this.charge;
	}
	
	public IncomeByConsume getConsumption(){
		if(this.consumption == null){
			return IncomeByConsume.DUMMY;
		}
		return this.consumption;
	}
	
	@Override
	public String toString(){
		return "[date = " + this.date +
			   ", create member = " + this.creates.size() +
			   ", consumption amount = " + getConsumption().getTotalAmount() +
			   ", consumption total = " + getConsumption().getTotalConsume() +
			   ", charge amount = " + getCharge().getChargeAmount() +
			   ", charge total = " + getCharge().getTotalActualCharge() +
			   ", refund amount = " + getCharge().getRefundAmount() +
			   ", refund total = " + getCharge().getTotalActualRefund() +
			   "]";
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("date", this.getDate());
		jm.putFloat("memberConsumption", getConsumption().getTotalConsume());
		jm.putFloat("memberCharge", getCharge().getTotalActualCharge());
		jm.putFloat("memberRefund", getCharge().getTotalActualRefund());
		jm.putInt("memberCreate", this.creates.size());
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		
	}
}
