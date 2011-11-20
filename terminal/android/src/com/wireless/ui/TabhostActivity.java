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

		//ȡ���µ�������еĲ�ƷList�������浽pickFood��List��
		OrderParcel orderParcel = getIntent().getParcelableExtra(OrderParcel.KEY_VALUE);
		for(int i = 0; i < orderParcel.foods.length; i++){
			_pickFoods.add(orderParcel.foods[i]);
		}
		
		// construct the tab host
		_tabHost = getTabHost();
		
		//���Tab
		TabSpec spec = _tabHost.newTabSpec(TAG_NUMBER)
							   .setIndicator(createTabIndicator("���", R.drawable.ic_tab_albums))
							   .setContent(new TabHost.TabContentFactory(){
								   @Override
								   public View createTabContent(String arg0) {
									   return LayoutInflater.from(TabhostActivity.this).inflate(R.layout.number, null);
								   }								   
							   });
		_tabHost.addTab(spec);
		
		//����Tab
		spec = _tabHost.newTabSpec(TAG_KITCHEN)
					   .setIndicator(createTabIndicator("�ֳ�", R.drawable.ic_tab_artists))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(TabhostActivity.this).inflate(R.layout.kitchen, null);
						   }								   
					   });
		_tabHost.addTab(spec);
		
		//���Tab
		spec = _tabHost.newTabSpec(TAG_PINYIN)
					   .setIndicator(createTabIndicator("ƴ��", R.drawable.ic_tab_songs))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(TabhostActivity.this).inflate(R.layout.pinyin, null);
						   }								   
					   });
		_tabHost.addTab(spec);
		
		/**
		 * Tab�л�ʱ������Ӧ��Adapter����ʾ��ͬ�ĵ�˷�ʽ
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
	 * ����ʱ���µ��Ʒ��List���ص���һ��Activity
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
	 * ͨ��"���"��"�ֳ�"��"ƴ��"��ʽѡ�в�Ʒ��
	 * ����Ʒ���浽List�У��˳�ʱ����List��Ϊ������ص���һ��Activity
	 * @param food
	 * 			ѡ�в�Ʒ����Ϣ
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
       				Toast.makeText(this, "�Բ���" + food.name + "���ֻ�ܵ�255��", 0).show();
       				pickedFood.setCount(new Float(255));
       			}else{
       				Toast.makeText(this, "���" + food.name + Util.float2String2(food.getCount()) + "��", 0).show();
       				pickedFood.setCount(orderAmount);        				
       			}
				isExist = true;
				break;
			}
		}
		if(!isExist){
			if(food.getCount() > 255){
				Toast.makeText(this, "�Բ���" + food.name + "���ֻ�ܵ�255��", 0).show();
			}else{
				Toast.makeText(this, "����" + food.name + Util.float2String2(food.getCount()) + "��", 0).show();
			}
			_pickFoods.add(food);			
		}
	}
	
	/**
	 * ���ñ��ɸѡ��View
	 */
	private void setupNumberView(){
		final PickFoodListView pickLstView = (PickFoodListView)findViewById(R.id.pickByNumLstView);
		EditText filterNumEdtTxt = ((EditText)findViewById(R.id.filterNumEdtTxt));
		filterNumEdtTxt.setText("");
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods);
		pickLstView.setFoodPickedListener(this);
		/**
		 * ����Ž��в�Ʒ��ɸѡ
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
	 * ���÷ֳ�ɸѡ��View
	 */
	private void setupKitchenView(){
		final PickFoodListView pickLstView = (PickFoodListView)findViewById(R.id.pickByKitchenLstView);
		EditText filterKitEdtTxt = (EditText)findViewById(R.id.filterKitchenEdtTxt);
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods);
		pickLstView.setFoodPickedListener(this);
		/**
		 * ���ֳ����в�Ʒ��ɸѡ
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
	 * ����ƴ��ɸѡ��View
	 */
	private void setupPinyinView(){
		final PickFoodListView pickLstView = (PickFoodListView)findViewById(R.id.pickByPinyinLstView);
		EditText filterPinyinEdtTxt = (EditText)findViewById(R.id.filterPinyinEdtTxt);
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods);
		pickLstView.setFoodPickedListener(this);
		/**
		 * ��ƴ�����в�Ʒ��ɸѡ
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
