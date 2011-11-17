package com.wireless.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wireless.common.Common;
import com.wireless.common.WirelessOrder;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.ui.DropActivity;
import com.wireless.ui.R;

public class DropAdapter extends BaseExpandableListAdapter  {
  private Context context;
  private List<String> perant;
  private List<List<OrderFood>> childs;
  DropActivity drop;
	 public DropAdapter(Context context,List<String> list,List<List<OrderFood>> alist){
		 this.context=context;
		 this.perant=list;
		 this.childs=alist;
		 drop=(DropActivity)context;
	 }
	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return perant.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return childs.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return perant.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childs.get(groupPosition).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view=convertView.inflate(context, R.layout.dropgrounpitem, null);
		((TextView) view.findViewById(R.id.grounname)).setText(perant.get(groupPosition));
		if(groupPosition==1){
			ImageView orderimage=(ImageView) view.findViewById(R.id.orderimage);
			orderimage.setBackgroundResource(R.drawable.commit);
			
			orderimage.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					drop.orderfood();
				}
			});
		}
		return view;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view=convertView.inflate(context, R.layout.dropchilditem, null);
		OrderFood food=childs.get(groupPosition).get(childPosition);
		((TextView) view.findViewById(R.id.foodname)).setText(food.name);
		((TextView) view.findViewById(R.id.accountvalue)).setText(String.valueOf(food.getCount()));
		((TextView) view.findViewById(R.id.pricevalue)).setText(String.valueOf(food.calcPrice2()));
		TextView taste=(TextView)view.findViewById(R.id.taste);
		if(food.tastePref.equals("�޿�ζ")){
			taste.setText("");
		}else{
			taste.setText(food.tastePref);
		}
		ImageView deletefood=(ImageView)view.findViewById(R.id.deletefood);
		ImageView addtaste=(ImageView)view.findViewById(R.id.addtaste);
		if(groupPosition==0){
			deletefood.setBackgroundResource(R.drawable.commit);
			deletefood.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(WirelessOrder.restaurant.pwd3.length()==0){
						Common.getCommon().dropnopawrFoods(context, childs, groupPosition,childPosition);
					}else{
						Common.getCommon().dropFoods(context, childs, groupPosition,childPosition);
					}
					
				}
			});
			
		}else{
			 deletefood.setBackgroundResource(R.drawable.commit);
             deletefood.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
				Common.getCommon().setPosition(childPosition);	
				Common.getCommon().getdeleteFoods(context, childs.get(groupPosition),Common.getCommon().getPosition(), 1);
				}
			});
			addtaste.setBackgroundResource(R.drawable.commit);
			addtaste.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						drop.Foodfunction(childPosition);
						
					}
				});
		}
		return view;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}
	
}
