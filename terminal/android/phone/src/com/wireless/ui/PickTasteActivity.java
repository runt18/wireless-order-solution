package com.wireless.ui;

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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
	int _currentView = 0; 
	private GestureDetector _detector; 


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
		
		 _detector = new GestureDetector(this); 
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
					setTasteView();		
				}else if(tag == TAG_STYLE){
					setStyleView();
				}else if(tag == TAG_SPEC){
					setSpecView();
				}
				_handler.sendEmptyMessage(0);
			}
		});
		
		_tabHost.setCurrentTabByTag(TAG_TASTE);
		setTasteView();
		_handler.sendEmptyMessage(0);	
		
	}

   //设置口味View
	public void setTasteView(){
		_tasteTxtView = (TextView)findViewById(R.id.foodTasteTxtView);
	    final ListView tasteLstView = (ListView)findViewById(R.id.tasteLstView);
	   ((EditText)findViewById(R.id.tastesearch)).setText("");
		tasteLstView.setAdapter(new TasteAdapter(WirelessOrder.foodMenu.tastes));
		
		//口味返回按钮
		((ImageView)findViewById(R.id.tasteback)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();	
				finish();				
			}
		});
		
		
		
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
				    	 if(String.valueOf(WirelessOrder.foodMenu.tastes[i].tasteAlias).startsWith(s.toString().trim())){
				    		 tastes.add(WirelessOrder.foodMenu.tastes[i]);
				    	 }
				    }
				    tasteLstView.setAdapter(new TasteAdapter(tastes.toArray(new Taste[tastes.size()])));
				}else{
					tasteLstView.setAdapter(new TasteAdapter(WirelessOrder.foodMenu.tastes));
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		
	}
	
	
	//设置做法View
	public void setStyleView(){
		_tasteTxtView = (TextView)findViewById(R.id.foodStyleTxtView);
    	final ListView styleLstView = (ListView)findViewById(R.id.styleLstView);
    	((EditText)findViewById(R.id.stylesearch)).setText("");
		styleLstView.setAdapter(new TasteAdapter(WirelessOrder.foodMenu.styles));
		
		//做法返回按钮
	    ((ImageView)findViewById(R.id.styleback)).setOnClickListener(new View.OnClickListener() {				
			@Override
			public void onClick(View v) {
				onBackPressed();	
				finish();	
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
				    	 if(String.valueOf(WirelessOrder.foodMenu.styles[i].tasteAlias).startsWith(s.toString().trim())){
				    		 styles.add(WirelessOrder.foodMenu.styles[i]);
				    	 }
				    }
					styleLstView.setAdapter(new TasteAdapter(styles.toArray(new Taste[styles.size()])));
					
				}else{
					styleLstView.setAdapter(new TasteAdapter(WirelessOrder.foodMenu.styles));
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
	}
	
	//设置规格View
	public void setSpecView(){
		_tasteTxtView = (TextView)findViewById(R.id.foodSpecTxtView);
		final ListView specLstView = (ListView)findViewById(R.id.specLstView);
		((EditText)findViewById(R.id.specsearch)).setText("");
		specLstView.setAdapter(new TasteAdapter(WirelessOrder.foodMenu.specs));
		
		//做法返回按钮
	    ((ImageView)findViewById(R.id.specback)).setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
				onBackPressed();	
				finish();
					
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
				    	 if(String.valueOf(WirelessOrder.foodMenu.specs[i].tasteAlias).startsWith(s.toString().trim())){
				    		 specs.add(WirelessOrder.foodMenu.specs[i]);
				    	 }
				    }
					 specLstView.setAdapter(new TasteAdapter(specs.toArray(new Taste[specs.size()])));
					
				}else{
					specLstView.setAdapter(new TasteAdapter(WirelessOrder.foodMenu.specs));
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
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
			//set number to taste
			((TextView)view.findViewById(R.id.nums)).setText(String.valueOf(_tastes[position].tasteAlias));
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
				if(_tastes[position].tasteAlias == _selectedFood.tastes[i].tasteAlias){
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
							Toast.makeText(PickTasteActivity.this, "添加" + _tastes[position].preference, 0).show();
						}else{
							Toast.makeText(PickTasteActivity.this, "最多只能添加" + _selectedFood.tastes.length + "种口味", 0).show();
							selectChkBox.setChecked(false);
						}						
						
					}else{
						int pos = _selectedFood.removeTaste(_tastes[position]);
						if(pos >= 0){
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
	
		
		//	/*
		//	 * 手势切换添加动画效果
		//	 */
		//	Animation anim = AnimationUtils.loadAnimation(PickTasteActivity.this, android.R.anim.slide_in_left);
		//	_tabHost.startAnimation(anim);

	
	
}
