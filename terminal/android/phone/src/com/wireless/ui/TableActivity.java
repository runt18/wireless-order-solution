package com.wireless.ui;

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
import android.app.Dialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.PRegion;
import com.wireless.protocol.PTable;
import com.wireless.ui.view.PullListView;
import com.wireless.ui.view.PullListView.OnRefreshListener;

public class TableActivity extends Activity {
	private PullListView mListView;
	private PopupWindow mPopWnd;

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
			for(PTable tbl : WirelessOrder.tables){
				validRegionID.add(tbl.regionID);
			}
				
			final List<PRegion> validRegions = new ArrayList<PRegion>();
			validRegions.add(new PRegion(FILTER_REGION_ALL, REGION_ALL_STR, 0));
			for(PRegion region : WirelessOrder.regions){
				if(validRegionID.contains(region.getRegionId())){
					validRegions.add(region);
				}
			}
			
			ListView popListView = (ListView)theActivity.mPopWnd.getContentView().findViewById(R.id.popWndList);

			popListView.setAdapter(new BaseAdapter() {
				
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					TextView view;
					PRegion region = validRegions.get(position);
					if(convertView == null){
						view =(TextView) LayoutInflater.from(theActivity.getApplicationContext()).inflate(R.layout.pop_wnd_item, null);
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
		
		private List<PTable> mFilterTable = new ArrayList<PTable>();
		
		private WeakReference<TableActivity> mActivity;
				
		RefreshHandler(TableActivity activity){
			mActivity = new WeakReference<TableActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg){

			final TableActivity theActivity = mActivity.get();
			
			mFilterTable.clear();
			mFilterTable.addAll(Arrays.asList(WirelessOrder.tables));
			
			Iterator<PTable> iter = mFilterTable.iterator();
			
			/**
			 * Calculate the idle and busy amount of tables
			 */
			int idleCnt = 0, busyCnt = 0, allCnt = 0;

			/**
			 * Filter the table source according to status & region condition
			 */
			while(iter.hasNext()){
				PTable t = iter.next();
				
				if(theActivity.mRegionCond == FILTER_REGION_ALL){
					if(t.isBusy()){
						busyCnt++;
					}else if(t.isIdle()){
						idleCnt++;
					}
					allCnt++;
					
				}else if(theActivity.mRegionCond == t.regionID){
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
					
				}else if(theActivity.mRegionCond == PRegion.REGION_1 && t.regionID != PRegion.REGION_1){
					iter.remove();
					
				}else if(theActivity.mRegionCond == PRegion.REGION_2 && t.regionID != PRegion.REGION_2){
					iter.remove();
					
				}else if(theActivity.mRegionCond == PRegion.REGION_3 && t.regionID != PRegion.REGION_3){
					iter.remove();
					
				}else if(theActivity.mRegionCond == PRegion.REGION_4 && t.regionID != PRegion.REGION_4){
					iter.remove();
					
				}else if(theActivity.mRegionCond == PRegion.REGION_5 && t.regionID != PRegion.REGION_5){
					iter.remove();
					
				}else if(theActivity.mRegionCond == PRegion.REGION_6 && t.regionID != PRegion.REGION_6){
					iter.remove();
					
				}else if(theActivity.mRegionCond == PRegion.REGION_7 && t.regionID != PRegion.REGION_7){
					iter.remove();
					
				}else if(theActivity.mRegionCond == PRegion.REGION_8 && t.regionID != PRegion.REGION_8){
					iter.remove();
					
				}else if(theActivity.mRegionCond == PRegion.REGION_9 && t.regionID != PRegion.REGION_9){
					iter.remove();
					
				}else if(theActivity.mRegionCond == PRegion.REGION_10 && t.regionID != PRegion.REGION_10){
					iter.remove();
					
				}else if(theActivity.mFilterCond.length() != 0){
					if(!(t.getName().contains(theActivity.mFilterCond) || Integer.toString(t.getAliasId()).startsWith(theActivity.mFilterCond))){
						iter.remove();
					}
				}
			}
			
			if(theActivity.mRegionCond == FILTER_REGION_ALL){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(REGION_ALL_STR);
				
			}else if(theActivity.mRegionCond == PRegion.REGION_1){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[0].getName());
				
			}else if(theActivity.mRegionCond == PRegion.REGION_2){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[1].getName());
				
			}else if(theActivity.mRegionCond == PRegion.REGION_3){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[2].getName());
				
			}else if(theActivity.mRegionCond == PRegion.REGION_4){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[3].getName());
				
			}else if(theActivity.mRegionCond == PRegion.REGION_5){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[4].getName());
				
			}else if(theActivity.mRegionCond == PRegion.REGION_6){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[5].getName());
				
			}else if(theActivity.mRegionCond == PRegion.REGION_7){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[6].getName());
				
			}else if(theActivity.mRegionCond == PRegion.REGION_8){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[7].getName());
				
			}else if(theActivity.mRegionCond == PRegion.REGION_9){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[8].getName());
				
			}else if(theActivity.mRegionCond == PRegion.REGION_10){
				((TextView)theActivity.findViewById(R.id.toptitle)).setText(WirelessOrder.regions[9].getName());
				
			}
			
				
			final List<Map<String, ?>> contents = new ArrayList<Map<String, ?>>();
			for(PTable tbl : mFilterTable){
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put(ITEM_THE_TABLE, tbl);
				map.put(ITEM_TAG_ID, tbl.getAliasId());
				map.put(ITEM_TAG_CUSTOM, tbl.getCustomNum());
				map.put(ITEM_TAG_TBL_NAME, tbl.getName());
				map.put(ITEM_TAG_STATE, tbl.getStatus());
				map.put(ITEM_TAG_STATE_NAME, tbl.isIdle() ? "����" : "�Ͳ�");
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
					/*
					 * set different table state's name color with state 
					 */
					short tblStatus = (Short)map.get(ITEM_TAG_STATE);
					TextView stateTxtView = (TextView)view.findViewById(R.id.table_state);
					ImageButton switchImgBtn = (ImageButton) view.findViewById(R.id.switch_table);
					if(tblStatus == (short)PTable.TABLE_BUSY){
						stateTxtView.setTextColor(Color.RED);
						switchImgBtn.setVisibility(View.VISIBLE);
					}else{
						stateTxtView.setTextColor(view.getResources().getColor(R.color.green));
						switchImgBtn.setVisibility(View.GONE);
					}
					
					switchImgBtn.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v)
						{
							theActivity.new AskTableDialog((PTable) map.get(ITEM_THE_TABLE)).show();
						}
					});
					//�µ���ť
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
						if(status == PTable.TABLE_BUSY){
							//Jump to TableDetailActivity in case of busy
							Intent intent = new Intent(TableActivity.this, TableDetailActivity.class);
							intent.putExtra(TableDetailActivity.KEY_TABLE_ID, tableAlias);
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
				mListView.requestFocus();
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
		View popupView = getLayoutInflater().inflate(R.layout.main_pop_window, null);

		// ���������View������pop-up window
		mPopWnd = new PopupWindow(
				popupView,
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, true);
		mPopWnd.setOutsideTouchable(true);
		mPopWnd.setBackgroundDrawable(new BitmapDrawable());
		mPopWnd.update();
		
		ListView popListView = (ListView)popupView.findViewById(R.id.popWndList);
		popListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PRegion region = (PRegion)view.getTag();
				mRegionCond = region.getRegionId();
				mDataHandler.sendEmptyMessage(0);
				mPopWnd.dismiss();
			}
			
		});
		//�����ϵİ�ť
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

	private class AskTableDialog extends Dialog
	{
		AskTableDialog(final PTable srcTable) {
			super(TableActivity.this, R.style.FullHeightDialog);
			setContentView(R.layout.alert);
			TextView title = (TextView)findViewById(R.id.ordername);
			title.setText("��������Ҫ������̨��:");
			
			((TextView)findViewById(R.id.table)).setText("̨�ţ�");
			Button okBtn = (Button)findViewById(R.id.confirm);
			okBtn.setText("ȷ��");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					EditText tblNoEdtTxt = (EditText)findViewById(R.id.mycount);
					try{
						int tableAlias = Integer.parseInt(tblNoEdtTxt.getText().toString().trim());
						PTable table = new PTable();
						table.setAliasId(tableAlias);
						new TransTblTask().execute(srcTable,table);
						dismiss();
					}catch(NumberFormatException e){
						Toast.makeText(TableActivity.this, "�������̨��" + tblNoEdtTxt.getText().toString().trim() + "��ʽ����ȷ������������" , Toast.LENGTH_SHORT).show();
					}

				}
			});
			
			Button cancelBtn = (Button)findViewById(R.id.alert_cancel);
			cancelBtn.setText("ȡ��");
			cancelBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();					
				}
			});
		}

		@Override
		public void onAttachedToWindow(){
			((EditText)findViewById(R.id.mycount)).setText("");
		}
	
	}
	
	private class TransTblTask extends com.wireless.lib.task.TransTblTask{
		private ProgressDialog mProgDialog;
		
		/**
		 * ��ִ������������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){			
			mProgDialog = ProgressDialog.show(TableActivity.this, "", "���ڽ�������...���Ժ�", true);
		}		
	
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�����ִ�������̨�Ĳ�����
		 */
		@Override
		protected void onPostExecute(Void nothing){
			
			mProgDialog.dismiss();
			
			/**
			 * Prompt user message if any error occurred.
			 */		
			if(mErrMsg != null){
				new AlertDialog.Builder(TableActivity.this)
				.setTitle("��ʾ")
				.setMessage(mErrMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{		
				Toast.makeText(getApplicationContext(), "��̨�ɹ�", Toast.LENGTH_SHORT).show();
				new QueryRegionTask().execute();
			}
		}
	}
	
	/**
	 * ����������Ϣ
	 */
	private class QueryRegionTask extends com.wireless.lib.task.QueryRegionTask{
		
		private ProgressDialog mProgDialog;
		
		/**
		 * ��ִ������������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){			
			mProgDialog = ProgressDialog.show(TableActivity.this, "", "���ڸ���������Ϣ...���Ժ�", true);
		}		
	
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�����ִ�������̨�Ĳ�����
		 */
		@Override
		protected void onPostExecute(PRegion[] regions){
			
			mProgDialog.dismiss();
			
			/**
			 * Prompt user message if any error occurred.
			 */		
			if(mErrMsg != null){
				mListView.onRefreshComplete();
				Toast.makeText(getApplicationContext(), "ˢ����������ʧ��,��������", Toast.LENGTH_SHORT).show();
				//mListView.setVisibility(View.GONE);
				//mRegionHandler.sendEmptyMessage(0);
				//WirelessOrder.tables = new PTable[0];
				//mDataHandler.sendEmptyMessage(0);
			}else{			
				
				WirelessOrder.regions = regions;
				
				new QueryTableTask().execute();
			}
		}
	}
	/**
	 * �����̨��Ϣ
	 */
	private class QueryTableTask extends com.wireless.lib.task.QueryTableTask {
		
		private ProgressDialog mProgDialog;
		
		/**
		 * ��ִ�������̨��Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			mProgDialog = ProgressDialog.show(TableActivity.this, "", "���ڸ��²�̨��Ϣ...���Ժ�", true);
		}


		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û��� ����ɹ�����ִ����������Ĳ�����
		 */
		@Override
		protected void onPostExecute(PTable[] tables) {
			
			mProgDialog.dismiss();
			
			/**
			 * Prompt user message if any error occurred.
			 */
			if(mBusinessException != null) {
				mListView.onRefreshComplete();
				Toast.makeText(getApplicationContext(), "ˢ�²�̨����ʧ��,��������", Toast.LENGTH_SHORT).show();

			} else {
				
				WirelessOrder.tables = tables;
				
				mListView.onRefreshComplete();				
				mRegionHandler.sendEmptyMessage(0);
				mDataHandler.sendEmptyMessage(0);
				mListView.setVisibility(View.VISIBLE);
				((TextView)findViewById(R.id.hint_text_table)).setVisibility(View.INVISIBLE);
				((AutoCompleteTextView)findViewById(R.id.search_view_table)).setText("");
				Toast.makeText(getApplicationContext(), "��̨��Ϣˢ�³ɹ�",	Toast.LENGTH_SHORT).show();
			} 
		}
	}
	/**
	 * �����ò�̨��״̬
	 */
	private abstract class QueryTableStatusTask extends com.wireless.lib.task.QueryTableStatusTask{

		private ProgressDialog _progDialog;

		QueryTableStatusTask(int tableAlias){
			super(tableAlias);
		}
		
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(TableActivity.this, "", "��ѯ" + mTblToQuery.getAliasId() + "�Ų�̨��Ϣ...���Ժ�", true);
		}
		
		/**
		 * �����Ӧ�Ĳ�������������������Ҫ�ĵ��Ĳ�̨��δ�µ�����
		 * �����Ӧ��Ϣ��ʾ���û���������ݲ�̨״̬���ֱ���ת���µ���ĵ����档
		 */
		@Override
		protected void onPostExecute(Byte tblStatus){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 * Otherwise perform the corresponding action.
			 */
			if(mErrMsg != null){
				new AlertDialog.Builder(TableActivity.this)
				.setTitle("��ʾ")
				.setMessage(mErrMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
				
			}else{			
				OnQueryTblStatus(tblStatus);
			}
		}	
		
		abstract void OnQueryTblStatus(int status);
		
	}	

}

