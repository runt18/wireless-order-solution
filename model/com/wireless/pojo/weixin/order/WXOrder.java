package com.wireless.pojo.weixin.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.exception.BusinessException;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.dishesOrder.OrderFood;

public class WXOrder implements Jsonable{
	
	public static enum Status{
		INVALID(1, "已失效"),
		COMMITTED(2, "已下单");
		
		private final int val;
		private final String desc;
		Status(int value, String text){
			this.val = value;
			this.desc = text;
		}
		public int getVal(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		public static Status valueOf(int value){
			for(Status status : values()){
				if(status.val == value){
					return status;
				}
			}
			throw new IllegalArgumentException("The val(" + value + ") is invalid.");
		}
	}
	
	public static enum Type{
		INSIDE(1, "店内"),
		BOOK(2, "预订"),
		TAKE_OUT(3, "外卖");
		
		private final int val;
		private final String desc;
		
		Type(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static Type valueOf(int val){
			for(Type type : values()){
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The type(val = " + val + ") pass id invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		private Status status;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public UpdateBuilder setStatus(Status status){
			this.status = status;
			return this;
		}
		
		public boolean isStatusChanged(){
			return this.status != null;
		}
		
		public WXOrder build(){
			return new WXOrder(this);
		}
	}
	
	public static abstract class InsertBuilder{
		private final String weixinSerial;
		private final Type type;
		private final Status status;
		private final WXOrder order = new WXOrder(0);
		
		InsertBuilder(String weixinSerial, Type type, Status status){
			this.weixinSerial = weixinSerial;
			this.type = type;
			this.status = status;
		}
		
		public InsertBuilder add(OrderFood foodToAdd) throws BusinessException{
			this.order.addFood(foodToAdd);
			return this;
		}
		
		public InsertBuilder addAll(List<OrderFood> foodsToAdd) throws BusinessException{
			for(OrderFood of : foodsToAdd){
				add(of);
			}
			return this;
		}
		
		public WXOrder build(){
			return new WXOrder(this);
		}
	} 
	
	public static class InsertBuilder4Inside extends InsertBuilder{
		public InsertBuilder4Inside(String weixinSerial){
			super(weixinSerial, Type.INSIDE, Status.COMMITTED);
		}
	}
	
	private int id;
	private String weixinSerial;
	private long birthDate;
	private int code;
	private Type type;
	private Status status;
	private int restaurantId;
	private final List<OrderFood> foods = new ArrayList<OrderFood>();
	
	private WXOrder(UpdateBuilder builder){
		setId(builder.id);
		setStatus(builder.status);
	}
	
	private WXOrder(InsertBuilder builder){
		setWeixinSerial(builder.weixinSerial);
		setType(builder.type);
		setStatus(builder.status);
	}
	
	public WXOrder(int id){
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getWeixinSerial() {
		return weixinSerial;
	}
	
	public void setWeixinSerial(String weixinSerial) {
		this.weixinSerial = weixinSerial;
	}
	
	public long getBirthDate() {
		return birthDate;
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
	
	public void setType(Type type){
		this.type = type;
	}
	
	public Type getType(){
		return this.type;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurant(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public List<OrderFood> getFoods() {
		return Collections.unmodifiableList(foods);
	}

	public void addFood(OrderFood foodToAdd) throws BusinessException{
		//Check to see whether the food to add is already contained. 
		int index = foods.indexOf(foodToAdd);
		
		//如果新添加的菜品在原来菜品List中已经存在相同的菜品，则累加数量
		//否则添加到菜品列表
		if(index >= 0){
			foods.get(index).addCount(foodToAdd.getCount());
		}else{
			foods.add(foodToAdd);
		}
	}
	
	public void addFoods(List<OrderFood> foodsToAdd) throws BusinessException{
		for(OrderFood of : foodsToAdd){
			addFood(of);
		}
	}
	
	public void setFoods(List<OrderFood> foods) throws BusinessException {
		this.foods.clear();
		addFoods(foods);
	}
	
	@Override
	public String toString(){
		return "id=" + id +
			   ", memberSerial=" + weixinSerial	+
			   ", code=" + code;
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
}
