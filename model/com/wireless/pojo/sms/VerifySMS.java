package com.wireless.pojo.sms;

import java.util.Map;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class VerifySMS implements Jsonable{
	
	public static enum ExpiredPeriod{
		
		SECOND_1(1000, "1秒"),
		MINUTE_1(60 * 1000, "1分钟"),
		MINUTE_5(60 * 5 * 1000, "5分钟"),
		MINUTE_10(60 * 10 * 1000, "10分钟");
		
		private final int expired;
		private final String desc;
		
		ExpiredPeriod(int expired, String desc){
			this.expired = expired;
			this.desc = desc;
		}
		
		public int getTime(){
			return this.expired;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	public static class InsertBuilder{
		
		private final long expired;
		
		public InsertBuilder(ExpiredPeriod period){
			this.expired = period.getTime();
		}
		
		public VerifySMS build(){
			return new VerifySMS(this);
		}
	}
	
	public static class VerifyBuilder{
		private final int id;
		private final int code;
		public VerifyBuilder(int id, int code){
			this.id = id;
			this.code = code;
		}
		
		public VerifySMS build(){
			return new VerifySMS(this);
		}
	}
	
	private int id;
	private int code;
	private long created;
	private long expired;
	
	public VerifySMS(int id){
		this.id = id;
	}
	
	private VerifySMS(VerifyBuilder builder){
		setId(builder.id);
		setCode(builder.code);
		setCreated(System.currentTimeMillis());
	}
	
	private VerifySMS(InsertBuilder builder){
		//Generate a verification code which ranges from 1000 through 9999
		setCode(Double.valueOf(Math.random() * 9000 + 1000).intValue());
		long current = System.currentTimeMillis();
		setCreated(current);
		setExpired(current + builder.expired);
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getCode() {
		return code;
	}
	
	public void setCode(int code) {
		this.code = code;
	}
	
	public long getCreated() {
		return created;
	}
	
	public void setCreated(long created) {
		this.created = created;
	}
	
	public long getExpired() {
		return expired;
	}
	
	public void setExpired(long expired) {
		this.expired = expired;
	}
	
	@Override
	public int hashCode(){
		return getId() * 17 + 31;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof VerifySMS)){
			return false;
		}else{
			return ((VerifySMS)obj).getId() == getId();
		}
	}
	
	@Override
	public String toString(){
		return "verification sms(id=" + getId() +
			   ",code=" + getCode() +
			   ",created=" + DateUtil.format(getCreated()) +
			   ",expired=" + DateUtil.format(getExpired()) + ")";
	}

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", id);
		jm.putInt("code", code);
		jm.putLong("expired", expired);
		jm.putLong("created", created);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	
}
