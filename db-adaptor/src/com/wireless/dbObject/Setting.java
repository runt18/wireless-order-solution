package com.wireless.dbObject;


public class Setting{
	
	/* β������ķ�ʽ */
	public final static short TAIL_NO_ACTION = 0;			//С�����ֲ�����
	public final static short TAIL_DECIMAL_CUT = 1;			//С��Ĩ��
	public final static short TAIL_DECIMAL_ROUND = 2;		//С����������	
	public int priceTail = TAIL_NO_ACTION;

	
	public boolean autoReprint = true;
	
	/* ���ʵ���ʾ��ѡ������  */
	public final static int RECEIPT_DISCOUNT = 0x01;		//���ʵ��Ƿ���ʾ�ۿ�
	public final static int RECEIPT_AMOUNT = 0x02;			//���ʵ��Ƿ���ʾ����
	public final static int RECEIPT_STATUS = 0x04;			//���ʵ��Ƿ���ʾ״̬
	public final static int RECEIPT_TOTAL_DISCOUNT = 0x08;	//���ʵ��Ƿ���ʾ�ۿ۶�
	public final static int RECEIPT_DEF = RECEIPT_DISCOUNT | RECEIPT_AMOUNT | RECEIPT_STATUS | RECEIPT_TOTAL_DISCOUNT;
	public int receiptStyle = 0;
	
	
}