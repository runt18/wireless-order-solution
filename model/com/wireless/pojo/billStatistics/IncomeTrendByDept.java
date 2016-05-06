package com.wireless.pojo.billStatistics;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class IncomeTrendByDept implements Jsonable, Comparable<IncomeTrendByDept>{

	private final DutyRange range;
	private final IncomeByDept deptIncome;
	
	public IncomeTrendByDept(DutyRange range, IncomeByDept deptIncome){
		this.range = range;
		this.deptIncome = deptIncome;
	}
	
	public DutyRange getRange(){
		return this.range;
	}
	
	public IncomeByDept getDeptIncome(){
		return this.deptIncome;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable(range, 0);
		jm.putJsonable(deptIncome, 0);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

	@Override
	public int compareTo(IncomeTrendByDept o) {
		return this.range.compareTo(o.range);
	}
	
}
