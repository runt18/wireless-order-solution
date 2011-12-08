package com.wireless.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.OnGestureListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.OrderParcel;
import com.wireless.common.WirelessOrder;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.SKitchen;
import com.wireless.protocol.Util;
import com.wireless.ui.view.PickFoodListView;

public class PickFoodActivity extends TabActivity implements PickFoodListView.OnFoodPickedListener,OnGestureListener{
	
	private final static String TAG_NUMBER = "number";
	private final static String TAG_KITCHEN = "kitchen";
	private final static String TAG_PINYIN = "pinyin";
	
	private ArrayList<OrderFood> _pickFoods = new ArrayList<OrderFood>();
	private TabHost _tabHost;
	private GestureDetector _detector; 
    private String _ketchenName;
    private TextView ketchenName;
    boolean dialogTag = false;
    private List<Food> _filterFoods;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 _detector = new GestureDetector(this); 
		
		setContentView(R.layout.table);
		
		
		
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
		
		//做法Tab
		spec = _tabHost.newTabSpec(TAG_KITCHEN)
					   .setIndicator(createTabIndicator("分厨", R.drawable.kitchen_selector))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(PickFoodActivity.this).inflate(R.layout.kitchen, null);
						   }								   
					   });
		_tabHost.addTab(spec);
		
		//规格Tab
		spec = _tabHost.newTabSpec(TAG_PINYIN)
					   .setIndicator(createTabIndicator("拼音", R.drawable.pinyin_selector))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(PickFoodActivity.this).inflate(R.layout.pinyin, null);
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
		boolean isExist = false;
		Iterator<OrderFood> iter = _pickFoods.iterator();
		while(iter.hasNext()){
			OrderFood pickedFood = iter.next();
			if(pickedFood.equals(food)){
				float orderAmount = food.getCount() + pickedFood.getCount();
       			if(orderAmount > 255){
       				Toast.makeText(this, "对不起，" + food.name + "最多只能点255份", 0).show();
       				pickedFood.setCount(new Float(255));
       			}else{
       				Toast.makeText(this, "添加" + food.name + Util.float2String2(food.getCount()) + "份", 0).show();
       				pickedFood.setCount(orderAmount);        				
       			}
				isExist = true;
				break;
			}
		}
		if(!isExist){
			if(food.getCount() > 255){
				Toast.makeText(this, "对不起，" + food.name + "最多只能点255份", 0).show();
			}else{
				Toast.makeText(this, "新增" + food.name + Util.float2String2(food.getCount()) + "份", 0).show();
			}
			_pickFoods.add(food);			
		}
	}
	
	/**
	 * 设置编号筛选的View
	 */
	private void setupNumberView(){
		final PickFoodListView pickLstView = (PickFoodListView)findViewById(R.id.pickByNumLstView);
		EditText filterNumEdtTxt = ((EditText)findViewById(R.id.filterNumEdtTxt));
		filterNumEdtTxt.setText("");
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods, PickFoodListView.TAG_NUM);
		pickLstView.setFoodPickedListener(this);
		((ImageView)findViewById(R.id.numback)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
				finish();
				
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
		EditText filterKitEdtTxt = (EditText)findViewById(R.id.filterKitchenEdtTxt);
		//初始化的时候厨房默认显示的厨房信息
		ketchenName = (TextView)findViewById(R.id.Spinner01);
		ketchenName.setText("全部");
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods, PickFoodListView.TAG_PINYIN);
		pickLstView.setFoodPickedListener(this);
        ((ImageView)findViewById(R.id.ketback)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
				finish();
				
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
		EditText filterPinyinEdtTxt = (EditText)findViewById(R.id.filterPinyinEdtTxt);
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods, PickFoodListView.TAG_PINYIN);
		pickLstView.setFoodPickedListener(this);
        ((ImageView)findViewById(R.id.pinback)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
				finish();
				
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
	 * 分厨页面中厨房选择的Dialog
	 */
	private class KitchenSelectDialog extends Dialog{

		private List<SKitchen> _deptParent;
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
				keyFood.kitchen = WirelessOrder.foodMenu.kitchens[i].alias_id;
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
			_deptParent = new ArrayList<SKitchen>();
			for(int i = 0; i < WirelessOrder.foodMenu.sKitchens.length; i++){
				for(int j = 0; j < validKitchens.size(); j++){
					if(WirelessOrder.foodMenu.sKitchens[i].alias_id == validKitchens.get(j).skitchen_id){
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
					if(validKitchens.get(j).skitchen_id == _deptParent.get(i).alias_id) {
						kitchens.add(validKitchens.get(j));
					}
				}
				_kitchenChild.add(kitchens);
			}
			
			setContentView(R.layout.expander_list_view);
			setTitle("请选择厨房");
			ExpandableListView kitchenLstView = (ExpandableListView)findViewById(R.id.kitchenSelectLstView);
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
			
			/**
			 * 选择某个厨房后，筛选出相应的菜品，并更新ListView
			 */
			kitchenLstView.setOnChildClickListener(new OnChildClickListener() {
				
				public boolean onChildClick(ExpandableListView parent, View v,
											int groupPosition, int childPosition, long id) {
					Kitchen selectedKitchen = _kitchenChild.get(groupPosition).get(childPosition);
					_filterFoods = new ArrayList<Food>();
					for(int i = 0; i < WirelessOrder.foodMenu.foods.length; i++){
						if(WirelessOrder.foodMenu.foods[i].kitchen == selectedKitchen.alias_id){
							_filterFoods.add(WirelessOrder.foodMenu.foods[i]);
						}
					}
					//选中厨房后从新赋值
					_ketchenName=_kitchenChild.get(groupPosition).get(childPosition).name;
					ketchenName.setText(_ketchenName);
					dialogTag = true;
					
					foodLstView.notifyDataChanged(_filterFoods.toArray(new Food[_filterFoods.size()]), PickFoodListView.TAG_PINYIN);
					dismiss();
					return true;					
				}
			});
		}
		
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return true;
	}
	 @Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
	    	_detector.onTouchEvent(ev);
			return super.dispatchTouchEvent(ev);
	}
	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}
    /*
     * 手势滑动执行方法
     */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
	        if(e1.getY()-e2.getY() > 80){
	        	return false;
	        }else{
	        	 if(e1.getX()-e2.getX() > 280){	   
	 				if(_tabHost.getCurrentTab() == 3)
	 					return false; 
	 				_tabHost.setCurrentTab(_tabHost.getCurrentTab()+1);

	 			 }else{	    
	 		      if(_tabHost.getCurrentTab() == 0)
	 					return false; 
	 				_tabHost.setCurrentTab(_tabHost.getCurrentTab()-1);		
	 		    		
	 			} 	
	        	 return true;  
	        }
	}
	        

	
}
