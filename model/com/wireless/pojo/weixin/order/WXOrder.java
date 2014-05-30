package com.wireless.pojo.weixin.order;

import java.util.ArrayList;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.util.DateUtil;

public class WXOrder implements Jsonable{
	public enum Status{
		NO_USED(1, "未使用"),
		USED(2, "已使用");
		
		private int value;
		private String text;
		Status(int value, String text){
			this.value = value;
			this.text = text;
		}
		public int getValue(){
			return this.value;
		}
		public String getText(){
			return this.text;
		}
		public static Status valueOf(int value){
			for(Status temp : values()){
				if(temp.value == value){
					return temp;
				}
			}
			throw new IllegalArgumentException("The val(" + value + ") is invalid.");
		}
	}
	
	public static class Builder{
		public Builder(){
			this.data = new WXOrder();
		}
		protected WXOrder data;
		public Builder setMemberSerial(String serial){
			data.setMemberSerial(serial);
			return this;
		}
		public Builder setFoods(List<WXOrderFood> foods){
			data.setFoods(foods);
			return this;
		}
//		public Builder setStatus(Status status){
//			data.setStatus(status);
//			return this;
//		}
//		public Builder setStatus(int status){
//			data.setStatus(status);
//			return this;
//		}
		public WXOrder build(){
			return data.clone();
		}
	}
	public static class InsertBuilder extends Builder{
		
	}
	
	private int rid;
	private int id;
	private String memberSerial;
	private long birthDate;
	private int code;
	private Status status;
	private Restaurant restaurant;
	private List<WXOrderFood> foods;
	
	public int getRid() {
		return rid;
	}
	public void setRid(int rid) {
		this.rid = rid;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMemberSerial() {
		return memberSerial;
	}
	public void setMemberSerial(String memberSerial) {
		this.memberSerial = memberSerial;
	}
	public long getBirthDate() {
		return birthDate;
	}
	public String getBirthDateFormat() {
		return DateUtil.format(birthDate);
	}
	public void setBirthDate(long birthDate) {
		this.birthDate = birthDate;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public void setStatus(int status) {
		this.status = Status.valueOf(status);
	}
	public Restaurant getRestaurant() {
		return restaurant;
	}
	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
	}
	public List<WXOrderFood> getFoods() {
		return foods;
	}
	public void setFoods(List<WXOrderFood> foods) {
		this.foods = foods;
	}
	
	@Override
	protected WXOrder clone() {
		WXOrder clone = new WXOrder();
		clone.setRid(this.rid);
		clone.setId(this.id);
		clone.setMemberSerial(this.memberSerial);
		clone.setBirthDate(this.birthDate);
		clone.setCode(this.code);
		clone.setStatus(this.status);
		clone.setRestaurant(this.restaurant);
		clone.setFoods(this.getFoods());
		
		return clone;
	}
	
	@Override
	public String toString(){
		return "id=" + id + ", restaurantId=" + rid + ", memberSerial=" + memberSerial
				+ ", code=" + code;
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putInt("rid", this.rid);
		jm.putString("memberSerial", this.memberSerial);
		jm.putLong("birthDate", this.birthDate);
		jm.putString("birthDateFormat", this.getBirthDateFormat());
		jm.putInt("statusValue", this.status.getValue());
		jm.putString("statusText", this.status.getText());
		jm.putInt("code", this.code);
		jm.putJsonable("restaurant", restaurant, 0);
		
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	/**
	 * 解析前台回传数据(新插入菜品格式)
	 * @param data
	 * @return
	 */
	public static List<WXOrderFood> unserializeByInsert(String data){
		List<WXOrderFood> list = new ArrayList<WXOrderFood>();
		if(data != null && data.trim().length() > 0){
			WXOrderFood tfood = null;
			String[] tstr = null;
			String[] items = data.split("<<si>>");
			for(int i = 0; i < items.length; i++){
				tstr = items[i] == null ? null : items[i].split("<<sa>>");
				if(tstr != null && tstr.length == 2){
					tfood = new WXOrderFood(Integer.valueOf(tstr[0]), Integer.valueOf(tstr[1]));
					list.add(tfood);
				}
			}
		}
		return list;
	}
	
}
