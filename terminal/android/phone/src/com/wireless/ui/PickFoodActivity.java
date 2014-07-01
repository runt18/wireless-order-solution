package com.wireless.ui;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.fragment.KitchenFragment;
import com.wireless.fragment.PickFoodFragment;
import com.wireless.fragment.TempFoodFragment;
import com.wireless.parcel.OrderParcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.dialog.AskOrderAmountDialog.ActionType;
import com.wireless.ui.dialog.AskOrderAmountDialog.OnFoodPickedListener;

public class PickFoodActivity extends FragmentActivity 
							  implements OnFoodPickedListener{
	
	//每个点菜方式的标签
	private static final int NUMBER_FRAGMENT = 1320;	//编号
	private static final int KITCHEN_FRAGMENT = 1321;	//分厨
	private static final int PINYIN_FRAGMENT = 1322;	//拼音
	private static final int TEMP_FOOD_FRAGMENT = 1323;	//临时菜

	private static final int DEFAULT_FRAGMENT = PINYIN_FRAGMENT;
	
	private int mCurFg = -1;
	
	private static final String TEMP_FOOD_FRAGMENT_TAG = "tempFoodFragmentTag";

	private ViewHandler mViewHandler;
	
	//通过临时账单保存新点菜
	private Order mTmpOrder = new Order();

	private static class ViewHandler extends Handler{
		private WeakReference<PickFoodActivity> mActivity;
		private TextView mTitleTextView;
		private ImageButton mNumBtn;
		private ImageButton mKitchenBtn;
		private ImageButton mPinyinBtn;
		private ImageButton mTempBtn;
		
		ViewHandler(PickFoodActivity activity)
		{
			mActivity = new WeakReference<PickFoodActivity>(activity);
			mTitleTextView = (TextView) activity.findViewById(R.id.toptitle);
			mTitleTextView.setVisibility(View.VISIBLE);
			
			mNumBtn = (ImageButton) activity.findViewById(R.id.imageButton_num_pickFood);
			mKitchenBtn = (ImageButton) activity.findViewById(R.id.imageButton_kitchen_pickFood);
			mPinyinBtn = (ImageButton) activity.findViewById(R.id.imageButton_spell_pickFood);
			mTempBtn = (ImageButton) activity.findViewById(R.id.imageButton_tempFood_pickFood);
		}
		
		@Override
		public void handleMessage(Message msg) {
			PickFoodActivity activity = mActivity.get();
			FragmentTransaction fgTrans = activity.getSupportFragmentManager().beginTransaction();

			switch(msg.what){
			
			case NUMBER_FRAGMENT:
				if(mActivity.get().mCurFg != NUMBER_FRAGMENT){
					//创建新菜品选择fragment, 替换原本的fragment
					fgTrans.replace(R.id.frameLayout_container_pickFood, PickFoodFragment.newInstanceByNum()).commit();
					
					mTitleTextView.setText("点菜 - 编号");
					setLastCate(NUMBER_FRAGMENT);
					mActivity.get().mCurFg = NUMBER_FRAGMENT;
				}
				break;
				
			case KITCHEN_FRAGMENT:
				if(mActivity.get().mCurFg != KITCHEN_FRAGMENT){
					fgTrans.replace(R.id.frameLayout_container_pickFood, new KitchenFragment()).commit();
					
					mTitleTextView.setText("点菜 - 分厨");
					setLastCate(KITCHEN_FRAGMENT);
					mActivity.get().mCurFg = KITCHEN_FRAGMENT;
				}
				break;
				
			case PINYIN_FRAGMENT:
				if(mActivity.get().mCurFg != PINYIN_FRAGMENT){
					//创建新菜品选择fragment, 替换原本的fragment
					fgTrans.replace(R.id.frameLayout_container_pickFood, PickFoodFragment.newInstanceByPinyin()).commit();
					
					mTitleTextView.setText("点菜 - 拼音");
					setLastCate(PINYIN_FRAGMENT);
					mActivity.get().mCurFg = PINYIN_FRAGMENT;
				}
				break;
			case TEMP_FOOD_FRAGMENT:
				if(mActivity.get().mCurFg != TEMP_FOOD_FRAGMENT){
					TempFoodFragment tempFgm = new TempFoodFragment();
					fgTrans.replace(R.id.frameLayout_container_pickFood, tempFgm, TEMP_FOOD_FRAGMENT_TAG).commit();
					
					mTitleTextView.setText("点菜 - 临时菜");
					setLastCate(TEMP_FOOD_FRAGMENT);
					mActivity.get().mCurFg = TEMP_FOOD_FRAGMENT;
				}
				break;
			}
		}
		
		private void setLastCate(int cate){
			PickFoodActivity activity = mActivity.get();
			//还原按样式
			mNumBtn.setImageResource(R.drawable.number_btn);
			mKitchenBtn.setImageResource(R.drawable.kitchen);
			mPinyinBtn.setImageResource(R.drawable.pinyin);
			mTempBtn.setImageResource(R.drawable.linshicai);

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
				mPinyinBtn.setImageResource(R.drawable.pinyin_down);
				break;
			case TEMP_FOOD_FRAGMENT:
//				editor.putInt(Params.LAST_PICK_CATE, 5);
				mTempBtn.setImageResource(R.drawable.linshicai_down);
			default:
				
			}
			editor.commit();
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pick_food_activity);
		
		mViewHandler = new ViewHandler(this);
		
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
				finish();
			}
		});

		//编号
		((ImageButton) findViewById(R.id.imageButton_num_pickFood)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(NUMBER_FRAGMENT);
			}
		});
		//分厨
		((ImageButton) findViewById(R.id.imageButton_kitchen_pickFood)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(KITCHEN_FRAGMENT);
			}
		});
		
		//拼音
		((ImageButton) findViewById(R.id.imageButton_spell_pickFood)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(PINYIN_FRAGMENT);
			}
		});
		//临时菜
		((ImageButton) findViewById(R.id.imageButton_tempFood_pickFood)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(TEMP_FOOD_FRAGMENT);
			}
		});

		/**
		 * 根据上次保存的记录，切换到相应的点菜方式
		 */
		int lastPickCate = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).getInt(Params.LAST_PICK_CATE, Params.PICK_BY_KITCHEN);
		switch(lastPickCate)
		{
		case Params.PICK_BY_NUMBER:
			mViewHandler.sendEmptyMessage(NUMBER_FRAGMENT);
			break;
		case Params.PICK_BY_KITCHEN:
			mViewHandler.sendEmptyMessage(KITCHEN_FRAGMENT);
			break;
		case Params.PICK_BY_PINYIN:
			mViewHandler.sendEmptyMessage(PINYIN_FRAGMENT);
			break;
		case 5:
			mViewHandler.sendEmptyMessage(TEMP_FOOD_FRAGMENT);
			break;
		default :
			mViewHandler.sendEmptyMessage(DEFAULT_FRAGMENT);
		}
	}

	/**
	 * 返回时将新点菜品的List返回到上一个Activity
	 */
	@Override
	public void onBackPressed() {
		// Add the temporary foods to the picked food list
		// except the ones without food name
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		
		//FIXME
		TempFoodFragment fgm = (TempFoodFragment) getSupportFragmentManager().findFragmentByTag(TEMP_FOOD_FRAGMENT_TAG);
		if(fgm != null){
			for(OrderFood f : fgm.getValidTempFood()){
				try {
					mTmpOrder.addFood(f, WirelessOrder.loginStaff);
				} catch (BusinessException e) {
					Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		}

		bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(mTmpOrder));
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		super.onBackPressed();
	}
	
	/**
	 * 选中菜品后添加到新点菜列表中
	 * @param food
	 *            选中菜品的信息
	 */
	@Override
	public void onFoodPicked(OrderFood food, ActionType actionType) {
		if(actionType == ActionType.ADD){
			try{
				mTmpOrder.addFood(food, WirelessOrder.loginStaff);
				
				TextView amountTxtView = ((TextView)findViewById(R.id.txtView_amount_left_topBar));
				amountTxtView.setVisibility(View.VISIBLE);
				amountTxtView.setText(mTmpOrder.getOrderFoods().size() + "");
				
				Toast.makeText(this, "添加"	+ (food.isHangup() ? "并叫起\"" : "\"") + food.toString() + "\"" +
									 NumericUtil.float2String2(food.getCount()) + "份", Toast.LENGTH_SHORT).show();
	
			}catch(BusinessException e){
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}

}
