package com.wireless.pojo.billStatistics;

import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class DutyRange {
	
	private long onDuty;		// 开始时间
	private long offDuty;		// 结束时间
	private Staff staff;		// 操作人
	
	public DutyRange(){
		
	}
	
	public DutyRange(long onDuty, long offDuty, Staff staff){
		this.onDuty = onDuty;
		this.offDuty = offDuty;
		this.staff = staff;
	}
	
	public DutyRange(long onDuty, long offDuty){
		this.onDuty = onDuty;
		this.offDuty = offDuty;
		this.staff = null;
	}
	
	public DutyRange(String onDuty, String offDuty, Staff staff){
		this.onDuty = DateUtil.parseDate(onDuty);
		this.offDuty = DateUtil.parseDate(offDuty);
		this.staff = staff;
	}
	
	public DutyRange(String onDuty, String offDuty){
		this.onDuty = DateUtil.parseDate(onDuty);
		this.offDuty = DateUtil.parseDate(offDuty);
		this.staff = null;
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
	public Staff getStaff() {
		return staff;
	}
	public void setStaff(Staff staff) {
		this.staff = staff;
	}
	
}
