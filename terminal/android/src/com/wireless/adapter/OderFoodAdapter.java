package com.wireless.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wireless.protocol.Food;
import com.wireless.protocol.Util;
import com.wireless.ui.R;

public class OderFoodAdapter extends BaseAdapter {
	private LayoutInflater minflater;
	private List<Food> foods;
	public OderFoodAdapter(Context context,List<Food> foods){
		minflater=LayoutInflater.from(context);
		this.foods=foods;
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
	public View getView(int position, View convertView, ViewGroup parent) {
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
		holder.foodname.setText(foods.get(position).name);
		holder.count.setText(Float.toString(foods.get(position).getCount()));
		holder.price.setText(Float.toString(foods.get(position).getPrice()*foods.get(position).getCount()));
//		for(int i=0;i<foods.get(position).tastes.length;i++){
//			taste+=foods.get(position).tastes[i];
//		}
//		holder.taste.setText(taste);
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
