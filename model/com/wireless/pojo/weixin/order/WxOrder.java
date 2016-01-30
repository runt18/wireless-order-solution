package com.wireless.pojo.weixin.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.exception.BusinessException;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.TakeoutAddress;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.util.DateUtil;

public class WxOrder implements Jsonable, Parcelable{
	
	public final static int WX_ORDER_PARCELABLE_SIMPLE = 0;
	public final static int WX_ORDER_PARCELABLE_COMPLEX = 1;
	
	public static enum Status{
		INVALID(1, "已失效"),
		COMMITTED(2, "待确认"),
		ORDER_ATTACHED(3, "已下单");
		
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
	
	public static class AttachBuilder{
		private final UpdateBuilder builder;
		
		public AttachBuilder(WxOrder wxOrder, Order order){
			builder = new UpdateBuilder(wxOrder).setOrder(order).setTable(order.getDestTbl()).setStatus(Status.ORDER_ATTACHED);
		}
		
		public UpdateBuilder asBuilder(){
			return this.builder;
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		private int orderId;
		private Status status;
		private int tableId;
		
		public UpdateBuilder(WxOrder wxOrder){
			this.id = wxOrder.getId();
		}
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public UpdateBuilder setTable(int tableId){
			this.tableId = tableId;
			return this;
		}
		
		public UpdateBuilder setTable(Table table){
			this.tableId = table.getId();
			return this;
		}
		
		public boolean isTableChanged(){
			return this.tableId > 0;
		}
		
		public UpdateBuilder setStatus(Status status){
			this.status = status;
			return this;
		}
		
		public boolean isStatusChanged(){
			return this.status != null;
		}
		
		public UpdateBuilder setOrder(int orderId){
			this.orderId = orderId;
			return this;
		}
		
		public UpdateBuilder setOrder(Order order){
			this.orderId = order.getId();
			return this;
		}
		
		public boolean isOrderChanged(){
			return this.orderId != 0;
		}
		
		public WxOrder build(){
			return new WxOrder(this);
		}
	}
	
	public static abstract class InsertBuilder{
		private final String weixinSerial;
		private final Type type;
		private final Status status;
		private Table table;
		private final WxOrder order = new WxOrder(0);
		
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
		
		public InsertBuilder setComment(String comment){
			this.order.setComment(comment);
			return this;
		}
		
		public InsertBuilder setTable(Table table){
			this.table = table;
			return this;
		}
		
		public String getWxSerial(){
			return this.weixinSerial;
		}
		
		public WxOrder build(){
			return new WxOrder(this);
		}
	} 
	
	public static class InsertBuilder4Takeout extends InsertBuilder{
		private final TakeoutAddress address;
		
		public InsertBuilder4Takeout(String weixinSerial, int addressId){
			super(weixinSerial, Type.TAKE_OUT, Status.COMMITTED);
			this.address = new TakeoutAddress(addressId);
		}
		
		public InsertBuilder4Takeout(String weixinSerial, TakeoutAddress address){
			super(weixinSerial, Type.TAKE_OUT, Status.COMMITTED);
			this.address = address;
		}
		
		public InsertBuilder4Takeout add(OrderFood foodToAdd) throws BusinessException{
			super.add(foodToAdd);
			return this;
		}
		
		public InsertBuilder4Takeout addAll(List<OrderFood> foodsToAdd) throws BusinessException{
			super.addAll(foodsToAdd);
			return this;
		}
	}
	
	public static class InsertBuilder4Inside extends InsertBuilder{
		public InsertBuilder4Inside(String weixinSerial){
			super(weixinSerial, Type.INSIDE, Status.COMMITTED);
		}
		
		public InsertBuilder4Inside add(OrderFood foodToAdd) throws BusinessException{
			super.add(foodToAdd);
			return this;
		}
		
		public InsertBuilder4Inside addAll(List<OrderFood> foodsToAdd) throws BusinessException{
			super.addAll(foodsToAdd);
			return this;
		}
	}
	
	private int id;
	private Member member;
	private Table table;
	private long birthDate;
	private int code;
	private int orderId;
	private Type type;
	private Status status;
	private int restaurantId;
	private String comment;
	private final List<OrderFood> foods = new ArrayList<OrderFood>();
	private TakeoutAddress address;
	
	private WxOrder(UpdateBuilder builder){
		setId(builder.id);
		setStatus(builder.status);
		setOrderId(builder.orderId);
		setTable(new Table(builder.tableId));
	}
	
	private WxOrder(InsertBuilder builder){
		setType(builder.type);
		setStatus(builder.status);
		setComment(builder.order.comment);
		setFoods(builder.order.getFoods());
		setTable(builder.table);
		if(builder.type == Type.TAKE_OUT){
			setTakoutAddress(((InsertBuilder4Takeout)builder).address);
		}
	}
	
	public WxOrder(int id){
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	
	public Member getMember(){
		return member;
	}
	
	public void setMember(Member member){
		this.member = member;
	}
	
	public TakeoutAddress getTakeoutAddress(){
		return this.address;
	}
	
	public void setTakoutAddress(TakeoutAddress address){
		this.address = address;
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
	
	public void setOrderId(int orderId){
		this.orderId = orderId;
	}
	
	public int getOrderId(){
		return this.orderId;
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
	
	public String getComment(){
		if(this.comment == null){
			return "";
		}
		return this.comment;
	}
	
	public void setComment(String comment){
		this.comment = comment;
	}
	
	public boolean hasComment(){
		return getComment().length() > 0;
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

	public void addFood(OrderFood foodToAdd){
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
	
	public void addFoods(List<OrderFood> foodsToAdd) {
		for(OrderFood of : foodsToAdd){
			addFood(of);
		}
	}
	
	public void setFoods(List<OrderFood> foods)  {
		this.foods.clear();
		addFoods(foods);
	}
	
	public float calcPrice(){
		Order order = new Order();
		order.setOrderFoods(this.foods);
		return order.calcTotalPrice();
	}
	
	public void setTable(Table table){
		this.table = table;
	}

	public Table getTable(){
		return this.table;
	}
	
	public boolean hasTable(){
		return this.table != null;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof WxOrder)){
			return false;
		}else{
			return this.id == ((WxOrder)obj).id;
		}
	}
	
	@Override
	public int hashCode(){
		return id * 31 + 17;
	}
	
	@Override
	public String toString(){
		return "id=" + id +
			   ", memberId=" + this.member.getId()	+
			   ", code=" + code;
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putInt("restaurantId", this.restaurantId);
		jm.putInt("code", this.code);
		jm.putString("date", DateUtil.format(this.birthDate));
		jm.putInt("type", this.type.getVal());
		jm.putInt("statusVal", this.status.getVal());
		jm.putString("statusDesc", this.status.getDesc());
		jm.putString("comment", this.comment);
		jm.putJsonableList("foods", this.foods, 0);
		jm.putJsonable("member", this.member, 0);
		jm.putJsonable("table", this.table, 0);
		jm.putInt("orderId", this.orderId);
		jm.putFloat("price", this.calcPrice());
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == WX_ORDER_PARCELABLE_SIMPLE){
			dest.writeInt(getId());
			dest.writeInt(getCode());
			
		}else if(flag == WX_ORDER_PARCELABLE_COMPLEX){
			dest.writeInt(getId());
			dest.writeInt(getCode());
			//FIXME dest.writeString(getWeixinSerial());
			dest.writeString(null);
			dest.writeLong(getBirthDate());
			dest.writeByte(getType().getVal());
			dest.writeByte(getStatus().getVal());
			dest.writeParcelList(getFoods(), OrderFood.OF_PARCELABLE_4_QUERY);
		}
	}

	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == WX_ORDER_PARCELABLE_SIMPLE){
			setId(source.readInt());
			setCode(source.readInt());
			
		}else if(flag == WX_ORDER_PARCELABLE_COMPLEX){
			setId(source.readInt());
			setCode(source.readInt());
			//FIXME setWeixinSerial(source.readString());
			source.readString();
			setBirthDate(source.readLong());
			setType(WxOrder.Type.valueOf(source.readByte()));
			setStatus(WxOrder.Status.valueOf(source.readByte()));
			setFoods(source.readParcelList(OrderFood.CREATOR));
		}
	}
	
	public final static Parcelable.Creator<WxOrder> CREATOR = new Parcelable.Creator<WxOrder>(){

		@Override
		public WxOrder newInstance() {
			return new WxOrder(0);
		}
		
		@Override
		public WxOrder[] newInstance(int size){
			return new WxOrder[size];
		}
		
	};
	
}
