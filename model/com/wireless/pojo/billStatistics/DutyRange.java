package com.wireless.pojo.billStatistics;

import com.wireless.pojo.util.DateUtil;

public class DutyRange {
	
	private long onDuty;		// 开始时间
	private long offDuty;		// 结束时间
	
	public DutyRange(){
		
	}
	
	public DutyRange(long onDuty, long offDuty){
		this.onDuty = onDuty;
		this.offDuty = offDuty;
	}
	
	public DutyRange(String onDuty, String offDuty){
		this.onDuty = DateUtil.parseDate(onDuty);
		this.offDuty = DateUtil.parseDate(offDuty);
	}
	
	public String getOnDutyFormat() {
		return DateUtil.format(onDuty, DateUtil.Pattern.DATE_TIME);
	}
	
	public String getOffDutyFormat() {
		return DateUtil.format(offDuty, DateUtil.Pattern.DATE_TIME);
	}
	
	public long getOnDuty() {
		return onDuty;
	}
	public void setOnDuty(long onDuty) {
		this.onDuty = onDuty;
	}
	public long getOffDuty() {
		return offDuty;
	}
	public void setOffDuty(long offDuty) {
		this.offDuty = offDuty;
	}
	
}
