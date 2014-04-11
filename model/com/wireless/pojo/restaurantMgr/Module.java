package com.wireless.pojo.restaurantMgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;

public class Module implements Comparable<Module>, Jsonable{

	public static enum Cate{
		UNKNOWN(0, "unknown"),
		BASIC(1, "basic"),
		MEMBER(2, "member"),
		INVENTORY(3, "inventory"),
		SMS(4, "sms");
		
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
			return Cate.UNKNOWN;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	public static enum Code{
		UNKNOWN(0, Cate.UNKNOWN, "未知"),
		BASIC(1000, Cate.BASIC, "基础"),
		MEMBER(2000, Cate.MEMBER, "会员"),
		INVENTORY(3000, Cate.INVENTORY, "库存"),
		SMS(4000, Cate.SMS, "短信");
		
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
			return Code.UNKNOWN;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public Cate getCate(){
			return this.cate;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		@Override
		public String toString(){
			return desc;
		}
	}
	
	private int id;
	private Code code = Code.UNKNOWN;
	
	public Module(int id, Code code){
		this(code);
		this.id = id;
	}
	
	public Module(Code code){
		this.code = code;
	}
	
	public int getId(){
		return this.id;
	}
	
	public Code getCode(){
		return this.code;
	}
	
	public Cate getCate(){
		return this.code.getCate();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Module)){
			return false;
		}else{
			return code == ((Module)obj).code;
		}
	}
	
	@Override
	public int hashCode(){
		return code.getVal() * 31 + 17;
	}
	
	@Override
	public String toString(){
		return code.toString();
	}

	@Override
	public int compareTo(Module module) {
		if(code.val > module.code.val){
			return 1;
		}else if(code.val < module.code.val){
			return -1;
		}else{
			return 0;
		}
	}

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("code", getCode().getVal());
		jm.put("desc", getCode().getDesc());
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
}
