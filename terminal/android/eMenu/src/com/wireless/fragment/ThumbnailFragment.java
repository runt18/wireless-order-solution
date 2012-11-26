package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.wireless.common.ShoppingCart;
import com.wireless.excep.BusinessException;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.util.imgFetcher.ImageCache.ImageCacheParams;
import com.wireless.util.imgFetcher.ImageFetcher;

public class ThumbnailFragment extends Fragment {
	private static final String KEY_SOURCE_FOODS = "keySourceFoods";
	private static int ITEM_AMOUNT_PER_PAGE = 6;
	private ImageFetcher mImageFetcher;
	
	private ViewPager mViewPager;
	
	ArrayList<List<OrderFood>> mSortedFoods = new ArrayList<List<OrderFood>>();
	
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

		mImageFetcher = new ImageFetcher(getActivity(), 0, 0);
//        mImageFetcher.setLoadingImage(R.drawable.null_pic);
        mImageFetcher.addImageCache(getActivity().getFragmentManager(), cacheParams);
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
    	
    	setFoodDatas(srcFoods);
    	
    	mViewPager = (ViewPager) view.findViewById(R.id.viewPager_thumbnailFgm);
        mViewPager.setOffscreenPageLimit(0);

        final FragmentStatePagerAdapter mPagerAdapter = new FragmentStatePagerAdapter(getFragmentManager()) {
			
			@Override
			public int getCount() {
				return mSortedFoods.size();
			}
			
			@Override
			public Fragment getItem(int arg0) {
				return ThumbnailItemFragment.newInstance(mSortedFoods.get(arg0), ThumbnailFragment.this.getId());
			}
		};
		
        mViewPager.post(new Runnable(){

			@Override
			public void run() {
				mViewPager.setAdapter(mPagerAdapter);
			}
        });     
        
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {			
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
//				//隐藏键盘
//				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//				imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
//				
//				if(!mSearchEditText.getText().toString().equals(""))
//					mSearchEditText.setText("");
				
				if(state == ViewPager.SCROLL_STATE_DRAGGING){
					mImageFetcher.setPauseWork(true);
				} else if(state == ViewPager.SCROLL_STATE_IDLE){
					mImageFetcher.setPauseWork(false);
				}
			}
		});
		return view;
	}
	
    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
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
        mImageFetcher.clearCache();
        mImageFetcher.closeCache();
    }
	/**
	 * 根据传入的数据 整理成6个一组
	 * @param srcFoods
	 */
	public void setFoodDatas(ArrayList<OrderFood> srcFoods){
		if(srcFoods == null)
			return ;
		int tLength = srcFoods.size();
		// 计算屏幕的页数
		int pageSize = (tLength / ITEM_AMOUNT_PER_PAGE) + (tLength	% ITEM_AMOUNT_PER_PAGE == 0 ? 0 : 1);
		mSortedFoods.clear();
		
		for(int pageNo=0; pageNo < pageSize; pageNo++){
			// 获取显示在此page显示的food对象
			ArrayList<OrderFood> food4Page = new ArrayList<OrderFood>();
			for (int i = 0; i < ITEM_AMOUNT_PER_PAGE; i++) {
				int index = pageNo * ITEM_AMOUNT_PER_PAGE + i;
				if (index < tLength) {
					food4Page.add(srcFoods.get(index));
				} else {
					break;
				}
			}
			mSortedFoods.add(food4Page);
		}
	}
	
	public ImageFetcher getImageFetcher(){
		return mImageFetcher;
	}
}


