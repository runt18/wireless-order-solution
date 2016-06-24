package com.wireless.pojo.promotion;

import java.text.ParseException;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class PromotionUseTime implements Jsonable, Comparable<PromotionUseTime>{
	
	public static enum Week{
		Monday(2, "星期一"),
		Tuesday(3, "星期二"),
		Wednesday(4, "星期三"),
		Thursday(5, "星期四"),
		Friday(6, "星期五"),
		Saturday(7, "星期六"),
		Sunday(1, "星期日");
		 
		private final int val;
		private final String desc;
		
		Week(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public static Week valueOf(int val){
			for(Week trigger : values()){
				if(trigger.val == val){
					return trigger;
				}
			}
			throw new IllegalArgumentException("The promotionUseDay (val = " + val + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}

	
	public static class InsertBuilder{
		private final Week week;
		private final long start;
		private final long end;
		private int promotionId;
		
		private InsertBuilder(Week week, long start, long end){
			this.week = week;
			this.start = start;
			this.end = end;
		}
		
		public static InsertBuilder newInstance(Week week, long start, long end){
			if(start > end){
				throw new IllegalArgumentException("时段开始时间不能比结束时间少");
			}
			InsertBuilder instance = new InsertBuilder(week, start, end);
			return instance;
		}
		
		public static InsertBuilder newInstance(Week week, String start, String end) throws ParseException{
			return newInstance(week, DateUtil.parseDate(start, DateUtil.Pattern.HOUR), DateUtil.parseDate(end, DateUtil.Pattern.HOUR));
		}
		
		public InsertBuilder setPromotionId(int promotionId){
			this.promotionId = promotionId;
			return this;
		}
		
		public InsertBuilder setPromotionId(Promotion promotion){
			this.promotionId = promotion.getId();
			return this;
		}
		
		public PromotionUseTime build(){
			return new PromotionUseTime(this);
		}

		@Override
		public boolean equals(Object obj){
			if(obj == null || !(obj instanceof InsertBuilder)){
				return false;
			}else{
				return this.week == ((InsertBuilder)obj).week;
			}
		}
	}
	
	
	private int promotionId;
	private Week week;
	private long start;
	private long end;
	
	private PromotionUseTime(InsertBuilder builder){
		this.promotionId = builder.promotionId;
		this.week = builder.week;
		this.start = builder.start;
		this.end = builder.end;
	}
	
	public PromotionUseTime(){
	}
	
	
	public int getPromotionId() {
		return promotionId;
	}

	public void setPromotionId(int promotionId) {
		this.promotionId = promotionId;
	}

	public Week getWeek() {
		return week;
	}

	public void setWeek(Week week) {
		this.week = week;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	@Override	
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("promotionId", this.promotionId);
		jm.putInt("week", this.week.val);
		jm.putString("weekName", this.week.desc);
		jm.putString("start",DateUtil.format(this.start, DateUtil.Pattern.HOUR));
		jm.putString("end", DateUtil.format(this.end, DateUtil.Pattern.HOUR));
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int compareTo(PromotionUseTime o) {
		if(this.week.val < o.week.val){
			return -1;
		}else if(this.week.val > o.week.val){
			return 1;
		}else{
			return 0;
		}
	}
	
}
