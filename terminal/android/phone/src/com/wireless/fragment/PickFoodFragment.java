package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.ui.R;

public class PickFoodFragment extends Fragment{
	private static final int REFRESH_FOODS = 43552;
	
	public static final String PICK_FOOD_FRAGMENT_TAG_NAME = "pickFoodFragmentTagName";
	public static final String PICK_FOOD_FRAGMENT_TAG = "pickFoodFragmentTag";
	public static final int PICK_FOOD_FRAGMENT_NUMBER = 87514;
	public static final int PICK_FOOD_FRAGMENT_SPELL = 87515;

	private FoodAdapter mAdapter;
	private FoodHandler mHandler ;
	private GridView mGridView;

	private String mFilterCond = "";
	
	private OnFoodPickedListener mFoodPickedListener;

	public static interface OnFoodPickedListener{
		/**
		 * 当PickFoodListView选中菜品后，回调此函数通知Activity选中的Food信息
		 * @param food 选中Food的信息
		 */
		public void onPicked(OrderFood food);
		
		/**
		 * 当PickFoodListView选中菜品后，回调此函数通知Activity选中的Food信息，并跳转到口味Activity
		 * @param food
		 * 			选中Food的信息
		 */
		public void onPickedWithTaste(OrderFood food);
	}
	/**
	 * 设置点完某个菜品后的回调函数
	 * @param foodPickedListener
	 */
	public void setFoodPickedListener(OnFoodPickedListener foodPickedListener){
		mFoodPickedListener = foodPickedListener;
	}
	
	private static class FoodHandler extends Handler{
		private WeakReference<PickFoodFragment> mFragment;
		private List<Food> mSrcFoods;

		FoodHandler(PickFoodFragment fragment) {
			this.mFragment = new WeakReference<PickFoodFragment>(fragment);
			
			mSrcFoods = Arrays.asList(WirelessOrder.foodMenu.foods);
		}
		
		@Override
		public void handleMessage(Message msg){
			PickFoodFragment fragment = mFragment.get();
			//将所有菜品进行条件筛选后存入adapter
			
			List<Food> tmpFoods;
			if(fragment.mFilterCond.length() != 0){
				tmpFoods = new ArrayList<Food>(mSrcFoods);
				Iterator<Food> iter = tmpFoods.iterator();
				while(iter.hasNext()){
					Food f = iter.next();
					if(!(String.valueOf(f.aliasID).startsWith(fragment.mFilterCond) || f.name.contains(fragment.mFilterCond))){
						iter.remove();
					}
				}				
			}else{
				tmpFoods = mSrcFoods;
			}
			
			fragment.mAdapter = fragment.new FoodAdapter(tmpFoods);
			fragment.mGridView.setAdapter(fragment.mAdapter);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mHandler = new FoodHandler(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pick_food_fragment, container, false);
		Bundle args = getArguments();
		((TextView) view.findViewById(R.id.textView_searchTitle_numberFragment)).setText(args.get(PICK_FOOD_FRAGMENT_TAG_NAME).toString());
		
        mGridView = (GridView) view.findViewById(R.id.gridView_numberFragment);
        //设置点菜侦听
        mGridView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Food food = (Food) view.getTag();
				if(!food.isSellOut()){
					new AskOrderAmountDialog(food).show();
				}
			}
        });
        
		//搜索框
        final EditText searchTxtView = (EditText)view.findViewById(R.id.editText_pickFoodFragment);
        //设置输入类型
        if(args.getInt(PICK_FOOD_FRAGMENT_TAG) == PICK_FOOD_FRAGMENT_NUMBER)
        	searchTxtView.setInputType(InputType.TYPE_CLASS_NUMBER);
        else searchTxtView.setInputType(InputType.TYPE_CLASS_TEXT);
        
        searchTxtView.addTextChangedListener(new TextWatcher(){
			@Override 
			public void afterTextChanged(Editable s) {}
			
			@Override
			public void beforeTextChanged(CharSequence s,int start, int count, int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mFilterCond  = s.length() == 0 ? "" : s.toString().trim();
				mHandler.sendEmptyMessage(REFRESH_FOODS);
			}
		});
        
		//删除搜索条件按钮
		((ImageButton) view.findViewById(R.id.imageButton_delete_numberFragment)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				searchTxtView.setText("");
			}
		});
		
		/**
		 * 菜品List滚动时隐藏软键盘
		 */
		mGridView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(searchTxtView.getWindowToken(), 0);
				mGridView.requestFocus();
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		
		//刷新菜品
		mHandler.sendEmptyMessage(REFRESH_FOODS);
        return view;
	}
	
	//关闭时清除侦听
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mGridView.setOnScrollListener(null);
	}

	//菜品显示的adapter
	private class FoodAdapter extends BaseAdapter{

		private List<Food> mFoods;

		FoodAdapter(List<Food> foods)
		{
			mFoods = foods;
		}
		@Override
		public int getCount() {
			return mFoods.size();
		}

		@Override
		public Object getItem(int position) {
			if(position < mFoods.size() && position >= 0){
				return mFoods.get(position);
			}else{
				return null;
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view ;
			if(convertView == null){
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pick_food_fragment_item, null);
			}else{
				view = convertView;
			}
			
			Food food = mFoods.get(position);
			view.setTag(food);
			
			((TextView) view.findViewById(R.id.textView_foodName_pickFoodFragment_item)).setText(food.name);
			((TextView) view.findViewById(R.id.textView_num_pickFoodFragment_item)).setText(Integer.toString(food.aliasID));
			((TextView) view.findViewById(R.id.textView_price_pickFoodFragment_item)).setText(Util.float2String2(food.getPrice()));
			//设置售罄的显示
			if(food.isSellOut()){
				((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.VISIBLE);
			}else{
				((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.GONE);
			}

			return view;
		}
	}
	
	/*
	 * 提示输入点菜数量的Dialog
	 */
	private class AskOrderAmountDialog extends Dialog{

		private OrderFood _selectedFood;
		
		AskOrderAmountDialog(Food food) {
			super(getActivity(), R.style.FullHeightDialog);
			
			_selectedFood = new OrderFood(food);
			
			setContentView(R.layout.order_confirm);
			
			((TextView)findViewById(R.id.orderTitleTxt)).setText("请输入" + _selectedFood.name + "的点菜数量");
			
			final EditText countEditText = (EditText)findViewById(R.id.amountEdtTxt);
			countEditText.setText("1");
			
			//数量加按钮
			((Button) findViewById(R.id.button_plus_orderConfirm)).setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					if(!countEditText.getText().toString().equals(""))
					{
						float curNum = Float.parseFloat(countEditText.getText().toString());
						countEditText.setText(Util.float2String2(++curNum));
					}
				}
			});
			//数量减按钮
			((Button) findViewById(R.id.button_minus_orderConfirm)).setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					if(!countEditText.getText().toString().equals(""))
					{
						float curNum = Float.parseFloat(countEditText.getText().toString());
						if(--curNum >= 1.0f)
						{
							countEditText.setText(Util.float2String2(curNum));
						}
					}
				}
			});
			
			//"确定"Button
			Button okBtn = (Button)findViewById(R.id.orderConfirmBtn);
			okBtn.setText("确定");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {			
					onPick(false);
				}
			});
			
			//"口味"Button
			Button tasteBtn = (Button)findViewById(R.id.orderTasteBtn);
			tasteBtn.setText("口味");
			tasteBtn.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View arg0) {
					onPick(true);
				}
			});
			
			//"取消"Button
			Button cancelBtn = (Button)findViewById(R.id.orderCancelBtn);
			cancelBtn.setText("取消");
			cancelBtn.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
			
			//"叫起"CheckBox
			CheckBox hurriedChkBox = (CheckBox)findViewById(R.id.orderHurriedChk);
			hurriedChkBox.setText("叫起");
			hurriedChkBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){			
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						_selectedFood.hangStatus = OrderFood.FOOD_HANG_UP;
						Toast.makeText(getActivity(), "叫起\"" + _selectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
					}else{
						_selectedFood.hangStatus = OrderFood.FOOD_NORMAL;
						Toast.makeText(getActivity(), "取消叫起\"" + _selectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
					}
					
				}
			});
		}
		
		/**
		 * 
		 * @param selectedFood
		 * @param pickTaste
		 */
		private void onPick(boolean pickTaste){
			try{
				float orderAmount = Float.parseFloat(((EditText)findViewById(R.id.amountEdtTxt)).getText().toString());
				
       			if(orderAmount > 255){
       				Toast.makeText(getActivity(), "对不起，\"" + _selectedFood.toString() + "\"最多只能点255份", Toast.LENGTH_SHORT).show();
       			}else{
       				_selectedFood.setCount(orderAmount);
       				if(mFoodPickedListener != null){	
       					if(pickTaste){
       						mFoodPickedListener.onPickedWithTaste(_selectedFood);
       					}else{
       						mFoodPickedListener.onPicked(_selectedFood);
       					}
       				}
					dismiss();
       			}
				
			}catch(NumberFormatException e){
				Toast.makeText(getActivity(), "您输入的数量格式不正确，请重新输入", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
