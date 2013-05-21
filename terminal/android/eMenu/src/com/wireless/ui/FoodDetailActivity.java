package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.excep.ProtocolException;
import com.wireless.fragment.PickTasteFragment;
import com.wireless.fragment.PickTasteFragment.OnTasteChangeListener;
import com.wireless.ordermenu.R;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.Food;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.util.ImageDialog;
import com.wireless.util.ShadowImageView;
import com.wireless.util.imgFetcher.ImageFetcher;

/**
 * 该activity显示菜品详情和底部的推荐菜
 * @author ggdsn1
 *
 */
@SuppressWarnings("deprecation")
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
		private View mTempTasteView;

		DisplayHandler(FoodDetailActivity activity)
		{
			mActivity =  new WeakReference<FoodDetailActivity>(activity);
			mTempTasteView = activity.findViewById(R.id.relativeLayout_foodDetail_tempTaste);
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
				mFoodNameTextView.setText(activity.mOrderFood.getName());
				mFoodPriceTextView.setText(NumericUtil.float2String2(activity.mOrderFood.getUnitPriceWithTaste()));
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
				
				if(activity.mOrderFood.getTasteGroup() != null)
				{
					if(activity.mOrderFood.getTasteGroup().hasSpec())
					{
						List<Taste> specs = activity.mOrderFood.getTasteGroup().getSpecs();
						if(WirelessOrder.foodMenu.specs.contains(specs.get(0))){
							for(int i = 0; i < WirelessOrder.foodMenu.specs.size(); i++){
								if(specs.get(0).equals(WirelessOrder.foodMenu.specs.get(i)))
								{
									((RadioButton)((RadioGroup) activity.findViewById(R.id.radioGroup_foodDetail)).getChildAt(i)).setChecked(true);
									break;
								}
							}
						}
					}
				}
				break;
			}
		}
	}
	
	/**
	 * 初始化各个控件和菜品主图
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.food_detail);
		
		OrderFoodParcel foodParcel = getIntent().getParcelableExtra(OrderFoodParcel.KEY_VALUE);
		mOrderFood = foodParcel.asOrderFood();
		mOrderFood.setCount(1f);
		if(!mOrderFood.hasTaste())
		{
			mOrderFood.makeTasteGroup();
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
					imgFetcher.loadImage(mOrderFood.asFood().getImage(), mFoodImageView);
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
				}catch(ProtocolException e){
					mOrderFood.setCount(oriCnt);
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		final EditText countEditText = (EditText) findViewById(R.id.editText_count_foodDetail);

		countEditText.setText(NumericUtil.float2String2(mOrderFood.getCount()));
		//增加数量的按钮
		((ImageButton) findViewById(R.id.imageButton_plus_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(!countEditText.getText().toString().equals(""))
				{
					float curNum = Float.parseFloat(countEditText.getText().toString());
					countEditText.setText(NumericUtil.float2String2(++curNum));
					mOrderFood.setCount(curNum);
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
						countEditText.setText(NumericUtil.float2String2(curNum));
						mOrderFood.setCount(curNum);
						
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
				//弹出品注对话框
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
			}
		});
		//规格
		((RadioGroup) findViewById(R.id.radioGroup_foodDetail)).setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				//tasteGroup没有创建则先创建
				if(!mOrderFood.hasTaste()){
					mOrderFood.makeTasteGroup();
				} 
				//清除旧规格
				for(Taste spec : WirelessOrder.foodMenu.specs){
					mOrderFood.getTasteGroup().removeTaste(spec);
				}
				//设置新规格
				switch(checkedId)
				{
				case R.id.radio0:
					mOrderFood.getTasteGroup().addTaste(WirelessOrder.foodMenu.specs.get(0));
					break;
				case R.id.radio1:
					mOrderFood.getTasteGroup().addTaste(WirelessOrder.foodMenu.specs.get(1));
					break;
				case R.id.radio2:
					break;
				}
				mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
			}
		});
		
		//设置底部推荐菜的数据和显示
		ArrayList<Food> mRecommendfoods = new ArrayList<Food>();
		for(Food f:WirelessOrder.foods)
		{
			if(f.isRecommend())
				mRecommendfoods.add(f);
		} 
		
		mImageFetcher.setImageSize(245, 160);
		LayoutParams lp = new LayoutParams(245, 160);
		
		//according to the resolution, display different size
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		switch(dm.densityDpi){
		case DisplayMetrics.DENSITY_LOW:
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			//use default properties
			break;
		case DisplayMetrics.DENSITY_HIGH:
			lp.width = 306;
			lp.height = 167;
			break;
		case DisplayMetrics.DENSITY_XHIGH: 
			lp.width = 490;
			lp.height = 320;
			break;
		}
		mImageFetcher.setImageSize(lp.width, lp.height);

		//推荐菜层
		LinearLayout linearLyaout = (LinearLayout) findViewById(R.id.linearLayout_foodDetail);
		for(final Food f:mRecommendfoods)
		{
			ShadowImageView image = new ShadowImageView(this);
			image.setPadding(0, 0, 3, 3);
			image.setLayoutParams(lp);
			image.setScaleType(ScaleType.CENTER_CROP);
			mImageFetcher.loadImage(f.getImage(), image);
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
	
	private void showDialog(String tab, final OrderFood f) {
		//设置推荐菜对话框 或 口味选择对话框
		if(tab == RECOMMEND_DIALOG)
		{
			if(mShowingFood == null || f.getAliasId() != mShowingFood.getAliasId())
			{
				ImageDialog dialog = new ImageDialog(this,android.R.style.Theme_Holo_Light_Dialog_NoActionBar, f.asFood());
				dialog.setOnDismissListener(this);
				dialog.show();
				mShowingFood = f.asFood();
			}
		} else{
			//口味选择对话框
			PickTasteFragment pickTasteFg = new PickTasteFragment();
			pickTasteFg.setOnTasteChangeListener(this);
			Bundle args = new Bundle();
			args.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(f));
			pickTasteFg.setArguments(args);
			pickTasteFg.show(getFragmentManager(), tab);
		}
	}

	/**
	 * 当口味改变时改变显示
	 */
	@Override
	public void onTasteChanged(OrderFood food) {
		mOrderFood = food;
		mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
	}
	
	//底部推荐菜的点击侦听
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
