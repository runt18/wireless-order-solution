package com.wireless.util;

import java.lang.ref.SoftReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;

import com.wireless.common.Params;
import com.wireless.ordermenu.R;


public class ImageLoader {
	
	private Context mContext;
	
	private static LruCache<String, SoftReference<Bitmap>> mImageCaches = 
		new LruCache<String, SoftReference<Bitmap>>(6){
		
		@Override
		protected void entryRemoved(boolean evicted, String key, SoftReference<Bitmap> oldValue, SoftReference<Bitmap> newValue){
			Bitmap image = oldValue.get();
			if(image != null){
				image.recycle();
				Log.d("ImageLoader", key + " succeed to recycle ");
			}
		}		

	};
	
	
	public ImageLoader(Context context){
		mContext = context;
		//mImageWoker.start();
	}
	
	public Bitmap loadImage(String imgName){
		Log.d("ImageLoader", imgName + " wants to create ");
		SoftReference<Bitmap> imgSoftRef = mImageCaches.get(imgName);
		if(imgSoftRef != null){
			Bitmap image = imgSoftRef.get();
			if(image != null){
				Log.d("ImageLoader", imgName + " is hit in caches ");
				return image;				
			}else{
				return createBitmap(imgName);				
			}
			
		}else{
			return createBitmap(imgName);
		}

	}
	
	private Bitmap createBitmap(String imgName){
		Bitmap image = BitmapFactory.decodeFile(android.os.Environment.getExternalStorageDirectory().getPath() + Params.IMG_STORE_PATH + imgName);
		if(image != null){
			Log.d("ImageLoader", imgName + " succeed to create ");
			synchronized (mImageCaches) {
				mImageCaches.put(imgName, new SoftReference<Bitmap>(image));				
			}
			return image;
		}else{
			Log.d("ImageLoader", imgName + " failed to create ");
			return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.null_pic);
		}
	}
	
//	private static Bitmap createBitmap(ImageView imgView, String imgName){
//		Bitmap image = BitmapFactory.decodeFile(android.os.Environment.getExternalStorageDirectory().getPath() + Params.IMG_STORE_PATH + imgName);
//		if(image != null){
//			Log.d("ImageLoader", imgName + " succeed to create ");
//			synchronized (mImageCaches) {
//				mImageCaches.put(imgName, new SoftReference<Bitmap>(image));
//			}
//			return image;
//		}else{
//			Log.d("ImageLoader", imgName + " failed to create ");
//			return BitmapFactory.decodeResource(imgView.getResources(), R.drawable.null_pic);
//		}
//	}
	
//	public void loadImageEx(ImageView imgView, String imgName){
//		Log.d("ImageLoader", imgName + " wants to create ");
//		SoftReference<Bitmap> imgSoftRef;
//		synchronized (mImageCaches) {
//			imgSoftRef = mImageCaches.get(imgName);
//		}
//		if(imgSoftRef != null){
//			Bitmap image = imgSoftRef.get();
//			if(image != null){
//				Log.d("ImageLoader", imgName + " is hit in caches ");
//				imgView.setImageBitmap(image);
//			}else{
//				imgMap.put(imgView, imgName);
//				synchronized (imgMap) {
//					imgMap.notifyAll();					
//				}
//			}
//			
//		}else{
//			imgMap.put(imgView, imgName);
//			synchronized (imgMap) {
//				imgMap.notifyAll();					
//			}
//		}
//	}
//	
//	private static ConcurrentHashMap<ImageView, String> imgMap = new ConcurrentHashMap<ImageView, String>();
//	
//	private static Thread mImageWoker = new Thread(){
//		
//		boolean isRunning = false;
//		
//		@Override
//		public void start(){
//			if(!isRunning){
//				super.start();
//				isRunning = true;
//			}
//		}
//		
//		@Override
//		public void run(){
//			while(true){
//				while(imgMap.size() == 0){
//					synchronized (imgMap) {
//						try{ imgMap.wait(); }
//						catch(InterruptedException e){}
//					}
//				}
//				
//				Iterator<Map.Entry<ImageView, String>> iter = imgMap.entrySet().iterator();
//				while(iter.hasNext()){
//					Map.Entry<ImageView, String> entry = iter.next();
//					final ImageView imgView = entry.getKey();
//					final Bitmap image = createBitmap(imgView, entry.getValue());
//					imgView.post(new Runnable(){
//						@Override
//						public void run(){
//							imgView.setImageBitmap(image);
//						}
//					});
//					iter.remove();
//				}
//				
//			}
//		}
//	};
	
}
