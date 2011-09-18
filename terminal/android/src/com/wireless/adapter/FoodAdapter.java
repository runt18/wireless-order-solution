package com.wireless.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wireless.protocol.Food;
import com.wireless.protocol.Util;
import com.wireless.ui.R;

public class FoodAdapter extends BaseAdapter {
	private LayoutInflater minflater;
	private Food[] foods;
	public FoodAdapter(Context context,Food[] foods){
		minflater=LayoutInflater.from(context);
		this.foods=foods;
	}
	
	@Override
	public int getCount() {
		
		return foods.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if(convertView==null){
			 convertView=minflater.inflate(R.layout.food_item, null);
			 holder=new Holder();
			 holder.foodname=(TextView)convertView.findViewById(R.id.foodname);
			 holder.foodprice=(TextView)convertView.findViewById(R.id.foodprice);
			 convertView.setTag(holder);
		}else{
			 holder=(Holder)convertView.getTag();
		}
		
		String status = "";
		if(foods[position].isSpecial()){
			status = "ÌØ";
		}
		if(foods[position].isRecommend()){
			if(status.length() == 0){
				status = "¼ö";
			}else{
				status = status + ",¼ö";
			}
		}
		if(foods[position].isGift()){
			if(status.length() == 0){
				status = "Ôù";
			}else{
				status = status + ",Ôù";
			}
		}
		if(status.length() != 0){
			status = "(" + status + ")";
		}
		holder.foodname.setText(foods[position].name+status);
		holder.foodprice.setText(Util.CURRENCY_SIGN +Float.toString(foods[position].getPrice()));
		return convertView;
	}
	
   public class Holder{
	   TextView foodname;
	   TextView foodprice;
	   
   }
}
