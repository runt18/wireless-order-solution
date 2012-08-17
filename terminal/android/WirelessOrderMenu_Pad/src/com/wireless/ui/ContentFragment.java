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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.wireless.common.Params;
import com.wireless.ordermenu.R;

public class ContentFragment extends Fragment {
	ImageAdapter mAdapter = null;
	Gallery mGallery;
   @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState) {  
	   View view = inflater.inflate(R.layout.content_layout, container,false);
	   return view;
   }

	public void onUpdateContent(int position) {
		mGallery.setSelection(position);
		Log.i("#########@@@@@@",""+position);
//		mAdapter.setImages(imageNames);
//		mAdapter.notifyDataSetChanged();
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
        mAdapter = new ImageAdapter(this.getActivity());
		File imagesDir = Environment.getExternalStorageDirectory();
		if(imagesDir != null)
			mAdapter.setStoreDir(imagesDir);
        mGallery.setAdapter(mAdapter);
	}
}

class ImageAdapter extends BaseAdapter {
	private final String NULL = "sign_null";
	private File storeDir = null;
	private Context mContext;
	// 图片的资源ID
	private ArrayList<String> imgPaths = new ArrayList<String>();

	// 构造函数
	public ImageAdapter(Context context) {
		this.mContext = context;
	}

	// 返回所有图片的个数
	@Override
	public int getCount() {
		return imgPaths.size();
	}

	// 返回图片路径
	@Override
	public Object getItem(int position) {
		return imgPaths.get(position);
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
		
		String filePath = imgPaths.get(position);
		if(filePath != NULL)
		{
			Bitmap bm = BitmapFactory.decodeFile(filePath);
			// 通过索引获得图片并设置给ImageView
			imageView.setImageBitmap(bm);
		}
		
		return imageView;
	}
	
	public void setStoreDir(File dir)
	{
		storeDir = dir;
		String storeDirString = storeDir.toString();
		if(storeDirString.endsWith("/"))
			storeDir = new File(storeDirString+Params.IMG_STORE_STRING);
		else storeDir = new File(storeDirString+"/"+Params.IMG_STORE_STRING);
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
			if(!storeDir.exists())
				storeDir.mkdir();
			
			 imgPaths.clear();
			 HashSet<String> existImgs = new HashSet<String>();
	
			 if(storeDir != null)
			 {
				 File[] allFiles = storeDir.listFiles();
				 for(File f:allFiles)
				 {
					 existImgs.add(f.getAbsolutePath());
//					 Log.i("abs ",f.getAbsolutePath());
				 }
			 }
			 
			 String storePath = storeDir.getAbsolutePath();
			 for(String s:imageNames)
			 {
//				 Log.i(s,"*********@");
				 String imgAbsPath = storePath+"/"+s;
//				 Log.i("ffffffffffffff",imgAbsPath);
				 if(existImgs.contains(imgAbsPath)){
					 imgPaths.add(imgAbsPath);
//					 Log.i("rrrrrrrrrrrr",imgAbsPath);
				 }
				 else {
//					 Log.i("ddddddddddd","9");
					 imgPaths.add(NULL);
				 }
			 }
//			 Log.i(NULL,""+imgPaths.size());
		}

	}
}
