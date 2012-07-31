package com.wireless.ui;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
	private SimpleAdapter mAdapter;
	PullListView mListView;
	TextView titleTextView;
	
	static final int ALL_BTN_CLICKED=21;
	static final int IDLE_BTN_CLICKED=22;
	static final int BUSY_BTN_CLICKED=23;
	
	
	static final int CHANGE_TO_ALL=24;
//	static final int CHANGE_TO_IDLE=25;
//	static final int CHANGE_TO_BUSY=26;
	
	static final int BACK_TO_ALL=27;
	
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
	
	//TableHandler mHandler;
	
	private static Handler _handler;
	
	private int mTableCond = FILTER_TABLE_ALL;			//the current table filter condition
	
	private final static int FILTER_TABLE_ALL = 0;		//table filter condition to all
	private final static int FILTER_TABLE_IDLE = 1;		//table filter condition to idle
	private final static int FILTER_TABLE_BUSY = 2;		//table filter condition to busy
	
	private int mRegionCond = FILTER_REGION_ALL;		//the current region filter condition
	
	private final static int FILTER_REGION_ALL = 0;		//region filter condition to all
	private final static int FILTER_REGION_1 = 1;		//region filter condition to 1st
	private final static int FILTER_REGION_2 = 2;		//region filter condition to 2nd
	private final static int FILTER_REGION_3 = 3;		//region filter condition to 3rd
	private final static int FILTER_REGION_4 = 4;		//region filter condition to 4th
	private final static int FILTER_REGION_5 = 5;		//region filter condition to 5th
	private final static int FILTER_REGION_6 = 6;		//region filter condition to 6th
	private final static int FILTER_REGION_7 = 7;		//region filter condition to 7th
	private final static int FILTER_REGION_8 = 8;		//region filter condition to 8th
	private final static int FILTER_REGION_9 = 9;		//region filter condition to 9th
	private final static int FILTER_REGION_10 = 10;		//region filter condition to 10th
	
	private String mFilterCond = "";					//the current filter string
	
	private static class RefreshHandler extends Handler{
		
		private List<Table> mFilterTable = new ArrayList<Table>();
		
		private WeakReference<TableActivity> mActivity;
				
		RefreshHandler(TableActivity activity){
			mActivity = new WeakReference<TableActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg){
			mFilterTable.clear();
			TableActivity theActivity = mActivity.get();
			
			mFilterTable.addAll(Arrays.asList(WirelessOrder.tables));
			
			Iterator<Table> iter = mFilterTable.iterator();
			while(iter.hasNext()){
				Table t = iter.next();
				
				/**
				 * Filter the table source according to status & region condition
				 */
				if(theActivity.mTableCond == FILTER_TABLE_IDLE && t.status != Table.TABLE_IDLE){
					iter.remove();
					
				}else if(theActivity.mTableCond == FILTER_TABLE_BUSY && t.status != Table.TABLE_BUSY){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_1 && t.regionID != Region.REGION_1){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_2 && t.regionID != Region.REGION_2){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_3 && t.regionID != Region.REGION_3){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_4 && t.regionID != Region.REGION_4){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_5 && t.regionID != Region.REGION_5){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_6 && t.regionID != Region.REGION_6){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_7 && t.regionID != Region.REGION_7){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_8 && t.regionID != Region.REGION_8){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_9 && t.regionID != Region.REGION_9){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_10 && t.regionID != Region.REGION_10){
					iter.remove();
					
				}else if(theActivity.mFilterCond.length() != 0){
					if(!(t.name.contains(theActivity.mFilterCond) || Integer.toString(t.aliasID).startsWith(theActivity.mFilterCond))){
						iter.remove();
					}
				}
				
				int idleCnt = 0;
				int busyCnt = 0;
				
				List<Map<String, ?>> contents = new ArrayList<Map<String, ?>>();
				for(Table tbl : mFilterTable){
					
					/**
					 * Calculate the idle and busy amount of tables
					 */
					if(t.status == Table.TABLE_BUSY){
						busyCnt++;
					}else if(t.status == Table.TABLE_IDLE){
						idleCnt++;
					}
					
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(ITEM_TAG_ID, tbl.aliasID + (tbl.name.length() == 0 ? "" : "(" + tbl.name + ")"));
					map.put(ITEM_TAG_CUSTOM, "人数: " + t.custom_num);
					map.put(ITEM_TAG_TBL_NAME, t.name);
					map.put(ITEM_TAG_STATE, "状态： " + (tbl.status == Table.TABLE_IDLE ? "空闲" : "就餐"));
					contents.add(map);
				}
				
				((TextView)theActivity.findViewById(R.id.left_txt_bottom)).setText(Integer.toString(mFilterTable.size()));
				((TextView)theActivity.findViewById(R.id.middle_txt_bottom)).setText(Integer.toString(idleCnt));
				((TextView)theActivity.findViewById(R.id.right_txt_bottom)).setText(Integer.toString(busyCnt));
				
				theActivity.mListView.setAdapter(new SimpleAdapter(theActivity.getApplicationContext(), 
						 						 				   contents,
						 						 				   R.layout.the_table, 
						 						 				   TableActivity.ITEM_TAGS,
						 						 				   TableActivity.ITEM_ID));
				
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table);
		_handler = new RefreshHandler(this);
		prepareUI();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		/**
		 * title
		 */
		titleTextView = (TextView) findViewById(R.id.toptitle);
		titleTextView.setVisibility(View.VISIBLE);
		titleTextView.setBackgroundResource(R.drawable.title_selector);
		titleTextView.setText("全部区域");
		
		//mHandler = new TableHandler();


		//mHandler.sendEmptyMessage(CHANGE_TO_ALL);

		new QueryRegionTask().execute();
	
		
		final PopupWindow popWnd;
		// 创建点击餐台状态后弹出区域的View
		final View popupView = getLayoutInflater()
				.inflate(R.layout.main_pop_window, null);
		// 创建与这个View关联的pop-up window
		popWnd = new PopupWindow(
				popupView,
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, true);
		popWnd.setOutsideTouchable(true);
		popWnd.setBackgroundDrawable(new BitmapDrawable());
		popWnd.update();	
		
		//FIXME
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.pop_wnd_item, new String[]{"区域1"});
		ListView popListView = (ListView)popupView.findViewById(R.id.popWndList);
		
		popListView.setAdapter(arrayAdapter);
		
		popListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(
					AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				//mHandler.sendEmptyMessage(arg2);
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
				_handler.sendEmptyMessage(0);
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
				_handler.sendEmptyMessage(0);
			}
		});
		
		
		/**
		 * “空闲”按钮
		 */
		ImageButton idleBtn = (ImageButton)findViewById(R.id.middle_btn_bottom);
		idleBtn.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				((AutoCompleteTextView)findViewById(R.id.search_view_table)).setText("");
				mTableCond = FILTER_TABLE_IDLE;
				_handler.sendEmptyMessage(0);
			}
			
		});

		/**
		 * “就餐”按钮
		 */
		ImageButton busyBtn = (ImageButton)findViewById(R.id.right_btn_bottom);
		busyBtn.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				((AutoCompleteTextView)findViewById(R.id.search_view_table)).setText("");
				mTableCond = FILTER_TABLE_BUSY;
				_handler.sendEmptyMessage(0);
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
		ImageButton regionAllBtn = (ImageButton)findViewById(R.id.left_btn_bottom);
		regionAllBtn.setImageResource(R.drawable.alldown);
		regionAllBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	
//	private class BottomOnClickListener implements OnClickListener{
//		static final int ALL = 0;
//		static final int IDLE = 1;
//		static final int BUSY =2;
//		private int which=0;
//		BottomOnClickListener(int which)
//		{
//			this.which=which;
//		}
//		@Override
//		public void onClick(View v) {
//			switch(which)
//			{
//			case 0:mHandler.sendEmptyMessage(ALL_BTN_CLICKED);break;
//			case 1:mHandler.sendEmptyMessage(IDLE_BTN_CLICKED);break;
//			case 2:mHandler.sendEmptyMessage(BUSY_BTN_CLICKED);break;
//			}
//		}
//	}

	class TableHandler extends Handler{
		private List<Map<String, ?>> allList,idleList, busyList;
		private ArrayList<Short> regions = new ArrayList<Short>();
		private Table[] tableSource;
		private ImageButton allBtn,idleBtn,busyBtn;
		TextView mAllTextView,mIdleTextView,mBusyTextView;
		private Region[] regionSource;
		private ArrayList<String> regionNames = new ArrayList<String>();
		TableHandler()
		{
			allList = new ArrayList<Map<String, ?>>();
			idleList = new ArrayList<Map<String, ?>>();
			busyList = new ArrayList<Map<String, ?>>();
			
			allBtn = (ImageButton)findViewById(R.id.left_btn_bottom);
			idleBtn = (ImageButton)findViewById(R.id.middle_btn_bottom);
			busyBtn = (ImageButton)findViewById(R.id.right_btn_bottom);
			mAllTextView = (TextView)findViewById(R.id.left_txt_bottom);
			mIdleTextView = (TextView)findViewById(R.id.middle_txt_bottom);
			mBusyTextView = (TextView)findViewById(R.id.right_txt_bottom);

			init();
		}
		
		private void init(){
			tableSource = WirelessOrder.tables == null ? new Table[0]: WirelessOrder.tables;
			regionSource = WirelessOrder.regions == null ? new Region[0] : WirelessOrder.regions;
			
			for (Table t : tableSource) {
				if(!regions.contains(t.regionID))
					regions.add(t.regionID);
				addData(t);
			}
			
			refreshRegion();
		}
		
		private void refreshRegion()
		{
			regionNames.add("全部区域");
			for(int i = 0; i < regionSource.length; i++){
				if(regions.contains(regionSource[i].regionID)){
					regionNames.add(regionSource[i].name);
				}
			}
//			
//			for(String region:regionNames)
//				Log.d("ddddddddd",region);
		}
		/**
		 * 
		 * @return 所有有餐台的区域名
		 */
		ArrayList<String> getRegions()
		{
			return regionNames;
		}
		/**
		 * 刷新handler里面的数据
		 */
		void refreshData()
		{
			clearData();
			regions.clear();
			regionNames.clear();
			init();
		}

		private void chooseRegion(int regionID)
		{
			clearData();
			for(Table t:tableSource)
			{
				if(t.regionID==regionID)
				{
					addData(t);
				}
			}
		}
		
		private void clearData()
		{
			allList.clear();
			idleList.clear();
			busyList.clear();
		}
		
		private void addData(Table t)
		{
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ID", t.aliasID);
			map.put("CUSTOM_NUM", "人数: " + t.custom_num);
			map.put("TABLE_NAME", t.name);

			if (t.status == 0) {
				String st = "空闲";
				map.put("STATE", "状态： " + st);
				idleList.add(map);
			} else {
				String st = "就餐";
				map.put("STATE", "状态： " + st);
				busyList.add(map);
			}
			allList.add(map);
		}
		
		@Override 
		public void handleMessage(Message msg){
			
			List<Map<String, ?>> list = null;
			boolean changed = false;
			switch (msg.what) {
			case 0:
				refreshData();
				list = allList;
				showNum();
				resetView();
				changed = true;
				break;
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
				int region= msg.what-1;
				if(regions.contains((short)(region)));
				{
					chooseRegion(region);
					list = allList;
					buttonUp();
					allBtn.setImageResource(R.drawable.alldown);
					showNum();
					titleTextView.setText(regionNames.get(msg.what));
					changed=true;
				}
				break;
			case ALL_BTN_CLICKED:
				list = allList;
				buttonUp();
				allBtn.setImageResource(R.drawable.alldown);
				changed=true;
				break;
			case IDLE_BTN_CLICKED: list = idleList;
				buttonUp();
				idleBtn.setImageResource(R.drawable.freedown);
				changed=true;
				break;
			case BUSY_BTN_CLICKED: list = busyList;
				buttonUp();
				busyBtn.setImageResource(R.drawable.eatingdown);
				changed=true;
				break;
			case CHANGE_TO_ALL: refreshData();
				list = allList;
				showNum();
				resetView();
				changed=true;
				break;
//			case CHANGE_TO_IDLE: list = idleList;
//				changed=true;
//				break;
//			case CHANGE_TO_BUSY: list = busyList;
//				changed=true;
//				break;
			case BACK_TO_ALL: list = allList;
				((TextView)findViewById(R.id.hint_text_table)).setVisibility(View.INVISIBLE);
				mListView.setVisibility(View.VISIBLE);

				changed=true;
				break;
			}
			if(changed)
				listChanged(list);
			
		}
		
		
		private void showNum()
		{
			String count=String.valueOf(allList.size());
			mAllTextView.setText(count);
			mAllTextView.setVisibility(View.VISIBLE);
			
			count=String.valueOf(idleList.size());
			mIdleTextView.setText(count);
			mIdleTextView.setVisibility(View.VISIBLE);
			
			count=String.valueOf(busyList.size());
			mBusyTextView.setText(count);
			mBusyTextView.setVisibility(View.VISIBLE);
		}
		
		/**
		 * 搜索匹配
		 * @param text 输入的字符串
		 */
		void matching(String text)
		{
			boolean isMatched = false;
			TextView hint = (TextView)findViewById(R.id.hint_text_table);
			hint.setVisibility(View.INVISIBLE);
			mListView.setVisibility(View.VISIBLE);

			List<Map<String, ?>> list = new ArrayList<Map<String, ?>>();
	
			for(Map<String,?> t : allList)
			{
				String aliasID = t.get(ITEM_TAGS[0]).toString();
				String customNum = t.get(ITEM_TAGS[1]).toString();
				String tableName = t.get(ITEM_TAGS[3]).toString();
				String state = t.get(ITEM_TAGS[2]).toString();
				if(aliasID.startsWith(text)||customNum.contains(text))
				{
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(ITEM_TAGS[0],aliasID);
					map.put(ITEM_TAGS[1],customNum );
					map.put(ITEM_TAGS[3],tableName);
					map.put(ITEM_TAGS[2], state);
					list.add(map);
					isMatched = true;
				}
			}
			if(isMatched)
			{
				listChanged(list);
			}
			else {
				list.clear();
				listChanged(list);
				hint.setVisibility(View.VISIBLE);
				mListView.setVisibility(View.GONE);
			}
		}
		
		private void resetView()
		{
			buttonUp();
			allBtn.setImageResource(R.drawable.alldown);
			titleTextView.setText("全部区域");
			mListView.setVisibility(View.VISIBLE);

			((TextView)findViewById(R.id.hint_text_table)).setVisibility(View.INVISIBLE);

		}
		private void buttonUp(){
			allBtn.setImageResource(R.drawable.all);
			idleBtn.setImageResource(R.drawable.free);
			busyBtn.setImageResource(R.drawable.eating);
		}
		
		private void listChanged(List<? extends Map<String,?>> list)
		{
			if(list!=null)
			{
				mAdapter = new SimpleAdapter(getApplicationContext(), list,
					R.layout.the_table, ITEM_TAGS,ITEM_ID);
				mListView.setAdapter(mAdapter);
			}
		}
	}

	
	private class QueryRegionTask extends AsyncTask<Void, Void, String>{
		/**
		 * 在执行请求区域信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			Toast.makeText(getApplicationContext(), "正在刷新", Toast.LENGTH_SHORT).show();
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
		/**
		 * 在执行请求区域信息前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
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
//					mHandler.refreshData();
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
				//mHandler.sendEmptyMessage(CHANGE_TO_ALL);
				((AutoCompleteTextView)findViewById(R.id.search_view_table)).setText("");
				mListView.onRefreshComplete();
				//mHandler.resetView();
				Toast.makeText(getApplicationContext(), "刷新成功",	Toast.LENGTH_SHORT).show();
				tv.setText("没有找到匹配的项");
				tv.setVisibility(View.INVISIBLE);
			} 
		}
	}
}
