package com.wireless.pojo.billStatistics;


public class DutyRange {
	
	private String onDuty;		// 开始时间
	private String offDuty;		// 结束时间
	private String name;		// 操作人
	
	public DutyRange(){
		
	}
	
	public DutyRange(String onDuty, String offDuty){
		this.onDuty = onDuty;
		this.offDuty = offDuty;
	}
	
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
