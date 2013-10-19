package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.Params;
import com.wireless.fragment.KitchenFragment;
import com.wireless.fragment.OrderFoodFragment;
import com.wireless.fragment.OrderFoodFragment.OnButtonClickedListener;
import com.wireless.fragment.OrderFoodFragment.OnOrderChangedListener;
import com.wireless.fragment.PickFoodFragment;
import com.wireless.parcel.OrderParcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.ui.dialog.AskOrderAmountDialog.OnFoodPickedListener;
import com.wireless.ui.dialog.CommitDialog;

public class QuickPickActivity extends FragmentActivity implements OnFoodPickedListener, 
																   OnButtonClickedListener,
																   OnOrderChangedListener{
	//每个点菜方式的标签
	private static final int NUMBER_FRAGMENT = 6320;
	private static final int KITCHEN_FRAGMENT = 6321;
	private static final int PINYIN_FRAGMENT = 6322;
	private static final int ORDER_FOOD_FRAGMENT = 6323;
	private int mLastView;

	
	//刷新每个view的handler
	private ViewHandler mViewHandler;
	
	private static class ViewHandler extends Handler{
		private WeakReference<QuickPickActivity> mActivity;
		
		private TextView mTitleTextView;
		private ImageButton mNumBtn;
		private ImageButton mKitchenBtn;
		private ImageButton mSpellBtn;
		private ImageButton mPickedBtn;

		ViewHandler(QuickPickActivity activity){
			mActivity = new WeakReference<QuickPickActivity>(activity);
			mTitleTextView = (TextView) activity.findViewById(R.id.toptitle);
			mTitleTextView.setVisibility(View.VISIBLE);
			
			mNumBtn = (ImageButton) activity.findViewById(R.id.imageButton_num_quickPick);
			mKitchenBtn = (ImageButton) activity.findViewById(R.id.imageButton_kitchen_quickPick);
			mSpellBtn = (ImageButton) activity.findViewById(R.id.imageButton_spell_quickPick);
			mPickedBtn = (ImageButton) activity.findViewById(R.id.imageButton_remark_quickPick);
			
		}
		
		@Override
		public void handleMessage(Message msg) {
			final QuickPickActivity activity = mActivity.get();
			FragmentTransaction fgmTrans = activity.getSupportFragmentManager().beginTransaction();
			
			switch(msg.what)
			{
			case NUMBER_FRAGMENT:
				//显示编号选择菜品的Fragment
				activity.findViewById(R.id.frameLayout_container_quickPick).setVisibility(View.VISIBLE);
				activity.findViewById(R.id.frameLayout_orderFood_quickPick).setVisibility(View.INVISIBLE);
				fgmTrans.replace(R.id.frameLayout_container_quickPick, PickFoodFragment.newInstanceByNum(), Integer.toString(NUMBER_FRAGMENT)).commit();
				
				activity.mLastView = NUMBER_FRAGMENT;
				mTitleTextView.setText("点菜 - 编号");
				setLastCate(NUMBER_FRAGMENT);
				
				break;
				
			case KITCHEN_FRAGMENT:
				
				//显示分厨选择菜品的Fragment
				activity.findViewById(R.id.frameLayout_container_quickPick).setVisibility(View.VISIBLE);
				activity.findViewById(R.id.frameLayout_orderFood_quickPick).setVisibility(View.INVISIBLE);
				fgmTrans.replace(R.id.frameLayout_container_quickPick, new KitchenFragment(), Integer.toString(KITCHEN_FRAGMENT)).commit();
				
				activity.mLastView = KITCHEN_FRAGMENT;
				mTitleTextView.setText("点菜 - 分厨");
				setLastCate(KITCHEN_FRAGMENT);
				break;
				
			case PINYIN_FRAGMENT:
				//显示拼音选择菜品的Fragment
				activity.findViewById(R.id.frameLayout_container_quickPick).setVisibility(View.VISIBLE);
				activity.findViewById(R.id.frameLayout_orderFood_quickPick).setVisibility(View.INVISIBLE);
				fgmTrans.replace(R.id.frameLayout_container_quickPick, PickFoodFragment.newInstanceByPinyin(), Integer.toString(PINYIN_FRAGMENT)).commit();
				
				activity.mLastView = PINYIN_FRAGMENT;
				mTitleTextView.setText("点菜 - 拼音");
				setLastCate(PINYIN_FRAGMENT);
				break;
				
			case ORDER_FOOD_FRAGMENT:
				//创建OrderFoodFragment
				activity.findViewById(R.id.frameLayout_container_quickPick).setVisibility(View.INVISIBLE);
				activity.findViewById(R.id.frameLayout_orderFood_quickPick).setVisibility(View.VISIBLE);
				
				activity.mLastView = ORDER_FOOD_FRAGMENT;
				mTitleTextView.setText("已点菜");
				setLastCate(ORDER_FOOD_FRAGMENT);
				break;
			}
		}
		
		private void setLastCate(int cate){
			
			QuickPickActivity activity = mActivity.get();
			//还原按样式
			mNumBtn.setImageResource(R.drawable.number_btn);
			mKitchenBtn.setImageResource(R.drawable.kitchen);
			mSpellBtn.setImageResource(R.drawable.pinyin);
			mPickedBtn.setImageResource(R.drawable.picked_food);
			//切换点菜方式时，保存当前的点菜模式
			Editor editor = activity.getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();
			
			switch(cate)
			{
			case NUMBER_FRAGMENT:
				editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_NUMBER);
				mNumBtn.setImageResource(R.drawable.number_btn_down);
				break;
			case KITCHEN_FRAGMENT:
				editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_KITCHEN);
				mKitchenBtn.setImageResource(R.drawable.kitchen_down);
				break;
			case PINYIN_FRAGMENT:
				editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_PINYIN);
				mSpellBtn.setImageResource(R.drawable.pinyin_down);
				break;
			case ORDER_FOOD_FRAGMENT:
				mPickedBtn.setImageResource(R.drawable.picked_food_down);
				break;
			}
			editor.commit();
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quick_pick);
		
		mViewHandler = new ViewHandler(this);
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		
		//返回Button
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
		
		//提交按钮
		TextView right = (TextView) findViewById(R.id.textView_right);
		right.setText("提交");
		right.setVisibility(View.VISIBLE);
		
		ImageButton commit = (ImageButton) findViewById(R.id.btn_right);
		commit.setVisibility(View.VISIBLE);
		commit.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//若未点菜，则提示。
				if(((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).hasNewOrderFood()){
					CommitDialog.newCommitDialog(((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).buildRequestOrder(0, 1)).show(getSupportFragmentManager(), CommitDialog.TAG);
				}else{
					Toast.makeText(getApplicationContext(), "您尚未点菜", Toast.LENGTH_SHORT).show();
				}
			}
		});

		//将菜品添加到"已点菜"
		OrderParcel orderParcel = getIntent().getParcelableExtra(OrderParcel.KEY_VALUE);
		if(orderParcel != null){
			((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).addFoods(orderParcel.asOrder().getOrderFoods());
		}
		
		//编号
		((ImageButton) findViewById(R.id.imageButton_num_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mLastView != NUMBER_FRAGMENT){
					mViewHandler.sendEmptyMessage(NUMBER_FRAGMENT);
				}
			}
		});
		
		//分厨
		((ImageButton) findViewById(R.id.imageButton_kitchen_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mLastView != KITCHEN_FRAGMENT){
					mViewHandler.sendEmptyMessage(KITCHEN_FRAGMENT);
				}
			}
		});
		
		//拼音
		((ImageButton) findViewById(R.id.imageButton_spell_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mLastView != PINYIN_FRAGMENT){
					mViewHandler.sendEmptyMessage(PINYIN_FRAGMENT);
				}
			}
		});
		
		//已点菜
		((ImageButton) findViewById(R.id.imageButton_remark_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(ORDER_FOOD_FRAGMENT);
			}
		});

		switchToOrderView();
	}

	@Override
	public void onBackPressed() {
		
		if(((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).hasNewOrderFood()){
			new AlertDialog.Builder(QuickPickActivity.this)
				.setTitle("退出确认")
				.setMessage("已点菜尚未提交，确定要退出？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					@Override
					public void onClick( DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				})
				.setNegativeButton("取消", null).show();
		}else{
			super.onBackPressed();
		}
	}

	private void switchToOrderView(){
		//根据上次保存的记录，切换到相应的点菜方式
		int lastPickCate = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).getInt(Params.LAST_PICK_CATE, Params.PICK_BY_PINYIN);
		switch(lastPickCate){
		case Params.PICK_BY_NUMBER:
			mViewHandler.sendEmptyMessage(NUMBER_FRAGMENT);
			break;
		case Params.PICK_BY_KITCHEN:
			mViewHandler.sendEmptyMessage(KITCHEN_FRAGMENT);
			break;
		case Params.PICK_BY_PINYIN:
			mViewHandler.sendEmptyMessage(PINYIN_FRAGMENT);
			break;
		default :
			mViewHandler.sendEmptyMessage(PINYIN_FRAGMENT);
		}
	}
	
	@Override
	public void onFoodPicked(OrderFood food) {
		((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).addFood(food);
	}

	@Override
	public void onPickFoodClicked() {
		switchToOrderView();		
	}

	@Override
	public void onOrderChanged(Order oriOrder, List<OrderFood> newFoodList) {
		
		TextView txtViewAmount = (TextView)findViewById(R.id.txtView_amount_right);

		if(newFoodList.isEmpty()){
			txtViewAmount.setVisibility(View.GONE);
		}else{
			txtViewAmount.setVisibility(View.VISIBLE);
			txtViewAmount.setText(newFoodList.size() + "");
		}
	}

}

