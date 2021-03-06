package com.wireless.pojo.system;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.DateUtil.Pattern;

public class BusinessHour implements Jsonable{

	private int id;
	private int restaurantId;
	private String name;
	private long opening;
	private long ending;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public String getName() {
		return this.name != null ? name : "";
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public long getOpening() {
		return opening;
	}
	
	public String getOpeningFormat() {
		return DateUtil.format(opening, Pattern.HOUR);
	}
	
	public void setOpening(long opening) {
		this.opening = opening;
	}
	
	public long getEnding() {
		return ending;
	}
	
	public String getEndingFormat() {
		return DateUtil.format(ending, Pattern.HOUR);
	}
	
	public void setEnding(long ending) {
		this.ending = ending;
	}
	
	public BusinessHour(int id){
		this.id = id;
	}
	
	private BusinessHour(UpdateBuilder builder){
		setId(builder.id);
		setRestaurantId(builder.restaurantId);
		setName(builder.name);
		setOpening(builder.opening);
		setEnding(builder.ending);
	}
	
	public static class InsertBuilder{
		BusinessHour data;
		public InsertBuilder(String name, HourRange hourRange){
			data = new BusinessHour(0);
			data.setName(name);
			data.setOpening(hourRange.getOpeningTime());
			data.setEnding(hourRange.getEndingTime());
		}
		
		public BusinessHour build(){
			return this.data;
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		private int restaurantId;
		private String name;
		private long opening;
		private long ending;
		
		public UpdateBuilder(int id){
			this.id = id;
		}

		public UpdateBuilder setName(String name) {
			this.name = name;
			return this;
		}

		public boolean isNameChanged(){
			return this.name != null;
		}
		
		public boolean isRangeChanged(){
			return opening != 0 || ending != 0;
		}
		
		public UpdateBuilder setHourRange(HourRange range){
			opening = range.getOpeningTime();
			ending = range.getEndingTime();
			return this;
		}
		
		public BusinessHour build(){
			return new BusinessHour(this);
		}
	}
	
	@Override
	public String toString(){
		return "businessHour(id=" + getId() + ", opening=" + getOpeningFormat() + ", ending=" + getEndingFormat(); 
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null && !(obj instanceof BusinessHour)){
			return false;
		}else{
			return this.id == ((BusinessHour)obj).id;
		}
	}
	
	@Override
	public int hashCode(){
		return 17 * 31 + this.id;
	}
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.getId());
		jm.putInt("restauranId", this.getRestaurantId());
		jm.putString("name", this.getName());
		jm.putLong("openingValue", this.getOpening());
		jm.putString("opening", this.getOpeningFormat());
		jm.putLong("endingValue", this.getEnding());
		jm.putString("ending", this.getEndingFormat());
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	
	

}
