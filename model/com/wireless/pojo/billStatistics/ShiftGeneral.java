package com.wireless.pojo.billStatistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.SortedList;

public class ShiftGeneral implements Jsonable{

	public static class StaffPayment implements Jsonable, Comparable<StaffPayment>{
		private String staffName;
		private int staffId;
		private float totalPrice;			//应交款项
		private float actualPrice;			//实交款项
		private List<PaymentGeneral> paymentGenerals = new ArrayList<PaymentGeneral>();
		
		public String getStaffName(){
			return this.staffName;
		}
		
		public void setStaffName(String name){
			this.staffName = name;
		}
		
		public int getStaffId(){
			return this.staffId;
		}
		
		public void setStaffId(int staffId){
			this.staffId = staffId;
		}
		
		public float getTotalPrice(){
			return this.totalPrice;
		}
		
		public void setTotalPrice(float totalPrice){
			this.totalPrice = totalPrice;
		}
		
		public float getActualPrice(){
			return this.actualPrice;
		}
		
		public void setActualPrice(float actualPrice){
			this.actualPrice = actualPrice;
		}

		public void addPaymentGeneral(PaymentGeneral paymentGeneral){
			this.paymentGenerals.add(paymentGeneral);
		}
		
		public List<PaymentGeneral> getPayments(){
			return Collections.unmodifiableList(paymentGenerals);
		}
		
		@Override
		public JsonMap toJsonMap(int flag) {
			JsonMap jm = new JsonMap();
			jm.putInt("staffId", staffId);
			jm.putString("staffName", staffName);
			jm.putFloat("totalPayment", totalPrice);
			jm.putFloat("actualPayment", actualPrice);
			jm.putJsonableList("paymentGenerals", paymentGenerals, 0);
			return jm;
		}

		@Override
		public void fromJsonMap(JsonMap jsonMap, int flag) {
			
		}

		@Override
		public int compareTo(StaffPayment o) {
			if(this.staffId > o.staffId){
				return 1;
			}else if(this.staffId < o.staffId){
				return -1;
			}else{
				return 0;
			}
		}
	}
	
	private final int id;
	private String staffName;
	private long onDuty;
	private long offDuty;
	private int restaurantId;
	private List<StaffPayment> payments = SortedList.newInstance();
	
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
	
	public void addPayment(StaffPayment payment){
		if(!payments.contains(payment)){
			payments.add(payment);
		}
	}
	
	public List<StaffPayment> getPayments(){
		return Collections.unmodifiableList(payments);
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
		jm.putJsonableList("payments", payments, 0);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}
