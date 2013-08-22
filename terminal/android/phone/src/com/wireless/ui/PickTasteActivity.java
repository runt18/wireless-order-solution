package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.NumericUtil;
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
	
	public final static String TAG_POP_TASTE = "常用";
	public final static String TAG_TASTE = "口味";
	public final static String TAG_STYLE = "做法";
	public final static String TAG_SPEC = "规格";
	public final static String TAG_PINZHU = "品注";
	
	public final static String INIT_TAG = "initial_tag";
	public static final String PICK_ALL_ORDER_TASTE = "pickAllOrderTaste";
	private OrderFood mSelectedFood;
	
	private float mPriceToPinZhu;
	private String mPinZhu;	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//get the food parcel from the intent
		OrderFoodParcel foodParcel = getIntent().getParcelableExtra(OrderFoodParcel.KEY_VALUE);
		mSelectedFood = foodParcel.asOrderFood();
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
		title.setText("口味");
		//返回按钮
		TextView left = (TextView) findViewById(R.id.textView_left);
		left.setText("返回");
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
		//常用View
		tasteScrollLayout.addView(setupPopularView());
		//口味View
		tasteScrollLayout.addView(setupTasteView());
		//规格View
		tasteScrollLayout.addView(setupSpecView());
		//品注View
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
		
		//常用Button
		((LinearLayout)findViewById(R.id.popTasteLayout)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				tasteScrollLayout.setToScreen(0);
			}
		});
		
		//口味Button
		((LinearLayout)findViewById(R.id.tasteLayout)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				tasteScrollLayout.setToScreen(1);
			}
		});
		
		//规格Button
		((LinearLayout)findViewById(R.id.specLayout)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				tasteScrollLayout.setToScreen(2);
			}
		});
		
		//品注Button
		if(!isAllOrderTaste){
			((LinearLayout)findViewById(R.id.pinzhuLayout)).setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
					tasteScrollLayout.setToScreen(3);
				}
			});
		}else {
			((LinearLayout)findViewById(R.id.pinzhuLayout)).setVisibility(View.GONE);
		}
			
		if(mSelectedFood.asFood().hasPopTastes()){
			tasteScrollLayout.setToScreen(0);
		}else{
			tasteScrollLayout.setToScreen(1);			
		}
		
		_handler = new TasteHandler(this);
		_handler.sendEmptyMessage(0);	
		
		String initTag = getIntent().getStringExtra(INIT_TAG);
		//根据传入的信息打开不同页面
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

	
	//设置常用View
	public View setupPopularView(){
		View popView = LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.popular, null);
    	final ListView popLstView = (ListView)popView.findViewById(R.id.popLstView);
    	((EditText)popView.findViewById(R.id.popSrchEdtTxt)).setText("");
		popLstView.setAdapter(new TasteAdapter(mSelectedFood.asFood().getPopTastes()));
		
	    
	    //滚动的时候隐藏输入法
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
		 * 在常用选择页面中按名称进行筛选
		 */
		((EditText)popView.findViewById(R.id.popSrchEdtTxt)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				List<Taste> popTastes = new ArrayList<Taste>();
				if(s.toString().length() != 0){
					 for(Taste popTaste : mSelectedFood.asFood().getPopTastes()){
				    	 if(popTaste.getPreference().contains(s.toString().trim())){
				    		 popTastes.add(popTaste);
				    	 }
				    }
					popLstView.setAdapter(new TasteAdapter(popTastes));
					
				}else{
					popLstView.setAdapter(new TasteAdapter(mSelectedFood.asFood().getPopTastes()));
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
	
   //设置口味View
	public View setupTasteView(){
		View tasteView = LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.taste, null);
	    final ListView tasteLstView = (ListView)tasteView.findViewById(R.id.tasteLstView);
	   ((EditText)tasteView.findViewById(R.id.tastesearch)).setText("");
		tasteLstView.setAdapter(new TasteAdapter(WirelessOrder.foodMenu.tastes));	
		
		
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
		((EditText)tasteView.findViewById(R.id.tastesearch)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				ArrayList<Taste> tastes = new ArrayList<Taste>();
				if(s.toString().length() != 0){
					for(Taste t : WirelessOrder.foodMenu.tastes){
				    	 if(t.getPreference().contains(s.toString().trim())){
				    		 tastes.add(t);
				    	 }
					}
				    tasteLstView.setAdapter(new TasteAdapter(tastes));
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
	
	
	//设置做法View
	public View setupStyleView(){
		View styleView = LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.style, null);
    	final ListView styleLstView = (ListView)styleView.findViewById(R.id.styleLstView);
    	((EditText)styleView.findViewById(R.id.stylesearch)).setText("");
		styleLstView.setAdapter(new TasteAdapter(WirelessOrder.foodMenu.styles));
		
	    
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
		((EditText)styleView.findViewById(R.id.stylesearch)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				ArrayList<Taste> styles = new ArrayList<Taste>();
				if(s.toString().length() != 0){
					for(Taste style : WirelessOrder.foodMenu.styles){
				    	 if(style.getPreference().contains(s.toString().trim())){
				    		 styles.add(style);
				    	 }
					}
					styleLstView.setAdapter(new TasteAdapter(styles));
					
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
	
	//设置规格View
	public View setupSpecView(){
		
		View specView = LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.specs, null);
				
		final ListView specLstView = (ListView)specView.findViewById(R.id.specLstView);
		((EditText)specView.findViewById(R.id.specsearch)).setText("");
		specLstView.setAdapter(new TasteAdapter(WirelessOrder.foodMenu.specs));

	    //滚动的时候隐藏输入法
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
		 * 在规格选择页面中按编号进行规格的筛选
		 */
		((EditText)specView.findViewById(R.id.specsearch)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				ArrayList<Taste> specs = new ArrayList<Taste>();
				if(s.toString().length() != 0){
					for(Taste spec : WirelessOrder.foodMenu.specs){
				    	 if(spec.getPreference().contains(s.toString().trim())){
				    		 specs.add(spec);
				    	 }
					}
					specLstView.setAdapter(new TasteAdapter(specs));
					
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
	
	//设置品注View
	public View setupPinZhuView(){
		View pinZhuView = LayoutInflater.from(PickTasteActivity.this).inflate(R.layout.pinzhu, null);
		
		final EditText pinZhuEdtTxt = ((EditText)pinZhuView.findViewById(R.id.pinZhuEdtTxt));
		final EditText priceEdtTxt = ((EditText)pinZhuView.findViewById(R.id.priceEdtTxt));
		pinZhuEdtTxt.requestFocus();
		
		mPinZhu = mSelectedFood.getTasteGroup().getTmpTastePref();
		mPriceToPinZhu = mSelectedFood.getTasteGroup().getTmpTastePrice();
		
		if(mSelectedFood.hasTmpTaste()){
			pinZhuEdtTxt.setText(mPinZhu);
			priceEdtTxt.setText(NumericUtil.float2String2(mPriceToPinZhu));
		}
		
		//品注的EditText的处理函数
		pinZhuEdtTxt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				mPinZhu = s.toString().trim();
   				if(mPinZhu != null || mPriceToPinZhu != 0){
   					mSelectedFood.getTasteGroup().setTmpTaste(Taste.newTmpTaste(mPinZhu, mPriceToPinZhu));
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
		
		//价格EditText的处理函数
		priceEdtTxt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				
				try{
					if(s.toString().trim().length() == 0){
						mPriceToPinZhu = 0;
						
					}else if(s.toString().trim().length() > 0){
						mPriceToPinZhu = Float.valueOf(s.toString());
					}
					
	   				if(mPinZhu != null || mPriceToPinZhu != 0){
	   					mSelectedFood.getTasteGroup().setTmpTaste(Taste.newTmpTaste(mPinZhu, mPriceToPinZhu));
	   				}
				}catch(NumberFormatException e){
					Toast.makeText(PickTasteActivity.this, "临时口味的价钱格式不正确，请重新输入", Toast.LENGTH_SHORT).show();
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
		bundle.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(mSelectedFood));
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		super.onBackPressed();
	}
	
	private class TasteAdapter extends BaseAdapter{

		private List<Taste> mTastes;
		
		TasteAdapter(List<Taste> tastes){
			mTastes = tastes;
		}
		
		@Override
		public int getCount() {
			return mTastes.size();
		}

		@Override
		public Object getItem(int position) {
			return mTastes.get(position);
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
			((TextView)view.findViewById(R.id.foodname)).setText(mTastes.get(position).getPreference());
			//set the price to taste
			if(mTastes.get(position).isCalcByRate()){
				((TextView)view.findViewById(R.id.txtView_1stKey_foodItem)).setText("比例：");
				((TextView)view.findViewById(R.id.txtView_1stValue_foodItem)).setText(NumericUtil.float2Int(mTastes.get(position).getRate()) + "%");
			}else{
				((TextView)view.findViewById(R.id.txtView_1stKey_foodItem)).setText("价格：");
				((TextView)view.findViewById(R.id.txtView_1stValue_foodItem)).setText(NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(mTastes.get(position).getPrice()));
			}
			//set the status to whether the taste is selected
			final CheckBox selectChkBox = (CheckBox)view.findViewById(R.id.chioce);
			selectChkBox.setChecked(false);
			selectChkBox.requestFocus();
			if(mSelectedFood.hasNormalTaste()){
				for(Taste taste : mSelectedFood.getTasteGroup().getNormalTastes()){
					if(mTastes.get(position).equals(taste)){
						selectChkBox.setChecked(true);
						break;						
					}
				}
			}
			
			selectChkBox.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(selectChkBox.isChecked()){
						if(mSelectedFood.getTasteGroup().addTaste(mTastes.get(position))){
							Toast.makeText(PickTasteActivity.this, "添加" + mTastes.get(position).getPreference(), Toast.LENGTH_SHORT).show();
						}
						
					}else{
						if(mSelectedFood.getTasteGroup().removeTaste(mTastes.get(position))){
							Toast.makeText(PickTasteActivity.this, "删除" + mTastes.get(position).getPreference(), Toast.LENGTH_SHORT).show();
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
						if(mSelectedFood.getTasteGroup().removeTaste(mTastes.get(position))){
							selectChkBox.setChecked(false);
							Toast.makeText(PickTasteActivity.this, "删除" + mTastes.get(position).getPreference(), Toast.LENGTH_SHORT).show();
						}else{
							selectChkBox.setChecked(false);							
						}
						
					}else{
						if(mSelectedFood.getTasteGroup().addTaste(mTastes.get(position))){
							selectChkBox.setChecked(true);
							Toast.makeText(PickTasteActivity.this, "添加" + mTastes.get(position).getPreference(), Toast.LENGTH_SHORT).show();
						}
					}
					_handler.sendEmptyMessage(0);
				}
			});

			return view;
		}
		
	}	
	
}
