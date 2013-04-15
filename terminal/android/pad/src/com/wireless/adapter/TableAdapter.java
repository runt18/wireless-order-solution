package com.wireless.adapter;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wireless.pad.R;
import com.wireless.protocol.PTable;

public class TableAdapter extends BaseAdapter {

	private ArrayList<PTable> _tables;
	private OnTableClickListener _tableClick;
	
	public TableAdapter() {
	}

	public TableAdapter(ArrayList<PTable> tables, OnTableClickListener tableClick) {
		this._tableClick = tableClick;
		this._tables = tables;
	}

	@Override
	public int getCount() {
		return _tables.size();
	}

	@Override
	public Object getItem(int position) {
		return _tables.get(position);
	}

	@Override
	public long getItemId(int position) {
		return _tables.get(position).getTableId();
	}


	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		
		View view;
		
		if(convertView == null) {
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gridviewitem, null);
		} else {
			view = convertView;
		}
		
		final PTable table = _tables.get(position);
		
		//根据餐台的不同状态设置背景
		if(table.isBusy()){
			((FrameLayout)view.findViewById(R.id.item1)).setBackgroundResource(R.drawable.av_r39_c15);
			((FrameLayout)view.findViewById(R.id.item4)).setBackgroundResource(R.drawable.av_r42_c15);
		}else{
			((FrameLayout)view.findViewById(R.id.item1)).setBackgroundResource(R.drawable.av_r40_c8);
			((FrameLayout)view.findViewById(R.id.item4)).setBackgroundResource(R.drawable.av_r43_c8);
		}
		//设置餐台台号
		((TextView)view.findViewById(R.id.item3)).setText(Integer.toString(table.getAliasId()));
		//设置餐台名称
		((TextView)view.findViewById(R.id.item5)).setText(table.getName());
		
		view.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//TODO jump to order activity
				if(_tableClick != null){
					_tableClick.onClick(table);
				}
			}
		});
		
		view.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				if (table.isBusy()) {
					new AlertDialog.Builder(parent.getContext())
						.setTitle("请选择" + table.getAliasId() + "号餐台的操作")
						.setItems(new String[] { "改单", "转台" }, null)
						.setNegativeButton("返回", null)
						.show();
					
				} else if (table.isIdle()) {
					new AlertDialog.Builder(parent.getContext())
						.setTitle("请选择" + table.getAliasId() + "号餐台的操作")
						.setItems(new String[] { "下单" }, null)
						.setNegativeButton("返回", null)
						.show();
				}
				return true;
			}
		});
		
		return view;
	}

	public static interface OnTableClickListener{
		public void onClick(PTable table);
	}
	
}


