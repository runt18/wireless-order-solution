package com.wireless.ui;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.OrderParcel;
import com.wireless.common.WirelessOrder;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.ui.view.PickFoodListView;

public class TabhostActivity extends TabActivity implements PickFoodListView.OnFoodPickedListener{
	
	private final static String TAG_NUMBER = "number";
	private final static String TAG_KITCHEN = "kitchen";
	private final static String TAG_PINYIN = "pinyin";
	
	private ArrayList<OrderFood> _pickFoods = new ArrayList<OrderFood>();
	private TabHost _tabHost;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
							   .setIndicator(createTabIndicator("编号", R.drawable.ic_tab_albums))
							   .setContent(new TabHost.TabContentFactory(){
								   @Override
								   public View createTabContent(String arg0) {
									   return LayoutInflater.from(TabhostActivity.this).inflate(R.layout.number, null);
								   }								   
							   });
		_tabHost.addTab(spec);
		
		//做法Tab
		spec = _tabHost.newTabSpec(TAG_KITCHEN)
					   .setIndicator(createTabIndicator("分厨", R.drawable.ic_tab_artists))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(TabhostActivity.this).inflate(R.layout.kitchen, null);
						   }								   
					   });
		_tabHost.addTab(spec);
		
		//规格Tab
		spec = _tabHost.newTabSpec(TAG_PINYIN)
					   .setIndicator(createTabIndicator("拼音", R.drawable.ic_tab_songs))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(TabhostActivity.this).inflate(R.layout.pinyin, null);
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
		
		_tabHost.setCurrentTabByTag(TAG_PINYIN);

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
	public void OnPicked(OrderFood food) {
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
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods);
		pickLstView.setFoodPickedListener(this);
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
					pickLstView.notifyDataChanged(filterFoods.toArray(new Food[filterFoods.size()]));
					
				}else{
					pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods);
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
		EditText filterKitEdtTxt = (EditText)findViewById(R.id.filterKitchenEdtTxt);
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods);
		pickLstView.setFoodPickedListener(this);
		/**
		 * 按分厨进行菜品的筛选
		 */
		filterKitEdtTxt.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().length() != 0){
					ArrayList<Food> filterFoods = new ArrayList<Food>();
					for(int i = 0; i < WirelessOrder.foodMenu.foods.length; i++){
						if(String.valueOf(WirelessOrder.foodMenu.foods[i].alias_id).startsWith(s.toString().trim())){
							filterFoods.add(WirelessOrder.foodMenu.foods[i]);
						}
					}
					pickLstView.notifyDataChanged(filterFoods.toArray(new Food[filterFoods.size()]));
					
				}else{
					pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
	}
	
	/**
	 * 设置拼音筛选的View
	 */
	private void setupPinyinView(){
		final PickFoodListView pickLstView = (PickFoodListView)findViewById(R.id.pickByPinyinLstView);
		EditText filterPinyinEdtTxt = (EditText)findViewById(R.id.filterPinyinEdtTxt);
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods);
		pickLstView.setFoodPickedListener(this);
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
							if(WirelessOrder.foodMenu.foods[i].pinyin.toLowerCase().contains(s.toString().toLowerCase())){
								filterFoods.add(WirelessOrder.foodMenu.foods[i]);
							}
						}else{
							filterFoods.add(WirelessOrder.foodMenu.foods[i]);
						}
					}
					pickLstView.notifyDataChanged(filterFoods.toArray(new Food[filterFoods.size()]));
					
				}else{
					pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
	}
	
}
