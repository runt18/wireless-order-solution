package com.wireless.pojo.promotion;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class CouponOperation implements Jsonable{

	public static class InsertBuilder{
		private final int couponId;
		private Operate operate;
		private int associateId;
		private String comment;
		
		public InsertBuilder(int couponId){
			this.couponId = couponId;
		}
		
		public InsertBuilder setOperate(Operate operate, int associateId){
			this.operate = operate;
			this.associateId = associateId;
			return this;
		}
		
		public InsertBuilder setComment(String comment){
			this.comment = comment;
			return this;
		}
		
		public CouponOperation build(){
			return new CouponOperation(this);
		}
	}
	
	public static enum Operate{
		FAST_ISSUE(1, "手动发券"),
		ORDER_ISSUE(2, "账单发券"),
		FAST_USE(20, "手动用券"),
		ORDER_USE(21, "账单用券"),
		WX_SUBSCRIBE_ISSUE(22, "微信关注用券")
		;
		Operate(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		private final int val;
		private final String desc;
		
		public int getVal(){
			return this.val;
		}
		
		public static Operate valueOf(int val){
			for(Operate operate : values()){
				if(operate.val == val){
					return operate;
				}
			}
			throw new IllegalArgumentException("The (val = " + val + ") passed is invalid.");
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	private int id;
	private int couponId;
	private String couponName;
	private float couponPrice;
	private Operate operate;
	private int associateId;
	private long operateDate;
	private String operateStaff;
	private String comment;
	
	private CouponOperation(InsertBuilder builder){
		this.couponId = builder.couponId;
		this.operate = builder.operate;
		this.associateId = builder.associateId;
		this.comment = builder.comment;
	}
	
	public CouponOperation(int id){
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getCouponId() {
		return couponId;
	}
	
	public void setCouponId(int couponId) {
		this.couponId = couponId;
	}
	
	public String getCouponName() {
		return couponName;
	}
	
	public void setCouponName(String couponName) {
		this.couponName = couponName;
	}
	
	public float getCouponPrice() {
		return couponPrice;
	}
	
	public void setCouponPrice(float couponPrice) {
		this.couponPrice = couponPrice;
	}
	
	public Operate getOperate() {
		return operate;
	}
	
	public void setOperate(Operate operate) {
		this.operate = operate;
	}
	
	public int getAssociateId() {
		return associateId;
	}
	
	public void setAssociateId(int associateId) {
		this.associateId = associateId;
	}
	
	public long getOperateDate() {
		return operateDate;
	}
	
	public void setOperateDate(long operateDate) {
		this.operateDate = operateDate;
	}
	
	public String getOperateStaff() {
		return operateStaff;
	}
	
	public void setOperateStaff(String operateStaff) {
		this.operateStaff = operateStaff;
	}
	
	public void setComment(String comment){
		this.comment = comment;
	}
	
	public String getComment(){
		if(this.comment == null){
			return "";
		}
		return this.comment;
	}
	
	@Override
	public int hashCode(){
		return this.id * 37 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof CouponOperation)){
			return false;
		}else{
			return this.id == ((CouponOperation)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return this.couponName;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putInt("couponId", this.couponId);
		jm.putString("couponName", this.couponName);
		jm.putFloat("couponPrice", this.couponPrice);
		jm.putInt("operate", this.operate.val);
		jm.putString("operateText", this.operate.toString());
		jm.putString("operateDate", DateUtil.format(this.operateDate, DateUtil.Pattern.DATE_TIME));
		jm.putString("operateStaff", this.operateStaff);
		jm.putString("comment", this.comment);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
	}
}
