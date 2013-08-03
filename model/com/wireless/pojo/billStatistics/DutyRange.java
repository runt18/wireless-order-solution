package com.wireless.pojo.billStatistics;

import com.wireless.pojo.system.SystemStaff;
import com.wireless.pojo.util.DateUtil;

public class DutyRange {
	
	private long onDuty;		// 开始时间
	private long offDuty;		// 结束时间
	private SystemStaff staff;		// 操作人
	
	public DutyRange(){
		
	}
	
	public DutyRange(long onDuty, long offDuty, SystemStaff staff){
		this.onDuty = onDuty;
		this.offDuty = offDuty;
		this.staff = staff;
	}
	
	public DutyRange(long onDuty, long offDuty){
		this.onDuty = onDuty;
		this.offDuty = offDuty;
		this.staff = null;
	}
	
	public DutyRange(String onDuty, String offDuty, SystemStaff staff){
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
		return DateUtil.format(onDuty);
	}
	
	public String getOffDutyFormat() {
		return DateUtil.format(offDuty);
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
	public SystemStaff getStaff() {
		return staff;
	}
	public void setStaff(SystemStaff staff) {
		this.staff = staff;
	}
	
}
