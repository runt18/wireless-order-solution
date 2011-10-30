package com.wireless.adapter;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wireless.protocol.Food;
import com.wireless.ui.R;
import com.wireless.ui.OrderActivity;

public class OderFoodAdapter extends BaseAdapter {
	private LayoutInflater minflater;
	private List<Food> foods;
	private OrderActivity orer;

	public OderFoodAdapter(Context context,List<Food> foods){
		minflater=LayoutInflater.from(context);
		this.foods=foods;
		orer=(OrderActivity)context;
	}
	
	@Override
	public int getCount() {
		
		return foods.size();
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder holder;
		if(convertView==null){
			 convertView=minflater.inflate(R.layout.oderfooditem, null);
			 holder=new Holder();
			 holder.foodname=(TextView)convertView.findViewById(R.id.foodname);
			 holder.count=(TextView)convertView.findViewById(R.id.accountvalue);
			 holder.price=(TextView)convertView.findViewById(R.id.pricevalue);
			 holder.taste=(TextView)convertView.findViewById(R.id.taste);
			 holder.deletefood=(ImageView)convertView.findViewById(R.id.deletefood);
			 holder.addtaste=(ImageView)convertView.findViewById(R.id.addtaste);
			 
			 convertView.setTag(holder);
		}else{
			 holder=(Holder)convertView.getTag();
		}
		
		String taste="";
	    if(foods.get(position).hangStatus==Food.FOOD_HANG_UP){
	    	holder.foodname.setText("(╫п)"+foods.get(position).name);
	    	Log.e("", "(╫п)"+foods.get(position).name);
	    }else if(foods.get(position).hangStatus==Food.FOOD_NORMAL){
	    	holder.foodname.setText(foods.get(position).name);
	    }
		
		holder.count.setText(Float.toString(foods.get(position).getCount()));
		holder.price.setText(Float.toString(foods.get(position).totalPrice2()));
        if(foods.get(position).tastePref.equals("нч©зн╤")){
        	holder.taste.setText("");
        }else{
		holder.taste.setText(foods.get(position).tastePref);
        }   
		
		 holder.deletefood.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				orer.Foodfunction(0,position);
			}
		});
		 holder.addtaste.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					orer.Foodfunction(1,position);
					
				}
			});
		return convertView;
	}
	
   public class Holder{
	   TextView foodname;
	   TextView count;
	   TextView price;
	   TextView taste;
	   ImageView deletefood;
	   ImageView addtaste;
   }
}
