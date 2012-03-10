package com.wireless.ui.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
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
	public List<OrderFood> getSourceData(){		
		return new ArrayList<OrderFood>(_tmpFoods);
	}	
	
	/**
	 * 增加一个临时菜
	 */
	public void addTemp(){
		OrderFood tmpFood = new OrderFood();
		tmpFood.isTemporary = true;
		tmpFood.foodAlias = Util.genTempFoodID();
		tmpFood.hangStatus = OrderFood.FOOD_NORMAL;
		tmpFood.setCount(new Float(1));
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
			foodNameEdtTxt.setOnFocusChangeListener(new OnFocusChangeListener() {				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(!hasFocus){
						if(_tmpFoods.indexOf(food) == position){
							String name = foodNameEdtTxt.getEditableText().toString().trim();
							if(name.equals("")){
								Toast.makeText(_context, "请输入临时菜" + (position + 1) + "的名称", 0).show();
							}else{
								food.name = foodNameEdtTxt.getEditableText().toString().replace(",", ";").replace("，", "；").trim();
								_tmpFoods.set(position, food);
							}
						}
					}
				}
			});
			
			/**
			 * 价钱赋值
			 */
			final EditText foodPriceEdtTxt = (EditText)view.findViewById(R.id.occasiprice);
			foodPriceEdtTxt.setText(Util.float2String2(food.getPrice()));
			foodPriceEdtTxt.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(!hasFocus){
						if(_tmpFoods.indexOf(food) == position){
							try{
								Float price = Float.parseFloat(foodPriceEdtTxt.getEditableText().toString());
								if(price >= 0 && price < 9999){
									food.setPrice(price);
									_tmpFoods.set(position, food);
								}else{
									foodPriceEdtTxt.setText(Util.float2String(food.getPrice()));
									Toast.makeText(_context, "临时菜" + (position + 1) + "的价格范围是0～9999", 0).show();
								}
							}catch(NumberFormatException e){
								Toast.makeText(_context, "您输入临时菜" + (position + 1) + "的价钱格式不正确，请重新输入", 0).show();
							}
						}
					}					
				}
			});
			
			
			/**
			 * 数量赋值
			 */
			final EditText foodAmountEdtTxt = (EditText)view.findViewById(R.id.occasicount);
			foodAmountEdtTxt.setText(Util.float2String2(food.getCount()));
			foodAmountEdtTxt.setOnFocusChangeListener(new OnFocusChangeListener() {				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(!hasFocus){
						if(_tmpFoods.indexOf(food) == position){
							try{
								Float amount = Float.parseFloat(foodAmountEdtTxt.getEditableText().toString());
								if(amount > 0 && amount <= 255){
									food.setCount(amount);
									_tmpFoods.set(position, food);
								}else{
									foodAmountEdtTxt.setText(Util.float2String2(food.getCount()));
									Toast.makeText(_context, "临时菜" + (position + 1) + "的数量范围是1～255", 0).show();
								}
							}catch(NumberFormatException e){
								Toast.makeText(_context, "您输入临时菜" + (position + 1) + "的数量格式不正确，请重新输入", 0).show();
							}
							
						}
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
