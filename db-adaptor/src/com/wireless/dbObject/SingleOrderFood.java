package com.wireless.dbObject;

import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Order;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Taste;

public class SingleOrderFood {
	public StaffTerminal staff = new StaffTerminal();		//����Ա����
	public long orderID;									//�˵���
	public long orderDate;									//����ʱ��
	public String name;										//��Ʒ����
	public float unitPrice;									//��Ʒ����
	public float orderCount;								//�������
	public float discount = 1;								//�ۿ�
	public Taste taste = new Taste();						//��Ʒ��ζ
	public Kitchen kitchen = new Kitchen();					//��Ʒ��������
	public Food food = new Food();							//��Ʒ����Ϣ
	public boolean isTemporary = false;						//�Ƿ���ʱ��
	public boolean isPaid = false;							//�Ƿ�����������
	public int payManner = Order.MANNER_CASH;				//���ʷ�ʽ(�ֽ�ˢ��������...)
	public float serviceRate = 0;							//�������
	
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
