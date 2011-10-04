package com.wireless.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wireless.protocol.Taste;
import com.wireless.protocol.Util;
import com.wireless.ui.R;

public class StylesAdapter extends BaseAdapter {
	private LayoutInflater minflater;
	private  List<Taste> stytles;
	public StylesAdapter(Context context,List<Taste> stytles){
		minflater=LayoutInflater.from(context);
		this.stytles=stytles;
	}
	
	@Override
	public int getCount() {
		
		return stytles.size();
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
	
		holder.foodname.setText(stytles.get(position).preference);
		holder.foodprice.setText(Util.CURRENCY_SIGN +Float.toString(stytles.get(position).getPrice()));
		return convertView;
	}
	
   public class Holder{
	   TextView foodname;
	   TextView foodprice;
	   
   }
}
