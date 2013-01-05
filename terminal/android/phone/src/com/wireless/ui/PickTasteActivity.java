package com.wireless.ui;

import java.lang.ref.WeakReference;
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
	
	private Handler _handler; 
	
	private static class TasteHandler extends Handler{
		
		private WeakReference<PickTasteActivity> mActivity;
		
		TasteHandler(PickTasteActivity activity){
			mActivity = new WeakReference<PickTasteActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message message){
			final PickTasteActivity theActivity = mActivity.get();
			((TextView)theActivity.findViewById(R.id.foodTasteTxtView)).setText(theActivity.mSelectedFood.toString());
		}
	};
	
	public final static String TAG_POP_TASTE = "����";
	public final static String TAG_TASTE = "��ζ";
	public final static String TAG_STYLE = "����";
	public final static String TAG_SPEC = "���";
	public final static String TAG_PINZHU = "Ʒע";
	
	public final static String INIT_TAG = "initial_tag";
	public static final String PICK_ALL_ORDER_TASTE = "pickAllOrderTaste";
	private OrderFood mSelectedFood;
	//private TabHost _tabHost;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//get the food parcel from the intent
		FoodParcel foodParcel = getIntent().getParcelableExtra(FoodParcel.KEY_VALUE);
		mSelectedFood = foodParcel;
		if(!mSelectedFood.hasTaste()){
			mSelectedFood.makeTasteGroup();
		}
		boolean isAllOrderTaste = false;
		if(getIntent().getBooleanExtra(PICK_ALL_ORDER_TASTE, false)){
			isAllOrderTaste = true;
		}
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
		//����View
		tasteScrollLayout.addView(setupPopularView());
		//��ζView
		tasteScrollLayout.addView(setupTasteView());
		//���View
		tasteScrollLayout.addView(setupSpecView());
		//ƷעView
		if(!isAllOrderTaste)
			tasteScrollLayout.addView(setupPinZhuView());
		
		tasteScrollLayout.setOnViewChangedListener(new OnViewChangedListener() {			
			@Override
			public void onViewChanged(int curScreen, View parent, View curView) {
				String tag = curView.getTag().toString();
				((TextView)findViewById(R.id.toptitle)).setText(tag);
				
				((LinearLayout)findViewById(R.id.popTasteLayout)).setBackgroundResource(R.drawable.tab_bg_unselected);
				((LinearLayout)findViewById(R.id.tasteLayout)).setBackgroundResource(R.drawable.tab_bg_unselected);
				((LinearLayout)findViewById(R.id.styleLayout)).setBackgroundResource(R.drawable.tab_bg_unselected);
				((LinearLayout)findViewById(R.id.specLayout)).setBackgroundResource(R.drawable.tab_bg_unselected);
				((LinearLayout)findViewById(R.id.pinzhuLayout)).setBackgroundResource(R.drawable.tab_bg_unselected);
				
				
				if(tag.equals(TAG_POP_TASTE)){
					((LinearLayout)findViewById(R.id.popTasteLayout)).setBackgroundResource(R.drawable.tab_bg_selected);
					
				}else if(tag.equals(TAG_TASTE)){
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
		
		//����Button
		((LinearLayout)findViewById(R.id.popTasteLayout)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				tasteScrollLayout.setToScreen(0);
			}
		});
		
		//��ζButton
		((LinearLayout)findViewById(R.id.tasteLayout)).setOnClickListener(new View.OnClickListener() {			
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
		if(!isAllOrderTaste)
			((LinearLayout)findViewById(R.id.pinzhuLayout)).setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
					tasteScrollLayout.setToScreen(3);
				}
			});
		else {
			((LinearLayout)findViewById(R.id.pinzhuLayout)).setVisibility(View.GONE);
		}
			
		if(mSelectedFood.popTastes.length != 0){
			tasteScrollLayout.setToScreen(0);
		}else{
			tasteScrollLayout.setToScreen(1);			
		}
		
		_handler = new TasteHandler(this);
		_handler.sendEmptyMessage(0);	
		
		String initTag = getIntent().getStringExtra(INIT_TAG);
		//���ݴ������Ϣ�򿪲�ͬҳ��
		if(initTag != null){
			if(initTag.equals(TAG_TASTE)){
				tasteScrollLayout.post(new Runnable() {
					@Override
					public void run() {
						((LinearLayout)findViewById(R.id.tasteLayout)).performClick();
					}
				});
			} else if(initTag.equals(TAG_PINZHU)){
				tasteScrollLayout.post(new Runnable() {
					
					@Override
					public void run() {
						((LinearLayout)findViewById(R.id.pinzhuLayout)).performClick();
					}
				});
			}
		}
	}

	
	//���ó���View
	public View setupPopularView(){
		View popView = LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.popular, null);
    	final ListView popLstView = (ListView)popView.findViewById(R.id.popLstView);
    	((EditText)popView.findViewById(R.id.popSrchEdtTxt)).setText("");
		popLstView.setAdapter(new TasteAdapter(mSelectedFood.popTastes));
		
	    
	    //������ʱ���������뷨
	    popLstView.setOnScrollListener(new OnScrollListener() {
				
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText)findViewById(R.id.popSrchEdtTxt)).getWindowToken(), 0);
			}
				
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					
			}
		});
	    
	    /**
		 * �ڳ���ѡ��ҳ���а����ƽ���ɸѡ
		 */
		((EditText)popView.findViewById(R.id.popSrchEdtTxt)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				ArrayList<Taste> popTastes = new ArrayList<Taste>();
				if(s.toString().length() != 0){
					 for(int i = 0; i < mSelectedFood.popTastes.length; i++){
				    	 if(mSelectedFood.popTastes[i].getPreference().contains(s.toString().trim())){
				    		 popTastes.add(mSelectedFood.popTastes[i]);
				    	 }
				    }
					popLstView.setAdapter(new TasteAdapter(popTastes.toArray(new Taste[popTastes.size()])));
					
				}else{
					popLstView.setAdapter(new TasteAdapter(mSelectedFood.popTastes));
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		popView.setTag(TAG_POP_TASTE);
		
		return popView;
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
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				
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
				    	 if(WirelessOrder.foodMenu.tastes[i].getPreference().contains(s.toString().trim())){
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
				    	 if(WirelessOrder.foodMenu.styles[i].getPreference().contains(s.toString().trim())){
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
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				
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
				    	 if(WirelessOrder.foodMenu.specs[i].getPreference().contains(s.toString().trim())){
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
		
		if(mSelectedFood.hasTmpTaste()){
			pinZhuEdtTxt.setText(mSelectedFood.getTasteGroup().getTmpTastePref());
			priceEdtTxt.setText(mSelectedFood.getTasteGroup().getTmpTastePrice().toString());
		}
		
		//Ʒע��EditText�Ĵ�����
		pinZhuEdtTxt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				String tmpTasteValue = s.toString().trim();
				if(tmpTasteValue.length() != 0){
					Taste tmpTaste;
					if(!mSelectedFood.hasTmpTaste()){
						tmpTaste = new Taste();
					} else tmpTaste = mSelectedFood.getTasteGroup().getTmpTaste();
					
					tmpTaste.aliasID = Util.genTempFoodID();
					tmpTaste.setPreference(tmpTasteValue);
					mSelectedFood.getTasteGroup().setTmpTaste(tmpTaste);
				}else{
					mSelectedFood.getTasteGroup().setTmpTaste(null);
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
//				if(mSelectedFood.hasTmpTaste()){
//					Taste tmpTaste = mSelectedFood.getTasteGroup().getTmpTaste();
//					try{
//						if(s.length() == 0){
//							tmpTaste.setPrice(Float.valueOf(0));
//						}else{
//							Float price = Float.valueOf(s.toString());
//							if(price >= 0 && price < 9999){
//								tmpTaste.setPrice(price);
//							}else{
//								priceEdtTxt.setText(tmpTaste.getPrice() > 9999 ? "" : Util.float2String2(tmpTaste.getPrice()));
//								priceEdtTxt.setSelection(priceEdtTxt.getText().length());
//								Toast.makeText(PickTasteActivity.this, "��ʱ��ζ�ļ۸�Χ��0��9999", Toast.LENGTH_SHORT).show();
//							}
//						}
//					}catch(NumberFormatException e){
//						priceEdtTxt.setText(tmpTaste.getPrice() > 9999 ? "" : Util.float2String2(tmpTaste.getPrice()));
//						priceEdtTxt.setSelection(priceEdtTxt.getText().length());
//						Toast.makeText(PickTasteActivity.this, "��ʱ��ζ�ļ�Ǯ��ʽ����ȷ������������", Toast.LENGTH_SHORT).show();
//					}
//				}else{
//					Toast.makeText(PickTasteActivity.this, "����������ʱ��ζ", Toast.LENGTH_SHORT).show();
//				}
				
				if(!mSelectedFood.hasTmpTaste()){
					Taste tmpTaste = new Taste();
					tmpTaste.aliasID = Util.genTempFoodID();
					mSelectedFood.getTasteGroup().setTmpTaste(tmpTaste);
				}
				
				Taste tmpTaste = mSelectedFood.getTasteGroup().getTmpTaste();
				try{
					if(s.length() == 0){
						tmpTaste.setPrice(Float.valueOf(0));
					}else{
						Float price = Float.valueOf(s.toString());
						if(price >= 0 && price < 9999){
							tmpTaste.setPrice(price);
						}else{
							priceEdtTxt.setText(tmpTaste.getPrice() > 9999 ? "" : Util.float2String2(tmpTaste.getPrice()));
							priceEdtTxt.setSelection(priceEdtTxt.getText().length());
							Toast.makeText(PickTasteActivity.this, "��ʱ��ζ�ļ۸�Χ��0��9999", Toast.LENGTH_SHORT).show();
						}
					}
				}catch(NumberFormatException e){
					priceEdtTxt.setText(tmpTaste.getPrice() > 9999 ? "" : Util.float2String2(tmpTaste.getPrice()));
					priceEdtTxt.setSelection(priceEdtTxt.getText().length());
					Toast.makeText(PickTasteActivity.this, "��ʱ��ζ�ļ�Ǯ��ʽ����ȷ������������", Toast.LENGTH_SHORT).show();
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
		if(!mSelectedFood.hasTaste()){
			mSelectedFood.clearTasetGroup();
		}
		Intent intent = new Intent(); 
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(mSelectedFood));
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		super.onBackPressed();
	}
	
	private class TasteAdapter extends BaseAdapter{

		private Taste[] mTastes;
		
		TasteAdapter(Taste[] tastes){
			mTastes = tastes;
		}
		
		@Override
		public int getCount() {
			return mTastes.length;
		}

		@Override
		public Object getItem(int position) {
			return mTastes[position];
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
			((TextView)view.findViewById(R.id.foodname)).setText(mTastes[position].getPreference());
			//set number to taste
			((TextView)view.findViewById(R.id.nums)).setText(String.valueOf(mTastes[position].aliasID));
			//set the price to taste
			if(mTastes[position].calc == Taste.CALC_RATE){
				((TextView)view.findViewById(R.id.foodprice)).setText(Util.float2Int(mTastes[position].getRate()) + "%");
			}else{
				((TextView)view.findViewById(R.id.foodprice)).setText(Util.CURRENCY_SIGN + Util.float2String2(mTastes[position].getPrice()));
			}
			//set the status to whether the taste is selected
			final CheckBox selectChkBox = (CheckBox)view.findViewById(R.id.chioce);
			selectChkBox.setChecked(false);
			selectChkBox.requestFocus();
			if(mSelectedFood.hasNormalTaste()){
				for(Taste taste : mSelectedFood.getTasteGroup().getNormalTastes()){
					if(mTastes[position].aliasID == taste.aliasID){
						selectChkBox.setChecked(true);
						break;						
					}
				}
			}
			
			selectChkBox.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(selectChkBox.isChecked()){
						if(mSelectedFood.getTasteGroup().addTaste(mTastes[position])){
							Toast.makeText(PickTasteActivity.this, "���" + mTastes[position].getPreference(), Toast.LENGTH_SHORT).show();
						}
						
					}else{
						if(mSelectedFood.getTasteGroup().removeTaste(mTastes[position])){
							Toast.makeText(PickTasteActivity.this, "ɾ��" + mTastes[position].getPreference(), Toast.LENGTH_SHORT).show();
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
						if(mSelectedFood.getTasteGroup().removeTaste(mTastes[position])){
							selectChkBox.setChecked(false);
							Toast.makeText(PickTasteActivity.this, "ɾ��" + mTastes[position].getPreference(), Toast.LENGTH_SHORT).show();
						}else{
							selectChkBox.setChecked(false);							
						}
						
					}else{
						if(mSelectedFood.getTasteGroup().addTaste(mTastes[position])){
							selectChkBox.setChecked(true);
							Toast.makeText(PickTasteActivity.this, "���" + mTastes[position].getPreference(), Toast.LENGTH_SHORT).show();
						}
					}
					_handler.sendEmptyMessage(0);
				}
			});

			return view;
		}
		
	}	
	
}
