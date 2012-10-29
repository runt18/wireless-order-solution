package com.wireless.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.ui.R;

public class TempFoodFragment extends Fragment {
	private ArrayList<Department> mValidDepts;
	private TempFoodAdapter mTempFoodAdapter;
	
	private static class ViewHolder{
		TextView kitchenTextView;
		EditText foodNameEditText;
		ImageButton deleteBtn;
		
		boolean isInitialized(){
			if(kitchenTextView != null && foodNameEditText!= null && deleteBtn != null)
				return true;
			else return false;
		}
		
		void refresh(OrderFood food){
			if(isInitialized())
			{
				kitchenTextView.setText(food.kitchen.dept.name);
				foodNameEditText.setText(food.name);
			}
		}
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.temp_food_fragment, null);
		
		final ListView tempFoodView = (ListView) view.findViewById(R.id.listView_tempFood_fgm) ;
		
		mTempFoodAdapter = new TempFoodAdapter();
		tempFoodView.setAdapter(mTempFoodAdapter);

		//添加按钮
		((ImageView) view.findViewById(R.id.add)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mTempFoodAdapter.add();
				//当添加项的view生成后让窗口弹出
				v.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener(){
					@Override
					public void onGlobalLayout() {
						View view = tempFoodView.getChildAt(mTempFoodAdapter.getCount()-1).findViewById(R.id.textView_kitchen_tempFood_item);
						if(view.getHeight() > 0)
						{
							view.performClick();
							view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						}
					}
				});
			}
		});
		return view;
	}

	class TempFoodAdapter extends BaseAdapter{
		private ArrayList<OrderFood> mTempFoods;

		TempFoodAdapter() {
			mTempFoods = new ArrayList<OrderFood>();
		}
		
		void add(){
			OrderFood tmpFood = new OrderFood();
			tmpFood.isTemporary = true;
			tmpFood.aliasID = Util.genTempFoodID();
			tmpFood.hangStatus = OrderFood.FOOD_NORMAL;
			tmpFood.kitchen = new Kitchen();
			mTempFoods.add(tmpFood);
			notifyDataSetChanged();
			((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(TempFoodFragment.this.getView().getWindowToken(), 0);
		}
		
		void remove(int position)
		{
			mTempFoods.remove(position);
			this.notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return mTempFoods.size();
		}

		@Override
		public Object getItem(int position) {
			return mTempFoods.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final OrderFood food = mTempFoods.get(position);

			final ViewHolder holder;
			final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if(view == null)
			{
				view = inflater.inflate(R.layout.temp_food_item, null);
				holder = new ViewHolder();

				holder.foodNameEditText = (EditText)view.findViewById(R.id.editText_foodName_tempFood_item);
				holder.kitchenTextView = (TextView) view.findViewById(R.id.textView_kitchen_tempFood_item);
				holder.deleteBtn = (ImageButton) view.findViewById(R.id.imageButton_delete_tempFood_item);
				view.setTag(holder);
			}
			else holder = (ViewHolder) view.getTag();
			//默认初始化为第一个部门
			if(food.kitchen.dept.name == null)
				food.kitchen.dept = mValidDepts.get(0);
			/**
			 * 菜名赋值
			 */
			//设置临时菜名称前删除文本框监听器
			if(holder.foodNameEditText.getTag() != null){
				holder.foodNameEditText.removeTextChangedListener((TextWatcher)holder.foodNameEditText.getTag());
			}
			holder.foodNameEditText.setText(food.name);
			TextWatcher textWatcher = new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					food.name = s.toString().replace(",", ";").replace("，", "；").trim();
					mTempFoods.set(position, food);
				}
			};
			
			holder.foodNameEditText.setTag(textWatcher);
			holder.foodNameEditText.addTextChangedListener(textWatcher);		
			
			holder.kitchenTextView.setTag(holder);
			holder.kitchenTextView.setOnClickListener(new View.OnClickListener() {
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
					popListView.setTag(v);
					popListView.setAdapter(new PopupAdapter());
					popListView.setOnItemClickListener(new OnItemClickListener(){
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int p, long id) {
							TextView v = (TextView) parent.getTag();
							Department dept = (Department) view.getTag();
							
							ViewHolder holder  = (ViewHolder)v.getTag();
							food.kitchen.dept = dept;
							mTempFoods.set(position, food);
							holder.refresh(food);
							mPopWindow.dismiss();
						}
					});
					//点击显示弹窗，并传递信息
					if(mPopWindow.isShowing())
						mPopWindow.dismiss();
					else{
						mPopWindow.showAsDropDown(v);
					}
				}
			});
			
			//删除按钮
			holder.deleteBtn.setTag(position);
			holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = (Integer) v.getTag();
					mTempFoodAdapter.remove(position);
					((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(TempFoodFragment.this.getView().getWindowToken(), 0);

				}
			});
				
			//刷新holder的显示
			holder.refresh(food);
			
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
				final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
