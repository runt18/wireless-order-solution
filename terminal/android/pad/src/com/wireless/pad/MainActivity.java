package com.wireless.pad;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.wireless.adapter.TableAdapter;
import com.wireless.common.WirelessOrder;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqQueryTable;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Table;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;
import com.wireless.view.MarqueeText;

public class MainActivity extends Activity implements OnClickListener,
		OnTouchListener, OnItemClickListener {

	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				loadTable(_tempResultTables);
				break;
			case 1:
				loadTable(WirelessOrder.tables);
				break;
			case 3:
				loadTable((Table[]) msg.obj);
				break;
			case 9:
				// 切换底部按钮显示
				changeBottonBtn();
				break;
			case 11:
				_serchTable.setBackgroundResource(R.drawable.av_r19_c17);
				UpdatePagePoint();
				break;
			}
		}
	}

	private final static short EXIT_APP = 0x000a;
	private final static short LOADDIALOG = 0x000b;
	private final static short BUSYTABLE = 0x000c;
	private final static short IDLETABLE = 0x000d;

	private EditText _inputTableId; // 输入餐台号框

	private Button _backBtn; // 退出应用程序按钮
	private Button _logoutBtn; // 注销按钮
	private Button _refurbishBtn; // 刷新按钮
	private Button _bottomFirstBtn; // 底部第一个按钮( 全部，取消)
	private Button _clearTablenum; // 清楚搜索餐台号内容按钮

	private TextView _usernameTv; // 当前用户名
	private TextView _tablecountSum; // 餐台数（总数）
	private TextView _tablecountIdle; // 餐台数（空闲）
	private TextView _tablecountBusy; // 餐台数（就餐）
	private TextView _regionsInfo; // 显示当前的区域
	private TextView _tableStatus; // 显示当前的餐台状态

	private MarqueeText _notice; // 公告显示文本

	private FrameLayout _showPopWindow; // 点击弹出选择区域
	private FrameLayout _inputTableNumarea; // 输入餐台号搜索区域
	private ViewFlipper _viewFlipper; // 餐台显示区域
	private LinearLayout _pagePoint; // 页码指示器

	private PopupWindow _popWindow; // 弹出框
	private View _popView; // 弹出框界面

	private LinearLayout _serchTable; // 查找餐台号的结果
	private Table _onClickTable; // 保存当前点击的餐台对象

	private List<BaseAdapter> adapterList = new ArrayList<BaseAdapter>(); // 存放每页的Adapter
	private List<ImageView> _pagePointList = new ArrayList<ImageView>(); // 存放页面指示器组件
	private List<Button> _regionsList = new ArrayList<Button>(); // 存放区域(0-9)按钮;

	private Table[] _tempResultTables; // 区域筛选出来的结果

	private Map<String, Short> _regionNameToregionID = new HashMap<String, Short>(); // 存放区域名--区域ID

	private float mStartX, mEndX; // 滑动切换页码时记录的X坐标值
	private boolean _touchFlag = true; // 滑动时防止误操作标记

	// 筛选 记录变量
	private short _regionConfine = -1; // 区域限制
	private String _tableNumConfine = ""; // 餐台号限制
	private byte _tableStatusConfine = -1; // 餐台状态限制

	// 底部状态标记 true区域 false数字
	private boolean _bottomFlag = true;

	// 处理界面的改变
	private MyHandler myhandler = new MyHandler();

	private void init() {
		// 打印信息
		// for(Table t : WirelessOrder.tables){
		// System.out.println(t.name + "fdfsds");
		// System.out.println(t.aliasID + "fdfsds");
		// }
		_notice = (MarqueeText) findViewById(R.id.notice);
		_refurbishBtn = (Button) findViewById(R.id.refurbish_btn);
		_refurbishBtn.setOnClickListener(this);
		_tablecountSum = (TextView) findViewById(R.id.tablecount_sum);
		_tablecountIdle = (TextView) findViewById(R.id.tablecount_idle);
		_tablecountBusy = (TextView) findViewById(R.id.tablecount_busy);
		_clearTablenum = (Button) findViewById(R.id.clearTableNum);
		_clearTablenum.setOnClickListener(this);
		_inputTableNumarea = (FrameLayout) findViewById(R.id.inputtableNumarea);
		_inputTableNumarea.setOnClickListener(this);
		_inputTableId = (EditText) findViewById(R.id.inputTableId);
		_inputTableId.setOnClickListener(this);
		_inputTableId.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (_serchTable != null)
					_serchTable
							.setBackgroundResource(R.drawable.griditem_bg_selector);
				short id = 0;
				try {
					id = Short.parseShort(s.toString().trim());
				} catch (Exception e) {
					return;
				}
				Table[] serachTable;
				if (_regionConfine != -1 || _tableStatusConfine != -1) {
					serachTable = _tempResultTables;
				} else {
					serachTable = WirelessOrder.tables;
				}
				int index = -1;
				for (int i = 0; i < serachTable.length; i++) {
					if (serachTable[i].aliasID == id) {
						index = i;
						break;
					}
				}
				if (index == -1)
					return;
				final int copyIndex = index;
				int pageIndex = index / 24;
				_viewFlipper.setDisplayedChild(pageIndex);
				new Thread() {
					public void run() {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						GridView gridview = (GridView) _viewFlipper
								.getCurrentView();
						View v = gridview.getChildAt(copyIndex % 24);
						_serchTable = (LinearLayout) v
								.findViewById(R.id.gridItemBg);
						myhandler.sendEmptyMessage(11);
					};
				}.start();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		_regionsInfo = (TextView) findViewById(R.id.regionsInfo_txt);
		_tableStatus = (TextView) findViewById(R.id.tablestatus_txt);
		_bottomFirstBtn = (Button) findViewById(R.id.bottomFirstBtn);
		_bottomFirstBtn.setOnClickListener(this);
		_backBtn = (Button) findViewById(R.id.back_btn);
		_backBtn.setOnClickListener(this);
		_showPopWindow = (FrameLayout) findViewById(R.id.showPopWindow);
		_showPopWindow.setOnClickListener(this);
		_viewFlipper = (ViewFlipper) findViewById(R.id.tableFlipper);
		_pagePoint = (LinearLayout) findViewById(R.id.page_point);
		// 加载餐台信息
		loadTable(WirelessOrder.tables);

		_regionsList.add((Button) findViewById(R.id.region_1));
		_regionsList.add((Button) findViewById(R.id.region_2));
		_regionsList.add((Button) findViewById(R.id.region_3));
		_regionsList.add((Button) findViewById(R.id.region_4));
		_regionsList.add((Button) findViewById(R.id.region_5));
		_regionsList.add((Button) findViewById(R.id.region_6));
		_regionsList.add((Button) findViewById(R.id.region_7));
		_regionsList.add((Button) findViewById(R.id.region_8));
		_regionsList.add((Button) findViewById(R.id.region_9));
		_regionsList.add((Button) findViewById(R.id.region_10));

		RegionsButtonListener rbl = new RegionsButtonListener();
		// 给区域按钮添加监听
		for (Button b : _regionsList) {
			b.setOnClickListener(rbl);
		}

		// 加载公告信息
		if (WirelessOrder.restaurant.info != null) {
			_notice.setText(WirelessOrder.restaurant.info.replaceAll("\n", ""));
		} else {
			_notice.setText("");
		}
		// 加载区域信息
		loadRegions();
		// 加载餐台状态统计信息
		loadTableStatus();
	}

	/*
	 * 加载餐台统计信息
	 */
	private void loadTableStatus() {
		int sum = 0;
		int idle = 0;
		int busy = 0;
		sum = WirelessOrder.tables.length;
		for (int i = 0; i < WirelessOrder.tables.length; i++) {
			if (WirelessOrder.tables[i].status == Table.TABLE_BUSY) {
				busy++;
			} else if (WirelessOrder.tables[i].status == Table.TABLE_IDLE) {
				idle++;
			}
		}
		_tablecountSum.setText("(" + sum + ")");
		_tablecountBusy.setText("(" + busy + ")");
		_tablecountIdle.setText("(" + idle + ")");
	}

	/*
	 * 加载区域信息
	 */
	private void loadRegions() {
		for (int i = 0; i < WirelessOrder.regions.length; i++) {
			// 设置区域按钮文本
			_regionsList.get(i).setText(WirelessOrder.regions[i].name);
			// 将区域与区域ID放入Map中
			_regionNameToregionID.put(WirelessOrder.regions[i].name,
					WirelessOrder.regions[i].regionID);
		}
	}

	/*
	 * 加载餐台信息
	 */
	private void loadTable(Table[] tablesArray) {
		int tablesum = tablesArray.length;
		int pageSize = tablesum / 24;
		if (tablesum % 24 != 0)
			pageSize += 1;

		// 清理餐台界面
		_viewFlipper.removeAllViews();
		adapterList.clear();
		// 初始化餐台gridview
		for (int i = 0; i < pageSize; i++) {
			GridView grid = new GridView(this);
			grid.setSelected(true);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.FILL_PARENT);
			grid.setOnTouchListener(this);
			grid.setOnItemClickListener(this);
			lp.gravity = Gravity.CENTER;
			grid.setNumColumns(6);
			grid.setLayoutParams(lp);
			grid.setSelector(android.R.color.transparent);
			TableAdapter tableAdapter = new TableAdapter(this, getTables(
					i * 24, tablesArray));
			grid.setAdapter(tableAdapter);
			adapterList.add(tableAdapter);
			_viewFlipper.addView(grid);
		}
		_pagePoint.removeAllViews();
		_pagePointList.clear();
		// 初始化页码指示器
		for (int i = 0; i < pageSize; i++) {
			ImageView point = new ImageView(this);
			point.setImageResource(R.drawable.av_r25_c31);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(0, 0, 25, 0);
			point.setLayoutParams(lp);
			_pagePoint.addView(point);
			_pagePointList.add(point);
		}
		UpdatePagePoint();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		init();
	}

	@Override
	public void onClick(View v) {
		int viewID = v.getId();
		switch (viewID) {
		case R.id.showPopWindow:
			if (_popWindow == null) {
				if (_popView == null) {
					_popView = this.getLayoutInflater().inflate(
							R.layout.main_pop_window, null);
					// 给筛选餐台状态按钮添加监听
					((Button) _popView.findViewById(R.id.statusAll))
							.setOnClickListener(this);
					((Button) _popView.findViewById(R.id.statusNobody))
							.setOnClickListener(this);
					((Button) _popView.findViewById(R.id.statusBusy))
							.setOnClickListener(this);
				}
				_popWindow = new PopupWindow(_popView,
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
						true);
				_popWindow.setOutsideTouchable(true);
				_popWindow.setBackgroundDrawable(new BitmapDrawable());
				_popWindow.update();
			}
			_popWindow.showAsDropDown(_showPopWindow);

			break;
		case R.id.back_btn:
			showDialog(EXIT_APP);
			break;
		case R.id.bottomFirstBtn: // 底部第一个按钮（取消，全部）

			if (_bottomFlag) {
				if (_regionConfine != -1) {
					_regionConfine = -1;
					_regionsInfo.setText("全部区域");
					_tempResultTables = WirelessOrder.tables;
					selectorRegion();
				}
			} else {
				// _inputTableId.clearFocus();
				_inputTableId.setFocusable(false);
				myhandler.sendEmptyMessage(9);
			}
			break;
		case R.id.statusAll:

			if (_tableStatusConfine != -1) {
				_tableStatusConfine = -1;
				_tableStatus.setText("(全部)");
				_popWindow.dismiss();
				selectorStatus();
			}

			break;
		case R.id.statusBusy:
			if (_tableStatusConfine != Table.TABLE_BUSY) {
				_tableStatusConfine = Table.TABLE_BUSY;
				_tableStatus.setText("(就餐)");
				_popWindow.dismiss();
				selectorStatus();
			}
			break;
		case R.id.statusNobody:
			if (_tableStatusConfine != Table.TABLE_IDLE) {
				_tableStatusConfine = Table.TABLE_IDLE;
				_tableStatus.setText("(空闲)");
				_popWindow.dismiss();
				selectorStatus();
			}
			break;
		case R.id.inputtableNumarea:
			if (_bottomFlag)
				myhandler.sendEmptyMessage(9);
			break;
		case R.id.inputTableId:
			if (_bottomFlag)
				myhandler.sendEmptyMessage(9);
			break;
		case R.id.clearTableNum:
			_inputTableId.setText("");
			break;
		case R.id.refurbish_btn:
			showDialog(LOADDIALOG);
			new QueryTableTask().execute();
			break;
		}
	}

	/**
	 * 获取从指定位置开始的24张餐台组成的List
	 * 
	 * @param startIndex
	 * @return
	 */
	private ArrayList<Table> getTables(int startIndex, Table[] tables) {
		ArrayList<Table> list = new ArrayList<Table>();
		for (int i = 0; i < 24; i++, startIndex++) {
			try {
				Table t = tables[startIndex];
				list.add(t);
			} catch (Exception e) {
				return list;
			}
		}
		return list;
	}

	/**
	 * 更新页码指示器
	 */
	private void UpdatePagePoint() {
		if (_viewFlipper.getChildCount() == 0)
			return;
		int currentIndex = _viewFlipper.getDisplayedChild();
		for (ImageView iv : _pagePointList) {
			iv.setImageResource(R.drawable.av_r25_c31);
		}
		_pagePointList.get(currentIndex)
				.setImageResource(R.drawable.av_r24_c28);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mStartX = event.getRawX();
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			mEndX = event.getRawX();
			if (mEndX - mStartX > 50) {
				_touchFlag = false;
				showPreviousPage();
				UpdatePagePoint();
			}
			if (mEndX - mStartX < -50) {
				_touchFlag = false;
				showNextPage();
				UpdatePagePoint();
			}
			if (Math.abs(mEndX - mStartX) <= 10) {
				_touchFlag = true;
			}
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (_touchFlag) {
			Toast.makeText(this, "点击啦", 100).show();
			Table[] t;
			if (_regionConfine != -1 || _tableStatusConfine != -1) {
				t = _tempResultTables;
			} else {
				t = WirelessOrder.tables;
			}
			int currentPage = _viewFlipper.getDisplayedChild();

			Table table = t[(currentPage * 24) + position];
			_onClickTable = table;
			if (table.status == Table.TABLE_BUSY) {
				showDialog(BUSYTABLE);
			} else if (table.status == Table.TABLE_IDLE) {
				showDialog(IDLETABLE);
			}
		}

	}

	/**
	 * 专门监听区域按钮的类
	 * 
	 * @author Administrator
	 * 
	 */
	class RegionsButtonListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (_bottomFlag) {
				// 底部显示区域按钮
				short clickRegion = _regionNameToregionID.get(((Button) v)
						.getText().toString());
				if (clickRegion != _regionConfine) {
					_regionsInfo.setText(((Button) v).getText().toString());
					_regionConfine = clickRegion;
					selectorRegion();
				}
			} else {
				// 底部显示数字按钮
				String s1 = ((Button) v).getText().toString();
				_inputTableId.append(s1);

			}
		}
	}

	/**
	 * 筛选符合条件的餐台显示
	 */
	private void selectorRegion() {
		_tempResultTables = null;
		List<Table> tmpTables = new ArrayList<Table>();
		for (int i = 0; i < WirelessOrder.tables.length; i++) {
			if (WirelessOrder.tables[i].regionID == _regionConfine) {
				tmpTables.add(WirelessOrder.tables[i]);
			}
		}
		_tempResultTables = new Table[tmpTables.size()];
		tmpTables.toArray(_tempResultTables);
		selectorStatus();
	}

	/**
	 * 筛选餐台的状态
	 */
	private void selectorStatus() {
		Table[] st;
		if (_regionConfine != -1) {
			// 已进行区域筛选
			st = _tempResultTables;
		} else {
			// 未进行区域筛选
			st = WirelessOrder.tables;
		}
		if (_tableStatusConfine == -1) {
			Message msg = new Message();
			msg.what = 3;
			msg.obj = st;
			myhandler.sendMessage(msg);
		} else if (_tableStatusConfine == Table.TABLE_BUSY) {
			List<Table> lt = new ArrayList<Table>();
			for (int i = 0; i < st.length; i++) {
				if (st[i].status == Table.TABLE_BUSY) {
					lt.add(st[i]);
				}
			}
			st = new Table[lt.size()];
			lt.toArray(st);
			Message msg = new Message();
			msg.what = 3;
			msg.obj = st;
			myhandler.sendMessage(msg);
		} else if (_tableStatusConfine == Table.TABLE_IDLE) {
			List<Table> lt = new ArrayList<Table>();
			for (int i = 0; i < st.length; i++) {
				if (st[i].status == Table.TABLE_IDLE) {
					lt.add(st[i]);
				}
			}
			st = new Table[lt.size()];
			lt.toArray(st);
			Message msg = new Message();
			msg.what = 3;
			msg.obj = st;
			myhandler.sendMessage(msg);
		}
	}

	/**
	 * 切换底部按钮
	 */
	private void changeBottonBtn() {
		if (_bottomFlag) {
			for (int i = 0; i < _regionsList.size(); i++) {
				_regionsList.get(i).setText("" + i);
			}
			_bottomFirstBtn.setText("取消");
		} else {
			loadRegions();
			_bottomFirstBtn.setText("全部");
		}
		_bottomFlag = !_bottomFlag;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case EXIT_APP:
			AlertDialog alertDialog = new AlertDialog.Builder(this)
					.setTitle("提示信息")
					.setMessage("您是否确定退出e点通?")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									android.os.Process
											.killProcess(android.os.Process
													.myPid());
									System.exit(0);
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									return;
								}
							}).create();
			return alertDialog;
		case LOADDIALOG:
			ProgressDialog pd;
			pd = new ProgressDialog(this);
			pd.setMessage("正在加载...请稍后.");
			return pd;
		case BUSYTABLE:
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setTitle("请选择" + _onClickTable.aliasID + "号餐台的操作");
			b.setItems(new String[] { "改单", "转台" }, null);
			b.setNegativeButton("返回", null);
			b.show();
			break;
		case IDLETABLE:
			Builder b2 = new Builder(this);
			b2.setTitle("请选择" + _onClickTable.aliasID + "号餐台的操作");
			b2.setItems(new String[] { "下单" }, null);
			b2.setNegativeButton("返回", null);
			b2.show();
			break;
		}
		return super.onCreateDialog(id);
	}

	/*
	 * 刷新餐台信息的异步任务
	 */
	class QueryTableTask extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showDialog(LOADDIALOG);
		}
		@Override
		protected String doInBackground(String... params) {
			String errMsg = null;
			try {
				WirelessOrder.tables = null;
				ProtocolPackage resp = ServerConnector.instance().ask(
						new ReqQueryTable());
				if (resp.header.type == Type.ACK) {
					WirelessOrder.tables = RespParser.parseQueryTable(resp);
				}
			} catch (IOException e) {
				errMsg = e.getMessage();
			}

			return errMsg;
		}

		@Override
		protected void onPostExecute(String result) {
			loadTableStatus();
			dismissDialog(LOADDIALOG);
			super.onPostExecute(result);
		}
	}

	/*
	 * 显示上一页
	 */
	private void showPreviousPage() {
		_viewFlipper.showPrevious();
	}

	/*
	 * 显示下一页
	 */
	private void showNextPage() {
		_viewFlipper.showNext();
	}

}
