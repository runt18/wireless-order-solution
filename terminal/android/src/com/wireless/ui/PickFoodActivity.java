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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class PickFoodActivity extends TabActivity implements PickFoodListView.OnFoodPickedListener{
	
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
									   return LayoutInflater.from(PickFoodActivity.this).inflate(R.layout.number, null);
								   }								   
							   });
		_tabHost.addTab(spec);
		
		//����Tab
		spec = _tabHost.newTabSpec(TAG_KITCHEN)
					   .setIndicator(createTabIndicator("�ֳ�", R.drawable.ic_tab_artists))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(PickFoodActivity.this).inflate(R.layout.kitchen, null);
						   }								   
					   });
		_tabHost.addTab(spec);
		
		//���Tab
		spec = _tabHost.newTabSpec(TAG_PINYIN)
					   .setIndicator(createTabIndicator("ƴ��", R.drawable.ic_tab_songs))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(PickFoodActivity.this).inflate(R.layout.pinyin, null);
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
		
		_tabHost.setCurrentTabByTag(TAG_NUMBER);
		setupNumberView();
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
	public void onPicked(OrderFood food) {
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
		RelativeLayout filterKitchen = (RelativeLayout)findViewById(R.id.filterKitchenRelaLayout);
		EditText filterKitEdtTxt = (EditText)findViewById(R.id.filterKitchenEdtTxt);
		pickLstView.notifyDataChanged(WirelessOrder.foodMenu.foods);
		pickLstView.setFoodPickedListener(this);
		/**
		 * �ڷֳ�ѡ��ҳ���а���Ž��в�Ʒ��ɸѡ
		 */
		filterKitEdtTxt.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().length() != 0){
					Food[] foods = pickLstView.getSourceData();
					ArrayList<Food> filterFoods = new ArrayList<Food>();
					for(int i = 0; i < foods.length; i++){
						if(String.valueOf(foods[i].alias_id).startsWith(s.toString().trim())){
							filterFoods.add(foods[i]);
						}
					}
					pickLstView.notifyDataChanged(filterFoods.toArray(new Food[filterFoods.size()]));
					
				}else{
					pickLstView.notifyDataChanged(pickLstView.getSourceData());
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		/**
		 * ��������ѡ��ҳ�沢ɸѡ����Ӧ�Ĳ�Ʒ
		 */
		filterKitchen.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				new KitchenSelectDialog(pickLstView).show();
			}
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
	
	/**
	 * �ֳ�ҳ���г���ѡ���Dialog
	 */
	private class KitchenSelectDialog extends Dialog{

		private List<SKitchen> _deptParent;
		private List<List<Kitchen>> _kitchenChild;
		
		KitchenSelectDialog(final PickFoodListView foodLstView) {
			super(PickFoodActivity.this, R.style.FullHeightDialog);
			
			/**
			 * �����в�Ʒ���а�������Ž�������
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
			 * ʹ�ö��ֲ����㷨ɸѡ���в�Ʒ�ĳ���
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
			 * ɸѡ���в�Ʒ�Ĳ���
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
			 * ɸѡ���������в�Ʒ�ĳ���
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
			setTitle("��ѡ�����");
			ExpandableListView kitchenLstView = (ExpandableListView)findViewById(R.id.kitchenSelectLstView);
			kitchenLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
			
			//����ListView��Adaptor
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
			 * ѡ��ĳ��������ɸѡ����Ӧ�Ĳ�Ʒ��������ListView
			 */
			kitchenLstView.setOnChildClickListener(new OnChildClickListener() {
				
				public boolean onChildClick(ExpandableListView parent, View v,
											int groupPosition, int childPosition, long id) {
					Kitchen selectedKitchen = _kitchenChild.get(groupPosition).get(childPosition);
					List<Food> filterFoods = new ArrayList<Food>();
					for(int i = 0; i < WirelessOrder.foodMenu.foods.length; i++){
						if(WirelessOrder.foodMenu.foods[i].kitchen == selectedKitchen.alias_id){
							filterFoods.add(WirelessOrder.foodMenu.foods[i]);
						}
					}
					
					foodLstView.notifyDataChanged(filterFoods.toArray(new Food[filterFoods.size()]));
					dismiss();
					return true;					
				}
			});
		}
		
	}
	
}
