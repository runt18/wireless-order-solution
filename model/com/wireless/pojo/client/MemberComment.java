package com.wireless.pojo.client;

import com.wireless.pojo.staffMgr.Staff;

public class MemberComment {

	public static enum Type{
		PUBLIC(1, "公开"),
		PRIVATE(2, "私有");
		
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
			throw new IllegalArgumentException("The val(" + val + ") is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		@Override
		public String toString(){
			return "type(val = " + val + ", desc = " + desc + ")";
		}
	}
	
	public static class CommitBuilder{
		private final int staffId;
		private final int memberId;
		private final Type type;
		private String comment;
		
		public static CommitBuilder newPublicBuilder(int staffId, int memberId, String comment){
			CommitBuilder builder = new CommitBuilder(staffId, memberId, Type.PUBLIC);
			builder.setComment(comment);
			return builder;
		}
		
		public static CommitBuilder newPrivateBuilder(int staffId, int memberId, String comment){
			CommitBuilder builder = new CommitBuilder(staffId, memberId, Type.PRIVATE);
			builder.setComment(comment);
			return builder;
		}
		
		private CommitBuilder(int staffId, int memberId, Type type){
			this.staffId = staffId;
			this.memberId = memberId;
			this.type = type;
		}
		
		public CommitBuilder setComment(String comment){
			this.comment = comment;
			return this;
		}
		
		public MemberComment build(){
			return new MemberComment(this);
		}
	}
	
	private Staff staff;
	private Member member;
	private String comment;
	private Type type;
	private long lastModified;
	
	private MemberComment(CommitBuilder builder){
		setStaff(new Staff(builder.staffId));
		setMember(new Member(builder.memberId));
		setType(builder.type);
		setComment(builder.comment);
		setLastModified(System.currentTimeMillis());
	}
	
	public static MemberComment newPublicComment(Staff staff, Member member, String comment){
		MemberComment publicComment = new MemberComment();
		publicComment.setStaff(staff);
		publicComment.setMember(member);
		publicComment.setComment(comment);
		publicComment.setType(Type.PUBLIC);
		return publicComment;
	}
	
	public static MemberComment newPrivateComment(Staff staff, Member member, String comment){
		MemberComment privateComment = new MemberComment();
		privateComment.setStaff(staff);
		privateComment.setMember(member);
		privateComment.setComment(comment);
		privateComment.setType(Type.PRIVATE);
		return privateComment;
	}
	
	private MemberComment(){
		
	}
	
	public Staff getStaff(){
		return staff;
	}
	
	public void setStaff(Staff staff){
		this.staff = staff;
	}
	
	public Member getMember(){
		return member;
	}
	
	public void setMember(Member member){
		this.member = member;
	}
	
	public Type getType(){
		return type;
	}
	
	public void setType(Type type){
		this.type = type;
	}
	
	public String getComment(){
		if(comment == null){
			return "";
		}else{
			return comment;
		}
	}
	
	public void setComment(String comment){
		this.comment = comment;
	}
	
	public long getLastModified(){
		return lastModified;
	}
	
	public void setLastModified(long lastModified){
		this.lastModified = lastModified;
	}
	
	int getStaffId(){
		return (staff != null ? staff.getId() : 0);
	}
	
	int getMemberId(){
		return (member != null ? member.getId() : 0);
	}
	
	@Override
	public int hashCode(){
		int result = 17;
		result = result * 31 + getStaffId();
		result = result * 31 + getMemberId();
		return result;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof MemberComment)){
			return false;
		}else{
			return getStaffId() == ((MemberComment)obj).getStaffId() &&
				   getMemberId() == ((MemberComment)obj).getMemberId() &&
				   getType() == ((MemberComment)obj).getType();
		}
	}
	
	@Override
	public String toString(){
		return this.comment;
	}
}
