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
		private int wxCardImgId;
		private String refreshToken;
		private String qrCode;
		private String wxSerial;
		private String paymentTemplate;
		private String couponDrawTemplate;
		private String couponTimeoutTemplate;
		private String chargeTemplate;
		private String orderNotifyTemplate;
		private PayType defaultOrderType;
		private PrefectMember prefectMemberStatus;
		private String refundTemplate;
		
		public boolean isRefundTemplate(){
			return this.refundTemplate != null;
		}
		
		public UpdateBuilder setTakeMoneyTemplate(String refundTemplate){
			this.refundTemplate = refundTemplate;
			return this;
		}
		
		public boolean isWxCardImgIdChange(){
			return this.wxCardImgId != 0;
		}
		
		public UpdateBuilder setWxCardImgId(int wxCardImgUrl){
			this.wxCardImgId = wxCardImgUrl;
			return this;
		}
		
		public int getWxCardImgId(){
			return this.wxCardImgId;
		}
		
		public UpdateBuilder setChargeTemplate(String template){
			this.chargeTemplate = template;
			return this;
		}
		
		public boolean isDefaultOrderTypeChanged(){
			return this.defaultOrderType != null;
		}
		
		public UpdateBuilder setDefaultOrderType(PayType type){
			this.defaultOrderType = type;
			return this;
		}
		
		public boolean isPrefectMemberStatusChanged(){
			return this.prefectMemberStatus != null;
		}
		
		public UpdateBuilder setPrefectMemberStatus(PrefectMember type){
			this.prefectMemberStatus = type;
			return this;
		}
		
		public boolean isChargeTemplateChanged(){
			return this.chargeTemplate != null;
		}
		
		public boolean isOrderNotifyTemplateChanged(){
			return this.orderNotifyTemplate != null;
		}
		
		public UpdateBuilder setOrderNotifyTemplate(String template){
			this.orderNotifyTemplate = template;
			return this;
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
	
	public static enum PayType{
		WX_PAY(1, "微信下单"),
		CONFIRM_BY_STAFF(2,"确认下单"),
		DIRECT_ORDER(3,"直接下单");
		
		private final int val;
		private final String desc;
		PayType(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getValue(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		public static PayType valueOf(int val){
			for(PayType type : values()){
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The PayType(val = " + val + ")is invaild.");
		}
	}
	
	public static enum PrefectMember{
		SHOW_PREFECMEMBER(0, "显示"),
		HIDE_PREFECTMEMBER(1, "不显示");
		
		private final int val;
		private final String desc;
		
		PrefectMember(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getValue(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		public static PrefectMember valueOf(int val){
			for(PrefectMember type : values()){
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The PrefectMember(val = " + val + ")is invaild.");
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
	private String nickName;
	private String headImgUrl;
	private String refreshToken;
	private String paymentTemplate;
	private String couponDrawTemplate;
	private String couponTimeoutTemplate;
	private String orderNotifyTemplate;
	private String refundTemplate;
	private OssImage wxCardImg;
	private String chargeTemplate;
	private PayType defaultOrderType = PayType.CONFIRM_BY_STAFF;
	private PrefectMember prefectMemberStatus = PrefectMember.SHOW_PREFECMEMBER;
	
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
		this.weixinSerial = builder.wxSerial;
		this.paymentTemplate = builder.paymentTemplate;
		this.couponDrawTemplate = builder.couponDrawTemplate;
		this.couponTimeoutTemplate = builder.couponTimeoutTemplate;
		this.chargeTemplate = builder.chargeTemplate;
		this.orderNotifyTemplate = builder.orderNotifyTemplate;
		this.defaultOrderType = builder.defaultOrderType;
		this.prefectMemberStatus = builder.prefectMemberStatus;
		if(builder.wxCardImgId != 0){
			this.wxCardImg = new OssImage(builder.wxCardImgId);
		}
		this.refundTemplate = builder.refundTemplate;
	}
	
	public WxRestaurant(int restaurantId){
		this.restaurantId = restaurantId;
	}
	
	public boolean hasRefundTemplate(){
		return refundTemplate != null;
	}
	
	public String getRefundTemplate() {
		return refundTemplate;
	}

	public void setRefundTemplate(String takeMoneyTemplate) {
		this.refundTemplate = takeMoneyTemplate;
	}

	public OssImage getWxCardImg() {
		return wxCardImg;
	}

	public void setWxCardImg(OssImage wxCardImg) {
		this.wxCardImg = wxCardImg;
	}

	public String getWeixinSerial() {
		if(weixinSerial == null){
			return "";
		}
		return weixinSerial;
	}
	
	public void setDefaultOrderType(PayType type){
		this.defaultOrderType = type;
	}

	public PayType getDefaultOrderType(){
		return this.defaultOrderType;
	}
	
	public void setPrefectMemberStatus(PrefectMember type){
		this.prefectMemberStatus = type;
	}

	public PrefectMember getPrefectMemberStatus(){
		return this.prefectMemberStatus;
	}
	
	public void setOrderNotifyTemplate(String template){
		this.orderNotifyTemplate = template;
	}
	
	public String getOrderNotifyTemplate(){
		return this.orderNotifyTemplate;
	}
	
	public boolean hasOrderNotifyTemplate(){
		return this.orderNotifyTemplate != null;
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
		jm.putBoolean("isAuth", hasQrCode());
		jm.putInt("defaultOrderType", getDefaultOrderType().val);
		jm.putString("defaultOrderTypeText", getDefaultOrderType().desc);
		jm.putInt("prefectMemberStatus", getPrefectMemberStatus().val);
		jm.putJsonable("wxCardImg", this.wxCardImg, 0);
		jm.putString("prefectMemberStatusText", getPrefectMemberStatus().desc);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}
