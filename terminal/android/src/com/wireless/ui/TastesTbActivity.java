package com.wireless.ui;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class TastesTbActivity extends TabActivity {
	private TabHost mTabHost;

	/**
	 * 首加载
	 */
	private void setupTabHost() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);

		mTabHost.setup();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// construct the tabhost
		setContentView(R.layout.tastetable);

		setupTabHost();
		// mTabHost.getTabWidget().setDividerDrawable(R.drawable.minitab_default);
		/**
		 * 添加tab项Activity
		 */
		Intent intent = new Intent().setClass(this, TasteActivity.class);
		setupTab(new TextView(this), "口味", R.drawable.ic_tab_albums, intent);
		intent = new Intent().setClass(this, StylesActivity.class);
		setupTab(new TextView(this), "做法", R.drawable.ic_tab_artists, intent);
		intent = new Intent().setClass(this,  SpecsActivity.class);
		setupTab(new TextView(this), "规格", R.drawable.ic_tab_songs, intent);

	}

	/**
	 * 逐个初始化控件tab
	 * 
	 * @param view
	 *            将要创建的view
	 * @param tag
	 *            显示的文字
	 * @param drawable
	 *            图片资源文件
	 * @param intent
	 *            将要添加的意图（Activity）
	 */
	private void setupTab(final View view, final String tag, int drawable,
			Intent intent) {
		View tabview = createTabView(mTabHost.getContext(), tag, drawable);
		TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tabview)
				.setContent(intent);
		mTabHost.addTab(setContent);

	}

	/**
	 * 关联布局文件，生成想要的view
	 * 
	 * @param context
	 *            上下文对象
	 * @param text
	 *            显示的文字
	 * @param drawable
	 *            图片资源文件
	 * @return 想要创建的view对象
	 */
	private static View createTabView(final Context context, final String text,
			int drawable) {
		View view = LayoutInflater.from(context).inflate(R.layout.tb_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		ImageView iv = (ImageView) view.findViewById(R.id.icon);
		iv.setImageResource(drawable);
		return view;
	}
}
