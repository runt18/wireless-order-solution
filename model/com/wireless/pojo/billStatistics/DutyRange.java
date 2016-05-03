package com.wireless.pojo.billStatistics;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class DutyRange implements Jsonable{
	
	private final long onDuty;		// 开始时间
	private final long offDuty;		// 结束时间
	
	public DutyRange(long onDuty, long offDuty){
		this.onDuty = onDuty;
		this.offDuty = offDuty;
	}
	
	public DutyRange(String onDuty, String offDuty){
		this.onDuty = DateUtil.parseDate(onDuty);
		this.offDuty = DateUtil.parseDate(offDuty);
	}

	public String getOnDutyFormat(DateUtil.Pattern pattern) {
		return DateUtil.format(onDuty, pattern);
	}
	
	public String getOffDutyFormat(DateUtil.Pattern pattern) {
		return DateUtil.format(offDuty, pattern);
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
	
	public long getOffDuty() {
		return offDuty;
	}

	@Override
	public int hashCode(){
		return getOnDutyFormat().hashCode() + getOffDutyFormat().hashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof DutyRange)){
			return false;
		}else{
			return this.onDuty / 1000 == ((DutyRange)obj).onDuty / 1000 && this.offDuty / 1000 == ((DutyRange)obj).offDuty / 1000;
		}
	}
	
	@Override
	public String toString(){
		return "on_duty = " + getOnDutyFormat() + 
				", off_duty = " + getOffDutyFormat(); 
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putLong("onDuty", getOnDuty());
		jm.putLong("offDuty", getOffDuty());
		jm.putString("offDutyFormat", getOffDutyFormat());
		jm.putString("onDutyFormat", getOnDutyFormat());
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
}
