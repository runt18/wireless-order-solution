package com.wireless.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wireless.pad.R;
import com.wireless.protocol.Table;

public class TableAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<Table> tables;

	public TableAdapter() {
	}

	public TableAdapter(Context context,  ArrayList<Table> tables) {
		this.context = context;
		this.tables = tables;
	}

	@Override
	public int getCount() {
		return tables.size();
	}

	@Override
	public Object getItem(int position) {
		return tables.get(position);
	}

	@Override
	public long getItemId(int position) {
		return tables.get(position).tableID;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View rowView = convertView;
		ViewCache viewCache;

		if (rowView == null) {
			// 初始化每项的View
			LayoutInflater inflater = LayoutInflater.from(context);
			rowView = inflater.inflate(R.layout.gridviewitem, null);
			viewCache = new ViewCache(rowView);
			rowView.setTag(viewCache);
		} else {
			viewCache = (ViewCache) rowView.getTag();
		}
		
		if(tables.get(position).status == Table.TABLE_BUSY ){
			viewCache.getItem1().setBackgroundResource(R.drawable.av_r39_c15);
			viewCache.getItem4().setBackgroundResource(R.drawable.av_r42_c15);
		}else{
			viewCache.getItem1().setBackgroundResource(R.drawable.av_r40_c8);
			viewCache.getItem4().setBackgroundResource(R.drawable.av_r43_c8);
		}
		viewCache.getItem3().setText("" + tables.get(position).aliasID);
		viewCache.getItem5().setText(tables.get(position).name);
//		viewCache.getItem5().setText("餐台");
		return rowView;
	}

}

class ViewCache {
	View baseView;
	FrameLayout item1;
	TextView item3;
	FrameLayout item4;
	TextView item5;

	public ViewCache(View baseView) {
		this.baseView = baseView;
	}

	public FrameLayout getItem1() {
		if (item1 == null)
			item1 = (FrameLayout) baseView.findViewById(R.id.item1);
		return item1;
	}


	public TextView getItem3() {
		if (item3 == null)
			item3 = (TextView) baseView.findViewById(R.id.item3);
		return item3;
	}

	public FrameLayout getItem4() {
		if (item4 == null)
			item4 = (FrameLayout) baseView.findViewById(R.id.item4);
		return item4;
	}

	public TextView getItem5() {
		if (item5 == null)
			item5 = (TextView) baseView.findViewById(R.id.item5);
		return item5;
	}

}
