package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.util.ScrollLayout;

public class PickTasteFragment extends DialogFragment {
	private ScrollLayout mScrollLayout;
	private List<Taste> mTastes;
	private OrderFood mOrderFood;
	
	private static final int TASTE_AMOUNT_PER_PAGE = 20;
	
	public static final String FOCUS_TASTE = "focus_taste";
	public static final String FOCUS_NOTE = "focus_note";
	
	private static final int TASTE_FOOD = 243459;
	private static final int TASTE_ALL = 89457;
	private static final int TASTE_SELECTED = 34874;
	private static final int TASTE_REMOVED = 33486;

	private String mFilterCond = "";
	private TasteRefreshHandler mTasteHandler;
	
	public interface OnTasteChangeListener{
		public void onTasteChange(OrderFood food);
	}
	
	private OnTasteChangeListener mOnTasteChangeListener;
	
	public void setOnTasteChangeListener(OnTasteChangeListener l)
	{
		mOnTasteChangeListener = l;
	}
	
	private static class TasteRefreshHandler extends Handler{
		private int mCurTasteGroup = TASTE_FOOD;
		private List<Taste> mFilterTaste = new ArrayList<Taste>();
		private WeakReference<PickTasteFragment> mFragment;
		private TextView mSelectedFoodPriceTextView ;
		private LinearLayout mPickedTasteLinear;
		
		TasteRefreshHandler(PickTasteFragment fragment){
			mFragment = new WeakReference<PickTasteFragment>(fragment);
		}
		
		@Override
		public void handleMessage(Message msg)
		{
			final PickTasteFragment fragment = mFragment.get();
			//初始化
			if(mPickedTasteLinear == null)
				mPickedTasteLinear = (LinearLayout) fragment.getView().findViewById(R.id.pickedTaste_linearLayout);
			if(mSelectedFoodPriceTextView == null)
				mSelectedFoodPriceTextView = (TextView) fragment.getView().findViewById(R.id.textView_selected_tastePrice);
			
			switch(msg.what)
			{
			case TASTE_FOOD :
				mCurTasteGroup = TASTE_FOOD;
				fragment.mTastes = Arrays.asList(fragment.mOrderFood.popTastes);
				refreshDisplay();
				break;
				
			case TASTE_ALL :
				mCurTasteGroup = TASTE_ALL;
				fragment.mTastes = Arrays.asList(WirelessOrder.foodMenu.tastes);
				refreshDisplay();
				break;
				
			case TASTE_SELECTED :
				refreshPickedTaste();
				break;
			case TASTE_REMOVED:
				refreshPickedTaste();
				refreshDisplay();
				break;
			}
		}
		
		private void refreshDisplay(){
			final PickTasteFragment fragment = mFragment.get();

			//重新加载要显示的taste数据
			mFilterTaste.clear();
			mFilterTaste.addAll(fragment.mTastes);
			Iterator<Taste> iter = mFilterTaste.iterator();
			while(iter.hasNext())
			{
				Taste t = iter.next();
				if(fragment.mFilterCond.length() != 0){
					if(!(t.getPreference().contains(fragment.mFilterCond))){
						iter.remove();
					}
				}
			}
			fragment.refreshTaste(mFilterTaste);
		}
		
		private void refreshPickedTaste(){
			final PickTasteFragment fragment = mFragment.get();
			mPickedTasteLinear.removeAllViews();
			if(fragment.mOrderFood.hasNormalTaste()){
				for(Taste t : fragment.mOrderFood.tastes)
				{
					if(t.aliasID != Taste.NO_TASTE)
					{
						Button btn = new Button(fragment.getActivity());
						btn.setText(t.getPreference());
						btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.search, 0);
						btn.setTag(t);
						mPickedTasteLinear.addView(btn);
						
						btn.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								Taste t = (Taste) v.getTag();
								fragment.mOrderFood.removeTaste(t);
								TasteRefreshHandler.this.sendEmptyMessage(TASTE_REMOVED);
							}
						});
					}
				}
			}
			mSelectedFoodPriceTextView.setText("" + fragment.mOrderFood.getPriceWithTaste());
		}

		int getCurTasteGroup() {
			return mCurTasteGroup;
		}
	}
	
	@Override 
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setStyle(PickTasteFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
		
		FoodParcel foodParcel = getArguments().getParcelable(FoodParcel.KEY_VALUE);
		mOrderFood = foodParcel;
		mTasteHandler = new TasteRefreshHandler(this);
		
		mTasteHandler.sendEmptyMessage(TASTE_FOOD);
		mTasteHandler.sendEmptyMessage(TASTE_SELECTED);
		
	}
	
	@Override 
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pick_taste_dialog, container, false);
		
		mScrollLayout  = (ScrollLayout) view.findViewById(R.id.scrollLayout_pickTaste);
		//点击不同的radiogroup按钮时显示不同的口味
		((RadioGroup) view.findViewById(R.id.radioGroup_taste_pickTaste)).setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup group,int checkedId) {
				switch(checkedId)
				{
				case R.id.radio0_pickTaste :
					mTasteHandler.sendEmptyMessage(TASTE_FOOD);
					break;
				case R.id.radio1_pickTaste :
					mTasteHandler.sendEmptyMessage(TASTE_ALL);
					break;
				}
			}
		});
		
		//设置品注的显示
		final EditText pinzhuEditText = (EditText) view.findViewById(R.id.editText_note_pickTaste);
		if(mOrderFood.hasTmpTaste())
			pinzhuEditText.setText(mOrderFood.tmpTaste.getPreference());
		if(getTag() == FOCUS_NOTE)
			pinzhuEditText.requestFocus();
		//搜索框
		((EditText)view.findViewById(R.id.editText_pickTaste)).addTextChangedListener(new TextWatcher(){
			@Override 
			public void afterTextChanged(Editable s) {}
			
			@Override
			public void beforeTextChanged(CharSequence s,int start, int count, int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mFilterCond = s.length() == 0 ? "" : s.toString().trim();
				mTasteHandler.sendEmptyMessage(mTasteHandler.getCurTasteGroup());
			}
		});
		//返回按钮，保存口味并隐藏对话框
		((Button) view.findViewById(R.id.button_confirm_pickTaste_dialog)).setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
				if(!mOrderFood.hasTmpTaste())
					mOrderFood.tmpTaste = new Taste();
				mOrderFood.tmpTaste.setPreference(pinzhuEditText.getText().toString());
				
				if(mOnTasteChangeListener != null){
					mOnTasteChangeListener.onTasteChange(mOrderFood);
				}
				dismiss();
			}
		});
		return view;
	}
	
	private void refreshTaste(List<Taste> tastes){
		if(tastes == null)
			return;
		int tLength = tastes.size();
		// 计算屏幕的页数
		int pageSize = (tLength / TASTE_AMOUNT_PER_PAGE) + (tLength	% TASTE_AMOUNT_PER_PAGE == 0 ? 0 : 1);
		// 清空所有Grid View
		mScrollLayout.removeAllViews();
		mScrollLayout.page = 0;
		mScrollLayout.mCurScreen = 0;
		mScrollLayout.mDefaultScreen = 0;
		
		for(int pageNo=0; pageNo < pageSize; pageNo++){
			// 每页餐台的Grid View
			GridView grid = new GridView(this.getActivity());
			grid.setSelected(true);
			
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			
			
			grid.setLayoutParams(lp);
			
			// 设置显示的列数
			grid.setNumColumns(5);
			
			grid.setHorizontalSpacing(8);
			grid.setVerticalSpacing(8);
			
			// 获取显示在此page显示的Taste对象
			ArrayList<Taste> taste4Page = new ArrayList<Taste>();
			for (int i = 0; i < TASTE_AMOUNT_PER_PAGE; i++) {
				int index = pageNo * TASTE_AMOUNT_PER_PAGE + i;
				if (index < tLength) {
					taste4Page.add(tastes.get(index));
				} else {
					break;
				}
			}
			grid.setAdapter(new TasteAdapter(taste4Page));
			mScrollLayout.addView(grid);
		}
	}
	
	private class TasteAdapter extends BaseAdapter {

		private ArrayList<Taste> mTastes;

		TasteAdapter(ArrayList<Taste> tastes) {
			mTastes = tastes;
		}

		@Override
		public int getCount() {
			return mTastes.size();
		}

		@Override
		public Object getItem(int position) {
			return mTastes.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mTastes.get(position).aliasID;
		}

		@Override
		public View getView(final int position, View convertView,
				final ViewGroup parent) {

			final View view;

			if (convertView == null) {
				view = LayoutInflater.from(parent.getContext()).inflate(
						R.layout.gridview_item_pick_taste, null);
			} else {
				view = convertView;
			}

			final Taste taste = mTastes.get(position);
			view.setTag(taste);
			
			//口味名和价格的显示
			((TextView) view.findViewById(R.id.textView_tasteName_gridItem)).setText(taste.getPreference());
			((TextView) view.findViewById(R.id.textView_tastePrice_gridItem)).setText("" + taste.getPrice());
			
			//根据是否是被选的菜来显示不同的外观
			final CheckBox selectChkBox = (CheckBox) view.findViewById(R.id.checkBox_pickTaste_item);
			final RelativeLayout background = (RelativeLayout)view.findViewById(R.id.realativeLayout_pickTaste_item);
			selectChkBox.setChecked(false);
			
			for(int i = 0; i < mOrderFood.tastes.length; i++){
				if(mTastes.get(position).aliasID == mOrderFood.tastes[i].aliasID){
					selectChkBox.setChecked(true);
					background.setBackgroundResource(R.color.green);
					break;
				}else{
					selectChkBox.setChecked(false);
					background.setBackgroundResource(R.color.blue);
				}
			}
			
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
			        
					if(selectChkBox.isChecked()){
						int pos = mOrderFood.removeTaste(mTastes.get(position));
						if(pos >= 0){
							selectChkBox.setChecked(false);
							Toast.makeText(PickTasteFragment.this.getActivity(), "删除" + mTastes.get(position).getPreference(), Toast.LENGTH_SHORT).show();
						}
						background.setBackgroundResource(R.color.blue);
					}else{
						int pos = mOrderFood.addTaste(mTastes.get(position));
						if(pos >= 0){
							selectChkBox.setChecked(true);
							background.setBackgroundResource(R.color.green);
							Toast.makeText(PickTasteFragment.this.getActivity(), "添加" + mTastes.get(position).getPreference(), Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(PickTasteFragment.this.getActivity(), "最多只能添加" + mOrderFood.tastes.length + "种口味", Toast.LENGTH_SHORT).show();
							background.setBackgroundResource(R.color.blue);
						}

					}
					mTasteHandler.sendEmptyMessage(TASTE_SELECTED);
				}
			});
			
			return view;
		}
	}
}