package com.wireless.pad;

import java.util.ArrayList;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Util;

public class PickTasteActivity extends TabActivity implements OnGestureListener{
	
	private GestureDetector _detector; 

	public static final String PICK_TASTE_ACTION = "com.wireless_pad.PickTasteActivty.PickTaste";
	public static final String NOT_PICK_TASTE_ACTION = "com.wireless_pad.PickTasteActivty.PickNoTaste";

	private Handler _handler = new Handler(){
		@Override
		public void handleMessage(Message message){
			_tasteTxtView.setText(_selectedFood.toString());
		}
	};
	
	private final static String TAG_TASTE = "taste";
	private final static String TAG_STYLE = "style";
	private final static String TAG_SPEC = "spec";
	
	private OrderFood _selectedFood;
	private TextView _tasteTxtView;
	private TabHost _tabHost;
	private BaseAdapter _tasteAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		 _detector = new GestureDetector(this); 
		
		// construct the tab host
		setContentView(R.layout.tastetable);		
		
		FoodParcel foodParcel = getIntent().getParcelableExtra(FoodParcel.KEY_VALUE);
		_selectedFood = foodParcel;
		// FIXME 
		if(_selectedFood.tmpTaste != null && _selectedFood.tmpTaste.aliasID == Integer.MIN_VALUE){
			_selectedFood.tmpTaste = null;
		}
		
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
					setupTasteView();		
				}else if(tag == TAG_STYLE){
					setupStyleView();
				}else if(tag == TAG_SPEC){
					setupSpecView();
				}
				_handler.sendEmptyMessage(0);
			}
		});
		
		_tabHost.setCurrentTabByTag(TAG_TASTE);
		setupTasteView();
		_handler.sendEmptyMessage(0);	
		
	}	

	/**
	 * 选择口味后，将OrderFood的信息放到Parcel，并发出广播Intent通知OrderActivity或者ChgOrderActivity
	 */
	private void sendPickTasteBoradcast(){
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(_selectedFood));
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setAction(PICK_TASTE_ACTION);
		intent.putExtras(bundle);
		sendBroadcast(intent);	
	}
	
	/**
	 * 删除所选菜品的所有口味
	 */
	private void removeAllTaste(){
		if(_selectedFood.tastes.length > 0){
			for(Taste taste : _selectedFood.tastes.clone()){
				_selectedFood.removeTaste(taste);
			}
			//refresh the taste preference
			_handler.sendEmptyMessage(0);
			//refresh the taste list view
			_tasteAdapter.notifyDataSetChanged();
			//send the broadcast event to notify OrderActivity or ChgOrderActivity
			sendPickTasteBoradcast();
		}
	}
	    //设置口味View
	public void setupTasteView(){
		
		//删除所有口味Button
		((Button)findViewById(R.id.cancelTasteBtn)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				removeAllTaste();
			}
		});
		
		_tasteTxtView = (TextView)findViewById(R.id.foodTasteTxtView);
	    final GridView tasteLstView = (GridView)findViewById(R.id.tasteLstView);
	    tasteLstView.setNumColumns(4);
	   ((EditText)findViewById(R.id.tastesearch)).setText("");
	   
	   _tasteAdapter = new TasteAdapter(WirelessOrder.foodMenu.tastes);
	   tasteLstView.setAdapter(_tasteAdapter);
		
		
		//滚动的时候隐藏输入法
		tasteLstView.setOnScrollListener(new OnScrollListener() {
				
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText)findViewById(R.id.tastesearch)).getWindowToken(), 0);
				}
				
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					
				}
		});
		
		
		/**
		 * 在口味选择页面中按编号进行口味的筛选
		 */
		((EditText)findViewById(R.id.tastesearch)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				ArrayList<Taste> tastes = new ArrayList<Taste>();
				if(s.toString().length() != 0){
				    for(int i = 0; i < WirelessOrder.foodMenu.tastes.length;i++){
				    	 if(WirelessOrder.foodMenu.tastes[i].preference.contains(s.toString().trim())){
				    		 tastes.add(WirelessOrder.foodMenu.tastes[i]);
				    	 }
				    }
				    _tasteAdapter = new TasteAdapter(tastes.toArray(new Taste[tastes.size()]));
				    tasteLstView.setAdapter(_tasteAdapter);
				}else{
					_tasteAdapter = new TasteAdapter(WirelessOrder.foodMenu.tastes);
					tasteLstView.setAdapter(_tasteAdapter);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		
	}
	
	
	//设置做法View
	public void setupStyleView(){
		_tasteTxtView = (TextView)findViewById(R.id.foodStyleTxtView);
    	final GridView styleLstView = (GridView)findViewById(R.id.styleLstView);
    	styleLstView.setNumColumns(4);
    	((EditText)findViewById(R.id.stylesearch)).setText("");
    	
    	_tasteAdapter = new TasteAdapter(WirelessOrder.foodMenu.styles);
		styleLstView.setAdapter(_tasteAdapter);
		
		//删除所有口味Button
		((Button)findViewById(R.id.cancelStyleBtn)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				removeAllTaste();
			}
		});
	    
	    //滚动的时候隐藏输入法
	    styleLstView.setOnScrollListener(new OnScrollListener() {
				
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText)findViewById(R.id.stylesearch)).getWindowToken(), 0);
				}
				
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					
				}
			});
	    
	    /**
		 * 在做法选择页面中按编号进行做法的筛选
		 */
		((EditText)findViewById(R.id.stylesearch)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				ArrayList<Taste> styles = new ArrayList<Taste>();
				if(s.toString().length() != 0){
					 for(int i = 0; i < WirelessOrder.foodMenu.styles.length;i++){
				    	 if(WirelessOrder.foodMenu.styles[i].preference.contains(s.toString().trim())){
				    		 styles.add(WirelessOrder.foodMenu.styles[i]);
				    	 }
				    }
					 _tasteAdapter = new TasteAdapter(styles.toArray(new Taste[styles.size()]));
					styleLstView.setAdapter(_tasteAdapter);
					
				}else{
					_tasteAdapter = new TasteAdapter(WirelessOrder.foodMenu.styles);
					styleLstView.setAdapter(_tasteAdapter);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
	}
	
	//设置规格View
	public void setupSpecView(){
		_tasteTxtView = (TextView)findViewById(R.id.foodSpecTxtView);
		final GridView specLstView = (GridView)findViewById(R.id.specLstView);
		specLstView.setNumColumns(4);
		((EditText)findViewById(R.id.specsearch)).setText("");
		
		_tasteAdapter = new TasteAdapter(WirelessOrder.foodMenu.specs);
		specLstView.setAdapter(_tasteAdapter);
		
		//删除所有口味Button
		((Button)findViewById(R.id.cancelSpecBtn)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				removeAllTaste();
			}
		});
	    
	    //滚动的时候隐藏输入法
	    specLstView.setOnScrollListener(new OnScrollListener() {
				
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText)findViewById(R.id.specsearch)).getWindowToken(), 0);
				}
				
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					
				}
			});
	    /**
		 * 在规格选择页面中按编号进行规格的筛选
		 */
		((EditText)findViewById(R.id.specsearch)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				ArrayList<Taste> specs = new ArrayList<Taste>();
				if(s.toString().length() != 0){
					 for(int i = 0; i < WirelessOrder.foodMenu.specs.length;i++){
				    	 if(WirelessOrder.foodMenu.specs[i].preference.contains(s.toString().trim())){
				    		 specs.add(WirelessOrder.foodMenu.specs[i]);
				    	 }
				    }
					 _tasteAdapter = new TasteAdapter(specs.toArray(new Taste[specs.size()]));
					specLstView.setAdapter(_tasteAdapter);
					
				}else{
					_tasteAdapter = new TasteAdapter(WirelessOrder.foodMenu.specs);
					specLstView.setAdapter(_tasteAdapter);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
	}
	
	/**
	 * Create the tab indicator
	 * @param text
	 * @param drawable
	 * @return
	 */
	private View createTabIndicator(String text, int drawable) {
		View view = LayoutInflater.from(_tabHost.getContext()).inflate(R.layout.tb_bg, null);
//		((TextView)view.findViewById(R.id.tabsText)).setText(text);
//		((ImageView) view.findViewById(R.id.icon)).setImageResource(drawable);
		
		android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		if(text.equals("口味")){
			lp.setMargins(15, 10, 2, 10);
			((Button)view.findViewById(R.id.tabicon)).setLayoutParams(lp);
		}
		if(text.equals("规格")){
			lp.setMargins(2, 10, 15, 10);
			((Button)view.findViewById(R.id.tabicon)).setLayoutParams(lp);
		}
		
		((Button)view.findViewById(R.id.tabicon)).setText(text);
		((Button)view.findViewById(R.id.tabicon)).setBackgroundResource(drawable);
		((Button)view.findViewById(R.id.tabicon)).setClickable(false);
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
			//set number to taste
			((TextView)view.findViewById(R.id.nums)).setText(String.valueOf(_tastes[position].aliasID));
			//set the price to taste
			if(_tastes[position].calc == Taste.CALC_RATE){
				((TextView)view.findViewById(R.id.foodprice)).setText(Util.float2Int(_tastes[position].getRate()) + "%");
			}else{
				((TextView)view.findViewById(R.id.foodprice)).setText(Util.CURRENCY_SIGN + Util.float2String2(_tastes[position].getPrice()));
			}
			//set the status to whether the taste is selected
			final CheckBox selectChkBox = (CheckBox)view.findViewById(R.id.chioce);
			selectChkBox.setChecked(false);
			selectChkBox.requestFocus();
			for(int i = 0; i < _selectedFood.tastes.length; i++){
				if(_tastes[position].aliasID == _selectedFood.tastes[i].aliasID){
					selectChkBox.setChecked(true);
					break;
				}
			}
			
			selectChkBox.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(selectChkBox.isChecked()){
						int pos = _selectedFood.addTaste(_tastes[position]);
						if(pos >= 0){
							sendPickTasteBoradcast();
							Toast.makeText(PickTasteActivity.this, "添加" + _tastes[position].preference, 0).show();
						}else{
							Toast.makeText(PickTasteActivity.this, "最多只能添加" + _selectedFood.tastes.length + "种口味", 0).show();
							selectChkBox.setChecked(false);
						}						
						
					}else{
						int pos = _selectedFood.removeTaste(_tastes[position]);
						if(pos >= 0){
							sendPickTasteBoradcast();
							Toast.makeText(PickTasteActivity.this, "删除" + _tastes[position].preference, 0).show();
						}
					}
					_handler.sendEmptyMessage(0);
				}
			});
			
			/**
			 * 口味的View操作
			 */			
			view.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
			        
					if(selectChkBox.isChecked()){
						int pos = _selectedFood.removeTaste(_tastes[position]);
						if(pos >= 0){
							sendPickTasteBoradcast();
							selectChkBox.setChecked(false);
							Toast.makeText(PickTasteActivity.this, "删除" + _tastes[position].preference, 0).show();
						}
						((LinearLayout)arg0.findViewById(R.id.item_body)).setBackgroundResource(R.drawable.item_bg_selector);
					}else{
						((LinearLayout)arg0.findViewById(R.id.item_body)).setBackgroundResource(R.drawable.item_bg2);
						int pos = _selectedFood.addTaste(_tastes[position]);
						if(pos >= 0){
							sendPickTasteBoradcast();
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
    @Override  
    public boolean onTouchEvent(MotionEvent event) {  
      
        return this._detector.onTouchEvent(event);  
    }  
    
    @Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
    	_detector.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}


	@Override
	public boolean onDown(MotionEvent e) {
		return true;
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
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,	float velocityY) {
		float scrollX = e1.getX()-e2.getX();
	    if(Math.abs(velocityX) > 200 && velocityY != 0 && Math.abs(scrollX)/Math.abs(e1.getY()-e2.getY()) > 1){
	    	if(scrollX>0){
	    		//此处添加代码用来显示下一个页面
	    		if(_tabHost.getCurrentTab() == 3)
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
	
	

	
	
}
