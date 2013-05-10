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
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import android.widget.ViewFlipper;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.protocol.OrderFood;

/**
 * this fragment extends {@link DialogFragment} 
 * <p> it use {@link ViewFlipper} and {@link GestureDetector} to handler the scroll operation
 * @author ggdsn1
 *
 */
public class PickTasteFragment extends DialogFragment  implements OnGestureListener {
	private ViewFlipper mFlipper;
	private GestureDetector mGDetector;

	private List<Taste> mTastes;	
	private OrderFood mOrderFood;
	
	private static final int TASTE_AMOUNT_PER_PAGE = 20;
	
	public static final String FOCUS_TASTE = "focus_taste";
	
	private static final int TASTE_FOOD = 243459;
	private static final int TASTE_ALL = 89457;
	private static final int TASTE_SELECTED = 34874;
	private static final int TASTE_REMOVED = 33486;

	//the taste search condition
	private String mFilterCond = "";
	private TasteRefreshHandler mTasteHandler;
	
	public interface OnTasteChangeListener{
		public void onTasteChanged(OrderFood food);
	}
	
	private OnTasteChangeListener mOnTasteChangeListener;

	
	public void setOnTasteChangeListener(OnTasteChangeListener l)
	{
		mOnTasteChangeListener = l;
	}
	
	/**
	 * the handler which can refresh all taste list
	 * @author ggdsn1
	 *
	 */
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
			
			/*
			 * according to the message, show different tag
			 */
			switch(msg.what)
			{
			case TASTE_FOOD :
				mCurTasteGroup = TASTE_FOOD;
				if(fragment.mOrderFood.getPopTastes().length != 0){
					fragment.mTastes = Arrays.asList(fragment.mOrderFood.getPopTastes());
				}else{
					fragment.mTastes = new ArrayList<Taste>(WirelessOrder.foodMenu.tastes);
				}
				refreshTasteDisplay();
				break;
				
			case TASTE_ALL :
				mCurTasteGroup = TASTE_ALL;
				fragment.mTastes = new ArrayList<Taste>(WirelessOrder.foodMenu.tastes);
				refreshTasteDisplay();
				break;
				
			case TASTE_SELECTED :
				refreshPickedTaste();
				break;
				
			case TASTE_REMOVED:
				refreshPickedTaste();
				refreshTasteDisplay();
				break;
			}
		}
		
		private void refreshTasteDisplay(){
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
		
		/**
		 * refresh all picked taste's display
		 */
		private void refreshPickedTaste(){
			final PickTasteFragment fragment = mFragment.get();
			mPickedTasteLinear.removeAllViews();
			if(fragment.mOrderFood.hasNormalTaste()){
				//如果不是规格，则显示该口味
				for(Taste normalTaste : fragment.mOrderFood.getTasteGroup().getNormalTastes()){
					boolean isSpec = false;
					for(Taste spec:WirelessOrder.foodMenu.specs)
					{
						if(normalTaste.equals(spec)){
							isSpec = true;
							break;
						}
					}
					//不是规格，则显示该口味的button
					if(!isSpec)
					{
						Button btn = new Button(fragment.getActivity());
						btn.setText(normalTaste.getPreference());
						btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.search, 0);
						btn.setTag(normalTaste);
						mPickedTasteLinear.addView(btn);
					
						btn.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								Taste t = (Taste) v.getTag();
								if(fragment.mOrderFood.hasTaste())
									fragment.mOrderFood.getTasteGroup().removeTaste(t);
								TasteRefreshHandler.this.sendEmptyMessage(TASTE_REMOVED);
							}
						});
					}
				}				
			}
			mSelectedFoodPriceTextView.setText("" + fragment.mOrderFood.getUnitPriceWithTaste());
		}

		int getCurTasteGroup() {
			return mCurTasteGroup;
		}
	}
	
	/**
	 * initial {@link GestureDetector} and some {@link Handler}
	 */
	@Override 
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setStyle(PickTasteFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
		
		mGDetector = new GestureDetector(this.getActivity(), this);
		
		OrderFoodParcel foodParcel = getArguments().getParcelable(OrderFoodParcel.KEY_VALUE);
		mOrderFood = foodParcel;
		mTasteHandler = new TasteRefreshHandler(this);
		
		mTasteHandler.sendEmptyMessage(TASTE_FOOD);
		mTasteHandler.sendEmptyMessage(TASTE_SELECTED);
		
	}
	
	@Override 
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pick_taste_dialog, container, false);
		
		mFlipper = (ViewFlipper) view.findViewById(R.id.viewFlipper_pickTasteDialog);
		
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
				if(!mOrderFood.hasTaste()){
					mOrderFood.makeTasteGroup();
				}
				if(mOnTasteChangeListener != null){
					mOnTasteChangeListener.onTasteChanged(mOrderFood);
				}
				dismiss();
			}
		});
		return view;
	}
	
	/**
	 * grouping all foods by {@link #TASTE_AMOUNT_PER_PAGE} and set the {@link Adapter} for {@link GridView}
	 * @param tastes
	 */
	private void refreshTaste(List<Taste> tastes){
		if(tastes == null)
			return;
		int tLength = tastes.size();
		// 计算屏幕的页数
		int pageSize = (tLength / TASTE_AMOUNT_PER_PAGE) + (tLength	% TASTE_AMOUNT_PER_PAGE == 0 ? 0 : 1);
		// 清空所有Grid View
		mFlipper.removeAllViews();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		
		for(int pageNo=0; pageNo < pageSize; pageNo++){
			// 每页餐台的Grid View
			GridView grid = new GridView(this.getActivity());
			grid.setSelected(true);
			grid.setLayoutParams(lp);
			
			// 设置显示的列数
			grid.setNumColumns(5);
			
			grid.setHorizontalSpacing(8);
			grid.setVerticalSpacing(8);
			//滚动侦听
			grid.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return mGDetector.onTouchEvent(event);
				}
			});
			//每项侦听器
			grid.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final CheckBox selectChkBox = (CheckBox) view.findViewById(R.id.checkBox_pickTaste_item);
					final RelativeLayout background = (RelativeLayout)view.findViewById(R.id.realativeLayout_pickTaste_item);
					//when clicked , highlight the box and refresh selected taste
					if(selectChkBox.isChecked()){
						if(mOrderFood.hasNormalTaste()){
							mOrderFood.getTasteGroup().removeTaste(mTastes.get(position));
							background.setBackgroundResource(R.color.blue);
							selectChkBox.setChecked(false);
						}
					}else{
						if(!mOrderFood.hasNormalTaste()){
							mOrderFood.makeTasteGroup();
						}
						mOrderFood.getTasteGroup().addTaste(mTastes.get(position));
						selectChkBox.setChecked(true);
						background.setBackgroundResource(R.color.green);
						Toast.makeText(PickTasteFragment.this.getActivity(), "添加" + mTastes.get(position).getPreference(), Toast.LENGTH_SHORT).show();
	
					}
					mTasteHandler.sendEmptyMessage(TASTE_SELECTED);
				}
			});
			
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
			mFlipper.addView(grid);
		}
	}
	
	/**
	 * the taste gridView adapter
	 * @author ggdsn1
	 *
	 */
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
			return mTastes.get(position).getAliasId();
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
			
			if(mOrderFood.hasNormalTaste()){
				for(Taste normalTaste : mOrderFood.getTasteGroup().getNormalTastes()){
					if(mTastes.get(position).equals(normalTaste)){
						selectChkBox.setChecked(true);
						background.setBackgroundResource(R.color.green);
						break;
					}else{
						selectChkBox.setChecked(false);
						background.setBackgroundResource(R.color.blue);
					}
				}
			}
			
			return view;
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	/**
	 * the fling method, handle finger's {@link MotionEvent}
	 */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		//fly 60px will start scroll 
		if (e1.getX() - e2.getX() > 60) {
			this.mFlipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_left_in));
			this.mFlipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_left_out));
			this.mFlipper.showNext();
			return true;
		} else if (e1.getX() - e2.getX() < -60) {
			this.mFlipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_right_in));
			this.mFlipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_right_out));
			this.mFlipper.showPrevious();
			return true;
		}		
		return false;
	}
}