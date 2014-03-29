package com.wireless.pojo.weixin.weixinInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;


public class WeixinInfo implements Jsonable{

	private int restaurantId;
	private int boundCouponType;
	private String weixinLogo;
	private String weixinInfo;
	private String weixinPromote;
	
	public int getRestaurantId() {
		return restaurantId;
	}
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	public int getBoundCouponType() {
		return boundCouponType;
	}
	public void setBoundCouponType(int boundCouponType) {
		this.boundCouponType = boundCouponType;
	}
	public String getWeixinLogo() {
		return weixinLogo;
	}
	public void setWeixinLogo(String weixinLogo) {
		this.weixinLogo = weixinLogo;
	}
	public String getWeixinInfo() {
		return weixinInfo;
	}
	public void setWeixinInfo(String weixinInfo) {
		this.weixinInfo = weixinInfo;
	}
	public String getWeixinPromote() {
		return weixinPromote;
	}
	public void setWeixinPromote(String weixinPromote) {
		this.weixinPromote = weixinPromote;
	}
	
	
	public static class Builder{
		protected WeixinInfo data;
		public Builder(){
			data = new WeixinInfo();
		}
		
		public WeixinInfo build(){
			return data;
		}
	}
	
	public static class InsertBuilder extends Builder{
		public InsertBuilder(int restaurantId){
			data.setRestaurantId(restaurantId);
		}
		public InsertBuilder setBoundCouponType(int couponType){
			data.setBoundCouponType(couponType);
			return this;
		}
		public InsertBuilder setWeixinLogo(String weixinLogo){
			data.setWeixinLogo(weixinLogo);
			return this;
		}
		public InsertBuilder setWeixinInfo(String weixinInfo) {
			data.setWeixinInfo(weixinInfo);
			return this;
		}
		public InsertBuilder setWeixinPromote(String weixinPromote){
			data.setWeixinPromote(weixinPromote);
			return this;
		}
	}
	
	public static class UpdateBuilder extends Builder{
		public UpdateBuilder(int restaurantId){
			data.setRestaurantId(restaurantId);
		}
		public UpdateBuilder setBoundCouponType(int couponType){
			data.setBoundCouponType(couponType);
			return this;
		}
		public UpdateBuilder setWeixinLogo(String weixinLogo){
			data.setWeixinLogo(weixinLogo);
			return this;
		}
		public UpdateBuilder setWeixinInfo(String weixinInfo) {
			data.setWeixinInfo(weixinInfo);
			return this;
		}
		public UpdateBuilder setWeixinPromote(String weixinPromote){
			data.setWeixinPromote(weixinPromote);
			return this;
		}
	}
	
	@Override
	public int hashCode(){
		return restaurantId * 31 + 17;
	}
	
	@Override 
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof WeixinInfo)){
			return false;
		}else{
			return getRestaurantId() == (((WeixinInfo)obj).getRestaurantId());
		}
	}
	
	@Override
	public String toString(){
		return "weixin_restaurant(" + getRestaurantId() + ")";
	}
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("id", this.restaurantId);
		jm.put("couponType", this.boundCouponType);
		jm.put("weixinLogo", this.weixinLogo);
		jm.put("weixinInfo", this.weixinInfo);
		jm.put("weixinPromote", this.weixinPromote);
		return Collections.unmodifiableMap(jm);
	}
	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
	

}
