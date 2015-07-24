package com.wireless.pojo.billStatistics.member;

import java.util.ArrayList;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class MemberStatistics implements Jsonable{

	public final static MemberStatistics DUMMY = new MemberStatistics();
	
	private final List<StatisticsByEachDay> statistics = new ArrayList<StatisticsByEachDay>();
	
	public MemberStatistics add(StatisticsByEachDay eachDay){
		statistics.add(eachDay);
		return this;
	}
	
	//消费金额
	public float getTotalConsume(){
		float total = 0;
		for(StatisticsByEachDay each : statistics){
			total += each.getConsumption().getTotalConsume();
		}
		return total;
	}
	
	//日均消费金额
	public float getAverageConsume(){
		if(statistics.isEmpty()){
			return 0;
		}else{
			return getTotalConsume() / statistics.size();
		}
	}
	
	//消费次数
	public int getTotalConsumeAmount(){
		int total = 0;
		for(StatisticsByEachDay each : statistics){
			total += each.getConsumption().getTotalAmount();
		}
		return total;
	}
	
	//日均消费次数
	public int getAverageConsumeAmount(){
		if(statistics.isEmpty()){
			return 0;
		}else{
			return getTotalChargeAmount() / statistics.size();
		}
	}
	
	//充值总额
	public float getTotalCharge(){
		float total = 0;
		for(StatisticsByEachDay each : statistics){
			total += each.getCharge().getTotalAccountCharge();
		}
		return total;
	}
	
	//日均充值额
	public float getAverageCharge(){
		if(statistics.isEmpty()){
			return 0;
		}else{
			return getTotalCharge() / statistics.size();
		}
	}
	
	//充值总次数
	public int getTotalChargeAmount(){
		int total = 0;
		for(StatisticsByEachDay each : statistics){
			total += each.getCharge().getChargeAmount();
		}
		return total;
	}
	
	//日均充值次数
	public int getAverageChargeAmount(){
		if(statistics.isEmpty()){
			return 0;
		}else{
			return getTotalChargeAmount() / statistics.size();
		}
	}
	
	//退款总数
	public float getTotalRefund(){
		float total = 0;
		for(StatisticsByEachDay each : statistics){
			total += each.getCharge().getTotalAccountRefund();
		}
		return total;
	}
	
	//日均退款
	public float getAverageRefund(){
		if(statistics.isEmpty()){
			return 0;
		}else{
			return getTotalRefund() / statistics.size();
		}
	}
	
	//退款总次数
	public int getTotalRefundAmount(){
		int total = 0;
		for(StatisticsByEachDay each : statistics){
			total += each.getCharge().getRefundAmount();
		}
		return total;
	}
	
	//日均退款次数
	public int getAverageRefundAmount(){
		if(statistics.isEmpty()){
			return 0;
		}else{
			return getTotalRefundAmount() / statistics.size();
		}
	}
	
	//总开卡数
	public int getTotalCreated(){
		int total = 0;
		for(StatisticsByEachDay each : statistics){
			total += each.getCreateMembers().size();
		}
		return total;
	}
	
	//日均开卡数
	public int getAverageCreated(){
		if(statistics.isEmpty()){
			return 0;
		}else{
			return getTotalCreated() / statistics.size();
		}
	}
	
	@Override
	public String toString(){
		final String sep = System.getProperty("line.separator");
		return "total_consume = " + getTotalConsume() +
			   ", total_charge = " + getTotalCharge() + 
			   ", total_refund = " + getTotalRefund() +
			   ", total_created = " + getTotalCreated() + sep +
			   this.statistics;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putFloat("totalConsume", this.getTotalConsume());
		jm.putFloat("avgConsume", this.getAverageConsume());
		jm.putFloat("totalCharge", this.getTotalCharge());
		jm.putFloat("avgCharge", this.getAverageCharge());
		jm.putFloat("totalRefund", this.getTotalRefund());
		jm.putFloat("avgRefund", this.getAverageRefund());
		jm.putFloat("totalCreated", this.getTotalCreated());
		jm.putFloat("avgCreated", this.getAverageCreated());
		jm.putJsonableList("memberEachDays", statistics, flag);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		
	}
}
