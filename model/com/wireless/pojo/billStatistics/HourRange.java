package com.wireless.pojo.billStatistics;

import java.text.ParseException;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class HourRange implements Jsonable{
	private final long opening;		// 开始时间
	private final long ending;		// 结束时间
	private final DateUtil.Pattern pattern;
	
	public HourRange(long opening, long ending){
		this(opening, ending, DateUtil.Pattern.TIME);
	}
	
	public HourRange(long opening, long ending, DateUtil.Pattern pattern){
		if(opening < ending){
			throw new IllegalArgumentException("开始时间不能小于结束时间");
		}
		this.opening = opening;
		this.ending = ending;
		this.pattern = pattern;
	}
	
	public HourRange(String opening, String ending) throws ParseException{
		this(DateUtil.parseDate(opening, DateUtil.Pattern.TIME), DateUtil.parseDate(ending, DateUtil.Pattern.TIME), DateUtil.Pattern.TIME);
	}
	
	public HourRange(String onDuty, String offDuty, DateUtil.Pattern pattern) throws ParseException{
		this(DateUtil.parseDate(onDuty, pattern), DateUtil.parseDate(offDuty, pattern), pattern);
	}
	
	public String getOpeningFormat() {
		return DateUtil.format(opening, this.pattern);
	}
	
	public String getEndingFormat() {
		return DateUtil.format(ending, this.pattern);
	}
	
	public long getOpeningTime() {
		return opening;
	}
	
	public long getEndingTime() {
		return ending;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
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
