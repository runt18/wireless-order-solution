package com.wireless.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
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
	//private Gallery mGallery;
	private ViewPager mViewPager;
	private List<Food> mFoods = new ArrayList<Food>();
	private ImageFetcher mImgFetcher;
	
	private int mCurrentPosition = 0;
	
	public interface OnPicChangedListener{
		void onPicChanged(Food curFood, int position);
	}
	
	public void setOnPicChangedListener(OnPicChangedListener l)
	{
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
	 * 根据position设置画廊显示的图片
	 * @param position
	 */
	public void setPosition(int position){
		if(mCurrentPosition != position)
		{
			mViewPager.setCurrentItem(position);
			mCurrentPosition = position;
		}
	}
	
	/**
	 * 根据Food设置画廊显示的图片
	 * @param foodToSet
	 */
	public void setPosition(Food foodToSet){
		int pos = 0;
		for(Food food : mFoods){
			if(food.equals(foodToSet)){
				if(mCurrentPosition != pos)
				{
					mViewPager.setCurrentItem(pos);
					mCurrentPosition = pos;
				}
				break;
			}
			pos++;
		}
	}
	
	public void notifyDataChanged(ArrayList<Food> foods){
		mFoods = foods;
		mGalleryAdapter.notifyDataSetChanged();
		mCurrentPosition = 0;
	}
	
	/**
	 * 设置新的Gallery数据源，并更新Gallery
	 * @param foods
	 */
	public void notifyDataChanged(Food[] foods){
		mFoods = Arrays.asList(foods);
		mGalleryAdapter.notifyDataSetChanged();	
		mCurrentPosition = 0;
	}
	
	public int getSelectedPosition(){
		return mViewPager.getCurrentItem();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.content_layout, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		
		super.onActivityCreated(savedInstanceState);		

		try{
			mOnPicClickListener = (OnPicClickListener)getActivity();
		}catch(ClassCastException e){
			
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
        	mFoods.clear();
        	for(FoodParcel foodParcel : foodParcels){
        		mFoods.add(foodParcel);
        	}        	
        }
		
        //Create the image fetcher without the image size since it only can be retrieved later. 
    	mImgFetcher = new ImageFetcher(getActivity(), 0, 0);
    	//Add the image cache with the percent of memory to the application.
    	mImgFetcher.addImageCache(getFragmentManager(), new ImageCache.ImageCacheParams(getActivity(), percent));
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
        mGalleryAdapter = new FragmentStatePagerAdapter (getFragmentManager()){
        	
            @Override
            public int getCount() {
                return mFoods.size();
            }
        	
            @Override
            public Fragment getItem(int position) {
                return ImageDetailFragment.newInstance(mFoods.get(position), GalleryFragment.this.getId(), scale);
            }            
        };
        
        mViewPager.setAdapter(mGalleryAdapter);        
        
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				if(mPicChangeListener != null){
					mPicChangeListener.onPicChanged(mFoods.get(position), position);
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {			
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				
			}
		});

	}
	
	@Override 
	public void onStop(){
		super.onStop();
	}
	
	@Override 
	public void onDestroy(){
		super.onDestroy();
		mImgFetcher.clearCache();
	}
	
	public ImageFetcher getImgFetcher(){
		return mImgFetcher;
	}
}

