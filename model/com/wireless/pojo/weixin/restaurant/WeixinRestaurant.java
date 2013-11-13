package com.wireless.pojo.weixin.restaurant;

import com.wireless.pojo.restaurantMgr.Restaurant;

public class WeixinRestaurant {

	public static enum Status{
		VERIFIED(1, "已验证"),
		BOUND(2, "已绑定");
		
		private final int val;
		private final String desc;
		Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.val == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The status(val = " + val + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return "status(val=" + val + ", desc=" + desc + ")";
		}
	}
	
	private String weixinSerial;
	private Restaurant restaurant;
	private long bindDate;
	private Status status = Status.VERIFIED;
	
	public String getWeixinSerial() {
		return weixinSerial;
	}

	public void setWeixinSerial(String weixinSerial) {
		this.weixinSerial = weixinSerial;
	}

	public Restaurant getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
	}

	public long getBindDate() {
		return bindDate;
	}

	public void setBindDate(long bindDate) {
		this.bindDate = bindDate;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public int hashCode(){
		return getWeixinSerial().hashCode();
	}
	
	@Override 
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof WeixinRestaurant)){
			return false;
		}else{
			return getWeixinSerial().equals(((WeixinRestaurant)obj).getWeixinSerial());
		}
	}
	
	@Override
	public String toString(){
		return "weixin_restaurant(" + getWeixinSerial() + "," + getRestaurant().getId() + ")";
	}
}
