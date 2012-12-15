package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.OrderFood;
import com.wireless.util.SearchFoodHandler;
import com.wireless.util.SearchFoodHandler.OnSearchItemClickListener;
import com.wireless.util.imgFetcher.ImageCache.ImageCacheParams;
import com.wireless.util.imgFetcher.ImageFetcher;

public class ThumbnailFragment extends Fragment implements OnSearchItemClickListener {
	private static final String KEY_SOURCE_FOODS = "keySourceFoods";
	private static int ITEM_AMOUNT_PER_PAGE = 6;
	private ImageFetcher mImageFetcher;
	
	private int mCurrentPos;
	
	private ViewPager mViewPager;
	
	private OnThumbnailChangedListener mThumbnailChangedListener;
	
	List<Entry<List<OrderFood>, OrderFood>> mGroupedFoods = new ArrayList<Entry<List<OrderFood>, OrderFood>>();
	protected SearchFoodHandler mSearchHandler;
	
	public static interface OnThumbnailChangedListener{
		public void onThumbnailChanged(List<OrderFood> foodsToCurrentGroup, OrderFood captainToCurrentGroup, int pos);
	}
	
	public void setThumbnailChangedListener(OnThumbnailChangedListener thumbnailChangedListener){
		mThumbnailChangedListener = thumbnailChangedListener;
	}
	
	public static ThumbnailFragment newInstance(ArrayList<Food> srcFoods){
		ThumbnailFragment fgm = new ThumbnailFragment();
		
		Bundle args = new Bundle();
		ArrayList<FoodParcel> foodParcels = new ArrayList<FoodParcel>();
		for(Food f: srcFoods){
			foodParcels.add(new FoodParcel(new OrderFood(f)));
		}
		args.putParcelableArrayList(KEY_SOURCE_FOODS, foodParcels);
		fgm.setArguments(args);
		
		return fgm;
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), 0.2f);
        //cacheParams.setMemCacheSizePercent(getActivity(), 0.25f);

		mImageFetcher = new ImageFetcher(getActivity(), 320, 300);
//        mImageFetcher.setLoadingImage(R.drawable.null_pic);
        mImageFetcher.addImageCache(getActivity().getFragmentManager(), cacheParams, "ThumbnailFragment");
        
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.thumbnail_fragment, null);
		
		Bundle args = getArguments();
		
    	ArrayList<FoodParcel> foodParcels = args.getParcelableArrayList(KEY_SOURCE_FOODS);
    	ArrayList<OrderFood> srcFoods = new ArrayList<OrderFood>();
    	for(FoodParcel foodParcel : foodParcels){
    		srcFoods.add(foodParcel);
    	}
    	
    	notifyDataSetChanged(srcFoods);
    	
    	mViewPager = (ViewPager) view.findViewById(R.id.viewPager_thumbnailFgm);
        mViewPager.setOffscreenPageLimit(0);
//        resetAdapter();
        
		final AutoCompleteTextView mSearchEditText = (AutoCompleteTextView) view.findViewById(R.id.editText_thumbnailFgm);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				mCurrentPos = position;
				if(mThumbnailChangedListener != null){
					mThumbnailChangedListener.onThumbnailChanged(mGroupedFoods.get(position).getKey(), mGroupedFoods.get(position).getValue(), position);
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { 			
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
//				//隐藏键盘
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
				
				if(!mSearchEditText.getText().toString().equals(""))
					mSearchEditText.setText("");
				
				if(state == ViewPager.SCROLL_STATE_DRAGGING){
					mImageFetcher.setPauseWork(true);
				} else if(state == ViewPager.SCROLL_STATE_IDLE){
					mImageFetcher.setPauseWork(false);
				}
			}
		});
        
		Button clearSearchBtn = (Button) view.findViewById(R.id.button_thumbnailFgm_clear);
		//搜索框
		mSearchHandler = new SearchFoodHandler(this, mSearchEditText, clearSearchBtn);
		mSearchHandler.setOnSearchItemClickListener(this);
		return view;
	}
	public void resetAdapter(){
		refreshFoodCount();
        final FragmentStatePagerAdapter mPagerAdapter = new FragmentStatePagerAdapter(getFragmentManager()) {
			
			@Override
			public int getCount() {
				return mGroupedFoods.size();
			}
			
			@Override
			public Fragment getItem(int position) {
				return ThumbnailItemFragment.newInstance(mGroupedFoods.get(position).getKey(), ThumbnailFragment.this.getId());
			}
		};
		
        mViewPager.post(new Runnable(){

			@Override
			public void run() {
				mViewPager.setAdapter(mPagerAdapter);
			}
        });     
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);	
		try{
			mThumbnailChangedListener = (OnThumbnailChangedListener)getActivity();
		}catch(ClassCastException e){
			
		}
	}
	
    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        (getView().findViewById(R.id.editText_thumbnailFgm)).clearFocus();
        
        this.resetAdapter();
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mImageFetcher.clearCache();
        mImageFetcher.closeCache();
//        mImageFetcherForSearch.clearCache();
    }
    
	/**
	 * 根据传入的数据 整理成6个一组
	 * @param srcFoods
	 */
	public void notifyDataSetChanged(ArrayList<OrderFood> srcFoods){
		if(mSearchHandler != null)
			mSearchHandler.refreshSrcFoods(WirelessOrder.foodMenu.foods);
		
		if(srcFoods != null){
			int tLength = srcFoods.size();
			// 计算屏幕的页数
			int pageSize = (tLength / ITEM_AMOUNT_PER_PAGE) + (tLength	% ITEM_AMOUNT_PER_PAGE == 0 ? 0 : 1);
			mGroupedFoods.clear();
			mCurrentPos = 0;
			for(int pageNo = 0; pageNo < pageSize; pageNo++){
				// 获取显示在此page显示的food对象
				final ArrayList<OrderFood> foodsToEachPage = new ArrayList<OrderFood>();
				for (int i = 0; i < ITEM_AMOUNT_PER_PAGE; i++) {
					int index = pageNo * ITEM_AMOUNT_PER_PAGE + i;
					if (index < tLength) {
						foodsToEachPage.add(srcFoods.get(index));
					} else {
						break;
					}
				}
				mGroupedFoods.add(new Entry<List<OrderFood>, OrderFood>(){

					private List<OrderFood> mFoods = foodsToEachPage;
					private OrderFood mCaptainFood= foodsToEachPage.get(0);
					
					@Override
					public List<OrderFood> getKey() {
						return mFoods;
					}

					@Override
					public OrderFood getValue() {
						return mCaptainFood;
					}

					@Override
					public OrderFood setValue(OrderFood newCaptain) {
						mCaptainFood = newCaptain;
						return mCaptainFood;
					}
					
				});
			}
		}
	}
	
	public ImageFetcher getImageFetcher(){
		return mImageFetcher;
	}
	
	/**
	 * Get the current group along with foods and captain.
	 * @return the current group along with foods and captain
	 */
	public Entry<List<OrderFood>, OrderFood> getCurGroup(){
		return mGroupedFoods.get(mCurrentPos);
	}
	
	/**
	 * Set the show the page according to specific position.
	 * @param pos the position to set
	 */
	private void setPosition(int pos){
		if(mCurrentPos != pos){
			mViewPager.setCurrentItem(pos, false);
			mCurrentPos = pos;
		}
	}
	
	/**
	 * Set the page to show according to a specific kitchen.
	 * @param kitchen the kitchen to search
	 */
	public void setPosByKitchen(Kitchen kitchen){
		int nCnt = 0;
		for(Entry<List<OrderFood>, OrderFood> entry : mGroupedFoods){
			for(OrderFood of : entry.getKey()){
				if(of.kitchen.equals(kitchen)){
					entry.setValue(of);
					setPosition(nCnt);
					return;
				}
			}
			nCnt++;
		}
	}
	
	/**
	 * Set the page to show according to a specific food.
	 * @param food the food to search
	 */
	public void setPosByFood(Food food){
		setPosByFood(new OrderFood(food));
	}
	
	/**
	 * Set the page to show according to a specific food.
	 * @param food the food to search
	 */
	public void setPosByFood(OrderFood food){
		int nCnt = 0;
		for(Entry<List<OrderFood>, OrderFood> entry : mGroupedFoods){
			for(OrderFood f : entry.getKey()){
				if(f.equals(food)){
					entry.setValue(f);
					setPosition(nCnt);
					return;
				}
			}
			nCnt++;
		}
	}
	
	public void  clearFoodCount(){
		for(Entry<List<OrderFood>, OrderFood> entry : mGroupedFoods){
			for(OrderFood f : entry.getKey()){
				f.setCount(0f);
			}
		}
	}
	
	public void refreshFoodCount(){
		for(Entry<List<OrderFood>, OrderFood> entry : mGroupedFoods){
			for(OrderFood f : entry.getKey()){
				OrderFood orderFood = ShoppingCart.instance().getFood(f.getAliasId());
				if(orderFood != null)
					f.setCount(orderFood.getCount());
			}
		}
	}

	@Override
	public void onSearchItemClick(Food food) {
		this.setPosByFood(food);
	}
}


