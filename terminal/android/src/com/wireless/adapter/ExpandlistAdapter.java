package com.wireless.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;


import com.wireless.protocol.Kitchen;
import com.wireless.protocol.SKitchen;
import com.wireless.ui.R;

public class ExpandlistAdapter extends BaseExpandableListAdapter {
	private Context context;
	private List<SKitchen> perant;
	private List<List<Kitchen>>  child;
	
	public ExpandlistAdapter(Context context,List<SKitchen> perant, List <List<Kitchen>> child){
		this.context=context;
		this.perant=perant;
		this.child=child;
		
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return perant.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return child.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return perant.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return child.get(groupPosition).get(childPosition);
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
		View view=convertView.inflate(context, R.layout.grounp, null);
		((TextView) view.findViewById(R.id.mygroup)).setText(perant.get(groupPosition).name);
		return view;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view=convertView.inflate(context, R.layout.child, null);
		((TextView) view.findViewById(R.id.mychild)).setText(child.get(groupPosition).get(childPosition).name);
		return view;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

}
