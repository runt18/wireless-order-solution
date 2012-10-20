package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.fragment.GalleryFragment.OnPicClickListener;
import com.wireless.fragment.PickTasteFragment;
import com.wireless.fragment.PickTasteFragment.OnTasteChangeListener;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.util.imgFetcher.ImageFetcher;

public class FoodDetailActivity extends Activity implements OnTasteChangeListener, OnPicClickListener{
	private static final int ORDER_FOOD_CHANGED = 234841;
	private static final String RECOMMEND_DIALOG = "recommend_dialog";

	private OrderFood mOrderFood;
	
	private DisplayHandler mDisplayHandler;
	private ArrayList<Food> mRecommendfoods;
	private ImageView mFoodImageView;
	private ImageFetcher mImageFetcher;
	
//	private GalleryFragment mRecFoodsFragment;

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

		DisplayHandler(FoodDetailActivity activity)
		{
			mActivity =  new WeakReference<FoodDetailActivity>(activity);
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
				mFoodPriceTextView = (TextView) activity.findViewById(R.id.textView_price_foodDetail);
			if(mTasteTextView == null)
				mTasteTextView = (TextView) activity.findViewById(R.id.textView_pickedTaste_foodDetail);
			if(mPinzhuTextView == null)
				mPinzhuTextView = (TextView) activity.findViewById(R.id.textView_pinzhu_foodDetail);

			switch(msg.what)
			{
			/*
			 * 当口味改变时改变显示
			 */
			case ORDER_FOOD_CHANGED:
				mFoodNameTextView.setText(activity.mOrderFood.name);
				mFoodPriceTextView.setText("" + activity.mOrderFood.getPriceWithTaste());
				if(activity.mOrderFood.hasNormalTaste()){
					mTasteTextView.setText(activity.mOrderFood.getNormalTastePref());					
				}else{
					mTasteTextView.setText("");
				}
				
				if(activity.mOrderFood.hasTmpTaste()){
					mPinzhuTextView.setText(activity.mOrderFood.tmpTaste.getPreference());
				}else{
					mPinzhuTextView.setText("");
				}
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
		
		mDisplayHandler = new DisplayHandler(this);
		mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
		

		//显示该菜品的主图
		mFoodImageView = (ImageView) findViewById(R.id.imageView_foodDetail);
		mFoodImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		
//		BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inJustDecodeBounds = true;
//		BitmapFactory.decodeFile(mOrderFood.image, options);
//		int imageHeight = options.outHeight;
//		int imageWidth = options.outWidth;
		
		mImageFetcher = new ImageFetcher(this, 600, 400);

		mImageFetcher.loadImage(mOrderFood.image, mFoodImageView);
		
		mFoodImageView.setOnClickListener(new OnClickListener(){
			//点击主图进入全屏界面
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FoodDetailActivity.this ,FullScreenActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(mOrderFood));
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		
		//点菜按钮
		((ImageView)findViewById(R.id.imageButton_addDish_foodDetail)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mOrderFood.setCount(Float.parseFloat(((TextView) findViewById(R.id.editText_count_foodDetail)).getText().toString()));
				ShoppingCart.instance().addFood(mOrderFood);
				Toast.makeText(getApplicationContext(), mOrderFood.name + "已添加", Toast.LENGTH_SHORT).show();
			}
		});
		
		final EditText countEditText = (EditText) findViewById(R.id.editText_count_foodDetail);
		countEditText.setText(String.valueOf(mOrderFood.getCount()));
		//增加数量的按钮
		((ImageButton) findViewById(R.id.imageButton_plus_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				float curNum = Float.parseFloat(countEditText.getText().toString());
				countEditText.setText("" + ++curNum);
				mOrderFood.setCount(curNum);
			}
		});
		//减少数量的按钮
		((ImageButton) findViewById(R.id.imageButton_minus_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				float curNum = Float.parseFloat(countEditText.getText().toString());
				if(--curNum >= 0)
				{
					countEditText.setText("" + curNum);
					mOrderFood.setCount(curNum);
				}
			}
		});
		//打开菜品选择对话框
		((Button) findViewById(R.id.button_pickTaste_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				onPicClick(null,0);
//FIXME 弹出对话框
//				showDialog(PickTasteFragment.FOCUS_TASTE, 0);
			}
		});
		
		((Button) findViewById(R.id.button_pinzhu_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showDialog(PickTasteFragment.FOCUS_NOTE, 0);
			}
		});
		//清空品注
		((Button) findViewById(R.id.button_removeAllTaste)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mOrderFood.tastes.length > 0){
					for(Taste t : mOrderFood.tastes.clone())
					{	
						mOrderFood.removeTaste(t);
					}
					mOrderFood.tmpTaste = null;
					mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
				}
			}
		});
		//设置两个tab
		TabHost mTabHost = (TabHost) findViewById(R.id.tabhost_foodDetail);
		mTabHost.setup();
		
		mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator("基本").setContent(R.id.tab1_foodDetail));
		mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator("其它").setContent(R.id.tab2_foodDetail));
		
		//设置底部推荐菜的数据和显示
		mRecommendfoods = new ArrayList<Food>();
		for(Food f:WirelessOrder.foods)
		{
			if(f.isRecommend())
				mRecommendfoods.add(f);
		}
		
		mImageFetcher.setImageSize(160, 160);

		LinearLayout linearLyaout = (LinearLayout) findViewById(R.id.linearLayout_foodDetail);
		for(Food f:mRecommendfoods)
		{
			ImageView image = new ImageView(this);
			image.setScaleType(ScaleType.CENTER_CROP);
			mImageFetcher.loadImage(f.image, image);
			linearLyaout.addView(image);
		}
		/*
		 * FIXME 修正fragment的样式
		 */
//		mRecFoodsFragment = GalleryFragment.newInstance(mRecommendfoods.toArray(new Food[mRecommendfoods.size()]), 0.5f, 1, ScaleType.FIT_CENTER);
//		FragmentTransaction fragmentTransaction = this.getFragmentManager().beginTransaction();
//		fragmentTransaction.replace(R.id.viewPager_container_foodDetail, mRecFoodsFragment).commit();
		 
//		Gallery gallery = (Gallery) findViewById(R.id.gallery_food_detail);
//		gallery.setAdapter(new RecommendFoodAdapter());
//		gallery.setOnItemClickListener(new OnItemClickListener(){
//			@Override
//			public void onItemClick(AdapterView<?> parent,View view, int position, long id) {
//				showDialog(RECOMMEND_DIALOG, position);
//			}
//		});
	}
	
	@Override 
	public void onDestroy(){
		super.onDestroy();
		mImageFetcher.clearCache();
	}
	
	protected void showDialog(String tab, int position) {
		//设置推荐菜对话框或口味选择对话框
		if(tab == RECOMMEND_DIALOG)
		{
//			//推荐菜对话框的view
//			RecommendFoodFragment recFoodFragment = new RecommendFoodFragment();
//			recFoodFragment.show(getFragmentManager(), tab);
			
//			mDialog.show();
//			mDialog.setPosition(position);
		} else{
			PickTasteFragment pickTasteFg = new PickTasteFragment();
			pickTasteFg.setOnTasteChangeListener(this);
			Bundle args = new Bundle();
			args.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(mOrderFood));
			pickTasteFg.setArguments(args);
			pickTasteFg.show(getFragmentManager(), tab);
		}
	}

//	private void initDialog() {
////		View dialogLayout = getLayoutInflater().inflate(R.layout.recommend_food_fragment, (ViewGroup) findViewById(R.id.rec_food_dialog_layout));
//		
//		mDialogFragment = (RecommendFoodFragment) getFragmentManager().findFragmentById(R.id.recommendFoodFragment_rec_dialog);
//		mDialogFragment.show(getFragmentManager(), RECOMMEND_DIALOG);
//		Dialog mDialog = mDialogFragment.getDialog();
//		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
////		mDialog.setContentView(dialogLayout);
//		
//		//设置对话框长宽
//		Window dialogWindow = mDialog.getWindow();
//		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//		lp.width = 900;
//		lp.height = 550;
//		dialogWindow.setAttributes(lp);
//	}

	/*
	 * 推荐菜对话框
	 */
//	private class RecFoodDialog implements GalleryFragment.OnPicClickListener,
//										   GalleryFragment.OnPicChangedListener
//	{
//		private Dialog mDialog;
//		private GalleryFragment mGalleryFragment;
//		private View mDialogLayout;
//		
//		RecFoodDialog(){
//			
//			mDialogLayout = getLayoutInflater().inflate(R.layout.recommend_dialog, (ViewGroup) findViewById(R.id.recommend_dialog_layout));
//			mDialog = new Dialog(FoodDetailActivity.this);
//			mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//			mDialog.setContentView(mDialogLayout);
//			
//			//设置对话框长宽
//			Window dialogWindow = mDialog.getWindow();
//			WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//			lp.width = 900;
//			lp.height = 550;
//			dialogWindow.setAttributes(lp);
//			
//			mGalleryFragment = GalleryFragment.newInstance(0.3f, 8, ScaleType.CENTER_INSIDE);
//			FragmentTransaction fragmentTransaction = FoodDetailActivity.this.getFragmentManager().beginTransaction();
//			fragmentTransaction.replace(R.id.viewPager_container_rec_dialog, mGalleryFragment).commit();
//			mGalleryFragment.setOnPicClickListener(this);
//			mGalleryFragment.setOnPicChangedListener(this);
//			
//			final EditText countEditText = (EditText) mDialogLayout.findViewById(R.id.editText_count_rec_dialog);
//			
//			//设置数量加减
//			((ImageButton) mDialogLayout.findViewById(R.id.imageButton_plus_rec_dialog)).setOnClickListener(new OnClickListener(){
//
//				@Override
//				public void onClick(View v) {
//					float curNum = Float.parseFloat(countEditText.getText().toString());
//					countEditText.setText("" + ++curNum);
//				}
//			});
//			
//			//设置数量减
//			((ImageButton)mDialogLayout.findViewById(R.id.imageButton_minus_recommendDialog)).setOnClickListener(new OnClickListener(){
//
//				@Override
//				public void onClick(View v) {
//					float curNum = Float.parseFloat(countEditText.getText().toString());
//					if(--curNum >= 0){
//						countEditText.setText("" + curNum);
//					}
//				}
//			});
//			//点菜按钮
//			((ImageButton)mDialogLayout.findViewById(R.id.imageButton_addFood_rec_dialog)).setOnClickListener(new OnClickListener(){
//				@Override
//				public void onClick(View v) {
//					mOrderFood = new OrderFood(mRecommendfoods.get(mGalleryFragment.getSelectedPosition()));
//					mOrderFood.setCount(Float.parseFloat(((EditText) mDialog.findViewById(R.id.editText_count_rec_dialog)).getText().toString()));
//					ShoppingCart.instance().addFood(mOrderFood);
//					Toast.makeText(getApplicationContext(), mOrderFood.name + "已添加", Toast.LENGTH_SHORT).show();
//				}
//			});
//			
//			((ImageButton)mDialogLayout.findViewById(R.id.imageButton_amplify_rec_dialog)).setOnClickListener(new OnClickListener(){
//				@Override
//				public void onClick(View v) {
//					Intent intent = new Intent(FoodDetailActivity.this ,FullScreenActivity.class);
//					Bundle bundle = new Bundle();
//					bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(mOrderFood));
//					intent.putExtras(bundle);
//					startActivity(intent);
//				}
//			});
//			
//		}
//		
//		void show(){
//			mGalleryFragment.notifyDataChanged(mRecommendfoods);			
//			mDialog.show();
//		}
//		
////		void dismiss(){
////			mDialog.dismiss();
////		}
//		
//		void setPosition(int position){
//			mGalleryFragment.setPosition(position);
//		}
//
//		@Override
//		public void onPicClick(Food food, int position) {
////			//当点击推荐菜时更新当前菜品
//			float count = Float.parseFloat(((EditText) mDialog.findViewById(R.id.editText_count_rec_dialog)).getText().toString());
//			mOrderFood = new OrderFood(mRecommendfoods.get(position));
//			mOrderFood.setCount(count);
//			mOrderFood.tmpTaste = new Taste();
//			mOrderFood.tmpTaste.setPreference("");
//			
//			Intent intent = getIntent();
//			Bundle bundle = new Bundle();
//			bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(mOrderFood));
//			intent.replaceExtras(bundle);
//			
//			mFoodImageView.setImageBitmap(mImgLoader.loadImage(mOrderFood.image));
//			mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
//			mDialog.dismiss();
//		}
//
//		@Override
//		public void onPicChanged(Food curFood, int position) {
//			((TextView) mDialogLayout.findViewById(R.id.textView_price_rec_dialog)).setText("" + curFood.getPrice());
//			((TextView) mDialogLayout.findViewById(R.id.textView_food_name_recommend_dialog)).setText(curFood.name);
//		}
//	}
	@Override
	public void onTasteChange(OrderFood food) {
		mOrderFood = food;
		mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
	}
	
	class RecommendFoodAdapter extends BaseAdapter{
    	
    	@Override
    	public int getCount() {
    		return mRecommendfoods.size();
    	}

    	// 返回图片路径
    	@Override
    	public Object getItem(int position) {
    		return mRecommendfoods.get(position);
    	}

    	// 返回图片在资源的位置
    	@Override
    	public long getItemId(int position) {
    		return position;
    	}

    	// 此方法是最主要的，他设置好的ImageView对象返回给Gallery
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		ImageView imageView;
    		if(convertView == null){
    			convertView = new ImageView(FoodDetailActivity.this);
    			imageView = (ImageView)convertView;
    			// 设置ImageView的伸缩规格，用了自带的属性值
    			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    			
    		}else {
    			imageView = (ImageView)convertView;
    		}
    		
    		BitmapFactory.Options options = new BitmapFactory.Options();
    		options.inJustDecodeBounds = true; 
    		BitmapFactory.decodeFile(mRecommendfoods.get(position).image, options);
//    		imageView.setImageBitmap(ImageResizer.decodeSampledBitmapFromFile((), reqWidth, reqHeight));
    		return imageView;
    	}
	}

	@Override
	public void onPicClick(Food food, int position) {
		showDialog(RECOMMEND_DIALOG, position);
	}
}
