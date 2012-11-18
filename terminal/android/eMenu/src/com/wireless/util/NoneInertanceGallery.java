package com.wireless.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

public class NoneInertanceGallery extends Gallery{

	public NoneInertanceGallery(Context context,
			AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public boolean onFling(MotionEvent e1,MotionEvent e2,float velocityX, float velocityY)
	{
		super.onFling(e1, e2, 0, velocityY);
		return false;
	}
}