package com.wireless.pojo.dishesOrder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.util.NumericUtil;

public class MixedPayment implements Parcelable{
	
	public static class InsertBuilder{
		private final int orderId;
		private final Map<PayType, Float> mixed = new HashMap<PayType, Float>();
		
		public InsertBuilder(Order order){
			this.orderId = order.getId();
		}
		
		public InsertBuilder(int orderId){
			this.orderId = orderId;
		}
		
		public InsertBuilder setPayments(Map<PayType, Float> mixed){
			if(mixed != null){
				this.mixed.clear();
				this.mixed.putAll(mixed);
			}
			return this;
		}
		
		public MixedPayment build(){
			return new MixedPayment(this);
		}
	}
	
	private int orderId;
	private final Map<PayType, Float> mixed = new HashMap<PayType, Float>();

	public MixedPayment(){
		
	}
	
	private MixedPayment(InsertBuilder builder){
		this.orderId = builder.orderId;
		this.mixed.putAll(builder.mixed);
	}
	
	public void add(PayType payType, float price){
		mixed.put(payType, price);
	}

	public Map<PayType, Float> getPayments(){
		return Collections.unmodifiableMap(mixed);
	}
	
	public float getPrice(){
		float price = 0;
		for(Entry<PayType, Float> entry : mixed.entrySet()){
			price += entry.getValue().floatValue();
		}
		return NumericUtil.roundFloat(price);
	}
	
	public void setOrderId(int orderId){
		this.orderId = orderId;
	}
	
	public int getOrderId(){
		return this.orderId;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeInt(mixed.size());
		for(Entry<PayType, Float> entry : mixed.entrySet()){
			dest.writeParcel(entry.getKey(), PayType.PAY_TYPE_PARCELABLE_SIMPLE);
			dest.writeFloat(entry.getValue());
		}
	}

	@Override
	public void createFromParcel(Parcel source) {
		int size = source.readInt();
		for(int i = 0; i < size; i++){
			PayType payType = source.readParcel(PayType.CREATOR);
			float price = source.readFloat();
			mixed.put(payType, price);
		}
	}
	
	public final static Parcelable.Creator<MixedPayment> CREATOR = new Parcelable.Creator<MixedPayment>() {
		
		@Override
		public MixedPayment[] newInstance(int size) {
			return new MixedPayment[size];
		}
		
		@Override
		public MixedPayment newInstance() {
			return new MixedPayment();
		}
	};
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof MixedPayment)){
			return false;
		}else{
			MixedPayment mixedPayment = ((MixedPayment)obj);
			return orderId == mixedPayment.orderId && mixed.equals(mixedPayment.mixed);
		}
	}
	
	@Override
	public String toString(){
		return "(" + orderId + ")" + mixed;
				
	}
}
