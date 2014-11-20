package com.wireless.parcel;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.weixin.order.WxOrder;

public class WxOrderParcel implements Parcelable{

	public static final String KEY_VALUE = WxOrderParcel.class.getName();
	
	private final WxOrder mSrc;
	
	public WxOrder asWxOrder(){
		return mSrc;
	}
	
	public WxOrderParcel(WxOrder wxOrder){
		this.mSrc = wxOrder;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	private WxOrderParcel(Parcel in){
		if(in.readInt() != 1){
			mSrc = new WxOrder(in.readInt());
			mSrc.setCode(in.readInt());
			mSrc.setOrderId(in.readInt());
			mSrc.setStatus(WxOrder.Status.valueOf(in.readInt()));
			mSrc.setType(WxOrder.Type.valueOf(in.readInt()));
			//unmarshal the foods		
			for(OrderFoodParcel foodParcel : in.createTypedArrayList(OrderFoodParcel.CREATOR)){
				mSrc.addFood(foodParcel.asOrderFood());
			}
		}else{
			mSrc = null;
		}
	}
	
	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		if(mSrc == null){
			parcel.writeInt(1);
		}else{
			parcel.writeInt(0);
			parcel.writeInt(mSrc.getId());
			parcel.writeInt(mSrc.getCode());
			parcel.writeInt(mSrc.getOrderId());
			parcel.writeInt(mSrc.getStatus().getVal());
			parcel.writeInt(mSrc.getType().getVal());
			//marshal the foods
			List<OrderFoodParcel> foodParcels = new ArrayList<OrderFoodParcel>(mSrc.getFoods().size());
			for(OrderFood of : mSrc.getFoods()){
				foodParcels.add(new OrderFoodParcel(of));
			}
			parcel.writeTypedList(foodParcels);
		}		
	}

    public static final Parcelable.Creator<WxOrderParcel> CREATOR = new Parcelable.Creator<WxOrderParcel>() {
    	public WxOrderParcel createFromParcel(Parcel in) {
   			return new WxOrderParcel(in);
    	}

    	public WxOrderParcel[] newArray(int size) {
    		return new WxOrderParcel[size];
    	}
    };
}
