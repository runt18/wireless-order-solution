package com.wireless.ui;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.wireless.ordermenu.R;
import com.wireless.protocol.Food;
import com.wireless.util.ImageLoader;

public class ContentFragment extends Fragment {
	private BaseAdapter mGalleryAdapter = null;
	Gallery mGallery;
	Handler mHandler;
	ArrayList<Food> mFoods = new ArrayList<Food>();
	
	public interface OnViewChangeListener{
		void onViewChange(Food value,int position);
	}
	
	public static OnViewChangeListener sDummyListener = new OnViewChangeListener() {
		@Override
		public void onViewChange(Food value,int position) {}
	};
	
	public static OnViewChangeListener mOnViewChangeListener = sDummyListener;
	
	public void setOnViewChangeListener(OnViewChangeListener l){
		mOnViewChangeListener = l;
	}
	
    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
	   return inflater.inflate(R.layout.content_layout, container, false);
   }

	public void setContentPosition(int position) {
		Log.i("set position",""+position);
		mGallery.setSelection(position);
	}
	
	public void setContent(ArrayList<Food> foods){
		mFoods = foods;
		mGalleryAdapter.notifyDataSetChanged();
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
        		//mImgLoader.loadImageEx(imageView, mFoods.get(position).image);
        		
        		return imageView;
        	}
        };
        
        mGallery.setAdapter(mGalleryAdapter);        
        
        mGallery.setCallbackDuringFling(true);
        mGallery.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
				mOnViewChangeListener.onViewChange(mFoods.get(position),position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
        });
	}
}

