package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.util.ImageLoader;
import com.wireless.util.PickTasteFragment;
import com.wireless.util.PickTasteFragment.OnTasteChangeListener;

public class FoodDetailActivity extends Activity implements OnTasteChangeListener{
	private static final int ORDER_FOOD_CHANGED = 234841;
	private static final String RECOMMEND_DIALOG = "recommend_dialog";

	private OrderFood mOrderFood;
	
	private ImageLoader mImgLoader;
	private DisplayHandler mDisplayHandler;
	private ArrayList<Food> mRecommendfoods;
	private ImageView mFoodImageView;

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
		
		mImgLoader = new ImageLoader(this);
		mDisplayHandler = new DisplayHandler(this);
		mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
		
		mFoodImageView = (ImageView) findViewById(R.id.imageView_foodDetail);
		mFoodImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		mFoodImageView.setImageBitmap(mImgLoader.loadImage(mOrderFood.image));
		mFoodImageView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FoodDetailActivity.this ,FullScreenActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(mOrderFood));
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		
		final EditText countEditText = (EditText) findViewById(R.id.editText_count_foodDetail);
		countEditText.setText(String.valueOf(mOrderFood.getCount()));
		
		((ImageButton) findViewById(R.id.imageButton_plus_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				float curNum = Float.parseFloat(countEditText.getText().toString());
				countEditText.setText("" + ++curNum);
				mOrderFood.setCount(curNum);
			}
		});
		
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
		
		((Button) findViewById(R.id.button_pickTaste_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showDialog(PickTasteFragment.FOCUS_TASTE, 0);
			}
		});
		
		((Button) findViewById(R.id.button_pinzhu_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showDialog(PickTasteFragment.FOCUS_NOTE, 0);
			}
		});
		
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
		
		TabHost mTabHost = (TabHost) findViewById(R.id.tabhost_foodDetail);
		mTabHost.setup();
		
		mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator("基本").setContent(R.id.tab1_foodDetail));
		mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator("其它").setContent(R.id.tab2_foodDetail));
		
		
		mRecommendfoods = new ArrayList<Food>();
		for(Food f:WirelessOrder.foods)
		{
			if(f.isRecommend())
				mRecommendfoods.add(f);
		}
		
		Gallery gallery = (Gallery) findViewById(R.id.gallery_food_detail);
		gallery.setAdapter(new RecommendFoodAdapter());
		gallery.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent,View view, int position, long id) {
				showDialog(RECOMMEND_DIALOG, position);
			}
		});
	}
	
	protected void showDialog(String tab, int position) {
		if(tab == RECOMMEND_DIALOG)
		{
			View dialogLayout = getLayoutInflater().inflate(R.layout.recommend_dialog, (ViewGroup) findViewById(R.id.recommend_dialog_layout));
			final Dialog dialog = new Dialog(FoodDetailActivity.this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(dialogLayout);
			dialog.show();
			
			Window dialogWindow = dialog.getWindow();
			WindowManager.LayoutParams lp = dialogWindow.getAttributes();
			lp.width = 900;
			lp.height = 650;
			dialogWindow.setAttributes(lp);
			
			Gallery gallery = (Gallery) dialog.findViewById(R.id.gallery_recommed_food_dialog);
			gallery.setAdapter(new RecommendFoodAdapter());
			gallery.setSelection(position);
			
			gallery.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
					float count = mOrderFood.getCount();
					mOrderFood = new OrderFood(mRecommendfoods.get(position));
					mOrderFood.setCount(count);
					mOrderFood.tmpTaste = new Taste();
					mOrderFood.tmpTaste.setPreference("");
					
					Intent intent = getIntent();
					Bundle bundle = new Bundle();
					bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(mOrderFood));
					intent.replaceExtras(bundle);
					
					mFoodImageView.setImageBitmap(mImgLoader.loadImage(mOrderFood.image));
					mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
					dialog.dismiss();
				}
			});
		} else{
			PickTasteFragment pickTasteFg = new PickTasteFragment();
			pickTasteFg.setOnTasteChangeListener(this);
			Bundle args = new Bundle();
			args.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(mOrderFood));
			pickTasteFg.setArguments(args);
			pickTasteFg.show(getFragmentManager(), tab);
		}
	}

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
    		imageView.setAdjustViewBounds(true);
    		imageView.setImageBitmap(mImgLoader.loadImage(mRecommendfoods.get(position).image));
    		return imageView;
    	}
	}
}
