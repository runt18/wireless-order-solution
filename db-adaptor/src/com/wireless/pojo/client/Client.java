package com.wireless.pojo.client;

import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings({"deprecation"})
public class Client {
	public static final int LEVEL_NORMAL = 0;
	public static final int LEVEL_RESERVED = 1;
	public static final String OPERATION_INSERT = "添加客户资料.";
	public static final String OPERATION_UPDATE = "修改客户资料.";
	private int clientID;				// 客户编号
	private int restaurantID;			// 餐厅编号
	private int clientTypeID; 			// 客户类型编号
	private ClientType clientType;		// 客户类型
	private String name;				// 客户名称
	private int sex;					// 性别
	private String tele;				// 电话
	private String mobile;				// 手机
	private long birthday;				// 生日
	private String IDCard;				// 身份证
	private String company;				// 公司
	private String tastePref;			// 口味
	private String taboo;				// 忌讳
	private String contactAddress;		// 联系地址
	private String comment;				// 备注
	private String birthDate;			// 录入时间
	private int level = Client.LEVEL_NORMAL;	// 客户级别 1:匿名用户(系统保留) 0:普通用户
	private int memberAccount;			// 已关联的会员账号数
		
	public int getClientID() {
		return clientID;
	}
	public void setClientID(int clientID) {
		this.clientID = clientID;
	}
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	public int getClientTypeID() {
		return clientTypeID;
	}
	public void setClientTypeID(int clientTypeID) {
		this.clientTypeID = clientTypeID;
	}
	public ClientType getClientType() {
		return clientType;
	}
	public void setClientType(ClientType clientType) {
		this.clientType = clientType;
	}
	public void setClientType(int typeID, String name, int parentID, int restaurantID) {
		this.clientType = new ClientType(typeID, name, parentID, restaurantID);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getSex() {
		return sex;
	}
	public String getSexDisplay() {
		return this.sex == 0 ? "男" : "女";
	}
	public void setSex(int sex) {
		this.sex = sex;
	}
	public String getTele() {
		return tele;
	}
	public void setTele(String tele) {
		this.tele = tele;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public long getBirthday() {
		return birthday;
	}
	public String getBirthdayFormat() {
		return this.birthday == 0 ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.birthday);
	}
	public void setBirthday(long birthday) {
		this.birthday = birthday;
	}
	public void setBirthday(String birthday) {
		if(birthday != null && birthday.trim().length() > 0){
			this.birthday = Date.parse(birthday.replaceAll("-", "/"));
		}else{
			this.birthday = 0;
		}
	}
	public String getIDCard() {
		return IDCard;
	}
	public void setIDCard(String iDCard) {
		IDCard = iDCard;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getTastePref() {
		return tastePref;
	}
	public void setTastePref(String tastePref) {
		this.tastePref = tastePref;
	}
	public String getTaboo() {
		return taboo;
	}
	public void setTaboo(String taboo) {
		this.taboo = taboo;
	}
	public String getContactAddress() {
		return contactAddress;
	}
	public void setContactAddress(String contactAddress) {
		this.contactAddress = contactAddress;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getMemberAccount() {
		return memberAccount;
	}
	public void setMemberAccount(int memberAccount) {
		this.memberAccount = memberAccount;
	}	
	
}
