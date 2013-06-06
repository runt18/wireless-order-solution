package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
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
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.ordermenu.R;
import com.wireless.parcel.DepartmentTreeParcel;
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.DepartmentTree;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.ComboFoodActivity2;
import com.wireless.ui.FoodDetailActivity;
import com.wireless.ui.FullScreenActivity;
import com.wireless.ui.MainActivity;
import com.wireless.util.ExhibitPopupWindow;
import com.wireless.util.ExhibitPopupWindow.OnExhibitOperateListener;
import com.wireless.util.SearchFoodHandler;
import com.wireless.util.SearchFoodHandler.OnSearchItemClickListener;
import com.wireless.util.imgFetcher.ImageCache;
import com.wireless.util.imgFetcher.ImageFetcher;

public class GalleryFragment extends Fragment implements OnSearchItemClickListener, OnExhibitOperateListener {
	
	/**
	 * The handler to refresh the current food.
	 */
	private static class FoodRefreshHandler extends Handler{ 
		
		private WeakReference<GalleryFragment> mFragment;
		
		FoodRefreshHandler(GalleryFragment theFragment){
			this.mFragment = new WeakReference<GalleryFragment>(theFragment);
		}
		
		@Override
		public void handleMessage(Message message){
			if(mFragment.get().mCurFood != null){
				
				OrderFood foodHasOrdered = ShoppingCart.instance().searchNewFoodByAlias(mFragment.get().mCurFood.getAliasId());
				float orderAmount;
				if(foodHasOrdered != null){
					orderAmount = foodHasOrdered.getCount();
				}else{
					orderAmount = 0;
				}
				
				View fgmView = mFragment.get().getView();

				if(fgmView != null){
					
					//菜品已点数量
					if(orderAmount != 0){
						(fgmView.findViewById(R.id.textView_galleryFgm_pickedHint)).setVisibility(View.VISIBLE);
						((TextView) fgmView.findViewById(R.id.textView_galleryFgm_count)).setText(NumericUtil.float2String2(orderAmount));
					}else{
						((TextView) fgmView.findViewById(R.id.textView_galleryFgm_count)).setText("");
						(fgmView.findViewById(R.id.textView_galleryFgm_pickedHint)).setVisibility(View.INVISIBLE);
					}
					
					//菜品名称和价钱
					((TextView) fgmView.findViewById(R.id.textView_foodName_galleryFgm)).setText(mFragment.get().mCurFood.getName());
					((TextView) fgmView.findViewById(R.id.textView_price_galleryFgm)).setText(NumericUtil.float2String2(mFragment.get().mCurFood.getPrice()));
					
					//更新菜品属性
					updateFoodStatus(fgmView);
				}
			}

		}
		
		private void updateFoodStatus(View fgmView){
			fgmView.findViewById(R.id.imageButton_special_galleryFgm).setVisibility(View.GONE);
			fgmView.findViewById(R.id.imageView_galleryFgm_hotSignal).setVisibility(View.GONE);
			fgmView.findViewById(R.id.imageView_galleryFgm_recSignal).setVisibility(View.GONE);
			fgmView.findViewById(R.id.imageView_galleryFgm_hotSmall).setVisibility(View.GONE);
			fgmView.findViewById(R.id.imageView_galleryFgm_recSmall).setVisibility(View.GONE);
			
			final int SPE_SIGNAL = 100;
			final int HOT_SIGNAL = 102;
			final int REC_SIGNAL = 103;
			
			List<Integer> status = new ArrayList<Integer>();
			if(mFragment.get().mCurFood.isSpecial()){
				status.add(SPE_SIGNAL);
			}
			if(mFragment.get().mCurFood.isHot()){
				status.add(HOT_SIGNAL);
			}
			if(mFragment.get().mCurFood.isRecommend()){
				status.add(REC_SIGNAL);
			}
			
			for(int i = 0; i < status.size(); i++) {
				Integer sign = status.get(i);
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
	}
	
	private final static String KEY_MEMORY_CACHE_PERCENT = "key_memory_cache_percent";
	private final static String KEY_CACHE_VIEW_AMOUNT = "key_cache_view_amount";
	private final static String KEY_IMAGE_SCALE_TYPE = "key_image_scale_type";
	
	private final static float DEFAULT_PERCENT_MEMORY_CACHE = 0.1f;
	private final static int DEFAULT_CACHE_VIEW_AMOUNT = 2;
	private final static ScaleType DEFAULT_IMAGE_SCALE_TYPE = ScaleType.CENTER_CROP;
	
	private FragmentStatePagerAdapter mGalleryAdapter;
	private ViewPager mViewPager;
	
	private ImageFetcher mImgFetcher;
	
	//搜索框
	private AutoCompleteTextView mSearchEditText;
	
	//搜索框的Handler
	private SearchFoodHandler mSearchHandler;

	//搜索框的条件
//	private String mFilterCond;
	
	//菜品列表信息
	private List<Food> mFoods = new ArrayList<Food>();
	
	//菜品信息的更新Handler
	private FoodRefreshHandler mHandler;
	
	//当前菜品
	private Food mCurFood;
	
	//当前位置
	private int mCurrentPosition = 0;
	
	//"厨房 - 首张图片位置"的键值对
	//private HashMap<PKitchen, Integer> mFoodPosByKitchenMap = new HashMap<PKitchen, Integer>();
	
	//"菜品 - 首张图片位置"的键值对
	//private HashMap<Food, Integer> mFoodPos = new HashMap<Food, Integer>();
	
	public static interface OnGalleryChangedListener{
		/**
		 * Called when gallery is changed.
		 * @param curFood
		 * @param position
		 */
		public void onGalleryChanged(Food curFood, int position);
	}
	
	public void setOnGalleryChangedListener(OnGalleryChangedListener l){
		mGalleryChangeListener = l;
	}
	
	private OnGalleryChangedListener mGalleryChangeListener;
	
	public static interface OnPicClickListener{
		void onPicClick(Food food , int position);
	}
	
	private ExhibitPopupWindow mComboPopup;
	private PopupWindow mIntroPopup;
	private Timer mIntroTimer;
	private static final String IS_IN_SUB_ACTIVITY = "isInSubActivity";	

	/**
	 * Factory method to generate a new instance of the fragment.
	 * 
	 * @param deptTree the department tree to this gallery
	 * @param percent Percent of memory class to use to size memory cache
	 * @param nCachedViews Amount of the cached pagers in the view pager
	 * @param scaleType 
	 * @return A new instance of GalleryFragment
	 */
	public static GalleryFragment newInstance(DepartmentTree deptTree, float percent, int nCachedViews, ImageView.ScaleType scaleType){
		
		GalleryFragment gf = new GalleryFragment();
		
        if (percent < 0.05f || percent > 0.8f) {
            throw new IllegalArgumentException("newInstance - percent must be between 0.05 and 0.8 (inclusive)");
        }
		Bundle args = new Bundle();
		args.putFloat(KEY_MEMORY_CACHE_PERCENT, percent);
		args.putInt(KEY_CACHE_VIEW_AMOUNT, nCachedViews < 0 ? 0 : nCachedViews);
		args.putInt(KEY_IMAGE_SCALE_TYPE, scaleType.ordinal());
		args.putParcelable(DepartmentTreeParcel.KEY_VALUE, new DepartmentTreeParcel(deptTree));
		gf.setArguments(args);
		return gf;
	}
	
	/**
	 * 获取当前画廊显示的Food
	 * @return
	 */
	public Food getCurFood(){
		return mCurFood;
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
			
		}
	}
	
	/**
	 * 根据Kitchen设置画廊显示的图片
	 * @param kitchen
	 */
	public void setPosByKitchen(Kitchen kitchen){
		int pos = 0;
		for(Food f : mFoods){
			if(f.getKitchen().equals(kitchen)){
				setPosition(pos);
				break;
			}
			pos++;
		}
	}
	
	/**
	 * 根据Food设置画廊显示的图片
	 * @param foodToSet
	 */
	public void setPosByFood(Food foodToSet){
		int pos = 0;
		for(Food f : mFoods){
			if(f.getAliasId() == foodToSet.getAliasId()){
				setPosition(pos);
				break;
			}
			pos++;
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //Create the image fetcher without the image size since it only can be retrieved later. 
    	mImgFetcher = new ImageFetcher(getActivity(), 0, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view =  inflater.inflate(R.layout.fragment_gallery, container, false);
		
//		((RelativeLayout) view.findViewById(R.id.top_bar_galleryFgm)).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
// 				
//			}
//		});
//		
//		((RelativeLayout)view.findViewById(R.id.relativeLayout_bottom_right)).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
// 				
//			}
//		});
		
		/**
		 * Gallery上的全屏Button，点击后跳转到FullScreenActivity 
		 */
		((ImageView)view.findViewById(R.id.imageButton_amplify_galleryFgm)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//如果是子activity，则直接返回
				Bundle bundle = getActivity().getIntent().getExtras();
				if(bundle != null){
					if(bundle.getBoolean(IS_IN_SUB_ACTIVITY, false)){
						getActivity().onBackPressed();
					}					
				} else {
					if(mCurFood != null && mCurFood.getName() != null){
						//否则打开新activity
						Intent intent = new Intent(getActivity(), FullScreenActivity.class);
						bundle = new Bundle();
						bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(mCurFood));
						bundle.putBoolean(IS_IN_SUB_ACTIVITY, true);
						intent.putExtras(bundle);
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
				try{
					OrderFood of = new OrderFood(mCurFood);
					of.setCount(1f);
					ShoppingCart.instance().addFood(of);

					mHandler.sendEmptyMessage(0);
					
					//Perform to show the associated foods
					if(!mComboPopup.isShowing()){
						getView().findViewById(R.id.button_galleryFgm_ComboFood).performClick();
					}
					
				}catch(BusinessException e){
					Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		//菜品详情
		((Button) view.findViewById(R.id.button_galleryFgm_detail)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCurFood != null && mCurFood.getName() != null){
					Bundle bundle = new Bundle();
					OrderFood orderFood = new OrderFood(mCurFood);
					orderFood.setCount(1f);

					Intent intent = null;
					if(mCurFood.isCombo()){
						intent = new Intent(getActivity(), ComboFoodActivity2.class);
					} else {
						intent = new Intent(getActivity(), FoodDetailActivity.class);
					}
					bundle.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(orderFood));
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

		try{
			mGalleryChangeListener = (OnGalleryChangedListener)getActivity();
		}catch(ClassCastException e){
			
		}
		
		float percent = DEFAULT_PERCENT_MEMORY_CACHE;
		ScaleType scaleType = DEFAULT_IMAGE_SCALE_TYPE;
		
        Bundle bundle = getArguments();
        if(bundle != null){
        	
        	percent = bundle.getFloat(KEY_MEMORY_CACHE_PERCENT);
        	scaleType = ScaleType.values()[bundle.getInt(KEY_IMAGE_SCALE_TYPE)];
        	
        	DepartmentTreeParcel deptTreeParcel = bundle.getParcelable(DepartmentTreeParcel.KEY_VALUE);
        	notifyDataSetChanged(deptTreeParcel.asDeptTree());
        	
        }
		
		bundle = getActivity().getIntent().getExtras();
		if(bundle != null){
			if(bundle.getBoolean(IS_IN_SUB_ACTIVITY, false)){
				((ImageView) getView().findViewById(R.id.imageButton_amplify_galleryFgm)).setImageResource(R.drawable.lessen_btn_selector);
				((Button) getView().findViewById(R.id.button_galleryFgm_detail)).setVisibility(View.GONE);
			}
		}
        
        mHandler = new FoodRefreshHandler(this);
        
    	mImgFetcher.addImageCache(getFragmentManager(), new ImageCache.ImageCacheParams(getActivity(), percent), "ImgCache#GalleryFragment");
    	
    	//Add the listener to retrieve the width and height of this fragment, then set them to image fetcher.
    	getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
    	     @SuppressWarnings("deprecation")
			@Override
   	          public void onGlobalLayout() {
    	    	 mImgFetcher.setImageSize(getView().getWidth(), getView().getHeight());
    	    	 getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
   	          }
    	});
        
        final ScaleType scale = scaleType;
        mGalleryAdapter = new FragmentStatePagerAdapter(getFragmentManager()){
    		@Override
            public int getCount() {
                return mFoods.size();
            }
        	
            @Override
            public Fragment getItem(int position) {
                return GalleryItemFragment.newInstance(mFoods.get(position), GalleryFragment.this.getTag(), scale);
            } 
        };

        mViewPager.post(new Runnable(){

			@Override
			public void run() {
				mViewPager.setAdapter(mGalleryAdapter);
			}
        });        
        
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				
				mCurrentPosition = position;

				mCurFood = mFoods.get(position);
				
				mHandler.sendEmptyMessage(0);
				
				if(mGalleryChangeListener != null){
					mGalleryChangeListener.onGalleryChanged(mFoods.get(position), position);
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
        
        //set popup's width by different resolution
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int popupWidth = 640;
        switch(dm.densityDpi){
			case DisplayMetrics.DENSITY_LOW:
				break; 
			case DisplayMetrics.DENSITY_MEDIUM:
				//use default properties
				break;
			case DisplayMetrics.DENSITY_HIGH:
				popupWidth = 800;
				break;
			case DisplayMetrics.DENSITY_XHIGH:
				popupWidth = 1280;
				break;
        }
        
        //设置关联菜弹出框
		mComboPopup = new ExhibitPopupWindow(getActivity().getLayoutInflater().inflate(R.layout.gallery_fgm_combo, null),
											 popupWidth,
											 LayoutParams.WRAP_CONTENT);
		
		mComboPopup.setOperateListener(this);
		
		//关联菜按钮
		Button comboBtn = (Button) getView().findViewById(R.id.button_galleryFgm_ComboFood);
		comboBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCurFood != null){
					mComboPopup.showAssociatedFoods(v, 50, 10, mCurFood);
				}
			}
		});
		
		
		//设置简介弹出框
		mIntroPopup = new PopupWindow(getActivity().getLayoutInflater().inflate(R.layout.gallery_fgm_intro, null),
									  LayoutParams.WRAP_CONTENT,
									  LayoutParams.WRAP_CONTENT);
		mIntroPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_small));
		mIntroPopup.setOutsideTouchable(true);
		
		//点击菜名弹出简介
		((TextView) getView().findViewById(R.id.textView_foodName_galleryFgm)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCurFood != null && mCurFood.getDesc() != null && !mCurFood.getDesc().equals("")){
					((TextView)mIntroPopup.getContentView().findViewById(R.id.textView_galleryFgm_intro)).setText(mCurFood.getDesc());
					mIntroPopup.showAsDropDown(v, 0, -100);
				} else {
					Toast.makeText(getActivity(), "此菜没有简介", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	@Override
	public void onStart() {
		super.onStart();
		mHandler.sendEmptyMessage(0);
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
				if(mIntroPopup != null && mCurFood != null && getView() != null && !mCurFood.getDesc().equals("")){
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
	
	private void notifyDataSetChanged(DepartmentTree deptTree){
		mSearchHandler.refreshSrcFoods(WirelessOrder.foodMenu.foods);
		mFoods = deptTree.asFoodList();
		mCurrentPosition = 0;
		if(!mFoods.isEmpty()){
			mCurFood = mFoods.get(0);
			if(!mFoods.isEmpty()){
				setPosition(0);
			}
		}
	}
	
//	private void notifyDataSetChanged(List<OrderFood> datas){
//		if(!datas.isEmpty()){
//			mSearchHandler.refreshSrcFoods(WirelessOrder.foodMenu.foods);
//			
//	    	mFoods.clear();
//	    	mFoodPosByKitchenMap.clear();
//	    	mFoodPos.clear();
//	    	
//			Food firstFood = datas.get(0);
//			int firstPos = 0;
//			
//			mFoodPosByKitchenMap.put(firstFood.getKitchen(), firstPos);
//			
//	    	for(OrderFood foodParcel : datas){
//	    		
//	    		mFoods.add(foodParcel);
//	    		
//	    		//设置菜品和对应首张图片位置
//	    		mFoodPos.put(foodParcel, firstPos);
//	    		
//	    		//设置厨房和对应菜品首张图片位置
//	   			if(!foodParcel.getKitchen().equals(firstFood.getKitchen())){
//	    			firstFood = foodParcel;
//	    			mFoodPosByKitchenMap.put(firstFood.getKitchen(), firstPos);
//	    		}
//	   			firstPos++;
//	    	}   
//		}
//	}
	
	public ImageFetcher getImgFetcher(){
		return mImgFetcher;
	}
	
	public void refresh() {
		mHandler.sendEmptyMessage(0);
	}

	@Override
	public void onSearchItemClick(Food food) {
		if(food.hasImage()){
			this.setPosByFood(food);
		}else {
			Toast toast = Toast.makeText(getActivity(), "此菜暂无图片可展示", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP|Gravity.RIGHT, 230, 100);
			toast.show();
		}
	}
	
	
//	private class SignalHolder{
//		private static final int SPE_SIGNAL = 100;
//		private static final int HOT_SIGNAL = 102;
//		private static final int REC_SIGNAL = 103;
//		private List<Integer> mSignals;
//		
//		SignalHolder(OrderFood food){
//			
//			mSignals = new ArrayList<Integer>();
//			if(food.isSpecial())
//				mSignals.add(SPE_SIGNAL);
//			if(food.isHot())
//				mSignals.add(HOT_SIGNAL);
//			if(food.isRecommend())
//				mSignals.add(REC_SIGNAL);
//			
//			refreshDisplay();
//		}
//		
//		private void refreshDisplay(){
//			View fgmView = getView();
//			dismissAllSignals();
//			for (int i = 0; i < mSignals.size(); i++) {
//				Integer sign = mSignals.get(i);
//				if(i == 0){
//					switch(sign){
//					case SPE_SIGNAL:
//						(fgmView.findViewById(R.id.imageButton_special_galleryFgm)).setVisibility(View.VISIBLE);
//						break;
//					case HOT_SIGNAL:
//						(fgmView.findViewById(R.id.imageView_galleryFgm_hotSignal)).setVisibility(View.VISIBLE);
//						break;
//					case REC_SIGNAL:
//						(fgmView.findViewById(R.id.imageView_galleryFgm_recSignal)).setVisibility(View.VISIBLE);
//						break;
//					}
//				} else {
//					switch(sign){
//					case SPE_SIGNAL:
//						break;
//					case HOT_SIGNAL:
//						fgmView.findViewById(R.id.imageView_galleryFgm_hotSmall).setVisibility(View.VISIBLE);
//						break;
//					case REC_SIGNAL:
//						fgmView.findViewById(R.id.imageView_galleryFgm_recSmall).setVisibility(View.VISIBLE);
//						break;
//					}
//				}
//			}
//		}
//		
//		private void dismissAllSignals(){
//			View fgmView = getView();
//			(fgmView .findViewById(R.id.imageButton_special_galleryFgm)).setVisibility(View.GONE);
//			(fgmView.findViewById(R.id.imageView_galleryFgm_hotSignal)).setVisibility(View.GONE);
//			(fgmView.findViewById(R.id.imageView_galleryFgm_recSignal)).setVisibility(View.GONE);
//			fgmView.findViewById(R.id.imageView_galleryFgm_hotSmall).setVisibility(View.GONE);
//			fgmView.findViewById(R.id.imageView_galleryFgm_recSmall).setVisibility(View.GONE);
//
//		}
//	}

	/**
	 * 点击后跳转到相应的菜品
	 */
	@Override
	public void onFoodClicked(Food clickedFood) {
		if(clickedFood != null){
			setPosByFood(clickedFood);
		}
	}
}

