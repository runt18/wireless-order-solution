package com.wireless.protocol;

public class MaterialDetail{
	public int restaurantID;
	
	int price;
	
	public void setPrice(Float _price){
		price = Util.float2Int(_price);
	}
	
	public Float getPrice(){
		return Util.int2Float(price);
	}
	
	public long date;
	
	public String staff;
	
	int amount;
	
	public void setAmount(Float _amount){
		amount = Util.float2Int(_amount);
	}
	
	public Float getAmount(){
		return Util.int2Float(amount);
	}
	
	public final static int TYPE_CONSUME = 0;	//消耗
	public final static int TYPE_WEAR = 1;		//报损
	public final static int TYPE_SELL = 2;		//销售
	public final static int TYPE_RETURN = 3;	//退货
	public final static int TYPE_INCOME = 4;	//入库
	public final static int TYPE_OUT = 5;		//调出
	public final static int TYPE_IN = 6;		//调入
	public final static int TYPE_CHECK = 7;		//盘点
	public int type = TYPE_CONSUME;
	
}
