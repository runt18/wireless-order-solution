package com.wireless.ui.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.ui.R;


public class TempListView extends ListView {
	
	private List<OrderFood> _tmpFoods = new ArrayList<OrderFood>();
	private BaseAdapter _adapter = new Adapter();
	private ArrayList<Department> mValidDepts;
	
	public TempListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setAdapter(_adapter);		
		
		/*
		 * 将所有菜品进行按厨房编号进行排序
		 */
		Food[] mOriFoods = new Food[WirelessOrder.foodMenu.foods.length];
		System.arraycopy(WirelessOrder.foodMenu.foods, 0, mOriFoods, 0,
				WirelessOrder.foodMenu.foods.length);
		Arrays.sort(mOriFoods, new Comparator<Food>() {
			@Override
			public int compare(Food food1, Food food2) {
				if (food1.kitchen.aliasID > food2.kitchen.aliasID) {
					return 1;
				} else if (food1.kitchen.aliasID < food2.kitchen.aliasID) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		/*
		 * 使用二分查找算法筛选出有菜品的厨房
		 */
		ArrayList<Kitchen> mValidKitchens = new ArrayList<Kitchen>();
		for (int i = 0; i < WirelessOrder.foodMenu.kitchens.length; i++) {
			Food keyFood = new Food();
			keyFood.kitchen.aliasID = WirelessOrder.foodMenu.kitchens[i].aliasID;
			int index = Arrays.binarySearch(mOriFoods, keyFood,
					new Comparator<Food>() {

						public int compare(Food food1, Food food2) {
							if (food1.kitchen.aliasID > food2.kitchen.aliasID) {
								return 1;
							} else if (food1.kitchen.aliasID < food2.kitchen.aliasID) {
								return -1;
							} else {
								return 0;
							}
						}
					});

			if (index >= 0) {
				mValidKitchens.add(WirelessOrder.foodMenu.kitchens[i]);
			}
		}
		/*
		 * 筛选出有菜品的部门
		 */
		mValidDepts = new ArrayList<Department>();
		for (int i = 0; i < WirelessOrder.foodMenu.depts.length; i++) {
			for (int j = 0; j < mValidKitchens.size(); j++) {
				if (WirelessOrder.foodMenu.depts[i].deptID == mValidKitchens.get(j).dept.deptID) {
					mValidDepts.add(WirelessOrder.foodMenu.depts[i]);
					break;
				}
			}
		}
	}

	
	/**
	 *listView的数据源
	 * @return
	 */
	public List<OrderFood> getSourceData(){		
		ArrayList<OrderFood> foods = new ArrayList<OrderFood>(_tmpFoods);
		//filter the temporary foods without food name
		Iterator<OrderFood> iter = foods.iterator();
		while (iter.hasNext()) {
			OrderFood food = iter.next();
			if (food.getName().equals("") || food.getPrice() > 9999) {
				iter.remove();
			}
		}
		return foods;
	}	
	
	/**
	 * 增加一个临时菜
	 */
	public void addTemp(){
		OrderFood tmpFood = new OrderFood();
		tmpFood.isTemporary = true;
		tmpFood.hangStatus = OrderFood.FOOD_NORMAL;
		tmpFood.setPrice(Float.valueOf(10000));
		tmpFood.setCount(Float.valueOf(1));
		tmpFood.kitchen = new Kitchen();
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
			final  LayoutInflater inflater = LayoutInflater.from(getContext());
			if(convertView == null){
				view = inflater.inflate(R.layout.temp_item, null);				
			}else{
				view = convertView;
			}
			TextView kitchenTextView = (TextView)view.findViewById(R.id.textView_occasion_kitchen);

			if(food.kitchen.dept.name != null)
				kitchenTextView.setText(food.kitchen.dept.name);
			kitchenTextView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//设置弹出框
					final PopupWindow mPopWindow = new PopupWindow(inflater.inflate(R.layout.temp_food_fragment_popup_window, null),
							140,LayoutParams.WRAP_CONTENT, true);
					mPopWindow.update();
					mPopWindow.setOutsideTouchable(true);
					mPopWindow.setBackgroundDrawable(new BitmapDrawable());
					//弹出框的内容
					ListView popListView = (ListView) mPopWindow.getContentView();
//					popListView.setTag(v);
					popListView.setAdapter(new PopupAdapter());
					popListView.setOnItemClickListener(new OnItemClickListener(){
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							//TODO 更改类型
//							TextView v = (TextView) parent.getTag();
							Department dept = (Department) view.getTag();
							
							food.kitchen.dept = dept;
							mPopWindow.dismiss();
						}					
					});
				}
			});
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
					food.setName(s.toString().replace(",", ";").replace("，", "；").trim());
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
			foodPriceEdtTxt.setText(food.getPrice() > 9999 ? "" : Util.float2String2(food.getPrice()));
			
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
								food.setPrice(price);
								_tmpFoods.set(position, food);
							}else{
								foodPriceEdtTxt.setText(food.getPrice() > 9999 ? "" : Util.float2String2(food.getPrice()));
								foodPriceEdtTxt.setSelection(foodPriceEdtTxt.getText().length());
								Toast.makeText(getContext(), "临时菜" + 
											   (food.getName().length() == 0 ? (position + 1) : "(" + food.getName() + ")") + 
											   "的价格范围是0～9999", Toast.LENGTH_SHORT).show();
							}
							
						}catch(NumberFormatException e){
							foodPriceEdtTxt.setText(food.getPrice() > 9999 ? "" : Util.float2String2(food.getPrice()));
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
			foodAmountEdtTxt.setText(Util.float2String2(food.getCount()));
			
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
								foodAmountEdtTxt.setText(Util.float2String2(food.getCount()));
								foodAmountEdtTxt.setSelection(foodAmountEdtTxt.getText().length());
								Toast.makeText(getContext(), "临时菜" + 
										   (food.getName().length() == 0 ? (position + 1) : "(" + food.getName() + ")") + 
											  "的数量范围是1～255", Toast.LENGTH_SHORT).show();
							}
						}catch(NumberFormatException e){
							foodAmountEdtTxt.setText(Util.float2String2(food.getCount()));
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
	
	class PopupAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mValidDepts.size();
		}

		@Override
		public Object getItem(int position) {
			return mValidDepts.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;

			if(view == null)
			{
				final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.temp_food_fragment_pop_list_item, null);
			}
			
			Department dept = mValidDepts.get(position);
			TextView textView = (TextView) view;
			textView.setText(dept.name);
			textView.setTag(dept);
			
			return view;
		}
		
	}
}
