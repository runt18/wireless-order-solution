package com.wireless.fragment;

import com.wireless.parcel.ComboOrderFoodParcel;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.ComboOrderFood;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class PinzhuTasteFragment extends Fragment{
	
	public static interface OnTmpTastePickedListener{
		public void onTmpTastePicked(Taste tmpTaste);
	}
	
	private String mPinZhu;
	private float mPriceToPinZhu;
	
	private OnTmpTastePickedListener mTmpTastePickedListener;
	
	public static PinzhuTasteFragment newInstance(ComboOrderFood comboFood){
		PinzhuTasteFragment fgm = new PinzhuTasteFragment();
		Bundle bundles = new Bundle();
		bundles.putParcelable(ComboOrderFoodParcel.KEY_VALUE, new ComboOrderFoodParcel(comboFood));
		fgm.setArguments(bundles);
		return fgm;
	}
	
	public static PinzhuTasteFragment newInstance(OrderFood orderFood){
		PinzhuTasteFragment fgm = new PinzhuTasteFragment();
		Bundle bundles = new Bundle();
		bundles.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(orderFood));
		fgm.setArguments(bundles);
		return fgm;
	}
	
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the TastePickedListener so we can send events to the host
        	mTmpTastePickedListener = (OnTmpTastePickedListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString() + " must implement TmpTastePickedListener");
        }
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pick_taste_by_pinzhu_fgm, container, false);
		
		final OrderFoodParcel orderFroodParcel = getArguments().getParcelable(OrderFoodParcel.KEY_VALUE);
		final ComboOrderFoodParcel comboParcel = getArguments().getParcelable(ComboOrderFoodParcel.KEY_VALUE);
		
		final EditText pinZhuEdtTxt = ((EditText)view.findViewById(R.id.txtView_pinzhu_pickPinzhuFgm));
		final EditText priceEdtTxt = ((EditText)view.findViewById(R.id.edtTxt_price_pickPinzhuFgm));
		
		if(orderFroodParcel != null){
			OrderFood selectedFood = orderFroodParcel.asOrderFood();
			mPinZhu = selectedFood.getTasteGroup().getTmpTastePref();
			mPriceToPinZhu = selectedFood.getTasteGroup().getTmpTastePrice();
			
			if(selectedFood.hasTmpTaste()){
				pinZhuEdtTxt.setText(mPinZhu);
				priceEdtTxt.setText(NumericUtil.float2String2(mPriceToPinZhu));
			}
			
		}else if(comboParcel != null){
			ComboOrderFood comboFood = comboParcel.asComboOrderFood();
			mPinZhu = comboFood.getTasteGroup().getTmpTastePref();
			mPriceToPinZhu = comboFood.getTasteGroup().getTmpTastePrice();
			
			if(comboFood.hasTmpTaste()){
				pinZhuEdtTxt.setText(mPinZhu);
				priceEdtTxt.setText(NumericUtil.float2String2(mPriceToPinZhu));
			}
		}
		
        // Request pinzhu focus and show soft keyboard automatically and select all the content
		pinZhuEdtTxt.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            	if(hasFocus){
	            	pinZhuEdtTxt.post(new Runnable(){
	                    @Override
	                    public void run() {
							pinZhuEdtTxt.selectAll();
	                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	                        imm.showSoftInput(pinZhuEdtTxt, InputMethodManager.SHOW_IMPLICIT);
							pinZhuEdtTxt.setOnFocusChangeListener(null);
	                    }
            		});
            	}
            }
        });
		pinZhuEdtTxt.requestFocus();
		
        //点击品注EditText后全选内容并弹出软键盘
//		pinZhuEdtTxt.setOnTouchListener(new OnTouchListener(){
//			@Override
//			public boolean onTouch(final View v, MotionEvent event) {
//				v.postDelayed(new Runnable(){
//					@Override
//					public void run() {
//						pinZhuEdtTxt.selectAll();
//					}
//					
//				}, 100);
//				return false;
//			}
//        	
//        });
		
		//品注的EditText的处理函数
		pinZhuEdtTxt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				mPinZhu = s.toString().trim();
				
				final Taste tmpTaste;
   				if(mPinZhu.length() != 0 || mPriceToPinZhu != 0){
   					tmpTaste = Taste.newTmpTaste(mPinZhu, mPriceToPinZhu);
   				}else{
   					tmpTaste = null;
   				}
   				
				if(orderFroodParcel != null){
					orderFroodParcel.asOrderFood().setTmpTaste(tmpTaste);
				}else if(comboParcel != null){
					comboParcel.asComboOrderFood().setTmpTaste(tmpTaste);
				}
				if(mTmpTastePickedListener != null){
					mTmpTastePickedListener.onTmpTastePicked(tmpTaste);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
		});
		
		//品注的删除Button处理函数
		((ImageButton)view.findViewById(R.id.imgButton_deletePinZhu_pickPinzhuFgm)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				pinZhuEdtTxt.setText("");
			}
		});
		
        //点击价钱EditText后全选内容并弹出软键盘
//		priceEdtTxt.setOnTouchListener(new OnTouchListener(){
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				v.postDelayed(new Runnable(){
//					@Override
//					public void run() {
//						priceEdtTxt.selectAll();
//					}
//					
//				}, 100);
//				return false;
//			}
//        	
//        });
		
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
					
					final Taste tmpTaste;
	   				if(mPinZhu.length() != 0 || mPriceToPinZhu != 0){
	   					tmpTaste = Taste.newTmpTaste(mPinZhu, mPriceToPinZhu);
	   				}else{
	   					tmpTaste = null;
	   				}
	   				
					if(orderFroodParcel != null){
						orderFroodParcel.asOrderFood().setTmpTaste(tmpTaste);
					}else if(comboParcel != null){
						comboParcel.asComboOrderFood().setTmpTaste(tmpTaste);
					}
					if(mTmpTastePickedListener != null){
						mTmpTastePickedListener.onTmpTastePicked(tmpTaste);
					}
					
				}catch(NumberFormatException e){
					Toast.makeText(getActivity(), "临时口味的价钱格式不正确，请重新输入", Toast.LENGTH_SHORT).show();
				}
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
		});
		
		//品注价钱的删除Button处理函数
		((ImageButton)view.findViewById(R.id.imgButton_deletePrice_pickPinzhuFgm)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				priceEdtTxt.setText("");
			}
		});
		
		return view;
	}
}
