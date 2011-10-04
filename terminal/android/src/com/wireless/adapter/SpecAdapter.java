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

public class SpecAdapter extends BaseAdapter {
	private LayoutInflater minflater;
	private  List<Taste> speces;
	public SpecAdapter(Context context,List<Taste> speces){
		minflater=LayoutInflater.from(context);
		this.speces=speces;
	}
	
	@Override
	public int getCount() {
		
		return speces.size();
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
	
		holder.foodname.setText(speces.get(position).preference);
		holder.foodprice.setText(Util.CURRENCY_SIGN +Float.toString(speces.get(position).getPrice()));
		return convertView;
	}
	
   public class Holder{
	   TextView foodname;
	   TextView foodprice;
	   
   }
}
