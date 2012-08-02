package com.wireless.ui;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.Region;
import com.wireless.protocol.ReqQueryRegion;
import com.wireless.protocol.ReqQueryTable;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Table;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;
import com.wireless.ui.view.PullListView;
import com.wireless.ui.view.PullListView.OnRefreshListener;

public class TableActivity extends Activity {
	private PullListView mListView;
	private View popupView;
	private ImageButton regionAllBtn ;
	private ImageButton idleBtn;
	private ImageButton busyBtn;
	
	private static final String ITEM_TAG_ID = "ID";
	private static final String ITEM_TAG_CUSTOM = "CUSTOM_NUM";
	private static final String ITEM_TAG_STATE = "STATE";
	private static final String ITEM_TAG_TBL_NAME = "TABLE_NAME";
	
	private static final String[] ITEM_TAGS = {
			ITEM_TAG_ID, 
			ITEM_TAG_CUSTOM, 
			ITEM_TAG_STATE,
			ITEM_TAG_TBL_NAME 
	};
	
	private static final int[] ITEM_ID = {
			R.id.text1_table,
			R.id.text2_table,
			R.id.text3_table,
			R.id.text4_table
	};
	
	private static Handler mDataHandler;
	private static Handler mRegionHandler;
	
	private int mTableCond = FILTER_TABLE_ALL;			//the current table filter condition
	
	private final static int FILTER_TABLE_ALL = 0;		//table filter condition to all
	private final static int FILTER_TABLE_IDLE = 1;		//table filter condition to idle
	private final static int FILTER_TABLE_BUSY = 2;		//table filter condition to busy
	
	private short mRegionCond = FILTER_REGION_ALL;		//the current region filter condition
	private final static short FILTER_REGION_ALL = Short.MIN_VALUE;		//region filter condition to all
	
	private String mFilterCond = "";					//the current filter string

	
	private static class RegionRefreshHandler extends Handler{
		
		private WeakReference<TableActivity> mActivity;
		
		RegionRefreshHandler(TableActivity activity){
			mActivity = new WeakReference<TableActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg){
			
			final TableActivity theActivity = mActivity.get();
	
			/**
			 * Filter the region containing data.
			 */
			HashSet<Short> validRegionID = new HashSet<Short>();
			for(Table tbl : WirelessOrder.tables){
				validRegionID.add(tbl.regionID);
			}
				
			final List<Region> validRegions = new ArrayList<Region>();
			validRegions.add(new Region(FILTER_REGION_ALL, "全部区域"));
			for(Region region : WirelessOrder.regions){
				if(validRegionID.contains(region.regionID)){
					validRegions.add(region);
				}
			}
			
			ListView popListView = (ListView)theActivity.popupView.findViewById(R.id.popWndList);

			popListView.setAdapter(new BaseAdapter() {
				
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					TextView view;
					Region region = validRegions.get(position);
					if(convertView == null){
						view =(TextView) LayoutInflater.from(theActivity.getApplicationContext()).inflate(R.layout.pop_wnd_item, null);
					}else{
						view = (TextView)convertView;
					}
					
					view.setText(region.name);
					view.setTag(region);
					
					return view;
				}
				
				@Override
				public long getItemId(int position) {
					return position;
				}
				
				@Override
				public Object getItem(int position) {
					return validRegions.get(position);
				}
				
				@Override
				public int getCount() {
					return validRegions.size();
				}
			});
			
		}
		
	}
	private static class RefreshHandler extends Handler{
		
		private List<Table> mFilterTable = new ArrayList<Table>();
		
		private WeakReference<TableActivity> mActivity;
				
		RefreshHandler(TableActivity activity){
			mActivity = new WeakReference<TableActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg){
			
			final TableActivity theActivity = mActivity.get();
			
			mFilterTable.clear();
			mFilterTable.addAll(Arrays.asList(WirelessOrder.tables));
			
			Iterator<Table> iter = mFilterTable.iterator();
			
			/**
			 * Calculate the idle and busy amount of tables
			 */
			int idleCnt = 0 ,busyCnt = 0,allCnt = 0;

			/**
			 * Filter the table source according to status & region condition
			 */
			while(iter.hasNext()){
				Table t = iter.next();
				
				if(theActivity.mRegionCond == FILTER_REGION_ALL){
					if(t.status == Table.TABLE_BUSY){
						busyCnt++;
					}else if(t.status == Table.TABLE_IDLE){
						idleCnt++;
					}
					allCnt++;
					
				}else if(theActivity.mRegionCond == t.regionID){
					if(t.status == Table.TABLE_BUSY){
						busyCnt++;
					}else if(t.status == Table.TABLE_IDLE){
						idleCnt++;
					}
					allCnt++;
				}				

				
				if(theActivity.mTableCond == FILTER_TABLE_IDLE && t.status != Table.TABLE_IDLE){
					iter.remove();
					
				}else if(theActivity.mTableCond == FILTER_TABLE_BUSY && t.status != Table.TABLE_BUSY){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.REGION_1 && t.regionID != Region.REGION_1){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.REGION_2 && t.regionID != Region.REGION_2){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.REGION_3 && t.regionID != Region.REGION_3){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.REGION_4 && t.regionID != Region.REGION_4){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.REGION_5 && t.regionID != Region.REGION_5){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.REGION_6 && t.regionID != Region.REGION_6){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.REGION_7 && t.regionID != Region.REGION_7){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.REGION_8 && t.regionID != Region.REGION_8){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.REGION_9 && t.regionID != Region.REGION_9){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.REGION_10 && t.regionID != Region.REGION_10){
					iter.remove();
					
				}else if(theActivity.mFilterCond.length() != 0){
					if(!(t.name.contains(theActivity.mFilterCond) || Integer.toString(t.aliasID).startsWith(theActivity.mFilterCond))){
						iter.remove();
					}
				}
			}
		
				
			List<Map<String, ?>> contents = new ArrayList<Map<String, ?>>();
			for(Table tbl : mFilterTable){
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put(ITEM_TAG_ID, tbl.aliasID + (tbl.name.length() == 0 ? "" : "(" + tbl.name + ")"));
				map.put(ITEM_TAG_CUSTOM, "人数: " + tbl.custom_num);
				map.put(ITEM_TAG_TBL_NAME, tbl.name);
				map.put(ITEM_TAG_STATE, "状态： " + (tbl.status == Table.TABLE_IDLE ? "空闲" : "就餐"));
				contents.add(map);
			}
			
			/*
			 * set the counts
			 */
			TextView allCountTextView = (TextView)theActivity.findViewById(R.id.left_txt_bottom);
			allCountTextView.setText(Integer.toString(allCnt));
			allCountTextView.setVisibility(View.VISIBLE);
			TextView idleCountTxtView = (TextView)theActivity.findViewById(R.id.middle_txt_bottom);
			idleCountTxtView.setText(Integer.toString(idleCnt));
			idleCountTxtView.setVisibility(View.VISIBLE);
			
			TextView busyCountTxtView = (TextView)theActivity.findViewById(R.id.right_txt_bottom);
			busyCountTxtView.setText(Integer.toString(busyCnt));
			busyCountTxtView.setVisibility(View.VISIBLE);
			
			/*
			 * set the hint if content is empty 
			 */
			TextView hintText = (TextView)theActivity.findViewById(R.id.hint_text_table);

			if(contents.isEmpty()){
				hintText.setText("没有匹配的项");
				hintText.setVisibility(View.VISIBLE);
			
			}else{
				hintText.setVisibility(View.INVISIBLE);
			}

			theActivity.mListView.setAdapter(new SimpleAdapter(theActivity.getApplicationContext(), 
					 						 				   contents,
					 						 				   R.layout.the_table, 
					 						 				   TableActivity.ITEM_TAGS,
					 						 				   TableActivity.ITEM_ID){
				@Override
				public View getView(int position, View convertView, ViewGroup parent){
					View view = super.getView(position, convertView, parent);
					((ImageButton)view.findViewById(R.id.add_table)).setOnClickListener(new OnClickListener(){			
						@Override
						public void onClick(View v) {
							//TODO jump to order activity
						}
					});
					return view;
				}
			});
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table);
		
		regionAllBtn = (ImageButton)findViewById(R.id.left_btn_bottom);
		idleBtn = (ImageButton)findViewById(R.id.middle_btn_bottom);
		busyBtn = (ImageButton)findViewById(R.id.right_btn_bottom);
		mDataHandler = new RefreshHandler(this);
		mRegionHandler = new RegionRefreshHandler(this);
		prepareUI();		
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		new QueryRegionTask().execute();

		/**
		 * title
		 */
		
		TextView titleTextView = (TextView) findViewById(R.id.toptitle);
		titleTextView.setVisibility(View.VISIBLE);
		titleTextView.setBackgroundResource(R.drawable.title_selector);
		titleTextView.setText("全部区域");
		
		/*
		 * the listview's item listener 
		 */
		mListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO jump to table detail activity
				System.out.println("position: "+position);
			}
		});
	}

	/*
	 * set the button's image
	 */
	private void buttonUp(){
		regionAllBtn.setImageResource(R.drawable.all);
		idleBtn.setImageResource(R.drawable.free);
		busyBtn.setImageResource(R.drawable.eating);
	}
	/**
	 * 初始化UI
	 */
	private void prepareUI() {
		/**
		 * "返回"Button
		 */
		TextView leftTxtView = (TextView) findViewById(R.id.textView_left);
		leftTxtView.setText("返回");
		leftTxtView.setVisibility(View.VISIBLE);

		ImageButton backBtn = (ImageButton) findViewById(R.id.btn_left);
		backBtn.setVisibility(View.VISIBLE);
		backBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		/**
		 * 搜索框
		 */
		final AutoCompleteTextView txtView = (AutoCompleteTextView)findViewById(R.id.search_view_table);
		txtView.addTextChangedListener(new TextWatcher(){
			@Override 
			public void afterTextChanged(Editable s) {}
			
			@Override
			public void beforeTextChanged(CharSequence s,int start, int count, int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mFilterCond = s.length() == 0 ? "" : s.toString().trim();
				mDataHandler.sendEmptyMessage(0);
			}
		});
		
		/**
		 * 刷新 button
		 */
		ImageButton refreshBtn = (ImageButton) findViewById(R.id.btn2_right);
		refreshBtn.setVisibility(View.VISIBLE);
		refreshBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new QueryRegionTask().execute();
				txtView.setText("");
			}
		});
		
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
																		 RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		lp.setMargins(0, 0, 64, 0);
		lp.addRule(RelativeLayout.VISIBLE);

		/**
		 * PullListView
		 */
		mListView = (PullListView) findViewById(R.id.listView_table);
		mListView.setOnRefreshListener(new OnRefreshListener(){
			@Override
			public void onRefresh() {
				new QueryRegionTask().execute();
			}
		});
		/**
		 * “全部”按钮
		 */
		ImageButton allBtn = (ImageButton)findViewById(R.id.btn_right);
		allBtn.setImageResource(R.drawable.home_selector);
		allBtn.setLayoutParams(lp);
		allBtn.setVisibility(View.VISIBLE);
		allBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				((AutoCompleteTextView)findViewById(R.id.search_view_table)).setText("");
				mTableCond = FILTER_TABLE_ALL;
				mRegionCond = FILTER_REGION_ALL;
				mDataHandler.sendEmptyMessage(0);
				((TextView)findViewById(R.id.toptitle)).setText("全部区域");
				buttonUp();
				regionAllBtn.setImageResource(R.drawable.alldown);
			}
		});
		
		
		/**
		 * “空闲”按钮
		 */
		idleBtn = (ImageButton)findViewById(R.id.middle_btn_bottom);
		idleBtn.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				((AutoCompleteTextView)findViewById(R.id.search_view_table)).setText("");
				mTableCond = FILTER_TABLE_IDLE;
				mDataHandler.sendEmptyMessage(0);
				buttonUp();
				idleBtn.setImageResource(R.drawable.freedown);
			}
			
		});

		/**
		 * “就餐”按钮
		 */
		busyBtn = (ImageButton)findViewById(R.id.right_btn_bottom);
		busyBtn.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				((AutoCompleteTextView)findViewById(R.id.search_view_table)).setText("");
				mTableCond = FILTER_TABLE_BUSY;
				mDataHandler.sendEmptyMessage(0);
				buttonUp();
				busyBtn.setImageResource(R.drawable.eatingdown);
			}
		});
		
		/**
		 * “清空”按钮
		 */
		ImageButton deleteBtn = (ImageButton)findViewById(R.id.deleteBtn_table);
		deleteBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				txtView.setText("");
			}
		});

		/**
		 * region all button
		 */
		regionAllBtn = (ImageButton)findViewById(R.id.left_btn_bottom);
		regionAllBtn.setImageResource(R.drawable.alldown);
		regionAllBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((AutoCompleteTextView)findViewById(R.id.search_view_table)).setText("");
				mTableCond = FILTER_TABLE_ALL;
				mDataHandler.sendEmptyMessage(0);
				buttonUp();
				regionAllBtn.setImageResource(R.drawable.alldown);
			}
		});
		
		// 创建点击餐台状态后弹出区域的View
		popupView = getLayoutInflater().inflate(R.layout.main_pop_window, null);
//		System.out.println(popupView);

		// 创建与这个View关联的pop-up window
		final PopupWindow popWnd = new PopupWindow(
				popupView,
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, true);
		popWnd.setOutsideTouchable(true);
		popWnd.setBackgroundDrawable(new BitmapDrawable());
		popWnd.update();
		
		ListView popListView = (ListView)popupView.findViewById(R.id.popWndList);
//		System.out.println(popListView);
		popListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Region region = (Region)view.getTag();
				mRegionCond = region.regionID;
				((TextView)findViewById(R.id.toptitle)).setText(region.name);
				mDataHandler.sendEmptyMessage(0);
				popWnd.dismiss();
			}
			
		});
		
		ImageButton titleBtn = (ImageButton)findViewById(R.id.title_btn_top);
		titleBtn.setVisibility(View.VISIBLE);
		titleBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (popWnd.isShowing()) {
					popWnd.dismiss();
				} else {
					popWnd.showAsDropDown(v);

				}
			}
		});
		
	}

	private class QueryRegionTask extends AsyncTask<Void, Void, String>{
		
		private ProgressDialog mProgDialog;
		
		/**
		 * 在执行请求区域信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			mProgDialog = ProgressDialog.show(TableActivity.this, "", "正在更新区域信息...请稍后", true);
		}
		
		/**
		 * 在新的线程中执行请求区域信息的操作
		 */
		@Override
		protected String doInBackground(Void... arg0) {
		
			String errMsg = null;
			try{
				WirelessOrder.regions = null;
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryRegion());
				if(resp.header.type == Type.ACK){
					WirelessOrder.regions = RespParser.parseQueryRegion(resp);
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			
			return errMsg;
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则执行请求餐台的操作。
		 */
		@Override
		protected void onPostExecute(String errMsg){
			
			mProgDialog.dismiss();
			
			/**
			 * Prompt user message if any error occurred.
			 */		
			if(errMsg != null){
				mListView.onRefreshComplete();
				Toast.makeText(getApplicationContext(), "刷新区域数据失败,请检查网络", Toast.LENGTH_SHORT).show();
				
			}else{				
				new QueryTableTask().execute();
			}
		}
	};
	/**
	 * 请求餐台信息
	 */
	private class QueryTableTask extends AsyncTask<Void, Void, String> {
		
		private ProgressDialog mProgDialog;
		
		/**
		 * 在执行请求餐台信息前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			mProgDialog = ProgressDialog.show(TableActivity.this, "", "正在更新餐台信息...请稍后", true);
		}

		/**
		 * 在新的线程中执行请求餐台信息的操作
		 */
		@Override
		protected String doInBackground(Void... arg0) {

			String errMsg = null;
			try {
				WirelessOrder.tables = null;
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryTable());
				if (resp.header.type == Type.ACK) {
					WirelessOrder.tables = RespParser.parseQueryTable(resp);
				}
			} catch (IOException e) {
				errMsg = e.getMessage();
			}

			return errMsg;
		}

		/**
		 * 根据返回的error message判断，如果发错异常则提示用户， 如果成功，则执行请求餐厅的操作。
		 */
		@Override
		protected void onPostExecute(String errMsg) {
			
			mProgDialog.dismiss();
			
			/**
			 * Prompt user message if any error occurred.
			 */
			TextView tv = (TextView)findViewById(R.id.hint_text_table);

			if (errMsg != null) {
				mListView.onRefreshComplete();
				Toast.makeText(getApplicationContext(), "刷新餐台数据失败,请检查网络", Toast.LENGTH_SHORT).show();
				mListView.setVisibility(View.GONE);
				tv.setText("请重新刷新数据");
				tv.setVisibility(View.VISIBLE);

			} else {
				mRegionHandler.sendEmptyMessage(0);
				mDataHandler.sendEmptyMessage(0);
				((AutoCompleteTextView)findViewById(R.id.search_view_table)).setText("");
				mListView.onRefreshComplete();
				Toast.makeText(getApplicationContext(), "餐台信息刷新成功",	Toast.LENGTH_SHORT).show();
			} 
		}
	}

}

