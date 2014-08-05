package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.ui.dialog.AskTableDialog;
import com.wireless.ui.dialog.AskTableDialog.OnTableSelectedListener;
import com.wireless.ui.view.PullListView;
import com.wireless.ui.view.PullListView.OnRefreshListener;

public class TableActivity extends FragmentActivity implements OnTableSelectedListener{
	private PullListView mListView;
	private PopupWindow mPopWnd;

	private ImageButton regionAllBtn ;
	private ImageButton idleBtn;
	private ImageButton busyBtn;
	private Timer mTblReflashTimer;
	private Table mSrcTbl;
	
	private BroadcastReceiver mReceiver;
	
	private static final String REGION_ALL_STR = "全部区域";
	
	private static final String ITEM_TAG_ID = "ID";
	private static final String ITEM_TAG_CUSTOM = "CUSTOM_NUM";
	private static final String ITEM_TAG_STATE_NAME = "STATE";
	private static final String ITEM_TAG_STATE = "STATE_NAME";
	private static final String ITEM_TAG_TBL_NAME = "TABLE_NAME";
	private static final String ITEM_THE_TABLE = "the_table";

	
	private static final String[] ITEM_TAGS = {
			ITEM_TAG_ID, 
			ITEM_TAG_CUSTOM, 
			ITEM_TAG_STATE_NAME,
			ITEM_TAG_TBL_NAME 
	};
	
	private static final int[] ITEM_ID = {
		R.id.table_id_text,
		R.id.table_cnt,
		R.id.table_state,
		R.id.table_name_text
	};

	private static Handler mDataHandler;
	private static Handler mRegionHandler;
	
	private int mTableCond = FILTER_TABLE_ALL;			//the current activity_table filter condition
	
	private final static int FILTER_TABLE_ALL = 0;		//activity_table filter condition to all
	private final static int FILTER_TABLE_IDLE = 1;		//activity_table filter condition to idle
	private final static int FILTER_TABLE_BUSY = 2;		//activity_table filter condition to busy
	
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
	
			ListView popListView = (ListView)theActivity.mPopWnd.getContentView().findViewById(R.id.listView_region_popup);

			popListView.setAdapter(new BaseAdapter() {
				
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					TextView view;
					Region region = WirelessOrder.regions.get(position);
					if(convertView == null){
						view =(TextView) LayoutInflater.from(theActivity.getApplicationContext()).inflate(R.layout.region_popup_wnd_item, parent, false);
					}else{
						view = (TextView)convertView;
					}
					
					view.setText(region.getName());
					view.setTag(region);
					
					return view;
				}
				
				@Override
				public long getItemId(int position) {
					return position;
				}
				
				@Override
				public Object getItem(int position) {
					return WirelessOrder.regions.get(position);
				}
				
				@Override
				public int getCount() {
					return WirelessOrder.regions.size();
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
			mFilterTable.addAll(WirelessOrder.tables);
			
			Iterator<Table> iter = mFilterTable.iterator();
			
			/**
			 * Calculate the idle and busy amount of tables
			 */
			int idleCnt = 0, busyCnt = 0, allCnt = 0;

			/**
			 * Filter the activity_table source according to status & region condition
			 */
			while(iter.hasNext()){
				Table t = iter.next();
				
				if(theActivity.mRegionCond == FILTER_REGION_ALL){
					if(t.isBusy()){
						busyCnt++;
					}else if(t.isIdle()){
						idleCnt++;
					}
					allCnt++;
					
				}else if(theActivity.mRegionCond == t.getRegion().getId()){
					if(t.isBusy()){
						busyCnt++;
					}else if(t.isIdle()){
						idleCnt++;
					}
					allCnt++;
				}				

				
				if(theActivity.mTableCond == FILTER_TABLE_IDLE && !t.isIdle()){
					iter.remove();
					
				}else if(theActivity.mTableCond == FILTER_TABLE_BUSY && !t.isBusy()){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.RegionId.REGION_1.getId() && t.getRegion().getId() != Region.RegionId.REGION_1.getId()){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.RegionId.REGION_2.getId() && t.getRegion().getId() != Region.RegionId.REGION_2.getId()){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.RegionId.REGION_3.getId() && t.getRegion().getId() != Region.RegionId.REGION_3.getId()){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.RegionId.REGION_4.getId() && t.getRegion().getId() != Region.RegionId.REGION_4.getId()){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.RegionId.REGION_5.getId() && t.getRegion().getId() != Region.RegionId.REGION_5.getId()){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.RegionId.REGION_6.getId() && t.getRegion().getId() != Region.RegionId.REGION_6.getId()){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.RegionId.REGION_7.getId() && t.getRegion().getId() != Region.RegionId.REGION_7.getId()){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.RegionId.REGION_8.getId() && t.getRegion().getId() != Region.RegionId.REGION_8.getId()){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.RegionId.REGION_9.getId() && t.getRegion().getId() != Region.RegionId.REGION_9.getId()){
					iter.remove();
					
				}else if(theActivity.mRegionCond == Region.RegionId.REGION_10.getId() && t.getRegion().getId() != Region.RegionId.REGION_10.getId()){
					iter.remove();
					
				}else if(theActivity.mFilterCond.length() != 0){
					if(!(t.getName().contains(theActivity.mFilterCond) || Integer.toString(t.getAliasId()).startsWith(theActivity.mFilterCond))){
						iter.remove();
					}
				}
			}
			
			if(theActivity.mRegionCond == FILTER_REGION_ALL){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(REGION_ALL_STR);
				
			}else if(theActivity.mRegionCond == Region.RegionId.REGION_1.getId()){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions.get(0).getName());
				
			}else if(theActivity.mRegionCond == Region.RegionId.REGION_2.getId()){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions.get(1).getName());
				
			}else if(theActivity.mRegionCond == Region.RegionId.REGION_3.getId()){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions.get(2).getName());
				
			}else if(theActivity.mRegionCond == Region.RegionId.REGION_4.getId()){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions.get(3).getName());
				
			}else if(theActivity.mRegionCond == Region.RegionId.REGION_5.getId()){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions.get(4).getName());
				
			}else if(theActivity.mRegionCond == Region.RegionId.REGION_6.getId()){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions.get(5).getName());
				
			}else if(theActivity.mRegionCond == Region.RegionId.REGION_7.getId()){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions.get(6).getName());
				
			}else if(theActivity.mRegionCond == Region.RegionId.REGION_8.getId()){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions.get(7).getName());
				
			}else if(theActivity.mRegionCond == Region.RegionId.REGION_9.getId()){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions.get(8).getName());
				
			}else if(theActivity.mRegionCond == Region.RegionId.REGION_10.getId()){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions.get(9).getName());
				
			}
			
				
			final List<Map<String, ?>> contents = new ArrayList<Map<String, ?>>();
			for(Table tbl : mFilterTable){
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put(ITEM_THE_TABLE, tbl);
				map.put(ITEM_TAG_ID, tbl.getAliasId());
				map.put(ITEM_TAG_CUSTOM, tbl.getCustomNum());
				map.put(ITEM_TAG_TBL_NAME, tbl.getName());
				map.put(ITEM_TAG_STATE, tbl.getStatus());
				map.put(ITEM_TAG_STATE_NAME, tbl.isIdle() ? "空闲" : "就餐");
				contents.add(map);
			}
			
			/*
			 * set the counts
			 */
			TextView allCountTextView = (TextView)theActivity.findViewById(R.id.txtView_allAmount_table);
			allCountTextView.setText(Integer.toString(allCnt));
			allCountTextView.setVisibility(View.VISIBLE);
			TextView idleCountTxtView = (TextView)theActivity.findViewById(R.id.txtView_busyAmount_table);
			idleCountTxtView.setText(Integer.toString(idleCnt));
			idleCountTxtView.setVisibility(View.VISIBLE);
			
			TextView busyCountTxtView = (TextView)theActivity.findViewById(R.id.txtView_idleAmount_table);
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

			/*
			 * set the button's image
			 */
			switch(theActivity.mTableCond){
			case FILTER_TABLE_ALL:
				theActivity.regionAllBtn.setImageResource(R.drawable.all_down);
				theActivity.idleBtn.setImageResource(R.drawable.table_idle);
				theActivity.busyBtn.setImageResource(R.drawable.table_busy);
				break;
			case FILTER_TABLE_IDLE:
				theActivity.regionAllBtn.setImageResource(R.drawable.all);
				theActivity.idleBtn.setImageResource(R.drawable.table_idle_down);
				theActivity.busyBtn.setImageResource(R.drawable.table_busy);
				break;
			case FILTER_TABLE_BUSY:
				theActivity.regionAllBtn.setImageResource(R.drawable.all);
				theActivity.idleBtn.setImageResource(R.drawable.table_idle);
				theActivity.busyBtn.setImageResource(R.drawable.table_busy_down);
			}
			
			theActivity.mListView.setAdapter(new SimpleAdapter(theActivity.getApplicationContext(), 
															   contents,
															   R.layout.table_activity_list_item, 
															   TableActivity.ITEM_TAGS,
															   TableActivity.ITEM_ID){
				@Override
				public View getView(int position, View convertView, ViewGroup parent){
					View view = super.getView(position, convertView, parent);
					final Map<String, ?> map = contents.get(position);
					view.setTag(map.get(ITEM_TAG_ID));
					/*
					 * set different activity_table state's name color with state 
					 */
					Table.Status tblStatus = (Table.Status)map.get(ITEM_TAG_STATE);
					TextView stateTxtView = (TextView)view.findViewById(R.id.table_state);
					ImageButton switchImgBtn = (ImageButton) view.findViewById(R.id.switch_table);
					if(tblStatus == Table.Status.BUSY){
						stateTxtView.setTextColor(Color.RED);
						switchImgBtn.setVisibility(View.VISIBLE);
					}else{
						stateTxtView.setTextColor(view.getResources().getColor(R.color.green));
						switchImgBtn.setVisibility(View.GONE);
					}
					
					//换台Button
					switchImgBtn.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v){
							theActivity.mSrcTbl = (Table) map.get(ITEM_THE_TABLE);
							AskTableDialog.newInstance().show(theActivity.getSupportFragmentManager(), AskTableDialog.TAG);
						}
					});
					
					//下单Button
					((ImageButton)view.findViewById(R.id.add_table)).setOnClickListener(new OnClickListener(){			
						@Override
						public void onClick(View v) {
							final int tableAlias = (Integer)map.get(ITEM_TAG_ID);
							
							Intent intent = new Intent(theActivity, OrderActivity.class);
							intent.putExtra(OrderActivity.KEY_TABLE_ID, String.valueOf(tableAlias));
							theActivity.startActivity(intent);
							
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
		setContentView(R.layout.table_activity);
		
		regionAllBtn = (ImageButton)findViewById(R.id.imgButton_all_table);
		idleBtn = (ImageButton)findViewById(R.id.imgButton_idle_table);
		busyBtn = (ImageButton)findViewById(R.id.imgButton_busy_table);
		mDataHandler = new RefreshHandler(this);
		mRegionHandler = new RegionRefreshHandler(this);
		prepareUI();		
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		new QueryRegionTask().execute();
		
		/**
		 * 接收系统事件，在屏幕关闭时kill掉餐台更新Timer，
		 * 屏幕点亮时重新打开餐台更新Timer
		 */
		mReceiver = new BroadcastReceiver() {			
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
					if(mTblReflashTimer != null){
						mTblReflashTimer.cancel();
					}
				}else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){					
					startRefreshTimer();
				}
			}
		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(mReceiver, intentFilter);
	}

	@Override
	protected void onResume(){
		super.onResume();			
		startRefreshTimer();
	}

	@Override
	protected void onPause(){
		super.onPause();
		if(mTblReflashTimer != null){
			mTblReflashTimer.cancel();
		}
	}	
	
	@Override 
	protected void onStop(){
		super.onStop();
		unregisterReceiver(mReceiver);	
	}
	
	private void startRefreshTimer(){
		if(mTblReflashTimer != null){
			mTblReflashTimer.cancel();
		}
		
		mTblReflashTimer = new Timer();

		/**
		 * 在MIN_PERIOD和MAX_PERIOD之间产生一个随机时间，按照这个时间段来定期更新餐台信息
		 */
		final long MIN_PERIOD = 5 * 60 * 1000;
		final long MAX_PERIOD = 10 * 60 * 1000;
		mTblReflashTimer.schedule(new TimerTask() {
									@Override
									public void run() {
										mListView.post(new Runnable() {
											@Override
											public void run() {
												new QueryRegionTask().execute();
											}
										});
									}
								}, 
								Math.round(Math.random() * (MAX_PERIOD - MIN_PERIOD) + MIN_PERIOD), 
								Math.round(Math.random() * (MAX_PERIOD - MIN_PERIOD) + MIN_PERIOD));
	}
	
	/**
	 * 初始化UI
	 */
	private void prepareUI() {
		
		/**
		 * Title Text View
		 */		
		TextView titleTextView = (TextView) findViewById(R.id.toptitle);
		titleTextView.setVisibility(View.VISIBLE);
		titleTextView.setBackgroundResource(R.drawable.title_selector);
		
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
		final AutoCompleteTextView searchTxtView = (AutoCompleteTextView)findViewById(R.id.txtView_srch_table);
		searchTxtView.addTextChangedListener(new TextWatcher(){
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
		 * PullListView
		 */
		mListView = (PullListView) findViewById(R.id.pull_listView_table);
		mListView.setOnRefreshListener(new OnRefreshListener(){
			@Override
			public void onRefresh() {
				new QueryRegionTask().execute();
			}
		});
		/*
		 * the listview's item listener 
		 */
		mListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final int tableAlias = (Integer)view.getTag();
				new QueryTableStatusTask(tableAlias) {					
					@Override
					void OnQueryTblStatus(Table.Status status) {
						if(status == Table.Status.BUSY){
							//Jump to TableDetailActivity in case of busy
							Intent intent = new Intent(TableActivity.this, TableDetailActivity.class);
							intent.putExtra(TableDetailActivity.KEY_TABLE_ID, tableAlias);
							startActivity(intent);
						}else{
							//Prompt user in case of idle
							Toast.makeText(TableActivity.this, tableAlias + "号餐台当前是空闲状态", Toast.LENGTH_SHORT).show();
						}
					}
				}.execute();

			}
		});
		
		/**
		 * 菜品List滚动时隐藏软键盘
		 */
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(searchTxtView.getWindowToken(), 0);
				mListView.requestFocus();
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				mListView.setFirstItem(firstVisibleItem);
			}
		});
		
		/**
		 * “全部”按钮
		 */
		ImageButton allBtn = (ImageButton)findViewById(R.id.btn_right);
		allBtn.setVisibility(View.VISIBLE);
		allBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mTableCond = FILTER_TABLE_ALL;
				mRegionCond = FILTER_REGION_ALL;
				
				new QueryRegionTask().execute();
				searchTxtView.setText("");
			}
		});
		
		TextView allTextView = (TextView)findViewById(R.id.textView_right);
		allTextView.setText("刷新");
		allTextView.setVisibility(View.VISIBLE);
		
		
		/**
		 * “空闲”按钮
		 */
		idleBtn = (ImageButton)findViewById(R.id.imgButton_idle_table);
		idleBtn.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				searchTxtView.setText("");
				mTableCond = FILTER_TABLE_IDLE;
				mDataHandler.sendEmptyMessage(0);
			}
			
		});

		/**
		 * “就餐”按钮
		 */
		busyBtn = (ImageButton)findViewById(R.id.imgButton_busy_table);
		busyBtn.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				searchTxtView.setText("");
				mTableCond = FILTER_TABLE_BUSY;
				mDataHandler.sendEmptyMessage(0);
			}
		});
		
		/**
		 * “清空”按钮
		 */
		ImageButton deleteBtn = (ImageButton)findViewById(R.id.imgButton_clearSrch_table);
		deleteBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				searchTxtView.setText("");
			}
		});

		/**
		 * region all button
		 */
		regionAllBtn = (ImageButton)findViewById(R.id.imgButton_all_table);
		regionAllBtn.setImageResource(R.drawable.all_down);
		regionAllBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				searchTxtView.setText("");
				mTableCond = FILTER_TABLE_ALL;
				mDataHandler.sendEmptyMessage(0);
			}
		});
		
		// 创建点击餐台状态后弹出区域的View
		View popupView = getLayoutInflater().inflate(R.layout.region_popup_wnd, null);

		// 创建与这个View关联的pop-up window
		mPopWnd = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		mPopWnd.setOutsideTouchable(true);
		mPopWnd.setBackgroundDrawable(new BitmapDrawable());
		mPopWnd.update();
		
		ListView popListView = (ListView)popupView.findViewById(R.id.listView_region_popup);
		popListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Region region = (Region)view.getTag();
				mRegionCond = region.getId();
				mDataHandler.sendEmptyMessage(0);
				mPopWnd.dismiss();
			}
			
		});
		//标题上的按钮
		ImageButton titleBtn = (ImageButton)findViewById(R.id.title_btn_top);
		titleBtn.setVisibility(View.VISIBLE);
		titleBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (mPopWnd.isShowing()) {
					mPopWnd.dismiss();
				} else {
					mPopWnd.showAsDropDown(v, -34, 0);
				}
			}
		});
		
	}

	private class TransTblTask extends com.wireless.lib.task.TransTblTask{
		private ProgressDialog mProgDialog;
		
		TransTblTask(Table srcTbl, Table destTbl){
			super(WirelessOrder.loginStaff, srcTbl, destTbl);
		}
		
		/**
		 * 在执行请求区域信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			mProgDialog = ProgressDialog.show(TableActivity.this, "", "正在交换数据...请稍后", true);
		}		
	
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则执行请求餐台的操作。
		 */
		@Override
		protected void onPostExecute(Void nothing){
			
			mProgDialog.dismiss();
			
			/**
			 * Prompt user message if any error occurred.
			 */		
			if(mErrMsg != null){
				new AlertDialog.Builder(TableActivity.this)
				.setTitle("提示")
				.setMessage(mErrMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{		
				Toast.makeText(getApplicationContext(), "换台成功", Toast.LENGTH_SHORT).show();
				new QueryRegionTask().execute();
			}
		}
	}
	
	/**
	 * 请求区域信息
	 */
	private class QueryRegionTask extends com.wireless.lib.task.QueryRegionTask{
		
		private ProgressDialog mProgDialog;
		
		/**
		 * 在执行请求区域信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			mProgDialog = ProgressDialog.show(TableActivity.this, "", "正在更新区域信息...请稍后", true);
		}		
	
		QueryRegionTask(){
			super(WirelessOrder.loginStaff);
		}
		
		@Override
		protected void onSuccess(List<Region> regions){
			mProgDialog.dismiss();
			WirelessOrder.regions.clear();
			WirelessOrder.regions.addAll(regions);
			
			new QueryTableTask().execute();
		}
		
		@Override
		protected void onFail(BusinessException e){
			mProgDialog.dismiss();
			mListView.onRefreshComplete();
			Toast.makeText(getApplicationContext(), "刷新区域数据失败,请检查网络", Toast.LENGTH_SHORT).show();
		}
	}
	/**
	 * 请求餐台信息
	 */
	private class QueryTableTask extends com.wireless.lib.task.QueryTableTask {
		
		private ProgressDialog mProgDialog;
		
		QueryTableTask(){
			super(WirelessOrder.loginStaff);
		}
		
		/**
		 * 在执行请求餐台信息前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			mProgDialog = ProgressDialog.show(TableActivity.this, "", "正在更新餐台信息...请稍后", true);
		}


		@Override
		protected void onSuccess(List<Table> tables){
			
			mProgDialog.dismiss();
			WirelessOrder.tables.clear();
			WirelessOrder.tables.addAll(tables);
			
			mListView.onRefreshComplete();				
			mRegionHandler.sendEmptyMessage(0);
			mDataHandler.sendEmptyMessage(0);
			mListView.setVisibility(View.VISIBLE);
			((TextView)findViewById(R.id.hint_text_table)).setVisibility(View.INVISIBLE);
			((AutoCompleteTextView)findViewById(R.id.txtView_srch_table)).setText("");
			Toast.makeText(getApplicationContext(), "餐台信息刷新成功",	Toast.LENGTH_SHORT).show();
		}
		
		@Override
		protected void onFail(BusinessException e){
			mProgDialog.dismiss();

			mListView.onRefreshComplete();
			Toast.makeText(getApplicationContext(), "刷新餐台数据失败,请检查网络", Toast.LENGTH_SHORT).show();

		}
		
	}
	/**
	 * 请求获得餐台的状态
	 */
	private abstract class QueryTableStatusTask extends com.wireless.lib.task.QueryTableStatusTask{

		private ProgressDialog _progDialog;

		QueryTableStatusTask(int tableAlias){
			super(WirelessOrder.loginStaff, tableAlias);
		}
		
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(TableActivity.this, "", "查询" + mTblToQuery.getAliasId() + "号餐台信息...请稍候", true);
		}
		
		/**
		 * 如果相应的操作不符合条件（比如要改单的餐台还未下单），
		 * 则把相应信息提示给用户，否则根据餐台状态，分别跳转到下单或改单界面。
		 */
		@Override
		protected void onPostExecute(Table.Status tblStatus){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 * Otherwise perform the corresponding action.
			 */
			if(mErrMsg != null){
				new AlertDialog.Builder(TableActivity.this)
				.setTitle("提示")
				.setMessage(mErrMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
				
			}else{			
				OnQueryTblStatus(tblStatus);
			}
		}	
		
		abstract void OnQueryTblStatus(Table.Status status);
		
	}

	@Override
	public void onTableSelected(Table destTbl) {
		new TransTblTask(mSrcTbl, destTbl).execute();
	}	

}

