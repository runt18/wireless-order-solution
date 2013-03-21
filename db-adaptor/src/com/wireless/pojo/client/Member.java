package com.wireless.pojo.client;

import com.wireless.pojo.system.Staff;
import com.wireless.util.DateUtil;

public class Member {
	public static final int STATUS_NORMAL = 0;
	public static final int STATUS_DISABLED = 1;
	public static final String OPERATION_INSERT = "添加会员资料.";
	public static final String OPERATION_UPDATE = "修改会员资料.";
	public static final String OPERATION_DELETE = "删除会员资料.";
	public static final String OPERATION_CHARGE = "会员充值.";
	private int id;
	private int restaurantID;
	private float baseBalance;
	private float extraBalance;
	private int point;
	private long birthDate;
	private long lastModDate;
	private String comment;
	private int status = Member.STATUS_NORMAL;
	
	private MemberType memberType = null;
	private MemberCard memberCard = null;
	private Client client = null;
	private Staff staff = null;
	
	public Member(){
		this.memberType = new MemberType();
		this.memberCard = new MemberCard();
		this.client = new Client();
		this.staff = new Staff();
		this.staff.setTerminal(null);
	}
	
	/**
	 * 
	 * @param id
	 * @param baseBalance
	 * @param extraBalance
	 * @param staffID
	 * @param comment
	 * @return
	 */
	public static Member buildToBalance(int id, float baseBalance, float extraBalance, long staffID, String comment){
		Member updateBalance = new Member();
		updateBalance.setId(id);
		updateBalance.setBaseBalance(baseBalance);
		updateBalance.setExtraBalance(extraBalance);
		updateBalance.getStaff().setId(staffID);
		updateBalance.setComment(comment);
		return updateBalance;
	}
	
	/**
	 * 
	 * @param id
	 * @param point
	 * @param staffID
	 * @param comment
	 * @return
	 */
	public static Member buildToPoint(int id, int point, long staffID, String comment){
		Member updateBalance = new Member();
		updateBalance.setId(id);
		updateBalance.setPoint(point);
		updateBalance.getStaff().setId(staffID);
		updateBalance.setComment(comment);
		return updateBalance;
	}
	
	public float getTotalBalance(){
		return (this.baseBalance + this.extraBalance);
	}
	
	public int getMemberTypeID() {
		return this.memberType == null ? 0 : this.memberType.getTypeID();
	}
	public void setMemberTypeID(int memberTypeID) {
		this.memberType = this.memberType == null ? new MemberType() : this.memberType;
		this.memberType.setTypeID(memberTypeID);
	}
	public int getMemberCardID() {
		return this.memberCard == null ? 0 : this.memberCard.getId();
	}
	public void setMemberCardID(int memberCardID) {
		this.memberCard = this.memberCard == null ? new MemberCard() : this.memberCard;
		this.memberCard.setId(memberCardID);
	}
	public long getStaffID() {
		return this.staff == null ? 0 : this.staff.getId();
	}
	public void setStaffID(long staffID) {
		this.staff = this.staff == null ? new Staff() : this.staff;
		this.staff.setId(staffID);
	}
	public int getClientID() {
		return this.client == null ? 0 : this.client.getClientID();
	}
	public void setClientID(int clientID) {
		this.client = this.client == null ? new Client() : this.client;
		this.client.setClientID(clientID);
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	public float getBaseBalance() {
		return baseBalance;
	}
	public void setBaseBalance(float baseBalance) {
		this.baseBalance = baseBalance;
	}
	public float getExtraBalance() {
		return extraBalance;
	}
	public void setExtraBalance(float extraBalance) {
		this.extraBalance = extraBalance;
	}
	public int getPoint() {
		return point;
	}
	public void setPoint(int point) {
		this.point = point;
	}
	public long getBirthDate() {
		return birthDate;
	}public String getBirthDateFormat() {
		return DateUtil.format(birthDate);
	}
	public void setBirthDate(long birthDate) {
		this.birthDate = birthDate;
	}
	public long getLastModDate() {
		return lastModDate;
	}
	public String getLastModDateFormat() {
		return DateUtil.format(lastModDate);
	}
	public void setLastModDate(long lastModDate) {
		this.lastModDate = lastModDate;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public MemberType getMemberType() {
		return memberType;
	}
	public void setMemberType(MemberType memberType) {
		this.memberType = memberType;
	}
	public MemberCard getMemberCard() {
		return memberCard;
	}
	public void setMemberCard(MemberCard memberCard) {
		this.memberCard = memberCard;
	}
	public Client getClient() {
		return client;
	}
	public void setClient(Client client) {
		this.client = client;
	}
	public Staff getStaff() {
		return staff;
	}
	public void setStaff(Staff staff) {
		this.staff = staff;
	}
	
}
