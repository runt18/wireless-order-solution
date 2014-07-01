package com.wireless.ui;

import java.lang.ref.WeakReference;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.wireless.fragment.PinzhuTasteFragment;
import com.wireless.fragment.PinzhuTasteFragment.OnTmpTastePickedListener;
import com.wireless.fragment.PopTasteFragment;
import com.wireless.fragment.PopTasteFragment.OnTastePickedListener;
import com.wireless.fragment.TasteFragment;
import com.wireless.parcel.ComboOrderFoodParcel;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.parcel.TasteGroupParcel;
import com.wireless.pojo.dishesOrder.ComboOrderFood;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.tasteMgr.Taste;

public class PickTasteActivity extends FragmentActivity  implements OnTastePickedListener,
							   			  							OnTmpTastePickedListener
{
	
	public static final String PICK_ALL_ORDER_TASTE = "pick_all_order_taste";
	
	public final static String PICK_TASTE_INIT_FGM = "pick_taste_initial_fgm";
	
	private OrderFood mSelectedFood;
	
	private ComboOrderFood mSelectedCombo;
	
	//每个口味方式的标签
	public static final int POP_TASTE_FRAGMENT = 0;		//常用
	public static final int ALL_TASTE_FRAGMENT = 1;		//口味
	public static final int PINZHU_FRAGMENT = 2;		//品注

	private int mCurFg = -1;
	
	private Handler mTasteHandler; 
	
	private Handler mFgmHandler;
	
	private static class FgmHandler extends Handler{
		private final WeakReference<PickTasteActivity> mActivity;
		private final TextView mTitleTextView;
		
		FgmHandler(PickTasteActivity activity){
			mActivity = new WeakReference<PickTasteActivity>(activity);
			mTitleTextView = (TextView) activity.findViewById(R.id.toptitle);
		}
		
		@Override
		public void handleMessage(Message msg) {
			PickTasteActivity activity = mActivity.get();
			FragmentTransaction fgTrans = activity.getSupportFragmentManager().beginTransaction();

			activity.findViewById(R.id.imgButton_pop_pickTaste).setPressed(false);
			activity.findViewById(R.id.imgButton_all_pickTaste).setPressed(false);
			activity.findViewById(R.id.imgButton_pinzhu_pickTaste).setPressed(false);
			
			if(msg.what == POP_TASTE_FRAGMENT && activity.mCurFg != POP_TASTE_FRAGMENT){
				mTitleTextView.setText("常用口味");
				activity.findViewById(R.id.imgButton_pop_pickTaste).setPressed(true);
				activity.mCurFg = POP_TASTE_FRAGMENT;
				//jump to pop taste fragment
				if(activity.mSelectedFood != null){
					fgTrans.replace(R.id.frameLayout_container_pickTaste, PopTasteFragment.newInstance(activity.mSelectedFood)).commit();
				}else if(activity.mSelectedCombo != null){
					fgTrans.replace(R.id.frameLayout_container_pickTaste, PopTasteFragment.newInstance(activity.mSelectedCombo)).commit();
				}

			}else if(msg.what == ALL_TASTE_FRAGMENT && activity.mCurFg != ALL_TASTE_FRAGMENT){
				mTitleTextView.setText("全部口味");
				activity.mCurFg = ALL_TASTE_FRAGMENT;
				activity.findViewById(R.id.imgButton_all_pickTaste).setPressed(true);
				//jump to all taste fragment
				if(activity.mSelectedFood != null){
					fgTrans.replace(R.id.frameLayout_container_pickTaste, TasteFragment.newInstance(activity.mSelectedFood)).commit();
				}else if(activity.mSelectedCombo != null){
					fgTrans.replace(R.id.frameLayout_container_pickTaste, TasteFragment.newInstance(activity.mSelectedCombo)).commit();
				}
				
			}else if(msg.what == PINZHU_FRAGMENT && activity.mCurFg != PINZHU_FRAGMENT){
				mTitleTextView.setText("品注");
				activity.findViewById(R.id.imgButton_pinzhu_pickTaste).setPressed(true);
				activity.mCurFg = PINZHU_FRAGMENT;
				//jump to pinzhu fragment
				if(activity.mSelectedFood != null){
					fgTrans.replace(R.id.frameLayout_container_pickTaste, PinzhuTasteFragment.newInstance(activity.mSelectedFood)).commit();
				}else if(activity.mSelectedCombo != null){
					fgTrans.replace(R.id.frameLayout_container_pickTaste, PinzhuTasteFragment.newInstance(activity.mSelectedCombo)).commit();
				}
				
			}
		}
	}
	
	private static class TasteHandler extends Handler{
		
		private WeakReference<PickTasteActivity> mActivity;
		
		TasteHandler(PickTasteActivity activity){
			mActivity = new WeakReference<PickTasteActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message message){
			final PickTasteActivity theActivity = mActivity.get();
			if(theActivity.mSelectedFood != null){
				((TextView)theActivity.findViewById(R.id.txtView_foodTaste_pickTaste)).setText(theActivity.mSelectedFood.toString());
			}else if(theActivity.mSelectedCombo != null){
				((TextView)theActivity.findViewById(R.id.txtView_foodTaste_pickTaste)).setText(theActivity.mSelectedCombo.toString());
			}
		}
	};
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//get the food parcel from the intent
		OrderFoodParcel foodParcel = getIntent().getParcelableExtra(OrderFoodParcel.KEY_VALUE);
		if(foodParcel != null){
			mSelectedFood = foodParcel.asOrderFood();
		}
		
		ComboOrderFoodParcel comboParcel = getIntent().getParcelableExtra(ComboOrderFoodParcel.KEY_VALUE);
		if(comboParcel != null){
			mSelectedCombo = comboParcel.asComboOrderFood();
		}

		setContentView(R.layout.pick_taste_activity);
		
		TextView title = (TextView) findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("口味");
		//返回按钮
		TextView left = (TextView) findViewById(R.id.textView_left);
		left.setText("返回");
		left.setVisibility(View.VISIBLE);
		
		ImageButton back = (ImageButton) findViewById(R.id.btn_left);
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();	
			}
		});
		
		mFgmHandler = new FgmHandler(this);
		
		//常用Button
		((ImageButton)findViewById(R.id.imgButton_pop_pickTaste)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				mFgmHandler.sendEmptyMessage(POP_TASTE_FRAGMENT);
			}
		});
		
		//口味Button
		((ImageButton)findViewById(R.id.imgButton_all_pickTaste)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				mFgmHandler.sendEmptyMessage(ALL_TASTE_FRAGMENT);
			}
		});
		
		
		//品注Button
		if(getIntent().getBooleanExtra(PICK_ALL_ORDER_TASTE, false)){
			//全单备注下不显示品注
			((ImageButton)findViewById(R.id.imgButton_pinzhu_pickTaste)).setVisibility(View.GONE);
		}else {
			((ImageButton)findViewById(R.id.imgButton_pinzhu_pickTaste)).setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
					mFgmHandler.sendEmptyMessage(PINZHU_FRAGMENT);
				}
			});
		}
			
		mTasteHandler = new TasteHandler(this);
		mTasteHandler.sendEmptyMessage(0);	
		
		final int initFgm = getIntent().getIntExtra(PICK_TASTE_INIT_FGM, POP_TASTE_FRAGMENT);
		//根据传入的信息打开不同页面
		mFgmHandler.post(new Runnable() {
			@Override
			public void run() {
				boolean hasPopTastes = false;
				if(mSelectedFood != null){
					hasPopTastes = mSelectedFood.hasTasteGroup();
				}else if(mSelectedCombo != null){
					hasPopTastes = mSelectedCombo.hasTasteGroup();
				}
				if(initFgm == POP_TASTE_FRAGMENT && !hasPopTastes){
					mFgmHandler.sendEmptyMessage(ALL_TASTE_FRAGMENT);
				}else{
					mFgmHandler.sendEmptyMessage(initFgm);
				}
			}
		});
	}

	
	@Override
	public void onBackPressed(){
		
		Intent intent = new Intent(); 
		Bundle bundle = new Bundle();
		if(mSelectedFood != null && mSelectedFood.hasTasteGroup()){
			bundle.putParcelable(TasteGroupParcel.KEY_VALUE, new TasteGroupParcel(mSelectedFood.getTasteGroup()));
		}else if(mSelectedCombo != null && mSelectedCombo.hasTasteGroup()){
			bundle.putParcelable(TasteGroupParcel.KEY_VALUE, new TasteGroupParcel(mSelectedCombo.getTasteGroup()));
		}else{
			bundle.putParcelable(TasteGroupParcel.KEY_VALUE, new TasteGroupParcel(null));
		}
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		super.onBackPressed();
	}
	
	@Override
	public void onTastePicked(Taste tasteToPick) {
		mTasteHandler.sendEmptyMessage(0);
	}


	@Override
	public void onTasteRemoved(Taste tasteToRemove) {
		mTasteHandler.sendEmptyMessage(0);
	}


	@Override
	public void onTmpTastePicked(Taste tmpTaste) {
		mTasteHandler.sendEmptyMessage(0);
	}	
	
}
