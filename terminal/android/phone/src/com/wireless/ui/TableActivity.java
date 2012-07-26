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
	private SimpleAdapter adapter;
	PullListView lv;
	DataPackage dataPackage;
	Context ctx;
	TextView allText,freeText,eatingText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table);
		ctx = this.getApplicationContext();
		init();
		prepareUI();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		String count=String.valueOf(dataPackage.getAllCount());
		allText.setText(count);
		allText.setVisibility(View.VISIBLE);
		
		count=String.valueOf(dataPackage.getFreeCount());
		freeText.setText(count);
		freeText.setVisibility(View.VISIBLE);
		
		count=String.valueOf(dataPackage.getEatingCount());
		eatingText.setText(count);
		eatingText.setVisibility(View.VISIBLE);
		

		
	}
	private void prepareUI() {
		// TODO Auto-generated method stub
		/**
		 * title
		 */
		TextView title = (TextView) findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("餐台");
		/**
		 * "返回"Button
		 */
		TextView left = (TextView) findViewById(R.id.textView_left);
		left.setText("返回");
		left.setVisibility(View.VISIBLE);

		ImageButton back = (ImageButton) findViewById(R.id.btn_left);
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		/**
		 * refresh button
		 */
		ImageButton refresh = (ImageButton) findViewById(R.id.btn2_right);
		refresh.setVisibility(View.VISIBLE);
		refresh.setOnClickListener(new View.OnClickListener() {
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

		ImageButton all = (ImageButton) findViewById(R.id.btn_right);
		all.setImageResource(R.drawable.home_selector);
		all.setLayoutParams(lp);
		all.setVisibility(View.VISIBLE);
		all.setOnClickListener(new AOnClickListener(AOnClickListener.ALL));
		
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
					Table[] _tableSource = WirelessOrder.tables == null ? new Table[0]
							: WirelessOrder.tables;
					String text = s.toString().trim();
					for(Table t:_tableSource)
					{

						if(String.valueOf(t.aliasID).startsWith(text)||t.name.contains(text))
						{
							HashMap<String, Object> map = new HashMap<String, Object>();
							map.put("id", t.aliasID);
							map.put("custonNum", "人数: " + t.custom_num);
							map.put("tableName", t.name);
							String st= t.status==0? "空闲":"就餐";
							map.put("state","状态： "+st);
							list.add(map);
						}

					}

				}
				else {
					list = dataPackage.getAll();
				}
				adapter = new SimpleAdapter(ctx, list,
						R.layout.the_table, new String[] {
								"id", "custonNum", "state",
								"tableName" }, new int[] {
								R.id.text1_table,
								R.id.text2_table,
								R.id.text3_table,
								R.id.text4_table });
				lv.setAdapter(adapter);
				
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
		
		allText = (TextView)findViewById(R.id.left_txt_bottom);
		
		
		/**
		 *  空闲 按钮
		 */
		ImageButton freeBtn = (ImageButton)findViewById(R.id.middle_btn_bottom);
		freeBtn.setOnClickListener(new AOnClickListener(AOnClickListener.FREE));
		freeText = (TextView)findViewById(R.id.middle_txt_bottom);
		
		/**
		 * 就餐列表 按钮
		 */
		ImageButton eatingBtn = (ImageButton)findViewById(R.id.right_btn_bottom);
		eatingBtn.setOnClickListener(new AOnClickListener(AOnClickListener.EATING));
		
		eatingText = (TextView)findViewById(R.id.right_txt_bottom);
		

	}
	/**
	 * 装配数据
	 */
	private void init() {
		// TODO Auto-generated method stub
		lv = (PullListView) findViewById(R.id.listView_table);

		dataPackage = new DataPackage();
		List<Map<String, ?>> list = dataPackage.getAll();

		adapter = new SimpleAdapter(this, list,
				R.layout.the_table,
				new String[] { "id", "custonNum", "state",
						"tableName" },
				new int[] { R.id.text1_table,
						R.id.text2_table, R.id.text3_table,
						R.id.text4_table });
		lv.setAdapter(adapter);

		lv.setOnRefreshListener(new aRefreshListener());

	}
	
	private class AOnClickListener implements OnClickListener{
		public static final int ALL = 0;
		public static final int FREE = 1;
		public static final int EATING =2;
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
			case 0: list = dataPackage.getAll();
				buttonUp();
				((ImageButton)findViewById(R.id.left_btn_bottom)).setImageResource(R.drawable.alldown);
				break;
			case 1: list = dataPackage.getFree();
				buttonUp();
				((ImageButton)findViewById(R.id.middle_btn_bottom)).setImageResource(R.drawable.freedown);
				break;
			case 2: list = dataPackage.getEating();
				buttonUp();
				((ImageButton)findViewById(R.id.right_btn_bottom)).setImageResource(R.drawable.eatingdown);
				break;
			}
			
			adapter = new SimpleAdapter(ctx, list,
					R.layout.the_table, new String[] {
							"id", "custonNum", "state",
							"tableName" }, new int[] {
							R.id.text1_table,
							R.id.text2_table,
							R.id.text3_table,
							R.id.text4_table });
			lv.setAdapter(adapter);
			
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
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 设置更新list的方法

			new QueryRegionTask().execute();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			dataPackage = new DataPackage();
			List<Map<String, ?>> list = dataPackage
					.getAll();
			adapter = new SimpleAdapter(ctx, list,
					R.layout.the_table,
					new String[] { "id",
							"custonNum", "state",
							"tableName" },
					new int[] { R.id.text1_table,
							R.id.text2_table,
							R.id.text3_table,
							R.id.text4_table });
			lv.setAdapter(adapter);
			lv.onRefreshComplete();
			((ImageButton)findViewById(R.id.left_btn_bottom)).setImageResource(R.drawable.alldown);
			((ImageButton)findViewById(R.id.middle_btn_bottom)).setImageResource(R.drawable.free);
			((ImageButton)findViewById(R.id.right_btn_bottom)).setImageResource(R.drawable.eating);
		}
		
	}
	
	private class aRefreshListener implements
			OnRefreshListener {

		@Override
		public void onRefresh() {
			// TODO Auto-generated method stub
			new RefreshTask().execute();
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
				map.put("id", t.aliasID);
				map.put("custonNum", "人数: " + t.custom_num);
				map.put("tableName", t.name);

				if (t.status == 0) {
					String st = "空闲";
					map.put("state", "状态： " + st);
					free.add(map);
				} else {
					String st = "就餐";
					map.put("state", "状态： " + st);
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
