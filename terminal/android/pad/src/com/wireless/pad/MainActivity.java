package com.wireless.pad;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.View;
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
import android.widget.Toast;

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
import com.wireless.protocol.ReqQueryRegion;
import com.wireless.protocol.ReqQueryRestaurant;
import com.wireless.protocol.ReqQueryTable;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;
import com.wireless.view.ScrollLayout;
import com.wireless.view.ScrollLayout.PageListener;

public class MainActivity extends Activity {

	private Handler _handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == FILTER_TABLE_BY_COND) {
				ArrayList<Table> tmpTbls = new ArrayList<Table>(
						Arrays.asList(WirelessOrder.tables));
				Iterator<Table> iter = tmpTbls.iterator();
				while (iter.hasNext()) {
					Table table = iter.next();
					if (_curTblStatus >= 0) {
						if (table.status != _curTblStatus) {
							iter.remove();
							continue;
						}
					}
					if (_curRegion >= 0) {
						if (table.regionID != _curRegion) {
							iter.remove();
							continue;
						}
					}
				}
				_tableSource = tmpTbls.toArray(new Table[tmpTbls.size()]);
				reflashTableArea();
				reflashTableStat();

			} else if (msg.what == REDRAW_RESTAURANT) {
				reflashRestaurantInfo();
			}
		}
	};

	private final static int TABLE_AMOUNT_PER_PAGE = 24; // 每页要显示餐台数量

	private short _curTblStatus = Short.MIN_VALUE; // 当前要筛选的餐台状态，小于0表示全部状态
	private short _curRegion = Short.MIN_VALUE; // 当前要筛选的餐台区域，小于0表示全部区域

	private StaffTerminal _staff;

	private ScrollLayout _tableAreaFlipper; // 餐台显示区域

	private LinearLayout _pageIndicator; // 页码指示器

	private Table[] _tableSource; // 主界面中用于餐台显示的数据源

	private final static int DIALOG_EXIT_APP = 0;
	private final static int DIALOG_STAFF_LOGIN = 1;

	private final static int FILTER_TABLE_BY_COND = 0; //
	private final static int REDRAW_RESTAURANT = 2; //

	private float mStartX, mEndX; // 滑动切换页码时记录的X坐标值

	private void init() {

		_tableAreaFlipper = (ScrollLayout) findViewById(R.id.tableFlipper);
		_tableAreaFlipper.getLayoutParams().height = this.getWindowManager()
				.getDefaultDisplay().getHeight() * 2 / 3;
		_tableAreaFlipper.setPageListener(new PageListener() {

			@Override
			public void page(int page) {
				// TODO Auto-generated method stub
				reflashPageIndictor();
			}
		});

		// 刷新Button
		((Button) findViewById(R.id.refurbish_btn))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new QueryRestaurantTask().execute();
					}
				});

		// 创建点击餐台状态后弹出区域的View
		final View tblStatusPopupView = getLayoutInflater().inflate(
				R.layout.main_pop_window, null);
		// 创建与这个View关联的pop-up window
		final PopupWindow popWnd = new PopupWindow(tblStatusPopupView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		popWnd.setOutsideTouchable(true);
		popWnd.setBackgroundDrawable(new BitmapDrawable());
		popWnd.update();

		// 餐台状态弹出View中的“全部”Button
		((Button) tblStatusPopupView.findViewById(R.id.statusAll))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_curTblStatus = Short.MIN_VALUE;
						((TextView) findViewById(R.id.tablestatus_txt))
								.setText("(全部)");
						popWnd.dismiss();
						_handler.sendEmptyMessage(FILTER_TABLE_BY_COND);
					}
				});

		// 餐台状态弹出View中的“空闲”Button
		((Button) tblStatusPopupView.findViewById(R.id.statusNobody))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_curTblStatus = Table.TABLE_IDLE;
						((TextView) findViewById(R.id.tablestatus_txt))
								.setText("(空闲)");
						popWnd.dismiss();
						_handler.sendEmptyMessage(FILTER_TABLE_BY_COND);
					}
				});

		// 餐台状态弹出View中的“就餐”Button
		((Button) tblStatusPopupView.findViewById(R.id.statusBusy))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_curTblStatus = Table.TABLE_BUSY;
						((TextView) findViewById(R.id.tablestatus_txt))
								.setText("(就餐)");
						popWnd.dismiss();
						_handler.sendEmptyMessage(FILTER_TABLE_BY_COND);
					}
				});

		// 响应餐台筛选状态的Click事件
		((FrameLayout) findViewById(R.id.showPopWindow))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (popWnd.isShowing()) {
							popWnd.dismiss();
						} else {
							popWnd.showAsDropDown(v);
						}
					}
				});

		// 餐台号输入框
		((EditText) findViewById(R.id.inputTableId))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						reflashTblNoInputBox();
					}
				});

		// 餐台号输入框的删除Button，单击时逐字删除
		((Button) findViewById(R.id.clearTableNum))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						EditText tblNoEdtTxt = ((EditText) findViewById(R.id.inputTableId));
						String s = tblNoEdtTxt.getText().toString();
						if (s.length() > 0) {
							tblNoEdtTxt.setText(s.substring(0, s.length() - 1));
						}
					}
				});

		// 餐台号输入框的删除Button，长按时全字删除
		((Button) findViewById(R.id.clearTableNum))
				.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						((EditText) findViewById(R.id.inputTableId))
								.setText("");
						return true;
					}
				});

		// 注销Button
		((Button) findViewById(R.id.logon_btn))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Editor editor = getSharedPreferences(Params.PREFS_NAME,
								Context.MODE_PRIVATE).edit();// 获取编辑器
						// editor.putLong(Params.STAFF_PIN, _staff.pin);
						editor.remove(Params.STAFF_PIN);
						// 提交修改
						editor.commit();
						_staff = null;
						showDialog(DIALOG_STAFF_LOGIN);
					}
				});

		// 返回Button
		((Button) findViewById(R.id.back_btn))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showDialog(DIALOG_EXIT_APP);
					}
				});

		// 加载餐台信息
		reflashTableArea();

		// 加载公告信息
		reflashRestaurantInfo();

		// 加载区域信息
		reflashRegion();

		// 加载餐台状态统计信息
		reflashTableStat();

		// 弹出登陆对话框(登陆操作)
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

			_handler.sendEmptyMessage(REDRAW_RESTAURANT);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.main_menu, menu);// 指定使用的XML
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int item_id = item.getItemId();
		switch (item_id) {
		// 点击跳转设置页面
		case R.id.menu_set:
			Intent intent = new Intent(this, WebSettingActivity.class);
			startActivity(intent);
			// finish();
			break;
		// 点击菜谱更新项
		case R.id.menu_update:
			// new QueryMenuTask().execute();
			break;

		}
		return true;

	}

	/**
	 * 刷新餐厅信息
	 */
	private void reflashRestaurantInfo() {
		if (WirelessOrder.restaurant != null) {
			TextView billBoard = (TextView) findViewById(R.id.notice);
			if (WirelessOrder.restaurant.info != null) {
				billBoard.setText(WirelessOrder.restaurant.info.replaceAll(
						"\n", ""));
			} else {
				billBoard.setText("");
			}

			TextView userName = (TextView) findViewById(R.id.username_value);
			if (_staff != null) {
				if (_staff.name != null) {
					userName.setText(WirelessOrder.restaurant.name + "("
							+ _staff.name + ")");
				} else {
					userName.setText(WirelessOrder.restaurant.name);
				}
			} else {
				userName.setText(WirelessOrder.restaurant.name);
			}
		}
	}

	/**
	 * 刷新餐台统计信息
	 */
	private void reflashTableStat() {
		int idle = 0;
		int busy = 0;
		for (int i = 0; i < _tableSource.length; i++) {
			if (_tableSource[i].status == Table.TABLE_BUSY) {
				busy++;
			} else if (_tableSource[i].status == Table.TABLE_IDLE) {
				idle++;
			}
		}
		// 设置餐台数（总数）
		((TextView) findViewById(R.id.tablecount_sum)).setText("("
				+ _tableSource.length + ")");
		// 设置餐台数（就餐）
		((TextView) findViewById(R.id.tablecount_busy)).setText("(" + busy
				+ ")");
		// 设置餐台数（空闲）
		((TextView) findViewById(R.id.tablecount_idle)).setText("(" + idle
				+ ")");
	}

	/**
	 * 刷新区域信息
	 */
	private void reflashRegion() {

		// 设置全部区域
		((Button) findViewById(R.id.bottomFirstBtn)).setText("全部");
		((Button) findViewById(R.id.bottomFirstBtn))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_curRegion = Short.MIN_VALUE;
						_curTblStatus = Short.MIN_VALUE;
						((TextView) findViewById(R.id.regionsInfo_txt))
								.setText("全部区域");
						((TextView) findViewById(R.id.tablestatus_txt))
								.setText("(全部)");
						_handler.sendEmptyMessage(FILTER_TABLE_BY_COND);
					}
				});

		View.OnClickListener regionClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_curRegion = (Short) v.getTag();
				((TextView) findViewById(R.id.regionsInfo_txt))
						.setText(((Button) v).getText());
				_handler.sendEmptyMessage(FILTER_TABLE_BY_COND);
			}
		};

		if (WirelessOrder.regions != null) {
			// 设置区域按钮文本和响应事件
			((Button) findViewById(R.id.region_1))
					.setText(WirelessOrder.regions[0].name);
			((Button) findViewById(R.id.region_1)).setTag(new Short(
					WirelessOrder.regions[0].regionID));
			((Button) findViewById(R.id.region_1))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_2))
					.setText(WirelessOrder.regions[1].name);
			((Button) findViewById(R.id.region_2)).setTag(new Short(
					WirelessOrder.regions[1].regionID));
			((Button) findViewById(R.id.region_2))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_3))
					.setText(WirelessOrder.regions[2].name);
			((Button) findViewById(R.id.region_3)).setTag(new Short(
					WirelessOrder.regions[2].regionID));
			((Button) findViewById(R.id.region_3))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_4))
					.setText(WirelessOrder.regions[3].name);
			((Button) findViewById(R.id.region_4)).setTag(new Short(
					WirelessOrder.regions[3].regionID));
			((Button) findViewById(R.id.region_4))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_5))
					.setText(WirelessOrder.regions[4].name);
			((Button) findViewById(R.id.region_5)).setTag(new Short(
					WirelessOrder.regions[4].regionID));
			((Button) findViewById(R.id.region_5))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_6))
					.setText(WirelessOrder.regions[5].name);
			((Button) findViewById(R.id.region_6)).setTag(new Short(
					WirelessOrder.regions[5].regionID));
			((Button) findViewById(R.id.region_6))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_7))
					.setText(WirelessOrder.regions[6].name);
			((Button) findViewById(R.id.region_7)).setTag(new Short(
					WirelessOrder.regions[6].regionID));
			((Button) findViewById(R.id.region_7))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_8))
					.setText(WirelessOrder.regions[7].name);
			((Button) findViewById(R.id.region_8)).setTag(new Short(
					WirelessOrder.regions[7].regionID));
			((Button) findViewById(R.id.region_8))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_9))
					.setText(WirelessOrder.regions[8].name);
			((Button) findViewById(R.id.region_9)).setTag(new Short(
					WirelessOrder.regions[8].regionID));
			((Button) findViewById(R.id.region_9))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_10))
					.setText(WirelessOrder.regions[9].name);
			((Button) findViewById(R.id.region_10)).setTag(new Short(
					WirelessOrder.regions[9].regionID));
			((Button) findViewById(R.id.region_10))
					.setOnClickListener(regionClickListener);

		}
	}

	/**
	 * 刷新餐台号输入框
	 */
	private void reflashTblNoInputBox() {

		((EditText) findViewById(R.id.inputTableId))
				.addTextChangedListener(new TextWatcher() {

					private LinearLayout _highLightedTbl = null;

					// 高亮餐台在餐台数据源中的位置
					private int _highLightedTblPos = 0;

					/**
					 * 餐台跟随餐台号的变化而自动选择
					 */
					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {

						try {
							/**
							 * 餐台号变化时，先去除原先餐台的高亮状态
							 */
							if (_highLightedTbl != null) {
								final LinearLayout highLightedTbl = _highLightedTbl;
								_tableAreaFlipper.post(new Runnable() {
									public void run() {
										highLightedTbl
												.setBackgroundResource(R.drawable.griditem_bg_selector);
									}
								});
							}

							int tblId = Integer.parseInt(s.toString().trim());

							_highLightedTblPos = -1;
							for (int i = 0; i < _tableSource.length; i++) {
								if (_tableSource[i].aliasID == tblId) {
									_highLightedTblPos = i;
									break;
								}
							}

							if (_highLightedTblPos != -1) {
								final int pos = _highLightedTblPos;
								// 自动跳转到高亮餐台所在那一屏
								_tableAreaFlipper
										.snapToScreen(_highLightedTblPos
												/ TABLE_AMOUNT_PER_PAGE);
								// 高亮显示与餐台号一致的餐台
								_tableAreaFlipper.post(new Runnable() {
									@Override
									public void run() {
										View v = ((GridView) _tableAreaFlipper
												.getChildAt(_highLightedTblPos
														/ TABLE_AMOUNT_PER_PAGE))
												.getChildAt(pos
														% TABLE_AMOUNT_PER_PAGE);
										_highLightedTbl = ((LinearLayout) v
												.findViewById(R.id.gridItemBg));
										_highLightedTbl
												.setBackgroundResource(R.drawable.av_r19_c17);
										reflashPageIndictor();
									}
								});

							}
						} catch (NumberFormatException e) {

						}

					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {

					}

					@Override
					public void afterTextChanged(Editable s) {

					}
				});

		// 设置取消
		((Button) findViewById(R.id.bottomFirstBtn)).setText("取消");
		((Button) findViewById(R.id.bottomFirstBtn))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						((EditText) findViewById(R.id.inputTableId))
								.setText("");
						reflashRegion();
					}
				});

		View.OnClickListener regionClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((EditText) findViewById(R.id.inputTableId))
						.append(((Button) v).getText().toString());
			}
		};

		if (WirelessOrder.regions != null) {
			// 设置0-9的数字按钮和响应事件
			((Button) findViewById(R.id.region_1)).setText("0");
			((Button) findViewById(R.id.region_1)).setTag(0);
			((Button) findViewById(R.id.region_1))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_2)).setText("1");
			((Button) findViewById(R.id.region_2)).setTag(1);
			((Button) findViewById(R.id.region_2))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_3)).setText("2");
			((Button) findViewById(R.id.region_3)).setTag(2);
			((Button) findViewById(R.id.region_3))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_4)).setText("3");
			((Button) findViewById(R.id.region_4)).setTag(3);
			((Button) findViewById(R.id.region_4))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_5)).setText("4");
			((Button) findViewById(R.id.region_5)).setTag(4);
			((Button) findViewById(R.id.region_5))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_6)).setText("5");
			((Button) findViewById(R.id.region_6)).setTag(5);
			((Button) findViewById(R.id.region_6))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_7)).setText("6");
			((Button) findViewById(R.id.region_7)).setTag(6);
			((Button) findViewById(R.id.region_7))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_8)).setText("7");
			((Button) findViewById(R.id.region_8)).setTag(7);
			((Button) findViewById(R.id.region_8))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_9)).setText("8");
			((Button) findViewById(R.id.region_9)).setTag(8);
			((Button) findViewById(R.id.region_9))
					.setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_10)).setText("9");
			((Button) findViewById(R.id.region_10)).setTag(9);
			((Button) findViewById(R.id.region_10))
					.setOnClickListener(regionClickListener);
		}

	}

	/**
	 * 根据传入的餐台信息，刷新餐台区域
	 */
	private void reflashTableArea() {

		// 计算屏幕的页数
		int pageSize = (_tableSource.length / TABLE_AMOUNT_PER_PAGE)
				+ (_tableSource.length % TABLE_AMOUNT_PER_PAGE == 0 ? 0 : 1);

		// 清空所有Grid View
		_tableAreaFlipper.removeAllViews();
		_tableAreaFlipper.page = 0;
		_tableAreaFlipper.mCurScreen = 0;
		_tableAreaFlipper.mDefaultScreen = 0;

		for (int pageNo = 0; pageNo < pageSize; pageNo++) {
			// 每页餐台的Grid View
			GridView grid = new GridView(this);

			grid.setSelected(true);

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.FILL_PARENT);
			lp.gravity = Gravity.CENTER;
			grid.setLayoutParams(lp);
			// 设置显示的列数
			grid.setNumColumns(6);

			grid.setSelector(android.R.color.transparent);

			// 获取显示在此page显示的Table对象
			ArrayList<Table> tables4Page = new ArrayList<Table>();
			for (int i = 0; i < TABLE_AMOUNT_PER_PAGE; i++) {
				int index = pageNo * TABLE_AMOUNT_PER_PAGE + i;
				if (index < _tableSource.length) {
					tables4Page.add(_tableSource[pageNo * TABLE_AMOUNT_PER_PAGE
							+ i]);
				} else {
					break;
				}
			}
			// 设置Grid的Adapter
			grid.setAdapter(new TableAdapter(tables4Page));

			// 添加Grid
			_tableAreaFlipper.addView(grid);
		}

		_pageIndicator = (LinearLayout) findViewById(R.id.page_point);
		_pageIndicator.removeAllViews();
		// 初始化页码指示器的每一项
		for (int i = 0; i < pageSize; i++) {
			ImageView point = new ImageView(this);
			point.setImageResource(R.drawable.av_r25_c31);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(0, 0, 25, 0);
			point.setLayoutParams(lp);
			_pageIndicator.addView(point);
		}
		// 刷新页码指示器
		reflashPageIndictor();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		_tableSource = WirelessOrder.tables == null ? new Table[0]
				: WirelessOrder.tables;
		init();
	}

	@Override
	protected void onStart() {
		super.onStart();
		((EditText) findViewById(R.id.inputTableId)).setText("");
		reflashRegion();
		new QueryTableTask().execute();
	}

	/**
	 * 刷新页码指示器
	 */
	private void reflashPageIndictor() {
		if (_tableAreaFlipper.getChildCount() > 0) {
			_pageIndicator.setVisibility(View.VISIBLE);
			for (int i = 0; i < _pageIndicator.getChildCount(); i++) {
				((ImageView) _pageIndicator.getChildAt(i))
						.setImageResource(R.drawable.av_r25_c31);
			}
			// highlight the active page point
			((ImageView) _pageIndicator.getChildAt(_tableAreaFlipper
					.getCurScreen())).setImageResource(R.drawable.av_r24_c28);

		} else {
			_pageIndicator.setVisibility(View.GONE);
		}
	}

	/**
	 * 切换底部按钮
	 */

	@Override
	protected Dialog onCreateDialog(int dialogID) {
		if (dialogID == DIALOG_EXIT_APP) {
			return new AlertDialog.Builder(this)
					.setTitle("提示信息")
					.setMessage("您是否确定退出e点通?")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									MainActivity.this.finish();
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

		} else if (dialogID == DIALOG_STAFF_LOGIN) {
			return new AskLoginDialog();

		} else {
			return null;
		}
	}

	/**
	 * 请求餐台信息
	 */
	private class QueryTableTask extends AsyncTask<Void, Void, String> {

		private ProgressDialog _progDialog;

		/**
		 * 在执行请求区域信息前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(MainActivity.this, "",
					"更新餐台信息...请稍候", true);
		}

		/**
		 * 在新的线程中执行请求餐台信息的操作
		 */
		@Override
		protected String doInBackground(Void... arg0) {

			String errMsg = null;
			try {
				WirelessOrder.tables = null;
				_tableSource = null;
				ProtocolPackage resp = ServerConnector.instance().ask(
						new ReqQueryTable());
				if (resp.header.type == Type.ACK) {
					WirelessOrder.tables = RespParser.parseQueryTable(resp);
					_tableSource = WirelessOrder.tables;
				}
			} catch (IOException e) {
				errMsg = e.getMessage();
			}

			return errMsg;
		}

		/**
		 * 根据返回的error message判断，如果发错异常则提示用户， 如果成功，则更新餐台区域，并请求区域信息。
		 */
		@Override
		protected void onPostExecute(String errMsg) {
			// make the progress dialog disappeared
			_progDialog.dismiss();

			/**
			 * Prompt user message if any error occurred.
			 */
			if (errMsg != null) {
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("提示")
						.setMessage(errMsg)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();
									}
								}).show();

			} else {
				reflashTableArea();
				reflashTableStat();
				Toast.makeText(MainActivity.this, "餐台更新成功", 0).show();
			}
		}
	}

	// 登录框Dialog
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
			setTitle("请输入账号与密码");
			final EditText pwdEdtTxt = (EditText) findViewById(R.id.pwd);
			final EditText staffTxtView = (EditText) findViewById(R.id.staffname);

			/**
			 * 帐号输入框显示员工列表
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

			// 获取自定义布局文件的视图
			View popupWndView = getLayoutInflater().inflate(
					R.layout.loginpopuwindow, null, false);
			// 创建PopupWindow实例
			_popupWindow = new PopupWindow(popupWndView, 320, 200, true);
			_popupWindow.setOutsideTouchable(true);
			_popupWindow.setFocusable(true);
			_popupWindow.setBackgroundDrawable(new BitmapDrawable());
			_popupWindow.update();
			ListView staffLstView = (ListView) popupWndView
					.findViewById(R.id.loginpopuwindow);
			_staffAdapter = new StaffsAdapter();
			staffLstView.setAdapter(_staffAdapter);

			/**
			 * 从下拉列表框中选择员工信息的操作
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
			 * 登录Button的点击操作
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
									errTxtView.setText("账号不能为空");

								} else if (_staff.pwd
										.equals(toHexString(digester.digest()))) {
									// 保存staff pin到文件里面
									Editor editor = getSharedPreferences(
											Params.PREFS_NAME,
											Context.MODE_PRIVATE).edit();// 获取编辑器
									editor.putLong(Params.STAFF_PIN, _staff.pin);
									// 提交修改
									editor.commit();
									_handler.sendEmptyMessage(REDRAW_RESTAURANT);
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
									errTxtView.setText("密码错误");
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
		 * 员工信息下拉框的Adapter
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
	 * 请求查询区域信息
	 */
	private class QueryRegionTask extends AsyncTask<Void, Void, String> {

		private ProgressDialog _progDialog;

		/**
		 * 在执行请求区域信息前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(MainActivity.this, "",
					"更新区域信息...请稍候", true);
		}

		/**
		 * 在新的线程中执行请求区域信息的操作
		 */
		@Override
		protected String doInBackground(Void... arg0) {

			String errMsg = null;
			try {
				WirelessOrder.regions = null;
				ProtocolPackage resp = ServerConnector.instance().ask(
						new ReqQueryRegion());
				if (resp.header.type == Type.ACK) {
					WirelessOrder.regions = RespParser.parseQueryRegion(resp);
				}
			} catch (IOException e) {
				errMsg = e.getMessage();
			}

			return errMsg;
		}

		/**
		 * 根据返回的error message判断，如果发错异常则提示用户， 如果成功，则执行更新区域信息。
		 */
		@Override
		protected void onPostExecute(String errMsg) {

			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if (errMsg != null) {
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("提示")
						.setMessage(errMsg)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();
									}
								}).show();

			} else {
				reflashRegion();
				Toast.makeText(MainActivity.this, "区域更新成功", 0).show();
				new QueryTableTask().execute();
			}
		}
	};

	/**
	 * 执行请求对应餐台的账单信息
	 */
	private class QueryOrderTask extends AsyncTask<Void, Void, String> {

		private ProgressDialog _progDialog;
		private int _tableID;
		private Order _order;
		private int _type = Type.UPDATE_ORDER;;

		QueryOrderTask(int tableID, int type) {
			_tableID = tableID;
			_type = type;
		}

		/**
		 * 在执行请求删单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(MainActivity.this, "", "查询"
					+ _tableID + "号餐台的信息...请稍候", true);
		}

		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			try {
				// 根据tableID请求数据
				ProtocolPackage resp = ServerConnector.instance().ask(
						new ReqQueryOrder(_tableID));
				if (resp.header.type == Type.ACK) {
					_order = RespParser.parseQueryOrder(resp,
							WirelessOrder.foodMenu);

				} else {
					if (resp.header.reserved == ErrorCode.TABLE_IDLE) {
						errMsg = _tableID + "号台还未下单";

					} else if (resp.header.reserved == ErrorCode.TABLE_NOT_EXIST) {
						errMsg = _tableID + "号台信息不存在";

					} else if (resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
						errMsg = "终端没有登记到餐厅，请联系管理人员。";

					} else if (resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
						errMsg = "终端已过期，请联系管理人员。";

					} else {
						errMsg = "未确定的异常错误(" + resp.header.reserved + ")";
					}
				}
			} catch (IOException e) {
				errMsg = e.getMessage();
			}

			return errMsg;
		}

		/**
		 * 根据返回的error message判断，如果发错异常则提示用户， 如果成功，则迁移到改单页面
		 */
		@Override
		protected void onPostExecute(String errMsg) {
			// make the progress dialog disappeared
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if (errMsg != null) {
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("提示")
						.setMessage(errMsg)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();
									}
								}).show();
			} else {
				if (_type == Type.UPDATE_ORDER) {
					// jump to the update order activity
					Intent intent = new Intent(MainActivity.this,
							ChgOrderActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable(OrderParcel.KEY_VALUE,
							new OrderParcel(_order));
					intent.putExtras(bundle);
					startActivity(intent);

				} else if (_type == Type.PAY_ORDER) {
					// jump to the pay order activity
					// Intent intent = new Intent(MainActivity.this,
					// BillActivity.class);
					// Bundle bundle = new Bundle();
					// bundle.putParcelable(OrderParcel.KEY_VALUE, new
					// OrderParcel(_order));
					// intent.putExtras(bundle);
					// startActivity(intent);
				}
			}
		}

	}

	/**
	 * 请求查询餐厅信息
	 */
	private class QueryRestaurantTask extends AsyncTask<Void, Void, String> {

		private ProgressDialog _progDialog;

		/**
		 * 在执行请求餐厅信息前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(MainActivity.this, "",
					"更新餐厅信息...请稍候", true);
		}

		/**
		 * 在新的线程中执行请求餐厅信息的操作
		 */
		@Override
		protected String doInBackground(Void... arg0) {

			String errMsg = null;
			try {
				ProtocolPackage resp = ServerConnector.instance().ask(
						new ReqQueryRestaurant());
				if (resp.header.type == Type.ACK) {
					WirelessOrder.restaurant = RespParser
							.parseQueryRestaurant(resp);
				}
			} catch (IOException e) {
				errMsg = e.getMessage();
			}

			return errMsg;
		}

		/**
		 * 根据返回的error message判断，如果发错异常则提示用户， 如果成功，则跳转到主界面。
		 */
		@Override
		protected void onPostExecute(String errMsg) {

			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if (errMsg != null) {
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("提示")
						.setMessage(errMsg)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();

									}
								}).show();

			} else {
				reflashRestaurantInfo();
				Toast.makeText(MainActivity.this, "餐厅信息更新成功", 0).show();
				new QueryRegionTask().execute();
			}
		}
	}

	/**
	 * 餐台信息的Adapter
	 * 
	 * @author Ying.Zhang
	 * 
	 */
	private class TableAdapter extends BaseAdapter {

		private ArrayList<Table> _tables;

		TableAdapter(ArrayList<Table> tables) {
			this._tables = tables;
		}

		@Override
		public int getCount() {
			return _tables.size();
		}

		@Override
		public Object getItem(int position) {
			return _tables.get(position);
		}

		@Override
		public long getItemId(int position) {
			return _tables.get(position).tableID;
		}

		@Override
		public View getView(int position, View convertView,
				final ViewGroup parent) {

			View view;

			if (convertView == null) {
				view = LayoutInflater.from(parent.getContext()).inflate(
						R.layout.gridviewitem, null);
			} else {
				view = convertView;
			}

			final Table table = _tables.get(position);

			// 根据餐台的不同状态设置背景
			if (table.status == Table.TABLE_BUSY) {
				((FrameLayout) view.findViewById(R.id.item1))
						.setBackgroundResource(R.drawable.av_r39_c15);
				((FrameLayout) view.findViewById(R.id.item4))
						.setBackgroundResource(R.drawable.av_r42_c15);
			} else {
				((FrameLayout) view.findViewById(R.id.item1))
						.setBackgroundResource(R.drawable.av_r40_c8);
				((FrameLayout) view.findViewById(R.id.item4))
						.setBackgroundResource(R.drawable.av_r43_c8);
			}
			// 设置餐台台号
			((TextView) view.findViewById(R.id.item3)).setText(Integer
					.toString(table.aliasID));
			// 设置餐台名称
			((TextView) view.findViewById(R.id.item5)).setText(table.name);

			view.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (table.status == Table.TABLE_IDLE) {
						// jump to the order activity with the table parcel in
						// case of idle
						Intent intent = new Intent(MainActivity.this,
								OrderActivity.class);
						Bundle bundle = new Bundle();
						bundle.putParcelable(TableParcel.KEY_VALUE,
								new TableParcel(table));
						intent.putExtras(bundle);
						startActivity(intent);

					} else if (table.status == Table.TABLE_BUSY) {
						// jump to change order activity with the order in case
						// of busy
						new QueryOrderTask(table.aliasID, Type.UPDATE_ORDER)
								.execute();
					}
				}
			});

			view.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					if (table.status == Table.TABLE_BUSY) {
						new AlertDialog.Builder(parent.getContext())
								.setTitle("请选择" + table.aliasID + "号餐台的操作")
								.setItems(new String[] { "改单", "转台" }, null)
								.setNegativeButton("返回", null).show();

					} else if (table.status == Table.TABLE_IDLE) {
						new AlertDialog.Builder(parent.getContext())
								.setTitle("请选择" + table.aliasID + "号餐台的操作")
								.setItems(new String[] { "下单" }, null)
								.setNegativeButton("返回", null).show();
					}
					return true;
				}
			});

			return view;
		}
	}
}
