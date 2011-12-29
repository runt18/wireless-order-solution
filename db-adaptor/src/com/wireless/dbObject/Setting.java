package com.wireless.dbObject;

import com.wireless.protocol.Order;

public class Setting{
	public int priceTail = Order.TAIL_NO_ACTION;
	public boolean autoReprint = true;
	
	/* 结帐单显示的选项设置  */
	public final static int RECEIPT_DISCOUNT = 0x01;		//结帐单是否显示折扣
	public final static int RECEIPT_AMOUNT = 0x02;			//结帐单是否显示数量
	public final static int RECEIPT_STATUS = 0x04;			//结帐单是否显示状态
	public final static int RECEIPT_TOTAL_DISCOUNT = 0x08;	//结帐单是否显示折扣额
	public final static int RECEIPT_DEF = RECEIPT_DISCOUNT | RECEIPT_AMOUNT | RECEIPT_STATUS | RECEIPT_TOTAL_DISCOUNT;
	public int receiptStyle = 0;
	
	
}