package com.wireless.pojo.stockMgr;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.SortedList;

public class MonthlyBalance implements Jsonable{

	private int id;
	private int restaurantId;
	private String StaffName;
	private long month;
	private List<MonthlyBalanceDetail> details = SortedList.newInstance(new Comparator<MonthlyBalanceDetail>(){
		@Override
		public int compare(MonthlyBalanceDetail arg0, MonthlyBalanceDetail arg1) {
			// 按部门id排序
			if(arg0.getDeptId() > arg1.getDeptId()){
				return -1;
			}else if(arg0.getDeptId() < arg1.getDeptId()){
				return 1;
			}else{
				return 0;
			}
		}	
	
	});
	
	
	
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
	public String getStaffName() {
		return StaffName;
	}
	public void setStaffName(String staffName) {
		StaffName = staffName;
	}
	public long getMonth() {
		return month;
	}
	public void setMonth(long month) {
		this.month = month;
	}
	public List<MonthlyBalanceDetail> getDetails() {
		return Collections.unmodifiableList(this.details);
	}
	public void setDetails(List<MonthlyBalanceDetail> details) {
		if(details != null){
			this.details.clear();
			this.details.addAll(details);
		}
	}
	public void addDetails(MonthlyBalanceDetail monthDetail) {
		if(monthDetail != null && !this.details.contains(monthDetail)){
			this.details.add(monthDetail);
		}
		
	}

	public static class InsertBuilder{
		private MonthlyBalance data = new MonthlyBalance();
		public InsertBuilder(int restaurantId, String staffName){
			this.data.setRestaurantId(restaurantId);
			this.data.setStaffName(staffName);
		}
		public List<MonthlyBalanceDetail> getMonthlyBalanceDetail(){
			return Collections.unmodifiableList(this.data.details);
		}
		
		public void addMonthlyBalanceDetail(MonthlyBalanceDetail detail){
			if(!this.data.details.contains(detail)){
				this.data.details.add(detail);
			}
		}
		
		public MonthlyBalance build(){
			return this.data;
		}
		
	}
	
	@Override
	public int hashCode(){
		return 17 * 31 + this.id;
	}
	
	@Override 
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof MonthlyBalance)){
			return false;
		}else{
			return this.id == ((MonthlyBalance)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return "monthlyBalance(id = " + this.id + ", staffName = " + this.StaffName + ", month = " + DateUtil.format(this.month) + ")";
	}
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("id", this.id);
		jm.put("restaurantId", this.restaurantId);
		jm.put("staffName", this.StaffName);
		jm.put("month", DateUtil.format(this.month));
		jm.put("details", this.details);
		return Collections.unmodifiableMap(jm);
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	
	
}
