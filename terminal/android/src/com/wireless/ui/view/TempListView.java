package com.wireless.ui.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
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
	private List<OrderFood> _tmpFoods = new ArrayList<OrderFood>();
	private BaseAdapter _adapter = new Adapter();
	
	public TempListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this._context = context;
		setAdapter(_adapter);		
	}

	
	/**
	 *listView的数据源
	 * @return
	 */
	public OrderFood[] getSourceData(){		
		return _tmpFoods.toArray(new OrderFood[_tmpFoods.size()]);
	}	
	
	public void addTemp(){
		OrderFood tmpFood = new OrderFood();
		tmpFood.isTemporary = true;
		tmpFood.alias_id = Util.genTempFoodID();
		tmpFood.hangStatus = OrderFood.FOOD_NORMAL;
		_tmpFoods.add(tmpFood);
		_adapter.notifyDataSetChanged();
	}
	
	/**
	 * 
	 * @param foods
	 */
	public void notifyDataChanged(){
		_adapter.notifyDataSetChanged();
	}	
	
	
	private class Adapter extends BaseAdapter{

		@Override
		public int getCount() {
			return _tmpFoods.size();
		}

		@Override
		public Object getItem(int position) {
			return _tmpFoods.get(position);
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
			((TextView)view.findViewById(R.id.occasin)).setText("临时菜" + (position + 1));
			
			final OrderFood food = _tmpFoods.get(position);
			
			/**
			 * 菜名赋值
			 */
			final EditText foodNameEdtTxt = (EditText)view.findViewById(R.id.occasiname);
			foodNameEdtTxt.setText(food.name);
			foodNameEdtTxt.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,	int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
//					if(!s.toString().equals(food.name)){
//						food.name = s.toString();
//						_tmpFoods.set(position, food);						
//					}
				}
			});

			
			/**
			 * 价钱赋值
			 */
			final EditText foodPriceEdtTxt = (EditText)view.findViewById(R.id.occasiprice);
			if(!foodPriceEdtTxt.getText().toString().equals("")){
				foodPriceEdtTxt.setText(String.valueOf(food.getPrice()));
			}
			foodPriceEdtTxt.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,	int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					try{
						food.setPrice(Float.parseFloat(s.toString()));
						_tmpFoods.set(position, food);
					}catch(NumberFormatException e) {
						Toast.makeText(_context, "请输入数字", 0).show();
					}						
				}
			});
			
			
			/**
			 * 数量赋值
			 */
			final EditText foodCountEdtTxt = (EditText)view.findViewById(R.id.occasicount);
			if(!foodCountEdtTxt.getText().toString().equals("")){
				foodCountEdtTxt.setText(String.valueOf(food.getCount()));
			}	
			foodCountEdtTxt.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {	}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {	}

				@Override
				public void afterTextChanged(Editable s) {
					try {
						food.setCount(Float.parseFloat(s.toString()));
						_tmpFoods.set(position, food);
					} catch(NumberFormatException e) {
						Toast.makeText(_context, "请输入数字", 0).show();
					}						
				}
			});			
			
			/**
			 * 点击删除菜按钮
			 */
			ImageView removefood = (ImageView)view.findViewById(R.id.remove);
			removefood.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {				
					_tmpFoods.remove(position);					
					_adapter.notifyDataSetChanged();
				}
			});
			return view;
		}		
		
	}
}
