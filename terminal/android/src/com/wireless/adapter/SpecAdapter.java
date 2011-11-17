package com.wireless.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.wireless.common.Common;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Util;
import com.wireless.ui.R;
import com.wireless.ui.SpecsActivity;

public class SpecAdapter extends BaseAdapter {
	private LayoutInflater minflater;
	private  List<Taste> speces;
	private Context context;
	private TextView test;
	private SpecsActivity spec;
	public SpecAdapter(Context context,List<Taste> speces,TextView test){
		minflater=LayoutInflater.from(context);
		this.speces=speces;
		this.context=context;
		this.test=test;
		spec=(SpecsActivity)context;
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		final Holder holder;
		if(convertView==null){
			 convertView=minflater.inflate(R.layout.food_item, null);
			 holder=new Holder();
			 holder.foodname=(TextView)convertView.findViewById(R.id.foodname);
			 holder.foodprice=(TextView)convertView.findViewById(R.id.foodprice);
			 holder.chioce=(CheckBox)convertView.findViewById(R.id.chioce);
			 convertView.setTag(holder);
		}else{
			 holder=(Holder)convertView.getTag();
		}
	
		holder.foodname.setText(speces.get(position).preference);
		holder.foodprice.setText(Util.CURRENCY_SIGN +Float.toString(speces.get(position).getPrice()));
		
		OrderFood food=Common.getCommon().getFoodlist().get(Common.getCommon().getPosition());
		for(int i=0;i<food.tastes.length;i++){
			  if(food.tastes[i].alias_id==speces.get(position).alias_id){
				 // tastes.get(position).setChoice(true);
					holder.chioce.setChecked(true);
				 break;
				}else{
				 //tag= false;
					holder.chioce.setChecked(false);
				}
			
		 }
		holder.chioce.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(holder.chioce.isChecked()){
					Common.getCommon().addtaste(context, Common.getCommon().getFoodlist(),speces, Common.getCommon().getPosition(), position,"规格",test, holder.chioce);
				
					
				}else{
					Common.getCommon().deletetaste(context, Common.getCommon().getFoodlist(),speces, Common.getCommon().getPosition(), position,"规格",test);
					
				}
			}
		});
		return convertView;
	}
	
   public class Holder{
	   TextView foodname;
	   TextView foodprice;
	   CheckBox chioce;
   }
}
