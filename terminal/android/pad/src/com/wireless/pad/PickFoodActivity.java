package com.wireless.pad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
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
import com.wireless.common.WirelessOrder;
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.view.TempListView;
import com.wireless.view.PickFoodListView;

public class PickFoodActivity extends TabActivity implements
   PickFoodListView.OnFoodPickedListener, OnGestureListener {

	private ArrayList<Kitchen> _validKitchens;
	private ArrayList<Department> _validDepts;

	private final static String TAG_NUMBER = "number";
	private final static String TAG_KITCHEN = "kitchen";
	private final static String TAG_PINYIN = "pinyin";
	private final static String TAG_OCCASIONAL = "occasional";


	private static ArrayList<OrderFood> _pickFoods = new ArrayList<OrderFood>();
	private TabHost _tabHost;
	private GestureDetector _detector;
	boolean dialogTag = false;
	private List<Food> _filterFoods;
	private TempListView _tempLstView;
	private TextView _centerTxtView;
	
  

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.table);
		_detector = new GestureDetector(this);

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
		 * Tab�л�ʱ����Ӧ��Adapter����ʾ��ͬ�ĵ�˷�ʽ
		 */
		_tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tag) {
				if (tag == TAG_NUMBER) {
					setupNumberView();

				} else if (tag == TAG_KITCHEN) {
					setupKitchenView();

				} else if (tag == TAG_PINYIN) {
					setupPinyinView();

				} else if (tag == TAG_OCCASIONAL) {
					setTempView();
				}
			}
		});

		_tabHost.setCurrentTabByTag(TAG_NUMBER);
		setupNumberView();
	}
	
	/**
	 * ��������ʱ���õ��б�ֵ
	 * */
     public static void onResume(List<OrderFood> pickFoods) {
		_pickFoods.clear();	
		_pickFoods = (ArrayList<OrderFood>) pickFoods;
		}
		
	
	
	

	@Override
	public void onDestroy(){
		((WindowManager)getSystemService(Context.WINDOW_SERVICE)).removeView(_centerTxtView);
		super.onDestroy();
	}
	
	/**
	 * ����ʱ���µ��Ʒ��List���ص���һ��Activity
	 */
	@Override
	public void onBackPressed() {
		if (_tempLstView != null) {
			_pickFoods.addAll(_tempLstView.getSourceData());
		}

		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		Order tmpOrder = new Order();
		tmpOrder.foods = _pickFoods.toArray(new OrderFood[_pickFoods.size()]);
		bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(tmpOrder));
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		super.onBackPressed();
	}

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
		((TextView) view.findViewById(R.id.tabsText)).setText(text);
		((ImageView) view.findViewById(R.id.icon)).setImageResource(drawable);
		return view;
	}

	/**
	 * ͨ��"���"��"�ֳ�"��"ƴ��"��ʽѡ�в�Ʒ�� ����Ʒ���浽List�У��˳�ʱ����List��Ϊ���ص���һ��Activity
	 * 
	 * @param food
	 *            ѡ�в�Ʒ����Ϣ
	 */
	@Override
	public void onPicked(OrderFood food) {
		addFood(food);
		
		Intent intent = new Intent();
		//����action
	    intent.setAction(MyBroadcastReceiver.ACTION);	
		Bundle bundle = new Bundle();
		Order tmpOrder = new Order();
		tmpOrder.foods = _pickFoods.toArray(new OrderFood[_pickFoods.size()]);
		bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(tmpOrder));
		intent.putExtras(bundle);
		//���͹㲥
	    sendBroadcast(intent);

		
	}

	/**
	 * ͨ��"���"��"�ֳ�"��"ƴ��"��ʽѡ�в�Ʒ�� ����Ʒ���浽List�У�����ת����ζActivityѡ���ζ
	 * 
	 * @param food
	 *            ѡ�в�Ʒ����Ϣ
	 */
	@Override
	public void onPickedWithTaste(OrderFood food) {
		//����ζ��ʱ����ִ�����һ��˵�����Ȼ���ٰѵ�ǰ����Ʒ������ζ���棬����ѡ���ζ
		onPicked(food);
		//����ҳ����ת��ͼ������Ҫ���ݵĲ�Ʒ���󴫵���ζ����
		Intent intent = new Intent();
		//intent.setAction(MyBroadcastReceiver.SENTTOTASTE);
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(food));
		intent.putExtras(bundle);
		//���͹㲥
	   // sendBroadcast(intent);
		goTo(intent,PickTasteActivity.class);
		
	}

	
	/**
	 * go to Activity method
	 */
	public  void goTo(Intent intent, Class<? extends Activity> cls) {
		OrderActivity.dynamic.removeAllViews();
		OrderActivity.dynamic.removeAllViewsInLayout();
		intent.setClass(PickFoodActivity.this, cls);
		View nowView = PickFoodActivity.this.getLocalActivityManager()
				.startActivity(cls.getName(), intent).getDecorView();
		OrderActivity.dynamic.addView(nowView);
		
	}
	
	

	/**
	 * go to Activity method
	 */
	public  void goTo(Class<? extends Activity> cls) {
		OrderActivity.dynamic.removeAllViews();
		OrderActivity.dynamic.removeAllViewsInLayout();
		View nowView = PickFoodActivity.this.getLocalActivityManager().startActivity(cls.getName(), new Intent(PickFoodActivity.this,cls))
				.getDecorView();
		OrderActivity.dynamic.addView(nowView);
		
	}
	/**
	 * ��Ӳ�Ʒ���ѵ�˵�List��
	 * 
	 * @param food
	 *            ѡ�еĲ�Ʒ��Ϣ
	 */
	private void addFood(OrderFood food) {

		int index = _pickFoods.indexOf(food);

		if (index != -1) {
			/**
			 * ���ԭ4�Ĳ�Ʒ�б����Ѱ�����ͬ�Ĳ�Ʒ�� ���µ�˵����ۼӵ�ԭ4�Ĳ�Ʒ��
			 */
			OrderFood pickedFood = _pickFoods.get(index);

			float orderAmount = food.getCount() + pickedFood.getCount();
			if (orderAmount > 255) {
				Toast.makeText(this, "�Բ���\"" + food.toString() + "\"���ֻ�ܵ�255��", 0).show();
				// pickedFood.setCount(new Float(255));
			} else {
				Toast.makeText(this, "���"	+ (food.hangStatus == OrderFood.FOOD_HANG_UP ? "������\"" : "\"") + food.toString() + "\""
								+ Util.float2String2(food.getCount()) + "��", 0)	.show();
				pickedFood.setCount(orderAmount);
				_pickFoods.set(index, pickedFood);
			}
		} else {
			if (food.getCount() > 255) {
				Toast.makeText(this, "�Բ���\"" + food.toString() + "\"���ֻ�ܵ�255��", 0).show();
			} else {
				Toast.makeText(this, "����"	+ (food.hangStatus == OrderFood.FOOD_HANG_UP ? "������\""
										: "\"") + food.toString() + "\""
								+ Util.float2String2(food.getCount()) + "��", 0)
						.show();
				_pickFoods.add(food);
			}
		}

		if (_tabHost.getCurrentTabTag() == TAG_NUMBER) {
			(((EditText) findViewById(R.id.filterNumEdtTxt))).setText("");
		} else if (_tabHost.getCurrentTabTag() == TAG_KITCHEN) {
			((EditText) findViewById(R.id.filterKitchenEdtTxt)).setText("");
		} else if (_tabHost.getCurrentTabTag() == TAG_PINYIN) {
			((EditText) findViewById(R.id.filterPinyinEdtTxt)).setText("");
		}
		
		
	}

	

	/**
	 * ���ñ��ɸѡ��View
	 */
	private void setupNumberView() {

		//��������
		final EditText filterNumEdtTxt = (EditText)findViewById(R.id.filterNumEdtTxt);
		filterNumEdtTxt.setText("");

		// ��Ų�8
		final LinearLayout numberSidebar = (LinearLayout) findViewById(R.id.NumsideIndex);
		//numberSidebar.setBackgroundColor(0xfbfdfe);
		numberSidebar.setOrientation(LinearLayout.VERTICAL);
		numberSidebar.removeAllViews();
		numberSidebar.setBackgroundResource(0);

		/**
		 * ��8��ָ����ʱ��������Ӧ������
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
						filterNumEdtTxt.setSelection(filterNumEdtTxt.getText().toString().length());	
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
		 * ��Ų�8���0-9������
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
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods,	PickFoodListView.TAG_NUM);
		pickLstView.setFoodPickedListener(this);
	

		/**
		 * ��ƷList��ʱ���������
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
					ArrayList<Food> filterFoods = new ArrayList<Food>();
					for (int i = 0; i < WirelessOrder.foodMenu.foods.length; i++) {
						if (String.valueOf(
								WirelessOrder.foodMenu.foods[i].aliasID)
								.startsWith(s.toString().trim())) {
							filterFoods.add(WirelessOrder.foodMenu.foods[i]);
						}
					}
					pickLstView.notifyDataChanged(
							filterFoods.toArray(new Food[filterFoods.size()]),
							PickFoodListView.TAG_NUM);

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

		// �ֳ��8
		final LinearLayout kitchenSidebar =(LinearLayout) findViewById(R.id.sideIndex);
		final PickFoodListView pickLstView = (PickFoodListView) findViewById(R.id.pickByKitchenLstView);
		// ����8
		kitchenSidebar.removeAllViews();
		//kitchenSidebar.setBackgroundColor(0xfbfdfe);
		kitchenSidebar.setBackgroundResource(0);

		
		/**
		 * ��8��ָ����ʱ����ʾ��Ӧ�Ĳ���
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
						 * ��ݲ�8ѡ�еĲ��ţ�ɸѡ����Ӧ�Ĳ��źͳ�
						 */
						List<Department> dept = new ArrayList<Department>();
						int deptID = ((Integer)((TextView)kitchenSidebar.getChildAt(curPos)).getTag());
						for (int i = 0; i < _validDepts.size(); i++) {
							if (_validDepts.get(i).deptID == deptID) {
								dept.add(_validDepts.get(i));
								break;
							}
						}
						List<Kitchen> kitchens = new ArrayList<Kitchen>();
						for (int i = 0; i < _validKitchens.size(); i++) {
							if (_validKitchens.get(i).deptID == deptID) {
								kitchens.add(_validKitchens.get(i));
							}
						}
						List<List<Kitchen>> kitchenChild = new ArrayList<List<Kitchen>>();
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
		
		// Ϊ��8���ɸѡ���
		for (Department d : _validDepts) {
			TextView tv = new TextView(this);
			tv.setText(d.name.subSequence(0, 2));
			tv.setTag(new Integer(d.deptID));
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			tv.setBackgroundDrawable(null);
			tv.setPadding(0, 5, 0, 5);
			tv.setTextColor(Color.GRAY);
			kitchenSidebar.addView(tv);
		}

		RelativeLayout filterKitchen = (RelativeLayout) findViewById(R.id.filterKitchenRelaLayout);
		final EditText filterKitEdtTxt = (EditText) findViewById(R.id.filterKitchenEdtTxt);
		filterKitEdtTxt.setText("");
		// ��ʼ����ʱ���Ĭ����ʾ�ĳ���Ϣ
		TextView ketchenName = (TextView)findViewById(R.id.Spinner01);
		ketchenName.setText("ȫ��");
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods,	PickFoodListView.TAG_PINYIN);
		pickLstView.setFoodPickedListener(this);



		/**
		 * ��ƷList��ʱ���������
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
		 * �ڷֳ�ѡ��ҳ���а�ƴ����в�Ʒ��ɸѡ
		 */
		filterKitEdtTxt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.toString().length() != 0) {
					Food[] foods = pickLstView.getSourceData();
					ArrayList<Food> filterFoods = new ArrayList<Food>();
					for (int i = 0; i < foods.length; i++) {
						if(String.valueOf(foods[i].pinyin).toLowerCase().contains(s.toString().toLowerCase())
								|| foods[i].name.contains(s.toString())) {
							filterFoods.add(foods[i]);
						}
					}

					pickLstView.notifyDataChanged(
							filterFoods.toArray(new Food[filterFoods.size()]),
							PickFoodListView.TAG_PINYIN);

				} else {
					if (dialogTag) {
						pickLstView.notifyDataChanged(_filterFoods
								.toArray(new Food[_filterFoods.size()]),
								PickFoodListView.TAG_PINYIN);
					} else {
						pickLstView.notifyDataChanged(
								WirelessOrder.foodMenu.foods,
								PickFoodListView.TAG_PINYIN);
					}

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
		 * �����ѡ��ҳ�沢ɸѡ����Ӧ�Ĳ�Ʒ
		 */
		filterKitchen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				/**
				 * ɸѡ��ÿ�������в�Ʒ�ĳ�
				 */
				List<List<Kitchen>> kitchenChild = new ArrayList<List<Kitchen>>();
				for (int i = 0; i < _validDepts.size(); i++) {
					List<Kitchen> kitchens = new ArrayList<Kitchen>();
					for (int j = 0; j < _validKitchens.size(); j++) {
						if (_validKitchens.get(j).deptID == _validDepts.get(i).deptID) {
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

		// ƴ���8
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
		 * ƴ���8��ʾA-Z����ĸ
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
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods,	PickFoodListView.TAG_PINYIN);
		pickLstView.setFoodPickedListener(this);
		/**
		 * ƴ��List��ʱ���������
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
		 * ��ƴ����в�Ʒ��ɸѡ
		 */
		filterPinyinEdtTxt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.toString().length() != 0) {
					ArrayList<Food> filterFoods = new ArrayList<Food>();
					for (int i = 0; i < WirelessOrder.foodMenu.foods.length; i++) {
						if (WirelessOrder.foodMenu.foods[i].pinyin != null) {
							if (WirelessOrder.foodMenu.foods[i].pinyin
									.toLowerCase().contains(
											s.toString().toLowerCase())
									|| WirelessOrder.foodMenu.foods[i].name
											.contains(s.toString())) {
								filterFoods
										.add(WirelessOrder.foodMenu.foods[i]);
							}
						} else {
							filterFoods.add(WirelessOrder.foodMenu.foods[i]);
						}
					}
					pickLstView.notifyDataChanged(
							filterFoods.toArray(new Food[filterFoods.size()]),
							PickFoodListView.TAG_PINYIN);

				} else {
					pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods,
							PickFoodListView.TAG_PINYIN);
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
	 * ������ʱ�˵�View
	 */
	private void setTempView() {

		_tempLstView = (TempListView) findViewById(R.id.tempListView);
		_tempLstView.notifyDataChanged();

		// ��ʱ�����
		((ImageView) findViewById(R.id.add))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_tempLstView.addTemp();
					}
				});
		

		/**
		 * list���ʱ���q������
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
	 * �ֳ�ҳ���г�ѡ���Dialog
	 */
	private class KitchenSelectDialog extends Dialog {

		private List<Department> _deptParent;
		private List<List<Kitchen>> _kitchenChild;

		KitchenSelectDialog(final PickFoodListView foodLstView,	List<Department> depts, List<List<Kitchen>> kitchens) {
			super(PickFoodActivity.this, R.style.FullHeightDialog);
			_deptParent = depts;
			_kitchenChild = kitchens;

			View dialogContent = View.inflate(PickFoodActivity.this, R.layout.expander_list_view, null);
			setTitle("��ѡ���");
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
							.setText(_deptParent.get(groupPosition).name);

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
					((TextView) view.findViewById(R.id.mychild)).setText(_kitchenChild.get(groupPosition).get(childPosition).name);
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
			 * ѡ��ĳ����ɸѡ����Ӧ�Ĳ�Ʒ��������ListView
			 */
			kitchenLstView.setOnChildClickListener(new OnChildClickListener() {

				public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
					
					Kitchen selectedKitchen = _kitchenChild.get(groupPosition).get(childPosition);
					_filterFoods = new ArrayList<Food>();
					for (int i = 0; i < WirelessOrder.foodMenu.foods.length; i++) {
						if (WirelessOrder.foodMenu.foods[i].kitchen.aliasID == selectedKitchen.aliasID) {
							_filterFoods.add(WirelessOrder.foodMenu.foods[i]);
						}
					}
					// ѡ�г����¸�ֵ
					((TextView)PickFoodActivity.this.findViewById(R.id.Spinner01)).setText(_kitchenChild.get(groupPosition).get(childPosition).name);
					dialogTag = true;

					foodLstView.notifyDataChanged(_filterFoods.toArray(new Food[_filterFoods.size()]),
												  PickFoodListView.TAG_PINYIN);
					dismiss();
					return true;
				}
			});

			/**
			 * ��ѡ��Dialog�ķ���Button��Ӧ�¼�
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

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (_detector.onTouchEvent(event)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		_detector.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	/*
	 * ���ƻ���ִ�з���
	 */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		float scrollX = e1.getX() - e2.getX();
		if (Math.abs(velocityX) > 200 && velocityY != 0
				&& Math.abs(scrollX) / Math.abs(e1.getY() - e2.getY()) > 1) {

			if (scrollX > 0) {
				// �˴���Ӵ�����4��ʾ��һ��ҳ��
				if (_tabHost.getCurrentTab() == 4)
					return false;
				_tabHost.setCurrentTab(_tabHost.getCurrentTab() + 1);

			} else {
				// �˴���Ӵ�����4��ʾ��һ��ҳ��
				if (_tabHost.getCurrentTab() == 0)
					return false;
				_tabHost.setCurrentTab(_tabHost.getCurrentTab() - 1);
			}

			return true;
		}

		return false;
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

		
		/**
		 * �����в�Ʒ���а����Ž�������
		 */
		Food[] tmpFoods = new Food[WirelessOrder.foodMenu.foods.length];
		System.arraycopy(WirelessOrder.foodMenu.foods, 0, tmpFoods, 0,
				WirelessOrder.foodMenu.foods.length);
		Arrays.sort(tmpFoods, new Comparator<Food>() {
			@Override
			public int compare(Food food1, Food food2) {
				if (food1.kitchen.aliasID
						> food2.kitchen.aliasID) {
					return 1;
				} else if (food1.kitchen.aliasID < food2.kitchen.aliasID) {
					return -1;
				} else {
					return 0;
				}
			}
		});

		/**
		 * ʹ�ö��ֲ����㷨ɸѡ���в�Ʒ�ĳ�
		 */
		_validKitchens = new ArrayList<Kitchen>();
		for (int i = 0; i < WirelessOrder.foodMenu.kitchens.length; i++) {
			Food keyFood = new Food();
			keyFood.kitchen.aliasID = WirelessOrder.foodMenu.kitchens[i].aliasID;
			int index = Arrays.binarySearch(tmpFoods, keyFood,
					new Comparator<Food>() {

						public int compare(Food food1, Food food2) {
							if (food1.kitchen .aliasID> food2.kitchen.aliasID) {
								return 1;
							} else if (food1.kitchen .aliasID < food2.kitchen.aliasID) {
								return -1;
							} else {
								return 0;
							}
						}
					});

			if (index >= 0) {
				_validKitchens.add(WirelessOrder.foodMenu.kitchens[i]);
			}
		}

		/**
		 * ɸѡ���в�Ʒ�Ĳ���
		 */
		_validDepts = new ArrayList<Department>();
		for (int i = 0; i < WirelessOrder.foodMenu.sKitchens.length; i++) {
			for (int j = 0; j < _validKitchens.size(); j++) {
				if (WirelessOrder.foodMenu.sKitchens[i].deptID == _validKitchens
						.get(j).deptID) {
					_validDepts.add(WirelessOrder.foodMenu.sKitchens[i]);
					break;
				}
			}
		}
	}

	
}
