package com.wireless.pojo.staffMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.distMgr.Discount;

public class Privilege implements Comparable<Privilege>, Parcelable, Jsonable{

	public static enum Cate{
		UNKNOWN(0, "未知", 0),
		FRONT_BUSINESS(1, "前台", 1),
		BASIC(2, "后台", 2),
		INVENTORY(3, "库存", 3),
		HISTORY(4, "历史", 4),
		MEMBER(5, "会员", 5),
		SYSTEM(6, "系统", 8),
		WEIXIN(7, "微信", 6),
		SMS(8, "短信", 7);
		
		private final int val;
		private final String desc;
		private final int displayId;
		
		Cate(int val, String desc, int displayId){
			this.val = val;
			this.desc = desc;
			this.displayId = displayId;
		}
		
		public static Cate valueOf(int val){
			for(Cate cate : values()){
				if(cate.val == val){
					return cate;
				}
			}
			return UNKNOWN;
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
		
		public int getDisplayId(){
			return displayId;
		}
	}
	
	public static enum Code{
		UNKNOWN(0, Cate.UNKNOWN, 1, "未知"),
		ADD_FOOD(1000, Cate.FRONT_BUSINESS, 1, "点菜"),
		CANCEL_FOOD(1001, Cate.FRONT_BUSINESS, 2, "退菜"),
		DISCOUNT(1002, Cate.FRONT_BUSINESS, 5, "折扣"),
		GIFT(1003, Cate.FRONT_BUSINESS, 6, "赠送"),
		RE_PAYMENT(1004, Cate.FRONT_BUSINESS, 9, "反结帐"),
		PAYMENT(1005, Cate.FRONT_BUSINESS, 8, "结帐"),
		CHECK_ORDER(1006, Cate.FRONT_BUSINESS, 10, "查看账单"),
		TEMP_PAYMENT(1007, Cate.FRONT_BUSINESS, 7, "暂结"),
		PRICE_PLAN(1008, Cate.FRONT_BUSINESS, 4, "价格方案"),
		TRANSFER_FOOD(1009, Cate.FRONT_BUSINESS, 3, "转菜"),
		BASIC(2000, Cate.BASIC, 1, "后台"),
		INVENTORY(5000, Cate.INVENTORY, 1, "库存"),
		HISTORY(4000, Cate.HISTORY, 1, "历史"),
		MEMBER_CHECK(3000, Cate.MEMBER, 1, "会员查询"),
		MEMBER_ADD(3001, Cate.MEMBER, 1, "会员增加"),
		MEMBER_MODIFY(3002, Cate.MEMBER, 2, "会员修改"),
		MEMBER_REMOVE(3003, Cate.MEMBER, 3, "会员删除"),
		MEMBER_CHARGE(3004, Cate.MEMBER, 4, "会员充值"),
		MEMBER_REFUND(3005, Cate.MEMBER, 5, "会员取款"),
		SYSTEM(6000, Cate.SYSTEM, 1, "系统"),
		WEIXIN(7000, Cate.WEIXIN, 1, "微信"),
		SMS(8000, Cate.SMS, 1, "短信");

		
		private final int val;
		private final Cate cate;
		private final int displayId;
		private final String desc;
		
		Code(int val, Cate cate, int displayId, String desc){
			this.val = val;
			this.cate = cate;
			this.displayId = displayId;
			this.desc = desc;
		}
		
		public static Code valueOf(int val){
			for(Code code : values()){
				if(code.val == val){
					return code;
				}
			}
			return UNKNOWN;
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
		
		public int getDisplayId(){
			return this.displayId;
		}
		
		public String getDesc(){
			return desc;
		}
	}
	
	public final static Comparator<Privilege> BY_CATE = new Comparator<Privilege>(){
		@Override
		public int compare(Privilege p0, Privilege p1) {
			if(p0.getCate().getDisplayId() < p1.getCate().getDisplayId()){
				return -1;
			}else if(p0.getCate().getDisplayId() > p1.getCate().getDisplayId()){
				return 1;
			}else{
				if(p0.getCode().getDisplayId() < p1.getCode().getDisplayId()){
					return -1;
				}else if(p0.getCode().getDisplayId() > p1.getCode().getDisplayId()){;
					return 1;
				}else{
					return 0;
				}
			}
		}
	};
	
	private int id;
	private Code code;
	private int restaurantId;
	private final List<Discount> discounts = new ArrayList<Discount>();
	
	Privilege(){
		
	}
	
	public Privilege(int id, Code code){
		setId(id);
		setCode(code);
	}
	
	public Privilege(Code code){
		setCode(code);
	}
	
	public Privilege(int id){
		setId(id);
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
			return getCode().getVal() == ((Privilege)obj).getCode().getVal();
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
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putInt("codeValue", getCode().getVal());
		jm.putString("codeText", getCode().getDesc());
		jm.putInt("restaurantId", this.restaurantId);
		jm.putJsonableList("discounts", this.discounts, 0);
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}
