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
import android.view.View;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table);
		ctx = this.getApplicationContext();
		prepareUI();

		init();

	}

	private void prepareUI() {
		// TODO Auto-generated method stub
		lv = (PullListView) findViewById(R.id.listView_table);

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

		ImageButton refresh = (ImageButton) findViewById(R.id.btn2_right);
		refresh.setVisibility(View.VISIBLE);

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
		all.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				List<Map<String, ?>> list = dataPackage
						.getAll();

				adapter = new SimpleAdapter(ctx, list,
						R.layout.the_table, new String[] {
								"id", "custonNum", "state",
								"tableName" }, new int[] {
								R.id.text1_table,
								R.id.text2_table,
								R.id.text3_table,
								R.id.text4_table });
				lv.setAdapter(adapter);
				list = dataPackage.getAll();
				adapter.notifyDataSetChanged();
			}
		});
	}

	private void init() {
		// TODO Auto-generated method stub

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

	private class aRefreshListener implements
			OnRefreshListener {

		@Override
		public void onRefresh() {
			// TODO Auto-generated method stub
			new AsyncTask<Void, Void, Void>() {
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
					// adapter.notifyDataSetChanged();
					lv.onRefreshComplete();
				}

			}.execute();
		}

	}

	private class DataPackage {
		private List<Map<String, ?>> all, free, eating;
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

		public void setAll(List<Map<String, ?>> all) {
			this.all = all;
		}

		public List<Map<String, ?>> getFree() {
			System.out.println(free);
			return free;
		}

		public void setFree(List<Map<String, ?>> free) {
			this.free = free;
		}

		public List<Map<String, ?>> getEating() {
			return eating;
		}

		public void setEating(List<Map<String, ?>> eating) {
			this.eating = eating;
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
