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
		if(weixinLogo == null){
			return "";
		}
		return weixinLogo;
	}
	
	public void setWeixinLogo(String weixinLogo) {
		this.weixinLogo = weixinLogo;
	}
	
	public String getWeixinInfo() {
		if(weixinInfo == null){
			return "";
		}
		return weixinInfo;
	}
	
	public void setWeixinInfo(String weixinInfo) {
		this.weixinInfo = weixinInfo;
	}
	
	public String getWeixinPromote() {
		if(weixinPromote == null){
			return "";
		}
		return weixinPromote;
	}
	
	public void setWeixinPromote(String weixinPromote) {
		this.weixinPromote = weixinPromote;
	}
	
	
	public static class InsertBuilder{
		
		private final int restaurantId;
		
		public InsertBuilder(int restaurantId){
			this.restaurantId = restaurantId;
		}
		
		public WeixinInfo build(){
			return new WeixinInfo(this);
		}
	}
	
	public static class UpdateBuilder {
		
		private final int restaurantId;
		private int boundCouponType;
		private String weixinLogo;
		private String weixinInfo;
		private String weixinPromote;
		
		public UpdateBuilder(int restaurantId){
			this.restaurantId = restaurantId;
		}
		
		public UpdateBuilder setBoundCouponType(int couponType){
			this.boundCouponType = couponType;
			return this;
		}
		
		public boolean isBoundCouponTypeChanged(){
			return this.boundCouponType != 0;
		}
		
		public UpdateBuilder setWeixinLogo(String weixinLogo){
			this.weixinLogo = weixinLogo;
			return this;
		}
		
		public boolean isWeixinLogoChanged(){
			return this.weixinLogo != null;
		}
		
		public UpdateBuilder setWeixinInfo(String weixinInfo) {
			this.weixinInfo = weixinInfo;
			return this;
		}
		
		public boolean isWeixinInfoChanged(){
			return this.weixinInfo != null;
		}
		
		public UpdateBuilder setWeixinPromote(String weixinPromote){
			this.weixinPromote = weixinPromote;
			return this;
		}
		
		public boolean isWeixinPromotChanged(){
			return weixinPromote != null;
		}
		
		public WeixinInfo build(){
			return new WeixinInfo(this);
		}
		
	}
	
	public WeixinInfo(int restaurantId){
		setRestaurantId(restaurantId);
	}
	
	private WeixinInfo(InsertBuilder builder){
		setRestaurantId(builder.restaurantId);
	}
	
	private WeixinInfo(UpdateBuilder builder){
		setRestaurantId(builder.restaurantId);
		setBoundCouponType(builder.boundCouponType);
		setWeixinLogo(builder.weixinLogo);
		setWeixinInfo(builder.weixinInfo);
		setWeixinPromote(builder.weixinPromote);
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
