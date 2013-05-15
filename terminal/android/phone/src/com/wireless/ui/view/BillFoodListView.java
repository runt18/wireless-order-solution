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

import com.wireless.pojo.util.NumericUtil;
import com.wireless.protocol.OrderFood;
import com.wireless.ui.R;

public class BillFoodListView extends ListView {
	
    private List<OrderFood> _foods;
    private BaseAdapter _adapter = null;
    
    
	public BillFoodListView(Context context, AttributeSet attrs) {
		super(context, attrs);
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
				view = LayoutInflater.from(getContext()).inflate(R.layout.billitem, null);
			}else{
				view = convertView;
			}
			OrderFood food = _foods.get(position);
			
			String status = "";
			if(food.asFood().isSpecial()){
				status = "特";
			}
			if(food.asFood().isRecommend()){
				if(status.length() == 0){
					status = "荐";
				}else{
					status = status + ",荐";
				}
			}
			if(food.asFood().isGift()){
				if(status.length() == 0){
					status = "赠";
				}else{
					status = status + ",赠";
				}
			}
			if(food.isTemp()){
				if(status.length() == 0){
					status = "临";
				}else{
					status = status + ",临";
				}
			}
			if(status.length() != 0){
				status = "(" + status + ")";
			}
			((TextView)view.findViewById(R.id.foodName)).setText(food.toString() + status);
			((TextView)view.findViewById(R.id.discountValue)).setText(food.getDiscount() == 1 ? "" : "(" + food.getDiscount() * 10 + "折)");
			((TextView)view.findViewById(R.id.accountValue)).setText(Float.toString(_foods.get(position).getCount()));
			((TextView)view.findViewById(R.id.priceValue)).setText(NumericUtil.CURRENCY_SIGN + Float.toString(_foods.get(position).calcPriceWithTaste()));
			((TextView)view.findViewById(R.id.operatorValue)).setText(_foods.get(position).getWaiter());
			((TextView)view.findViewById(R.id.orderDate)).setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(_foods.get(position).getOrderDate()));
			
			return view;
		}
		
	}

}
