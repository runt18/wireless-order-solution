package com.wireless.ui;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class TabhostActivity extends TabActivity {
	private TabHost mTabHost;
	private AppContext appcontext;
	/**
	 * �׼���
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
		setContentView(R.layout.table);
		appcontext = (AppContext) getApplication();
		appcontext.activityList.add(TabhostActivity.this);
		setupTabHost();
		// mTabHost.getTabWidget().setDividerDrawable(R.drawable.minitab_default);
		/**
		 * ���tab��Activity
		 */
		Intent intent = new Intent().setClass(this, NumberActivity.class);
		setupTab(new TextView(this), "���", R.drawable.ic_tab_albums, intent);
		intent = new Intent().setClass(this, KitchenActivity.class);
		setupTab(new TextView(this), "�ֳ�", R.drawable.ic_tab_artists, intent);
		intent = new Intent().setClass(this, PingyinActivity.class);
		setupTab(new TextView(this), "ƴ��", R.drawable.ic_tab_songs, intent);

	}

	/**
	 * �����ʼ���ؼ�tab
	 * 
	 * @param view
	 *            ��Ҫ������view
	 * @param tag
	 *            ��ʾ������
	 * @param drawable
	 *            ͼƬ��Դ�ļ�
	 * @param intent
	 *            ��Ҫ��ӵ���ͼ��Activity��
	 */
	private void setupTab(final View view, final String tag, int drawable,
			Intent intent) {
		View tabview = createTabView(mTabHost.getContext(), tag, drawable);
		TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tabview)
				.setContent(intent);
		mTabHost.addTab(setContent);

	}

	/**
	 * ���������ļ���������Ҫ��view
	 * 
	 * @param context
	 *            �����Ķ���
	 * @param text
	 *            ��ʾ������
	 * @param drawable
	 *            ͼƬ��Դ�ļ�
	 * @return ��Ҫ������view����
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
