package com.wireless.pojo.stockMgr;

import java.util.ArrayList;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class MonthlyBalance implements Jsonable{

	private int id;
	private int restaurantId;
	private String StaffName;
	private long month;
	private List<MonthlyCost> monthlyCost = new ArrayList<MonthlyCost>();
//	private List<MonthlyBalanceDetail> details = SortedList.newInstance(new Comparator<MonthlyBalanceDetail>(){
//		@Override
//		public int compare(MonthlyBalanceDetail arg0, MonthlyBalanceDetail arg1) {
//			// 按部门id排序
//			if(arg0.getDeptId() > arg1.getDeptId()){
//				return -1;
//			}else if(arg0.getDeptId() < arg1.getDeptId()){
//				return 1;
//			}else{
//				return 0;
//			}
//		}	
//	
//	});
	
	public MonthlyBalance(){}
	
	public MonthlyBalance(InsertBuilder builder){
		this.restaurantId = builder.restaurantId;
		this.StaffName = builder.StaffName;
		this.month = builder.month;
		this.setMonthlyCost(builder.monthlyCost);
	}
	
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
//	public List<MonthlyBalanceDetail> getDetails() {
//		return Collections.unmodifiableList(this.details);
//	}
//	public void setDetails(List<MonthlyBalanceDetail> details) {
//		if(details != null){
//			this.details.clear();
//			this.details.addAll(details);
//		}
//	}
	public void setMonthlyCost(List<MonthlyCost> costs){
		if(this.monthlyCost != null){
			this.monthlyCost.clear();
		}
		
		this.monthlyCost.addAll(costs);
	}
	
	public MonthlyBalance addMonthlyCost(MonthlyCost cost){
		this.monthlyCost.add(cost);
		return this;
	}
	
	public List<MonthlyCost> getMonthlyCost(){
		return this.monthlyCost;
	}
	
//	public void addDetails(MonthlyBalanceDetail monthDetail) {
//		if(monthDetail != null && !this.details.contains(monthDetail)){
//			this.details.add(monthDetail);
//		}
//		
//	}

	public static class InsertBuilder{
		private int id;
		private int restaurantId;
		private String StaffName;
		private long month;
		private List<MonthlyCost> monthlyCost = new ArrayList<MonthlyCost>();
		
		public int getId() {
			return id;
		}
		public InsertBuilder setId(int id) {
			this.id = id;
			return this;
		}
		public int getRestaurantId() {
			return restaurantId;
		}
		public InsertBuilder setRestaurantId(int restaurantId) {
			this.restaurantId = restaurantId;
			return this;
		}
		public String getStaffName() {
			return StaffName;
		}
		public InsertBuilder setStaffName(String staffName) {
			StaffName = staffName;
			return this;
		}
		public long getMonth() {
			return month;
		}
		public InsertBuilder setMonth(long month) {
			this.month = month;
			return this;
		}
		
		public InsertBuilder setMonthlyCost(List<MonthlyCost> costs){
			if(this.monthlyCost != null){
				this.monthlyCost.clear();
			}
			
			this.monthlyCost.addAll(costs);
			return this;
		}
		
		public InsertBuilder addMonthlyCost(MonthlyCost cost){
			this.monthlyCost.add(cost);
			return this;
		}
//
//		private MonthlyBalance data = new MonthlyBalance();
//		public InsertBuilder(int restaurantId, String staffName){
//			this.data.setRestaurantId(restaurantId);
//			this.data.setStaffName(staffName);
//		}
//		public List<MonthlyBalanceDetail> getMonthlyBalanceDetail(){
//			return Collections.unmodifiableList(this.data.details);
//		}
//		
//		public void addMonthlyBalanceDetail(MonthlyBalanceDetail detail){
//			if(!this.data.details.contains(detail)){
//				this.data.details.add(detail);
//			}
//		}
		
		public MonthlyBalance build(){
			return new MonthlyBalance(this);
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
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putInt("restaurantId", this.restaurantId);
		jm.putString("staffName", this.StaffName);
		jm.putString("month", DateUtil.format(this.month));
//		jm.putJsonableList("details", this.details, 0);
		jm.putJsonableList("monthlyCost", this.monthlyCost, 0);
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	
	
}
