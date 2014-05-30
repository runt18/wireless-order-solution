package com.wireless.pojo.billStatistics;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class ShiftGeneral implements Jsonable{

	private final int id;
	private String staffName;
	private long onDuty;
	private long offDuty;
	private int restaurantId;
	
	public ShiftGeneral(int id){
		this.id = id;
	}

	public String getStaffName() {
		return staffName;
	}

	public void setStaffName(String staffName) {
		this.staffName = staffName;
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

	public int getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}

	public int getId() {
		return id;
	}
	
	@Override 
	public int hashCode(){
		return id * 17 + 31;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof ShiftGeneral)){
			return false;
		}else{
			return getId() == ((ShiftGeneral)obj).getId();
		}
	}
	
	@Override
	public String toString(){
		return "shift general("	+
			   "on:" + DateUtil.format(getOnDuty(), DateUtil.Pattern.DATE_TIME) +
			   ",off:" + DateUtil.format(getOffDuty(), DateUtil.Pattern.DATE_TIME) + ")";
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("staffName", this.staffName);
		jm.putLong("onDuty", this.onDuty);
		jm.putLong("offDuty", this.offDuty);
		jm.putString("onDutyFormat", DateUtil.format(getOnDuty(), DateUtil.Pattern.DATE_TIME));
		jm.putString("offDutyFormat", DateUtil.format(getOffDuty(), DateUtil.Pattern.DATE_TIME));
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}
