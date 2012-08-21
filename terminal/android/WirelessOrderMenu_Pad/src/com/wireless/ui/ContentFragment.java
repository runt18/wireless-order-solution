package com.wireless.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.wireless.common.Params;
import com.wireless.ordermenu.R;

public class ContentFragment extends Fragment {
	ImageAdapter mAdapter = null;
	Gallery mGallery;
	Handler mHandler;
	
	public interface OnViewChangeListener{
		void onViewChange(int value);
	}
	
	public static OnViewChangeListener sDummyListener = new OnViewChangeListener() {
		@Override
		public void onViewChange(int value) {}
	};
	
	public static OnViewChangeListener mOnViewChangeListener = sDummyListener;
	
	public void setOnViewChangeListener(OnViewChangeListener l)
	{	mOnViewChangeListener = l;}
	
   @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState) {  
	   View view = inflater.inflate(R.layout.content_layout, container,false);
	   return view;
   }

	public void onUpdateContent(int position) {
		mGallery.setSelection(position);
		Log.i("#########@@@@@@",""+position);
	}
	
	public void setContent(ArrayList<String> imageNames)
	{
		mAdapter.setImages(imageNames);
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
        mGallery = (Gallery)this.getActivity().findViewById(R.id.gallery1);
        mHandler = new Handler();
        mAdapter = new ImageAdapter(this.getActivity(),mHandler);
		File imagesDir = Environment.getExternalStorageDirectory();
		if(imagesDir != null)
			mAdapter.setStoreDir(imagesDir);
        mGallery.setAdapter(mAdapter);
        mGallery.setCallbackDuringFling(true);
        mGallery.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
				// TODO Auto-generated method stub
				mOnViewChangeListener.onViewChange(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
        });
	}
}

class ImageAdapter extends BaseAdapter {
	private final String NULL = "sign_null";
	private File mStoreDir = null;
	private Context mContext;
	private AsynImageLoader mImgLoader;
	// 图片的资源ID
	private ArrayList<String> mImgPaths = new ArrayList<String>();
	private Bitmap mDefaultBitmap ;

	// 构造函数
	public ImageAdapter(Context context,Handler handler) {
		this.mContext = context;
		mImgLoader = new AsynImageLoader(handler);
		mDefaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.loading_pic);
	}

	// 返回所有图片的个数
	@Override
	public int getCount() {
		return mImgPaths.size();
	}

	// 返回图片路径
	@Override
	public Object getItem(int position) {
		return mImgPaths.get(position);
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
		if(convertView == null)
		{
			convertView = new ImageView(mContext);
			imageView = (ImageView)convertView;
			// 设置ImageView的伸缩规格，用了自带的属性值
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		}
		else {
			imageView = (ImageView)convertView;
		}
		//XXX
		String filePath = mImgPaths.get(position);
		if(filePath != NULL)
		{
//			Bitmap bm = BitmapFactory.decodeFile(filePath);
			// 通过索引获得图片并设置给ImageView
//			imageView.setImageBitmap(bm);
//			imageView.setImageResource(R.drawable.ic_launcher);
			imageView.setTag(filePath);
			mImgLoader.loadBitmap(imageView,mDefaultBitmap);
		}
		else {
			imageView.setImageResource(R.drawable.null_pic);
		}
		

		
		return imageView;
	}
	
	public void setStoreDir(File dir)
	{
		mStoreDir = dir;
		String storeDirString = mStoreDir.toString();
		if(storeDirString.endsWith("/"))
			mStoreDir = new File(storeDirString+Params.IMG_STORE_PATH);
		else mStoreDir = new File(storeDirString+"/"+Params.IMG_STORE_PATH);
//		Log.i("dir ", storeDir.getAbsolutePath());
//		Log.i("dir string",  storeDirString);

	}
	
	/*
	 * 用 “储存路径” + 图片名称 组成完整路径
	 * 在路径中读取图片
	 * 判断组合的路径中的图片是否真实存在
	 */
	 public void setImages(ArrayList<String> imageNames){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			if(!mStoreDir.exists())
				mStoreDir.mkdir();
			
			mImgPaths.clear();
			 HashSet<String> existImgs = new HashSet<String>();
	
			 if(mStoreDir != null)
			 {
				 File[] allFiles = mStoreDir.listFiles();
				 for(File f:allFiles)
				 {
					 existImgs.add(f.getAbsolutePath());
//					 Log.i("abs ",f.getAbsolutePath());
				 }
			 }
			 
			 String storePath = mStoreDir.getAbsolutePath();
			 for(String s:imageNames)
			 {
//				 Log.i(s,"*********@");
				 String imgAbsPath = storePath+"/"+s;
//				 Log.i("ffffffffffffff",imgAbsPath);
				 if(existImgs.contains(imgAbsPath)){
					 mImgPaths.add(imgAbsPath);
//					 Log.i("rrrrrrrrrrrr",imgAbsPath);
				 }
				 else {
//					 Log.i("ddddddddddd","9");
					 mImgPaths.add(NULL);
				 }
			 }
//			 Log.i(NULL,""+imgPaths.size());
		}

	}
}
