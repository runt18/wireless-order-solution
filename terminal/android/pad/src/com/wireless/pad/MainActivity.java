package com.wireless.pad;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
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
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.wireless.adapter.TableAdapter;
import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.parcel.OrderParcel;
import com.wireless.parcel.TableParcel;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.ReqQueryOrder;
import com.wireless.protocol.ReqQueryTable;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;
import com.wireless.view.MarqueeText;

public class MainActivity extends Activity implements OnClickListener,
		OnTouchListener {

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
			case REDRAW_FOOD_MENU:
				if (WirelessOrder.foodMenu == null) {
					((TextView) findViewById(R.id.username_txt)).setText("");
					((TextView) findViewById(R.id.notice)).setText("");
				}
				break;

			case REDRAW_RESTAURANT:
				if (WirelessOrder.restaurant != null) {
					TextView billBoard = (TextView) findViewById(R.id.notice);
					if (WirelessOrder.restaurant.info != null) {
						billBoard.setText(WirelessOrder.restaurant.info
								.replaceAll("\n", ""));
					} else {
						billBoard.setText("");
					}

					TextView userName = (TextView) findViewById(R.id.username_value);
					if (_staff != null) {
						if (_staff.name != null) {
							userName.setText(WirelessOrder.restaurant.name
									+ "(" + _staff.name + ")");
						} else {
							userName.setText(WirelessOrder.restaurant.name);
						}
					} else {
						userName.setText(WirelessOrder.restaurant.name);
					}
				}
				break;

			}
		}
	}

	private final static short EXIT_APP = 0x000a;
	private final static short LOADDIALOG = 0x000b;
	private final static short BUSYTABLE = 0x000c;
	private final static short IDLETABLE = 0x000d;
	private static final int REDRAW_FOOD_MENU = 0x000f;

	private StaffTerminal _staff;

	private static final int DIALOG_STAFF_LOGIN = 4;
	private static final int REDRAW_RESTAURANT = 2;

	private EditText _inputTableId; // �����̨�ſ�

	private Button _backBtn; // �˳�Ӧ�ó���ť
	private Button _logoutBtn; // ע����ť
	private Button _refurbishBtn; // ˢ�°�ť
	private Button _bottomFirstBtn; // �ײ���һ����ť( ȫ����ȡ��)
	private Button _clearTablenum; // ���������̨�����ݰ�ť
	
	private Button _allRefresh ;	//ȫ��ˢ��

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
	private byte _tableStatusConfine = -1; // ��̨״̬����

	// �ײ�״̬��� true���� false����
	private boolean _bottomFlag = true;

	// �������ĸı�
	private MyHandler myhandler = new MyHandler();

	private void init() {
		_allRefresh = (Button)findViewById(R.id.reAll_btn);
		_allRefresh.setOnClickListener(this);
		_logoutBtn = (Button)findViewById(R.id.logon_btn);
		_logoutBtn.setOnClickListener(this);
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
				int id = 0;
				try {
					id = Integer.parseInt(s.toString().trim());
				} catch (Exception e) {
					return;
				}
				if (_tempResultTables == null)
					_tempResultTables = WirelessOrder.tables;
				int index = -1;
				for (int i = 0; i < _tempResultTables.length; i++) {
					if (_tempResultTables[i].aliasID == id) {
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

		// ������½�Ի���(��½����)
		// if (false) {
		if (WirelessOrder.staffs != null) {
			SharedPreferences sharedPreferences = getSharedPreferences(
					Params.PREFS_NAME, Context.MODE_PRIVATE);
			long pin = sharedPreferences.getLong(Params.STAFF_PIN,
					Params.DEF_STAFF_PIN);
			if (pin == Params.DEF_STAFF_PIN) {
				/**
				 * Show the login dialog if logout before.
				 */
				showDialog(DIALOG_STAFF_LOGIN);
			} else {
				/**
				 * Directly login with the previous staff account if user does
				 * NOT logout before. Otherwise show the login dialog.
				 */
				_staff = null;
				for (int i = 0; i < WirelessOrder.staffs.length; i++) {
					if (WirelessOrder.staffs[i].pin == pin) {
						_staff = WirelessOrder.staffs[i];
					}
				}
				if (_staff != null) {
					ReqPackage.setGen(new PinGen() {
						@Override
						public long getDeviceId() {
							return _staff.pin;
						}

						@Override
						public short getDeviceType() {
							return Terminal.MODEL_STAFF;
						}
					});
				} else {
					showDialog(DIALOG_STAFF_LOGIN);
				}
			}

			myhandler.sendEmptyMessage(REDRAW_RESTAURANT);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.main_menu, menu);// ָ��ʹ�õ�XML
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int item_id = item.getItemId();
		switch (item_id) {
		// �����ת����ҳ��
		case R.id.menu_set:
			Intent intent = new Intent(this, WebSettingActivity.class);
			startActivity(intent);
			// finish();
			break;
		// ������׸�����
		case R.id.menu_update:
			//new QueryMenuTask().execute();
			break;

		}
		return true;

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
			//grid.setOnItemClickListener(this);
			lp.gravity = Gravity.CENTER;
			grid.setNumColumns(6);
			grid.setLayoutParams(lp);
			grid.setSelector(android.R.color.transparent);
			TableAdapter tableAdapter = new TableAdapter(getTables(i * 24, tablesArray), 
				new TableAdapter.OnTableClickListener() {				
					@Override
					public void onClick(Table table) {
						if(table.status == Table.TABLE_IDLE){
							//jump to the order activity with the table parcel
							Intent intent = new Intent(MainActivity.this, OrderActivity.class);
							Bundle bundle = new Bundle();
							bundle.putParcelable(TableParcel.KEY_VALUE, new TableParcel(table));
							intent.putExtras(bundle);
							startActivity(intent);
							
						}else if(table.status == Table.TABLE_BUSY){
							//TODO jump to change order activity with the order parcel
							new QueryOrderTask(table.aliasID, Type.UPDATE_ORDER).execute();
						}
					}
				});
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
		
		case R.id.reAll_btn:
			_regionConfine = -1;
			_tableStatusConfine = -1;
			_inputTableId.setText("");
			_regionsInfo.setText("ȫ������");
			_tableStatus.setText("(ȫ��)");
			_tempResultTables = WirelessOrder.tables;
			myhandler.sendEmptyMessage(0);
			break;
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
				// _popWindow = new PopupWindow(_popView,
				// LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT,
				// true);
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
					_inputTableId.setText("");
					_tempResultTables = WirelessOrder.tables;
					selectorTable();
				}
			} else {
				_inputTableId.setFocusable(false);
				myhandler.sendEmptyMessage(9);
			}
			break;
		case R.id.statusAll:

			if (_tableStatusConfine != -1) {
				_tableStatusConfine = -1;
				_tableStatus.setText("(ȫ��)");
				_popWindow.dismiss();
				selectorTable();
			}

			break;
		case R.id.statusBusy:
			if (_tableStatusConfine != Table.TABLE_BUSY) {
				_tableStatusConfine = Table.TABLE_BUSY;
				_tableStatus.setText("(�Ͳ�)");
				_popWindow.dismiss();
				selectorTable();
			}
			break;
		case R.id.statusNobody:
			if (_tableStatusConfine != Table.TABLE_IDLE) {
				_tableStatusConfine = Table.TABLE_IDLE;
				_tableStatus.setText("(����)");
				_popWindow.dismiss();
				selectorTable();
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
		case R.id.logon_btn:
			showDialog(DIALOG_STAFF_LOGIN);
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

//	@Override
//	public void onItemClick(AdapterView<?> parent, View view, int position,
//			long id) {
//		if (_touchFlag) {
//			Table[] t;
//			if (_regionConfine != -1 || _tableStatusConfine != -1) {
//				t = _tempResultTables;
//			} else {
//				t = WirelessOrder.tables;
//			}
//			int currentPage = _viewFlipper.getDisplayedChild();
//
//			Table table = t[(currentPage * 24) + position];
//			_onClickTable = table;
//			if (table.status == Table.TABLE_BUSY) {
//				showDialog(BUSYTABLE);
//			} else if (table.status == Table.TABLE_IDLE) {
//				showDialog(IDLETABLE);
//			}
//		}
//
//	}

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
					selectorTable();
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
	// private void selectorRegion() {
	// _tempResultTables = null;
	// List<Table> tmpTables = new ArrayList<Table>();
	// for (int i = 0; i < WirelessOrder.tables.length; i++) {
	// if (WirelessOrder.tables[i].regionID == _regionConfine) {
	// tmpTables.add(WirelessOrder.tables[i]);
	// }
	// }
	// _tempResultTables = new Table[tmpTables.size()];
	// tmpTables.toArray(_tempResultTables);
	// selectorStatus();
	// }

	/**
	 * ɸѡ��̨��״̬
	 */
	// private void selectorStatus() {
	// Table[] st;
	// if (_regionConfine != -1) {
	// // �ѽ�������ɸѡ
	// st = _tempResultTables;
	// } else {
	// // δ��������ɸѡ
	// st = WirelessOrder.tables;
	// }
	// if (_tableStatusConfine == -1) {
	// Message msg = new Message();
	// msg.what = 3;
	// msg.obj = st;
	// myhandler.sendMessage(msg);
	// } else if (_tableStatusConfine == Table.TABLE_BUSY) {
	// List<Table> lt = new ArrayList<Table>();
	// for (int i = 0; i < st.length; i++) {
	// if (st[i].status == Table.TABLE_BUSY) {
	// lt.add(st[i]);
	// }
	// }
	// st = new Table[lt.size()];
	// lt.toArray(st);
	// Message msg = new Message();
	// msg.what = 3;
	// msg.obj = st;
	// myhandler.sendMessage(msg);
	// } else if (_tableStatusConfine == Table.TABLE_IDLE) {
	// List<Table> lt = new ArrayList<Table>();
	// for (int i = 0; i < st.length; i++) {
	// if (st[i].status == Table.TABLE_IDLE) {
	// lt.add(st[i]);
	// }
	// }
	// st = new Table[lt.size()];
	// lt.toArray(st);
	// Message msg = new Message();
	// msg.what = 3;
	// msg.obj = st;
	// myhandler.sendMessage(msg);
	// }
	// }

	/**
	 * ͳһɸѡ�ķ�����ɸѡ״̬������
	 */
	private void selectorTable() {
		_tempResultTables = null;
		List<Table> tmpTables = new ArrayList<Table>();
		for (int i = 0; i < WirelessOrder.tables.length; i++) {
			// �˶� ��������
			if (!(WirelessOrder.tables[i].regionID == _regionConfine || _regionConfine == -1)) {
				continue;
			}
			// �˶�״̬����
			if (!(WirelessOrder.tables[i].status == _tableStatusConfine || _tableStatusConfine == -1)) {
				continue;
			}
			tmpTables.add(WirelessOrder.tables[i]);
		}
		_tempResultTables = new Table[tmpTables.size()];
		tmpTables.toArray(_tempResultTables);
		Message msg = new Message();
		msg.what = 0;
		myhandler.sendMessage(msg);
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

		case DIALOG_STAFF_LOGIN:
			return new AskLoginDialog();
		}
		return super.onCreateDialog(id);
	}

	/*
	 * ˢ�²�̨��Ϣ���첽����
	 */
	class QueryTableTask extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
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
		_viewFlipper.setOutAnimation(this, R.anim.ad_previous_out);
		_viewFlipper.setInAnimation(this, R.anim.ad_previous_in);
		_viewFlipper.showPrevious();
	}

	/*
	 * ��ʾ��һҳ
	 */
	private void showNextPage() {
		_viewFlipper.setOutAnimation(this, R.anim.ad_next_out);
		_viewFlipper.setInAnimation(this, R.anim.ad_next_in);
		_viewFlipper.showNext();
	}

	// ��¼��Dialog
	public class AskLoginDialog extends Dialog {

		private PopupWindow _popupWindow;
		private BaseAdapter _staffAdapter;

		AskLoginDialog() {
			super(MainActivity.this, 0);
			setContentView(R.layout.login_dialog);
			getWindow().getAttributes().width = (int) (getWindow()
					.getWindowManager().getDefaultDisplay().getWidth() * 0.55);
			getWindow().getAttributes().height = (int) (getWindow()
					.getWindowManager().getDefaultDisplay().getHeight() * 0.35);
			setTitle("�������˺�������");
			final EditText pwdEdtTxt = (EditText) findViewById(R.id.pwd);
			final EditText staffTxtView = (EditText) findViewById(R.id.staffname);

			/**
			 * �ʺ��������ʾԱ���б�
			 */
			staffTxtView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (_popupWindow.isShowing()) {
						_popupWindow.dismiss();
					} else {
						_popupWindow.showAsDropDown(
								findViewById(R.id.staffname), 0, -7);
					}
				}
			});

			// ��ȡ�Զ��岼���ļ�����ͼ
			View popupWndView = getLayoutInflater().inflate(
					R.layout.loginpopuwindow, null, false);
			// ����PopupWindowʵ��
			_popupWindow = new PopupWindow(popupWndView, 320, 200, true);
			_popupWindow.setOutsideTouchable(true);
			_popupWindow.setBackgroundDrawable(new BitmapDrawable());
			_popupWindow.update();
			ListView staffLstView = (ListView) popupWndView
					.findViewById(R.id.loginpopuwindow);
			_staffAdapter = new StaffsAdapter();
			staffLstView.setAdapter(_staffAdapter);

			/**
			 * �������б����ѡ��Ա����Ϣ�Ĳ���
			 */
			staffLstView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					_staff = WirelessOrder.staffs[position];
					staffTxtView.setText(_staff.name);
					_popupWindow.dismiss();
				}
			});

			/**
			 * ��¼Button�ĵ������
			 */
			((Button) findViewById(R.id.login))
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {

							TextView errTxtView = (TextView) findViewById(R.id.error);

							try {
								// Convert the password into MD5
								MessageDigest digester = MessageDigest
										.getInstance("MD5");
								digester.update(pwdEdtTxt.getText().toString()
										.getBytes(), 0, pwdEdtTxt.getText()
										.toString().getBytes().length);

								if (staffTxtView.getText().toString()
										.equals("")) {
									errTxtView.setText("�˺Ų���Ϊ��");

								} else if (_staff.pwd
										.equals(toHexString(digester.digest()))) {
									// ����staff pin���ļ�����
									Editor editor = getSharedPreferences(
											Params.PREFS_NAME,
											Context.MODE_PRIVATE).edit();// ��ȡ�༭��
									editor.putLong(Params.STAFF_PIN, _staff.pin);
									// �ύ�޸�
									editor.commit();
									myhandler
											.sendEmptyMessage(REDRAW_RESTAURANT);
									// set the pin generator according to the
									// staff login
									ReqPackage.setGen(new PinGen() {
										@Override
										public long getDeviceId() {
											return _staff.pin;
										}

										@Override
										public short getDeviceType() {
											return Terminal.MODEL_STAFF;
										}

									});
									dismiss();

								} else {
									errTxtView.setText("�������");
								}

							} catch (NoSuchAlgorithmException e) {
								errTxtView.setText(e.getMessage());
								;
							}
						}
					});

		}

		@Override
		public void onAttachedToWindow() {
			((TextView) findViewById(R.id.error)).setText("");
			((EditText) findViewById(R.id.pwd)).setText("");
			((TextView) findViewById(R.id.staffname)).setText("");
			if (_staffAdapter != null) {
				_staffAdapter.notifyDataSetChanged();
			}
		}

		/**
		 * Convert the md5 byte to hex string.
		 * 
		 * @param md5Msg
		 *            the md5 byte value
		 * @return the hex string to this md5 byte value
		 */
		private String toHexString(byte[] md5Msg) {
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < md5Msg.length; i++) {
				if (md5Msg[i] >= 0x00 && md5Msg[i] < 0x10) {
					hexString.append("0").append(
							Integer.toHexString(0xFF & md5Msg[i]));
				} else {
					hexString.append(Integer.toHexString(0xFF & md5Msg[i]));
				}
			}
			return hexString.toString();
		}

		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				finish();
			}
			return super.onKeyDown(keyCode, event);
		}

		/**
		 * Ա����Ϣ�������Adapter
		 */
		private class StaffsAdapter extends BaseAdapter {

			public StaffsAdapter() {

			}

			@Override
			public int getCount() {
				return WirelessOrder.staffs.length;
			}

			@Override
			public Object getItem(int position) {
				return null;
			}

			@Override
			public long getItemId(int position) {
				return 0;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
					convertView = LayoutInflater.from(MainActivity.this)
							.inflate(R.layout.orderpopuwindowitem, null);
					((TextView) convertView
							.findViewById(R.id.popuwindowfoodname))
							.setText(WirelessOrder.staffs[position].name);
				} else {
					((TextView) convertView
							.findViewById(R.id.popuwindowfoodname))
							.setText(WirelessOrder.staffs[position].name);
				}
				return convertView;
			}

		}
	}

	/**
	 * ִ�������Ӧ��̨���˵���Ϣ 
	 */
	private class QueryOrderTask extends AsyncTask<Void, Void, String>{

		private ProgressDialog _progDialog;
		private int _tableID;
		private Order _order;
		private int _type = Type.UPDATE_ORDER;;
		
		QueryOrderTask(int tableID, int type){
			_tableID = tableID;
			_type = type;
		}
		
		/**
		 * ��ִ������ɾ������ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(MainActivity.this, "", "��ѯ" + _tableID + "�Ų�̨����Ϣ...���Ժ�", true);
		}
		
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			try{
				//����tableID��������
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryOrder(_tableID));
				if(resp.header.type == Type.ACK){
					_order = RespParser.parseQueryOrder(resp, WirelessOrder.foodMenu);
					
				}else{
    				if(resp.header.reserved == ErrorCode.TABLE_IDLE) {
    					errMsg = _tableID + "��̨��δ�µ�";
    					
    				}else if(resp.header.reserved == ErrorCode.TABLE_NOT_EXIST) {
    					errMsg = _tableID + "��̨��Ϣ������";

    				}else if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
    					errMsg = "�ն�û�еǼǵ�����������ϵ������Ա��";

    				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
    					errMsg = "�ն��ѹ��ڣ�����ϵ������Ա��";

    				}else{
    					errMsg = "δȷ�����쳣����(" + resp.header.reserved + ")";
    				}
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			
			return errMsg;
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�����Ǩ�Ƶ��ĵ�ҳ��
		 */
		@Override
		protected void onPostExecute(String errMsg){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if(errMsg != null){
				new AlertDialog.Builder(MainActivity.this)
				.setTitle("��ʾ")
				.setMessage(errMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				if(_type == Type.UPDATE_ORDER){
					//jump to the update order activity
					Intent intent = new Intent(MainActivity.this, ChgOrderActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(_order));
					intent.putExtras(bundle);
					startActivity(intent);
					
				}else if(_type == Type.PAY_ORDER){
					//jump to the pay order activity
//					Intent intent = new Intent(MainActivity.this, BillActivity.class);
//					Bundle bundle = new Bundle();
//					bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(_order));
//					intent.putExtras(bundle);
//					startActivity(intent);
				}
			}
		}
		
	}

}
