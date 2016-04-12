package com.wireless.pojo.member.represent;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class RepresentChain implements Jsonable{
	
	private int id;
	private int restaurantId;
	private long subscribeDate;
	private int recommendMemberId;
	private String recommendMember;
	private int recommendPoint;
	private float recommendMoney;
	private int subscribeMemberId;
	private String subscribeMember;
	private int subscribePoint;
	private float subscribeMoney;
	
	public RepresentChain(int id){
		this.id = id;
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
	
	public long getSubscribeDate() {
		return subscribeDate;
	}
	
	public void setSubscribeDate(long subscribeDate) {
		this.subscribeDate = subscribeDate;
	}
	
	public int getRecommendMemberId() {
		return recommendMemberId;
	}
	
	public void setRecommendMemberId(int recommendMemberId) {
		this.recommendMemberId = recommendMemberId;
	}
	
	public String getRecommendMember() {
		if(this.recommendMember == null){
			return "";
		}
		return recommendMember;
	}
	
	public void setRecommendMember(String recommendMember) {
		this.recommendMember = recommendMember;
	}
	
	public int getRecommendPoint() {
		return recommendPoint;
	}
	
	public void setRecommendPoint(int recommendPoint) {
		this.recommendPoint = recommendPoint;
	}
	
	public float getRecommendMoney() {
		return recommendMoney;
	}
	
	public void setRecommendMoney(float recommendMoney) {
		this.recommendMoney = recommendMoney;
	}
	
	public int getSubscribeMemberId() {
		return subscribeMemberId;
	}
	
	public void setSubscribeMemberId(int subscribeMemberId) {
		this.subscribeMemberId = subscribeMemberId;
	}
	
	public String getSubscribeMember() {
		if(this.subscribeMember == null){
			return "";
		}
		return subscribeMember;
	}
	
	public void setSubscribeMember(String subscribeMember) {
		this.subscribeMember = subscribeMember;
	}
	
	public int getSubscribePoint() {
		return subscribePoint;
	}
	
	public void setSubscribePoint(int subscribePoint) {
		this.subscribePoint = subscribePoint;
	}
	
	public float getSubscribeMoney() {
		return subscribeMoney;
	}
	
	public void setSubscribeMoney(float subscribeMoney) {
		this.subscribeMoney = subscribeMoney;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putInt("restaurantId", this.restaurantId);
		jm.putString("subscribeDate", DateUtil.format(this.subscribeDate, DateUtil.Pattern.DATE_TIME));
		jm.putInt("recommendMemberId", this.recommendMemberId);
		jm.putString("recommendMember", this.recommendMember);
		jm.putInt("recommendPoint", this.recommendPoint);
		jm.putFloat("recommendMoney", this.recommendMoney);
		jm.putInt("subscribeMemberId", this.subscribeMemberId);
		jm.putString("subscribeMember",  this.subscribeMember);
		jm.putInt("subscribePoint", this.subscribePoint);
		jm.putFloat("subscribeMoney", this.subscribeMoney);
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		// TODO Auto-generated method stub
		
	}
}
