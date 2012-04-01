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
				// �л��ײ���ť��ʾ
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

	private EditText _inputTableId; // �����̨�ſ�

	private Button _backBtn; // �˳�Ӧ�ó���ť
	private Button _logoutBtn; // ע����ť
	private Button _refurbishBtn; // ˢ�°�ť
	private Button _bottomFirstBtn; // �ײ���һ����ť( ȫ����ȡ��)
	private Button _clearTablenum; // ���������̨�����ݰ�ť

	private TextView _usernameTv; // ��ǰ�û���
	private TextView _tablecountSum; // ��̨����������
	private TextView _tablecountIdle; // ��̨�������У�
	private TextView _tablecountBusy; // ��̨�����Ͳͣ�
	private TextView _regionsInfo; // ��ʾ��ǰ������
	private TextView _tableStatus; // ��ʾ��ǰ�Ĳ�̨״̬

	private MarqueeText _notice; // ������ʾ�ı�

	private FrameLayout _showPopWindow; // �������ѡ������
	private FrameLayout _inputTableNumarea; // �����̨����������
	private ViewFlipper _viewFlipper; // ��̨��ʾ����
	private LinearLayout _pagePoint; // ҳ��ָʾ��

	private PopupWindow _popWindow; // ������
	private View _popView; // ���������

	private LinearLayout _serchTable; // ���Ҳ�̨�ŵĽ��
	private Table _onClickTable; // ���浱ǰ����Ĳ�̨����

	private List<BaseAdapter> adapterList = new ArrayList<BaseAdapter>(); // ���ÿҳ��Adapter
	private List<ImageView> _pagePointList = new ArrayList<ImageView>(); // ���ҳ��ָʾ�����
	private List<Button> _regionsList = new ArrayList<Button>(); // �������(0-9)��ť;

	private Table[] _tempResultTables; // ����ɸѡ�����Ľ��

	private Map<String, Short> _regionNameToregionID = new HashMap<String, Short>(); // ���������--����ID

	private float mStartX, mEndX; // �����л�ҳ��ʱ��¼��X����ֵ
	private boolean _touchFlag = true; // ����ʱ��ֹ��������

	// ɸѡ ��¼����
	private short _regionConfine = -1; // ��������
	private String _tableNumConfine = ""; // ��̨������
	private byte _tableStatusConfine = -1; // ��̨״̬����

	// �ײ�״̬��� true���� false����
	private boolean _bottomFlag = true;

	// �������ĸı�
	private MyHandler myhandler = new MyHandler();

	private void init() {
		// ��ӡ��Ϣ
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
		// ���ز�̨��Ϣ
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
		// ������ť��Ӽ���
		for (Button b : _regionsList) {
			b.setOnClickListener(rbl);
		}

		// ���ع�����Ϣ
		if (WirelessOrder.restaurant.info != null) {
			_notice.setText(WirelessOrder.restaurant.info.replaceAll("\n", ""));
		} else {
			_notice.setText("");
		}
		// ����������Ϣ
		loadRegions();
		// ���ز�̨״̬ͳ����Ϣ
		loadTableStatus();
	}

	/*
	 * ���ز�̨ͳ����Ϣ
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
	 * ����������Ϣ
	 */
	private void loadRegions() {
		for (int i = 0; i < WirelessOrder.regions.length; i++) {
			// ��������ť�ı�
			_regionsList.get(i).setText(WirelessOrder.regions[i].name);
			// ������������ID����Map��
			_regionNameToregionID.put(WirelessOrder.regions[i].name,
					WirelessOrder.regions[i].regionID);
		}
	}

	/*
	 * ���ز�̨��Ϣ
	 */
	private void loadTable(Table[] tablesArray) {
		int tablesum = tablesArray.length;
		int pageSize = tablesum / 24;
		if (tablesum % 24 != 0)
			pageSize += 1;

		// �����̨����
		_viewFlipper.removeAllViews();
		adapterList.clear();
		// ��ʼ����̨gridview
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
		// ��ʼ��ҳ��ָʾ��
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
					// ��ɸѡ��̨״̬��ť��Ӽ���
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
		case R.id.bottomFirstBtn: // �ײ���һ����ť��ȡ����ȫ����

			if (_bottomFlag) {
				if (_regionConfine != -1) {
					_regionConfine = -1;
					_regionsInfo.setText("ȫ������");
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
				_tableStatus.setText("(ȫ��)");
				_popWindow.dismiss();
				selectorStatus();
			}

			break;
		case R.id.statusBusy:
			if (_tableStatusConfine != Table.TABLE_BUSY) {
				_tableStatusConfine = Table.TABLE_BUSY;
				_tableStatus.setText("(�Ͳ�)");
				_popWindow.dismiss();
				selectorStatus();
			}
			break;
		case R.id.statusNobody:
			if (_tableStatusConfine != Table.TABLE_IDLE) {
				_tableStatusConfine = Table.TABLE_IDLE;
				_tableStatus.setText("(����)");
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
	 * ��ȡ��ָ��λ�ÿ�ʼ��24�Ų�̨��ɵ�List
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
	 * ����ҳ��ָʾ��
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
			Toast.makeText(this, "�����", 100).show();
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
	 * ר�ż�������ť����
	 * 
	 * @author Administrator
	 * 
	 */
	class RegionsButtonListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (_bottomFlag) {
				// �ײ���ʾ����ť
				short clickRegion = _regionNameToregionID.get(((Button) v)
						.getText().toString());
				if (clickRegion != _regionConfine) {
					_regionsInfo.setText(((Button) v).getText().toString());
					_regionConfine = clickRegion;
					selectorRegion();
				}
			} else {
				// �ײ���ʾ���ְ�ť
				String s1 = ((Button) v).getText().toString();
				_inputTableId.append(s1);

			}
		}
	}

	/**
	 * ɸѡ���������Ĳ�̨��ʾ
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
	 * ɸѡ��̨��״̬
	 */
	private void selectorStatus() {
		Table[] st;
		if (_regionConfine != -1) {
			// �ѽ�������ɸѡ
			st = _tempResultTables;
		} else {
			// δ��������ɸѡ
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
	 * �л��ײ���ť
	 */
	private void changeBottonBtn() {
		if (_bottomFlag) {
			for (int i = 0; i < _regionsList.size(); i++) {
				_regionsList.get(i).setText("" + i);
			}
			_bottomFirstBtn.setText("ȡ��");
		} else {
			loadRegions();
			_bottomFirstBtn.setText("ȫ��");
		}
		_bottomFlag = !_bottomFlag;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case EXIT_APP:
			AlertDialog alertDialog = new AlertDialog.Builder(this)
					.setTitle("��ʾ��Ϣ")
					.setMessage("���Ƿ�ȷ���˳�e��ͨ?")
					.setPositiveButton("ȷ��",
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
					.setNegativeButton("ȡ��",
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
			pd.setMessage("���ڼ���...���Ժ�.");
			return pd;
		case BUSYTABLE:
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setTitle("��ѡ��" + _onClickTable.aliasID + "�Ų�̨�Ĳ���");
			b.setItems(new String[] { "�ĵ�", "ת̨" }, null);
			b.setNegativeButton("����", null);
			b.show();
			break;
		case IDLETABLE:
			Builder b2 = new Builder(this);
			b2.setTitle("��ѡ��" + _onClickTable.aliasID + "�Ų�̨�Ĳ���");
			b2.setItems(new String[] { "�µ�" }, null);
			b2.setNegativeButton("����", null);
			b2.show();
			break;
		}
		return super.onCreateDialog(id);
	}

	/*
	 * ˢ�²�̨��Ϣ���첽����
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
	 * ��ʾ��һҳ
	 */
	private void showPreviousPage() {
		_viewFlipper.showPrevious();
	}

	/*
	 * ��ʾ��һҳ
	 */
	private void showNextPage() {
		_viewFlipper.showNext();
	}

}
