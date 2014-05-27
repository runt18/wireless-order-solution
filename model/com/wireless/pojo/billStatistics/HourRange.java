package com.wireless.pojo.billStatistics;

import java.text.ParseException;
import java.util.Map;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class HourRange implements Jsonable{
	private final long opening;		// 开始时间
	private final long ending;		// 结束时间
	
	public HourRange(long onDuty, long offDuty){
		this.opening = onDuty;
		this.ending = offDuty;
	}
	
	public HourRange(String onDuty, String offDuty) throws ParseException{
		this.opening = DateUtil.parseDate(onDuty, DateUtil.Pattern.TIME);
		this.ending = DateUtil.parseDate(offDuty, DateUtil.Pattern.TIME);
	}
	
	public String getOpeningFormat() {
		return DateUtil.format(opening, DateUtil.Pattern.TIME);
	}
	
	public String getEndingFormat() {
		return DateUtil.format(ending, DateUtil.Pattern.TIME);
	}
	
	public long getOpeningTime() {
		return opening;
	}
	
	public long getEndingTime() {
		return ending;
	}

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putLong("openingTime", getOpeningTime());
		jm.putLong("endingTime", getEndingTime());
		jm.putString("openingFormat", getEndingFormat());
		jm.putString("endingFormat", getOpeningFormat());
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}
