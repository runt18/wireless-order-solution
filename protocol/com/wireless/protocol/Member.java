package com.wireless.protocol;

public class Member {
	public String alias_id;
	public String name;
	public String tele;
	public Float balance;
	
	public Member(String id, String mName, String phone, Float bal){
		alias_id = id;
		name = mName;
		tele = phone;
		balance = bal;
	}
	
}
