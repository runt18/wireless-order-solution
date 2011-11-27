package com.wireless.dbObject;

public class MaterialDetail{
	private int restaurantID;
	
	private float price;
	
	private long date;
	
	private String staff;
	
	private float amount;
	
	private int type = TYPE_CONSUME;
	
	public final static int TYPE_CONSUME = 0;	//����
	public final static int TYPE_WEAR = 1;		//����
	public final static int TYPE_SELL = 2;		//����
	public final static int TYPE_RETURN = 3;	//�˻�
	public final static int TYPE_OUT_WARE = 4;	//����
	public final static int TYPE_INCOME = 5;	//���
	public final static int TYPE_OUT = 6;		//����
	public final static int TYPE_IN = 7;		//����
	public final static int TYPE_CHECK = 8;		//�̵�
	
	public int getRestaurantID() {
		return restaurantID;
	}

	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public String getStaff() {
		return staff;
	}

	public void setStaff(String staff) {
		this.staff = staff;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	
}
