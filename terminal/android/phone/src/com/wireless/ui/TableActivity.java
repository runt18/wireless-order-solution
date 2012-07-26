package com.wireless.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.ProtocolPackage;
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
	DataPackage mDataPackage;
	Context ctx;
	TextView mAllTextView,mIdleTextView,mBusyTextView;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table);
		ctx = this.getApplicationContext();
		prepareData();
		prepareUI();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		String count=String.valueOf(mDataPackage.getAllCount());
		mAllTextView.setText(count);
		mAllTextView.setVisibility(View.VISIBLE);
		
		count=String.valueOf(mDataPackage.getFreeCount());
		mIdleTextView.setText(count);
		mIdleTextView.setVisibility(View.VISIBLE);
		
		count=String.valueOf(mDataPackage.getEatingCount());
		mBusyTextView.setText(count);
		mBusyTextView.setVisibility(View.VISIBLE);
	}
	/**
	 * 初始化UI
	 */
	private void prepareUI() {
		// TODO Auto-generated method stub
		/**
		 * title
		 */
		TextView titleTextView = (TextView) findViewById(R.id.toptitle);
		titleTextView.setVisibility(View.VISIBLE);
		titleTextView.setText("餐台");
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
		 * refresh button
		 */
		ImageButton refreshBtn = (ImageButton) findViewById(R.id.btn2_right);
		refreshBtn.setVisibility(View.VISIBLE);
		refreshBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new RefreshTask().execute();

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
		allBtn.setOnClickListener(new AOnClickListener(AOnClickListener.ALL));
		
		/**
		 * 搜索框
		 */
		final AutoCompleteTextView txtView = (AutoCompleteTextView)findViewById(R.id.search_view_table);
		txtView.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s,
					int start, int count, int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s,
					int start, int before, int count) {
				// TODO Auto-generated method stub
				List<Map<String, ?>> list = null;
				if(s.length()!=0)
				{

					list = new ArrayList<Map<String, ?>>();
					Table[] tableSource = WirelessOrder.tables == null ? new Table[0]
							: WirelessOrder.tables;
					String text = s.toString().trim();
					for(Table t:tableSource)
					{

						if(String.valueOf(t.aliasID).startsWith(text)||t.name.contains(text))
						{
							HashMap<String, Object> map = new HashMap<String, Object>();
							map.put(tabs[0], t.aliasID);
							map.put(tabs[1], "人数: " + t.custom_num);
							map.put(tabs[3], t.name);
							String st= t.status==0? "空闲":"就餐";
							map.put(tabs[2],"状态： "+st);
							list.add(map);
						}

					}

				}
				else {
					list = mDataPackage.getAll();
				}
				mAdapter = new SimpleAdapter(ctx, list,
						R.layout.the_table, tabs, layouts);
				mListView.setAdapter(mAdapter);
				
			}
			
		});
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
		regionAllBtn.setOnClickListener(new AOnClickListener(AOnClickListener.ALL));
		
		mAllTextView = (TextView)findViewById(R.id.left_txt_bottom);
		
		
		/**
		 *  空闲 按钮
		 */
		ImageButton freeBtn = (ImageButton)findViewById(R.id.middle_btn_bottom);
		freeBtn.setOnClickListener(new AOnClickListener(AOnClickListener.FREE));
		mIdleTextView = (TextView)findViewById(R.id.middle_txt_bottom);
		
		/**
		 * 就餐列表 按钮
		 */
		ImageButton eatingBtn = (ImageButton)findViewById(R.id.right_btn_bottom);
		eatingBtn.setOnClickListener(new AOnClickListener(AOnClickListener.EATING));
		
		mBusyTextView = (TextView)findViewById(R.id.right_txt_bottom);
		

	}
	/**
	 * 装配数据
	 */
	private void prepareData() {
		// TODO Auto-generated method stub
		mListView = (PullListView) findViewById(R.id.listView_table);

		mDataPackage = new DataPackage();
		List<Map<String, ?>> list = mDataPackage.getAll();

		mAdapter = new SimpleAdapter(this, list,
				R.layout.the_table,tabs,layouts);
		mListView.setAdapter(mAdapter);

		mListView.setOnRefreshListener(new OnRefreshListener(){
			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub
				new RefreshTask().execute();
			}
		});

	}
	
	private class AOnClickListener implements OnClickListener{
		static final int ALL = 0;
		static final int FREE = 1;
		static final int EATING =2;
		private int which=0;
		AOnClickListener(int which)
		{
			this.which=which;
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			List<Map<String, ?>> list = null;
			switch(which)
			{
			case 0: list = mDataPackage.getAll();
				buttonUp();
				((ImageButton)findViewById(R.id.left_btn_bottom)).setImageResource(R.drawable.alldown);
				break;
			case 1: list = mDataPackage.getFree();
				buttonUp();
				((ImageButton)findViewById(R.id.middle_btn_bottom)).setImageResource(R.drawable.freedown);
				break;
			case 2: list = mDataPackage.getEating();
				buttonUp();
				((ImageButton)findViewById(R.id.right_btn_bottom)).setImageResource(R.drawable.eatingdown);
				break;
			}
			
			mAdapter = new SimpleAdapter(ctx, list,
					R.layout.the_table, tabs,layouts);
			mListView.setAdapter(mAdapter);
			
		}
		
		public void buttonUp(){
			((ImageButton)findViewById(R.id.left_btn_bottom)).setImageResource(R.drawable.all);
			((ImageButton)findViewById(R.id.middle_btn_bottom)).setImageResource(R.drawable.free);
			((ImageButton)findViewById(R.id.right_btn_bottom)).setImageResource(R.drawable.eating);
		}
		
	}
	
	private class RefreshTask extends AsyncTask<Void,Void,Void>
	{
		@Override
		protected void onPreExecute() {
			Toast.makeText(ctx, "正在刷新", Toast.LENGTH_SHORT).show();
		}
		@Override
		protected Void doInBackground(
				Void... params) {
			// 设置更新list的方法

			new QueryRegionTask().execute();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mDataPackage = new DataPackage();
			List<Map<String, ?>> list = mDataPackage
					.getAll();
			mAdapter = new SimpleAdapter(ctx, list,
					R.layout.the_table,
					new String[] { "id",
							"custonNum", "state",
							"tableName" },
					new int[] { R.id.text1_table,
							R.id.text2_table,
							R.id.text3_table,
							R.id.text4_table });
			mListView.setAdapter(mAdapter);
			mListView.onRefreshComplete();
			((ImageButton)findViewById(R.id.left_btn_bottom)).setImageResource(R.drawable.alldown);
			((ImageButton)findViewById(R.id.middle_btn_bottom)).setImageResource(R.drawable.free);
			((ImageButton)findViewById(R.id.right_btn_bottom)).setImageResource(R.drawable.eating);
		}
		
	}


	private class DataPackage {
		private List<Map<String, ?>> all, free, eating;
		public int getAllCount() {
			return all==null ? -1:all.size();
		}

		public int getFreeCount() {
			return free==null? -1: free.size();
		}

		public int getEatingCount() {
			return eating==null? -1: eating.size();
		}

		private Table[] _tableSource;

		DataPackage() {
			_tableSource = WirelessOrder.tables == null ? new Table[0]
					: WirelessOrder.tables;

			all = new ArrayList<Map<String, ?>>();
			free = new ArrayList<Map<String, ?>>();
			eating = new ArrayList<Map<String, ?>>();

			this.init();
		}

		private void init() {
			for (Table t : _tableSource) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("ID", t.aliasID);
				map.put("CUSTOM_NUM", "人数: " + t.custom_num);
				map.put("TABLE_NAME", t.name);

				if (t.status == 0) {
					String st = "空闲";
					map.put("STATE", "状态： " + st);
					free.add(map);
				} else {
					String st = "就餐";
					map.put("STATE", "状态： " + st);
					eating.add(map);
				}
				// String st= t.status==0? "空闲":"就餐";

				all.add(map);

			}
		}

		public List<Map<String, ?>> getAll() {
			return all;
		}


		public List<Map<String, ?>> getFree() {
			return free;
		}


		public List<Map<String, ?>> getEating() {
			return eating;
		}

	}

	/**
	 * 请求查询区域信息
	 */
	private class QueryRegionTask extends
			AsyncTask<Void, Void, String> {
		/**
		 * 在执行请求区域信息前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
		}

		/**
		 * 在新的线程中执行请求区域信息的操作
		 */
		@Override
		protected String doInBackground(Void... arg0) {

			String errMsg = null;
			try {
				WirelessOrder.regions = null;
				ProtocolPackage resp = ServerConnector
						.instance().ask(
								new ReqQueryRegion());
				if (resp.header.type == Type.ACK) {
					WirelessOrder.regions = RespParser
							.parseQueryRegion(resp);
				}
			} catch (IOException e) {
				errMsg = e.getMessage();
			}

			return errMsg;
		}

		/**
		 * 根据返回的error message判断，如果发错异常则提示用户， 如果成功，则执行请求餐台的操作。
		 */
		@Override
		protected void onPostExecute(String errMsg) {
			/**
			 * Prompt user message if any error occurred.
			 */
			if (errMsg != null) {
				Toast.makeText(ctx, "刷新区域数据失败",
						Toast.LENGTH_SHORT).show();

			} else {
				new QueryTableTask().execute();
			}
		}
	};

	/**
	 * 请求餐台信息
	 */
	private class QueryTableTask extends
			AsyncTask<Void, Void, String> {
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
				ProtocolPackage resp = ServerConnector
						.instance()
						.ask(new ReqQueryTable());
				if (resp.header.type == Type.ACK) {
					WirelessOrder.tables = RespParser
							.parseQueryTable(resp);
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
			if (errMsg != null) {
				Toast.makeText(ctx, "刷新餐台数据失败,请检查网络",
						Toast.LENGTH_SHORT).show();

			} else {
				Toast.makeText(ctx, "刷新成功",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
