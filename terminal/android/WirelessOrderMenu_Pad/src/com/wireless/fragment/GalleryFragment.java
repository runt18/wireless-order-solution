package com.wireless.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wireless.ordermenu.R;
import com.wireless.protocol.Food;
import com.wireless.util.imgFetcher.ImageCache;
import com.wireless.util.imgFetcher.ImageFetcher;

public class GalleryFragment extends Fragment {
	private ImageView.ScaleType mCurScaleType = ImageView.ScaleType.CENTER_CROP;
	private FragmentStatePagerAdapter mGalleryAdapter = null;
	//private Gallery mGallery;
	private ViewPager mViewPager;
	private List<Food> mFoods = new ArrayList<Food>();
	private ImageFetcher mImgFetcher;
	
	public interface OnPicChangedListener{
		void onPicChanged(Food curFood, int position);
	}
	
	private OnPicChangedListener mPicChangeListener;
	
	
	public static interface OnPicClickedListener{
		void onPicClicked(Food food , int position);
	}
	
	OnPicClickedListener mOnPicClickListener;	

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.content_layout, container, false);
	}
	
	/**
	 * 根据position设置画廊显示的图片
	 * @param position
	 */
	public void setPosition(int position){
		mViewPager.setCurrentItem(position);
	}
	
	/**
	 * 根据Food设置画廊显示的图片
	 * @param foodToSet
	 */
	public void setPosition(Food foodToSet){
		int pos = 0;
		for(Food food : mFoods){
			if(food.equals(foodToSet)){
				mViewPager.setCurrentItem(pos);
				break;
			}
			pos++;
		}
	}
	
	public void notifyDataChanged(ArrayList<Food> foods){
		mFoods = foods;
		mGalleryAdapter.notifyDataSetChanged();
	}
	
	public void setScaleType(ImageView.ScaleType type){
		mCurScaleType = type;
	}
	
	/**
	 * 设置新的Gallery数据源，并更新Gallery
	 * @param foods
	 */
	public void notifyDataChanged(Food[] foods){
		mFoods = Arrays.asList(foods);
		mGalleryAdapter.notifyDataSetChanged();		
	}
	
	public int getSelectedPosition(){
		return mViewPager.getCurrentItem();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		
		super.onActivityCreated(savedInstanceState);		

		try{
			mOnPicClickListener = (OnPicClickedListener)getActivity();
		}catch(ClassCastException e){
			
		}
		
		try{
			mPicChangeListener = (OnPicChangedListener)getActivity();
		}catch(ClassCastException e){
			
		}
		
    	mImgFetcher = new ImageFetcher(getActivity(), 
				GalleryFragment.this.getView().getWidth(), 
				GalleryFragment.this.getView().getHeight());
    	
    	mImgFetcher.addImageCache(getFragmentManager(), new ImageCache.ImageCacheParams(getActivity()));
		
        mViewPager = (ViewPager) this.getView().findViewById(R.id.picViewPager);
        mViewPager.setOffscreenPageLimit(2);
        
        mGalleryAdapter = new FragmentStatePagerAdapter (getFragmentManager()){
        	
            @Override
            public int getCount() {
                return mFoods.size();
            }
        	
            @Override
            public Fragment getItem(int position) {
                return ImageDetailFragment.newInstance(mFoods.get(position), GalleryFragment.this.getId());
            }            


//        	// 此方法是最主要的，他设置好的ImageView对象返回给Gallery
//        	@Override
//        	public View getView(int position, View convertView, ViewGroup parent) {
//        		ImageView imageView;
//        		if(convertView == null){
//        			convertView = new ImageView(getActivity());
//        			imageView = (ImageView)convertView;
//        			// 设置ImageView的伸缩规格，用了自带的属性值
//        			imageView.setScaleType(curScaleType);
//        			
//        		}else {
//        			imageView = (ImageView)convertView;
//        		}
//        		imageView.setAdjustViewBounds(true);
//        		//imageView.setImageBitmap(mImgLoader.loadImage(mFoods.get(position).image));
//        		mImgFetcher.loadImage(mFoods.get(position).image, imageView);
//        		return imageView;
//        	}
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

