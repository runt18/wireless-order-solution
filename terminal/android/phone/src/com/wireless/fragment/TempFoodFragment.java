package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.R;
import com.wireless.ui.dialog.AskOrderAmountDialog.OnFoodPickedListener;

public class TempFoodFragment extends Fragment {
	
	private OnFoodPickedListener mFoodPickedListener;
	
	private TempFoodAdapter mTempFoodAdapter;
	
	private List<Kitchen> mKitchens = new ArrayList<Kitchen>();

	private boolean isSent;
	
	private static class ViewHolder{
		TextView kitchenTextView;
		EditText foodNameEditText;
		EditText amountEditText;
		EditText priceEdittext;
		ImageButton deleteBtn;
		 
		boolean isInitialized(){
			if(kitchenTextView != null && foodNameEditText!= null && 
					deleteBtn != null && amountEditText!= null && priceEdittext != null)
				return true;
			else return false;
		}
		
		void refresh(OrderFood food){
			if(isInitialized()){
				kitchenTextView.setText(food.getKitchen().getName());
				foodNameEditText.setText(food.getName());
				amountEditText.setText(NumericUtil.float2String2(food.getCount()));
				if(food.getPrice() != 0f)
					priceEdittext.setText(NumericUtil.float2String2(food.getPrice()));
				else priceEdittext.setText("");
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		for(Kitchen kitchen : WirelessOrder.foodMenu.kitchens){
			if(kitchen.isAllowTemp()){
				mKitchens.add(kitchen);
			}
		}
	}
 
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
        	mFoodPickedListener = (OnFoodPickedListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement FoodPickedListener");
        }
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pick_food_by_temp_fgm, null);
		
		final ListView tempFoodView = (ListView) view.findViewById(R.id.listView_tempFood_fgm) ;
		
		mTempFoodAdapter = new TempFoodAdapter();
		tempFoodView.setAdapter(mTempFoodAdapter);		

		//添加按钮
		View addBtn = view.findViewById(R.id.relativeLayout_tempFoodFgm_add);
		addBtn.setBackgroundResource(R.drawable.temp_food_fgm_add_selector);
		addBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mKitchens.isEmpty()){
					mTempFoodAdapter.add();
					//当添加项的view生成后让窗口弹出
					v.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener(){
						@Override
						public void onGlobalLayout() {
							
							View childView = tempFoodView.getChildAt(mTempFoodAdapter.getCount() - 1);
							if(childView != null){
								View view = childView.findViewById(R.id.textView_kitchen_tempFood_item);
								if(view.getHeight() > 0){
									view.performClick();
									view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
								}
							} else {
								tempFoodView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
							}
						}
					});
					
					tempFoodView.smoothScrollToPosition(tempFoodView.getBottom());
				} else {
					Toast.makeText(getActivity(), "没有可添加临时菜的厨房,请先在菜品管理中设置", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		tempFoodView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//隐藏键盘
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(tempFoodView.getWindowToken(), 0);
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		return view;
	}

	@Override
	public void onStop() {
		if(!isSent){
			if(mFoodPickedListener != null)
				for(OrderFood of: getValidTempFood()){
					mFoodPickedListener.onFoodPicked(of);
				}
		}
		super.onStop();
	}

	public List<OrderFood> getValidTempFood(){
		List<OrderFood> validFoods = new ArrayList<OrderFood>();
		for(OrderFood f : mTempFoodAdapter.getFoods()){
			if(f.getName() != null && !f.getName().equals("") && f.getKitchen() != null){
				validFoods.add(f);
			}
		}
		isSent = true;
		return validFoods;
	}
	
	private class TempFoodAdapter extends BaseAdapter{
		private ArrayList<OrderFood> mTempFoods;

		TempFoodAdapter() {
			mTempFoods = new ArrayList<OrderFood>();
		}
		
		void add(){
			OrderFood tmpFood = new OrderFood();
			tmpFood.setTemp(true);
			tmpFood.asFood().setKitchen(new Kitchen());
			tmpFood.setCount(1f);
			mTempFoods.add(tmpFood);
			notifyDataSetChanged();
			((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(TempFoodFragment.this.getView().getWindowToken(), 0);
		}
		
		void remove(int position){
			mTempFoods.remove(position);
			notifyDataSetChanged();
		}
		
		ArrayList<OrderFood> getFoods(){
			return mTempFoods;
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
			//初始化view 和 holder 
			if(view == null){
				view = inflater.inflate(R.layout.pick_food_by_temp_item, null);
				holder = new ViewHolder();

				holder.foodNameEditText = (EditText)view.findViewById(R.id.editText_foodName_tempFood_item);
				holder.kitchenTextView = (TextView) view.findViewById(R.id.textView_kitchen_tempFood_item);
				holder.amountEditText = (EditText) view.findViewById(R.id.editText_tempFoodItem_amount);
				holder.priceEdittext = (EditText) view.findViewById(R.id.editText_price_tempFood_item);
				holder.deleteBtn = (ImageButton) view.findViewById(R.id.imageButton_delete_tempFood_item);
				view.setTag(holder);
				
			}else{
				holder = (ViewHolder) view.getTag();
			}
			
//			默认初始化为第一个部门
			if(food.getKitchen().getName() == null)
				food.asFood().setKitchen(mKitchens.get(0));
			/**
			 * 菜名赋值
			 */
			//设置临时菜名称前删除文本框监听器
			if(holder.foodNameEditText.getTag() != null){
				holder.foodNameEditText.removeTextChangedListener((TextWatcher)holder.foodNameEditText.getTag());
			}
			holder.foodNameEditText.setText(food.getName());
			
			FoodNameWatcher nameWatcher = new FoodNameWatcher();
			nameWatcher.setFood(food, position);
			 
			holder.foodNameEditText.setTag(nameWatcher);
			holder.foodNameEditText.addTextChangedListener(nameWatcher);	
			//数量赋值
			if(holder.amountEditText.getTag() != null)
				holder.amountEditText.removeTextChangedListener((TextWatcher)holder.amountEditText.getTag());
			holder.amountEditText.setText(NumericUtil.float2String2(food.getCount()));
			
			FoodAmountWatcher amountWatcher = new FoodAmountWatcher();
			amountWatcher.setFood(food, position);
			
			holder.amountEditText.setTag(amountWatcher);
			holder.amountEditText.addTextChangedListener(amountWatcher);
			//价格赋值
			if(holder.priceEdittext.getTag() != null)
				holder.priceEdittext.removeTextChangedListener((TextWatcher) holder.priceEdittext.getTag());
			holder.priceEdittext.setText(NumericUtil.float2String2(food.getPrice()));
			
			FoodPriceTextWatcher priceWatcher = new FoodPriceTextWatcher();
			priceWatcher.setFood(food, position);
			
			holder.priceEdittext.setTag(priceWatcher);
			holder.priceEdittext.addTextChangedListener(priceWatcher);
			
			//厨房赋值
			holder.kitchenTextView.setTag(holder);
			holder.kitchenTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View kitchenTextView) {
					if(mKitchens.size() > 1){
						//设置弹出框
						final PopupWindow popWnd = new PopupWindow(
								inflater.inflate(R.layout.pick_food_by_temp_fgm_popup_wnd, null),
								180, LayoutParams.WRAP_CONTENT, true){
	
							@Override
							public void dismiss() {
								if(food.getKitchen() != null && !mKitchens.isEmpty())
									if(food.getKitchen().getName() == null)
									{
										food.asFood().setKitchen(mKitchens.get(0));
										mTempFoods.set(position, food);
										holder.refresh(food);
									}
								super.dismiss();
							}
						};
						popWnd.setOutsideTouchable(true);
						popWnd.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_small));
						popWnd.update();
	
						//弹出框的内容 
						ListView popListView = (ListView) popWnd.getContentView().findViewById(R.id.listView_tempFood_pop);
						popListView.setTag(kitchenTextView);
						popListView.setAdapter(new PopupAdapter(mKitchens));
						popListView.setCacheColorHint(Color.TRANSPARENT);
						popListView.setOnItemClickListener(new OnItemClickListener(){
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int p, long id) {
								TextView kitchenTextView = (TextView) parent.getTag();
								Kitchen kitchen = (Kitchen) view.getTag();
								
								ViewHolder holder  = (ViewHolder)kitchenTextView.getTag();
								food.asFood().setKitchen(kitchen);
								mTempFoods.set(position, food);
								holder.refresh(food);
								popWnd.dismiss();
							}
						});
						//点击显示弹窗，并传递信息
						if(popWnd.isShowing()){
							popWnd.dismiss();
						}else{
							popWnd.showAsDropDown(kitchenTextView);
						}
						
					}else if(mKitchens.size() == 1){
						food.asFood().setKitchen(mKitchens.get(0));
						holder.refresh(food);
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
		
		class FoodNameWatcher implements TextWatcher{
			private OrderFood mFood;
			private int mPosition;
			
//			public OrderFood getFood() {
//				return mFood;
//			}

			public void setFood(OrderFood mFood, int position) {
				this.mFood = mFood;
				mPosition = position;
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(!s.toString().equals("")){
					mFood.asFood().setName(s.toString().replace(",", ";").replace("，", "；").trim());
					mTempFoods.set(mPosition, mFood);
				}
			}
		}
		
		class FoodAmountWatcher implements TextWatcher{
			private OrderFood mFood;
			private int mPosition;
			
			public void setFood(OrderFood mFood, int position) {
				this.mFood = mFood;
				mPosition = position;
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if(!s.toString().equals(""))
				{
					mFood.setCount(Float.valueOf(s.toString().replace(",", ";").replace("，", "；").trim()));
					mTempFoods.set(mPosition, mFood);
				}
			}
		}
		
		class FoodPriceTextWatcher implements TextWatcher{
			private OrderFood mFood;
			private int mPosition;
			
			public void setFood(OrderFood mFood, int position) {
				this.mFood = mFood;
				mPosition = position;
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if(!s.toString().equals(""))
				{
					mFood.asFood().setPrice(Float.valueOf(s.toString()));
					mTempFoods.set(mPosition, mFood);
				}
			}
			
		}
	}
	
	private class PopupAdapter extends BaseAdapter{

		List<Kitchen> mKitchensAllowTemp;
		
		PopupAdapter(List<Kitchen> kitchensAllowTemp){
			mKitchensAllowTemp = kitchensAllowTemp;
		}
		
		@Override
		public int getCount() {
			return mKitchensAllowTemp.size();
		}

		@Override
		public Object getItem(int position) {
			return mKitchensAllowTemp.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;

			if(convertView == null){
				final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.pick_food_by_temp_fgm_pop_list_item, null);
			}else{
				view = convertView;
			}
			
			Kitchen kitchen = mKitchensAllowTemp.get(position);
			
			TextView textView = (TextView) view.findViewById(R.id.textView_tempFood_popList_item_kcName);
			textView.setText(kitchen.getName());
			view.setTag(kitchen);
			
			return view;
		}
	}
	
}
