package com.wireless.dbObject;


public class Setting{
	
	/* 尾数处理的方式 */
	public final static short TAIL_NO_ACTION = 0;			//小数部分不处理
	public final static short TAIL_DECIMAL_CUT = 1;			//小数抹零
	public final static short TAIL_DECIMAL_ROUND = 2;		//小数四舍五入	
	int mPriceTail = TAIL_NO_ACTION;

	public boolean isTailNoAction(){
		return mPriceTail == TAIL_NO_ACTION;
	}
	
	public boolean isTailDecimalCut(){
		return mPriceTail == TAIL_DECIMAL_CUT;
	}
	
	public boolean isTailDecimalRound(){
		return mPriceTail == TAIL_DECIMAL_ROUND;
	}
	
	public void setPriceTail(int priceTail){
		this.mPriceTail = priceTail;
	}
	
	public int getPriceTail(){
		return this.mPriceTail;
	}
	
	boolean mAutoReprint = true;
	
	public boolean isAutoReprint(){
		return mAutoReprint;
	}
	
	public void setAutoReprint(boolean onOff){
		mAutoReprint = onOff;
	}
	
	/* 结帐单显示的选项设置  */
	public final static int RECEIPT_DISCOUNT = 0x01;		//结帐单是否显示折扣
	public final static int RECEIPT_AMOUNT = 0x02;			//结帐单是否显示数量
	public final static int RECEIPT_STATUS = 0x04;			//结帐单是否显示状态
	public final static int RECEIPT_TOTAL_DISCOUNT = 0x08;	//结帐单是否显示折扣额
	public final static int RECEIPT_DEF = RECEIPT_DISCOUNT | RECEIPT_AMOUNT | RECEIPT_STATUS | RECEIPT_TOTAL_DISCOUNT;
	int mReceiptStyle = 0;
	
	public int getReceiptStyle(){
		return mReceiptStyle;
	}
	
	public void setReceiptStyle(int receiptStyle){
		this.mReceiptStyle = receiptStyle;
	}
	
	int mEraseQuota;
	
	public void setEraseQuota(int eraseQuota){
		this.mEraseQuota = eraseQuota;
	}
	
	public int getEraseQuota(){
		return this.mEraseQuota;
	}
	
	public boolean hasEraseQuota(){
		return mEraseQuota != 0;
	}
	
}