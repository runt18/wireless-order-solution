package com.wireless.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
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
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.FoodParcel;
import com.wireless.common.OrderParcel;
import com.wireless.common.WirelessOrder;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.ui.view.OrderFoodListView;
import com.wireless.ui.view.PickFoodListView;
import com.wireless.ui.view.TempListView;







public class PickFoodActivity extends TabActivity 
							  implements PickFoodListView.OnFoodPickedListener, OnGestureListener{
	
	private final static String TAG_NUMBER = "number";
	private final static String TAG_KITCHEN = "kitchen";
	private final static String TAG_PINYIN = "pinyin";
	private final static String TAG_OCCASIONAL = "occasional";
	
	private final static int PICK_WITH_TASTE = 0;
	
	private ArrayList<OrderFood> _pickFoods = new ArrayList<OrderFood>();
	private TabHost _tabHost;
	private GestureDetector _detector; 
    private String _ketchenName;
    private TextView ketchenName;
    boolean dialogTag = false;
    private List<Food> _filterFoods;
    private TempListView _tempLstView;
    private PopupWindow _popupWindow;
    private OrderFoodListView _orderLstView;
    private boolean orderlistTag = false;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.table);
		_detector = new GestureDetector(this); 
		
		
		//取得新点菜中已有的菜品List，并保存到pickFood的List中
		OrderParcel orderParcel = getIntent().getParcelableExtra(OrderParcel.KEY_VALUE);
		for(int i = 0; i < orderParcel.foods.length; i++){
			_pickFoods.add(orderParcel.foods[i]);
		}
		
		// construct the tab host
		_tabHost = getTabHost();
		
		//编号Tab
		TabSpec spec = _tabHost.newTabSpec(TAG_NUMBER)
							   .setIndicator(createTabIndicator("编号", R.drawable.number_selector))
							   .setContent(new TabHost.TabContentFactory(){
								   @Override
								   public View createTabContent(String arg0) {
									   return LayoutInflater.from(PickFoodActivity.this).inflate(R.layout.number, null);
								   }								   
							   });
		_tabHost.addTab(spec);
		
		//分厨Tab
		spec = _tabHost.newTabSpec(TAG_KITCHEN)
					   .setIndicator(createTabIndicator("分厨", R.drawable.kitchen_selector))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(PickFoodActivity.this).inflate(R.layout.kitchen, null);
						   }								   
					   });
		_tabHost.addTab(spec);
		
		//拼音Tab
		spec = _tabHost.newTabSpec(TAG_PINYIN)
					   .setIndicator(createTabIndicator("拼音", R.drawable.pinyin_selector))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(PickFoodActivity.this).inflate(R.layout.pinyin, null);
						   }								   
					   });
		
		_tabHost.addTab(spec);
		
		//临时菜Tab
		spec = _tabHost.newTabSpec(TAG_OCCASIONAL)
					   .setIndicator(createTabIndicator("临时菜", R.drawable.occasional_selector))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(PickFoodActivity.this).inflate(R.layout.temp, null);
						   }								   
					   });
		_tabHost.addTab(spec);
		
		/**
		 * Tab切换时更换相应的Adapter，显示不同的点菜方式
		 */
		_tabHost.setOnTabChangedListener(new OnTabChangeListener() {			
			@Override
			public void onTabChanged(String tag) {
				if(tag == TAG_NUMBER){
					setupNumberView();
					
				}else if(tag == TAG_KITCHEN){
					setupKitchenView();
					
				}else if(tag == TAG_PINYIN){
					setupPinyinView();
					
				} else if(tag == TAG_OCCASIONAL){					
					setTempView();
				}
			}
		});
		
		_tabHost.setCurrentTabByTag(TAG_NUMBER);
		setupNumberView();
	}

	/**
	 * 返回时将新点菜品的List返回到上一个Activity
	 */
	@Override
	public void onBackPressed(){
		//Add the temporary foods to the picked food list
		//except the ones without food name
		if(_tempLstView != null){
			List<OrderFood> tmpFoods = _tempLstView.getSourceData();
			Iterator<OrderFood> iter = tmpFoods.iterator();
			while(iter.hasNext()){
				if(iter.next().name.equals("")){
					iter.remove();
				}
			}
			_pickFoods.addAll(tmpFoods);
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
	 * @param text
	 * @param drawable
	 * @return
	 */
	private View createTabIndicator(String text, int drawable) {
		View view = LayoutInflater.from(_tabHost.getContext()).inflate(R.layout.tb_bg, null);
		((TextView)view.findViewById(R.id.tabsText)).setText(text);
		((ImageView) view.findViewById(R.id.icon)).setImageResource(drawable);
		return view;
	}

	/**
	 * 通过"编号"、"分厨"、"拼音"方式选中菜品后，
	 * 将菜品保存到List中，退出时将此List作为结果返回到上一个Activity
	 * @param food
	 * 			选中菜品的信息
	 */
	@Override
	public void onPicked(OrderFood food) {
		addFood(food);
	}
	
	/**
	 * 通过"编号"、"分厨"、"拼音"方式选中菜品后，
	 * 将菜品保存到List中，并跳转到口味Activity选择口味
	 * @param food
	 * 			选中菜品的信息
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
	 * @param food
	 * 			选中的菜品信息
	 */
	private void addFood(OrderFood food){
		
		int index = _pickFoods.indexOf(food);

		if(index != -1){
			/**
			 * 如果原来的菜品列表中已包含有相同的菜品，
			 * 则将新点菜的数量累加到原来的菜品中
			 */
			OrderFood pickedFood = _pickFoods.get(index);
			
			float orderAmount = food.getCount() + pickedFood.getCount();
   			if(orderAmount > 255){
   				Toast.makeText(this, "对不起，\"" + food.toString() + "\"最多只能点255份", 0).show();
   				//pickedFood.setCount(new Float(255));
   			}else{
   				Toast.makeText(this, "添加" + (food.hangStatus == OrderFood.FOOD_HANG_UP ? "并叫起\"" : "\"") + food.toString() + "\"" + Util.float2String2(food.getCount()) + "份", 0).show();
   				pickedFood.setCount(orderAmount);
   				_pickFoods.set(index, pickedFood);
   			}
		}else{
			if(food.getCount() > 255){
				Toast.makeText(this, "对不起，\"" + food.toString() + "\"最多只能点255份", 0).show();
			}else{
				Toast.makeText(this, "新增" + (food.hangStatus == OrderFood.FOOD_HANG_UP ? "并叫起\"" : "\"") + food.toString() + "\"" + Util.float2String2(food.getCount()) + "份", 0).show();
				_pickFoods.add(food);
			}
		}
		
		if(_tabHost.getCurrentTabTag() == TAG_NUMBER){
			(((EditText)findViewById(R.id.filterNumEdtTxt))).setText("");
		}else if(_tabHost.getCurrentTabTag() == TAG_KITCHEN){
			((EditText)findViewById(R.id.filterKitchenEdtTxt)).setText("");
		}else if(_tabHost.getCurrentTabTag() == TAG_PINYIN){
			((EditText)findViewById(R.id.filterPinyinEdtTxt)).setText("");
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode == RESULT_OK){			
			if(requestCode == PICK_WITH_TASTE){
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
	private void setupNumberView(){
		final PickFoodListView pickLstView = (PickFoodListView)findViewById(R.id.pickByNumLstView);
		final EditText filterNumEdtTxt = ((EditText)findViewById(R.id.filterNumEdtTxt));
		filterNumEdtTxt.setText("");
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods, PickFoodListView.TAG_NUM);
		//已点菜按钮
		ImageView numberOrder = (ImageView)findViewById(R.id.numorder);
		//已点菜按钮事件
		numberOrder.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(orderlistTag == false){
					 showOrderList();
					_popupWindow.showAsDropDown(v);
					orderlistTag = true;
				}else{
					_popupWindow.dismiss();
			        orderlistTag = false;
				
				}
				
				
			}
		});
		
		pickLstView.setFoodPickedListener(this);
		((ImageView)findViewById(R.id.numback)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
				finish();
				
			}
		});
		
		pickLstView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(filterNumEdtTxt.getWindowToken(), 0);
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});
		
		/**
		 * 按编号进行菜品的筛选
		 */
		filterNumEdtTxt.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().length() != 0){
					ArrayList<Food> filterFoods = new ArrayList<Food>();
					for(int i = 0; i < WirelessOrder.foodMenu.foods.length; i++){
						if(String.valueOf(WirelessOrder.foodMenu.foods[i].alias_id).startsWith(s.toString().trim())){
							filterFoods.add(WirelessOrder.foodMenu.foods[i]);
						}
					}
					pickLstView.notifyDataChanged(filterFoods.toArray(new Food[filterFoods.size()]), PickFoodListView.TAG_NUM);
					
				}else{
					pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods, PickFoodListView.TAG_NUM);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
	}
	
	/**
	 * 设置分厨筛选的View
	 */
	private void setupKitchenView(){
		
		final PickFoodListView pickLstView = (PickFoodListView)findViewById(R.id.pickByKitchenLstView);
		RelativeLayout filterKitchen = (RelativeLayout)findViewById(R.id.filterKitchenRelaLayout);
		final EditText filterKitEdtTxt = (EditText)findViewById(R.id.filterKitchenEdtTxt);
		filterKitEdtTxt.setText("");
		//初始化的时候厨房默认显示的厨房信息
		ketchenName = (TextView)findViewById(R.id.Spinner01);
		ketchenName.setText("全部");
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods, PickFoodListView.TAG_PINYIN);
		pickLstView.setFoodPickedListener(this);
		
		//已点菜按钮
		ImageView numberOrder = (ImageView)findViewById(R.id.kitchenorder);
		//已点菜按钮事件
		numberOrder.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(orderlistTag == false){
					 showOrderList();
					_popupWindow.showAsDropDown(v);
					orderlistTag = true;
				}else{
					_popupWindow.dismiss();
			        orderlistTag = false;
				
				}
				
				
			}
		});
		
        ((ImageView)findViewById(R.id.ketback)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
				finish();
				
			}
		});
        
	    pickLstView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(filterKitEdtTxt.getWindowToken(), 0);
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});
		/**
		 * 在分厨选择页面中按拼音进行菜品的筛选
		 */
		filterKitEdtTxt.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().length() != 0){
					Food[] foods = pickLstView.getSourceData();
					ArrayList<Food> filterFoods = new ArrayList<Food>();
					for(int i = 0; i < foods.length; i++){
						if(String.valueOf(foods[i].pinyin).toLowerCase().contains(s.toString().toLowerCase())||foods[i].name.contains(s.toString())){
							filterFoods.add(foods[i]);
						}
					}
					
					pickLstView.notifyDataChanged(filterFoods.toArray(new Food[filterFoods.size()]), PickFoodListView.TAG_PINYIN);
					
					
				}else{
					if(dialogTag){
						 pickLstView.notifyDataChanged(_filterFoods.toArray(new Food[_filterFoods.size()]), PickFoodListView.TAG_PINYIN);
					}else{
						 pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods, PickFoodListView.TAG_PINYIN);
					}
					  
				
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		/**
		 * 弹出厨房选择页面并筛选出相应的菜品
		 */
		filterKitchen.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				new KitchenSelectDialog(pickLstView).show();
			}
		});
	}
	
	/**
	 * 设置拼音筛选的View
	 */
	private void setupPinyinView(){
		final PickFoodListView pickLstView = (PickFoodListView)findViewById(R.id.pickByPinyinLstView);
		final EditText filterPinyinEdtTxt = (EditText)findViewById(R.id.filterPinyinEdtTxt);
		filterPinyinEdtTxt.setText("");
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods, PickFoodListView.TAG_PINYIN);
		pickLstView.setFoodPickedListener(this);
		
		//已点菜按钮
		ImageView numberOrder = (ImageView)findViewById(R.id.pinorder);
		//已点菜按钮事件
		numberOrder.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(orderlistTag == false){
					 showOrderList();
					_popupWindow.showAsDropDown(v);
					orderlistTag = true;
				}else{
					_popupWindow.dismiss();
			        orderlistTag = false;
				
				}
				
				
			}
		});
        ((ImageView)findViewById(R.id.pinback)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
				finish();
				
			}
		});
        
        pickLstView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(filterPinyinEdtTxt.getWindowToken(), 0);
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});
        
		/**
		 * 按拼音进行菜品的筛选
		 */
		filterPinyinEdtTxt.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().length() != 0){
					ArrayList<Food> filterFoods = new ArrayList<Food>();
					for(int i = 0; i < WirelessOrder.foodMenu.foods.length; i++){
						if(WirelessOrder.foodMenu.foods[i].pinyin != null){
							if(WirelessOrder.foodMenu.foods[i].pinyin.toLowerCase().contains(s.toString().toLowerCase())||WirelessOrder.foodMenu.foods[i].name.contains(s.toString())){
								filterFoods.add(WirelessOrder.foodMenu.foods[i]);
							}
						}else{
							filterFoods.add(WirelessOrder.foodMenu.foods[i]);
						}
					}
					pickLstView.notifyDataChanged(filterFoods.toArray(new Food[filterFoods.size()]), PickFoodListView.TAG_PINYIN);
					
				}else{
					pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods, PickFoodListView.TAG_PINYIN);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
	}
	
	
	/**
	 * 设置临时菜的View
	 */
	private void setTempView(){
		
		_tempLstView = (TempListView)findViewById(R.id.tempListView);
		_tempLstView.notifyDataChanged();
		
		//临时菜返回键
		((ImageView)findViewById(R.id.tempback)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
				finish();
			}
		}); 
		
		//临时菜添加
       ((ImageView)findViewById(R.id.add)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				_tempLstView.addTemp();
			}
		}); 
       
       
       /**
		 * list滚动的时候屏蔽软键盘
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
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	
	/**
	 * 分厨页面中厨房选择的Dialog
	 */
	private class KitchenSelectDialog extends Dialog{

		private List<Department> _deptParent;
		private List<List<Kitchen>> _kitchenChild;
		
		KitchenSelectDialog(final PickFoodListView foodLstView) {
			super(PickFoodActivity.this, R.style.FullHeightDialog);
			
			/**
			 * 将所有菜品进行按厨房编号进行排序
			 */
			Food[] tmpFoods = new Food[WirelessOrder.foodMenu.foods.length];
			System.arraycopy(WirelessOrder.foodMenu.foods, 0, tmpFoods, 0, WirelessOrder.foodMenu.foods.length);
			Arrays.sort(tmpFoods, new Comparator<Food>(){
				@Override
				public int compare(Food food1, Food food2) {
					if(food1.kitchen > food2.kitchen){
						return 1;
					}else if(food1.kitchen < food2.kitchen){
						return -1;
					}else{
						return 0;
					}
				}				
			});
			
			/**
			 * 使用二分查找算法筛选出有菜品的厨房
			 */
			ArrayList<Kitchen> validKitchens = new ArrayList<Kitchen>();
			for(int i = 0; i < WirelessOrder.foodMenu.kitchens.length; i++){
				Food keyFood = new Food();
				keyFood.kitchen = WirelessOrder.foodMenu.kitchens[i].kitchenID;
				int index = Arrays.binarySearch(tmpFoods, keyFood, new Comparator<Food>(){

					public int compare(Food food1, Food food2) {
						if(food1.kitchen > food2.kitchen){
							return 1;
						}else if(food1.kitchen < food2.kitchen){
							return -1;
						}else{
							return 0;
						}
					}
				});
				
				if(index >= 0){
					validKitchens.add(WirelessOrder.foodMenu.kitchens[i]);
				}
			}
			
			/**
			 * 筛选出有菜品的部门
			 */
			_deptParent = new ArrayList<Department>();
			for(int i = 0; i < WirelessOrder.foodMenu.sKitchens.length; i++){
				for(int j = 0; j < validKitchens.size(); j++){
					if(WirelessOrder.foodMenu.sKitchens[i].deptID == validKitchens.get(j).deptID){
						_deptParent.add(WirelessOrder.foodMenu.sKitchens[i]);
						break;
					}
				}
			}
			
			/**
			 * 筛选出部门中有菜品的厨房
			 */
			_kitchenChild = new ArrayList<List<Kitchen>>();
			for(int i = 0; i < _deptParent.size(); i++){
				List<Kitchen> kitchens = new ArrayList<Kitchen>();
				for(int j = 0; j < validKitchens.size(); j++){
					if(validKitchens.get(j).deptID == _deptParent.get(i).deptID) {
						kitchens.add(validKitchens.get(j));
					}
				}
				_kitchenChild.add(kitchens);
			}
			
		

			
			View dialogContent = View.inflate(PickFoodActivity.this, 
					R.layout.expander_list_view, null);
			setTitle("请选择厨房");
			ExpandableListView kitchenLstView = (ExpandableListView)dialogContent.findViewById(R.id.kitchenSelectLstView);
			//kitchenLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
			
			//设置ListView的Adaptor
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
					if(convertView != null){
						view = convertView;
					}else{
						view = View.inflate(PickFoodActivity.this, R.layout.grounp, null);
					}
					
					((TextView)view.findViewById(R.id.mygroup)).setText(_deptParent.get(groupPosition).name);
					
					if(isExpanded){
						((ImageView)view.findViewById(R.id.kitchenarrow)).setBackgroundResource(R.drawable.point);
					}else{
						((ImageView)view.findViewById(R.id.kitchenarrow)).setBackgroundResource(R.drawable.point02);
				
					}
					return view;
				}

				
				@Override
				public View getChildView(int groupPosition, int childPosition,
						boolean isLastChild, View convertView, ViewGroup parent) {
					View view;
					if(convertView != null){
						view = convertView;
					}else{
						view = View.inflate(PickFoodActivity.this, R.layout.child, null);
					}
					((TextView)view.findViewById(R.id.mychild)).setText(_kitchenChild.get(groupPosition).get(childPosition).name);
					return view;
				}

				@Override
				public boolean isChildSelectable(int groupPosition, int childPosition) {
					return true;
				}
			});
			
//			设置展开所有的二级菜单
			for(int i=0;i<_deptParent.size();i++)
				kitchenLstView.expandGroup(i);

			
			/**
			 * 选择某个厨房后，筛选出相应的菜品，并更新ListView
			 */
			kitchenLstView.setOnChildClickListener(new OnChildClickListener() {
				
				public boolean onChildClick(ExpandableListView parent, View v,
											int groupPosition, int childPosition, long id) {
					Kitchen selectedKitchen = _kitchenChild.get(groupPosition).get(childPosition);
					_filterFoods = new ArrayList<Food>();
					for(int i = 0; i < WirelessOrder.foodMenu.foods.length; i++){
						if(WirelessOrder.foodMenu.foods[i].kitchen == selectedKitchen.kitchenID){
							_filterFoods.add(WirelessOrder.foodMenu.foods[i]);
						}
					}
					//选中厨房后从新赋值
					_ketchenName=_kitchenChild.get(groupPosition).get(childPosition).name;
					ketchenName.setText(_ketchenName);
					dialogTag = true;
					
					foodLstView.notifyDataChanged(_filterFoods.toArray(new Food[_filterFoods.size()]),PickFoodListView.TAG_PINYIN);
					dismiss();
					return true;					
				}
			});
			
			Button dialogBackBtn = (Button) dialogContent.findViewById(R.id.dialog_back_btn);
			dialogBackBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
			setContentView(dialogContent);
		}
		
	}


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (_detector.onTouchEvent(event))
        {
            return true;
        } else
        {
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
     * 手势滑动执行方法
     */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {	   
		float scrollX = e1.getX()-e2.getX();
	    if(Math.abs(velocityX) > 200 && velocityY != 0 && Math.abs(scrollX)/Math.abs(e1.getY()-e2.getY()) > 1){

	    	if(scrollX>0){
	    		//此处添加代码用来显示下一个页面
	    		if(_tabHost.getCurrentTab() == 4)
 					return false; 
 				_tabHost.setCurrentTab(_tabHost.getCurrentTab()+1);

	    	}
	    	else{
	    		//此处添加代码用来显示上一个页面
	    		  if(_tabHost.getCurrentTab() == 0)
	 					return false; 
	 				_tabHost.setCurrentTab(_tabHost.getCurrentTab()-1);	
	    	}
	    	
	    	return true;
	    }

	   return false;
	}
	
	
	/**
	 * 点击按钮显示已点菜列表
	 */
	public void showOrderList(){		
		
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		int width = display.getWidth();//获取屏幕宽度
		int heigh = display.getHeight();//获取屏幕高度
		View popupWindow_view = getLayoutInflater().inflate(    //获取自定义布局文件的视图    
                R.layout.orderlistpupowindow, null,false);     
        _popupWindow = new PopupWindow(popupWindow_view, LayoutParams.WRAP_CONTENT-20, heigh/2, false);//创建PopupWindow实例   
        _popupWindow.setOutsideTouchable(true);
        _popupWindow.setAnimationStyle(R.style.popuwindow);
        ListView _orderLstView = (ListView)popupWindow_view.findViewById(R.id.orderpupowindowLstView);
        _orderLstView.setAdapter(new popuwindowadapter(this,_pickFoods));
        
	}
	
	/**
	 * 点击按钮显示已点菜列表adapter
	 */
	public class popuwindowadapter extends BaseAdapter{
		
       private ArrayList<OrderFood> tmpFoods;
       private Context context;
       
		public popuwindowadapter(Context context,ArrayList<OrderFood>  mpickFoods){
			this.context = context;
			this.tmpFoods = mpickFoods;
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return tmpFoods.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder;
			if(convertView == null){
				 convertView = LayoutInflater.from(context).inflate(R.layout.orderpopuwindowitem, null);
				 holder = new Holder();
				 holder.foodname = (TextView)convertView.findViewById(R.id.popuwindowfoodname);
				 convertView.setTag(holder);
			}else{
				holder = (Holder)convertView.getTag();
			}
		    
			OrderFood food = tmpFoods.get(position);
		    holder.foodname.setText(food.name);
			return convertView;
		}
		
		private class Holder
		{
			TextView foodname;
		
		}
	}
}
