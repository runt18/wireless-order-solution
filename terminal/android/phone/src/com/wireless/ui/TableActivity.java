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
	
	final String[] tabs={
		"ID", "CUSTOM_NUM", "STATE",
		"TABLE_NAME" 
	};
	
	final int[] layouts={
			R.id.text1_table,
			R.id.text2_table,
			R.id.text3_table,
			R.id.text4_table
	};
	
	TableHandler mHandler;
	
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
				if(theActivity.mTableCond == FILTER_TABLE_IDLE && t.status == Table.TABLE_IDLE){
					iter.remove();
					
				}else if(theActivity.mTableCond == FILTER_TABLE_BUSY && t.status == Table.TABLE_BUSY){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_1 && t.regionID == Region.REGION_1){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_2 && t.regionID == Region.REGION_2){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_3 && t.regionID == Region.REGION_3){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_4 && t.regionID == Region.REGION_4){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_5 && t.regionID == Region.REGION_5){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_6 && t.regionID == Region.REGION_6){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_7 && t.regionID == Region.REGION_7){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_8 && t.regionID == Region.REGION_8){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_9 && t.regionID == Region.REGION_9){
					iter.remove();
					
				}else if(theActivity.mRegionCond == FILTER_REGION_10 && t.regionID == Region.REGION_10){
					iter.remove();
					
				}
				
				List<Map<String, ?>> 
				for(Table t : mFilterTable){
					
				}
				
				List<>
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

				theActivity.mListView.setAdapter(new SimpleAdapter(theActivity.getApplicationContext(), 
						 						 				   mFilterTable,
						 						 				   R.layout.the_table, 
						 						 				   theActivity.tabs,
						 						 				   theActivity.layouts));
				
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table);
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
		
		mHandler = new TableHandler();
		mListView = (PullListView) findViewById(R.id.listView_table);

		mHandler.sendEmptyMessage(CHANGE_TO_ALL);
		mListView.setOnRefreshListener(new OnRefreshListener(){
			@Override
			public void onRefresh() {
				new QueryRegionTask().execute();
			}
		});
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
		
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.pop_wnd_item,mHandler.getRegions());
		ListView popListView = (ListView)popupView.findViewById(R.id.popWndList);
		
		popListView.setAdapter(arrayAdapter);
		
		popListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(
					AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(arg2);
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
			@Override public void afterTextChanged(Editable s) {}
			@Override public void beforeTextChanged(CharSequence s,int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s,
					int start, int before, int count) {
				// TODO Auto-generated method stub
				if(s.length()!=0)
				{
					String text = s.toString().trim();
					mHandler.matching(text);
				}
				else {
					mHandler.sendEmptyMessage(BACK_TO_ALL);
				}
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
		
		/**
		 * 显示全部 按钮
		 */
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_VERTICAL,
				RelativeLayout.TRUE);
		lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
				RelativeLayout.TRUE);
		lp.setMargins(0, 0, 64, 0);
		lp.addRule(RelativeLayout.VISIBLE);

		ImageButton allBtn = (ImageButton) findViewById(R.id.btn_right);
		allBtn.setImageResource(R.drawable.home_selector);
		allBtn.setLayoutParams(lp);
		allBtn.setVisibility(View.VISIBLE);
		allBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(CHANGE_TO_ALL);
				((AutoCompleteTextView)findViewById(R.id.search_view_table)).setText("");
			}
		});
		

		/**
		 * 清空按钮
		 */
		ImageButton deleteBtn = (ImageButton)findViewById(R.id.deleteBtn_table);
		deleteBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				txtView.setText("");
			}
		});

		/**
		 * region all button
		 */
		ImageButton regionAllBtn = (ImageButton)findViewById(R.id.left_btn_bottom);
		regionAllBtn.setImageResource(R.drawable.alldown);
		regionAllBtn.setOnClickListener(new BottomOnClickListener(BottomOnClickListener.ALL));

		/**
		 *  空闲 按钮
		 */
		ImageButton freeBtn = (ImageButton)findViewById(R.id.middle_btn_bottom);
		freeBtn.setOnClickListener(new BottomOnClickListener(BottomOnClickListener.IDLE));
		
		/**
		 * 就餐列表 按钮
		 */
		ImageButton eatingBtn = (ImageButton)findViewById(R.id.right_btn_bottom);
		eatingBtn.setOnClickListener(new BottomOnClickListener(BottomOnClickListener.BUSY));
		
	}
	
	private class BottomOnClickListener implements OnClickListener{
		static final int ALL = 0;
		static final int IDLE = 1;
		static final int BUSY =2;
		private int which=0;
		BottomOnClickListener(int which)
		{
			this.which=which;
		}
		@Override
		public void onClick(View v) {
			switch(which)
			{
			case 0:mHandler.sendEmptyMessage(ALL_BTN_CLICKED);break;
			case 1:mHandler.sendEmptyMessage(IDLE_BTN_CLICKED);break;
			case 2:mHandler.sendEmptyMessage(BUSY_BTN_CLICKED);break;
			}
		}
	}

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
				String aliasID = t.get(tabs[0]).toString();
				String customNum = t.get(tabs[1]).toString();
				String tableName = t.get(tabs[3]).toString();
				String state = t.get(tabs[2]).toString();
				if(aliasID.startsWith(text)||customNum.contains(text))
				{
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(tabs[0],aliasID);
					map.put(tabs[1],customNum );
					map.put(tabs[3],tableName);
					map.put(tabs[2], state);
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
					R.layout.the_table, tabs,layouts);
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
				mHandler.sendEmptyMessage(CHANGE_TO_ALL);
				((AutoCompleteTextView)findViewById(R.id.search_view_table)).setText("");
				mListView.onRefreshComplete();
				mHandler.resetView();
				Toast.makeText(getApplicationContext(), "刷新成功",	Toast.LENGTH_SHORT).show();
				tv.setText("没有找到匹配的项");
				tv.setVisibility(View.INVISIBLE);
			} 
		}
	}
}
