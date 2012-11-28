package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.excep.BusinessException;
import com.wireless.fragment.PickTasteFragment;
import com.wireless.fragment.PickTasteFragment.OnTasteChangeListener;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Util;
import com.wireless.util.ImageDialog;
import com.wireless.util.ShadowImageView;
import com.wireless.util.imgFetcher.ImageFetcher;

public class FoodDetailActivity extends Activity implements OnTasteChangeListener, OnDismissListener{
	private static final int ORDER_FOOD_CHANGED = 234841;
	private static final String RECOMMEND_DIALOG = "recommend_dialog";

	private OrderFood mOrderFood;
	
	private DisplayHandler mDisplayHandler;
	private ImageView mFoodImageView;
	private ImageFetcher mImageFetcher;
	
	private Food mShowingFood;
	
	/*
	 * 显示该菜品详细情况的handler
	 * 当菜品改变时改变显示
	 */
	private static class DisplayHandler extends Handler{
		private WeakReference<FoodDetailActivity> mActivity;
		private TextView mFoodNameTextView;
		private TextView mFoodPriceTextView;
		private TextView mTasteTextView;
		private TextView mPinzhuTextView;
//		private TextView mPriceText;
		private View mTempTasteView;
//		private TextView mIntroTextView;

		DisplayHandler(FoodDetailActivity activity)
		{
			mActivity =  new WeakReference<FoodDetailActivity>(activity);
//			mPriceText = (TextView) activity.findViewById(R.id.textView_price_foodDetail);
			mTempTasteView = activity.findViewById(R.id.relativeLayout_foodDetail_tempTaste);
			
//			mIntroTextView = (TextView)activity.findViewById(R.id.textView_foodDetail_intro);
		}
		
		@Override
		public void handleMessage(Message msg)
		{
			FoodDetailActivity activity = mActivity.get();
			/*
			 * 初始化各个view
			 */
			if(mFoodNameTextView == null)
				mFoodNameTextView = (TextView) activity.findViewById(R.id.textView_foodName_foodDetail);
			if(mFoodPriceTextView == null)
				mFoodPriceTextView = (TextView) activity.findViewById(R.id.textView_foodDetail_price);
			if(mTasteTextView == null)
				mTasteTextView = (TextView) activity.findViewById(R.id.textView_foodDetail_taste);
			if(mPinzhuTextView == null)
				mPinzhuTextView = (TextView) activity.findViewById(R.id.textView_foodDetail_tempFood);

			switch(msg.what)
			{
			/*
			 * 当口味改变时改变显示
			 */
			case ORDER_FOOD_CHANGED:
				mFoodNameTextView.setText(activity.mOrderFood.name);
				mFoodPriceTextView.setText(Util.float2String2(activity.mOrderFood.getPriceWithTaste()));
				if(activity.mOrderFood.hasNormalTaste()){
					mTasteTextView.setText(activity.mOrderFood.getTasteGroup().getNormalTastePref());					
				}else{
					mTasteTextView.setText("");
				}
				
				if(activity.mOrderFood.hasTmpTaste()){
					mPinzhuTextView.setText(activity.mOrderFood.getTasteGroup().getTmpTastePref());
					mTempTasteView.setVisibility(View.VISIBLE);
				}else{
					mPinzhuTextView.setText("");
					mTempTasteView.setVisibility(View.INVISIBLE);
				}
				
//				if(activity.mOrderFood.)
				break;
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.food_detail);
		
		FoodParcel foodParcel = getIntent().getParcelableExtra(FoodParcel.KEY_VALUE);
		mOrderFood = foodParcel;
		if(!mOrderFood.hasTaste())
		{
			mOrderFood.makeTasteGroup();
			mOrderFood.getTasteGroup().addTaste(WirelessOrder.foodMenu.specs[2]);
		}
		
		mDisplayHandler = new DisplayHandler(this);
		mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
		

		//显示该菜品的主图
		mFoodImageView = (ImageView) findViewById(R.id.imageView_foodDetail);
		mFoodImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		
		mImageFetcher = new ImageFetcher(this, 600, 400);

		final ImageFetcher imgFetcher = new ImageFetcher(this, 600, 400);
		mFoodImageView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if(mFoodImageView.getHeight() > 0)
				{
					imgFetcher.setImageSize(mFoodImageView.getWidth(), mFoodImageView.getHeight());
					imgFetcher.loadImage(mOrderFood.image, mFoodImageView);
					mFoodImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		});
		
		//点菜按钮
		((ImageButton)findViewById(R.id.imageButton_addFood_foodDetail)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				float oriCnt = mOrderFood.getCount();
				try{
					mOrderFood.setCount(Float.parseFloat(((TextView) findViewById(R.id.editText_count_foodDetail)).getText().toString()));
					ShoppingCart.instance().addFood(mOrderFood);
					onBackPressed();
				}catch(BusinessException e){
					mOrderFood.setCount(oriCnt);
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		final EditText countEditText = (EditText) findViewById(R.id.editText_count_foodDetail);
//		final TextView mFoodPriceTextView = (TextView) findViewById(R.id.textView_foodDetail_price);	

		countEditText.setText(Util.float2String2(mOrderFood.getCount()));
		//增加数量的按钮
		((ImageButton) findViewById(R.id.imageButton_plus_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(!countEditText.getText().toString().equals(""))
				{
					float curNum = Float.parseFloat(countEditText.getText().toString());
					countEditText.setText(Util.float2String2(++curNum));
					mOrderFood.setCount(curNum);
//					mFoodPriceTextView.setText(Util.float2String2(mOrderFood.getCount() * mOrderFood.getPriceWithTaste()));
				}
			}
		});

		//减少数量的按钮
		((ImageButton) findViewById(R.id.imageButton_minus_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(!countEditText.getText().toString().equals(""))
				{
					float curNum = Float.parseFloat(countEditText.getText().toString());
					if(--curNum >= 1)
					{
						countEditText.setText(Util.float2String2(curNum));
						mOrderFood.setCount(curNum);
//						mFoodPriceTextView.setText(Util.float2String2(mOrderFood.getCount() * mOrderFood.getPriceWithTaste()));
						
					}
				}
			}
		});
		//打开口味选择对话框
		((ImageButton) findViewById(R.id.button_pickTaste_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showDialog(PickTasteFragment.FOCUS_TASTE, mOrderFood);
			}
		});
		//品注按钮
		((ImageButton) findViewById(R.id.button_foodDetail_tempTaste)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				final EditText tempEditText = new EditText(FoodDetailActivity.this);
				tempEditText.setSingleLine();
				if(mOrderFood.hasTmpTaste())
				{
					tempEditText.setText(mOrderFood.getTasteGroup().getTmpTastePref());
					tempEditText.selectAll();
				}
				
				new AlertDialog.Builder(FoodDetailActivity.this).setTitle("请输入品注:")
					.setView(tempEditText)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							if(!mOrderFood.hasTaste()){
								mOrderFood.makeTasteGroup();
							}
							if(!tempEditText.getText().toString().equals(""))
							{
								Taste tmpTaste = new Taste();
								tmpTaste.setPreference(tempEditText.getText().toString());
								mOrderFood.getTasteGroup().setTmpTaste(tmpTaste);
							} else {
								mOrderFood.getTasteGroup().setTmpTaste(null);
							}
							
							onTasteChanged(mOrderFood);
						}
					})
					.setNegativeButton("取消", null).show();
//				showDialog(PickTasteFragment.FOCUS_NOTE, mOrderFood);
			}
		});
//		//清空品注
//		((ImageButton) findViewById(R.id.button_removeAllTaste)).setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				mOrderFood.clearTasetGroup();
//				mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
//			}
//		});
		//规格
		((RadioGroup) findViewById(R.id.radioGroup_foodDetail)).setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				//tasteGroup没有创建则先创建
				if(!mOrderFood.hasTaste()){
					mOrderFood.makeTasteGroup();
				} 
				//清楚旧规格
				for(Taste spec : WirelessOrder.foodMenu.specs){
					mOrderFood.getTasteGroup().removeTaste(spec);
				}
				
				switch(checkedId)
				{
				case R.id.radio0:
					mOrderFood.getTasteGroup().addTaste(WirelessOrder.foodMenu.specs[0]);
					break;
				case R.id.radio1:
					mOrderFood.getTasteGroup().addTaste(WirelessOrder.foodMenu.specs[1]);
					break;
				case R.id.radio2:
					mOrderFood.getTasteGroup().addTaste(WirelessOrder.foodMenu.specs[2]);
					break;
				}
				mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
			}
		});
		
//		//设置两个tab
//		TabHost mTabHost = (TabHost) findViewById(R.id.tabhost_foodDetail);
//		mTabHost.setup();
//		
//		mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator("基本").setContent(R.id.tab1_foodDetail));
//		mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator("其它").setContent(R.id.tab2_foodDetail));
		
		//设置底部推荐菜的数据和显示
		ArrayList<Food> mRecommendfoods = new ArrayList<Food>();
		for(Food f:WirelessOrder.foods)
		{
			if(f.isRecommend())
				mRecommendfoods.add(f);
		} 
		
		mImageFetcher.setImageSize(300, 300);
		LayoutParams lp = new LayoutParams(245, 160);
		//推荐菜层
		LinearLayout linearLyaout = (LinearLayout) findViewById(R.id.linearLayout_foodDetail);
		for(final Food f:mRecommendfoods)
		{
			ShadowImageView image = new ShadowImageView(this);
			image.setPadding(0, 0, 3, 3);
			image.setLayoutParams(lp);
			image.setScaleType(ScaleType.CENTER_CROP);
			mImageFetcher.loadImage(f.image, image);
			linearLyaout.addView(image);
			//设置推荐菜点击侦听
			image.setOnClickListener(new FoodDetailOnClickListener(f));
		}
	}
	
	@Override 
	public void onDestroy(){
		super.onDestroy();
		mImageFetcher.clearCache();
	}
	
	protected void showDialog(String tab, final OrderFood f) {
		//设置推荐菜对话框或口味选择对话框
		if(tab == RECOMMEND_DIALOG)
		{
			if(mShowingFood == null || f.getAliasId() != mShowingFood.getAliasId())
			{
				ImageDialog dialog = new ImageDialog(this,android.R.style.Theme_Holo_Light_Dialog_NoActionBar, f);
				dialog.setOnDismissListener(this);
				dialog.show();
				mShowingFood = f;
			}
		} else{
			PickTasteFragment pickTasteFg = new PickTasteFragment();
			pickTasteFg.setOnTasteChangeListener(this);
			Bundle args = new Bundle();
			args.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(f));
			pickTasteFg.setArguments(args);
			pickTasteFg.show(getFragmentManager(), tab);
		}
	}

	@Override
	public void onTasteChanged(OrderFood food) {
		mOrderFood = food;
		mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
	}
	
	class FoodDetailOnClickListener implements OnClickListener{
		Food mFood;
		public FoodDetailOnClickListener(Food mFood) {
			this.mFood = mFood;
		}
		@Override
		public void onClick(View v) {
			showDialog(RECOMMEND_DIALOG, new OrderFood(mFood));
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		mShowingFood = null;
	}
}
