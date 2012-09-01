package com.wireless.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.wireless.ordermenu.R;
import com.wireless.protocol.Food;

public class GalleryFragment extends Fragment {
	
	private BaseAdapter mGalleryAdapter = null;
	private Gallery mGallery;
	private List<Food> mFoods = new ArrayList<Food>();
	
	public static interface OnPicChangedListener{
		void onPicChanged(Food value, int position);
	}
	
	private OnPicChangedListener mPicChangeListener;
	
	public void setOnViewChangeListener(OnPicChangedListener l){
		mPicChangeListener = l;
	}
	
	public static interface OnItemClickListener{
		void onItemClick(Food food , int position);
	}
	
	private OnItemClickListener mOnItemClickListener;
	
	public void setOnItemClickListener(OnItemClickListener l)
	{
		mOnItemClickListener = l;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.content_layout, container, false);
	}

	/**
	 * 根据position设置画廊显示的图片
	 * @param position
	 */
	public void setPosition(int position){
		mGallery.setSelection(position);
	}
	
	/**
	 * 根据Food设置画廊显示的图片
	 * @param foodToSet
	 */
	public void setPosition(Food foodToSet){
		int pos = 0;
		for(Food food : mFoods){
			if(food.equals(foodToSet)){
				mGallery.setSelection(pos);
				break;
			}
			pos++;
		}
	}
	
	//FIXME to be deleted
	public void setContent(ArrayList<Food> foods){
		mFoods = foods;
		mGalleryAdapter.notifyDataSetChanged();
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
       return mGallery.getSelectedItemPosition();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		
		super.onActivityCreated(savedInstanceState);
		
        mGallery = (Gallery)this.getActivity().findViewById(R.id.noneInertanceGallery1);
        mGalleryAdapter = new BaseAdapter(){
        	
        	private ImageLoader mImgLoader = new ImageLoader(getActivity());
        	
        	@Override
        	public int getCount() {
        		return mFoods.size();
        	}

        	// 返回图片路径
        	@Override
        	public Object getItem(int position) {
        		return mFoods.get(position);
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
        			convertView = new ImageView(getActivity());
        			imageView = (ImageView)convertView;
        			// 设置ImageView的伸缩规格，用了自带的属性值
        			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        			
        		}else {
        			imageView = (ImageView)convertView;
        		}
        		
        		imageView.setImageBitmap(mImgLoader.loadImage(mFoods.get(position).image));
        		return imageView;
        	}
        };
        
        mGallery.setAdapter(mGalleryAdapter);        
        
        mGallery.setCallbackDuringFling(true);
        mGallery.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
				if(mPicChangeListener != null){
					mPicChangeListener.onPicChanged(mFoods.get(position), position);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
        });
        
        mGallery.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent,View view, int position, long id) {
				if(mOnItemClickListener != null)
					mOnItemClickListener.onItemClick(mFoods.get(position), position);
			}
        });
	}
}

