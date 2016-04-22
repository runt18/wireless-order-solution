package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.wireless.lib.task.QuerySellOutTask;
import com.wireless.ordermenu.R;
import com.wireless.parcel.DepartmentTreeParcel;
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.DepartmentTree;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodUnit;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.ComboFoodActivity;
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
				
				OrderFood foodHasOrdered = ShoppingCart.instance().searchInNew(mFragment.get().mCurFood);
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
					if(mFragment.get().mCurFood.hasFoodUnit()){
						((TextView) fgmView.findViewById(R.id.textView_priceTag_galleryFgm)).setText("多单位");
						((TextView) fgmView.findViewById(R.id.textView_price_galleryFgm)).setVisibility(View.GONE);
					}else{
						((TextView) fgmView.findViewById(R.id.textView_priceTag_galleryFgm)).setText("单价:");
						((TextView) fgmView.findViewById(R.id.textView_price_galleryFgm)).setVisibility(View.VISIBLE);
						((TextView) fgmView.findViewById(R.id.textView_price_galleryFgm)).setText(NumericUtil.float2String2(mFragment.get().mCurFood.getPrice()));
					}
					Food curFood = mFragment.get().mCurFood;
					if(curFood.isCombo())
						((TextView) fgmView.findViewById(R.id.button_galleryFgm_detail)).setText("套餐详情");
					else ((TextView) fgmView.findViewById(R.id.button_galleryFgm_detail)).setText("菜品详情");

					//更新菜品属性
					updateFoodStatus(fgmView);
				}
			}

		}
		
		private void updateFoodStatus(View fgmView){
			fgmView.findViewById(R.id.imageView_galleryFgm_sellOutSmall).setVisibility(View.GONE);
			fgmView.findViewById(R.id.imageButton_special_galleryFgm).setVisibility(View.GONE);
			fgmView.findViewById(R.id.imageView_galleryFgm_hotSignal).setVisibility(View.GONE);
			fgmView.findViewById(R.id.imageView_galleryFgm_recSignal).setVisibility(View.GONE);
			fgmView.findViewById(R.id.imageView_galleryFgm_hotSmall).setVisibility(View.GONE);
			fgmView.findViewById(R.id.imageView_galleryFgm_recSmall).setVisibility(View.GONE);
			
			if(mFragment.get().mCurFood.isSellOut()){
				fgmView.findViewById(R.id.imageView_galleryFgm_sellOutSmall).setVisibility(View.VISIBLE);
			}else{
				fgmView.findViewById(R.id.imageView_galleryFgm_sellOutSmall).setVisibility(View.GONE);
			}
			if(mFragment.get().mCurFood.isSpecial()){
				(fgmView.findViewById(R.id.imageButton_special_galleryFgm)).setVisibility(View.VISIBLE);
			}else{
				(fgmView.findViewById(R.id.imageButton_special_galleryFgm)).setVisibility(View.GONE);
			}
			if(mFragment.get().mCurFood.isHot()){
				fgmView.findViewById(R.id.imageView_galleryFgm_hotSmall).setVisibility(View.VISIBLE);
			}else{
				fgmView.findViewById(R.id.imageView_galleryFgm_hotSmall).setVisibility(View.GONE);
			}
			if(mFragment.get().mCurFood.isRecommend()){
				fgmView.findViewById(R.id.imageView_galleryFgm_recSmall).setVisibility(View.VISIBLE);
			}else{
				fgmView.findViewById(R.id.imageView_galleryFgm_recSmall).setVisibility(View.GONE);
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
	
	//更新估清菜品Task
	private QuerySellOutTask sellOutTask;
	
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
					final OrderFood of = new OrderFood(mCurFood);
					of.setCount(1f);
					
					if(mCurFood.hasFoodUnit()){
						List<String> items = new ArrayList<String>();
						for(FoodUnit unit : mCurFood.getFoodUnits()){
							items.add(unit.toString());
						}
						new AlertDialog.Builder(getActivity()).setTitle(of.getName())
						   .setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog, int which) {
								try {
									of.setFoodUnit(mCurFood.getFoodUnits().get(which));
									ShoppingCart.instance().addFood(of);
									mHandler.sendEmptyMessage(0);
								} catch (BusinessException e) {
									Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
								}
							}
						}).setNegativeButton("返回", null).show();
						
					}else{

						ShoppingCart.instance().addFood(of);
	
						mHandler.sendEmptyMessage(0);
						
						//Perform to show the associated foods
						if(!mComboPopup.isShowing()){
							getView().findViewById(R.id.button_galleryFgm_ComboFood).performClick();
						}
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
						intent = new Intent(getActivity(), ComboFoodActivity.class);
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
				
				//更新估清菜品的状态
				if(sellOutTask == null || sellOutTask.getStatus() == AsyncTask.Status.FINISHED){
					sellOutTask = new QuerySellOutTask(WirelessOrder.loginStaff, WirelessOrder.foods) {
						
						@Override
						public void onSuccess(List<Food> sellOutFoods) {
							for(Food f : mFoods){
								f.setSellOut(false);
								f.setLimit(false);
								f.setLimitAmount(0);
								f.setLimitRemaing(0);
							}
							
							for(Food sellOut : sellOutFoods){
								int index = mFoods.indexOf(sellOut);
								if(index >= 0){
									mFoods.get(index).setStatus(sellOut.getStatus());
									mFoods.get(index).setStatus(sellOut.getStatus());
									if(mFoods.get(index).isLimit()){
										mFoods.get(index).setLimitAmount(sellOut.getLimitAmount());
										mFoods.get(index).setLimitRemaing(sellOut.getLimitRemaing());
									}
								}
							}
						}
						
						@Override
						public void onFail(BusinessException arg0) {
							
						}
					};
					sellOutTask.execute();
				}
				
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
		mComboPopup = new ExhibitPopupWindow(getActivity().getLayoutInflater().inflate(R.layout.gallery_fgm_combo, (ViewGroup)this.getActivity().getWindow().getDecorView(), false),
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
		mIntroPopup = new PopupWindow(getActivity().getLayoutInflater().inflate(R.layout.gallery_fgm_intro, (ViewGroup)this.getActivity().getWindow().getDecorView(), false),
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
					//设置简介高度
			        DisplayMetrics dm = new DisplayMetrics();
			        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
			        int introPopupPosition = -240;
			        switch(dm.densityDpi){
						case DisplayMetrics.DENSITY_LOW:
							break; 
						case DisplayMetrics.DENSITY_MEDIUM:
							//use default properties
							break;
						case DisplayMetrics.DENSITY_HIGH:
							introPopupPosition = -360;
							break;
						case DisplayMetrics.DENSITY_XXHIGH:
						case DisplayMetrics.DENSITY_XHIGH:
							introPopupPosition = -480;
							break;
			        }
			        
					mIntroPopup.showAsDropDown(v, 0, introPopupPosition);
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
		}, 2400);
	}
	
	private void notifyDataSetChanged(DepartmentTree deptTree){
		mFoods = deptTree.asFoodList();
		mCurrentPosition = 0;
		if(!mFoods.isEmpty()){
			mCurFood = mFoods.get(0);
			setPosition(0);
		}
	}
	
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
			toast.setGravity(Gravity.TOP | Gravity.END, 230, 100);
			toast.show();
		}
	}
	
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

