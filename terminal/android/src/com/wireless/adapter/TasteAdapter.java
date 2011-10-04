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

public class TasteAdapter extends BaseAdapter {
	private LayoutInflater minflater;
	private  List<Taste> tastes;
	public TasteAdapter(Context context,List<Taste> tastes){
		minflater=LayoutInflater.from(context);
		this.tastes=tastes;
	}
	
	@Override
	public int getCount() {
		
		return tastes.size();
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
	
		holder.foodname.setText(tastes.get(position).preference);
		holder.foodprice.setText(Util.CURRENCY_SIGN +Float.toString(tastes.get(position).getPrice()));
		return convertView;
	}
	
   public class Holder{
	   TextView foodname;
	   TextView foodprice;
	   
   }
}
