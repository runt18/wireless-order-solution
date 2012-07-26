package com.wireless.ui;

import java.util.ArrayList;

import android.app.Activity;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Util;
import com.wireless.ui.view.ScrollLayout;
import com.wireless.ui.view.ScrollLayout.OnViewChangedListener;

public class PickTasteActivity extends Activity{
	
	private Handler _handler = new Handler(){
		@Override
		public void handleMessage(Message message){
			((TextView)findViewById(R.id.foodTasteTxtView)).setText(_selectedFood.toString());
		}
	};
	
	private final static String TAG_TASTE = "��ζ";
	private final static String TAG_STYLE = "����";
	private final static String TAG_SPEC = "���";
	private final static String TAG_PINZHU = "Ʒע";
	
	private OrderFood _selectedFood;
	//private TabHost _tabHost;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//get the food parcel from the intent
		FoodParcel foodParcel = getIntent().getParcelableExtra(FoodParcel.KEY_VALUE);
		_selectedFood = foodParcel;
		
		setContentView(R.layout.tastetable);
		
		TextView title = (TextView) findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("��ζ");
		//���ذ�ť
		TextView left = (TextView) findViewById(R.id.textView_left);
		left.setText("����");
		left.setVisibility(View.VISIBLE);
		
		ImageButton back = (ImageButton) findViewById(R.id.btn_left);
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();	
				finish();				
			}
		});
		
		final ScrollLayout tasteScrollLayout = (ScrollLayout)findViewById(R.id.tasteScrollLayout);
		//��ζView
		tasteScrollLayout.addView(setupTasteView());
		//����View
		tasteScrollLayout.addView(setupStyleView());
		//���View
		tasteScrollLayout.addView(setupSpecView());
		//Ʒע
		tasteScrollLayout.addView(setupPinZhuView());
		
		tasteScrollLayout.setOnViewChangedListener(new OnViewChangedListener() {			
			@Override
			public void onViewChanged(int curScreen, View parent, View curView) {
				String tag = curView.getTag().toString();
				((TextView)findViewById(R.id.toptitle)).setText(tag);
				
				((LinearLayout)findViewById(R.id.tasteLayout)).setBackgroundResource(R.drawable.tab_bg_unselected);
				((LinearLayout)findViewById(R.id.styleLayout)).setBackgroundResource(R.drawable.tab_bg_unselected);
				((LinearLayout)findViewById(R.id.specLayout)).setBackgroundResource(R.drawable.tab_bg_unselected);
				((LinearLayout)findViewById(R.id.pinzhuLayout)).setBackgroundResource(R.drawable.tab_bg_unselected);
				if(tag.equals(TAG_TASTE)){
					((LinearLayout)findViewById(R.id.tasteLayout)).setBackgroundResource(R.drawable.tab_bg_selected);
					
				}else if(tag.equals(TAG_STYLE)){
					((LinearLayout)findViewById(R.id.styleLayout)).setBackgroundResource(R.drawable.tab_bg_selected);
					
				}else if(tag.equals(TAG_SPEC)){
					((LinearLayout)findViewById(R.id.specLayout)).setBackgroundResource(R.drawable.tab_bg_selected);

				}else if(tag.equals(TAG_PINZHU)){
					((LinearLayout)findViewById(R.id.pinzhuLayout)).setBackgroundResource(R.drawable.tab_bg_selected);

				}
			}
		});
		
		//��ζButton
		((LinearLayout)findViewById(R.id.tasteLayout)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				tasteScrollLayout.setToScreen(0);
			}
		});
		
		//����Button
		((LinearLayout)findViewById(R.id.styleLayout)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				tasteScrollLayout.setToScreen(1);
			}
		});
		
		//���Button
		((LinearLayout)findViewById(R.id.specLayout)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				tasteScrollLayout.setToScreen(2);
			}
		});
		
		//ƷעButton
		((LinearLayout)findViewById(R.id.pinzhuLayout)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				tasteScrollLayout.setToScreen(3);
			}
		});
		tasteScrollLayout.setToScreen(0);
		
		_handler.sendEmptyMessage(0);	
		
	}

	
   //���ÿ�ζView
	public View setupTasteView(){
		View tasteView = LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.taste, null);
	    final ListView tasteLstView = (ListView)tasteView.findViewById(R.id.tasteLstView);
	   ((EditText)tasteView.findViewById(R.id.tastesearch)).setText("");
		tasteLstView.setAdapter(new TasteAdapter(WirelessOrder.foodMenu.tastes));	
		
		
		//������ʱ���������뷨
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
		 * �ڿ�ζѡ��ҳ���а���Ž��п�ζ��ɸѡ
		 */
		((EditText)tasteView.findViewById(R.id.tastesearch)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				ArrayList<Taste> tastes = new ArrayList<Taste>();
				if(s.toString().length() != 0){
				    for(int i = 0; i < WirelessOrder.foodMenu.tastes.length;i++){
				    	 if(WirelessOrder.foodMenu.tastes[i].preference.contains(s.toString().trim())){
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
		
		tasteView.setTag(TAG_TASTE);
		
		return tasteView;
		
	}
	
	
	//��������View
	public View setupStyleView(){
		View styleView = LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.style, null);
    	final ListView styleLstView = (ListView)styleView.findViewById(R.id.styleLstView);
    	((EditText)styleView.findViewById(R.id.stylesearch)).setText("");
		styleLstView.setAdapter(new TasteAdapter(WirelessOrder.foodMenu.styles));
		
	    
	    //������ʱ���������뷨
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
		 * ������ѡ��ҳ���а���Ž���������ɸѡ
		 */
		((EditText)styleView.findViewById(R.id.stylesearch)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				ArrayList<Taste> styles = new ArrayList<Taste>();
				if(s.toString().length() != 0){
					 for(int i = 0; i < WirelessOrder.foodMenu.styles.length;i++){
				    	 if(WirelessOrder.foodMenu.styles[i].preference.contains(s.toString().trim())){
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
		
		styleView.setTag(TAG_STYLE);
		
		return styleView;
	}
	
	//���ù��View
	public View setupSpecView(){
		
		View specView = LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.specs, null);
				
		final ListView specLstView = (ListView)specView.findViewById(R.id.specLstView);
		((EditText)specView.findViewById(R.id.specsearch)).setText("");
		specLstView.setAdapter(new TasteAdapter(WirelessOrder.foodMenu.specs));

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
		((EditText)specView.findViewById(R.id.specsearch)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				ArrayList<Taste> specs = new ArrayList<Taste>();
				if(s.toString().length() != 0){
					 for(int i = 0; i < WirelessOrder.foodMenu.specs.length;i++){
				    	 if(WirelessOrder.foodMenu.specs[i].preference.contains(s.toString().trim())){
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
		
		specView.setTag(TAG_SPEC);
		
		return specView;
	}
	
	//����ƷעView
	public View setupPinZhuView(){
		View pinZhuView = LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.pinzhu, null);
		
		final EditText pinZhuEdtTxt = ((EditText)pinZhuView.findViewById(R.id.pinZhuEdtTxt));
		final EditText priceEdtTxt = ((EditText)pinZhuView.findViewById(R.id.priceEdtTxt));
		pinZhuEdtTxt.requestFocus();
		
		if(_selectedFood.tmpTaste != null){
			pinZhuEdtTxt.setText(_selectedFood.tmpTaste.preference);
			priceEdtTxt.setText(_selectedFood.tmpTaste.getPrice().toString());
		}
		
		//Ʒע��EditText�Ĵ�����
		pinZhuEdtTxt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				String tmpTasteValue = s.toString().trim();
				if(tmpTasteValue.length() != 0){
					_selectedFood.tmpTaste = new Taste();
					_selectedFood.tmpTaste.aliasID = Util.genTempFoodID();
					_selectedFood.tmpTaste.preference = tmpTasteValue;
				}else{
					_selectedFood.tmpTaste = null;
				}
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
						}else{
							Float price = Float.valueOf(s.toString());
							if(price >= 0 && price < 9999){
								_selectedFood.tmpTaste.setPrice(price);
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
		
		pinZhuView.setTag(TAG_PINZHU);  
		
		return pinZhuView;

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
//	private View createTabIndicator(String text, int drawable) {
//		View view = LayoutInflater.from(_tabHost.getContext()).inflate(R.layout.tb_bg, null);
//		((TextView)view.findViewById(R.id.tabsText)).setText(text);
//		((ImageView) view.findViewById(R.id.icon)).setImageResource(drawable);
//		return view;
//	}
	
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
							Toast.makeText(PickTasteActivity.this, "���" + _tastes[position].preference, Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(PickTasteActivity.this, "���ֻ�����" + _selectedFood.tastes.length + "�ֿ�ζ", Toast.LENGTH_SHORT).show();
							selectChkBox.setChecked(false);
						}						
						
					}else{
						int pos = _selectedFood.removeTaste(_tastes[position]);
						if(pos >= 0){
							Toast.makeText(PickTasteActivity.this, "ɾ��" + _tastes[position].preference, Toast.LENGTH_SHORT).show();
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
							selectChkBox.setChecked(false);
							Toast.makeText(PickTasteActivity.this, "ɾ��" + _tastes[position].preference, Toast.LENGTH_SHORT).show();
						}
						
					}else{
						int pos = _selectedFood.addTaste(_tastes[position]);
						if(pos >= 0){
							selectChkBox.setChecked(true);
							Toast.makeText(PickTasteActivity.this, "���" + _tastes[position].preference, Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(PickTasteActivity.this, "���ֻ�����" + _selectedFood.tastes.length + "�ֿ�ζ", Toast.LENGTH_SHORT).show();
						}
					}
					_handler.sendEmptyMessage(0);
				}
			});

			return view;
		}
		
	}	
	
}
