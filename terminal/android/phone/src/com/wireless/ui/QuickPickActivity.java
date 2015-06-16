package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
import android.view.inputmethod.InputMethodManager;
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
import com.wireless.parcel.WxOrderParcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.weixin.order.WxOrder;
import com.wireless.ui.dialog.AskOrderAmountDialog.ActionType;
import com.wireless.ui.dialog.AskOrderAmountDialog.OnFoodPickedListener;
import com.wireless.ui.dialog.QuickPickCommitDialog;

public class QuickPickActivity extends FragmentActivity implements OnFoodPickedListener, 
																   OnButtonClickedListener,
																   OnOrderChangedListener{
	//ÿ����˷�ʽ�ı�ǩ
	private static final int NUMBER_FRAGMENT = 6320;
	private static final int KITCHEN_FRAGMENT = 6321;
	private static final int PINYIN_FRAGMENT = 6322;
	private static final int ORDER_FOOD_FRAGMENT = 6323;
	private int mLastView;

	
	//ˢ��ÿ��view��handler
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
			mTitleTextView = (TextView) activity.findViewById(R.id.txtView_centralTitle_topBar);
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
				//��ʾ���ѡ���Ʒ��Fragment
				activity.findViewById(R.id.frameLayout_container_quickPick).setVisibility(View.VISIBLE);
				activity.findViewById(R.id.frameLayout_orderFood_quickPick).setVisibility(View.INVISIBLE);
				fgmTrans.replace(R.id.frameLayout_container_quickPick, PickFoodFragment.newInstanceByNum(), Integer.toString(NUMBER_FRAGMENT)).commit();
				
				activity.mLastView = NUMBER_FRAGMENT;
				mTitleTextView.setText("��� - ���");
				setLastCate(NUMBER_FRAGMENT);
				
				break;
				
			case KITCHEN_FRAGMENT:
				
				//��ʾ�ֳ�ѡ���Ʒ��Fragment
				activity.findViewById(R.id.frameLayout_container_quickPick).setVisibility(View.VISIBLE);
				activity.findViewById(R.id.frameLayout_orderFood_quickPick).setVisibility(View.INVISIBLE);
				fgmTrans.replace(R.id.frameLayout_container_quickPick, new KitchenFragment(), Integer.toString(KITCHEN_FRAGMENT)).commit();
				
				activity.mLastView = KITCHEN_FRAGMENT;
				mTitleTextView.setText("��� - �ֳ�");
				setLastCate(KITCHEN_FRAGMENT);
				break;
				
			case PINYIN_FRAGMENT:
				//��ʾƴ��ѡ���Ʒ��Fragment
				activity.findViewById(R.id.frameLayout_container_quickPick).setVisibility(View.VISIBLE);
				activity.findViewById(R.id.frameLayout_orderFood_quickPick).setVisibility(View.INVISIBLE);
				fgmTrans.replace(R.id.frameLayout_container_quickPick, PickFoodFragment.newInstanceByPinyin(), Integer.toString(PINYIN_FRAGMENT)).commit();
				
				activity.mLastView = PINYIN_FRAGMENT;
				mTitleTextView.setText("��� - ƴ��");
				setLastCate(PINYIN_FRAGMENT);
				break;
				
			case ORDER_FOOD_FRAGMENT:
				//����OrderFoodFragment
				activity.findViewById(R.id.frameLayout_container_quickPick).setVisibility(View.INVISIBLE);
				activity.findViewById(R.id.frameLayout_orderFood_quickPick).setVisibility(View.VISIBLE);
				
				activity.mLastView = ORDER_FOOD_FRAGMENT;
				mTitleTextView.setText("�ѵ��");
				setLastCate(ORDER_FOOD_FRAGMENT);
				break;
			}
		}
		
		private void setLastCate(int cate){
			
			QuickPickActivity activity = mActivity.get();
			//��ԭ����ʽ
			mNumBtn.setImageResource(R.drawable.number_btn);
			mKitchenBtn.setImageResource(R.drawable.kitchen);
			mSpellBtn.setImageResource(R.drawable.pinyin);
			mPickedBtn.setImageResource(R.drawable.picked_food);
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
		setContentView(R.layout.quick_pick_activity);
		
		mViewHandler = new ViewHandler(this);
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		
		//����Button
		TextView left = (TextView) findViewById(R.id.txtView_leftBtn_topBar);
		left.setText("����");
		left.setVisibility(View.VISIBLE);
		
		ImageButton back = (ImageButton) findViewById(R.id.imageButton_left_topBar);
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		//�ύ��ť
		TextView right = (TextView) findViewById(R.id.txtView_rightBtn_topBar);
		right.setText("�ύ");
		right.setVisibility(View.VISIBLE);
		
		//����Ʒ��ӵ�"�ѵ��"
//		OrderParcel orderParcel = getIntent().getParcelableExtra(OrderParcel.KEY_VALUE);
//		if(orderParcel != null){
//			((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).addFoods(orderParcel.asOrder().getOrderFoods());
//		}
		
		final List<WxOrder> wxOrders = new ArrayList<WxOrder>();
		List<WxOrderParcel> wxOrderParcels = getIntent().getParcelableArrayListExtra(WxOrderParcel.KEY_VALUE);
		if(wxOrderParcels != null){
			for(WxOrderParcel parcel : wxOrderParcels){
				((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).addFoods(parcel.asWxOrder().getFoods());
				wxOrders.add(parcel.asWxOrder());
			}
		}
		
		ImageButton commit = (ImageButton) findViewById(R.id.imageButton_right_topBar);
		commit.setVisibility(View.VISIBLE);
		commit.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//��δ��ˣ�����ʾ��
				if(((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).hasNewOrderFood()){
					Order reqOrder = new Order(0);
					for(WxOrder wxOrder : wxOrders){
						reqOrder.addWxOrder(wxOrder);
					}
					reqOrder.setOrderFoods(((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).getNewFoods());
					QuickPickCommitDialog.newCommitDialog(reqOrder).show(getSupportFragmentManager(), QuickPickCommitDialog.TAG);
				}else{
					Toast.makeText(getApplicationContext(), "����δ���", Toast.LENGTH_SHORT).show();
				}
			}
		});

		//���
		((ImageButton) findViewById(R.id.imageButton_num_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mLastView != NUMBER_FRAGMENT){
					mViewHandler.sendEmptyMessage(NUMBER_FRAGMENT);
				}
			}
		});
		
		//�ֳ�
		((ImageButton) findViewById(R.id.imageButton_kitchen_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mLastView != KITCHEN_FRAGMENT){
					mViewHandler.sendEmptyMessage(KITCHEN_FRAGMENT);
				}
			}
		});
		
		//ƴ��
		((ImageButton) findViewById(R.id.imageButton_spell_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mLastView != PINYIN_FRAGMENT){
					mViewHandler.sendEmptyMessage(PINYIN_FRAGMENT);
				}
			}
		});
		
		//�ѵ��
		((ImageButton) findViewById(R.id.imageButton_remark_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(ORDER_FOOD_FRAGMENT);
			}
		});

		if(((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).hasNewOrderFood()){
			mViewHandler.sendEmptyMessage(ORDER_FOOD_FRAGMENT);
		}else{
			switchToOrderView();
		}
	}

	@Override
	public void onBackPressed() {
		
		if(((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).hasNewOrderFood()){
			new AlertDialog.Builder(QuickPickActivity.this)
				.setTitle("�˳�ȷ��")
				.setMessage("�ѵ����δ�ύ��ȷ��Ҫ�˳���")
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){
					@Override
					public void onClick( DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				})
				.setNegativeButton("ȡ��", null).show();
		}else{
			super.onBackPressed();
		}
	}

	private void switchToOrderView(){
		//�����ϴα���ļ�¼���л�����Ӧ�ĵ�˷�ʽ
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
	public void onFoodPicked(OrderFood food, ActionType type) {
		if(type == ActionType.ADD){
			if(mLastView == NUMBER_FRAGMENT){
				final View srchView = findViewById(R.id.editText_search_pickFoodFragment);
				srchView.postDelayed(new Runnable(){
					@Override
					public void run() {
						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(srchView, InputMethodManager.SHOW_IMPLICIT);
					}
				}, 500);
			}
			((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).addFood(food);
		}
	}

	@Override
	public void onPickFoodClicked() {
		switchToOrderView();		
	}

	@Override
	public void onOrderChanged(Order oriOrder, List<OrderFood> newFoodList) {
		
		TextView txtViewAmount = (TextView)findViewById(R.id.txtView_amount_right_topBar);

		if(newFoodList.isEmpty()){
			txtViewAmount.setVisibility(View.GONE);
		}else{
			txtViewAmount.setVisibility(View.VISIBLE);
			txtViewAmount.setText(Integer.toString(newFoodList.size()));
		}
	}

}

