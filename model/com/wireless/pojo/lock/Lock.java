package com.wireless.pojo.lock;

import com.wireless.pojo.member.Member;

public class Lock {

	public static class InsertBulder4Charge extends InsertBuilder{
		
		public InsertBulder4Charge(Member member){
			this(member.getId());
		}
		
		public InsertBulder4Charge(int memberId){
			super(Operation.MEMBER_CHARGE, memberId);
		}
	}
	
	public static class InsertBuilder{
		private final Operation operation;
		private final int associatedId;
		
		InsertBuilder(Operation operation, int associatedId){
			this.operation = operation;
			this.associatedId = associatedId;
		}
		
		public Lock build(){
			return new Lock(operation, associatedId);
		}
	}
	 
	public static enum Operation{
		MEMBER_CHARGE(1, "会员充值");
		
		private final int val;
		private final String desc;
		
		Operation(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static Operation valueOf(int val){
			for(Operation operation : values()){
				if(operation.val == val){
					return operation;
				}
			}
			throw new IllegalArgumentException("The operation (val=" + val + ") is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	private Operation operation;
	private int associatedId;
	
	public Lock(Operation operation, int associatedId){
		this.operation = operation;
		this.associatedId = associatedId;
	}
	
	public Operation getOperation(){
		return this.operation;
	}
	
	public void setAssociatedId(int associated){
		this.associatedId = associated;
	}
	
	public int getAssociatedId(){
		return this.associatedId;
	}
	
	@Override
	public int hashCode(){
		return this.operation.hashCode() + Integer.valueOf(this.associatedId).hashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Lock)){
			return false;
		}else{
			return ((Lock)obj).operation == this.operation && ((Lock)obj).associatedId == this.associatedId;
		}
	}
	
	@Override
	public String toString(){
		return operation.desc + "," + associatedId;
	}
	
}
