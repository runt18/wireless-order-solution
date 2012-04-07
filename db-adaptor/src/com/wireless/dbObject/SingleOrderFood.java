package com.wireless.dbObject;

import java.sql.Timestamp;

import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Taste;

public class SingleOrderFood {
	public String waiter;						//����Ա����
	public long orderID;						//�˵���
	public Timestamp orderDate;					//����ʱ��
	public String name;							//��Ʒ����
	public float unitPrice;						//��Ʒ����
	public float orderCount;					//�������
	public float discount = 1;					//�ۿ�
	public Taste taste = new Taste();			//��Ʒ��ζ
	public Kitchen kitchen = new Kitchen();		//��Ʒ��������
	
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
