package com.wireless.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.excep.BusinessException;
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
import com.wireless.util.SearchRunnable;
import com.wireless.util.imgFetcher.ImageCache;
import com.wireless.util.imgFetcher.ImageFetcher;

public class GalleryFragment extends Fragment {
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
	private ImageFetcher mImgFetcher , mFetcherForSearch;
	
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
		return mFoods.get(mCurrentPosition);
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
	
//	/**
//	 * 设置新的Gallery数据源，并更新Gallery
//	 * @param foods
//	 */
//	public void notifyDataChanged(Food[] foods){
//		List<OrderFood> Allfoods = ShoppingCart.instance().getAllFoods();
//		for(int i = 0; i < foods.length; i++){
//			OrderFood of = new OrderFood(foods[i]);
//			
//			for(OrderFood foodOrdered : Allfoods){
//				if(foods[i].equals(foodOrdered)){
//					of.setCount(foodOrdered.getCount());
//					break;
//				}
//			}
//			mFoods.add(of);
//		}
//		mGalleryAdapter.notifyDataSetChanged();	
//		mCurrentPosition = 0;
//		mSearchHandler = new FoodSearchHandler(GalleryFragment.this);
//	}
	
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.content_layout, container, false);
		
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
					//否则打开新activity
					Intent intent = new Intent(getActivity(), FullScreenActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(mOrderFood));
					intent.putExtras(bundle);
					intent.putExtra(IS_IN_SUB_ACTIVITY, true);
					getActivity().startActivityForResult(intent, MainActivity.MAIN_ACTIVITY_RES_CODE);
				}
				
			}
		});
		
//		final SearchRunnable searchRun = new SearchRunnable();
		mFetcherForSearch = new ImageFetcher(getActivity(), 50, 50);

		//搜索框
		mSearchEditText = (AutoCompleteTextView) view.findViewById(R.id.editText_galleryFgm);
		mSearchHandler = new SearchFoodHandler(this, mFetcherForSearch, mSearchEditText);
		final SearchRunnable searchRun = new SearchRunnable(mSearchHandler);

//		mSearchEditText.clearFocus();
		final ImageButton clearSearchBtn = (ImageButton) view.findViewById(R.id.imageButton_galleryFgm_clear);
		//清除输入按钮
		clearSearchBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSearchEditText.setText("");  
				
				//隐藏键盘
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
			}
		});
		//设置弹出框背景
		mSearchEditText.setDropDownBackgroundResource(R.drawable.main_search_list_bg);
		//侦听输入字符改变
		mSearchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				String mFilterCond = s.length() == 0 ? "" : s.toString().trim();
				mSearchEditText.removeCallbacks(searchRun);
				//延迟500毫秒显示结果
				if(!mFilterCond.equals("")){
					searchRun.setmFilterCond(mFilterCond);
					mSearchEditText.postDelayed(searchRun, 500);
				}
			}
		});
		//侦听弹出框点击项
		mSearchEditText.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Food food = (Food) view.getTag();
				//清空edittext数据
				clearSearchBtn.performClick();
				//若有图片则跳转到相应的大图
				if(food.image != null)
				{
					GalleryFragment.this.setPosByFood(food);

				} else{
					Toast toast = Toast.makeText(GalleryFragment.this.getActivity(), "此菜暂无图片可展示", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP|Gravity.RIGHT, 0, 100);
					toast.show();
				}
//				//隐藏键盘
//				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//				imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
			
			}
		});
		
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

//					//显示弹出框
//					if(!mCountHintView.isShown())
//						mCountHintView.setVisibility(View.VISIBLE);
//					TextView countText = (TextView)mCountHintView.findViewById(R.id.textView_main_popup_count);
//					int count = Integer.parseInt(countText.getText().toString());
//					countText.setText(""+ ++count);
//					//一秒之后消失
//					mCountHintView.removeCallbacks(dismissRunnable);
//					mCountHintView.postDelayed(dismissRunnable, 1000);
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
				if(mOrderFood != null){
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
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);		

		mOrderFood = new OrderFood(WirelessOrder.foods[0]);
		
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
		int nCacheViews = DEFAULT_CACHE_VIEW_AMOUNT;
		ScaleType scaleType = DEFAULT_IMAGE_SCALE_TYPE;
		
        Bundle bundle = getArguments();
        if(bundle != null){
        	
        	percent = bundle.getFloat(KEY_MEMORY_CACHE_PERCENT);
        	nCacheViews = bundle.getInt(KEY_CACHE_VIEW_AMOUNT);
        	scaleType = ScaleType.values()[bundle.getInt(KEY_IMAGE_SCALE_TYPE)];
        	ArrayList<FoodParcel> foodParcels = bundle.getParcelableArrayList(KEY_SRC_FOODS);
        	
        	ArrayList<OrderFood> srcFoods = new ArrayList<OrderFood>();
        	srcFoods.addAll(foodParcels);
        	
        	notifyDataSetChanged(srcFoods);
        }
		
        //Create the image fetcher without the image size since it only can be retrieved later. 
    	mImgFetcher = new ImageFetcher(getActivity(), 0, 0);
    	//Add the image cache with the percent of memory to the application.
//    	mImgFetcher.setImageCache(new ImageCache(new ImageCacheParams(getActivity(), 0.1f)));
    	mImgFetcher.addImageCache(getFragmentManager(), new ImageCache.ImageCacheParams(getActivity(), percent), "GalleryFragment");
    	//Add the listener to retrieve the width and height of this fragment, then set them to image fetcher.
    	getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
    	     @Override
   	          public void onGlobalLayout() {
    	    	 mImgFetcher.setImageSize(getView().getWidth(), getView().getHeight());
    	    	 getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
   	          }
    	});
    	
        mViewPager = (ViewPager) this.getView().findViewById(R.id.picViewPager);
        mViewPager.setOffscreenPageLimit(nCacheViews);
        
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
		mFetcherForSearch.clearCache();
	}
	
	public void notifyDataSetChanged(ArrayList<OrderFood> datas){
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

		if(food.isSpecial())
			((ImageButton) fgmView.findViewById(R.id.imageButton_special_galleryFgm)).setVisibility(View.VISIBLE);
		else ((ImageButton)fgmView.findViewById(R.id.imageButton_special_galleryFgm)).setVisibility(View.GONE);

		if(food.isRecommend())
			((ImageButton)fgmView.findViewById(R.id.imageButton_rec_galleryFgm)).setVisibility(View.VISIBLE);
		else ((ImageButton)fgmView.findViewById(R.id.imageButton_rec_galleryFgm)).setVisibility(View.GONE);

		if(food.isCurPrice())
			((ImageButton)fgmView.findViewById(R.id.imageButton_current_galleryFgm)).setVisibility(View.VISIBLE);
		else ((ImageButton)fgmView.findViewById(R.id.imageButton_current_galleryFgm)).setVisibility(View.GONE);

		if(food.isHot())
			((ImageView) fgmView.findViewById(R.id.imageView_galleryFgm_hotSignal)).setVisibility(View.VISIBLE);
		else ((ImageView) fgmView.findViewById(R.id.imageView_galleryFgm_hotSignal)).setVisibility(View.GONE);
		
		if(food.isGift())
			((ImageView) fgmView.findViewById(R.id.imageView_galleryFgm_giftSignal)).setVisibility(View.VISIBLE);
		else ((ImageView) fgmView.findViewById(R.id.imageView_galleryFgm_giftSignal)).setVisibility(View.GONE);

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
}

