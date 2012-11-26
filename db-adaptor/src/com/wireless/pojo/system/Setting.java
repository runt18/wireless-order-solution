package com.wireless.pojo.system;

import com.wireless.util.WebParams;

public class Setting {
	
	private int settingID;
	private int restaurantID;
	private int priceTail = WebParams.TAIL_NO_ACTION;	// 金额尾数处理方式  0:不处理  1:抹零 2:四舍五入
	private int autoReprint;		// 是否自动打印
	private long receiptStyle;  
	private int eraseQuota;
	
	public int getSettingID() {
		return settingID;
	}
	public void setSettingID(int settingID) {
		this.settingID = settingID;
	}
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
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
	public int getEraseQuota() {
		return eraseQuota;
	}
	public void setEraseQuota(int eraseQuota) {
		this.eraseQuota = eraseQuota;
	}
	public boolean getEraseQuotaStatus(){
		return this.eraseQuota != 0;
	}
}
