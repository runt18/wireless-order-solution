package com.wireless.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.excep.BusinessException;
import com.wireless.lib.task.QueryFoodAssociationTask;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.ui.FoodDetailActivity;
import com.wireless.ui.FullScreenActivity;
import com.wireless.ui.MainActivity;
import com.wireless.util.SearchFoodHandler;
import com.wireless.util.SearchFoodHandler.OnSearchItemClickListener;
import com.wireless.util.imgFetcher.ImageCache;
import com.wireless.util.imgFetcher.ImageFetcher;

public class GalleryFragment extends Fragment implements OnSearchItemClickListener {
	private final static String KEY_MEMORY_CACHE_PERCENT = "key_memory_cache_percent";
	private final static String KEY_CACHE_VIEW_AMOUNT = "key_cache_view_amount";
	private final static String KEY_IMAGE_SCALE_TYPE = "key_image_scale_type";
	private final static String KEY_SRC_FOODS = "key_src_foods";
	
	private final static float DEFAULT_PERCENT_MEMORY_CACHE = 0.1f;
	private final static int DEFAULT_CACHE_VIEW_AMOUNT = 2;
	private final static ScaleType DEFAULT_IMAGE_SCALE_TYPE = ScaleType.CENTER_CROP;
	
	private FragmentStatePagerAdapter mGalleryAdapter = null;
	private ViewPager mViewPager;
	private List<OrderFood> mFoods = new ArrayList<OrderFood>();
	private ImageFetcher mImgFetcher;
	
	//搜索框
	private AutoCompleteTextView mSearchEditText;
	
	//搜索框的Handler
//	private FoodSearchHandler mSearchHandler;
	private SearchFoodHandler mSearchHandler;

	//搜索框的条件
//	private String mFilterCond;
	
	//当前菜品
	private OrderFood mOrderFood;
	
	//当前位置
	private int mCurrentPosition = 0;
	
	//"厨房 - 首张图片位置"的键值对
	private HashMap<Kitchen, Integer> mFoodPosByKitchenMap = new HashMap<Kitchen, Integer>();
	
	//"菜品 - 首张图片位置"的键值对
	private HashMap<OrderFood, Integer> mFoodPos = new HashMap<OrderFood, Integer>();
	
	public interface OnPicChangedListener{
		void onPicChanged(OrderFood curFood, int position);
	}
	
	public void setOnPicChangedListener(OnPicChangedListener l){
		mPicChangeListener = l;
	}
	
	private OnPicChangedListener mPicChangeListener;
	
	public static interface OnPicClickListener{
		void onPicClick(Food food , int position);
	}
	public void setOnPicClickListener(OnPicClickListener l)
	{
		mOnPicClickListener = l;
	}

	OnPicClickListener mOnPicClickListener;
	private PopupWindow mComboPopup;
	private PopupWindow mIntroPopup;
	private Timer mIntroTimer;
	private static final String IS_IN_SUB_ACTIVITY = "isInSubActivity";	

	/**
	 * Factory method to generate a new instance of the fragment.
	 * 
	 * @param percent Percent of memory class to use to size memory cache
	 * @param nCachedViews Amount of the cached pagers in the view pager
	 * @param scaleType 
	 * @return A new instance of GalleryFragment
	 */
	public static GalleryFragment newInstance(Food[] srcFoods, float percent, int nCachedViews, ImageView.ScaleType scaleType){
		GalleryFragment gf = new GalleryFragment();
        if (percent < 0.05f || percent > 0.8f) {
            throw new IllegalArgumentException("newInstance - percent must be between 0.05 and 0.8 (inclusive)");
        }
		Bundle args = new Bundle();
		args.putFloat(KEY_MEMORY_CACHE_PERCENT, percent);
		args.putInt(KEY_CACHE_VIEW_AMOUNT, nCachedViews < 0 ? 0 : nCachedViews);
		args.putInt(KEY_IMAGE_SCALE_TYPE, scaleType.ordinal());
		ArrayList<FoodParcel> foodParcels = new ArrayList<FoodParcel>(srcFoods.length);
		for(int i = 0; i < srcFoods.length; i++){
			foodParcels.add(new FoodParcel(new OrderFood(srcFoods[i])));
		}
		args.putParcelableArrayList(KEY_SRC_FOODS, foodParcels);
		gf.setArguments(args);
		return gf;
	}
	
	/**
	 * 获取当前画廊显示的Food
	 * @return
	 */
	public OrderFood getCurFood(){
		if(mCurrentPosition < mFoods.size())
			return mFoods.get(mCurrentPosition);
		else return new OrderFood();
	}
	
	/**
	 * 根据position设置画廊显示的图片
	 * @param position
	 */
	private void setPosition(final int position){
		if(mCurrentPosition != position){
			mViewPager.post(new Runnable(){
				@Override
				public void run() {
					mViewPager.setCurrentItem(position);
				}
			});
			mCurrentPosition = position;
		}
	}
	
	/**
	 * 根据Kitchen设置画廊显示的图片
	 * @param kitchen
	 */
	public void setPosByKitchen(Kitchen kitchen){
		Integer pos = mFoodPosByKitchenMap.get(kitchen);
		if(pos != null){
			setPosition(pos);
		}
	}
	
	/**
	 * 根据Food设置画廊显示的图片
	 * @param foodToSet
	 */
	public void setPosByFood(Food foodToSet){
		Integer pos = mFoodPos.get(new OrderFood(foodToSet));
		if(pos != null){
			setPosition(pos);
		}else{
			refreshShowing(new OrderFood(foodToSet));
		}		
	}
	
	public int getSelectedPosition(){
		return mViewPager.getCurrentItem();
	}
	
	public OrderFood getFood(int position){
		return mFoods.get(position);
	}
	
	public OrderFood getCurrentFood(){
		if(mOrderFood != null)
			return mOrderFood;
		else return null;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        //Create the image fetcher without the image size since it only can be retrieved later. 
    	mImgFetcher = new ImageFetcher(getActivity(), 0, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.gallery_fgm, container, false);
		
//		mSearchHandler = new FoodSearchHandler(this);

		((RelativeLayout) view.findViewById(R.id.top_bar_galleryFgm)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
 				
			}
		});
		
		((RelativeLayout)view.findViewById(R.id.relativeLayout_bottom_right)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
 				
			}
		});
		
		/**
		 * Gallery上的全屏Button，点击后跳转到FullScreenActivity 
		 */
		((ImageView)view.findViewById(R.id.imageButton_amplify_galleryFgm)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//如果是子activity，则直接返回
				if(getActivity().getIntent().getBooleanExtra(IS_IN_SUB_ACTIVITY, false)){
					
					getActivity().onBackPressed();
				} else {
					if(mOrderFood != null && mOrderFood.name != null){
						//否则打开新activity
						Intent intent = new Intent(getActivity(), FullScreenActivity.class);
						Bundle bundle = new Bundle();
						bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(mOrderFood));
						intent.putExtras(bundle);
						intent.putExtra(IS_IN_SUB_ACTIVITY, true);
						getActivity().startActivityForResult(intent, MainActivity.MAIN_ACTIVITY_RES_CODE);
					}
				}
				
			}
		});
		
		//搜索框
		mSearchEditText = (AutoCompleteTextView) view.findViewById(R.id.editText_galleryFgm);
		Button clearSearchBtn = (Button) view.findViewById(R.id.button_galleryFgm_clear);

		mSearchHandler = new SearchFoodHandler(this, mSearchEditText, clearSearchBtn);
		mSearchHandler.setOnSearchItemClickListener(this);
		//点菜按钮
		((ImageView) view.findViewById(R.id.imageButton_add_galleryFgm)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				float oriCnt = mOrderFood.getCount();
				try{
					mOrderFood.setCount(1f);
					ShoppingCart.instance().addFood(mOrderFood);
					mOrderFood.setCount(++ oriCnt);

					//显示已点数量
					((TextView) getView().findViewById(R.id.textView_galleryFgm_count)).setText(Util.float2String2(mOrderFood.getCount()));
					(getView().findViewById(R.id.textView_galleryFgm_pickedHint)).setVisibility(View.VISIBLE);

					getView().findViewById(R.id.button_galleryFgm_ComboFood).performClick();
				}catch(BusinessException e){
					mOrderFood.setCount(-- oriCnt);
					Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
		//菜品详情
		((Button) view.findViewById(R.id.button_galleryFgm_detail)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mOrderFood != null && mOrderFood.name != null){
					Intent intent = new Intent(getActivity(), FoodDetailActivity.class);
					Bundle bundle = new Bundle();
					OrderFood orderFood = new OrderFood(mOrderFood);
					orderFood.setCount(1f);
					
					bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(orderFood));
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		});
		
		int nCacheViews = DEFAULT_CACHE_VIEW_AMOUNT;
		
        Bundle bundle = getArguments();
        if(bundle != null){
        	
        	nCacheViews = bundle.getInt(KEY_CACHE_VIEW_AMOUNT);
        }
    	
        mViewPager = (ViewPager) view.findViewById(R.id.picViewPager);
        mViewPager.setOffscreenPageLimit(nCacheViews);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);		

		if(WirelessOrder.foods.length != 0)
			mOrderFood = new OrderFood(WirelessOrder.foods[0]);
		else mOrderFood = new OrderFood();
		
		if(getActivity().getIntent().getBooleanExtra(IS_IN_SUB_ACTIVITY, false))
		{
			((ImageView) getView().findViewById(R.id.imageButton_amplify_galleryFgm)).setImageResource(R.drawable.lessen_btn_selector);
			((Button) getView().findViewById(R.id.button_galleryFgm_detail)).setVisibility(View.GONE);
		}
		
		try{
			mPicChangeListener = (OnPicChangedListener)getActivity();
		}catch(ClassCastException e){
			
		}
		
		float percent = DEFAULT_PERCENT_MEMORY_CACHE;
//		int nCacheViews = DEFAULT_CACHE_VIEW_AMOUNT;
		ScaleType scaleType = DEFAULT_IMAGE_SCALE_TYPE;
		
        Bundle bundle = getArguments();
        if(bundle != null){
        	
        	percent = bundle.getFloat(KEY_MEMORY_CACHE_PERCENT);
//        	nCacheViews = bundle.getInt(KEY_CACHE_VIEW_AMOUNT);
        	scaleType = ScaleType.values()[bundle.getInt(KEY_IMAGE_SCALE_TYPE)];
        	ArrayList<FoodParcel> foodParcels = bundle.getParcelableArrayList(KEY_SRC_FOODS);
        	
        	ArrayList<OrderFood> srcFoods = new ArrayList<OrderFood>();
        	srcFoods.addAll(foodParcels);
        	
        	notifyDataSetChanged(srcFoods);
        }
		
    	mImgFetcher.addImageCache(getFragmentManager(), new ImageCache.ImageCacheParams(getActivity(), percent), "GalleryFragment");
//    	//Add the listener to retrieve the width and height of this fragment, then set them to image fetcher.
    	getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
    	     @Override
   	          public void onGlobalLayout() {
    	    	 mImgFetcher.setImageSize(getView().getWidth(), getView().getHeight());
    	    	 getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
   	          }
    	});
        
        final ScaleType scale = scaleType;
        mGalleryAdapter = new MyFragmentStatePagerAdapter (getFragmentManager(), scale);
        
        mViewPager.post(new Runnable(){

			@Override
			public void run() {
				mViewPager.setAdapter(mGalleryAdapter);
			}
        });        
        
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				
				refreshShowing(mFoods.get(position));
				
				mCurrentPosition = position;

				if(mPicChangeListener != null){
					mPicChangeListener.onPicChanged(mFoods.get(position), position);
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {			
				refreshIntroTimer();
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				//隐藏键盘
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
				
				if(!mSearchEditText.getText().toString().equals(""))
					mSearchEditText.setText("");
				
				if(state == ViewPager.SCROLL_STATE_DRAGGING){
					mImgFetcher.setPauseWork(true);
				} else if(state == ViewPager.SCROLL_STATE_IDLE){
					mImgFetcher.setPauseWork(false);
				}
			}
		});
        //准备弹出框
		mComboPopup = new PopupWindow(getActivity().getLayoutInflater().inflate(R.layout.gallery_fgm_combo, null),
				640,LayoutParams.WRAP_CONTENT);
		mComboPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_small));
		mComboPopup.setOutsideTouchable(true);
		
		final ImageFetcher comboFetcher = new ImageFetcher(getActivity(), 200,144);
		//关联菜按钮
		final Button comboBtn = (Button) getView().findViewById(R.id.button_galleryFgm_ComboFood);
		comboBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mOrderFood != null){
					//如果当前菜中的关联菜为空
					new QueryFoodAssociationTask(mOrderFood, false){

						@Override
						protected void onPostExecute(Food[] result) {
							super.onPostExecute(result);
							if(result == null || result.length == 0){
								Toast.makeText(getActivity(), "此菜无关联菜", Toast.LENGTH_SHORT).show();
								//如果返回后依然是当前菜品
							} else if(mFoodToAssociate.equals(mOrderFood)){
								LayoutInflater inflater = getActivity().getLayoutInflater();
								LinearLayout comboLayout = (LinearLayout) mComboPopup.getContentView().findViewById(R.id.linearLayout_galleryFgm_combo);
								comboLayout.removeAllViews(); 
								//将所有关联菜添加
								for(Food food : result){
									if(food.image != null){
										View foodView = inflater.inflate(R.layout.gallery_fgm_combo_item, null);
										TextView nameText = (TextView)foodView.findViewById(R.id.textView_galleryFgm_combo_item);
										
										if(food.name != null)
											nameText.setText(food.name);
										
										ImageView imgView = (ImageView) foodView.findViewById(R.id.imageView_galleryFgm_combo_item);
										imgView.setScaleType(ScaleType.CENTER_CROP);
										comboFetcher.loadImage(food.image, imgView );
										
										comboLayout.addView(foodView);
										foodView.setTag(food);
										foodView.setOnClickListener(new OnClickListener() {
											@Override
											public void onClick(View v) {
												Food food = (Food) v.getTag();
												//如果有图片则跳转，没有则提示
												if(food.image != null){
													setPosByFood(food);
													mComboPopup.dismiss();
												} else {
													Toast toast = Toast.makeText(getActivity(), "此菜暂无图片可展示", Toast.LENGTH_SHORT);
													toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
													toast.show();
												}
											}
										});
	
										//点菜按钮
										View addBtn = foodView.findViewById(R.id.button_galleryFgm_combo_item_add);
										addBtn.setTag(food);
										addBtn.setOnClickListener(new OnClickListener() {
											@Override
											public void onClick(View v) {
												Food food = (Food) v.getTag();
												try {
													ShoppingCart.instance().addFood(new OrderFood(food));
													Toast toast = Toast.makeText(getActivity(), "1份"+food.name+"已添加", Toast.LENGTH_SHORT);
													toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
													toast.show();
												} catch (BusinessException e) {
													Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
												}
											}
										});
									}
								}
								//显示
								mComboPopup.showAsDropDown(comboBtn, -100,0);
								mComboPopup.getContentView().post(new Runnable() {
									@Override
									public void run() {
										//滚回到第一个
										((HorizontalScrollView)mComboPopup.getContentView().findViewById(R.id.horizontalScrollView_galleryFgm_combo)).smoothScrollTo(0, 0);		
									}
								});
							}
						}
					}.execute(WirelessOrder.foodMenu);
				}
			}
		});
		//简介弹出框
		mIntroPopup = new PopupWindow(getActivity().getLayoutInflater().inflate(R.layout.gallery_fgm_intro, null),
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mIntroPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_small));
		mIntroPopup.setOutsideTouchable(true);
		//点击菜名弹出简介
		((TextView) getView().findViewById(R.id.textView_foodName_galleryFgm)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mOrderFood != null && mOrderFood.desc != null){
					((TextView)mIntroPopup.getContentView().findViewById(R.id.textView_galleryFgm_intro)).setText(mOrderFood.desc);
					mIntroPopup.showAsDropDown(v, 0, -100);
				} else {
					Toast.makeText(getActivity(), "此菜没有简介", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	class MyFragmentStatePagerAdapter extends FragmentStatePagerAdapter{
    	
        private ScaleType mScale;

		public MyFragmentStatePagerAdapter(FragmentManager fm, ScaleType scaleType) {
			super(fm);
			mScale = scaleType;
		}

		@Override
        public int getCount() {
            return mFoods.size();
        }
    	
        @Override
        public Fragment getItem(int position) {
            return ImageDetailFragment.newInstance(mFoods.get(position), GalleryFragment.this.getId(), mScale);
        }            
    }
	
	@Override
	public void onStart() {
		super.onStart();
		this.refreshFoodsCount();
		refreshShowing(this.getCurFood());
		mSearchEditText.clearFocus();
	}
	
	@Override 
	public void onDestroy(){
		super.onDestroy();
		mImgFetcher.clearCache();
		mImgFetcher = null;
//		mFetcherForSearch.clearCache();
	}
	/*
	 * 刷新菜品简介的timer
	 */
	private void refreshIntroTimer(){
		if(mIntroTimer != null)
			mIntroTimer.cancel();
		
		mIntroTimer = new Timer();
		//延迟3秒显示简介
		mIntroTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(mIntroPopup != null && mOrderFood != null && mOrderFood.desc != null){
					getView().post(new Runnable() {
						@Override
						public void run() {
							getView().findViewById(R.id.textView_foodName_galleryFgm).performClick();
						}
					});
				}
			}
		}, 3000);
	}
	public void notifyDataSetChanged(ArrayList<OrderFood> datas){
		if(!datas.isEmpty()){
			mSearchHandler.refreshSrcFoods(WirelessOrder.foodMenu.foods);
			
	    	mFoods.clear();
	    	mFoodPosByKitchenMap.clear();
	    	mFoodPos.clear();
	    	
			Food firstFood = datas.get(0);
			int firstPos = 0;
			
			mFoodPosByKitchenMap.put(firstFood.kitchen, firstPos);
			
	    	for(OrderFood foodParcel : datas){
	    		
	    		mFoods.add(foodParcel);
	    		
	    		//设置菜品和对应首张图片位置
	    		mFoodPos.put(foodParcel, firstPos);
	    		
	    		//设置厨房和对应菜品首张图片位置
	   			if(!foodParcel.kitchen.equals(firstFood.kitchen)){
	    			firstFood = foodParcel;
	    			mFoodPosByKitchenMap.put(firstFood.kitchen, firstPos);
	    		}
	   			firstPos++;
	    	}   
		}
	}
	
	
	
	public ImageFetcher getImgFetcher(){
		return mImgFetcher;
	}
	/**
	 * 调用该方法以更新当前菜品的显示
	 * @param food 传入的菜品
	 * @param position 
	 */
	public void refreshShowing(OrderFood food){
		mOrderFood = ShoppingCart.instance().getFood(food.getAliasId());
		if(mOrderFood == null)
			mOrderFood = food;
		
		View fgmView = getView();

		if(mOrderFood.getCount() != 0f)
		{
			(fgmView.findViewById(R.id.textView_galleryFgm_pickedHint)).setVisibility(View.VISIBLE);
			((TextView) fgmView.findViewById(R.id.textView_galleryFgm_count)).setText(Util.float2String2(mOrderFood.getCount()));
		}
		else{
			((TextView) fgmView.findViewById(R.id.textView_galleryFgm_count)).setText("");
			(fgmView.findViewById(R.id.textView_galleryFgm_pickedHint)).setVisibility(View.INVISIBLE);
		}
		
		
		((TextView) fgmView.findViewById(R.id.textView_foodName_galleryFgm)).setText(food.name);
		((TextView) fgmView.findViewById(R.id.textView_price_galleryFgm)).setText(Util.float2String2(food.getPrice()));
		new SignalHolder(food);
	}
	
	public void refreshFoodsCount(){
		for(OrderFood f:mFoods) 
		{
			OrderFood orderFood = ShoppingCart.instance().getFood(f.getAliasId());
			if(orderFood != null)
				f.setCount(orderFood.getCount());
			else f.setCount(0f);
		}
	}

	public void clearFoodCounts() {
		for(OrderFood f:mFoods)
		{
			f.setCount(0f);
		}
	}

	@Override
	public void onSearchItemClick(Food food) {
		if(food.image != null){
			this.setPosByFood(food);
		}
		else {
			Toast toast = Toast.makeText(getActivity(), "此菜暂无图片可展示", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP|Gravity.RIGHT, 230, 100);
			toast.show();
		}
	}
	
	private class SignalHolder{
		private static final int SPE_SIGNAL = 100;
		private static final int HOT_SIGNAL = 102;
		private static final int REC_SIGNAL = 103;
		private ArrayList<Integer> mSignals;
		
		SignalHolder(OrderFood food){
			
			mSignals = new ArrayList<Integer>();
			if(food.isSpecial())
				mSignals.add(SPE_SIGNAL);
			if(food.isHot())
				mSignals.add(HOT_SIGNAL);
			if(food.isRecommend())
				mSignals.add(REC_SIGNAL);
			
			refreshDisplay();
		}
		
		void refreshDisplay(){
			View fgmView = getView();
			dismissAllSignals();
			for (int i = 0; i < mSignals.size(); i++) {
				Integer sign = mSignals.get(i);
				if(i == 0){
					switch(sign){
					case SPE_SIGNAL:
						(fgmView.findViewById(R.id.imageButton_special_galleryFgm)).setVisibility(View.VISIBLE);
						break;
					case HOT_SIGNAL:
						(fgmView.findViewById(R.id.imageView_galleryFgm_hotSignal)).setVisibility(View.VISIBLE);
						break;
					case REC_SIGNAL:
						(fgmView.findViewById(R.id.imageView_galleryFgm_recSignal)).setVisibility(View.VISIBLE);
						break;
					}
				} else {
					switch(sign){
					case SPE_SIGNAL:
						break;
					case HOT_SIGNAL:
						fgmView.findViewById(R.id.imageView_galleryFgm_hotSmall).setVisibility(View.VISIBLE);
						break;
					case REC_SIGNAL:
						fgmView.findViewById(R.id.imageView_galleryFgm_recSmall).setVisibility(View.VISIBLE);
						break;
					}
				}
			}
		}
		
		void dismissAllSignals(){
			View fgmView = getView();
			(fgmView .findViewById(R.id.imageButton_special_galleryFgm)).setVisibility(View.GONE);
			(fgmView.findViewById(R.id.imageView_galleryFgm_hotSignal)).setVisibility(View.GONE);
			(fgmView.findViewById(R.id.imageView_galleryFgm_recSignal)).setVisibility(View.GONE);
			fgmView.findViewById(R.id.imageView_galleryFgm_hotSmall).setVisibility(View.GONE);
			fgmView.findViewById(R.id.imageView_galleryFgm_recSmall).setVisibility(View.GONE);

		}
	}
}

