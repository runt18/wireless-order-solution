package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.protocol.Region;
import com.wireless.protocol.Table;

public class ViewPagerTablePanelFragment extends Fragment {

	private DataRefreshHandler mTableRefreshHandler;
	private RegionRefreshHandler mRegionRefreshHandler;
    private ViewPager mPager;
	private TablePagerAdapter mAdapter;

	private int mTableCond = FILTER_TABLE_ALL;			//the current table filter condition
	private String mFilterCond = "";					//the current filter string

	private final static int FILTER_TABLE_ALL = 0;		//table filter condition to all
	private final static int FILTER_TABLE_IDLE = 1;		//table filter condition to idle
	private final static int FILTER_TABLE_BUSY = 2;		//table filter condition to busy
	
	private short mRegionCond = FILTER_REGION_ALL;		//the current region filter condition
	private final static short FILTER_REGION_ALL = Short.MIN_VALUE;		//region filter condition to all
	private static final String REGION_ALL_STR = "全部区域";
	/*
	 * 餐台显示的handler
	 * 根据区域显示不同数量的餐台
	 */
	private static class DataRefreshHandler extends Handler{
		private List<Table> mFilterTable = new ArrayList<Table>();
		private WeakReference<ViewPagerTablePanelFragment> mFragment;
		
		DataRefreshHandler(ViewPagerTablePanelFragment viewPagerTablePanelFragment)
		{
			mFragment = new WeakReference<ViewPagerTablePanelFragment>(viewPagerTablePanelFragment);
		}
		
		@Override
		public void handleMessage(Message msg)
		{
			ViewPagerTablePanelFragment fragment = mFragment.get();
			mFilterTable.clear();
			mFilterTable.addAll(Arrays.asList(WirelessOrder.tables));
			Iterator<Table> iter = mFilterTable.iterator();

			/**
			 * Filter the table source according to status & region condition
			 */
			while(iter.hasNext()){
				Table t = iter.next();

				if(fragment.mTableCond == FILTER_TABLE_IDLE && t.status != Table.TABLE_IDLE){
					iter.remove();
					
				}else if(fragment.mTableCond == FILTER_TABLE_BUSY && t.status != Table.TABLE_BUSY){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_1 && t.regionID != Region.REGION_1){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_2 && t.regionID != Region.REGION_2){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_3 && t.regionID != Region.REGION_3){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_4 && t.regionID != Region.REGION_4){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_5 && t.regionID != Region.REGION_5){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_6 && t.regionID != Region.REGION_6){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_7 && t.regionID != Region.REGION_7){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_8 && t.regionID != Region.REGION_8){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_9 && t.regionID != Region.REGION_9){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_10 && t.regionID != Region.REGION_10){
					iter.remove();
					
				}else if(fragment.mFilterCond.length() != 0){
					if(!(t.name.contains(fragment.mFilterCond) || Integer.toString(t.aliasID).startsWith(fragment.mFilterCond))){
						iter.remove();
					}
				}
			}
//			fragment.mTables = mFilterTable;
			fragment.mAdapter = fragment.new TablePagerAdapter(fragment.getFragmentManager(), mFilterTable);
			fragment.mPager.setAdapter(fragment.mAdapter);

		}
	}
	
	/*
	 * 区域选择的handler
	 * 根据选择的区域显示不同的餐台
	 */
	private static class RegionRefreshHandler extends Handler{
		
		private WeakReference<ViewPagerTablePanelFragment> mFragment;
		
		RegionRefreshHandler(ViewPagerTablePanelFragment viewPagerTablePanelFragment)
		{
			mFragment = new WeakReference<ViewPagerTablePanelFragment>(viewPagerTablePanelFragment);
		}

		@Override
		public void handleMessage(Message msg){
			final ViewPagerTablePanelFragment fragment = mFragment.get();
			
			/**
			 * Filter the region containing data.
			 */
			HashSet<Short> validRegionID = new HashSet<Short>();
			for(Table tbl : WirelessOrder.tables){
				validRegionID.add(tbl.regionID);
			}
			
			/*
			 * 根据条件筛选出所有要显示的区域
			 */
			final List<Region> validRegions = new ArrayList<Region>();
			validRegions.add(new Region(FILTER_REGION_ALL, REGION_ALL_STR));
			for(Region region : WirelessOrder.regions){
				if(validRegionID.contains(region.regionID)){
					validRegions.add(region);
				}
			}
			
			LinearLayout hScrollViewLinearLayout = (LinearLayout) fragment.getView().findViewById(R.id.hScrollView_linearLayout);
			hScrollViewLinearLayout.removeAllViews();
			for(Region r : validRegions)
			{
				RelativeLayout view = (RelativeLayout) LayoutInflater.from(fragment.getActivity()).inflate(R.layout.region_item, null);
				((TextView)view.findViewById(R.id.textView_region)).setText(r.name);
				view.setTag(r);
				
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						Region region = (Region) v.getTag();
						fragment.mRegionCond = region.regionID;
						fragment.mTableRefreshHandler.sendEmptyMessage(0);
					}
				});
				
				hScrollViewLinearLayout.addView(view);
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mTableRefreshHandler = new DataRefreshHandler(this);
		mRegionRefreshHandler = new RegionRefreshHandler(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.dialog_tab3,container,false);
		

        mPager = (ViewPager) view.findViewById(R.id.viewPager_dialogTab3);
        mPager.setOffscreenPageLimit(2);

		
		final AutoCompleteTextView tableNumEditText = (AutoCompleteTextView)view.findViewById(R.id.editText_table_num);
		
		tableNumEditText.addTextChangedListener(new TextWatcher(){
			@Override 
			public void afterTextChanged(Editable s) {}
			
			@Override
			public void beforeTextChanged(CharSequence s,int start, int count, int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mFilterCond = s.length() == 0 ? "" : s.toString().trim();
				mTableRefreshHandler.sendEmptyMessage(0);
			}
		});
		
		/*
		 * “清空”按钮
		 */
		((ImageButton) view.findViewById(R.id.deleteBtn_dialog_tab1)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				tableNumEditText.setText("");
			}
		});
		
		/*
		 * 人数增加的按钮
		 */
		final TextView cstmNumTextView = (TextView) view.findViewById(R.id.textView_customNum);
		((ImageButton) view.findViewById(R.id.imageButton_plus_tab1)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				int curNum = Integer.parseInt(cstmNumTextView.getText().toString());
				cstmNumTextView.setText("" + ++curNum);
			}
		});
		
		/*
		 * 人数减少的按钮
		 */
		((ImageButton) view.findViewById(R.id.imageButton_minus_tab1)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				int curNum = Integer.parseInt(cstmNumTextView.getText().toString());
				if(--curNum >= 1)
					cstmNumTextView.setText("" + curNum);
			}
		});
		
		/*
		 * 刷新餐台按钮
		 */
		((Button) view.findViewById(R.id.button_tab1_refresh)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				new QueryTableTask().execute();
			}
		});
		
		return view;
	}
	
	
	
	@Override
	public void onStart() {
		super.onStart();
		new QueryTableTask().execute();
	}



	/*
	 * 请求餐台信息
	 */
	private class QueryTableTask extends com.wireless.lib.task.QueryTableTask {
		
		private ProgressDialog mProgDialog;
		
		/*
		 * 在执行请求餐台信息前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			mProgDialog = ProgressDialog.show(ViewPagerTablePanelFragment.this.getActivity(), "", "正在更新餐台信息...请稍后", true);
		}


		/*
		 * 根据返回的error message判断，如果发错异常则提示用户， 如果成功，则执行请求餐厅的操作。
		 */
		@Override
		protected void onPostExecute(Table[] tables) {
			
			mProgDialog.dismiss();
			
			/**
			 * Prompt user message if any error occurred.
			 */
			if(mErrMsg != null) {
				Toast.makeText(ViewPagerTablePanelFragment.this.getActivity(), "刷新餐台数据失败,请检查网络", Toast.LENGTH_SHORT).show();

			} else {
				
				WirelessOrder.tables = tables;
				
				mRegionRefreshHandler.sendEmptyMessage(0);
				mTableRefreshHandler.sendEmptyMessage(0);
				((AutoCompleteTextView) getView().findViewById(R.id.editText_table_num)).setText("");
				Toast.makeText(ViewPagerTablePanelFragment.this.getActivity(), "餐台信息刷新成功",	Toast.LENGTH_SHORT).show();
			} 
		}
	}
	
	private class TablePagerAdapter extends FragmentStatePagerAdapter{

		private List<Table> mTables;

		public TablePagerAdapter(android.app.FragmentManager fragmentManager, List<Table> mFilterTable) {
			super(fragmentManager);
			
			mTables = mFilterTable;
		}

		@Override
		public Fragment getItem(int arg0) {
			return TableItemFragment.newInstance(mTables, ViewPagerTablePanelFragment.this.getId());
		}

		@Override
		public int getCount() {
			return mTables.size();
		}
		
	}
}
