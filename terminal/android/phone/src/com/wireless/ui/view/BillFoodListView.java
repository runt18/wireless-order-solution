package com.wireless.ui.view;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.util.NumericUtil;
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
				view = LayoutInflater.from(getContext()).inflate(R.layout.bill_activity_food_item, null);
			}else{
				view = convertView;
			}
			OrderFood food = _foods.get(position);
			
			String status = "";
			if(food.asFood().isSpecial()){
				status = "��";
			}
			if(food.asFood().isRecommend()){
				if(status.length() == 0){
					status = "��";
				}else{
					status = status + ",��";
				}
			}
			if(food.asFood().isGift()){
				if(status.length() == 0){
					status = "��";
				}else{
					status = status + ",��";
				}
			}
			if(food.isTemp()){
				if(status.length() == 0){
					status = "��";
				}else{
					status = status + ",��";
				}
			}
			if(status.length() != 0){
				status = "(" + status + ")";
			}
			((TextView)view.findViewById(R.id.txtView_foodName_billItem)).setText(food.toString() + status);
			((TextView)view.findViewById(R.id.txtView_discountValue_billItem)).setText(food.getDiscount() == 1 ? "" : "(" + food.getDiscount() * 10 + "��)");
			((TextView)view.findViewById(R.id.txtView_amountValue_billItem)).setText(Float.toString(_foods.get(position).getCount()));
			((TextView)view.findViewById(R.id.txtView_priceValue_billItem)).setText(NumericUtil.CURRENCY_SIGN + Float.toString(_foods.get(position).calcPrice()));
			((TextView)view.findViewById(R.id.txtView_staffValue_billItem)).setText(_foods.get(position).getWaiter());
			((TextView)view.findViewById(R.id.txtView_dateValue_billItem)).setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(_foods.get(position).getOrderDate()));
			
			return view;
		}
		
	}

}
