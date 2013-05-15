package com.wireless.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.pad.R;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.protocol.OrderFood;


public class TempListView extends ListView {
	
	private List<OrderFood> _tmpFoods = new ArrayList<OrderFood>();
	private BaseAdapter _adapter = new Adapter();
	
	public TempListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setAdapter(_adapter);		
	}

	
	/**
	 * 从临时菜列表中删除并返回有效的临时菜品(包含菜品名和价钱)
	 * @return
	 * 		包含有效的临时菜品信息
	 */
	public List<OrderFood> removeValidFoods(){
		ArrayList<OrderFood> validFoods = new ArrayList<OrderFood>();
		//filter the temporary foods with food name and price
		Iterator<OrderFood> iter = _tmpFoods.iterator();
		while (iter.hasNext()) {
			OrderFood food = iter.next();
			if(!(food.getName().equals("") || food.getPrice() > 9999)) {
				validFoods.add(food);
				iter.remove();
			}
		}
		_adapter.notifyDataSetChanged();
		return validFoods;
	}
	
	/**
	 * 增加一个临时菜
	 */
	public void addTemp(){
		OrderFood tmpFood = new OrderFood();
		tmpFood.setTemp(true);
		tmpFood.asFood().setAliasId((int)(System.currentTimeMillis() % 65535));
		tmpFood.asFood().setPrice(Float.valueOf(10000));
		tmpFood.setCount(Float.valueOf(1));
		_tmpFoods.add(tmpFood);
		_adapter.notifyDataSetChanged();
		//隐藏软键盘
		((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindowToken(), 0);
		//滚动到最后一项
		post( new Runnable() {     
			@Override
			public void run() { 
				smoothScrollToPosition(getCount());
			}
		});
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
			
			final OrderFood food = _tmpFoods.get(position);
			
			View view;			
			
			if(convertView == null){
				view = LayoutInflater.from(getContext()).inflate(R.layout.temp_item, null);				
			}else{
				view = convertView;
			}
			
			((TextView)view.findViewById(R.id.occasin)).setText("临时菜" + (position + 1));			
			
			/**
			 * 菜名赋值
			 */
			EditText foodNameEdtTxt = (EditText)view.findViewById(R.id.occasiname);
			//设置临时菜名称前删除文本框监听器
			if(foodNameEdtTxt.getTag() != null){
				foodNameEdtTxt.removeTextChangedListener((TextWatcher)foodNameEdtTxt.getTag());
			}
			foodNameEdtTxt.setText(food.getName());
			TextWatcher textWatcher = new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
					
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					food.asFood().setName(s.toString().replace(",", ";").replace("，", "；").trim());
					_tmpFoods.set(position, food);
				}
			};
			
			foodNameEdtTxt.setTag(textWatcher);
			foodNameEdtTxt.addTextChangedListener(textWatcher);		

			
			/**
			 * 价钱赋值
			 */
			final EditText foodPriceEdtTxt = (EditText)view.findViewById(R.id.occasiprice);
			//设置临时菜价钱前删除文本框监听器
			if(foodPriceEdtTxt.getTag() != null){
				foodPriceEdtTxt.removeTextChangedListener((TextWatcher)foodPriceEdtTxt.getTag());
			}
			foodPriceEdtTxt.setText(food.getPrice() > 9999 ? "" : NumericUtil.float2String2(food.getPrice()));
			
			textWatcher = new TextWatcher() {				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					
				}				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
					
				}				
				@Override
				public void afterTextChanged(Editable s) {
					if(s.toString().length() != 0){
						try{
							Float price = Float.parseFloat(s.toString());
							if(price >= 0 && price < 9999){
								food.asFood().setPrice(price);
								_tmpFoods.set(position, food);
							}else{
								foodPriceEdtTxt.setText(food.getPrice() > 9999 ? "" : NumericUtil.float2String2(food.getPrice()));
								foodPriceEdtTxt.setSelection(foodPriceEdtTxt.getText().length());
								Toast.makeText(getContext(), "临时菜" + 
											   (food.getName().length() == 0 ? (position + 1) : "(" + food.getName() + ")") + 
											   "的价格范围是0～9999", Toast.LENGTH_SHORT).show();
							}
							
						}catch(NumberFormatException e){
							foodPriceEdtTxt.setText(food.getPrice() > 9999 ? "" : NumericUtil.float2String2(food.getPrice()));
							foodPriceEdtTxt.setSelection(foodPriceEdtTxt.getText().length());
							Toast.makeText(getContext(), "您输入临时菜" + 
										  (food.getName().length() == 0 ? (position + 1) : "(" + food.getName() + ")") + 
										  "的价钱格式不正确，请重新输入", Toast.LENGTH_SHORT).show();
						}						
					}
				}
			};
			
			foodPriceEdtTxt.setTag(textWatcher);
			foodPriceEdtTxt.addTextChangedListener(textWatcher);			
			
			/**
			 * 数量赋值
			 */
			final EditText foodAmountEdtTxt = (EditText)view.findViewById(R.id.occasicount);
			//设置临时菜数量前删除文本框监听器
			if(foodAmountEdtTxt.getTag() != null){
				foodAmountEdtTxt.removeTextChangedListener((TextWatcher)foodAmountEdtTxt.getTag());
			}
			foodAmountEdtTxt.setText(NumericUtil.float2String2(food.getCount()));
			
			textWatcher = new TextWatcher() {				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					
				}				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
					
				}				
				@Override
				public void afterTextChanged(Editable s) {
					if(s.toString().length() != 0){
						try{
							Float amount = Float.parseFloat(s.toString());
							if(amount > 0 && amount <= 255){
								food.setCount(amount);
								_tmpFoods.set(position, food);
							}else{
								foodAmountEdtTxt.setText(NumericUtil.float2String2(food.getCount()));
								foodAmountEdtTxt.setSelection(foodAmountEdtTxt.getText().length());
								Toast.makeText(getContext(), "临时菜" + 
										   (food.getName().length() == 0 ? (position + 1) : "(" + food.getName() + ")") + 
											  "的数量范围是1～255", Toast.LENGTH_SHORT).show();
							}
						}catch(NumberFormatException e){
							foodAmountEdtTxt.setText(NumericUtil.float2String2(food.getCount()));
							foodAmountEdtTxt.setSelection(foodAmountEdtTxt.getText().length());
							Toast.makeText(getContext(), "您输入临时菜" + 
										  (food.getName().length() == 0 ? (position + 1) : "(" + food.getName() + ")") + 
										  "的数量格式不正确，请重新输入", Toast.LENGTH_SHORT).show();
						}						
					}
				}
			};
	       
			foodAmountEdtTxt.setTag(textWatcher);
			foodAmountEdtTxt.addTextChangedListener(textWatcher);	
	        
			
			/**
			 * 点击删除菜按钮
			 */
			ImageView removefood = (ImageView)view.findViewById(R.id.remove);
			removefood.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {		
					_tmpFoods.remove(position);
					_adapter.notifyDataSetChanged();
					//隐藏软键盘
					((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindowToken(), 0);
				}
			});
			return view;
		}		
		
	}
}
