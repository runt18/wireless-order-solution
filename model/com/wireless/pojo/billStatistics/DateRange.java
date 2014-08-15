package com.wireless.pojo.billStatistics;

import java.text.ParseException;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class DateRange implements Jsonable{
	private final long opening;		// 开始时间
	private final long ending;		// 结束时间
	private final DateUtil.Pattern pattern;
	
	public DateRange(long opening, long ending){
		this(opening, ending, DateUtil.Pattern.DATE);
	}
	
	public DateRange(long opening, long ending, DateUtil.Pattern pattern){
		if(opening > ending){
			throw new IllegalArgumentException("开始日期不能大于结束日期");
		}
		this.opening = opening;
		this.ending = ending;
		this.pattern = pattern;
	}
	
	public DateRange(String opening, String ending) throws ParseException{
		this(DateUtil.parseDate(opening, DateUtil.Pattern.DATE), DateUtil.parseDate(ending, DateUtil.Pattern.DATE), DateUtil.Pattern.DATE);
	}
	
	public DateRange(String onDuty, String offDuty, DateUtil.Pattern pattern) throws ParseException{
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
	public int hashCode(){
		int hashCode = (int)opening * 31 + 17;
		hashCode += ending * 31 + 17;
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof DateRange)){
			return false;
		}else{
			return opening == ((DateRange)obj).opening && ending == ((DateRange)obj).ending;
		}
	}
	
	@Override
	public String toString(){
		return "opening[" + getOpeningFormat() + "], ending[" + getEndingFormat() + "]"; 
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
