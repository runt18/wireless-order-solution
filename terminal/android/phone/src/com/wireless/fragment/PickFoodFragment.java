package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
		 * ��PickFoodListViewѡ�в�Ʒ�󣬻ص��˺���֪ͨActivityѡ�е�Food��Ϣ
		 * @param food ѡ��Food����Ϣ
		 */
		public void onPicked(OrderFood food);
		
		/**
		 * ��PickFoodListViewѡ�в�Ʒ�󣬻ص��˺���֪ͨActivityѡ�е�Food��Ϣ������ת����ζActivity
		 * @param food
		 * 			ѡ��Food����Ϣ
		 */
		public void onPickedWithTaste(OrderFood food);
	}
	/**
	 * ���õ���ĳ����Ʒ��Ļص�����
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
//			/**
//			 * �����в�Ʒ���а�������Ž�������
//			 */
//			mFoods = new Food[WirelessOrder.foodMenu.foods.length];
//			System.arraycopy(WirelessOrder.foodMenu.foods, 0, mFoods, 0,
//					WirelessOrder.foodMenu.foods.length);
//			Arrays.sort(mFoods, new Comparator<Food>() {
//				@Override
//				public int compare(Food food1, Food food2) {
//					if (food1.kitchen.aliasID > food2.kitchen.aliasID) {
//						return 1;
//					} else if (food1.kitchen.aliasID < food2.kitchen.aliasID) {
//						return -1;
//					} else {
//						return 0;
//					}
//				}
//			});
		}
		
		@Override
		public void handleMessage(Message msg){
			PickFoodFragment fragment = mFragment.get();
			//�����в�Ʒ��������ɸѡ�����adapter
			
			List<Food> tmpFoods;
			if(fragment.mFilterCond.length() != 0){
				tmpFoods = new ArrayList<Food>(mSrcFoods);
				Iterator<Food> iter = tmpFoods.iterator();
				while(iter.hasNext()){
					Food f = iter.next();
					String filerCond = fragment.mFilterCond.toLowerCase();
					if(!(f.name.toLowerCase().contains(filerCond) || 
					   f.getPinyin().contains(filerCond) || 
					   f.getPinyinShortcut().contains(filerCond))){
						iter.remove();
					}
					
					/**
					 * Sort the food by order count after filtering
					 */
					Collections.sort(tmpFoods, new Comparator<Food>(){
						public int compare(Food lhs, Food rhs) {
							if(lhs.statistics.orderCnt > rhs.statistics.orderCnt){
								return 1;
							}else if(lhs.statistics.orderCnt < rhs.statistics.orderCnt){
								return -1;
							}else{
								return 0;
							}
						}				
					});
					
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
		
        mGridView = (GridView) view.findViewById(R.id.gridView_numberFragment);
        //���õ������
        mGridView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Food food = (Food) view.getTag();
				if(!food.isSellOut()){
					((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.GONE);
					new AskOrderAmountDialog(food).show();
				}else{
					((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.VISIBLE);
					Toast.makeText(getActivity(), food.name + "������", Toast.LENGTH_SHORT).show();
				}
			}
        });
        
		//������
        final EditText searchTxtView = (EditText)view.findViewById(R.id.editText_pickFoodFragment);
        searchTxtView.setHint(args.get(PICK_FOOD_FRAGMENT_TAG_NAME).toString());
        searchTxtView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchTxtView.selectAll();
			}
		});
        //������������
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
        
		//ɾ������������ť
		((ImageButton) view.findViewById(R.id.imageButton_delete_numberFragment)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				searchTxtView.setText("");
			}
		});
		
		/**
		 * ��ƷList����ʱ���������
		 */
		mGridView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(searchTxtView.getWindowToken(), 0);
//				mGridView.requestFocus();
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		
		//ˢ�²�Ʒ
		mHandler.sendEmptyMessage(REFRESH_FOODS);
        return view;
	}
	
	//�ر�ʱ�������
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mGridView.setOnScrollListener(null);
	}

	//��Ʒ��ʾ��adapter
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
			//�������̫�����10�ض�
			if(food.name.length() >= 10){
				((TextView) view.findViewById(R.id.textView_foodName_pickFoodFragment_item)).setText(food.name.substring(0, 10));
			}else{
				((TextView) view.findViewById(R.id.textView_foodName_pickFoodFragment_item)).setText(food.name);
			}

			((TextView) view.findViewById(R.id.textView_num_pickFoodFragment_item)).setText(Integer.toString(food.getAliasId()));
			((TextView) view.findViewById(R.id.textView_price_pickFoodFragment_item)).setText(Util.float2String2(food.getPrice()));
			
			//������������ʾ
			if(food.isSellOut()){
				((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.VISIBLE);
			}else{
				((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.INVISIBLE);
			}
			
//			LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.linearLayout_pickFood_fgm_item);
//			linearLayout.removeAllViews();
//			XmlResourceParser xrp = getResources().getXml(R.color.my_color);  
//			try {  
//			    ColorStateList csl = ColorStateList.createFromXml(getResources(), xrp);  
//			    tv.setTextColor(csl);  
//			} catch (Exception e) {  
//			}  
//			//��
//			if(food.isGift()){
//				TextView text = new TextView(getActivity());
//				text.setText("��");
//				text.setTextSize(16f);
//				text.setTextColor(Color.YELLOW);
//				linearLayout.addView(text);
//			}
//			//ʱ
//			if(food.isCurPrice()){
//				TextView text = new TextView(getActivity());
//				text.setText("ʱ");
//				text.setTextSize(16f);
//				text.setTextColor(Color.MAGENTA);
//				linearLayout.addView(text);
//			}
//			//�Ƽ�
//			if(food.isRecommend()){
//				TextView text = new TextView(getActivity());
//				text.setText("��");
//				text.setTextSize(16f);
//				text.setTextColor(Color.CYAN);
//				linearLayout.addView(text);
//			}
//			//��
//			if(food.isSpecial()){
//				TextView text = new TextView(getActivity());
//				text.setText("��");
//				text.setTextSize(16f);
//				text.setTextColor(Color.GREEN);
//				linearLayout.addView(text);
//			}
//			//��
//			if(food.isCombo()){
//				TextView text = new TextView(getActivity());
//				text.setText("��");
//				text.setTextSize(16f);
//				text.setTextColor(Color.GREEN);
//				linearLayout.addView(text);
//			}
			 
			return view;
		}
	}
	
	/*
	 * ��ʾ������������Dialog
	 */
	private class AskOrderAmountDialog extends Dialog{

		private OrderFood _selectedFood;
		
		AskOrderAmountDialog(Food food) {
			super(getActivity(), R.style.FullHeightDialog);
			
			_selectedFood = new OrderFood(food);
			
			setContentView(R.layout.order_confirm);
			
			((TextView)findViewById(R.id.orderTitleTxt)).setText("������" + _selectedFood.name + "�ĵ������");
			
			final EditText countEditText = (EditText)findViewById(R.id.amountEdtTxt);
			//���ʱȫѡ
			countEditText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					countEditText.selectAll();
				}
			});
			
			//�����Ӱ�ť
			((ImageButton) findViewById(R.id.button_plus_orderConfirm)).setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					try{
						float curNum = Float.parseFloat(countEditText.getText().toString());
						if(++curNum <= 255){
							countEditText.setText(Util.float2String2(curNum));
						}else{
							Toast.makeText(getActivity(), "����������ܳ���255", Toast.LENGTH_SHORT).show();
						}
					}catch(NumberFormatException e){
						
					}
					if(!countEditText.getText().toString().equals(""))
					{
						float curNum = Float.parseFloat(countEditText.getText().toString());
						countEditText.setText(Util.float2String2(curNum));
					}
				}
			});
			//��������ť
			((ImageButton) findViewById(R.id.button_minus_orderConfirm)).setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					try{
						float curNum = Float.parseFloat(countEditText.getText().toString());
						if(--curNum >= 1.0f){
							countEditText.setText(Util.float2String2(curNum));
						}
					}catch(NumberFormatException e){
						
					}
				}
			});
			
			//"ȷ��"Button
			Button okBtn = (Button)findViewById(R.id.orderConfirmBtn);
			okBtn.setText("ȷ��");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {			
					onPick(false);
				}
			});
			
			//"��ζ"Button
			Button tasteBtn = (Button)findViewById(R.id.orderTasteBtn);
			tasteBtn.setText("��ζ");
			tasteBtn.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View arg0) {
					onPick(true);
				}
			});
			
			//"ȡ��"Button
			Button cancelBtn = (Button)findViewById(R.id.orderCancelBtn);
			cancelBtn.setText("ȡ��");
			cancelBtn.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
			
			//"����"CheckBox
			CheckBox hurriedChkBox = (CheckBox)findViewById(R.id.orderHurriedChk);
			hurriedChkBox.setText("����");
			hurriedChkBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){			
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						_selectedFood.hangStatus = OrderFood.FOOD_HANG_UP;
						Toast.makeText(getActivity(), "����\"" + _selectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
					}else{
						_selectedFood.hangStatus = OrderFood.FOOD_NORMAL;
						Toast.makeText(getActivity(), "ȡ������\"" + _selectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
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
       				Toast.makeText(getActivity(), "�Բ���\"" + _selectedFood.toString() + "\"���ֻ�ܵ�255��", Toast.LENGTH_SHORT).show();
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
					//������������
					final EditText searchText = (EditText) getView().findViewById(R.id.editText_pickFoodFragment);
					searchText.setText("");

       			}
				
			}catch(NumberFormatException e){
				Toast.makeText(getActivity(), "�������������ʽ����ȷ������������", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
