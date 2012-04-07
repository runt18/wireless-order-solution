package com.wireless.dbObject;

import java.sql.Timestamp;

import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Taste;

public class SingleOrderFood {
	public String waiter;						//服务员姓名
	public long orderID;						//账单号
	public Timestamp orderDate;					//操作时间
	public String name;							//菜品名称
	public float unitPrice;						//菜品单价
	public float orderCount;					//点菜数量
	public float discount = 1;					//折扣
	public Taste taste = new Taste();			//菜品口味
	public Kitchen kitchen = new Kitchen();		//菜品所属厨房
	
	/**
	 * Calculate the total price to this food as below.
	 * <br>price = (unit_price * discount + taste_price) * count 
	 * @return the total price to this food
	 */
	public float calcPrice2(){
		return (float)Math.round((unitPrice * discount + taste.getPrice()) * orderCount * 100) / 100;
	}
	
	/**
	 * Calculate the total price to this food as below.
	 * <br>price = unit_price * discount * count 
	 * @return the total price to this food
	 */
	public Float calcPrice(){
		return (float)Math.round((unitPrice * discount * orderCount) * 100) / 100;
	}
}
