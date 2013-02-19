package com.wireless.util;


/**
 * 更新搜索框列表的runnable
 * @author ggdsn1
 * @see SearchFoodHandler
 */
public class SearchRunnable implements Runnable{
	private SearchFoodHandler mSearchHandler;
	private String mFilterCond;

	public SearchRunnable(SearchFoodHandler mSearchHandler) {
		super();
		this.mSearchHandler = mSearchHandler;
	}

	
	public void setmFilterCond(String mFilterCond) {
		this.mFilterCond = mFilterCond;
	}


	@Override
	public void run() {
		mSearchHandler.setmFilterCond(mFilterCond);
		//仅刷新
		mSearchHandler.sendEmptyMessage(0);
	}
}