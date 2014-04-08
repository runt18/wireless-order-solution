package com.wireless.pojo.restaurantMgr;

public class Module {

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
		UNKNOWN(0, Cate.UNKNOWN, "unknown"),
		BASIC(1000, Cate.BASIC, "basic"),
		MEMBER(2000, Cate.MEMBER, "member"),
		INVENTORY(3000, Cate.INVENTORY, "inventory"),
		SMS(4000, Cate.SMS, "sms");
		
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
			return this.cate.desc + "," + this.desc;
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
}
