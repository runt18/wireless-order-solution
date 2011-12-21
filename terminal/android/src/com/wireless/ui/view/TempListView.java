package com.wireless.ui.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.ui.R;


public class TempListView extends ListView {
	private Context _context;
	private OrderFood[] _orderfoods = null;
	private BaseAdapter _adapter = null;
	public TempListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this._context = context;
	}

	
	/**
	 *listView的数据源
	 * @return
	 */
	public OrderFood[] getSourceData(){
		
		return _orderfoods;
	}
	
	
	/**
	 * 
	 * @param foods
	 */
	public void notifyDataChanged(OrderFood[] foods){
		_orderfoods = foods;
		if(_adapter != null){
			_adapter.notifyDataSetChanged();
		}else{
			setAdapter(new Adapter());
		}
	}	
	
	
	private class Adapter extends BaseAdapter{

		@Override
		public int getCount() {
			return _orderfoods.length;
		}

		@Override
		public Object getItem(int position) {
			return _orderfoods[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view;
			if(convertView == null){
				view = LayoutInflater.from(_context).inflate(R.layout.temp_item, null);
			}else{
				view = convertView;
			}
			((TextView)view.findViewById(R.id.occasin)).setText("临时菜"+position);
			
			/**
			 * 菜名赋值
			 */
			final EditText foodname = (EditText)view.findViewById(R.id.occasiname);
			foodname .setText(_orderfoods[position].name);
			foodname.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before,
						int count) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					if(!s.equals("")){
						_orderfoods[position].name = s.toString();
					}
					
				}
			});

			
			/**
			 * 价钱赋值
			 */
			final EditText foodprice = (EditText)view.findViewById(R.id.occasiprice);
			if(!foodprice.getText().toString().equals("")){
				foodprice.setText(String.valueOf(_orderfoods[position].getPrice()));
			}
			foodprice.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before,
						int count) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					if(!s.equals("")){
						try {
							_orderfoods[position].setPrice(Float.parseFloat(s.toString()));
							Log.e("dsd", _orderfoods[position].getPrice()+"");
						} catch (Exception e) {
							Toast.makeText(_context, "请输入数字", 0).show();
						}
						
					}
					
				}
			});
			
			
			/**
			 * 数量赋值
			 */
			final EditText foodcount = (EditText)view.findViewById(R.id.occasicount);
			if(!foodcount.getText().toString().equals("")){
				foodcount.setText(String.valueOf(_orderfoods[position].getCount()));
			}	
			foodcount.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before,
						int count) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					if(!s.equals("")){
						try {
							_orderfoods[position].setCount(Float.parseFloat(s.toString()));
							Log.e("qqqqq", _orderfoods[position].getCount()+"");
						} catch (Exception e) {
							Toast.makeText(_context, "请输入数字", 0).show();
						}
						
					}
					
				}
			});
		    
			/**
			 * 其他属性赋值
			 */
			_orderfoods[position].isTemporary = true;
			_orderfoods[position].alias_id = Util.genTempFoodID();
			_orderfoods[position].hangStatus = OrderFood.FOOD_HANG_UP;
			
			
			/**
			 * 点击删除菜按钮
			 */
			ImageView removefood = (ImageView)view.findViewById(R.id.remove);
			removefood.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					List<OrderFood> orderlist = new ArrayList<OrderFood>(Arrays.asList(_orderfoods));
					
					orderlist.remove(orderlist.get(position));
				
					notifyDataChanged(orderlist.toArray(new OrderFood[orderlist.size()]));
				}
			});
			return view;
		}
		
		
		
	}
}
