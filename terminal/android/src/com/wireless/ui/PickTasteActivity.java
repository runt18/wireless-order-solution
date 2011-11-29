package com.wireless.ui;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.FoodParcel;
import com.wireless.common.WirelessOrder;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Util;

public class PickTasteActivity extends TabActivity {
	
	private Handler _handler = new Handler(){
		@Override
		public void handleMessage(Message message){
			_tasteTxtView.setText(_selectedFood.name + "-" + _selectedFood.tastePref);
		}
	};
	
	private final static String TAG_TASTE = "taste";
	private final static String TAG_STYLE = "style";
	private final static String TAG_SPEC = "spec";
	
	private OrderFood _selectedFood;
	private TextView _tasteTxtView;
	private TabHost _tabHost;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//get the food parcel from the intent
		FoodParcel foodParcel = getIntent().getParcelableExtra(FoodParcel.KEY_VALUE);
		_selectedFood = foodParcel;
		
		// construct the tab host
		setContentView(R.layout.tastetable);		
		_tabHost = getTabHost();
		
		//口味Tab
		TabSpec spec = _tabHost.newTabSpec(TAG_TASTE)
							   .setIndicator(createTabIndicator("口味", R.drawable.ic_tab_albums))
							   .setContent(new TabHost.TabContentFactory(){
								   @Override
								   public View createTabContent(String arg0) {
									   return LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.taste, null);
								   }								   
							   });
		_tabHost.addTab(spec);
		
		//做法Tab
		spec = _tabHost.newTabSpec(TAG_STYLE)
					   .setIndicator(createTabIndicator("做法", R.drawable.ic_tab_artists))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.style, null);
						   }								   
					   });
		_tabHost.addTab(spec);
		
		//规格Tab
		spec = _tabHost.newTabSpec(TAG_SPEC)
					   .setIndicator(createTabIndicator("规格", R.drawable.ic_tab_songs))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.specs, null);
						   }								   
					   });
		_tabHost.addTab(spec);
		
		/**
		 * Tab切换时更换相应的Adapter，显示不同种类的口味
		 */
		_tabHost.setOnTabChangedListener(new OnTabChangeListener() {			
			@Override
			public void onTabChanged(String tag) {
				if(tag == TAG_TASTE){
					_tasteTxtView = (TextView)findViewById(R.id.foodTasteTxtView);
					ListView tasteLstView = (ListView)findViewById(R.id.tasteLstView);
					tasteLstView.setAdapter(new TasteAdapter(WirelessOrder.foodMenu.tastes));
					
				}else if(tag == TAG_STYLE){
					_tasteTxtView = (TextView)findViewById(R.id.foodStyleTxtView);
					ListView tasteLstView = (ListView)findViewById(R.id.styleLstView);
					tasteLstView.setAdapter(new TasteAdapter(WirelessOrder.foodMenu.styles));
					
				}else if(tag == TAG_SPEC){
					_tasteTxtView = (TextView)findViewById(R.id.foodSpecTxtView);
					ListView tasteLstView = (ListView)findViewById(R.id.specLstView);
					tasteLstView.setAdapter(new TasteAdapter(WirelessOrder.foodMenu.specs));
				}
				_handler.sendEmptyMessage(0);
			}
		});
		
		_tabHost.setCurrentTabByTag(TAG_TASTE);
		_tasteTxtView = (TextView)findViewById(R.id.foodTasteTxtView);
		ListView tasteLstView = (ListView)findViewById(R.id.tasteLstView);
		tasteLstView.setAdapter(new TasteAdapter(WirelessOrder.foodMenu.tastes));
		_handler.sendEmptyMessage(0);
	}


	@Override
	public void onBackPressed(){
		Intent intent = new Intent(); 
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(_selectedFood));
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
	
	private class TasteAdapter extends BaseAdapter{

		private Taste[] _tastes;
		
		TasteAdapter(Taste[] tastes){
			_tastes = tastes;
		}
		
		@Override
		public int getCount() {
			return _tastes.length;
		}

		@Override
		public Object getItem(int position) {
			return _tastes[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view;
			if(convertView == null){
				view = LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.food_item, null);				
			}else{
				view = convertView;
			}
			//set name to taste
			((TextView)view.findViewById(R.id.foodname)).setText(_tastes[position].preference);
			//set the price to taste
			if(_tastes[position].calc == Taste.CALC_RATE){
				((TextView)view.findViewById(R.id.foodprice)).setText(Util.float2Int(_tastes[position].getRate()) + "%");
			}else{
				((TextView)view.findViewById(R.id.foodprice)).setText(Util.CURRENCY_SIGN + Util.float2String2(_tastes[position].getPrice()));
			}
			//set the status to whether the taste is selected
			final CheckBox selectChkBox = (CheckBox)view.findViewById(R.id.chioce);
			selectChkBox.setChecked(false);
			for(int i = 0; i < _selectedFood.tastes.length; i++){
				if(_tastes[position].alias_id == _selectedFood.tastes[i].alias_id){
					selectChkBox.setChecked(true);
					break;
				}
			}
			
			/**
			 * 口味的View操作
			 */			
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(selectChkBox.isChecked()){
						int pos = _selectedFood.removeTaste(_tastes[position]);
						if(pos >= 0){
							selectChkBox.setChecked(false);
							Toast.makeText(PickTasteActivity.this, "删除" + _tastes[position].preference, 0).show();
						}
						
					}else{
						int pos = _selectedFood.addTaste(_tastes[position]);
						if(pos >= 0){
							selectChkBox.setChecked(true);
							Toast.makeText(PickTasteActivity.this, "添加" + _tastes[position].preference, 0).show();
						}else{
							Toast.makeText(PickTasteActivity.this, "最多只能添加" + _selectedFood.tastes.length + "种口味", 0).show();
						}
					}
					_handler.sendEmptyMessage(0);
				}
			});

			return view;
		}
		
	}
}
