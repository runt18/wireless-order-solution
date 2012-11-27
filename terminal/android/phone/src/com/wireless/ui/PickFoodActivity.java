package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

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
import com.wireless.fragment.TempFoodFragment;
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;

public class PickFoodActivity extends FragmentActivity implements 
			com.wireless.fragment.PickFoodFragment.OnFoodPickedListener, 
			com.wireless.fragment.KitchenFragment.OnFoodPickedListener,
			com.wireless.fragment.TempFoodFragment.OnFoodPickedListener
{
	
	//ÿ����˷�ʽ�ı�ǩ
	private static final int NUMBER_FRAGMENT = 1320;	//���
	private static final int KITCHEN_FRAGMENT = 1321;	//�ֳ�
	private static final int PINYIN_FRAGMENT = 1322;	//ƴ��
	private static final int TEMP_FOOD_FRAGMENT = 1323;

	private static final int DEFAULT_FRAGMENT = PINYIN_FRAGMENT;
	
	private int mCurFg = -1;
	
	//activity���ر�ǩ
	private static final int PICK_WITH_TASTE = 6755;
	public static final String TEMP_FOOD_FRAGMENT_TAG = "tempFoodFragmentTag";

	private ViewHandler mViewHandler;
	
	//�����ѵ�˵��б�
	//private ArrayList<OrderFood> mPickFoods = new ArrayList<OrderFood>();
	
	//ͨ����ʱ�˵������µ��
	private Order mTmpOrder = new Order();;

	private static class ViewHandler extends Handler{
		private WeakReference<PickFoodActivity> mActivity;
		private TextView mTitleTextView;
		private ImageButton mNumBtn;
		private ImageButton mKitchenBtn;
		private ImageButton mSpellBtn;
		private ImageButton mTempBtn;
		
		ViewHandler(PickFoodActivity activity)
		{
			mActivity = new WeakReference<PickFoodActivity>(activity);
			mTitleTextView = (TextView) activity.findViewById(R.id.toptitle);
			mTitleTextView.setVisibility(View.VISIBLE);
			
			mNumBtn = (ImageButton) activity.findViewById(R.id.imageButton_num_pickFood);
			mKitchenBtn = (ImageButton) activity.findViewById(R.id.imageButton_kitchen_pickFood);
			mSpellBtn = (ImageButton) activity.findViewById(R.id.imageButton_spell_pickFood);
			mTempBtn = (ImageButton) activity.findViewById(R.id.imageButton_tempFood_pickFood);
		}
		
		@Override
		public void handleMessage(Message msg) {
			PickFoodActivity activity = mActivity.get();
			FragmentTransaction fgTrans = activity.getSupportFragmentManager().beginTransaction();

			switch(msg.what){
			
			case NUMBER_FRAGMENT:
				if(mActivity.get().mCurFg != NUMBER_FRAGMENT){
					//�����²�Ʒѡ��fragment
					PickFoodFragment numFragment = new PickFoodFragment();
					numFragment.setFoodPickedListener(activity);
					//������ʾ����
					Bundle args = new Bundle();
					args.putInt(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG, PickFoodFragment.PICK_FOOD_FRAGMENT_NUMBER);
					args.putString(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG_NAME, "������������");
					numFragment.setArguments(args);
					//�滻ԭ����fragment
					fgTrans.replace(R.id.frameLayout_container_pickFood, numFragment).commit();
					
					mTitleTextView.setText("��� - ���");
					setLastCate(NUMBER_FRAGMENT);
					mActivity.get().mCurFg = NUMBER_FRAGMENT;
				}
				break;
				
			case KITCHEN_FRAGMENT:
				if(mActivity.get().mCurFg != KITCHEN_FRAGMENT){
					KitchenFragment kitchenFragment = new KitchenFragment();
					kitchenFragment.setFoodPickedListener(activity);
					fgTrans.replace(R.id.frameLayout_container_pickFood, kitchenFragment).commit();
					
					mTitleTextView.setText("��� - �ֳ�");
					setLastCate(KITCHEN_FRAGMENT);
					mActivity.get().mCurFg = KITCHEN_FRAGMENT;
				}
				break;
				
			case PINYIN_FRAGMENT:
				if(mActivity.get().mCurFg != PINYIN_FRAGMENT){
					//�����²�Ʒѡ��fragment
					PickFoodFragment spellFragment = new PickFoodFragment();
					spellFragment.setFoodPickedListener(activity);
					//������ʾ����
					Bundle spellArgs = new Bundle();
					spellArgs.putInt(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG, PickFoodFragment.PICK_FOOD_FRAGMENT_SPELL);
					spellArgs.putString(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG_NAME, "������ƴ������");
					spellFragment.setArguments(spellArgs);
					//�滻ԭ����fragment
					fgTrans.replace(R.id.frameLayout_container_pickFood, spellFragment).commit();
					
					mTitleTextView.setText("��� - ƴ��");
					setLastCate(PINYIN_FRAGMENT);
					mActivity.get().mCurFg = PINYIN_FRAGMENT;
				}
				break;
			case TEMP_FOOD_FRAGMENT:
				if(mActivity.get().mCurFg != TEMP_FOOD_FRAGMENT){
					TempFoodFragment tempFgm = new TempFoodFragment();
					tempFgm.setFoodPickedListener(activity);
					fgTrans.replace(R.id.frameLayout_container_pickFood, tempFgm, TEMP_FOOD_FRAGMENT_TAG).commit();
					
					mTitleTextView.setText("��� - ��ʱ��");
					setLastCate(TEMP_FOOD_FRAGMENT);
					mActivity.get().mCurFg = TEMP_FOOD_FRAGMENT;
				}
				break;
			}
		}
		
		private void setLastCate(int cate){
			PickFoodActivity activity = mActivity.get();
			//��ԭ����ʽ
			mNumBtn.setImageResource(R.drawable.number_btn);
			mKitchenBtn.setImageResource(R.drawable.kitchen);
			mSpellBtn.setImageResource(R.drawable.pinyin);
			mTempBtn.setImageResource(R.drawable.linshicai);

			//�л���˷�ʽʱ�����浱ǰ�ĵ��ģʽ
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
			case TEMP_FOOD_FRAGMENT:
				//TODO ���һ���ֶ�
				editor.putInt(Params.LAST_PICK_CATE, 5);
				mTempBtn.setImageResource(R.drawable.linshicai_down);
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
		
		// ȡ���µ�������еĲ�ƷList�������浽pickFood��List��
//		OrderParcel orderParcel = getIntent().getParcelableExtra(OrderParcel.KEY_VALUE);
//		for (int i = 0; i < orderParcel.foods.length; i++) {
//			mPickFoods.add(orderParcel.foods[i]);
//		}
		//mTmpOrder = getIntent().getParcelableExtra(OrderParcel.KEY_VALUE);
		
		//����Button
		TextView left = (TextView) findViewById(R.id.textView_left);
		left.setText("����");
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

		//���
		((ImageButton) findViewById(R.id.imageButton_num_pickFood)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(NUMBER_FRAGMENT);
			}
		});
		//�ֳ�
		((ImageButton) findViewById(R.id.imageButton_kitchen_pickFood)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(KITCHEN_FRAGMENT);
			}
		});
		
		//ƴ��
		((ImageButton) findViewById(R.id.imageButton_spell_pickFood)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(PINYIN_FRAGMENT);
			}
		});
		//��ʱ��
		((ImageButton) findViewById(R.id.imageButton_tempFood_pickFood)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(TEMP_FOOD_FRAGMENT);
			}
		});

		/**
		 * �����ϴα���ļ�¼���л�����Ӧ�ĵ�˷�ʽ
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
	 * ����ʱ���µ��Ʒ��List���ص���һ��Activity
	 */
	@Override
	public void onBackPressed() {
		// Add the temporary foods to the picked food list
		// except the ones without food name
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		
		TempFoodFragment fgm = (TempFoodFragment) getSupportFragmentManager().findFragmentByTag(TEMP_FOOD_FRAGMENT_TAG);
		if(fgm != null){
			ArrayList<OrderFood> tempFoods = fgm.getValidTempFood();
			for(OrderFood f:tempFoods)
			{
				try {
					mTmpOrder.addFood(f);
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
	
	//activity���غ󽫲�Ʒ��ӽ��ѵ����
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == PICK_WITH_TASTE) {
				/**
				 * ��ӿ�ζ����ӵ�pickList��
				 */
				FoodParcel foodParcel = data.getParcelableExtra(FoodParcel.KEY_VALUE);
				addFood(foodParcel);
			}
		}
	}
	/**
	 * ͨ��"���"��"�ֳ�"��"ƴ��"��ʽѡ�в�Ʒ�� ����Ʒ���浽List�У��˳�ʱ����List��Ϊ������ص���һ��Activity
	 * 
	 * @param food
	 *            ѡ�в�Ʒ����Ϣ
	 */
	@Override
	public void onPicked(OrderFood food) {
		addFood(food);
	}

	/**
	 * ͨ��"���"��"�ֳ�"��"ƴ��"��ʽѡ�в�Ʒ�� ����Ʒ���浽List�У�����ת����ζActivityѡ���ζ
	 * 
	 * @param food
	 *            ѡ�в�Ʒ����Ϣ
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
	 * ��Ӳ�Ʒ���ѵ�˵�List��
	 * 
	 * @param food
	 *            ѡ�еĲ�Ʒ��Ϣ
	 */
	private void addFood(OrderFood food) {
		try{
			mTmpOrder.addFood(food);
			
			Toast.makeText(this, "���"	+ (food.hangStatus == OrderFood.FOOD_HANG_UP ? "������\"" : "\"") + food.toString() + "\"" +
								 Util.float2String2(food.getCount()) + "��", Toast.LENGTH_SHORT)	.show();
			
		}catch(BusinessException e){
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
}
