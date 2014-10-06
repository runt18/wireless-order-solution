package com.wireless.pojo.weixin.restaurant;

import com.wireless.pojo.oss.OssImage;


public class WeixinRestaurant {

	public static class UpdateBuilder{
		private OssImage weixinLogo;
		private String weixinInfo;
		private String weixinAppId;
		private String weixinAppSecret;
		
		public UpdateBuilder setWeixinLogo(int ossImageId){
			this.weixinLogo = new OssImage(ossImageId);
			return this;
		}
		
		public UpdateBuilder setWeixinLogo(OssImage logo){
			this.weixinLogo = logo;
			return this;
		}
		
		public boolean isWeixinLogoChanged(){
			return this.weixinLogo != null;
		}
		
		public UpdateBuilder setWeixinInfo(String info){
			this.weixinInfo = info;
			return this;
		}
		
		public boolean isWeixinInfoChanged(){
			return this.weixinInfo != null;
		}
		
		public UpdateBuilder setWeixinAppId(String appId){
			this.weixinAppId = appId;
			return this;
		}
		
		public boolean isWeixinAppIdChanged(){
			return this.weixinAppId != null;
		}
		
		public UpdateBuilder setWeixinAppSecret(String secret){
			this.weixinAppSecret = secret;
			return this;
		}
		
		public boolean isWeixinSecretChanged(){
			return this.weixinAppSecret != null;
		}
		
		public WeixinRestaurant build(){
			return new WeixinRestaurant(this);
		}
	}
	
	public static enum Status{
		CREATED(1, "已创建"),
		VERIFIED(2, "已验证"),
		BOUND(3, "已绑定");
		
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
	private int restaurantId;
	private long bindDate;
	private Status status = Status.CREATED;
	private OssImage weixinLogo;
	private String weixinInfo;
	private String weixinAppId;
	private String weixinAppSecret;
	
	private WeixinRestaurant(UpdateBuilder builder){
		this.weixinLogo = builder.weixinLogo;
		this.weixinInfo = builder.weixinInfo;
		this.weixinAppId = builder.weixinAppId;
		this.weixinAppSecret = builder.weixinAppSecret;
	}
	
	public WeixinRestaurant(int restaurantId){
		this.restaurantId = restaurantId;
	}
	
	public String getWeixinSerial() {
		return weixinSerial;
	}

	public void setWeixinSerial(String weixinSerial) {
		this.weixinSerial = weixinSerial;
	}

	public int getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
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
	
	public boolean hasWeixinLogo(){
		return this.weixinLogo != null;
	}
	
	public OssImage getWeixinLogo() {
		return weixinLogo;
	}

	public void setWeixinLogo(OssImage weixinLogo) {
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

	public String getWeixinAppId() {
		if(weixinAppId == null){
			return "";
		}
		return weixinAppId;
	}

	public void setWeixinAppId(String weixinAppId) {
		this.weixinAppId = weixinAppId;
	}

	public String getWeixinAppSecret() {
		if(weixinAppSecret == null){
			return "";
		}
		return weixinAppSecret;
	}

	public void setWeixinAppSecret(String weixinAppSecret) {
		this.weixinAppSecret = weixinAppSecret;
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
		return "weixin_restaurant(" + getWeixinSerial() + "," + restaurantId + ")";
	}
}
