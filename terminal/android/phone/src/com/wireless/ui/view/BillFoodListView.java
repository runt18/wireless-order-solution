package com.wireless.ui.view;

import java.text.SimpleDateFormat;
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
import com.wireless.protocol.Util;
import com.wireless.ui.R;

public class BillFoodListView extends ListView {
	
    private List<OrderFood> _foods;
    private BaseAdapter _adapter = null;
    
    
	public BillFoodListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	
	/*
	 *����ListView����
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
			return _foods.size();
		}

		@Override
		public Object getItem(int position) {
			return _foods.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if(convertView == null){
				view =LayoutInflater.from(getContext()).inflate(R.layout.billitem, null);
			}else{
				view = convertView;
			}
			
			((TextView)view.findViewById(R.id.foodName)).setText(_foods.get(position).toString());
			((TextView)view.findViewById(R.id.accountValue)).setText(Float.toString(_foods.get(position).getCount()));
			((TextView)view.findViewById(R.id.priceValue)).setText(Util.CURRENCY_SIGN + Float.toString(_foods.get(position).calcPriceWithTaste()));
			((TextView)view.findViewById(R.id.operatorValue)).setText(_foods.get(position).waiter);
			((TextView)view.findViewById(R.id.orderDate)).setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(_foods.get(position).orderDate));
			
			return view;
		}
		
	}

}
