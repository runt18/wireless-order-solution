package com.wireless.pojo.staffMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.distMgr.Discount;

public class Privilege implements Comparable<Privilege>, Parcelable, Jsonable{

	public static enum Cate{
		FRONT_BUSINESS(1, "前台"),
		BASIC(2, "后台"),
		INVENTORY(3, "库存"),
		HISTORY(4, "历史"),
		MEMBER(5, "会员"),
		SYSTEM(6, "系统");
		
		private final int val;
		private final String desc;
		
		Cate(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static Cate valueOf(int val){
			for(Cate cate : values()){
				if(cate.val == val){
					return cate;
				}
			}
			throw new IllegalArgumentException("The cate(val = " + val + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return "code(" + this.val + ", " + this.desc + ")";
		}
		
		public int getVal(){
			return val;
		}
		
		public String getDesc(){
			return desc;
		}
	}
	
	public static enum Code{
		FRONT_BUSINESS(1000, Cate.FRONT_BUSINESS, "点菜"),
		CANCEL_FOOD(1001, Cate.FRONT_BUSINESS, "退菜"),
		DISCOUNT(1002, Cate.FRONT_BUSINESS, "折扣"),
		GIFT(1003, Cate.FRONT_BUSINESS, "赠送"),
		RE_PAYMENT(1004, Cate.FRONT_BUSINESS, "反结帐"),
		PAYMENT(1005, Cate.FRONT_BUSINESS, "结帐"),
		CHECK_ORDER(1006, Cate.FRONT_BUSINESS, "账单"),
		BASIC(2000, Cate.BASIC, "后台"),
		INVENTORY(3000, Cate.INVENTORY, "库存"),
		HISTORY(4000, Cate.HISTORY, "历史"),
		MEMBER(5000, Cate.MEMBER, "会员"),
		SYSTEM(6000, Cate.SYSTEM, "系统");
		
		private final int val;
		private final Cate cate;
		private final String desc;
		
		Code(int val, Cate cate, String desc){
			this.val = val;
			this.cate = cate;
			this.desc = desc;
		}
		
		public static Code valueOf(int val){
			for(Code code : values()){
				if(code.val == val){
					return code;
				}
			}
			throw new IllegalArgumentException("The code(val = " + val + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return "code(" + this.val + ", " + this.desc + ")";
		}
		
		public Cate getCate(){
			return cate;
		}
		
		public int getVal(){
			return val;
		}
		
		public String getDesc(){
			return desc;
		}
	}
	
	private int id;
	private Code code;
	private int restaurantId;
	private List<Discount> discounts = new ArrayList<Discount>();
	
	Privilege(){
		
	}
	
	public Privilege(int id, Code code, int restaurantId){
		setId(id);
		setCode(code);
		setRestaurantId(restaurantId);
	}
	
	public Privilege(Code code){
		setCode(code);
	}
	public Privilege(int pId){
		setId(pId);
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
	
	public boolean isAllDiscount(){
		return discounts.isEmpty();
	}
	
	public void setAllDiscount(){
		discounts.clear();
	}
	
	public List<Discount> getDiscounts() {
		return Collections.unmodifiableList(discounts);
	}
	
	public void addDiscount(Discount discount){
		if(discount != null){
			discounts.add(discount);
		}
	}
	
	public Cate getCate(){
		return this.code.getCate();
	}
	
	public Code getCode(){
		return this.code;
	}
	
	public void setCode(Code code){
		if(code != null){
			this.code = code;
		}
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Privilege)){
			return false;
		}else{
			return getId() == ((Privilege)obj).getId();
		}
	}
	
	@Override
	public int hashCode(){
		return 31 * id + 17;
	}
	
	@Override
	public String toString(){
		return code.toString();
	}

	@Override
	public int compareTo(Privilege o) {
		if(getCode().getVal() > o.getCode().getVal()){
			return 1;
		}else if(getCode().getVal() < o.getCode().getVal()){
			return -1;
		}else{
			return 0;
		}
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeInt(getCode().getVal());
		dest.writeParcelList(discounts, Discount.DISCOUNT_PARCELABLE_COMPLEX);
	}

	@Override
	public void createFromParcel(Parcel source) {
		setCode(Code.valueOf(source.readInt()));
		discounts.clear();
		discounts.addAll(source.readParcelList(Discount.CREATOR));
	}

	public final static Parcelable.Creator<Privilege> CREATOR = new Parcelable.Creator<Privilege>(){

		@Override
		public Privilege newInstance() {
			return new Privilege();
		}
		
		@Override
		public Privilege[] newInstance(int size){
			return new Privilege[size];
		}
		
	};

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("id", this.id);
		jm.put("codeValue", getCode().getVal());
		jm.put("codeText", getCode().getDesc());
		jm.put("restaurantId", this.restaurantId);
		jm.put("discounts", this.discounts);
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
}
