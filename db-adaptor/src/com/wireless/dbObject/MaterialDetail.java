package com.wireless.dbObject;

import com.wireless.protocol.Department;
import com.wireless.protocol.Food;

public class MaterialDetail{
	public int restaurantID;
	
	public Food food = new Food();
	
	public Department dept = new Department();
	
	public float price;
	
	public long date;
	
	public String staff;
	
	public float amount;
	
	public int type = TYPE_CONSUME;
	
	public final static int TYPE_CONSUME = 0;	//消耗
	public final static int TYPE_WEAR = 1;		//报损
	public final static int TYPE_SELL = 2;		//销售
	public final static int TYPE_RETURN = 3;	//退货
	public final static int TYPE_OUT_WARE = 4;	//出仓
	public final static int TYPE_INCOME = 5;	//入库
	public final static int TYPE_OUT = 6;		//调出
	public final static int TYPE_IN = 7;		//调入
	public final static int TYPE_CHECK = 8;		//盘点
	
	/**
	 * Calculate the price to this material detail.
	 * @return
	 */
	public float calcPrice(){
		return (float)Math.round(price * amount * 100) / 100;
	}
	
}
