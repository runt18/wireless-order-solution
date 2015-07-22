package com.wireless.pojo.billStatistics.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.pojo.member.Member;

public class StatisticsByEachDay {

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
		return "date = " + this.date +
			   ", create member = " + this.creates.size() +
			   ", consumption amount = " + getConsumption().getTotalAmount() +
			   ", consumption total = " + getConsumption().getTotalConsume() +
			   ", charge amount = " + getCharge().getChargeAmount() +
			   ", charge total = " + getCharge().getTotalActualCharge() +
			   ", refund amount = " + getCharge().getRefundAmount() +
			   ", refund total = " + getCharge().getTotalActualRefund();
	}
}
