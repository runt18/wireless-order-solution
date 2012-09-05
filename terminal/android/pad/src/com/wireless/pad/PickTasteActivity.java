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
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Util;

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
		
		FoodParcel foodParcel = getIntent().getParcelableExtra(FoodParcel.KEY_VALUE);
		_selectedFood = foodParcel;
		// FIXME 
		if(_selectedFood.tmpTaste != null && _selectedFood.tmpTaste.aliasID == Integer.MIN_VALUE){
			_selectedFood.tmpTaste = null;
		}
		
		_tabHost = getTabHost();
		
		//����Tab
		TabSpec spec = _tabHost.newTabSpec(TAG_POP)
					   .setIndicator(createTabIndicator("����", R.drawable.kitchen_selector))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.pop_taste, null);
						   }								   
					   });
		_tabHost.addTab(spec);
		
		//��ζTab
		spec = _tabHost.newTabSpec(TAG_TASTE)
					   .setIndicator(createTabIndicator("��ζ", R.drawable.number_selector))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.taste, null);
						   }								   
					   });
		_tabHost.addTab(spec);
		
		
		
		//����Tab
//		spec = _tabHost.newTabSpec(TAG_STYLE)
//					   .setIndicator(createTabIndicator("����", R.drawable.kitchen_selector))
//					   .setContent(new TabHost.TabContentFactory(){
//						   @Override
//						   public View createTabContent(String arg0) {
//							   return LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.style, null);
//						   }								   
//					   });
//		_tabHost.addTab(spec);
		
		
		
		//���Tab
		spec = _tabHost.newTabSpec(TAG_SPEC)
					   .setIndicator(createTabIndicator("���",  R.drawable.pinyin_selector))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.specs, null);
						   }								   
					   });
		_tabHost.addTab(spec);
		
		

		//ƷעTab
		spec = _tabHost.newTabSpec(TAG_PINZHU)
					   .setIndicator(createTabIndicator("Ʒע",R.drawable.occasional_selector))
					   .setContent(new TabHost.TabContentFactory(){
						   @Override
						   public View createTabContent(String arg0) {
							   return LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.pinzhu, null);
						   }								   
					   });
		_tabHost.addTab(spec);
		
		/**
		 * Tab�л�ʱ������Ӧ��Adapter����ʾ��ͬ����Ŀ�ζ
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
		
		if(_selectedFood.popTastes.length != 0){
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
	 * ѡ���ζ�󣬽�OrderFood����Ϣ�ŵ�Parcel���������㲥Intent֪ͨOrderActivity����ChgOrderActivity
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
	 * ɾ����ѡ��Ʒ�����п�ζ
	 */
	private void removeAllTaste(){
		if(_selectedFood.tastes.length > 0){
			for(Taste taste : _selectedFood.tastes.clone()){
				_selectedFood.removeTaste(taste);
			}
			_selectedFood.tmpTaste = null;
			
			//refresh the taste getPreference()
			_handler.sendEmptyMessage(0);
			//refresh the taste list view
			_tasteAdapter.notifyDataSetChanged();
			//send the broadcast event to notify OrderActivity or ChgOrderActivity
			sendPickTasteBoradcast();
		}
	}
	
   //���ÿ�ζView
	private void setupPopTasteView(){
		
		//ɾ�����п�ζButton
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
	   
	   _tasteAdapter = new TasteAdapter(_selectedFood.popTastes);
	   popLstView.setAdapter(_tasteAdapter);
		
		
		//������ʱ���������뷨
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
		 * �ڿ�ζѡ��ҳ���а���Ž��п�ζ��ɸѡ
		 */
		((EditText)findViewById(R.id.popSrchEdtTxt)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				ArrayList<Taste> popTastes = new ArrayList<Taste>();
				if(s.toString().length() != 0){
					 for(int i = 0; i < _selectedFood.popTastes.length; i++){
				    	 if(_selectedFood.popTastes[i].getPreference().contains(s.toString().trim())){
				    		 popTastes.add(_selectedFood.popTastes[i]);
				    	 }
				    }
					 
					_tasteAdapter = new TasteAdapter(popTastes.toArray(new Taste[popTastes.size()]));
					popLstView.setAdapter(_tasteAdapter);
					
				}else{
					_tasteAdapter = new TasteAdapter(_selectedFood.popTastes);
					popLstView.setAdapter(_tasteAdapter);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});			
	}
	    //���ÿ�ζView
	private void setupTasteView(){
		
		//ɾ�����п�ζButton
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
		
		
		//������ʱ���������뷨
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
		 * �ڿ�ζѡ��ҳ���а���Ž��п�ζ��ɸѡ
		 */
		((EditText)findViewById(R.id.tastesearch)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				ArrayList<Taste> tastes = new ArrayList<Taste>();
				if(s.toString().length() != 0){
				    for(int i = 0; i < WirelessOrder.foodMenu.tastes.length;i++){
				    	 if(WirelessOrder.foodMenu.tastes[i].getPreference().contains(s.toString().trim())){
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
	
	
	//��������View
	private void setupStyleView(){
		_tasteTxtView = (TextView)findViewById(R.id.foodStyleTxtView);
    	final GridView styleLstView = (GridView)findViewById(R.id.styleLstView);
    	styleLstView.setNumColumns(4);
    	((EditText)findViewById(R.id.stylesearch)).setText("");
    	
    	_tasteAdapter = new TasteAdapter(WirelessOrder.foodMenu.styles);
		styleLstView.setAdapter(_tasteAdapter);
		
		//ɾ�����п�ζButton
		((Button)findViewById(R.id.cancelStyleBtn)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				removeAllTaste();
			}
		});
	    
	    //������ʱ���������뷨
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
		 * ������ѡ��ҳ���а���Ž���������ɸѡ
		 */
		((EditText)findViewById(R.id.stylesearch)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				ArrayList<Taste> styles = new ArrayList<Taste>();
				if(s.toString().length() != 0){
					 for(int i = 0; i < WirelessOrder.foodMenu.styles.length;i++){
				    	 if(WirelessOrder.foodMenu.styles[i].getPreference().contains(s.toString().trim())){
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
	
	//���ù��View
	private void setupSpecView(){
		_tasteTxtView = (TextView)findViewById(R.id.foodSpecTxtView);
		final GridView specLstView = (GridView)findViewById(R.id.specLstView);
		specLstView.setNumColumns(4);
		((EditText)findViewById(R.id.specsearch)).setText("");
		
		_tasteAdapter = new TasteAdapter(WirelessOrder.foodMenu.specs);
		specLstView.setAdapter(_tasteAdapter);
		
		//ɾ�����п�ζButton
		((Button)findViewById(R.id.cancelSpecBtn)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				removeAllTaste();
			}
		});
	    
	    //������ʱ���������뷨
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
		 * �ڹ��ѡ��ҳ���а���Ž��й���ɸѡ
		 */
		((EditText)findViewById(R.id.specsearch)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				ArrayList<Taste> specs = new ArrayList<Taste>();
				if(s.toString().length() != 0){
					 for(int i = 0; i < WirelessOrder.foodMenu.specs.length;i++){
				    	 if(WirelessOrder.foodMenu.specs[i].getPreference().contains(s.toString().trim())){
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
	
	//����ƷעView
	private void setupPinZhuView(){
	
		final EditText pinZhuEdtTxt = ((EditText)findViewById(R.id.pinZhuEdtTxt));
		final EditText priceEdtTxt = ((EditText)findViewById(R.id.priceEdtTxt));
		pinZhuEdtTxt.requestFocus();
		
		if(_selectedFood.tmpTaste != null){
			pinZhuEdtTxt.setText(_selectedFood.tmpTaste.getPreference());
			priceEdtTxt.setText(_selectedFood.tmpTaste.getPrice().toString());
		}
		
		//ɾ�����п�ζButton
		((Button)findViewById(R.id.cancelPinZhuBtn)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pinZhuEdtTxt.setText("");
				priceEdtTxt.setText("");
				removeAllTaste();
			}
		});
		
		//Ʒע��EditText�Ĵ�����
		pinZhuEdtTxt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				String tmpTasteValue = s.toString().trim();
				if(tmpTasteValue.length() != 0){
					_selectedFood.tmpTaste = new Taste();
					_selectedFood.tmpTaste.aliasID = Util.genTempFoodID();
					_selectedFood.tmpTaste.setPreference(tmpTasteValue);
				}else{
					_selectedFood.tmpTaste = null;
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
		
		//�۸�EditText�Ĵ�����
		priceEdtTxt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				if(_selectedFood.tmpTaste != null){
					try{
						if(s.length() == 0){
							_selectedFood.tmpTaste.setPrice(new Float(0));
							sendPickTasteBoradcast();
						}else{
							Float price = Float.valueOf(s.toString());
							if(price >= 0 && price < 9999){
								_selectedFood.tmpTaste.setPrice(price);
								sendPickTasteBoradcast();
							}else{
								priceEdtTxt.setText(_selectedFood.tmpTaste.getPrice() > 9999 ? "" : Util.float2String2(_selectedFood.tmpTaste.getPrice()));
								priceEdtTxt.setSelection(priceEdtTxt.getText().length());
								Toast.makeText(PickTasteActivity.this, "��ʱ��ζ�ļ۸�Χ��0��9999", Toast.LENGTH_SHORT).show();
							}
						}
					}catch(NumberFormatException e){
						priceEdtTxt.setText(_selectedFood.tmpTaste.getPrice() > 9999 ? "" : Util.float2String2(_selectedFood.tmpTaste.getPrice()));
						priceEdtTxt.setSelection(priceEdtTxt.getText().length());
						Toast.makeText(PickTasteActivity.this, "��ʱ��ζ�ļ�Ǯ��ʽ����ȷ������������", Toast.LENGTH_SHORT).show();
					}
				}else{
					Toast.makeText(PickTasteActivity.this, "����������ʱ��ζ", Toast.LENGTH_SHORT).show();
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
		if(text.equals("��ζ")){
			lp.setMargins(15, 10, 0, 10);
			((Button)view.findViewById(R.id.tabicon)).setLayoutParams(lp);
		}
		if(text.equals("Ʒע")){
			lp.setMargins(0, 10, 15, 10);
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
			((TextView)view.findViewById(R.id.foodname)).setText(_tastes[position].getPreference());
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
					((LinearLayout)view.findViewById(R.id.item_body)).setBackgroundResource(R.drawable.item_bg2);
					break;
				}else{
					selectChkBox.setChecked(false);
					((LinearLayout)view.findViewById(R.id.item_body)).setBackgroundResource(R.drawable.item_bg_selector);
				}
			}
			
			selectChkBox.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(selectChkBox.isChecked()){
						int pos = _selectedFood.addTaste(_tastes[position]);
						if(pos >= 0){
							sendPickTasteBoradcast();
							Toast.makeText(PickTasteActivity.this, "���" + _tastes[position].getPreference(), Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(PickTasteActivity.this, "���ֻ�����" + _selectedFood.tastes.length + "�ֿ�ζ", Toast.LENGTH_SHORT).show();
							selectChkBox.setChecked(false);
						}						
						
					}else{
						int pos = _selectedFood.removeTaste(_tastes[position]);
						if(pos >= 0){
							sendPickTasteBoradcast();
							Toast.makeText(PickTasteActivity.this, "ɾ��" + _tastes[position].getPreference(), Toast.LENGTH_SHORT).show();
						}
					}
					_handler.sendEmptyMessage(0);
				}
			});
			
			/**
			 * ��ζ��View����
			 */			
			view.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
			        
					if(selectChkBox.isChecked()){
						int pos = _selectedFood.removeTaste(_tastes[position]);
						if(pos >= 0){
							sendPickTasteBoradcast();
							selectChkBox.setChecked(false);
							Toast.makeText(PickTasteActivity.this, "ɾ��" + _tastes[position].getPreference(), Toast.LENGTH_SHORT).show();
						}
						((LinearLayout)arg0.findViewById(R.id.item_body)).setBackgroundResource(R.drawable.item_bg_selector);
					}else{
						int pos = _selectedFood.addTaste(_tastes[position]);
						if(pos >= 0){
							sendPickTasteBoradcast();
							selectChkBox.setChecked(true);
							((LinearLayout)arg0.findViewById(R.id.item_body)).setBackgroundResource(R.drawable.item_bg2);
							Toast.makeText(PickTasteActivity.this, "���" + _tastes[position].getPreference(), Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(PickTasteActivity.this, "���ֻ�����" + _selectedFood.tastes.length + "�ֿ�ζ", Toast.LENGTH_SHORT).show();
							((LinearLayout)arg0.findViewById(R.id.item_body)).setBackgroundResource(R.drawable.item_bg_selector);
						}

					}
					_handler.sendEmptyMessage(0);
				}
			});

			return view;
		}
		
	}	
	
}
