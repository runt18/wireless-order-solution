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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
	
	private static Handler dataHandler,regionHandler;
	
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

	private final static int REFRESH_REGION = -1;
	private static class ViewHandler extends Handler
	{
		private WeakReference<TableActivity> mActivity;
		
		ViewHandler(TableActivity activity){
			mActivity = new WeakReference<TableActivity>(activity);
		}
		ArrayList<String> regionNames = new ArrayList<String>();

		@Override
		public void handleMessage(Message msg)
		{
			final TableActivity theActivity = mActivity.get();

			switch(msg.what)
			{
			case REFRESH_REGION:
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(regionNames.get(theActivity.mRegionCond));
				Log.d("title","changed");
				break;
			default:
	
				ArrayList<Short> regions = new ArrayList<Short>();
				regionNames.clear();
				
				Table[] tableSource = WirelessOrder.tables == null ? new Table[0]: WirelessOrder.tables;
				Region[] regionSource = WirelessOrder.regions == null ? new Region[0] : WirelessOrder.regions;
				
				for (Table t : tableSource) {
					if(!regions.contains(t.regionID))
						regions.add(t.regionID);
				}
				regionNames.add("全部区域");
				for(int i = 0; i < regionSource.length; i++){
					if(regions.contains(regionSource[i].regionID)){
						regionNames.add(regionSource[i].name);
					}
				}
	//			for(String s:regionNames)
	//			{
	//				System.out.println(s);
	//			}
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(regionNames.get(theActivity.mRegionCond));
				
				ListView popListView = (ListView)theActivity.popupView.findViewById(R.id.popWndList);
				ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(theActivity.getApplicationContext(), R.layout.pop_wnd_item,regionNames);
				popListView.setAdapter(arrayAdapter);
			}
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
			mFilterTable.clear();
			final TableActivity theActivity = mActivity.get();
			
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
			}
			
			int idleCnt = 0;
			int busyCnt = 0;
				
			List<Map<String, ?>> contents = new ArrayList<Map<String, ?>>();
			for(Table tbl : mFilterTable){
					
				/**
				 * Calculate the idle and busy amount of tables
				 */
				if(tbl.status == Table.TABLE_BUSY){
					busyCnt++;
				}else if(tbl.status == Table.TABLE_IDLE){
					idleCnt++;
				}
					
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put(ITEM_TAG_ID, tbl.aliasID + (tbl.name.length() == 0 ? "" : "(" + tbl.name + ")"));
				map.put(ITEM_TAG_CUSTOM, "人数: " + tbl.custom_num);
				map.put(ITEM_TAG_TBL_NAME, tbl.name);
				map.put(ITEM_TAG_STATE, "状态： " + (tbl.status == Table.TABLE_IDLE ? "空闲" : "就餐"));
				contents.add(map);
			}
			
			if(theActivity.mTableCond!=FILTER_TABLE_IDLE&&theActivity.mTableCond!=FILTER_TABLE_BUSY){
				TextView allCountTextView = (TextView)theActivity.findViewById(R.id.left_txt_bottom);
				allCountTextView.setText(Integer.toString(mFilterTable.size()));
				allCountTextView.setVisibility(View.VISIBLE);
				TextView idleCountTxtView = (TextView)theActivity.findViewById(R.id.middle_txt_bottom);
				idleCountTxtView.setText(Integer.toString(idleCnt));
				idleCountTxtView.setVisibility(View.VISIBLE);
				
				TextView busyCountTxtView = (TextView)theActivity.findViewById(R.id.right_txt_bottom);
				busyCountTxtView.setText(Integer.toString(busyCnt));
				busyCountTxtView.setVisibility(View.VISIBLE);
			}
			

			final class ViewHolder {
				public ImageButton imgBtn;
			}
			theActivity.mListView.setAdapter(new SimpleAdapter(theActivity.getApplicationContext(), 
					 						 				   contents,
					 						 				   R.layout.the_table, 
					 						 				   TableActivity.ITEM_TAGS,
					 						 				   TableActivity.ITEM_ID){
				@Override
				public View getView(int position, View convertView, ViewGroup parent){
					ViewHolder holder = null;
					if(convertView==null)
					{
						holder = new ViewHolder();
						convertView = LayoutInflater.from(theActivity.getApplicationContext()).inflate(R.layout.the_table, null);
						holder.imgBtn = (ImageButton)convertView.findViewById(R.id.add_table);
						convertView.setTag(holder);
					}
					
					else {
						holder = (ViewHolder)convertView.getTag();
					}
					holder.imgBtn.setOnClickListener(new OnClickListener(){
			
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							System.out.println("btn clicked");
						}
					});
					return super.getView(position, convertView, parent);
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
		dataHandler = new RefreshHandler(this);
		regionHandler = new ViewHandler(this);
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
		
		mListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent,
					View view, int position, long id) {
				// TODO Auto-generated method stub
				System.out.println("position: "+position);
			}
		});
	}

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
				dataHandler.sendEmptyMessage(0);
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
				dataHandler.sendEmptyMessage(0);
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
				dataHandler.sendEmptyMessage(0);
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
				dataHandler.sendEmptyMessage(0);
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
				dataHandler.sendEmptyMessage(0);
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
			public void onItemClick(
					AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				mRegionCond=arg2;
				dataHandler.sendEmptyMessage(0);
				regionHandler.sendEmptyMessage(REFRESH_REGION);

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
				regionHandler.sendEmptyMessage(0);
				dataHandler.sendEmptyMessage(0);
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

