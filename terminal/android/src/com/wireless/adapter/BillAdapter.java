package com.wireless.adapter;

import java.util.List;

import com.wireless.adapter.OderFoodAdapter.Holder;
import com.wireless.protocol.Food;
import com.wireless.ui.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BillAdapter extends BaseAdapter {
	private LayoutInflater minflater;
	private List<Food> foods;
	
	public BillAdapter(Context context,List<Food> foods){
		minflater=LayoutInflater.from(context);
		this.foods=foods;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		
			Holder holder;
			if(convertView==null){
				 convertView=minflater.inflate(R.layout.billitem, null);
				 holder=new Holder();
				 holder.foodname=(TextView)convertView.findViewById(R.id.foodname);
				 holder.count=(TextView)convertView.findViewById(R.id.accountvalue);
				 holder.price=(TextView)convertView.findViewById(R.id.pricevalue);
				 holder.taste=(TextView)convertView.findViewById(R.id.taste);
				 convertView.setTag(holder);
			}else{
				 holder=(Holder)convertView.getTag();
			}
			
			
		    holder.foodname.setText(foods.get(position).name);
			holder.count.setText(Float.toString(foods.get(position).getCount()));
			holder.price.setText(Float.toString(foods.get(position).totalPrice2()));
	        if(foods.get(position).tastePref.equals("нч©зн╤")){
	        	holder.taste.setText("");
	        }else{
			holder.taste.setText(foods.get(position).tastePref);
	        }   
			
			
			return convertView;
	}
	 public class Holder{
		   TextView foodname;
		   TextView count;
		   TextView price;
		   TextView taste;
	   }
}
