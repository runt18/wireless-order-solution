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
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
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
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.Region;
import com.wireless.protocol.ReqQueryRegion;
import com.wireless.protocol.ReqQueryTable;
import com.wireless.protocol.ReqTableStatus;
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
	private Timer mTblReflashTimer;

	private BroadcastReceiver mReceiver;
	
	private static final String REGION_ALL_STR = "ȫ������";
	
	private static final String ITEM_TAG_ID = "ID";
	private static final String ITEM_TAG_CUSTOM = "CUSTOM_NUM";
	private static final String ITEM_TAG_STATE_NAME = "STATE";
	private static final String ITEM_TAG_STATE = "STATE_NAME";
	private static final String ITEM_TAG_TBL_NAME = "TABLE_NAME";
	
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
			validRegions.add(new Region(FILTER_REGION_ALL, REGION_ALL_STR));
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
			int idleCnt = 0, busyCnt = 0, allCnt = 0;

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
			
			if(theActivity.mRegionCond == FILTER_REGION_ALL){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(REGION_ALL_STR);
				
			}else if(theActivity.mRegionCond == Region.REGION_1){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[0].name);
				
			}else if(theActivity.mRegionCond == Region.REGION_2){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[1].name);
				
			}else if(theActivity.mRegionCond == Region.REGION_3){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[2].name);
				
			}else if(theActivity.mRegionCond == Region.REGION_4){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[3].name);
				
			}else if(theActivity.mRegionCond == Region.REGION_5){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[4].name);
				
			}else if(theActivity.mRegionCond == Region.REGION_6){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[5].name);
				
			}else if(theActivity.mRegionCond == Region.REGION_7){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[6].name);
				
			}else if(theActivity.mRegionCond == Region.REGION_8){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[7].name);
				
			}else if(theActivity.mRegionCond == Region.REGION_9){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[8].name);
				
			}else if(theActivity.mRegionCond == Region.REGION_10){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[9].name);
				
			}
			
				
			final List<Map<String, ?>> contents = new ArrayList<Map<String, ?>>();
			for(Table tbl : mFilterTable){
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put(ITEM_TAG_ID, tbl.aliasID);
				map.put(ITEM_TAG_CUSTOM,tbl.custom_num);
				map.put(ITEM_TAG_TBL_NAME, tbl.name);
				map.put(ITEM_TAG_STATE, tbl.status);
				map.put(ITEM_TAG_STATE_NAME, tbl.status == Table.TABLE_IDLE ? "����" : "�Ͳ�");
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
				hintText.setText("û��ƥ�����");
				hintText.setVisibility(View.VISIBLE);			
			}else{
				hintText.setVisibility(View.INVISIBLE);
			}

			/*
			 * set the button's image
			 */
			switch(theActivity.mTableCond){
			case FILTER_TABLE_ALL:
				theActivity.regionAllBtn.setImageResource(R.drawable.alldown);
				theActivity.idleBtn.setImageResource(R.drawable.free);
				theActivity.busyBtn.setImageResource(R.drawable.eating);
				break;
			case FILTER_TABLE_IDLE:
				theActivity.regionAllBtn.setImageResource(R.drawable.all);
				theActivity.idleBtn.setImageResource(R.drawable.freedown);
				theActivity.busyBtn.setImageResource(R.drawable.eating);
				break;
			case FILTER_TABLE_BUSY:
				theActivity.regionAllBtn.setImageResource(R.drawable.all);
				theActivity.idleBtn.setImageResource(R.drawable.free);
				theActivity.busyBtn.setImageResource(R.drawable.eatingdown);
			}
			
			theActivity.mListView.setAdapter(new SimpleAdapter(theActivity.getApplicationContext(), 
					   contents,
					   R.layout.table_item, 
					   TableActivity.ITEM_TAGS,
					   TableActivity.ITEM_ID){
				@Override
				public View getView(int position, View convertView, ViewGroup parent){
					View view = super.getView(position, convertView, parent);
					final Map<String, ?> map = contents.get(position);
					view.setTag(map.get(ITEM_TAG_ID));
					//TODO
					/*
					 * set different table state's name color with state 
					 */
					short tblStatus = (Short)map.get(ITEM_TAG_STATE);
					TextView stateTxtView = (TextView)view.findViewById(R.id.table_state);
					RelativeLayout itemLayout = (RelativeLayout) view.findViewById(R.id.table_item_layout);
					if(tblStatus == (short)Table.TABLE_BUSY){
						stateTxtView.setTextColor(Color.RED);
						itemLayout.setBackgroundResource(R.drawable.busy_item_bg);
						
					}else{
						stateTxtView.setTextColor(view.getResources().getColor(R.color.green));
						itemLayout.setBackgroundResource(R.drawable.table_item_selector);
					}
					
					((ImageButton)view.findViewById(R.id.add_table)).setOnClickListener(new OnClickListener(){			
						@Override
						public void onClick(View v) {
							final int tableAlias = (Integer)map.get(ITEM_TAG_ID);
							theActivity.new QueryTableStatusTask(tableAlias){
								@Override
								void OnQueryTblStatus(int status) {
									if(status == Table.TABLE_IDLE){
										//jump to the order activity with the table id if the table is idle
										Intent intent = new Intent(theActivity, OrderActivity.class);
										intent.putExtra(MainActivity.KEY_TABLE_ID, String.valueOf(tableAlias));
										theActivity.startActivity(intent);
									}else if(status == Table.TABLE_BUSY){
										//jump to change order activity with the table alias id if the table is busy
										Intent intent = new Intent(theActivity, ChgOrderActivity.class);
										intent.putExtra(MainActivity.KEY_TABLE_ID, String.valueOf(tableAlias));
										theActivity.startActivity(intent);
									}
								}								
							}.execute();
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
	protected void onStart(){
		super.onStart();
		new QueryRegionTask().execute();
		
		/**
		 * ����ϵͳ�¼�������Ļ�ر�ʱkill����̨����Timer��
		 * ��Ļ����ʱ���´򿪲�̨����Timer
		 */
		mReceiver = new BroadcastReceiver() {			
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
					if(mTblReflashTimer != null){
						mTblReflashTimer.cancel();
					}
				}else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){					
					startReflashTimer();
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
		startReflashTimer();
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
	
	private void startReflashTimer(){
		if(mTblReflashTimer != null){
			mTblReflashTimer.cancel();
		}
		
		mTblReflashTimer = new Timer();

		/**
		 * ��MIN_PERIOD��MAX_PERIOD֮�����һ�����ʱ�䣬�������ʱ��������ڸ��²�̨��Ϣ
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
	 * ��ʼ��UI
	 */
	private void prepareUI() {
		
		/**
		 * Title Text View
		 */		
		TextView titleTextView = (TextView) findViewById(R.id.toptitle);
		titleTextView.setVisibility(View.VISIBLE);
		titleTextView.setBackgroundResource(R.drawable.title_selector);
		
		/**
		 * "����"Button
		 */
		TextView leftTxtView = (TextView) findViewById(R.id.textView_left);
		leftTxtView.setText("����");
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
		 * ������
		 */
		final AutoCompleteTextView searchTxtView = (AutoCompleteTextView)findViewById(R.id.search_view_table);
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
		mListView = (PullListView) findViewById(R.id.listView_table);
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
					void OnQueryTblStatus(int status) {
						if(status == Table.TABLE_BUSY){
							//Jump to TableDetailActivity in case of busy
							Intent intent = new Intent(TableActivity.this,TableDetailActivity.class);
							intent.putExtra(MainActivity.KEY_TABLE_ID, tableAlias);
							startActivity(intent);
						}else{
							//Prompt user in case of idle
							Toast.makeText(TableActivity.this, tableAlias + "�Ų�̨��ǰ�ǿ���״̬", Toast.LENGTH_SHORT).show();
						}
					}
				}.execute();

			}
		});
		
		/**
		 * ��ƷList����ʱ���������
		 */
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(searchTxtView.getWindowToken(), 0);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				mListView.setFirstItem(firstVisibleItem);
			}
		});
		/**
		 * ��ȫ������ť
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
		allTextView.setText("ˢ��");
		allTextView.setVisibility(View.VISIBLE);
		
		
		/**
		 * �����С���ť
		 */
		idleBtn = (ImageButton)findViewById(R.id.middle_btn_bottom);
		idleBtn.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				searchTxtView.setText("");
				mTableCond = FILTER_TABLE_IDLE;
				mDataHandler.sendEmptyMessage(0);
			}
			
		});

		/**
		 * ���Ͳ͡���ť
		 */
		busyBtn = (ImageButton)findViewById(R.id.right_btn_bottom);
		busyBtn.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				searchTxtView.setText("");
				mTableCond = FILTER_TABLE_BUSY;
				mDataHandler.sendEmptyMessage(0);
			}
		});
		
		/**
		 * ����ա���ť
		 */
		ImageButton deleteBtn = (ImageButton)findViewById(R.id.deleteBtn_table);
		deleteBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				searchTxtView.setText("");
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
				searchTxtView.setText("");
				mTableCond = FILTER_TABLE_ALL;
				mDataHandler.sendEmptyMessage(0);
			}
		});
		
		// ���������̨״̬�󵯳������View
		popupView = getLayoutInflater().inflate(R.layout.main_pop_window, null);

		// ���������View������pop-up window
		final PopupWindow popWnd = new PopupWindow(
				popupView,
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, true);
		popWnd.setOutsideTouchable(true);
		popWnd.setBackgroundDrawable(new BitmapDrawable());
		popWnd.update();
		
		ListView popListView = (ListView)popupView.findViewById(R.id.popWndList);
		popListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Region region = (Region)view.getTag();
				mRegionCond = region.regionID;
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
					popWnd.showAsDropDown(v, -44, 0);
				}
			}
		});
		
	}

	/**
	 * Generate the message according to the error code 
	 * @param tableID the table id associated with this error
	 * @param errCode the error code
	 * @return the error message
	 */
	private String genErrMsg(int tableID, byte errCode){
		if(errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
			return "�ն�û�еǼǵ�����������ϵ������Ա��";
		}else if(errCode == ErrorCode.TERMINAL_EXPIRED) {
			return "�ն��ѹ��ڣ�����ϵ������Ա��";
		}else if(errCode == ErrorCode.TABLE_NOT_EXIST){
			return tableID + "�Ų�̨��Ϣ������";
		}else{
			return null;
		}
	}
	
	/**
	 * ����������Ϣ
	 */
	private class QueryRegionTask extends AsyncTask<Void, Void, String>{
		
		private ProgressDialog mProgDialog;
		
		/**
		 * ��ִ������������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){			
			mProgDialog = ProgressDialog.show(TableActivity.this, "", "���ڸ���������Ϣ...���Ժ�", true);
		}
		
		/**
		 * ���µ��߳���ִ������������Ϣ�Ĳ���
		 */
		@Override
		protected String doInBackground(Void... arg0) {
		
			String errMsg = null;
			try{
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryRegion());
				if(resp.header.type == Type.ACK){
					WirelessOrder.regions = RespParser.parseQueryRegion(resp);
				}else{
					WirelessOrder.regions = new Region[0];
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			
			return errMsg;
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�����ִ�������̨�Ĳ�����
		 */
		@Override
		protected void onPostExecute(String errMsg){
			
			mProgDialog.dismiss();
			
			/**
			 * Prompt user message if any error occurred.
			 */		
			if(errMsg != null){
				mListView.onRefreshComplete();
				Toast.makeText(getApplicationContext(), "ˢ����������ʧ��,��������", Toast.LENGTH_SHORT).show();
				mListView.setVisibility(View.GONE);
				mRegionHandler.sendEmptyMessage(0);
				WirelessOrder.tables = new Table[0];
				mDataHandler.sendEmptyMessage(0);
			}else{				
				new QueryTableTask().execute();
			}
		}
	};
	/**
	 * �����̨��Ϣ
	 */
	private class QueryTableTask extends AsyncTask<Void, Void, String> {
		
		private ProgressDialog mProgDialog;
		
		/**
		 * ��ִ�������̨��Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			mProgDialog = ProgressDialog.show(TableActivity.this, "", "���ڸ��²�̨��Ϣ...���Ժ�", true);
		}

		/**
		 * ���µ��߳���ִ�������̨��Ϣ�Ĳ���
		 */
		@Override
		protected String doInBackground(Void... arg0) {

			String errMsg = null;
			try {
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryTable());
				if (resp.header.type == Type.ACK) {
					WirelessOrder.tables = RespParser.parseQueryTable(resp);
				}else{
					WirelessOrder.tables = new Table[0];
				}
			} catch (IOException e) {
				errMsg = e.getMessage();
			}

			return errMsg;
		}

		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û��� ����ɹ�����ִ����������Ĳ�����
		 */
		@Override
		protected void onPostExecute(String errMsg) {
			
			mProgDialog.dismiss();
			
			/**
			 * Prompt user message if any error occurred.
			 */
			if (errMsg != null) {
				mListView.onRefreshComplete();
				Toast.makeText(getApplicationContext(), "ˢ�²�̨����ʧ��,��������", Toast.LENGTH_SHORT).show();

			} else {
				mRegionHandler.sendEmptyMessage(0);
				mDataHandler.sendEmptyMessage(0);
				mListView.setVisibility(View.VISIBLE);
				((TextView)findViewById(R.id.hint_text_table)).setVisibility(View.INVISIBLE);
				((AutoCompleteTextView)findViewById(R.id.search_view_table)).setText("");
				mListView.onRefreshComplete();
				Toast.makeText(getApplicationContext(), "��̨��Ϣˢ�³ɹ�",	Toast.LENGTH_SHORT).show();
			} 
		}
	}
	/**
	 * �����ò�̨��״̬
	 */
	private abstract class QueryTableStatusTask extends AsyncTask<Void, Void, String>{

		private byte _tableStatus = Table.TABLE_IDLE;
		private int _tableAlias;
		private ProgressDialog _progDialog;

		QueryTableStatusTask(int tableAlias){
			_tableAlias =  tableAlias;
		}
		
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(TableActivity.this, "", "��ѯ" + _tableAlias + "�Ų�̨��Ϣ...���Ժ�", true);
		}
		
		/**
		 * ���µ��߳���ִ�������̨״̬�Ĳ���
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			try{
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqTableStatus(_tableAlias));

				if(resp.header.type == Type.ACK){
					_tableStatus = resp.header.reserved;
					
				}else{
					errMsg = genErrMsg(_tableAlias, resp.header.reserved);
					if(errMsg == null){
						errMsg = "δȷ�����쳣����(" + resp.header.reserved + ")";
					}
				}					
				
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			
			return errMsg;
		}
		
		/**
		 * �����Ӧ�Ĳ�������������������Ҫ�ĵ��Ĳ�̨��δ�µ�����
		 * �����Ӧ��Ϣ��ʾ���û���������ݲ�̨״̬���ֱ���ת���µ���ĵ����档
		 */
		@Override
		protected void onPostExecute(String errMsg){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 * Otherwise perform the corresponding action.
			 */
			if(errMsg != null){
				new AlertDialog.Builder(TableActivity.this)
				.setTitle("��ʾ")
				.setMessage(errMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
				
			}else{			
				OnQueryTblStatus(_tableStatus);
			}
		}	
		
		abstract void OnQueryTblStatus(int status);
		
	}	

}

