package com.wireless.pad;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
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
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
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
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.parcel.OrderParcel;
import com.wireless.parcel.TableParcel;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.Region;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.ReqQueryOrder;
import com.wireless.protocol.ReqQueryRegion;
import com.wireless.protocol.ReqQueryRestaurant;
import com.wireless.protocol.ReqQueryStaff;
import com.wireless.protocol.ReqQueryTable;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;
import com.wireless.view.ScrollLayout;
import com.wireless.view.ScrollLayout.OnViewChangedListner;

public class MainActivity extends Activity {

	private Handler _handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == FILTER_TABLE_BY_COND) {
				ArrayList<Table> tmpTbls = new ArrayList<Table>(Arrays.asList(WirelessOrder.tables));
				Iterator<Table> iter = tmpTbls.iterator();
				while (iter.hasNext()) {
					Table table = iter.next();
					if (_curTblStatus != ALL_STATUS) {
						if (table.status != _curTblStatus) {
							iter.remove();
							continue;
						}
					}
					if (_curRegion != ALL_REGION) {
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

	//��̨״̬����Timer
	private Timer _tblReflashTimer;		
	
	private final static short ALL_STATUS = Short.MIN_VALUE;
	// ��ǰҪɸѡ�Ĳ�̨״̬��С��0��ʾȫ��״̬
	private short _curTblStatus = ALL_STATUS; 
	
	private final static short ALL_REGION = Short.MIN_VALUE;
	// ��ǰҪɸѡ�Ĳ�̨����С��0��ʾȫ������
	private short _curRegion = ALL_REGION; 

	// ÿҳҪ��ʾ��̨����
	private final static int TABLE_AMOUNT_PER_PAGE = 24; 

	private final static int NETWORK_SET = 0;

	private StaffTerminal _staff;

	// ��̨��ʾ����
	private ScrollLayout _tblScrolledArea; 

	// �����������ڲ�̨��ʾ������Դ
	private Table[] _tableSource = new Table[0]; 

	private final static int DIALOG_EXIT_APP = 0;
	private final static int DIALOG_STAFF_LOGIN = 1;

	private final static int FILTER_TABLE_BY_COND = 0; //
	private final static int REDRAW_RESTAURANT = 2; //

	// private float mStartX, mEndX; // �����л�ҳ��ʱ��¼��X����ֵ

	private void init() {

		// ȫ��ˢ��Button
		((Button) findViewById(R.id.reAll_btn))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_curRegion = ALL_REGION;
						_curTblStatus = ALL_STATUS;
						((TextView) findViewById(R.id.regionsInfo_txt))
								.setText("ȫ������");
						((TextView) findViewById(R.id.tablestatus_txt))
								.setText("(ȫ��)");
						_handler.sendEmptyMessage(FILTER_TABLE_BY_COND);
					}
				});

		// ��ʾ��̨��scroll view group
		_tblScrolledArea = (ScrollLayout) findViewById(R.id.tableFlipper);
		_tblScrolledArea.getLayoutParams().height = this.getWindowManager()
				.getDefaultDisplay().getHeight() * 2 / 3;
		_tblScrolledArea.setOnViewChangedListener(new OnViewChangedListner() {
			@Override
			public void onViewChanged(int curScreen, View parent, View curView) {
				reflashPageIndictor();
			}
		});

		// ˢ��Button
		((Button) findViewById(R.id.refurbish_btn))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new QueryRestaurantTask().execute();
					}
				});

		//
		final TextSwitcher numberSwitcher = (TextSwitcher) findViewById(R.id.switcher);
		numberSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
			@Override
			public View makeView() {
				TextView t = new TextView(MainActivity.this);
				t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
				t.setTextSize(100);
				t.setTextColor(Color.rgb(178, 34, 34));
				return t;
			}
		});

		Animation in = AnimationUtils.loadAnimation(this, R.anim.sw);
		in.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				numberSwitcher.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				numberSwitcher.setVisibility(View.INVISIBLE);
			}
		});
		numberSwitcher.setInAnimation(in);
//		numberSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
//		numberSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
		
		// ���������̨״̬�󵯳������View
		final View tblStatusPopupView = getLayoutInflater().inflate(
				R.layout.main_pop_window, null);
		// ���������View������pop-up window
		final PopupWindow popWnd = new PopupWindow(tblStatusPopupView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		popWnd.setOutsideTouchable(true);
		popWnd.setBackgroundDrawable(new BitmapDrawable());
		popWnd.update();

		// ��̨״̬����View�еġ�ȫ����Button
		((Button) tblStatusPopupView.findViewById(R.id.statusAll))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_curTblStatus = ALL_STATUS;
						((TextView) findViewById(R.id.tablestatus_txt))
								.setText("(ȫ��)");
						popWnd.dismiss();
						_handler.sendEmptyMessage(FILTER_TABLE_BY_COND);
					}
				});

		// ��̨״̬����View�еġ����С�Button
		((Button) tblStatusPopupView.findViewById(R.id.statusNobody))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_curTblStatus = Table.TABLE_IDLE;
						((TextView) findViewById(R.id.tablestatus_txt))
								.setText("(����)");
						popWnd.dismiss();
						_handler.sendEmptyMessage(FILTER_TABLE_BY_COND);
					}
				});

		// ��̨״̬����View�еġ��Ͳ͡�Button
		((Button) tblStatusPopupView.findViewById(R.id.statusBusy))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_curTblStatus = Table.TABLE_BUSY;
						((TextView) findViewById(R.id.tablestatus_txt))
								.setText("(�Ͳ�)");
						popWnd.dismiss();
						_handler.sendEmptyMessage(FILTER_TABLE_BY_COND);
					}
				});

		// ��Ӧ��̨ɸѡ״̬��Click�¼�
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

		// ��̨״̬����View�еġ�ȫ����Button
		((Button) tblStatusPopupView.findViewById(R.id.statusAll))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_curTblStatus = Short.MIN_VALUE;
						((TextView) findViewById(R.id.tablestatus_txt))
								.setText("(ȫ��)");
						popWnd.dismiss();
						_handler.sendEmptyMessage(FILTER_TABLE_BY_COND);
					}
				});

		// ��̨״̬����View�еġ����С�Button
		((Button) tblStatusPopupView.findViewById(R.id.statusNobody))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_curTblStatus = Table.TABLE_IDLE;
						((TextView) findViewById(R.id.tablestatus_txt))
								.setText("(����)");
						popWnd.dismiss();
						_handler.sendEmptyMessage(FILTER_TABLE_BY_COND);
					}
				});

		// ��̨״̬����View�еġ��Ͳ͡�Button
		((Button) tblStatusPopupView.findViewById(R.id.statusBusy))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_curTblStatus = Table.TABLE_BUSY;
						((TextView) findViewById(R.id.tablestatus_txt))
								.setText("(�Ͳ�)");
						popWnd.dismiss();
						_handler.sendEmptyMessage(FILTER_TABLE_BY_COND);
					}
				});

		// ��Ӧ��̨ɸѡ״̬��Click�¼�
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

		// ��̨�������
		((EditText) findViewById(R.id.inputTableId))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						reflashTblNoInputBox();
					}
				});

		// ��̨��������ɾ��Button������ʱ����ɾ��
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

		// ��̨��������ɾ��Button������ʱȫ��ɾ��
		((Button) findViewById(R.id.clearTableNum))
				.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						((EditText) findViewById(R.id.inputTableId))
								.setText("");
						return true;
					}
				});

		// ע��Button
		((Button) findViewById(R.id.logon_btn))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new QueryStaffTask(false).execute();
					}
				});

		// ����Button
		((Button) findViewById(R.id.back_btn))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showDialog(DIALOG_EXIT_APP);
					}
				});

		// ���ز�̨��Ϣ
		reflashTableArea();

		// ���ع�����Ϣ
		reflashRestaurantInfo();

		// ����������Ϣ
		reflashRegion();

		// ���ز�̨״̬ͳ����Ϣ
		reflashTableStat();

		// ������½�Ի���(��½����)
		if (WirelessOrder.staffs != null) {
			long pin = getSharedPreferences(Params.PREFS_NAME,
					Context.MODE_PRIVATE).getLong(Params.STAFF_PIN,
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
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.main_menu, menu);// ָ��ʹ�õ�XML
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// �����ת����ҳ��
		case R.id.menu_set:
			Intent intent = new Intent(MainActivity.this, WebSettingActivity.class);
			startActivityForResult(intent, NETWORK_SET);
			// finish();
			break;
		// ������׸�����
		case R.id.menu_update:
			// new QueryMenuTask().execute();
			break;
			
		//���ڽ���
		case R.id.menu_abount:
			Intent aboutintent = new Intent(MainActivity.this,AboutActivity.class);
			startActivity(aboutintent);
			break;
			

		}
		return true;

	}

	/**
	 * ˢ�²�����Ϣ
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
	 * ˢ�²�̨ͳ����Ϣ
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
		// ���ò�̨����������
		((TextView) findViewById(R.id.tablecount_sum)).setText("("	+ _tableSource.length + ")");
		// ���ò�̨�����Ͳͣ�
		((TextView) findViewById(R.id.tablecount_busy)).setText("(" + busy	+ ")");
		// ���ò�̨�������У�
		((TextView) findViewById(R.id.tablecount_idle)).setText("(" + idle + ")");
	}

	/**
	 * ˢ��������Ϣ
	 */
	private void reflashRegion() {

		// ����ȫ������
		((Button) findViewById(R.id.bottomFirstBtn)).setText("ȫ��");
		((Button) findViewById(R.id.bottomFirstBtn)).setPadding(0, 0, 0, 0);

		// ����ȫ������Button����Ӧ�¼�
		((Button) findViewById(R.id.bottomFirstBtn))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_curRegion = Short.MIN_VALUE;
						_curTblStatus = Short.MIN_VALUE;
						((TextView) findViewById(R.id.regionsInfo_txt)).setText("ȫ������");
						((TextView) findViewById(R.id.tablestatus_txt)).setText("(ȫ��)");
						highLightRegionBtn();
						_handler.sendEmptyMessage(FILTER_TABLE_BY_COND);
					}
				});

		// ����ÿ������Button����Ӧ�¼�
		View.OnClickListener regionClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_curRegion = (Short) v.getTag();
				((TextView) findViewById(R.id.regionsInfo_txt)).setText(((Button) v).getText());
				highLightRegionBtn();
				_handler.sendEmptyMessage(FILTER_TABLE_BY_COND);
			}
		};

		if (WirelessOrder.regions != null) {
			// ��������ť�ı�����Ӧ�¼�
			((Button) findViewById(R.id.region_1)).setText(WirelessOrder.regions[0].name);
			((Button) findViewById(R.id.region_1)).setTag(new Short(WirelessOrder.regions[0].regionID));
			((Button) findViewById(R.id.region_1)).setPadding(0, 0, 0, 0);
			((Button) findViewById(R.id.region_1)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_2)).setText(WirelessOrder.regions[1].name);
			((Button) findViewById(R.id.region_2)).setTag(new Short(WirelessOrder.regions[1].regionID));
			((Button) findViewById(R.id.region_2)).setPadding(0, 0, 0, 0);
			((Button) findViewById(R.id.region_2)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_3)).setText(WirelessOrder.regions[2].name);
			((Button) findViewById(R.id.region_3)).setTag(new Short(WirelessOrder.regions[2].regionID));
			((Button) findViewById(R.id.region_3)).setPadding(0, 0, 0, 0);
			((Button) findViewById(R.id.region_3)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_4)).setText(WirelessOrder.regions[3].name);
			((Button) findViewById(R.id.region_4)).setTag(new Short(WirelessOrder.regions[3].regionID));
			((Button) findViewById(R.id.region_4)).setPadding(0, 0, 0, 0);
			((Button) findViewById(R.id.region_4)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_5)).setText(WirelessOrder.regions[4].name);
			((Button) findViewById(R.id.region_5)).setTag(new Short(WirelessOrder.regions[4].regionID));
			((Button) findViewById(R.id.region_5)).setPadding(0, 0, 0, 0);
			((Button) findViewById(R.id.region_5)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_6)).setText(WirelessOrder.regions[5].name);
			((Button) findViewById(R.id.region_6)).setTag(new Short(WirelessOrder.regions[5].regionID));
			((Button) findViewById(R.id.region_6)).setPadding(0, 0, 0, 0);
			((Button) findViewById(R.id.region_6)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_7)).setText(WirelessOrder.regions[6].name);
			((Button) findViewById(R.id.region_7)).setTag(new Short(WirelessOrder.regions[6].regionID));
			((Button) findViewById(R.id.region_7)).setPadding(0, 0, 0, 0);
			((Button) findViewById(R.id.region_7)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_8)).setText(WirelessOrder.regions[7].name);
			((Button) findViewById(R.id.region_8)).setTag(new Short(WirelessOrder.regions[7].regionID));
			((Button) findViewById(R.id.region_8)).setPadding(0, 0, 0, 0);
			((Button) findViewById(R.id.region_8)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_9)).setText(WirelessOrder.regions[8].name);
			((Button) findViewById(R.id.region_9)).setTag(new Short(WirelessOrder.regions[8].regionID));
			((Button) findViewById(R.id.region_9)).setPadding(0, 0, 0, 0);
			((Button) findViewById(R.id.region_9)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_10)).setText(WirelessOrder.regions[9].name);
			((Button) findViewById(R.id.region_10)).setTag(new Short(WirelessOrder.regions[9].regionID));
			((Button) findViewById(R.id.region_10)).setPadding(0, 0, 0, 0);
			((Button) findViewById(R.id.region_10)).setOnClickListener(regionClickListener);

		}
		
		highLightRegionBtn();
	}

	/**
	 * ˢ�²�̨�������
	 */
	private void reflashTblNoInputBox() {

		((EditText) findViewById(R.id.inputTableId))
				.addTextChangedListener(new TextWatcher() {

					private LinearLayout _highLightedTbl = null;

					// ������̨�ڲ�̨����Դ�е�λ��
					private int _highLightedTblPos = 0;

					/**
					 * ��̨�����̨�ŵı仯���Զ�ѡ��
					 */
					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {

						try {
							/**
							 * ��̨�ű仯ʱ����ȥ��ԭ�Ȳ�̨�ĸ���״̬
							 */
							if (_highLightedTbl != null) {
								final LinearLayout highLightedTbl = _highLightedTbl;
								_tblScrolledArea.post(new Runnable() {
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
								// �Զ���ת��������̨������һ��
								_tblScrolledArea.setToScreen(_highLightedTblPos / TABLE_AMOUNT_PER_PAGE);
								// ������ʾ���̨��һ�µĲ�̨
								_tblScrolledArea.post(new Runnable() {
									@Override
									public void run() {
										View v = ((GridView)_tblScrolledArea.getChildAt(_highLightedTblPos	/ TABLE_AMOUNT_PER_PAGE)).getChildAt(pos % TABLE_AMOUNT_PER_PAGE);
										_highLightedTbl = ((LinearLayout)v.findViewById(R.id.gridItemBg));
										_highLightedTbl.setBackgroundResource(R.drawable.av_r19_c17);
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

		// ����ȡ��
		((Button) findViewById(R.id.bottomFirstBtn)).setText("ȡ��");
		((Button)findViewById(R.id.bottomFirstBtn)).setBackgroundResource(R.drawable.av_bottombtn_selector);
		((Button)findViewById(R.id.bottomFirstBtn)).setPadding(0, 0, 0, 0);
		((Button) findViewById(R.id.bottomFirstBtn))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						((EditText) findViewById(R.id.inputTableId))
								.setText("");
						reflashRegion();
					}
				});

		
		//������ּ�Button����Ӧ�¼�
		View.OnClickListener regionClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String num = ((Button) v).getText().toString();
				// �˴�����Ļ�м���ʾ����
				TextSwitcher numberSwitcher = (TextSwitcher)findViewById(R.id.switcher);
				numberSwitcher.setVisibility(View.VISIBLE);
				numberSwitcher.setText(num);
				((EditText) findViewById(R.id.inputTableId)).append(num);
			}
		};

		if (WirelessOrder.regions != null) {

			// ����0-9�����ְ�ť����Ӧ�¼�
			((Button) findViewById(R.id.region_1)).setText("0");
			((Button) findViewById(R.id.region_1)).setTag(0);
			((Button) findViewById(R.id.region_1)).setBackgroundResource(R.drawable.av_bottombtn_selector);
			((Button) findViewById(R.id.region_1)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_2)).setText("1");
			((Button) findViewById(R.id.region_2)).setTag(1);
			((Button) findViewById(R.id.region_3)).setBackgroundResource(R.drawable.av_bottombtn_selector);
			((Button) findViewById(R.id.region_2)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_3)).setText("2");
			((Button) findViewById(R.id.region_3)).setTag(2);
			((Button) findViewById(R.id.region_3)).setBackgroundResource(R.drawable.av_bottombtn_selector);
			((Button) findViewById(R.id.region_3)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_4)).setText("3");
			((Button) findViewById(R.id.region_4)).setTag(3);
			((Button) findViewById(R.id.region_4)).setBackgroundResource(R.drawable.av_bottombtn_selector);
			((Button) findViewById(R.id.region_4)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_5)).setText("4");
			((Button) findViewById(R.id.region_5)).setTag(4);
			((Button) findViewById(R.id.region_5)).setBackgroundResource(R.drawable.av_bottombtn_selector);
			((Button) findViewById(R.id.region_5)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_6)).setText("5");
			((Button) findViewById(R.id.region_6)).setTag(5);
			((Button) findViewById(R.id.region_6)).setBackgroundResource(R.drawable.av_bottombtn_selector);
			((Button) findViewById(R.id.region_6)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_7)).setText("6");
			((Button) findViewById(R.id.region_7)).setTag(6);
			((Button) findViewById(R.id.region_7)).setBackgroundResource(R.drawable.av_bottombtn_selector);
			((Button) findViewById(R.id.region_7)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_8)).setText("7");
			((Button) findViewById(R.id.region_8)).setTag(7);
			((Button) findViewById(R.id.region_8)).setBackgroundResource(R.drawable.av_bottombtn_selector);
			((Button) findViewById(R.id.region_8)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_9)).setText("8");
			((Button) findViewById(R.id.region_9)).setTag(8);
			((Button) findViewById(R.id.region_9)).setBackgroundResource(R.drawable.av_bottombtn_selector);
			((Button) findViewById(R.id.region_9)).setOnClickListener(regionClickListener);

			((Button) findViewById(R.id.region_10)).setText("9");
			((Button) findViewById(R.id.region_10)).setTag(9);
			((Button) findViewById(R.id.region_10)).setBackgroundResource(R.drawable.av_bottombtn_selector);
			((Button) findViewById(R.id.region_10)).setOnClickListener(regionClickListener);
		}

	}

	/**
	 * ���ݴ���Ĳ�̨��Ϣ��ˢ�²�̨����
	 */
	private void reflashTableArea() {

		// ������Ļ��ҳ��
		int pageSize = (_tableSource.length / TABLE_AMOUNT_PER_PAGE)
				+ (_tableSource.length % TABLE_AMOUNT_PER_PAGE == 0 ? 0 : 1);

		// �������Grid View
		_tblScrolledArea.removeAllViews();
		_tblScrolledArea.page = 0;
		_tblScrolledArea.mCurScreen = 0;
		_tblScrolledArea.mDefaultScreen = 0;

		for (int pageNo = 0; pageNo < pageSize; pageNo++) {
			// ÿҳ��̨��Grid View
			GridView grid = new GridView(this);

			grid.setSelected(true);

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.FILL_PARENT);
			lp.gravity = Gravity.CENTER;
			grid.setLayoutParams(lp);
			// ������ʾ������
			grid.setNumColumns(6);

			grid.setSelector(android.R.color.transparent);

			// ��ȡ��ʾ�ڴ�page��ʾ��Table����
			ArrayList<Table> tables4Page = new ArrayList<Table>();
			for (int i = 0; i < TABLE_AMOUNT_PER_PAGE; i++) {
				int index = pageNo * TABLE_AMOUNT_PER_PAGE + i;
				if (index < _tableSource.length) {
					tables4Page.add(_tableSource[pageNo * TABLE_AMOUNT_PER_PAGE	+ i]);
				} else {
					break;
				}
			}
			// ����Grid��Adapter
			grid.setAdapter(new TableAdapter(tables4Page));

			// ���Grid
			_tblScrolledArea.addView(grid);
			
			
		}

		LinearLayout pageIndicator = (LinearLayout) findViewById(R.id.page_point);
		pageIndicator.removeAllViews();
		// ��ʼ��ҳ��ָʾ����ÿһ��
		for (int i = 0; i < pageSize; i++) {
			ImageView point = new ImageView(this);
			point.setImageResource(R.drawable.av_r25_c31);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(0, 0, 25, 0);
			point.setLayoutParams(lp);
			pageIndicator.addView(point);
		}
		// ˢ��ҳ��ָʾ��
		reflashPageIndictor();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		_tableSource = WirelessOrder.tables == null ? new Table[0] : WirelessOrder.tables;
		init();
	}

	@Override
	protected void onStart() {
		super.onStart();
		((EditText) findViewById(R.id.inputTableId)).setText("");
		reflashRegion();
		new QueryTableTask().execute();
	}

	@Override
	protected void onResume(){
		super.onResume();
		_tblReflashTimer = new Timer();
		/**
		 * ��MIN_PERIOD��MAX_PERIOD֮�����һ�����ʱ�䣬�������ʱ��������ڸ��²�̨��Ϣ
		 */
		final long MIN_PERIOD =  5 * 60 * 1000;
		final long MAX_PERIOD = 10 * 60 * 1000;
		_tblReflashTimer.schedule(new TimerTask() {			
			@Override
			public void run() {
				_tblScrolledArea.post(new Runnable(){
					@Override
					public void run(){
						new QueryTableTask().execute();						
					}					
				});
			}
		}, 
		Math.round(Math.random() * (MAX_PERIOD - MIN_PERIOD) + MIN_PERIOD), 
		Math.round(Math.random() * (MAX_PERIOD - MIN_PERIOD) + MIN_PERIOD));
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		_tblReflashTimer.cancel();
	}
	
	/**
	 * �ж�����һ��Activity���ص�
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// ������趨Activity���أ�����������Ա����Ϣ
		if (requestCode == NETWORK_SET) {
			if (resultCode == RESULT_OK) {
				// ��������Ա����Ϣ�����²���
				ReqPackage.setGen(new PinGen() {
					@Override
					public long getDeviceId() {
						return WirelessOrder.pin;
					}

					@Override
					public short getDeviceType() {
						return Terminal.MODEL_ANDROID;
					}
				});
				new QueryStaffTask(true).execute();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * ˢ��ҳ��ָʾ��
	 */
	private void reflashPageIndictor() {
		LinearLayout pageIndicator = (LinearLayout) findViewById(R.id.page_point);
		if (_tblScrolledArea.getChildCount() > 0) {
			pageIndicator.setVisibility(View.VISIBLE);
			for (int i = 0; i < pageIndicator.getChildCount(); i++) {
				((ImageView) pageIndicator.getChildAt(i))
						.setImageResource(R.drawable.av_r25_c31);
			}
			// highlight the active page point
			((ImageView) pageIndicator.getChildAt(_tblScrolledArea
					.getCurScreen())).setImageResource(R.drawable.av_r24_c28);

		} else {
			pageIndicator.setVisibility(View.GONE);
		}
	}

	/**
	 * �л��ײ���ť
	 */
	@Override
	protected Dialog onCreateDialog(int dialogID) {
		if (dialogID == DIALOG_EXIT_APP) {
			return new AlertDialog.Builder(this)
					.setTitle("��ʾ��Ϣ")
					.setMessage("���Ƿ�ȷ���˳�e��ͨ?")
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									MainActivity.this.finish();
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

		} else if (dialogID == DIALOG_STAFF_LOGIN) {
			return new AskLoginDialog();

		} else {
			return null;
		}
	}

	/**
	 * �����̨��Ϣ
	 */
	private class QueryTableTask extends AsyncTask<Void, Void, String> {

		private ProgressDialog _progDialog;

		/**
		 * ��ִ������������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(MainActivity.this, "",
					"���²�̨��Ϣ...���Ժ�", true);
		}

		/**
		 * ���µ��߳���ִ�������̨��Ϣ�Ĳ���
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
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û��� ����ɹ�������²�̨���򣬲�����������Ϣ��
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
						.setTitle("��ʾ")
						.setMessage(errMsg)
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();
									}
								}).show();

			} else {
				reflashTableArea();
				reflashTableStat();
				Toast.makeText(MainActivity.this, "��̨���³ɹ�", 0).show();
			}
		}
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
			_popupWindow = new PopupWindow(popupWndView, 325, 200, true);
			_popupWindow.setOutsideTouchable(true);
			_popupWindow.setFocusable(true);
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
									errTxtView.setText("�������");
								}

							} catch (NoSuchAlgorithmException e) {
								errTxtView.setText(e.getMessage());
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
	 * �����ѯ������Ϣ
	 */
	private class QueryRegionTask extends AsyncTask<Void, Void, String> {

		private ProgressDialog _progDialog;

		/**
		 * ��ִ������������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(MainActivity.this, "",
					"����������Ϣ...���Ժ�", true);
		}

		/**
		 * ���µ��߳���ִ������������Ϣ�Ĳ���
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
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û��� ����ɹ�����ִ�и���������Ϣ��
		 */
		@Override
		protected void onPostExecute(String errMsg) {

			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if (errMsg != null) {
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("��ʾ")
						.setMessage(errMsg)
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();
									}
								}).show();

			} else {
				reflashRegion();
				Toast.makeText(MainActivity.this, "������³ɹ�", 0).show();
				new QueryTableTask().execute();
			}
		}
	};

	/**
	 * ִ�������Ӧ��̨���˵���Ϣ
	 */
	private class QueryOrderTask extends AsyncTask<Void, Void, String> {

		private ProgressDialog _progDialog;
		private Table _tbl2Query;
		private Order _order;
		private int _tblStatus = ErrorCode.TABLE_IDLE;

		QueryOrderTask(Table tbl) {
			_tbl2Query = tbl;
		}

		/**
		 * ��ִ������ɾ������ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(MainActivity.this, "", "��ѯ"	+ _tbl2Query.aliasID + "�Ų�̨����Ϣ...���Ժ�", true);
		}

		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			try {
				// ����tableID��������
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryOrder(_tbl2Query.aliasID));
				if (resp.header.type == Type.ACK) {
					_order = RespParser.parseQueryOrder(resp, WirelessOrder.foodMenu);
					_tblStatus = ErrorCode.TABLE_BUSY;

				} else {
					if (resp.header.reserved == ErrorCode.TABLE_IDLE) {
						_tblStatus = ErrorCode.TABLE_IDLE;

					} else if (resp.header.reserved == ErrorCode.TABLE_NOT_EXIST) {
						errMsg = _tbl2Query.aliasID + "��̨��Ϣ������";

					} else if (resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
						errMsg = "�ն�û�еǼǵ�����������ϵ������Ա��";

					} else if (resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
						errMsg = "�ն��ѹ��ڣ�����ϵ������Ա��";

					} else {
						errMsg = "δȷ�����쳣����(" + resp.header.reserved + ")";
					}
				}
			} catch (IOException e) {
				errMsg = e.getMessage();
			}

			return errMsg;
		}

		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û��� ����ɹ�����Ǩ�Ƶ��ĵ�ҳ��
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
						.setTitle("��ʾ")
						.setMessage(errMsg)
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();
									}
								}).show();
			} else {
				
				if(_tblStatus == ErrorCode.TABLE_IDLE){
					// jump to the order activity with the table parcel in case of idle
					Intent intent = new Intent(MainActivity.this, OrderActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable(TableParcel.KEY_VALUE,	new TableParcel(_tbl2Query));
					intent.putExtras(bundle);
					startActivity(intent);
					
				}else if (_tblStatus == ErrorCode.TABLE_BUSY) {
					// jump to the update order activity
					Intent intent = new Intent(MainActivity.this, ChgOrderActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable(OrderParcel.KEY_VALUE,	new OrderParcel(_order));
					intent.putExtras(bundle);
					startActivity(intent);

				}
			}
		}

	}

	/**
	 * �����ѯ������Ϣ
	 */
	private class QueryRestaurantTask extends AsyncTask<Void, Void, String> {

		private ProgressDialog _progDialog;

		/**
		 * ��ִ�����������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(MainActivity.this, "",
					"���²�����Ϣ...���Ժ�", true);
		}

		/**
		 * ���µ��߳���ִ�����������Ϣ�Ĳ���
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
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û��� ����ɹ�������ת�������档
		 */
		@Override
		protected void onPostExecute(String errMsg) {

			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if (errMsg != null) {
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("��ʾ")
						.setMessage(errMsg)
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();

									}
								}).show();

			} else {
				reflashRestaurantInfo();
				Toast.makeText(MainActivity.this, "������Ϣ���³ɹ�", 0).show();
				new QueryRegionTask().execute();
			}
		}
	}

	/**
	 * �����ѯԱ����Ϣ
	 */
	private class QueryStaffTask extends AsyncTask<Void, Void, String> {

		private ProgressDialog _progDialog;

		private boolean _isTableUpdate;

		QueryStaffTask(boolean isMenuUpdate) {
			_isTableUpdate = isMenuUpdate;
		}

		/**
		 * ִ��Ա����Ϣ����ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {

			_progDialog = ProgressDialog.show(MainActivity.this, "",
					"���ڸ���Ա����Ϣ...���Ժ�", true);
		}

		/**
		 * ���µ��߳���ִ������Ա����Ϣ�Ĳ���
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			try {
				WirelessOrder.staffs = null;
				ReqPackage.setGen(new PinGen() {
					@Override
					public long getDeviceId() {
						return WirelessOrder.pin;
					}

					@Override
					public short getDeviceType() {
						return Terminal.MODEL_ANDROID;
					}
				});
				ProtocolPackage resp = ServerConnector.instance().ask(
						new ReqQueryStaff());
				if (resp.header.type == Type.ACK) {
					WirelessOrder.staffs = RespParser.parseQueryStaff(resp);
				} else {
					if (resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
						errMsg = "�ն�û�еǼǵ�����������ϵ������Ա��";
					} else if (resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
						errMsg = "�ն��ѹ��ڣ�����ϵ������Ա��";
					} else {
						errMsg = "����Ա����Ϣʧ�ܣ����������źŻ��������ӡ�";
					}
					throw new IOException(errMsg);
				}
			} catch (IOException e) {
				errMsg = e.getMessage();
			}
			return errMsg;
		}

		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û��� ���Ա����Ϣ����ɹ�������ʾ��¼Dialog��
		 */
		@Override
		protected void onPostExecute(String errMsg) {
			// make the progress dialog disappeared
			_progDialog.dismiss();
			((TextView) findViewById(R.id.username_value)).setText("");
			((TextView) findViewById(R.id.notice)).setText("");
			/**
			 * Prompt user message if any error occurred, otherwise show the
			 * login dialog
			 */
			if (errMsg != null) {
				new AlertDialog.Builder(MainActivity.this).setTitle("��ʾ")
						.setMessage(errMsg).setPositiveButton("ȷ��", null)
						.show();

			} else {
				if (WirelessOrder.staffs == null) {
					new AlertDialog.Builder(MainActivity.this).setTitle("��ʾ")
							.setMessage("û�в�ѯ���κε�Ա����Ϣ�����ڹ����̨�����Ա����Ϣ")
							.setPositiveButton("ȷ��", null).show();
				} else {
					Editor editor = getSharedPreferences(Params.PREFS_NAME,
							Context.MODE_PRIVATE).edit();// ��ȡ�༭��
					editor.putLong(Params.STAFF_PIN, Params.DEF_STAFF_PIN);
					editor.commit();
					showDialog(DIALOG_STAFF_LOGIN);
					if (_isTableUpdate) {
						new QueryTableTask().execute();
					}
				}
			}
		}
	}

	/**
	 * ��̨��Ϣ��Adapter
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

			// ���ݲ�̨�Ĳ�ͬ״̬���ñ���
			if (table.status == Table.TABLE_BUSY) {
				((FrameLayout) view.findViewById(R.id.item1)).setBackgroundResource(R.drawable.av_r39_c15);
				((FrameLayout) view.findViewById(R.id.item4)).setBackgroundResource(R.drawable.av_r42_c15);
			} else {
				((FrameLayout) view.findViewById(R.id.item1)).setBackgroundResource(R.drawable.av_r40_c8);
				((FrameLayout) view.findViewById(R.id.item4)).setBackgroundResource(R.drawable.av_r43_c8);
			}
			// ���ò�̨̨��
			((TextView) view.findViewById(R.id.item3)).setText(Integer.toString(table.aliasID));
			// ���ò�̨����
			((TextView) view.findViewById(R.id.item5)).setText(table.name);

			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					new QueryOrderTask(table).execute();
				}
			});

			view.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					if (table.status == Table.TABLE_BUSY) {
						new AlertDialog.Builder(parent.getContext())
								.setTitle("��ѡ��" + table.aliasID + "�Ų�̨�Ĳ���")
								.setItems(new String[] { "�ĵ�", "ת̨","����" },
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog,	int which) {
												if (which == 0) {
													// jump to change order activity with the order in case of busy
													new QueryOrderTask(table).execute();
												} else if (which == 1) {
													
												} else if( which == 2){
													// jump to Bill activity with the order in case of busy
													new QueryOrderBillTask(table.aliasID,Type.PAY_ORDER).execute();
												}
											}
										}).setNegativeButton("����", null).show();

					} else if (table.status == Table.TABLE_IDLE) {
						new AlertDialog.Builder(parent.getContext())
								.setTitle("��ѡ��" + table.aliasID + "�Ų�̨�Ĳ���")
								.setItems(new String[] { "�µ�" },
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,	int which) {
											if (which == 0) {
												// jump to the order activity with the table parcel in case of idle
												Intent intent = new Intent(MainActivity.this, OrderActivity.class);
												Bundle bundle = new Bundle();
												bundle.putParcelable(TableParcel.KEY_VALUE,	new TableParcel(table));
												intent.putExtras(bundle);
												startActivity(intent);
											}
										}
									})
								.setNegativeButton("����", null).show();
					}
					return true;
				}
			});

			return view;
		}
	}

	/**
	 * ѡ������ʱ������ť����
	 */
//	private void hightLightBtn(Button btn) {
//		// btn.setBackgroundResource(R.drawable.av_r12_c28_2);
//		if (btn == highLightBtn) {
//			return;
//		}
//		if (highLightBtn == null) {
//			highLightBtn = btn;
//			highLightBtn.setBackgroundResource(R.drawable.av_r12_c28_2);
//			return;
//		}
//		highLightBtn.setBackgroundResource(R.drawable.av_bottombtn_selector);
//		highLightBtn.setPadding(0, 0, 0, 0);
//		highLightBtn = btn;
//		highLightBtn.setBackgroundResource(R.drawable.av_r12_c28_2);
//		highLightBtn.setPadding(0, 0, 0, 0);
//
//	}
	
	private void highLightRegionBtn(){
		((Button)findViewById(R.id.bottomFirstBtn)).setBackgroundResource(R.drawable.av_bottombtn_selector);
		((Button)findViewById(R.id.bottomFirstBtn)).setPadding(0, 0, 0, 0);
		((Button)findViewById(R.id.region_1)).setBackgroundResource(R.drawable.av_bottombtn_selector);
		((Button)findViewById(R.id.region_1)).setPadding(0, 0, 0, 0);
		((Button)findViewById(R.id.region_2)).setBackgroundResource(R.drawable.av_bottombtn_selector);
		((Button)findViewById(R.id.region_2)).setPadding(0, 0, 0, 0);
		((Button)findViewById(R.id.region_3)).setBackgroundResource(R.drawable.av_bottombtn_selector);
		((Button)findViewById(R.id.region_3)).setPadding(0, 0, 0, 0);
		((Button)findViewById(R.id.region_4)).setBackgroundResource(R.drawable.av_bottombtn_selector);
		((Button)findViewById(R.id.region_4)).setPadding(0, 0, 0, 0);
		((Button)findViewById(R.id.region_5)).setBackgroundResource(R.drawable.av_bottombtn_selector);
		((Button)findViewById(R.id.region_5)).setPadding(0, 0, 0, 0);
		((Button)findViewById(R.id.region_6)).setBackgroundResource(R.drawable.av_bottombtn_selector);
		((Button)findViewById(R.id.region_6)).setPadding(0, 0, 0, 0);
		((Button)findViewById(R.id.region_7)).setBackgroundResource(R.drawable.av_bottombtn_selector);
		((Button)findViewById(R.id.region_7)).setPadding(0, 0, 0, 0);
		((Button)findViewById(R.id.region_8)).setBackgroundResource(R.drawable.av_bottombtn_selector);
		((Button)findViewById(R.id.region_8)).setPadding(0, 0, 0, 0);
		((Button)findViewById(R.id.region_9)).setBackgroundResource(R.drawable.av_bottombtn_selector);
		((Button)findViewById(R.id.region_9)).setPadding(0, 0, 0, 0);
		((Button)findViewById(R.id.region_10)).setBackgroundResource(R.drawable.av_bottombtn_selector);
		((Button)findViewById(R.id.region_10)).setPadding(0, 0, 0, 0);

		if(_curRegion < 0){
			((Button)findViewById(R.id.bottomFirstBtn)).setBackgroundResource(R.drawable.av_r12_c28_2);
			
		}else if(_curRegion == Region.REGION_1){			
			((Button)findViewById(R.id.region_1)).setBackgroundResource(R.drawable.av_r12_c28_2);
			
		}else if(_curRegion == Region.REGION_2){
			((Button)findViewById(R.id.region_2)).setBackgroundResource(R.drawable.av_r12_c28_2);
			
		}else if(_curRegion == Region.REGION_3){
			((Button)findViewById(R.id.region_3)).setBackgroundResource(R.drawable.av_r12_c28_2);
			
		}else if(_curRegion == Region.REGION_4){
			((Button)findViewById(R.id.region_4)).setBackgroundResource(R.drawable.av_r12_c28_2);
			
		}else if(_curRegion == Region.REGION_5){
			((Button)findViewById(R.id.region_5)).setBackgroundResource(R.drawable.av_r12_c28_2);
			
		}else if(_curRegion == Region.REGION_6){
			((Button)findViewById(R.id.region_6)).setBackgroundResource(R.drawable.av_r12_c28_2);
			
		}else if(_curRegion == Region.REGION_7){
			((Button)findViewById(R.id.region_7)).setBackgroundResource(R.drawable.av_r12_c28_2);
			
		}else if(_curRegion == Region.REGION_8){
			((Button)findViewById(R.id.region_8)).setBackgroundResource(R.drawable.av_r12_c28_2);
			
		}else if(_curRegion == Region.REGION_9){
			((Button)findViewById(R.id.region_9)).setBackgroundResource(R.drawable.av_r12_c28_2);
			
		}else if(_curRegion == Region.REGION_10){
			((Button)findViewById(R.id.region_10)).setBackgroundResource(R.drawable.av_r12_c28_2);
			
		}
	}
	
	
	/**
	 * ִ�������Ӧ��̨���˵���Ϣ 
	 */
	private class QueryOrderBillTask extends AsyncTask<Void, Void, String>{

		private ProgressDialog _progDialog;
		private int _tableID;
		private Order _order;
		private int _type = Type.UPDATE_ORDER;;
		
		QueryOrderBillTask(int tableID, int type){
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
				 if(_type == Type.PAY_ORDER){
					//jump to the pay order activity
					Intent intent = new Intent(MainActivity.this, BillActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(_order));
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		}
		
	}
}
