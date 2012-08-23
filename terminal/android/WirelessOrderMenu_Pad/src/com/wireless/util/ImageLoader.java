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
		new LruCache<String, SoftReference<Bitmap>>(5){
		
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
			mImageCaches.put(imgName, new SoftReference<Bitmap>(image));
			return image;
		}else{
			Log.d("ImageLoader", imgName + " failed to create ");
			return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.null_pic);
		}

	}
	
}
