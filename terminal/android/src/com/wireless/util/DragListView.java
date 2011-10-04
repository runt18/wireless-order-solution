package com.wireless.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.ListView;

public class DragListView extends ListView implements ListView.OnScrollListener {

	// »¬¶¯Í£¶Ù×´Ì¬
	private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

	public DragListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnScrollListener(this);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		boolean result = super.onInterceptTouchEvent(event);

		if (mScrollState == OnScrollListener.SCROLL_STATE_FLING) {
			return true;
		}

		return result;
	}

	private OnNeedMoreListener mOnNeedMoreListener;

	public static interface OnNeedMoreListener {
		public void needMore();
	}

	public void setOnNeedMoreListener(OnNeedMoreListener onNeedMoreListener) {
		mOnNeedMoreListener = onNeedMoreListener;
	}

	private int mFirstVisibleItem;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (mOnNeedMoreListener == null) {
			return;
		}

		if (firstVisibleItem != mFirstVisibleItem) {
			if (firstVisibleItem + visibleItemCount >= totalItemCount) {
				mOnNeedMoreListener.needMore();
			}
		} else {
			mFirstVisibleItem = firstVisibleItem;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mScrollState = scrollState;
	}

}
