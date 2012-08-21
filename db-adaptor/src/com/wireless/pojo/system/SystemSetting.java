package com.wireless.pojo.system;

import java.util.ArrayList;
import java.util.List;

import com.wireless.util.WebParams;

public class SystemSetting {
	private int id;				// 餐厅编号
	private String name;		// 餐厅名称
	private String info;		// 餐厅公告
	private String account;		// 餐厅账号
	private long recordAlive;	// 
	private int settingID;		// 餐厅设置编号
	private String address;		// 餐厅地址
	private int priceTail = WebParams.TAIL_NO_ACTION;	// 金额尾数处理方式  0:不处理  1:抹零 2:四舍五入
	private int autoReprint;	// 是否自动打印
	private long receiptStyle;   //
	private List<Staff> staff = new ArrayList<Staff>();   // 餐厅员工(暂未使用该字段)
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public long getRecordAlive() {
		return recordAlive;
	}
	public void setRecordAlive(long recordAlive) {
		this.recordAlive = recordAlive;
	}
	public int getSettingID() {
		return settingID;
	}
	public void setSettingID(int settingID) {
		this.settingID = settingID;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getPriceTail() {
		return priceTail;
	}
	public void setPriceTail(int priceTail) {
		this.priceTail = priceTail;
	}
	public int getAutoReprint() {
		return autoReprint;
	}
	public void setAutoReprint(int autoReprint) {
		this.autoReprint = autoReprint;
	}
	public long getReceiptStyle() {
		return receiptStyle;
	}
	public void setReceiptStyle(long receiptStyle) {
		this.receiptStyle = receiptStyle;
	}
	public List<Staff> getStaff() {
		return staff;
	}
	public void setStaff(List<Staff> staff) {
		this.staff = staff;
	}
	
	/**
	 * 金额尾数处理方式显示信息
	 * @return
	 */
	public String getPriceTailDisplay() {
		String display = "不处理";
		if(priceTail == WebParams.TAIL_NO_ACTION){
			display = "不处理";
		}else if(priceTail == WebParams.TAIL_DECIMAL_CUT){
			display = "抹零";
		}else if(priceTail == WebParams.TAIL_DECIMAL_ROUND){
			display = "四舍五入";
		}
		return display;
	}
	
}
