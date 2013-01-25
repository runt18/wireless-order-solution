package com.wireless.panorama.util;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PanoramaViewPager extends ViewPager {

	public PanoramaViewPager(Context context) {
		super(context);
	}

	public PanoramaViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptHoverEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		super.onInterceptHoverEvent(event);
		return false;
	}

}
