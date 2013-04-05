package com.wireless.pad;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.PDepartment;
import com.wireless.protocol.PKitchen;
import com.wireless.view.PickFoodListView;
import com.wireless.view.TempListView;

public class PickFoodActivity extends TabActivity implements
   PickFoodListView.OnFoodPickedListener {

	public static final String PICK_FOOD_ACTION = "com.wireless.pad.PickFoodActivity.PickFood";
	public static final String PICK_TASTE_ACTION = "com.wireless.pad.PickFoodActivity.PickTaste";

	
	private List<PKitchen> _validKitchens;
	private List<PDepartment> _validDepts;

	private final static String TAG_NUMBER = "number";
	private final static String TAG_KITCHEN = "kitchen";
	private final static String TAG_PINYIN = "pinyin";
	private final static String TAG_OCCASIONAL = "occasional";


	private ArrayList<OrderFood> _pickFoods = new ArrayList<OrderFood>();
	private TabHost _tabHost;
	private List<Food> _filterKitchenFoods;
	private TempListView _tempLstView;
	private TextView _centerTxtView;  

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.table);
		
		init();

		// construct the tab host
		_tabHost = getTabHost();

		// ���Tab
		TabSpec spec = _tabHost
				.newTabSpec(TAG_NUMBER)
				.setIndicator(
						createTabIndicator("���", R.drawable.number_selector))
				.setContent(new TabHost.TabContentFactory() {
					@Override
					public View createTabContent(String arg0) {
						return LayoutInflater.from(PickFoodActivity.this)
								.inflate(R.layout.number, null);
					}
				});
		_tabHost.addTab(spec);

		// �ֳ�Tab
		spec = _tabHost
				.newTabSpec(TAG_KITCHEN)
				.setIndicator(
						createTabIndicator("�ֳ�", R.drawable.kitchen_selector))
				.setContent(new TabHost.TabContentFactory() {
					@Override
					public View createTabContent(String arg0) {
						return LayoutInflater.from(PickFoodActivity.this)
								.inflate(R.layout.kitchen, null);
					}
				});
		_tabHost.addTab(spec);

		// ƴ��Tab
		spec = _tabHost
				.newTabSpec(TAG_PINYIN)
				.setIndicator(
						createTabIndicator("ƴ��", R.drawable.pinyin_selector))
				.setContent(new TabHost.TabContentFactory() {
					@Override
					public View createTabContent(String arg0) {
						return LayoutInflater.from(PickFoodActivity.this)
								.inflate(R.layout.pinyin, null);
					}
				});

		_tabHost.addTab(spec);

		// ��ʱ��Tab
		spec = _tabHost
				.newTabSpec(TAG_OCCASIONAL)
				.setIndicator(
						createTabIndicator("��ʱ��",
								R.drawable.occasional_selector))
				.setContent(new TabHost.TabContentFactory() {
					@Override
					public View createTabContent(String arg0) {
						return LayoutInflater.from(PickFoodActivity.this)
								.inflate(R.layout.temp, null);
					}
				});
		_tabHost.addTab(spec);

		/**
		 * Tab�л�ʱ������Ӧ��Adapter����ʾ��ͬ�ĵ�˷�ʽ
		 */
		_tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tag) {
				Editor editor = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();
				if (tag == TAG_NUMBER) {
					editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_NUMBER);
					setupNumberView();

				} else if (tag == TAG_KITCHEN) {
					editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_KITCHEN);
					setupKitchenView();

				} else if (tag == TAG_PINYIN) {
					editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_PINYIN);
					setupPinyinView();

				} else if (tag == TAG_OCCASIONAL) {
					setupTempView();
				}
				editor.commit();
			}
		});

		/**
		 * �����ϴα���ļ�¼���л�����Ӧ�ĵ�˷�ʽ
		 */
		int lastPickCate = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).getInt(Params.LAST_PICK_CATE, Params.PICK_BY_KITCHEN);
		if(lastPickCate == Params.PICK_BY_NUMBER){
			_tabHost.setCurrentTabByTag(TAG_NUMBER);
			setupNumberView();
			
		}else if(lastPickCate == Params.PICK_BY_KITCHEN){
			_tabHost.setCurrentTabByTag(TAG_KITCHEN);
			
		}else if(lastPickCate == Params.PICK_BY_PINYIN){
			_tabHost.setCurrentTabByTag(TAG_PINYIN);

		}else{
			_tabHost.setCurrentTabByTag(TAG_NUMBER);
		}
	}
	

	@Override
	public void onDestroy(){
		((WindowManager)getSystemService(Context.WINDOW_SERVICE)).removeView(_centerTxtView);
		super.onDestroy();
	}
	
	
	/**
	 * ����ʱ���µ��Ʒ��List���ص���һ��Activity
	 */
//	@Override
//	public void onBackPressed() {
//		if (_tempLstView != null) {
//			_pickFoods.addAll(_tempLstView.getSourceData());
//		}
//
//		Intent intent = new Intent();
//		Bundle bundle = new Bundle();
//		Order tmpOrder = new Order();
//		tmpOrder.foods = _pickFoods.toArray(new OrderFood[_pickFoods.size()]);
//		bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(tmpOrder));
//		intent.putExtras(bundle);
//		setResult(RESULT_OK, intent);
//		super.onBackPressed();
//	}

	/**
	 * Create the tab indicator
	 * 
	 * @param text
	 * @param drawable
	 * @return
	 */
	private View createTabIndicator(String text, int drawable) {
		View view = LayoutInflater.from(_tabHost.getContext()).inflate(
				R.layout.tb_bg, null);
//		((TextView) view.findViewById(R.id.tabsText)).setText(text);
//		((ImageView) view.findViewById(R.id.icon)).setImageResource(drawable);
		
		android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		if(text.equals("���")){
			lp.setMargins(15, 10, 0, 10);
			((Button)view.findViewById(R.id.tabicon)).setLayoutParams(lp);
		}
		if(text.equals("��ʱ��")){
			lp.setMargins(0, 10, 15, 10);
			((Button)view.findViewById(R.id.tabicon)).setLayoutParams(lp);
		}
		((Button)view.findViewById(R.id.tabicon)).setText(text);
		((Button)view.findViewById(R.id.tabicon)).setBackgroundResource(drawable);
		((Button)view.findViewById(R.id.tabicon)).setClickable(false);
		return view;
	}

	/**
	 * ͨ��"���"��"�ֳ�"��"ƴ��"��ʽѡ�в�Ʒ�� ����Ʒ���浽List�У��˳�ʱ����List��Ϊ������ص���һ��Activity
	 * 
	 * @param food
	 *            ѡ�в�Ʒ����Ϣ
	 */
	@Override
	public void onPicked(OrderFood food) {
		_pickFoods.add(food);

		/**
		 * ����Ӳ�Ʒ��broadcast����ʽ����
		 */
		Order tmpOrder = new Order();
		tmpOrder.setOrderFoods(_pickFoods.toArray(new OrderFood[_pickFoods.size()]));
		Bundle bundle = new Bundle();
		bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(tmpOrder));
		Intent intent = new Intent().setAction(PICK_FOOD_ACTION);
		intent.putExtras(bundle);
	    sendBroadcast(intent);		
	    
		if (_tabHost.getCurrentTabTag() == TAG_NUMBER) {
			(((EditText) findViewById(R.id.filterNumEdtTxt))).setText("");
		} else if (_tabHost.getCurrentTabTag() == TAG_KITCHEN) {
			((EditText) findViewById(R.id.filterKitchenEdtTxt)).setText("");
		} else if (_tabHost.getCurrentTabTag() == TAG_PINYIN) {
			((EditText) findViewById(R.id.filterPinyinEdtTxt)).setText("");
		}	
	    _pickFoods.clear();
	    
	}

	/**
	 * ͨ��"���"��"�ֳ�"��"ƴ��"��ʽѡ�в�Ʒ�� ����Ʒ���浽List�У�����ת����ζActivityѡ���ζ
	 * 
	 * @param food
	 *            ѡ�в�Ʒ����Ϣ
	 */
	@Override
	public void onPickedWithTaste(OrderFood food) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(food));
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setAction(PICK_TASTE_ACTION);
		intent.putExtras(bundle);
		sendBroadcast(intent);				
	}

	/**
	 * ���ñ��ɸѡ��View
	 */
	private void setupNumberView() {

		//��������
		final EditText filterNumEdtTxt = (EditText)findViewById(R.id.filterNumEdtTxt);
		filterNumEdtTxt.setText("");

		// ��Ų���
		final LinearLayout numberSidebar = (LinearLayout) findViewById(R.id.NumsideIndex);
		//numberSidebar.setBackgroundColor(0xfbfdfe);
		numberSidebar.setOrientation(LinearLayout.VERTICAL);
		numberSidebar.removeAllViews();
		numberSidebar.setBackgroundResource(0);

		/**
		 * ������ָ����ʱ��������Ӧ������
		 */
		numberSidebar.setOnTouchListener(new View.OnTouchListener() {			
			
			private int _prePos = -1;
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//Log.i(v.toString(), event.getAction() + ", " + "x=" + event.getX() + ", y=" + event.getY());
				int curPos = 0;
				if(event.getY() < numberSidebar.getChildAt(numberSidebar.getChildCount() - 1).getBottom()){
					
					curPos = (new Float(event.getY() / numberSidebar.getChildAt(0).getHeight()).intValue()) % numberSidebar.getChildCount();
					
					if(event.getAction() == MotionEvent.ACTION_DOWN){
						//show the popup number in the center of screen 
						_centerTxtView.setVisibility(View.VISIBLE);
						_centerTxtView.setText(((TextView)numberSidebar.getChildAt(curPos)).getText());
						numberSidebar.setBackgroundResource(R.drawable.side_bar_bg);

						
					}else if(event.getAction() == MotionEvent.ACTION_MOVE){
						if(curPos != _prePos){
							//show the popup number in the center of screen 
							_centerTxtView.setVisibility(View.VISIBLE);
							_centerTxtView.setText(((TextView)numberSidebar.getChildAt(curPos)).getText());
						}
						
					}else if(event.getAction() == MotionEvent.ACTION_UP){
						_centerTxtView.setVisibility(View.INVISIBLE);
						filterNumEdtTxt.append(((TextView)numberSidebar.getChildAt(curPos)).getText().toString());
//						filterNumEdtTxt.setSelection(filterNumEdtTxt.getText().toString().length());	
						numberSidebar.setBackgroundResource(0);

					}
					
					_prePos = curPos;
					
					return true;
	
				}else{
					_centerTxtView.setVisibility(View.INVISIBLE);
					numberSidebar.setBackgroundResource(0);
					return false;
				}			
			}
		});

		/**
		 * ��Ų������0-9������
		 */
		for(int i = 0; i < 10; i++) {
			final TextView tv = new TextView(PickFoodActivity.this);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
			MarginLayoutParams ml = new MarginLayoutParams(MarginLayoutParams.FILL_PARENT, MarginLayoutParams.WRAP_CONTENT);
			tv.setLayoutParams(ml);
			tv.setText(Integer.toString(i));
			numberSidebar.addView(tv);
			tv.setGravity(Gravity.CENTER_HORIZONTAL);
		}
		
		/**
		 * ���������ɾ��Button
		 */
		((ImageView)findViewById(R.id.numberdelete)).setOnClickListener(new View.OnClickListener() {			
			@Override
		    public void onClick(View v) {
				//����ɾ��
				String s = filterNumEdtTxt.getText().toString();
				if(s.length() > 0){
					filterNumEdtTxt.setText(s.substring(0, s.length() - 1));
					filterNumEdtTxt.setSelection(filterNumEdtTxt.getText().length());					
				}
			}
		});
		

		final PickFoodListView pickLstView = (PickFoodListView)findViewById(R.id.pickByNumLstView);
		pickLstView.setNumColumns(4);
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods,	PickFoodListView.TAG_NUM);
		pickLstView.setFoodPickedListener(this);
	

		/**
		 * ��ƷList����ʱ���������
		 */
		pickLstView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(
								filterNumEdtTxt.getWindowToken(), 0);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});

		/**
		 * ����Ž��в�Ʒ��ɸѡ
		 */
		filterNumEdtTxt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.toString().length() != 0) {
					List<Food> filterFoods = new ArrayList<Food>();
					for(Food f : WirelessOrder.foodMenu.foods){
						if (String.valueOf(f.getAliasId()).startsWith(s.toString().trim())) {
							filterFoods.add(f);
						}
					}
					pickLstView.notifyDataChanged(filterFoods, PickFoodListView.TAG_NUM);

				} else {
					pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods,
							PickFoodListView.TAG_NUM);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	/**
	 * ���÷ֳ�ɸѡ��View
	 */
	private void setupKitchenView() {

		// �ֳ�����
		final LinearLayout kitchenSidebar =(LinearLayout) findViewById(R.id.sideIndex);
		final PickFoodListView pickLstView = (PickFoodListView) findViewById(R.id.pickByKitchenLstView);
		pickLstView.setNumColumns(4);
		// �������
		kitchenSidebar.removeAllViews();
		//kitchenSidebar.setBackgroundColor(0xfbfdfe);
		kitchenSidebar.setBackgroundResource(0);

		
		/**
		 * ������ָ����ʱ����ʾ��Ӧ�Ĳ���
		 */
		kitchenSidebar.setOnTouchListener(new View.OnTouchListener() {			
			
			private int _prePos = -1;
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//Log.i(v.toString(), event.getAction() + ", " + "x=" + event.getX() + ", y=" + event.getY());
				int curPos = 0;
				if(event.getY() < kitchenSidebar.getChildAt(kitchenSidebar.getChildCount() - 1).getBottom()){
					
					curPos = (new Float(event.getY() / kitchenSidebar.getChildAt(0).getHeight()).intValue()) % kitchenSidebar.getChildCount();
					
					if(event.getAction() == MotionEvent.ACTION_DOWN){
						//show the department in the center of screen 
						_centerTxtView.setVisibility(View.VISIBLE);
						_centerTxtView.setText(((TextView)kitchenSidebar.getChildAt(curPos)).getText());
						kitchenSidebar.setBackgroundResource(R.drawable.side_bar_bg);

						
					}else if(event.getAction() == MotionEvent.ACTION_MOVE){
						if(curPos != _prePos){
							//show the department in the center of screen 
							_centerTxtView.setVisibility(View.VISIBLE);
							_centerTxtView.setText(((TextView)kitchenSidebar.getChildAt(curPos)).getText());
						}
						
					}else if(event.getAction() == MotionEvent.ACTION_UP){
						kitchenSidebar.setBackgroundResource(0);
						//disappear the center text view
						_centerTxtView.setVisibility(View.INVISIBLE);
						/**
						 * ���ݲ���ѡ�еĲ��ţ�ɸѡ����Ӧ�Ĳ��źͳ���
						 */
						List<PDepartment> dept = new ArrayList<PDepartment>();
						int deptID = ((Integer)((TextView)kitchenSidebar.getChildAt(curPos)).getTag());
						for (int i = 0; i < _validDepts.size(); i++) {
							if (_validDepts.get(i).getId() == deptID) {
								dept.add(_validDepts.get(i));
								break;
							}
						}
						List<PKitchen> kitchens = new ArrayList<PKitchen>();
						for (int i = 0; i < _validKitchens.size(); i++) {
							if (_validKitchens.get(i).getDept().getId() == deptID) {
								kitchens.add(_validKitchens.get(i));
							}
						}
						List<List<PKitchen>> kitchenChild = new ArrayList<List<PKitchen>>();
						kitchenChild.add(kitchens);
						new KitchenSelectDialog(pickLstView, dept, kitchenChild).show();											
					}
					
					_prePos = curPos;
					
					return true;
	
				}else{
					_centerTxtView.setVisibility(View.INVISIBLE);
					kitchenSidebar.setBackgroundResource(0);
					return false;
				}			
			}
		});
		
		// Ϊ�������ɸѡ����
		for (PDepartment d : _validDepts) {
			TextView tv = new TextView(this);
			tv.setText(d.getName().subSequence(0, 2));
			tv.setTag(Integer.valueOf(d.getId()));
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			tv.setBackgroundDrawable(null);
			tv.setPadding(0, 5, 0, 5);
			tv.setTextColor(Color.GRAY);
			kitchenSidebar.addView(tv);
		}

		RelativeLayout filterKitchen = (RelativeLayout) findViewById(R.id.filterKitchenRelaLayout);
		final EditText filterKitEdtTxt = (EditText) findViewById(R.id.filterKitchenEdtTxt);
		filterKitEdtTxt.setText("");
		// ��ʼ����ʱ�����Ĭ����ʾ�ĳ�����Ϣ
		TextView ketchenName = (TextView)findViewById(R.id.Spinner01);
		ketchenName.setText("ȫ��");
		_filterKitchenFoods = new ArrayList<Food>(WirelessOrder.foodMenu.foods);
		pickLstView.notifyDataChanged(_filterKitchenFoods, PickFoodListView.TAG_PINYIN);
		pickLstView.setFoodPickedListener(this);


		/**
		 * ������ʽ�µķ���Button
		 */
		pickLstView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(
								filterKitEdtTxt.getWindowToken(), 0);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});
		

		/**
		 * ��ƷList����ʱ���������
		 */
		filterKitEdtTxt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.toString().length() != 0) {
					ArrayList<Food> filterFoods = new ArrayList<Food>();
					for(Food food : _filterKitchenFoods){
						if(String.valueOf(food.getPinyin()).toLowerCase().contains(s.toString().trim().toLowerCase())
								|| food.getName().contains(s.toString().trim())) {
							filterFoods.add(food);
						}
					}

					pickLstView.notifyDataChanged(filterFoods, PickFoodListView.TAG_PINYIN);

				} else {
					pickLstView.notifyDataChanged(_filterKitchenFoods, PickFoodListView.TAG_PINYIN);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		/**
		 * �ڷֳ�ѡ��ҳ���а�ƴ�����в�Ʒ��ɸѡ
		 */
		filterKitchen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				/**
				 * ��������ѡ��ҳ�沢ɸѡ����Ӧ�Ĳ�Ʒ
				 */
				List<List<PKitchen>> kitchenChild = new ArrayList<List<PKitchen>>();
				for (int i = 0; i < _validDepts.size(); i++) {
					List<PKitchen> kitchens = new ArrayList<PKitchen>();
					for (int j = 0; j < _validKitchens.size(); j++) {
						if (_validKitchens.get(j).getDept().getId() == _validDepts.get(i).getId()) {
							kitchens.add(_validKitchens.get(j));
						}
					}
					kitchenChild.add(kitchens);
				}
				new KitchenSelectDialog(pickLstView, _validDepts, kitchenChild)
						.show();
			}
		});
	}

	/**
	 * ����ƴ��ɸѡ��View
	 */
	private void setupPinyinView() {
		// ƴ�������
		final EditText filterPinyinEdtTxt = (EditText)findViewById(R.id.filterPinyinEdtTxt);
		filterPinyinEdtTxt.setText("");

		// ƴ������
		final LinearLayout pinyinSidebar = (LinearLayout) findViewById(R.id.PinyinsideIndex); 
		pinyinSidebar.setBackgroundColor(0xfbfdfe);
		pinyinSidebar.setOrientation(LinearLayout.VERTICAL);
		pinyinSidebar.removeAllViews();
		pinyinSidebar.setBackgroundResource(0);

		
		pinyinSidebar.setOnTouchListener(new View.OnTouchListener() {			
			
			private int _prePos = -1;
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//Log.i(v.toString(), event.getAction() + ", " + "x=" + event.getX() + ", y=" + event.getY());
				int curPos = 0;
				if(event.getY() < pinyinSidebar.getChildAt(pinyinSidebar.getChildCount() - 1).getBottom()){
					
					curPos = (new Float(event.getY() / pinyinSidebar.getChildAt(0).getHeight()).intValue()) % pinyinSidebar.getChildCount();
					
					if(event.getAction() == MotionEvent.ACTION_DOWN){
						//show the department in the center of screen 
						_centerTxtView.setVisibility(View.VISIBLE);
						_centerTxtView.setText(((TextView)pinyinSidebar.getChildAt(curPos)).getText());
						pinyinSidebar.setBackgroundResource(R.drawable.side_bar_bg);

						
					}else if(event.getAction() == MotionEvent.ACTION_MOVE){
						if(curPos != _prePos){
							//show the department in the center of screen 
							_centerTxtView.setVisibility(View.VISIBLE);
							_centerTxtView.setText(((TextView)pinyinSidebar.getChildAt(curPos)).getText());
						}
						
					}else if(event.getAction() == MotionEvent.ACTION_UP){
						//disappear the center text view
						_centerTxtView.setVisibility(View.INVISIBLE);
						filterPinyinEdtTxt.append(((TextView)pinyinSidebar.getChildAt(curPos)).getText().toString());
						filterPinyinEdtTxt.setSelection(filterPinyinEdtTxt.getText().toString().length());		
						pinyinSidebar.setBackgroundResource(0);
					}
					
					_prePos = curPos;
					
					return true;
	
				}else{
					_centerTxtView.setVisibility(View.INVISIBLE);
					pinyinSidebar.setBackgroundResource(0);
					return false;
				}			
			}
		});
		

		/**
		 * ƴ��������ʾA-Z����ĸ
		 */
		for(char c = 'A'; c <= 'Z'; c++) {
			final TextView tv = new TextView(PickFoodActivity.this);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
			tv.setGravity(Gravity.CENTER_HORIZONTAL);
			tv.setText(Character.toString(c));
			pinyinSidebar.addView(tv);
		}
		
		/**
		 * ƴ��������ɾ��Button��Ӧ�¼�
		 */
		((ImageView)findViewById(R.id.pinyindelete)).setOnClickListener(new View.OnClickListener() {					
			@Override
		    public void onClick(View v) {
				//����ɾ��
				String s = filterPinyinEdtTxt.getText().toString();
				if(s.length() > 0){
					filterPinyinEdtTxt.setText(s.substring(0, s.length() - 1));
					filterPinyinEdtTxt.setSelection(filterPinyinEdtTxt.getText().length());					
				}
			}
		});


		
		final PickFoodListView pickLstView = (PickFoodListView) findViewById(R.id.pickByPinyinLstView);
		pickLstView.setNumColumns(4);
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods,	PickFoodListView.TAG_PINYIN);
		pickLstView.setFoodPickedListener(this);
		/**
		 * ƴ��List����ʱ���������
		 */
		pickLstView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(
								filterPinyinEdtTxt.getWindowToken(), 0);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});

		/**
		 * ��ƴ�����в�Ʒ��ɸѡ
		 */
		filterPinyinEdtTxt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.toString().length() != 0) {
					ArrayList<Food> filterFoods = new ArrayList<Food>();
					for(Food f : WirelessOrder.foodMenu.foods){
						if (f.getPinyin() != null) {
							if (f.getPinyin().toLowerCase().contains(s.toString().trim().toLowerCase()) ||
								f.getPinyinShortcut().toLowerCase().contains(s.toString().trim().toLowerCase()) ||
								f.getName().contains(s.toString().trim().toLowerCase())) 
							{
								filterFoods.add(f);
							}
						} else {
							filterFoods.add(f);
						}
					}
					pickLstView.notifyDataChanged(filterFoods, PickFoodListView.TAG_PINYIN);

				} else {
					pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods, PickFoodListView.TAG_PINYIN);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}


	/**
	 * ������ʱ�˵�View
	 */
	private void setupTempView() {

		_tempLstView = (TempListView) findViewById(R.id.tempListView);
		_tempLstView.notifyDataChanged();

		//ȷ�������ʱ��Button
		((Button)findViewById(R.id.addTmpFoodBtn)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
			
				if(_tempLstView != null){
					List<OrderFood> tmpFoods = _tempLstView.removeValidFoods();
					
					if(tmpFoods.size() != 0){
						//�����ʱ��Ʒ����ƷList�У��������ʱ��Ʒ�б�
						_pickFoods.addAll(tmpFoods);
						
						//֪ͨ�µ��View������Ϣ
						Order tmpOrder = new Order();
						tmpOrder.setOrderFoods(_pickFoods.toArray(new OrderFood[_pickFoods.size()]));
						Bundle bundle = new Bundle();
						bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(tmpOrder));
						Intent intent = new Intent().setAction(PICK_FOOD_ACTION);
						intent.putExtras(bundle);
					    sendBroadcast(intent);	
					    _pickFoods.clear();
					    
					}else{					
						Toast.makeText(PickFoodActivity.this, "�Բ���, ���������ʱ����Ϣ����ȷ", 0).show();
					}
				}
			}
		});

		// ��ʱ�����
		((ImageView) findViewById(R.id.add))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_tempLstView.addTemp();
					}
		});
		
		/**
		 * list������ʱ�����������
		 */
		_tempLstView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				InputMethodManager input = (InputMethodManager) PickFoodActivity.this
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				input.hideSoftInputFromWindow(view.getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});
	}

	/**
	 * �ֳ�ҳ���г���ѡ���Dialog
	 */
	private class KitchenSelectDialog extends Dialog {

		private List<PDepartment> _deptParent;
		private List<List<PKitchen>> _kitchenChild;

		KitchenSelectDialog(final PickFoodListView foodLstView,	List<PDepartment> depts, List<List<PKitchen>> kitchens) {
			super(PickFoodActivity.this, R.style.FullHeightDialog);
			_deptParent = depts;
			_kitchenChild = kitchens;

			View dialogContent = View.inflate(PickFoodActivity.this, R.layout.expander_list_view, null);
			setTitle("��ѡ�����");
			ExpandableListView kitchenLstView = (ExpandableListView) dialogContent.findViewById(R.id.kitchenSelectLstView);
			// kitchenLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));

			// ����ListView��Adaptor
			kitchenLstView.setAdapter(new BaseExpandableListAdapter() {

				@Override
				public int getGroupCount() {
					return _deptParent.size();
				}

				@Override
				public int getChildrenCount(int groupPosition) {
					return _kitchenChild.get(groupPosition).size();
				}

				@Override
				public Object getGroup(int groupPosition) {
					return _deptParent.get(groupPosition);
				}

				@Override
				public Object getChild(int groupPosition, int childPosition) {
					return _kitchenChild.get(groupPosition).get(childPosition);
				}

				@Override
				public long getGroupId(int groupPosition) {
					return groupPosition;
				}

				@Override
				public long getChildId(int groupPosition, int childPosition) {
					return childPosition;
				}

				@Override
				public boolean hasStableIds() {
					return false;
				}

				@Override
				public View getGroupView(int groupPosition, boolean isExpanded,
						View convertView, ViewGroup parent) {
					View view;
					if (convertView != null) {
						view = convertView;
					} else {
						view = View.inflate(PickFoodActivity.this,
								R.layout.grounp, null);
					}

					((TextView) view.findViewById(R.id.mygroup))
							.setText(_deptParent.get(groupPosition).getName());

					if (isExpanded) {
						((ImageView) view.findViewById(R.id.kitchenarrow))
								.setBackgroundResource(R.drawable.point);
					} else {
						((ImageView) view.findViewById(R.id.kitchenarrow))
								.setBackgroundResource(R.drawable.point02);

					}
					return view;
				}

				@Override
				public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
					View view;
					if (convertView != null) {
						view = convertView;
					} else {
						view = View.inflate(PickFoodActivity.this, R.layout.child, null);
					}
					((TextView) view.findViewById(R.id.mychild)).setText(_kitchenChild.get(groupPosition).get(childPosition).getName());
					return view;
				}

				@Override
				public boolean isChildSelectable(int groupPosition,	int childPosition) {
					return true;
				}
			});

			// ����չ�����еĶ����˵�
			for (int i = 0; i < _deptParent.size(); i++) {
				kitchenLstView.expandGroup(i);
			}

			/**
			 * ѡ��ĳ��������ɸѡ����Ӧ�Ĳ�Ʒ��������ListView
			 */
			kitchenLstView.setOnChildClickListener(new OnChildClickListener() {

				public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
					
					PKitchen selectedKitchen = _kitchenChild.get(groupPosition).get(childPosition);
					
					_filterKitchenFoods.clear();
					for(Food f : WirelessOrder.foodMenu.foods){
						if (f.getKitchen().getAliasId() == selectedKitchen.getAliasId()) {
							_filterKitchenFoods.add(f);
						}
					}
					// ѡ�г�������¸�ֵ
					((TextView)PickFoodActivity.this.findViewById(R.id.Spinner01)).setText(_kitchenChild.get(groupPosition).get(childPosition).getName());

					foodLstView.notifyDataChanged(_filterKitchenFoods,
												  PickFoodListView.TAG_PINYIN);
					dismiss();
					return true;
				}
			});

			/**
			 * ����ѡ��Dialog�ķ���Button��Ӧ�¼�
			 */
			((Button)dialogContent.findViewById(R.id.dialog_back_btn)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
			
			setContentView(dialogContent);
		}

	}

	private void init() {
		
		/**
		 * �����м���ʾ��TextView
		 */
		WindowManager wndMgr = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflate = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);        
        _centerTxtView = (TextView)inflate.inflate(R.layout.list_position, null);
        wndMgr.addView(_centerTxtView, new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                													  WindowManager.LayoutParams.TYPE_APPLICATION,
                													  WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                													  | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                													  PixelFormat.TRANSLUCENT));
        _centerTxtView.setVisibility(View.INVISIBLE);

//        /**
//		 * �����в�Ʒ���а�������Ž�������
//		 */
//		Food[] tmpFoods = new Food[WirelessOrder.foodMenu.foods.length];
//		System.arraycopy(WirelessOrder.foodMenu.foods, 0, tmpFoods, 0,
//				WirelessOrder.foodMenu.foods.length);
//		Arrays.sort(tmpFoods, new Comparator<Food>() {
//			@Override
//			public int compare(Food food1, Food food2) {
//				if (food1.getKitchen().getAliasId()
//						> food2.getKitchen().getAliasId()) {
//					return 1;
//				} else if (food1.getKitchen().getAliasId() < food2.getKitchen().getAliasId()) {
//					return -1;
//				} else {
//					return 0;
//				}
//			}
//		});
//
//		/**
//		 * ʹ�ö��ֲ����㷨ɸѡ���в�Ʒ�ĳ���
//		 */
//		_validKitchens = new ArrayList<PKitchen>();
//		for (int i = 0; i < WirelessOrder.foodMenu.kitchens.length; i++) {
//			Food keyFood = new Food();
//			keyFood.getKitchen().setAliasId(WirelessOrder.foodMenu.kitchens[i].getAliasId());
//			int index = Arrays.binarySearch(tmpFoods, keyFood,
//					new Comparator<Food>() {
//
//						public int compare(Food food1, Food food2) {
//							if (food1.getKitchen().getAliasId()> food2.getKitchen().getAliasId()) {
//								return 1;
//							} else if (food1.getKitchen().getAliasId() < food2.getKitchen().getAliasId()) {
//								return -1;
//							} else {
//								return 0;
//							}
//						}
//					});
//
//			if (index >= 0) {
//				_validKitchens.add(WirelessOrder.foodMenu.kitchens[i]);
//			}
//		}
//
//		/**
//		 * ɸѡ���в�Ʒ�Ĳ���
//		 */
//		_validDepts = new ArrayList<PDepartment>();
//		for (int i = 0; i < WirelessOrder.foodMenu.depts.length; i++) {
//			for (int j = 0; j < _validKitchens.size(); j++) {
//				if (WirelessOrder.foodMenu.depts[i].getId() == _validKitchens.get(j).getDept().getId()) {
//					_validDepts.add(WirelessOrder.foodMenu.depts[i]);
//					break;
//				}
//			}
//		}
		
		_validKitchens = WirelessOrder.foodMenu.kitchens;
		_validDepts = WirelessOrder.foodMenu.depts;
	}

	
}
