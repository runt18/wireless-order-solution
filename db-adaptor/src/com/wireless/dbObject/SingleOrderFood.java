package com.wireless.dbObject;

import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Order;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Taste;

public class SingleOrderFood {
	public StaffTerminal staff = new StaffTerminal();		//服务员姓名
	public long orderID;									//账单号
	public long orderDate;									//操作时间
	public float unitPrice;									//菜品单价
	public float orderCount;								//点菜数量
	public float discount = 1;								//折扣
	public Taste taste = new Taste();						//菜品口味
	public Kitchen kitchen = new Kitchen();					//菜品所属厨房
	public Food food = new Food();							//菜品的信息
	public boolean isTemporary = false;						//是否临时菜
	public boolean isPaid = false;							//是否曾经反结帐
	public int payManner = Order.MANNER_CASH;				//结帐方式(现金、刷卡、挂账...)
	public float serviceRate = 0;							//服务费率
	public String comment = null;							//备注
	
	/**
	 * Return whether the order food is gifted.
	 * @return
	 */
	public boolean isGift(){
		return food.isGift();
	}
	
	/**
	 * Return whether the order food is discount.
	 * @return
	 */
	public boolean isDiscount(){
		return discount < 1;
	}
	
	/**
	 * Return whether the order food is cancelled.
	 * @return
	 */
	public boolean isCancelled(){
		return orderCount < 0;
	}
	
	/**
	 * Calculate the discount price to this food as below.<br>
	 * price = unit_price * (1 - discount)
	 * @return the discount price to this food
	 */
	public float calcDiscountPrice(){
		return (float)Math.round(unitPrice * orderCount * (1 - discount) * 100) / 100;
	}
	
	/**
	 * Calculate the total price to this food as below.
	 * <br>price = (unit_price * discount + taste_price) * count * (1 + service_rate)
	 * @return the total price to this food
	 */
	public float calcPriceWithService(){
		return (float)Math.round(calcPriceWithTaste() * (1 + serviceRate) * 100) / 100;
	}
	
	/**
	 * Calculate the total price to this food as below.
	 * <br>price = (unit_price * discount + taste_price) * count 
	 * @return the total price to this food
	 */
	public float calcPriceWithTaste(){
		return (float)Math.round((unitPrice * discount + taste.getPrice()) * orderCount * 100) / 100;
	}
	
	/**
	 * Calculate the total price to this food as below.
	 * <br>price = unit_price * discount * count 
	 * @return the total price to this food
	 */
	public float calcPrice(){
		return (float)Math.round((unitPrice * discount * orderCount) * 100) / 100;
	}
}
