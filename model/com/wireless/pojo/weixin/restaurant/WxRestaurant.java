package com.wireless.pojo.weixin.restaurant;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.oss.OssImage;


public class WxRestaurant implements Jsonable{

	public static class UpdateBuilder{
		private OssImage weixinLogo;
		private String weixinInfo;
		private String weixinAppId;
		private String weixinAppSecret;
		private String qrCodeUrl;
		private String nickName;
		private String headImgUrl;
		private String refreshToken;
		private String qrCode;
		private QrCodeStatus qrCodeStatus;
		private String wxSerial;
		private String paymentTemplate;
		private String couponDrawTemplate;
		private String couponTimeoutTemplate;
		private String chargeTemplate;
		
		public UpdateBuilder setChargeTemplate(String template){
			this.chargeTemplate = template;
			return this;
		}
		
		public boolean isChargeTemplateChanged(){
			return this.chargeTemplate != null;
		}
		
		public UpdateBuilder setPaymentTemplate(String template){
			this.paymentTemplate = template;
			return this;
		}
		
		public boolean isPaymentTemplateChanged(){
			return this.paymentTemplate != null;
		}
		
		public UpdateBuilder setCouponDrawTemplate(String template){
			this.couponDrawTemplate = template;
			return this;
		}
		
		public boolean isCouponDrawTemplateChanged(){
			return this.couponDrawTemplate != null;
		}
		
		public UpdateBuilder setCouponTimeoutTemplate(String template){
			this.couponTimeoutTemplate = template;
			return this;
		}
		
		public boolean isCouponTimeoutTemplateChanged(){
			return this.couponTimeoutTemplate != null;
		}
		
		public boolean isWxSerialChanged(){
			return this.wxSerial != null;
		}
		
		public UpdateBuilder setWxSerial(String wxSerial){
			this.wxSerial = wxSerial;
			return this;
		}
		
		public boolean isQrCodeChanged(){
			return this.qrCode != null;
		}
		
		public UpdateBuilder setQrCode(String qrCode){
			this.qrCode = qrCode;
			return this;
		}
		
		public UpdateBuilder setQrCodeStatus(QrCodeStatus status){
			this.qrCodeStatus = status;
			return this;
		}
		
		public boolean isQrCodeStatusChanged(){
			return this.qrCodeStatus != null;
		}
		
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

		public UpdateBuilder setQrCodeUrl(String qrCodeUrl){
			this.qrCodeUrl = qrCodeUrl;
			return this;
		}
		
		public boolean isQrCodeUrlChanged(){
			return this.qrCodeUrl != null;
		}
		
		public UpdateBuilder setNickName(String nickName){
			this.nickName = nickName;
			return this;
		}
		
		public boolean isNickNameChanged(){
			return this.nickName != null;
		}
		
		public UpdateBuilder setHeadImgUrl(String headImgUrl){
			this.headImgUrl = headImgUrl;
			return this;
		}
		
		public boolean isHeadImgUrlChanged(){
			return this.headImgUrl != null;
		}
		
		public UpdateBuilder setRefreshToken(String refreshToken){
			this.refreshToken = refreshToken;
			return this;
		}
		
		public boolean isRefreshTokenChanged(){
			return this.refreshToken != null;
		}
		
		public WxRestaurant build(){
			return new WxRestaurant(this);
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
	
	public static enum QrCodeStatus{
		NORMAL(1, "正常"),
		HIDDEN(2, "隐藏");
		
		private final int val;
		private final String desc;
		
		QrCodeStatus(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		public boolean isNormal(){
			return this == NORMAL;
		}
		
		public boolean isHidden(){
			return this == HIDDEN;
		}
		
		public static QrCodeStatus valueOf(int val){
			for(QrCodeStatus status : values()){
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
	private String qrCodeUrl;
	private String qrCode;
	private QrCodeStatus qrCodeStatus;
	private String nickName;
	private String headImgUrl;
	private String refreshToken;
	private String paymentTemplate;
	private String couponDrawTemplate;
	private String couponTimeoutTemplate;
	private String chargeTemplate;
	
	private WxRestaurant(UpdateBuilder builder){
		this.weixinLogo = builder.weixinLogo;
		this.weixinInfo = builder.weixinInfo;
		this.weixinAppId = builder.weixinAppId;
		this.weixinAppSecret = builder.weixinAppSecret;
		this.qrCodeUrl = builder.qrCodeUrl;
		this.nickName = builder.nickName;
		this.headImgUrl = builder.headImgUrl;
		this.refreshToken = builder.refreshToken;
		this.qrCode = builder.qrCode;
		this.qrCodeStatus = builder.qrCodeStatus;
		this.weixinSerial = builder.wxSerial;
		this.paymentTemplate = builder.paymentTemplate;
		this.couponDrawTemplate = builder.couponDrawTemplate;
		this.couponTimeoutTemplate = builder.couponTimeoutTemplate;
		this.chargeTemplate = builder.chargeTemplate;
	}
	
	public WxRestaurant(int restaurantId){
		this.restaurantId = restaurantId;
	}
	
	public String getWeixinSerial() {
		if(weixinSerial == null){
			return "";
		}
		return weixinSerial;
	}

	public void setQrCodeStatus(QrCodeStatus status){
		this.qrCodeStatus = status;
	}
	
	public QrCodeStatus getQrCodeStatus(){
		return this.qrCodeStatus;
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

	public String getQrCodeUrl() {
		if(qrCodeUrl == null){
			return "";
		}
		return qrCodeUrl;
	}

	public void setQrCodeUrl(String qrCodeUrl) {
		this.qrCodeUrl = qrCodeUrl;
	}

	public void setQrCode(String qrCode){
		this.qrCode = qrCode;
	}
	
	public boolean hasQrCode(){
		return getQrCode().length() > 0;
	}
	
	public String getQrCode(){
		if(this.qrCode == null){
			return "";
		}
		return this.qrCode;
	}
	
	public String getNickName() {
		if(nickName == null){
			return "";
		}
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getHeadImgUrl() {
		if(headImgUrl == null){
			return "";
		}
		return headImgUrl;
	}

	public void setHeadImgUrl(String headImgUrl) {
		this.headImgUrl = headImgUrl;
	}

	public String getRefreshToken() {
		if(refreshToken == null){
			return "";
		}
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public void setPaymentTemplate(String paymentTemplate){
		this.paymentTemplate = paymentTemplate;
	}

	public String getPaymentTemplate(){
		if(this.paymentTemplate == null){
			return "";
		}
		return this.paymentTemplate;
	}
	
	public boolean hasPaymentTemplate(){
		return this.getPaymentTemplate().length() != 0;
	}

	public void setCouponDrawTemplate(String couponDrawTemplate){
		this.couponDrawTemplate = couponDrawTemplate;
	}
	
	public String getCouponDrawTemplate(){
		if(this.couponDrawTemplate == null){
			return "";
		}
		return this.couponDrawTemplate;
	}
	
	public boolean hasCouponDrawTemplate(){
		return this.getCouponDrawTemplate().length() != 0;
	}
	
	public void setCouponTimeoutTemplate(String couponTimeoutTemplate){
		this.couponTimeoutTemplate = couponTimeoutTemplate;
	}
	
	public String getCouponTimeoutTemplate(){
		if(this.couponTimeoutTemplate == null){
			return "";
		}
		return this.couponTimeoutTemplate;
	}
	
	public boolean hasCouponTimeoutTemplate(){
		return this.getCouponTimeoutTemplate().length() != 0;
	}
	
	public void setChargeTemplate(String template){
		this.chargeTemplate = template;
	}
	
	public String getChargeTemplate(){
		if(this.chargeTemplate == null){
			return "";
		}
		return this.chargeTemplate;
	}
	
	public boolean hasChargeTemplate(){
		return getChargeTemplate().length() > 0;
	}
	
	@Override
	public int hashCode(){
		return getWeixinSerial().hashCode();
	}
	
	@Override 
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof WxRestaurant)){
			return false;
		}else{
			return getWeixinSerial().equals(((WxRestaurant)obj).getWeixinSerial());
		}
	}
	
	@Override
	public String toString(){
		return "weixin_restaurant(" + getWeixinSerial() + "," + restaurantId + ")";
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("weixinAppId", getWeixinAppId());
		jm.putString("weixinAppSecret", getWeixinAppSecret());
		jm.putString("weixinInfo", getWeixinInfo());
		jm.putString("weixinSerial", getWeixinSerial());
		jm.putString("headImgUrl", getHeadImgUrl());
		jm.putString("nickName", getNickName());
		jm.putString("refreshToken", getRefreshToken());
		jm.putString("qrCodeUrl", getQrCodeUrl());
		jm.putString("qrCode", getQrCode());
		jm.putInt("qrCodeStatus", getQrCodeStatus().getVal());
		jm.putBoolean("isAuth", hasQrCode());
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}
