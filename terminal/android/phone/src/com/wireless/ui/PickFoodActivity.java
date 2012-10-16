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
import com.wireless.excep.BusinessException;
import com.wireless.fragment.KitchenFragment;
import com.wireless.fragment.PickFoodFragment;
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;

public class PickFoodActivity extends FragmentActivity implements 
			com.wireless.fragment.PickFoodFragment.OnFoodPickedListener, 
			com.wireless.fragment.KitchenFragment.OnFoodPickedListener
{
	
	//每个点菜方式的标签
	private static final int NUMBER_FRAGMENT = 1320;	//编号
	private static final int KITCHEN_FRAGMENT = 1321;	//分厨
	private static final int PINYIN_FRAGMENT = 1322;	//拼音
	private static final int DEFAULT_FRAGMENT = PINYIN_FRAGMENT;
	
	private int mCurFg = -1;
	
	//activity返回标签
	private static final int PICK_WITH_TASTE = 6755;

	private ViewHandler mViewHandler;
	
	//储存已点菜的列表
	//private ArrayList<OrderFood> mPickFoods = new ArrayList<OrderFood>();
	
	//通过临时账单保存新点菜
	private Order mTmpOrder = new Order();;

	private static class ViewHandler extends Handler{
		private WeakReference<PickFoodActivity> mActivity;
		private TextView mTitleTextView;
		private ImageButton mNumBtn;
		private ImageButton mKitchenBtn;
		private ImageButton mSpellBtn;
		
		ViewHandler(PickFoodActivity activity)
		{
			mActivity = new WeakReference<PickFoodActivity>(activity);
			mTitleTextView = (TextView) activity.findViewById(R.id.toptitle);
			mTitleTextView.setVisibility(View.VISIBLE);
			
			mNumBtn = (ImageButton) activity.findViewById(R.id.imageButton_num_pickFood);
			mKitchenBtn = (ImageButton) activity.findViewById(R.id.imageButton_kitchen_pickFood);
			mSpellBtn = (ImageButton) activity.findViewById(R.id.imageButton_spell_pickFood);

		}
		
		@Override
		public void handleMessage(Message msg) {
			PickFoodActivity activity = mActivity.get();
			FragmentTransaction fgTrans = activity.getSupportFragmentManager().beginTransaction();

			switch(msg.what){
			
			case NUMBER_FRAGMENT:
				if(mActivity.get().mCurFg != NUMBER_FRAGMENT){
					//创建新菜品选择fragment
					PickFoodFragment numFragment = new PickFoodFragment();
					numFragment.setFoodPickedListener(activity);
					//设置显示参数
					Bundle args = new Bundle();
					args.putInt(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG, PickFoodFragment.PICK_FOOD_FRAGMENT_NUMBER);
					args.putString(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG_NAME, "编号：");
					numFragment.setArguments(args);
					//替换原本的fragment
					fgTrans.replace(R.id.frameLayout_container_pickFood, numFragment).commit();
					
					mTitleTextView.setText("点菜 - 编号");
					setLastCate(NUMBER_FRAGMENT);
					mActivity.get().mCurFg = NUMBER_FRAGMENT;
				}
				break;
				
			case KITCHEN_FRAGMENT:
				if(mActivity.get().mCurFg != KITCHEN_FRAGMENT){
					KitchenFragment kitchenFragment = new KitchenFragment();
					kitchenFragment.setFoodPickedListener(activity);
					fgTrans.replace(R.id.frameLayout_container_pickFood, kitchenFragment).commit();
					
					mTitleTextView.setText("点菜 - 分厨");
					setLastCate(KITCHEN_FRAGMENT);
					mActivity.get().mCurFg = KITCHEN_FRAGMENT;
				}
				break;
				
			case PINYIN_FRAGMENT:
				if(mActivity.get().mCurFg != PINYIN_FRAGMENT){
					//创建新菜品选择fragment
					PickFoodFragment spellFragment = new PickFoodFragment();
					spellFragment.setFoodPickedListener(activity);
					//设置显示参数
					Bundle spellArgs = new Bundle();
					spellArgs.putInt(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG, PickFoodFragment.PICK_FOOD_FRAGMENT_SPELL);
					spellArgs.putString(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG_NAME, "拼音：");
					spellFragment.setArguments(spellArgs);
					//替换原本的fragment
					fgTrans.replace(R.id.frameLayout_container_pickFood, spellFragment).commit();
					
					mTitleTextView.setText("点菜 - 拼音");
					setLastCate(PINYIN_FRAGMENT);
					mActivity.get().mCurFg = PINYIN_FRAGMENT;
				}
				break;
			}
		}
		
		private void setLastCate(int cate){
			PickFoodActivity activity = mActivity.get();
			//还原按样式
			mNumBtn.setImageResource(R.drawable.number_btn);
			mKitchenBtn.setImageResource(R.drawable.kitchen);
			mSpellBtn.setImageResource(R.drawable.pinyin);
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
			default:
				
			}
			editor.commit();
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pick_food);
		
		mViewHandler = new ViewHandler(this);
		
		// 取得新点菜中已有的菜品List，并保存到pickFood的List中
//		OrderParcel orderParcel = getIntent().getParcelableExtra(OrderParcel.KEY_VALUE);
//		for (int i = 0; i < orderParcel.foods.length; i++) {
//			mPickFoods.add(orderParcel.foods[i]);
//		}
		//mTmpOrder = getIntent().getParcelableExtra(OrderParcel.KEY_VALUE);
		
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
//		if (_tempLstView != null) {
//			_pickFoods.addAll(_tempLstView.getSourceData());
//		}

		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(mTmpOrder));
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		super.onBackPressed();
	}
	
	//activity返回后将菜品添加进已点菜中
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == PICK_WITH_TASTE) {
				/**
				 * 添加口味后添加到pickList中
				 */
				FoodParcel foodParcel = data.getParcelableExtra(FoodParcel.KEY_VALUE);
				addFood(foodParcel);
			}
		}
	}
	/**
	 * 通过"编号"、"分厨"、"拼音"方式选中菜品后， 将菜品保存到List中，退出时将此List作为结果返回到上一个Activity
	 * 
	 * @param food
	 *            选中菜品的信息
	 */
	@Override
	public void onPicked(OrderFood food) {
		addFood(food);
	}

	/**
	 * 通过"编号"、"分厨"、"拼音"方式选中菜品后， 将菜品保存到List中，并跳转到口味Activity选择口味
	 * 
	 * @param food
	 *            选中菜品的信息
	 */
	@Override
	public void onPickedWithTaste(OrderFood food) {
		Intent intent = new Intent(this, PickTasteActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(food));
		intent.putExtras(bundle);
		startActivityForResult(intent, PICK_WITH_TASTE);
	}
	
	/**
	 * 添加菜品到已点菜的List中
	 * 
	 * @param food
	 *            选中的菜品信息
	 */
	private void addFood(OrderFood food) {
		try{
			mTmpOrder.addFood(food);
			
			Toast.makeText(this, "添加"	+ (food.hangStatus == OrderFood.FOOD_HANG_UP ? "并叫起\"" : "\"") + food.toString() + "\"" +
								 Util.float2String2(food.getCount()) + "份", Toast.LENGTH_SHORT)	.show();
			
		}catch(BusinessException e){
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
}
