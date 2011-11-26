package com.wireless.ui.view;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wireless.protocol.OrderFood;
import com.wireless.ui.R;

public class BillFoodListView extends ListView {
	
    private Context _context;
    private List<OrderFood> _foods;
    private BaseAdapter _adapter = null;
    
    
	public BillFoodListView(Context context,AttributeSet attrs) {
		super(context,attrs);
		// TODO Auto-generated constructor stub
		this._context = context;
	}
	
	
	/*
	 *更新ListView数据
	 * 
	 */
	public void notifyDataChanged(List<OrderFood> foods){
		this._foods = foods;
		if(_adapter != null){
			_adapter.notifyDataSetChanged();
		}else{
			setAdapter(new Adapter());
		}
	}
	
	private class Adapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return _foods.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return _foods.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view;
			if(convertView == null){
				view =LayoutInflater.from(_context).inflate(R.layout.billitem,null);
			}else{
				view = convertView;
			}
			
			((TextView)view.findViewById(R.id.foodname)).setText(_foods.get(position).name);
			((TextView)view.findViewById(R.id.accountvalue)).setText(Float.toString(_foods.get(position).getCount()));
			((TextView)view.findViewById(R.id.pricevalue)).setText(Float.toString(_foods.get(position).calcPrice2()));
			((TextView)view.findViewById(R.id.taste)).setText(_foods.get(position).tastePref);
			
			return view;
		}
		
	}

}
