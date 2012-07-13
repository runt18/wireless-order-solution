package com.wireless.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Display;
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
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.ui.view.PickFoodListView;
import com.wireless.ui.view.ScrollLayout;
import com.wireless.ui.view.ScrollLayout.OnViewChangedListner;
import com.wireless.ui.view.TempListView;

public class PickFoodActivity extends Activity implements PickFoodListView.OnFoodPickedListener {

	private ArrayList<Kitchen> _validKitchens;
	private ArrayList<Department> _validDepts;

	private final static String TAG_NUMBER = "编号";
	private final static String TAG_KITCHEN = "分厨";
	private final static String TAG_PINYIN = "拼音";
	private final static String TAG_OCCASIONAL = "临时菜";

	private final static int PICK_WITH_TASTE = 0;

	private ScrollLayout _foodScrollLayout;
	private View _currentView;
	
	private ArrayList<OrderFood> _pickFoods = new ArrayList<OrderFood>();

	private List<Food> _filterKitchenFoods;
	private TempListView _tempLstView;
	private PopupWindow _popupWindow;
	private ListView _popupLstView;
	private TextView _centerTxtView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pick_food);

		init();

		// 取得新点菜中已有的菜品List，并保存到pickFood的List中
		OrderParcel orderParcel = getIntent().getParcelableExtra(
				OrderParcel.KEY_VALUE);
		for (int i = 0; i < orderParcel.foods.length; i++) {
			_pickFoods.add(orderParcel.foods[i]);
		}
		
		//返回Button
		((ImageView)findViewById(R.id.pickFoodBackBtn)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				onBackPressed();
				finish();
			}
		});
		
		//已点菜shortcut的响应事件
		((ImageView)findViewById(R.id.foodHolderImgView)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(_popupWindow.isShowing()) {
					_popupWindow.dismiss();
					
				}else{					
					if(_tempLstView != null){
						 //Combine both temporary and picked foods
						List<OrderFood> pickedFoods = new ArrayList<OrderFood>(_pickFoods);
						pickedFoods.addAll(_tempLstView.getSourceData());
						_popupLstView.setAdapter(new PopupWndAdapter(PickFoodActivity.this, pickedFoods));
					}else{
						_popupLstView.setAdapter(new PopupWndAdapter(PickFoodActivity.this, _pickFoods));
					}
					_popupWindow.showAsDropDown(v, -120, 5);
				}
			}
		});
		
		_foodScrollLayout = (ScrollLayout)findViewById(R.id.pickFoodScrollLayout);
		_foodScrollLayout.setOnViewChangedListener(new OnViewChangedListner() {
			
			@Override
			public void onViewChanged(int curScreen, View parent, View curView) {
				_currentView = curView;
				String tag = curView.getTag().toString();
				((TextView)findViewById(R.id.pickFoodTxtView)).setText("点菜-" + tag.substring(0, 2));
				
				_centerTxtView.setVisibility(View.INVISIBLE);
				
				//切换点菜方式时，保存当前的点菜模式
				Editor editor = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();
				
				((LinearLayout)findViewById(R.id.numberLayout)).setBackgroundResource(R.drawable.tab_bg_unselected);
				((LinearLayout)findViewById(R.id.kitchenLayout)).setBackgroundResource(R.drawable.tab_bg_unselected);
				((LinearLayout)findViewById(R.id.pinyinLayout)).setBackgroundResource(R.drawable.tab_bg_unselected);
				((LinearLayout)findViewById(R.id.tempLayout)).setBackgroundResource(R.drawable.tab_bg_unselected);
				if(tag.equals(TAG_NUMBER)){
					editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_NUMBER);
					((LinearLayout)findViewById(R.id.numberLayout)).setBackgroundResource(R.drawable.tab_bg_selected);
					
				}else if(tag.equals(TAG_KITCHEN)){
					editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_KITCHEN);
					((LinearLayout)findViewById(R.id.kitchenLayout)).setBackgroundResource(R.drawable.tab_bg_selected);
					
				}else if(tag.equals(TAG_PINYIN)){
					editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_PINYIN);
					((LinearLayout)findViewById(R.id.pinyinLayout)).setBackgroundResource(R.drawable.tab_bg_selected);
					
				}else if(tag.equals(TAG_OCCASIONAL)){
					((LinearLayout)findViewById(R.id.tempLayout)).setBackgroundResource(R.drawable.tab_bg_selected);
					
				}
				editor.commit();
			}
		});
		//编号点菜View
		_foodScrollLayout.addView(setupNumberView());
		//分厨点菜View
		_foodScrollLayout.addView(setupKitchenView());
		//拼音点菜View
		_foodScrollLayout.addView(setupPinyinView());
		//临时菜点菜View
		_foodScrollLayout.addView(setupTempView());
		
		//编号点菜Button
		((LinearLayout)findViewById(R.id.numberLayout)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				_foodScrollLayout.setToScreen(0);
			}
		});
		
		//分厨点菜Button
		((LinearLayout)findViewById(R.id.kitchenLayout)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				_foodScrollLayout.setToScreen(1);
			}
		});
		
		//拼音点菜Button
		((LinearLayout)findViewById(R.id.pinyinLayout)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				_foodScrollLayout.setToScreen(2);
			}
		});
		
		
		//临时菜点菜Button
		((LinearLayout)findViewById(R.id.tempLayout)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				_foodScrollLayout.setToScreen(3);
			}
		});
		
		/**
		 * 根据上次保存的记录，切换到相应的点菜方式
		 */
		int lastPickCate = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).getInt(Params.LAST_PICK_CATE, Params.PICK_BY_KITCHEN);
		if(lastPickCate == Params.PICK_BY_NUMBER){
			_foodScrollLayout.setToScreen(0);
			
		}else if(lastPickCate == Params.PICK_BY_KITCHEN){
			_foodScrollLayout.setToScreen(1);
			
		}else if(lastPickCate == Params.PICK_BY_PINYIN){
			_foodScrollLayout.setToScreen(2);

		}else{
			_foodScrollLayout.setToScreen(0);
		}
	}

	@Override
	public void onDestroy(){
		((WindowManager)getSystemService(Context.WINDOW_SERVICE)).removeView(_centerTxtView);
		super.onDestroy();
	}
	
	/**
	 * 返回时将新点菜品的List返回到上一个Activity
	 */
	@Override
	public void onBackPressed() {
		//dismiss the order shortcut
		if(_popupWindow.isShowing()){
			_popupWindow.dismiss();
		}
		
		// Add the temporary foods to the picked food list
		// except the ones without food name
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
	 * 通过"编号"、"分厨"、"拼音"方式选中菜品后， 将菜品保存到List中，退出时将此List作为结果返回到上一个Activity
	 * 
	 * @param food
	 *            选中菜品的信息
	 */
	@Override
	public void onPicked(OrderFood food) {
		addFood(food);
	}

	/**
	 * 通过"编号"、"分厨"、"拼音"方式选中菜品后， 将菜品保存到List中，并跳转到口味Activity选择口味
	 * 
	 * @param food
	 *            选中菜品的信息
	 */
	@Override
	public void onPickedWithTaste(OrderFood food) {
		Intent intent = new Intent(this, PickTasteActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(food));
		intent.putExtras(bundle);
		startActivityForResult(intent, PICK_WITH_TASTE);
	}

	/**
	 * 添加菜品到已点菜的List中
	 * 
	 * @param food
	 *            选中的菜品信息
	 */
	private void addFood(OrderFood food) {

		int index = _pickFoods.indexOf(food);

		if (index != -1) {
			/**
			 * 如果原来的菜品列表中已包含有相同的菜品， 则将新点菜的数量累加到原来的菜品中
			 */
			OrderFood pickedFood = _pickFoods.get(index);

			float orderAmount = food.getCount() + pickedFood.getCount();
			if (orderAmount > 255) {
				Toast.makeText(this, "对不起，\"" + food.toString() + "\"最多只能点255份", 0).show();
				// pickedFood.setCount(new Float(255));
			} else {
				Toast.makeText(this, "添加"	+ (food.hangStatus == OrderFood.FOOD_HANG_UP ? "并叫起\"" : "\"") + food.toString() + "\""
								+ Util.float2String2(food.getCount()) + "份", 0)	.show();
				pickedFood.setCount(orderAmount);
				_pickFoods.set(index, pickedFood);
			}
		} else {
			if (food.getCount() > 255) {
				Toast.makeText(this, "对不起，\"" + food.toString() + "\"最多只能点255份", 0).show();
			} else {
				Toast.makeText(this, "新增"	+ (food.hangStatus == OrderFood.FOOD_HANG_UP ? "并叫起\""
										: "\"") + food.toString() + "\""
								+ Util.float2String2(food.getCount()) + "份", 0)
						.show();
				_pickFoods.add(food);
			}
		}

		if (_currentView.getTag().equals(TAG_NUMBER)) {
			(((EditText) findViewById(R.id.filterNumEdtTxt))).setText("");
			
		} else if (_currentView.getTag().equals(TAG_KITCHEN)) {
			((EditText) findViewById(R.id.filterKitchenEdtTxt)).setText("");
			
		} else if (_currentView.getTag().equals(TAG_PINYIN)) {
			((EditText) findViewById(R.id.filterPinyinEdtTxt)).setText("");
		}
		
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == PICK_WITH_TASTE) {
				/**
				 * 添加口味后添加到pickList中
				 */
				FoodParcel foodParcel = data.getParcelableExtra(FoodParcel.KEY_VALUE);
				addFood(foodParcel);
			}
		}
	}

	/**
	 * 设置编号筛选的View
	 */
	private View setupNumberView() {

		View numberView = LayoutInflater.from(PickFoodActivity.this).inflate(R.layout.number, null);
		
		numberView.setTag(TAG_NUMBER);
		
		//编号输入框
		final EditText filterNumEdtTxt = (EditText)numberView.findViewById(R.id.filterNumEdtTxt);
		filterNumEdtTxt.setText("");

		// 编号侧栏
		final LinearLayout numberSidebar = (LinearLayout) numberView.findViewById(R.id.NumsideIndex);
		//numberSidebar.setBackgroundColor(0xfbfdfe);
		numberSidebar.setOrientation(LinearLayout.VERTICAL);
		numberSidebar.removeAllViews();
		numberSidebar.setBackgroundResource(0);

		/**
		 * 侧栏手指滑动时，输入相应的数字
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
						filterNumEdtTxt.setSelection(filterNumEdtTxt.getText().length());	
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
		 * 编号侧栏添加0-9的数字
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
		 * 编号输入框的删除Button
		 */
		((ImageView)numberView.findViewById(R.id.numberdelete)).setOnClickListener(new View.OnClickListener() {			
			@Override
		    public void onClick(View v) {
				//逐字删除
				String s = filterNumEdtTxt.getText().toString();
				if(s.length() > 0){
					filterNumEdtTxt.setText(s.substring(0, s.length() - 1));
					filterNumEdtTxt.setSelection(filterNumEdtTxt.getText().length());					
				}
			}
		});
		


		final PickFoodListView pickLstView = (PickFoodListView)numberView.findViewById(R.id.pickByNumLstView);
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods,	PickFoodListView.TAG_NUM);
		pickLstView.setFoodPickedListener(this);
		
		/**
		 * 菜品List滚动时隐藏软键盘
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
		 * 按编号进行菜品的筛选
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
		
		return numberView;
	}

	/**
	 * 设置分厨筛选的View
	 */
	private View setupKitchenView() {

		View kitchenView = LayoutInflater.from(PickFoodActivity.this).inflate(R.layout.kitchen, null);
		
		kitchenView.setTag(TAG_KITCHEN);
		
		// 分厨侧栏
		final LinearLayout kitchenSidebar =(LinearLayout) kitchenView.findViewById(R.id.sideIndex);
		final PickFoodListView pickLstView = (PickFoodListView) kitchenView.findViewById(R.id.pickByKitchenLstView);
		// 清除侧栏
		kitchenSidebar.removeAllViews();
		//kitchenSidebar.setBackgroundColor(0xfbfdfe);
		kitchenSidebar.setBackgroundResource(0);

		
		/**
		 * 侧栏手指滑动时，显示相应的部门
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
						 * 根据侧栏选中的部门，筛选出相应的部门和厨房
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
							if (_validKitchens.get(i).dept.deptID == deptID) {
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
		
		// 为侧栏添加筛选条件
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

		RelativeLayout filterKitchen = (RelativeLayout) kitchenView.findViewById(R.id.filterKitchenRelaLayout);
		final EditText filterKitEdtTxt = (EditText) kitchenView.findViewById(R.id.filterKitchenEdtTxt);
		filterKitEdtTxt.setText("");
		// 初始化的时候厨房默认显示的厨房信息
		TextView ketchenName = (TextView)kitchenView.findViewById(R.id.Spinner01);
		ketchenName.setText("全部");
		_filterKitchenFoods = new ArrayList<Food>(Arrays.asList(WirelessOrder.foodMenu.foods));
		pickLstView.notifyDataChanged(_filterKitchenFoods.toArray(new Food[_filterKitchenFoods.size()]), PickFoodListView.TAG_PINYIN);
		pickLstView.setFoodPickedListener(this);

		/**
		 * 菜品List滚动时隐藏软键盘
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
		 * 在分厨选择页面中按拼音进行菜品的筛选
		 */
		filterKitEdtTxt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.toString().length() != 0) {
					ArrayList<Food> filterFoods = new ArrayList<Food>();
					for(Food food : _filterKitchenFoods){
						if(String.valueOf(food.pinyin).toLowerCase().contains(s.toString().toLowerCase())
								|| food.name.contains(s.toString())) {
							filterFoods.add(food);
						}
					}

					pickLstView.notifyDataChanged(
							filterFoods.toArray(new Food[filterFoods.size()]),
							PickFoodListView.TAG_PINYIN);

				} else {
					pickLstView.notifyDataChanged(_filterKitchenFoods.toArray(new Food[_filterKitchenFoods.size()]), PickFoodListView.TAG_PINYIN);
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
		 * 弹出厨房选择页面并筛选出相应的菜品
		 */
		filterKitchen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				/**
				 * 筛选出每个部门中有菜品的厨房
				 */
				List<List<Kitchen>> kitchenChild = new ArrayList<List<Kitchen>>();
				for (int i = 0; i < _validDepts.size(); i++) {
					List<Kitchen> kitchens = new ArrayList<Kitchen>();
					for (int j = 0; j < _validKitchens.size(); j++) {
						if (_validKitchens.get(j).dept.deptID == _validDepts.get(i).deptID) {
							kitchens.add(_validKitchens.get(j));
						}
					}
					kitchenChild.add(kitchens);
				}
				new KitchenSelectDialog(pickLstView, _validDepts, kitchenChild)
						.show();
			}
		});
		
		return kitchenView;
	}

	/**
	 * 设置拼音筛选的View
	 */
	private View setupPinyinView() {
		
		View pinyinView = LayoutInflater.from(PickFoodActivity.this).inflate(R.layout.pinyin, null);
		
		pinyinView.setTag(TAG_PINYIN);
		
		// 拼音输入框
		final EditText filterPinyinEdtTxt = (EditText)pinyinView.findViewById(R.id.filterPinyinEdtTxt);
		filterPinyinEdtTxt.setText("");

		// 拼音侧栏
		final LinearLayout pinyinSidebar = (LinearLayout) pinyinView.findViewById(R.id.PinyinsideIndex); 
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
						filterPinyinEdtTxt.setSelection(filterPinyinEdtTxt.getText().length());		
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
		 * 拼音侧栏显示A-Z的字母
		 */
		for(char c = 'A'; c <= 'Z'; c++) {
			final TextView tv = new TextView(PickFoodActivity.this);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
			tv.setGravity(Gravity.CENTER_HORIZONTAL);
			tv.setText(Character.toString(c));
			pinyinSidebar.addView(tv);
		}
		
		/**
		 * 拼音输入框的删除Button响应事件
		 */
		((ImageView)pinyinView.findViewById(R.id.pinyindelete)).setOnClickListener(new View.OnClickListener() {					
			@Override
		    public void onClick(View v) {
				//逐字删除
				String s = filterPinyinEdtTxt.getText().toString();
				if(s.length() > 0){
					filterPinyinEdtTxt.setText(s.substring(0, s.length() - 1));
					filterPinyinEdtTxt.setSelection(filterPinyinEdtTxt.getText().length());					
				}
			}
		});
		
		final PickFoodListView pickLstView = (PickFoodListView) pinyinView.findViewById(R.id.pickByPinyinLstView);
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods,	PickFoodListView.TAG_PINYIN);
		pickLstView.setFoodPickedListener(this);
		/**
		 * 拼音List滚动时隐藏软键盘
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
		 * 按拼音进行菜品的筛选
		 */
		filterPinyinEdtTxt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.toString().length() != 0) {
					ArrayList<Food> filterFoods = new ArrayList<Food>();
					for(Food food : WirelessOrder.foodMenu.foods){
						if(String.valueOf(food.pinyin).toLowerCase().contains(s.toString().toLowerCase()) ||
						   food.name.contains(s.toString())){
							filterFoods.add(food);
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
		
		return pinyinView;
	}

	/**
	 * 设置临时菜的View
	 */
	private View setupTempView() {
		
		View tempView = LayoutInflater.from(PickFoodActivity.this).inflate(R.layout.temp, null);

		tempView.setTag(TAG_OCCASIONAL);
		
		_popupWindow.dismiss();
		_tempLstView = (TempListView) tempView.findViewById(R.id.tempListView);
		_tempLstView.notifyDataChanged();


		// 临时菜添加
		((ImageView) tempView.findViewById(R.id.add))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_tempLstView.addTemp();
					}
				});
		

		/**
		 * List滚动的时候屏蔽软键盘
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
		
		return tempView;
	}

	/**
	 * 分厨页面中厨房选择的Dialog
	 */
	private class KitchenSelectDialog extends Dialog {

		private List<Department> _deptParent;
		private List<List<Kitchen>> _kitchenChild;

		KitchenSelectDialog(final PickFoodListView foodLstView,	List<Department> depts, List<List<Kitchen>> kitchens) {
			super(PickFoodActivity.this, R.style.FullHeightDialog);
			_deptParent = depts;
			_kitchenChild = kitchens;

			View dialogContent = View.inflate(PickFoodActivity.this, R.layout.expander_list_view, null);
			setTitle("请选择厨房");
			ExpandableListView kitchenLstView = (ExpandableListView) dialogContent.findViewById(R.id.kitchenSelectLstView);
			// kitchenLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));

			// 设置ListView的Adaptor
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

			// 设置展开所有的二级菜单
			for (int i = 0; i < _deptParent.size(); i++) {
				kitchenLstView.expandGroup(i);
			}

			/**
			 * 选择某个厨房后，筛选出相应的菜品，并更新ListView
			 */
			kitchenLstView.setOnChildClickListener(new OnChildClickListener() {

				public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
					
					Kitchen selectedKitchen = _kitchenChild.get(groupPosition).get(childPosition);
					_filterKitchenFoods.clear();
					for (int i = 0; i < WirelessOrder.foodMenu.foods.length; i++) {
						if (WirelessOrder.foodMenu.foods[i].kitchen.aliasID == selectedKitchen.aliasID) {
							_filterKitchenFoods.add(WirelessOrder.foodMenu.foods[i]);
						}
					}
					// 选中厨房后从新赋值
					((TextView)PickFoodActivity.this.findViewById(R.id.Spinner01)).setText(_kitchenChild.get(groupPosition).get(childPosition).name);

					foodLstView.notifyDataChanged(_filterKitchenFoods.toArray(new Food[_filterKitchenFoods.size()]),
												  PickFoodListView.TAG_PINYIN);
					dismiss();
					return true;
				}
			});

			/**
			 * 厨房选择Dialog的返回Button响应事件
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

	/**
	 * 点击按钮显示已点菜列表adapter
	 */
	private class PopupWndAdapter extends BaseAdapter {

		private List<OrderFood> _orderFoods;
		private Context _context;

		public PopupWndAdapter(Context context, List<OrderFood> pickFoods) {
			this._context = context;
			this._orderFoods = pickFoods;
		}

		@Override
		public int getCount() {
			return _orderFoods.size();
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
			OrderFood food = _orderFoods.get(position);
			if(convertView == null){
				convertView = LayoutInflater.from(_context).inflate(R.layout.orderpopuwindowitem, null);
				((TextView)convertView.findViewById(R.id.popuwindowfoodname)).setText(toFoodString(food));
			} else {
				((TextView)convertView.findViewById(R.id.popuwindowfoodname)).setText(toFoodString(food));
			}
			return convertView;
		}
		
		private String toFoodString(OrderFood food){
			String s = "";
			if(food.isTemporary){
				s = "(临)";
			}
			s += food.name + "(" + Util.float2String2(food.getCount()) + ")";
			return s;
		}
	}

	private void init() {
		
		/**
		 * 创建中间显示的TextView
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
		 * 创建已点菜shortcut的PopupWindow
		 */
		Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		// 获取自定义布局文件的视图
		View popupWndView = getLayoutInflater().inflate(R.layout.orderlistpupowindow, null, false);
		// 创建PopupWindow实例
		_popupWindow = new PopupWindow(popupWndView, 200, new Float(display.getHeight() / 2.5).intValue(), false);
		_popupWindow.setOutsideTouchable(true);
		_popupWindow.setAnimationStyle(R.style.popuwindow);
		_popupLstView = (ListView)popupWndView.findViewById(R.id.orderpupowindowLstView);
		
		/**
		 * 将所有菜品进行按厨房编号进行排序
		 */
		Food[] tmpFoods = new Food[WirelessOrder.foodMenu.foods.length];
		System.arraycopy(WirelessOrder.foodMenu.foods, 0, tmpFoods, 0,
				WirelessOrder.foodMenu.foods.length);
		Arrays.sort(tmpFoods, new Comparator<Food>() {
			@Override
			public int compare(Food food1, Food food2) {
				if (food1.kitchen.aliasID > food2.kitchen.aliasID) {
					return 1;
				} else if (food1.kitchen.aliasID < food2.kitchen.aliasID) {
					return -1;
				} else {
					return 0;
				}
			}
		});

		/**
		 * 使用二分查找算法筛选出有菜品的厨房
		 */
		_validKitchens = new ArrayList<Kitchen>();
		for (int i = 0; i < WirelessOrder.foodMenu.kitchens.length; i++) {
			Food keyFood = new Food();
			keyFood.kitchen.aliasID = WirelessOrder.foodMenu.kitchens[i].aliasID;
			int index = Arrays.binarySearch(tmpFoods, keyFood,
					new Comparator<Food>() {

						public int compare(Food food1, Food food2) {
							if (food1.kitchen.aliasID > food2.kitchen.aliasID) {
								return 1;
							} else if (food1.kitchen.aliasID < food2.kitchen.aliasID) {
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
		 * 筛选出有菜品的部门
		 */
		_validDepts = new ArrayList<Department>();
		for (int i = 0; i < WirelessOrder.foodMenu.depts.length; i++) {
			for (int j = 0; j < _validKitchens.size(); j++) {
				if (WirelessOrder.foodMenu.depts[i].deptID == _validKitchens.get(j).dept.deptID) {
					_validDepts.add(WirelessOrder.foodMenu.depts[i]);
					break;
				}
			}
		}
	}
}
