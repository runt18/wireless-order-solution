package com.wireless.pad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.protocol.OrderFood;

public class PickTasteActivity extends TabActivity{
	
	public static final String PICK_TASTE_ACTION = "com.wireless_pad.PickTasteActivty.PickTaste";
	public static final String NOT_PICK_TASTE_ACTION = "com.wireless_pad.PickTasteActivty.PickNoTaste";

	private Handler _handler = new Handler(){
		@Override
		public void handleMessage(Message message){
			_tasteTxtView.setText(_selectedFood.toString());
		}
	};
	
	private final static String TAG_POP = "pop";
	private final static String TAG_TASTE = "taste";
	private final static String TAG_STYLE = "style";
	private final static String TAG_SPEC = "spec";
	private final static String TAG_PINZHU = "pinzhu";
	
	private OrderFood _selectedFood;
	private TextView _tasteTxtView;
	private TabHost _tabHost;
	private BaseAdapter _tasteAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// construct the tab host
		setContentView(R.layout.tastetable);		
		
		OrderFoodParcel foodParcel = getIntent().getParcelableExtra(OrderFoodParcel.KEY_VALUE);
		_selectedFood = foodParcel;
		// FIXME 
//		if(_selectedFood.tmpTaste != null && _selectedFood.tmpTaste.aliasID == Integer.MIN_VALUE){
//			_selectedFood.tmpTaste = null;
//		}
		
		_tabHost = getTabHost();
		
		//常用Tab
		TabSpec spec = _tabHost.newTabSpec(TAG_POP)
					   .setIndicator(createTabIndicator("常用", R.drawable.kitchen_selector))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.pop_taste, null);
						   }								   
					   });
		_tabHost.addTab(spec);
		
		//口味Tab
		spec = _tabHost.newTabSpec(TAG_TASTE)
					   .setIndicator(createTabIndicator("口味", R.drawable.number_selector))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.taste, null);
						   }								   
					   });
		_tabHost.addTab(spec);
		
		
		
		//做法Tab
//		spec = _tabHost.newTabSpec(TAG_STYLE)
//					   .setIndicator(createTabIndicator("做法", R.drawable.kitchen_selector))
//					   .setContent(new TabHost.TabContentFactory(){
//						   @Override
//						   public View createTabContent(String arg0) {
//							   return LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.style, null);
//						   }								   
//					   });
//		_tabHost.addTab(spec);
		
		
		
		//规格Tab
		spec = _tabHost.newTabSpec(TAG_SPEC)
					   .setIndicator(createTabIndicator("规格",  R.drawable.pinyin_selector))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.specs, null);
						   }								   
					   });
		_tabHost.addTab(spec);
		
		

		//品注Tab
		spec = _tabHost.newTabSpec(TAG_PINZHU)
					   .setIndicator(createTabIndicator("品注",R.drawable.occasional_selector))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.pinzhu, null);
						   }								   
					   });
		_tabHost.addTab(spec);
		
		/**
		 * Tab切换时更换相应的Adapter，显示不同种类的口味
		 */
		_tabHost.setOnTabChangedListener(new OnTabChangeListener() {			
			@Override
			public void onTabChanged(String tag) {
				if(tag == TAG_POP){
					setupPopTasteView();
				}else if(tag == TAG_TASTE){
					setupTasteView();		
				}else if(tag == TAG_STYLE){
					setupStyleView();
				}else if(tag == TAG_SPEC){
					setupSpecView();
				}else if(tag == TAG_PINZHU){
					setupPinZhuView();
				}
				_handler.sendEmptyMessage(0);
			}
		});
		
		if(_selectedFood.getPopTastes().length != 0){
			_tabHost.setCurrentTabByTag(TAG_POP);
			setupPopTasteView();
		}else{
			_tabHost.setCurrentTabByTag(TAG_TASTE);
			setupTasteView();
		}
		
//		_tabHost.setCurrentTabByTag(TAG_TASTE);
//		setupTasteView();

		_handler.sendEmptyMessage(0);	
		
	}	

	/**
	 * 选择口味后，将OrderFood的信息放到Parcel，并发出广播Intent通知OrderActivity或者ChgOrderActivity
	 */
	private void sendPickTasteBoradcast(){
		Bundle bundle = new Bundle();
		bundle.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(_selectedFood));
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
		if(_selectedFood.hasTaste()){
			
			_selectedFood.setTasteGroup(null);
			
			//refresh the taste getPreference()
			_handler.sendEmptyMessage(0);
			//refresh the taste list view
			_tasteAdapter.notifyDataSetChanged();
			//send the broadcast event to notify OrderActivity or ChgOrderActivity
			sendPickTasteBoradcast();
		}
	}
	
   //设置口味View
	private void setupPopTasteView(){
		
		//删除所有口味Button
		((Button)findViewById(R.id.cancelPopBtn)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				removeAllTaste();
			}
		});
		
		_tasteTxtView = (TextView)findViewById(R.id.foodPopTasteTxtView);
	    final GridView popLstView = (GridView)findViewById(R.id.popLstView);
	    popLstView.setNumColumns(4);
	   ((EditText)findViewById(R.id.popSrchEdtTxt)).setText("");
	   
	   _tasteAdapter = new TasteAdapter(Arrays.asList(_selectedFood.getPopTastes()));
	   popLstView.setAdapter(_tasteAdapter);
		
		
		//滚动的时候隐藏输入法
		popLstView.setOnScrollListener(new OnScrollListener() {
				
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText)findViewById(R.id.tastesearch)).getWindowToken(), 0);
			}
				
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		
			}
		});
		
		
		/**
		 * 在口味选择页面中按编号进行口味的筛选
		 */
		((EditText)findViewById(R.id.popSrchEdtTxt)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				ArrayList<Taste> popTastes = new ArrayList<Taste>();
				if(s.toString().length() != 0){
					 for(int i = 0; i < _selectedFood.getPopTastes().length; i++){
				    	 if(_selectedFood.getPopTastes()[i].getPreference().contains(s.toString().trim())){
				    		 popTastes.add(_selectedFood.getPopTastes()[i]);
				    	 }
				    }
					 
					_tasteAdapter = new TasteAdapter(popTastes);
					popLstView.setAdapter(_tasteAdapter);
					
				}else{
					_tasteAdapter = new TasteAdapter(Arrays.asList(_selectedFood.getPopTastes()));
					popLstView.setAdapter(_tasteAdapter);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});			
	}
	    //设置口味View
	private void setupTasteView(){
		
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
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		
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
					for(Taste t : WirelessOrder.foodMenu.tastes){
				    	 if(t.getPreference().contains(s.toString().trim())){
				    		 tastes.add(t);
				    	 }
				    }
				    _tasteAdapter = new TasteAdapter(tastes);
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
	private void setupStyleView(){
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
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					
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
					for(Taste style : WirelessOrder.foodMenu.styles){
						if(style.getPreference().contains(s.toString().trim())){
				    		 styles.add(style);
				    	 }
				    }
					 _tasteAdapter = new TasteAdapter(styles);
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
	private void setupSpecView(){
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
					for(Taste spec : WirelessOrder.foodMenu.specs){
				    	 if(spec.getPreference().contains(s.toString().trim())){
				    		 specs.add(spec);
				    	 }
				    }
					 _tasteAdapter = new TasteAdapter(specs);
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
	
	//设置品注View
	private void setupPinZhuView(){
	
		final EditText pinZhuEdtTxt = ((EditText)findViewById(R.id.pinZhuEdtTxt));
		final EditText priceEdtTxt = ((EditText)findViewById(R.id.priceEdtTxt));
		pinZhuEdtTxt.requestFocus();
		
//		if(_selectedFood.tmpTaste != null){
//			pinZhuEdtTxt.setText(_selectedFood.tmpTaste.getPreference());
//			priceEdtTxt.setText(_selectedFood.tmpTaste.getPrice().toString());
//		}
		if(_selectedFood.hasTmpTaste()){
			pinZhuEdtTxt.setText(_selectedFood.getTasteGroup().getTmpTastePref());
			priceEdtTxt.setText(NumericUtil.float2String2(_selectedFood.getTasteGroup().getTmpTastePrice()));
		}
		
		//删除所有口味Button
		((Button)findViewById(R.id.cancelPinZhuBtn)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pinZhuEdtTxt.setText("");
				priceEdtTxt.setText("");
				removeAllTaste();
			}
		});
		
		//品注的EditText的处理函数
		pinZhuEdtTxt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				String tmpTasteValue = s.toString().trim();
				if(tmpTasteValue.length() != 0){
					if(!_selectedFood.hasTaste()){
						_selectedFood.makeTasteGroup();
					}
					Taste tmpTaste = new Taste();
					tmpTaste.setPreference(tmpTasteValue);
					_selectedFood.getTasteGroup().setTmpTaste(tmpTaste);
					
				}else{
					if(_selectedFood.hasTaste()){
						_selectedFood.getTasteGroup().setTmpTaste(null);
					}
				}
				sendPickTasteBoradcast();
				_handler.sendEmptyMessage(0);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
		});
		
		//价格EditText的处理函数
		priceEdtTxt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				if(_selectedFood.hasTmpTaste()){
					try{
						if(s.length() == 0){
							_selectedFood.getTasteGroup().getTmpTaste().setPrice(Float.valueOf(0));
							sendPickTasteBoradcast();
						}else{
							Float price = Float.valueOf(s.toString());
							if(price >= 0 && price < 9999){
								_selectedFood.getTasteGroup().getTmpTaste().setPrice(price);
								sendPickTasteBoradcast();
							}else{
								priceEdtTxt.setText(_selectedFood.getTasteGroup().getTmpTaste().getPrice() > 9999 ? "" : NumericUtil.float2String2(_selectedFood.getTasteGroup().getTmpTaste().getPrice()));
								priceEdtTxt.setSelection(priceEdtTxt.getText().length());
								Toast.makeText(PickTasteActivity.this, "临时口味的价格范围是0～9999", Toast.LENGTH_SHORT).show();
							}
						}
					}catch(NumberFormatException e){
						priceEdtTxt.setText(_selectedFood.getTasteGroup().getTmpTaste().getPrice() > 9999 ? "" : NumericUtil.float2String2(_selectedFood.getTasteGroup().getTmpTaste().getPrice()));
						priceEdtTxt.setSelection(priceEdtTxt.getText().length());
						Toast.makeText(PickTasteActivity.this, "临时口味的价钱格式不正确，请重新输入", Toast.LENGTH_SHORT).show();
					}
				}else{
					Toast.makeText(PickTasteActivity.this, "请先输入临时口味", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
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
			lp.setMargins(15, 10, 0, 10);
			((Button)view.findViewById(R.id.tabicon)).setLayoutParams(lp);
		}
		if(text.equals("品注")){
			lp.setMargins(0, 10, 15, 10);
			((Button)view.findViewById(R.id.tabicon)).setLayoutParams(lp);
		}
		
		((Button)view.findViewById(R.id.tabicon)).setText(text);
		((Button)view.findViewById(R.id.tabicon)).setBackgroundResource(drawable);
		((Button)view.findViewById(R.id.tabicon)).setClickable(false);
		return view;
	}
	
	private class TasteAdapter extends BaseAdapter{

		private List<Taste> _tastes;
		
		TasteAdapter(List<Taste> tastes){
			_tastes = tastes;
		}
		
		@Override
		public int getCount() {
			return _tastes.size();
		}

		@Override
		public Object getItem(int position) {
			return _tastes.get(position);
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
			((TextView)view.findViewById(R.id.foodname)).setText(_tastes.get(position).getPreference());
			//set number to taste
			((TextView)view.findViewById(R.id.nums)).setText(String.valueOf(_tastes.get(position).getAliasId()));
			//set the price to taste
			if(_tastes.get(position).isCalcByRate()){
				((TextView)view.findViewById(R.id.foodprice)).setText(NumericUtil.float2Int(_tastes.get(position).getRate()) + "%");
			}else{
				((TextView)view.findViewById(R.id.foodprice)).setText(NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(_tastes.get(position).getPrice()));
			}
			//set the status to whether the taste is selected
			final CheckBox selectChkBox = (CheckBox)view.findViewById(R.id.chioce);
			selectChkBox.setChecked(false);
			selectChkBox.requestFocus();
			if(_selectedFood.hasTaste()){
				for(Taste t : _selectedFood.getTasteGroup().getTastes()){
					if(t.equals(_tastes.get(position))){
						selectChkBox.setChecked(true);
						((LinearLayout)view.findViewById(R.id.item_body)).setBackgroundResource(R.drawable.item_bg2);
						break;
					}else{
						selectChkBox.setChecked(false);
						((LinearLayout)view.findViewById(R.id.item_body)).setBackgroundResource(R.drawable.item_bg_selector);
					}
				}
			}
			
			selectChkBox.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(selectChkBox.isChecked()){
						if(!_selectedFood.hasTaste()){
							_selectedFood.makeTasteGroup();
						}
						if(_selectedFood.getTasteGroup().addTaste(_tastes.get(position))){
							sendPickTasteBoradcast();
							Toast.makeText(PickTasteActivity.this, "添加" + _tastes.get(position).getPreference(), Toast.LENGTH_SHORT).show();							
						}
						
					}else{
						if(_selectedFood.hasNormalTaste()){
							if(_selectedFood.getTasteGroup().removeTaste(_tastes.get(position))){
								sendPickTasteBoradcast();
								Toast.makeText(PickTasteActivity.this, "删除" + _tastes.get(position).getPreference(), Toast.LENGTH_SHORT).show();								
							}
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
						if(_selectedFood.hasNormalTaste()){
							if(_selectedFood.getTasteGroup().removeTaste(_tastes.get(position))){
								sendPickTasteBoradcast();
								selectChkBox.setChecked(false);
								Toast.makeText(PickTasteActivity.this, "删除" + _tastes.get(position).getPreference(), Toast.LENGTH_SHORT).show();
								
							}
						}
						((LinearLayout)arg0.findViewById(R.id.item_body)).setBackgroundResource(R.drawable.item_bg_selector);
						
					}else{
						if(!_selectedFood.hasTaste()){
							_selectedFood.makeTasteGroup();
						}
						if(_selectedFood.getTasteGroup().addTaste(_tastes.get(position))){
							sendPickTasteBoradcast();
							selectChkBox.setChecked(true);
							((LinearLayout)arg0.findViewById(R.id.item_body)).setBackgroundResource(R.drawable.item_bg2);
							Toast.makeText(PickTasteActivity.this, "添加" + _tastes.get(position).getPreference(), Toast.LENGTH_SHORT).show();							
						}

					}
					_handler.sendEmptyMessage(0);
				}
			});

			return view;
		}
		
	}	
	
}
