package com.wireless.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.PickTasteActivity;
import com.wireless.ui.R;
import com.wireless.ui.view.ScrollLayout;
import com.wireless.ui.view.ScrollLayout.OnViewChangedListener;

public class AskOrderAmountDialog extends DialogFragment{

	private static final int PICK_WITH_TASTE = 1;
	
	public final static String TAG = "AskOrderAmountDialog";
	
	private OrderFood mSelectedFood;
	
	private OnFoodPickedListener mFoodPickedListener;
	
	private static final String PARENT_ID_KEY = "ParentIdKey";
	private int mParentId;
	
	private float mPriceToPinZhu;
	private String mPinZhu;
	
	public static AskOrderAmountDialog newInstance(Food food, int parentId){
		AskOrderAmountDialog fgm = new AskOrderAmountDialog();
		Bundle bundles = new Bundle();
		bundles.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(new OrderFood(food)));
		bundles.putInt(PARENT_ID_KEY, parentId);
		fgm.setArguments(bundles);
		return fgm;
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
        	mFoodPickedListener = (OnFoodPickedListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement FoodPickedListener");
        }
    }
	
	@Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		mParentId = getArguments().getInt(PARENT_ID_KEY);
		OrderFoodParcel orderFoodParcel = getArguments().getParcelable(OrderFoodParcel.KEY_VALUE);
		mSelectedFood = orderFoodParcel.asOrderFood();
		
        // Set title for this dialog
        getDialog().setTitle(mSelectedFood.getName());
		
        // Inflate the layout to use as dialog or embedded fragment
        final View view = inflater.inflate(R.layout.ask_order_amount_dialog, container, false);
        
        final EditText countEditText = (EditText)view.findViewById(R.id.editText_askOrderAmount_amount);
        
        //点击数量EditText后全选内容并弹出软键盘
        countEditText.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				countEditText.selectAll();
				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			    imm.showSoftInput(v, 0);
				return true;
			}
        	
        });
        
		//数量加按钮
		((ImageButton)view.findViewById(R.id.button_askOrderAmount_plus)).setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				try{
					float curNum = Float.parseFloat(countEditText.getText().toString());
					if(++curNum <= 255){
						countEditText.setText(NumericUtil.float2String2(curNum));
					}else{
						Toast.makeText(getActivity(), "点菜数量不能超过255", Toast.LENGTH_SHORT).show();
					}
				}catch(NumberFormatException e){
					
				}
				if(!countEditText.getText().toString().equals("")){
					float curNum = Float.parseFloat(countEditText.getText().toString());
					countEditText.setText(NumericUtil.float2String2(curNum));
				}
			}
		});
		
		//数量减按钮
		((ImageButton) view.findViewById(R.id.button_askOrderAmount_minus)).setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				try{
					float curNum = Float.parseFloat(countEditText.getText().toString());
					if(--curNum >= 1.0f){
						countEditText.setText(NumericUtil.float2String2(curNum));
					}
				}catch(NumberFormatException e){
					 
				}
			}
		});
        
		//"确定"Button
		Button okBtn = (Button)view.findViewById(R.id.button_askOrderAmount_confirm);
		okBtn.setText("确定");
		okBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {			
				onPick();
			}
		});
		
		//"口味"Button
		Button tasteBtn = (Button)view.findViewById(R.id.button_askOrderAmount_taste);
		tasteBtn.setOnClickListener(new View.OnClickListener() {				
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getActivity(), PickTasteActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(mSelectedFood));
				bundle.putString(PickTasteActivity.INIT_TAG, PickTasteActivity.TAG_TASTE);
				intent.putExtras(bundle);
				AskOrderAmountDialog.this.startActivityForResult(intent, PICK_WITH_TASTE);
			}
		});
		
		//"取消"Button
		Button cancelBtn = (Button)view.findViewById(R.id.button_askOrderAmount_cancel);
		cancelBtn.setText("取消");
		cancelBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		//"叫起"CheckBox
		CheckBox hurriedChkBox = (CheckBox)view.findViewById(R.id.checkBox_askOrderAmount_hurry);
		hurriedChkBox.setText("叫起");
		hurriedChkBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					mSelectedFood.setHangup(true);
					Toast.makeText(getActivity(), "叫起\"" + mSelectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
				}else{
					mSelectedFood.setHangup(false);
					Toast.makeText(getActivity(), "取消叫起\"" + mSelectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		final ScrollLayout scrollLayout = (ScrollLayout)view.findViewById(R.id.scrollLayout_askOrderAmount_dialog);
		scrollLayout.setOnViewChangedListener(new OnViewChangedListener(){
			@Override
			public void onViewChanged(int curScreen, View parent, View curView){
				((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(scrollLayout.getWindowToken(), 0);
			}
		});
		
		//设置常用口味GridView
		GridView tasteGridView = (GridView)view.findViewById(R.id.gridView_askOrderAmount_dialog);
    	
		if(mSelectedFood.asFood().hasPopTastes()){
			
			tasteGridView.setVisibility(View.VISIBLE);
			
			final List<Taste> popTastes = new ArrayList<Taste>(mSelectedFood.asFood().getPopTastes());
			//只显示前8个常用口味
			while(popTastes.size() > 8){
				popTastes.remove(popTastes.size() - 1);
			}
			
			tasteGridView.setAdapter(new BaseAdapter() {
				
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					View view = inflater.inflate(R.layout.ask_order_amount_dialog_item, null);
					CheckBox checkBox = (CheckBox) view;
					Taste thisTaste = popTastes.get(position);
					checkBox.setTag(thisTaste);
					checkBox.setText(thisTaste.getPreference());
					
					checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							Taste thisTaste = (Taste) buttonView.getTag();
							if(isChecked){
								buttonView.setBackgroundColor(buttonView.getResources().getColor(R.color.orange));
								
								if(!mSelectedFood.hasTaste()){
									mSelectedFood.makeTasteGroup();
								} 
								mSelectedFood.getTasteGroup().addTaste(thisTaste);
							} else {
								buttonView.setBackgroundColor(buttonView.getResources().getColor(R.color.green));
								mSelectedFood.getTasteGroup().removeTaste(thisTaste);
							}
							getDialog().setTitle(mSelectedFood.toString());
						}
					});
					return view;
				}
				
				@Override
				public long getItemId(int position) {
					return position;
				}
				
				@Override
				public Object getItem(int position) {
					return popTastes.get(position);
				}
				
				@Override
				public int getCount() {
					return popTastes.size();
				}
			});
		}else{
			tasteGridView.setVisibility(View.GONE);
		}
		
		//设置品注编辑
		final EditText pinZhuEdtTxt = ((EditText)view.findViewById(R.id.edtTxt_pinzhu_askOrderAmount_dialog));
		final EditText priceEdtTxt = ((EditText)view.findViewById(R.id.edtTxt_pinzhuPrice_askOrderAmount_dialog));
		
		//品注的EditText的处理函数
		pinZhuEdtTxt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				mPinZhu = s.toString().trim();
   				if(mPinZhu != null || mPriceToPinZhu != 0){
   					mSelectedFood.getTasteGroup().setTmpTaste(Taste.newTmpTaste(mPinZhu, mPriceToPinZhu));
   				}
				getDialog().setTitle(mSelectedFood.toString());
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
					if(s.length() > 0){
						mPriceToPinZhu = Float.valueOf(s.toString());
					}
					
	   				if(mPinZhu != null || mPriceToPinZhu != 0){
	   					mSelectedFood.getTasteGroup().setTmpTaste(Taste.newTmpTaste(mPinZhu, mPriceToPinZhu));
	   				}
	   				
					getDialog().setTitle(mSelectedFood.toString());

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
		
        return view;
    }
	
	public AskOrderAmountDialog() {

	}
	
	/**
	 * 
	 * @param selectedFood
	 * @param pickTaste
	 */
	private void onPick(){
		try{
			float orderAmount = Float.parseFloat(((EditText)getView().findViewById(R.id.editText_askOrderAmount_amount)).getText().toString());
			
   			if(orderAmount > 255){
   				Toast.makeText(getActivity(), "对不起，\"" + mSelectedFood.toString() + "\"最多只能点255份", Toast.LENGTH_SHORT).show();
   			}else{
   				mSelectedFood.setCount(orderAmount);
   				if(mFoodPickedListener != null){	
   					mFoodPickedListener.onFoodPicked(mSelectedFood);
   				}
   				
   				//Clear up the text to search box
				View srchEditText = getFragmentManager().findFragmentById(mParentId).getView().findViewById(R.id.editText_pickFoodFragment);
				if(srchEditText != null){
					((EditText)srchEditText).setText("");
				}

				dismiss();
   			}
			
		}catch(NumberFormatException e){
			Toast.makeText(getActivity(), "您输入的数量格式不正确，请重新输入", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == PICK_WITH_TASTE) {
				//口味修改后模拟"确定"Button的添加菜品行为
				OrderFoodParcel foodParcel = data.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
				mSelectedFood = foodParcel.asOrderFood();
				((Button)getView().findViewById(R.id.button_askOrderAmount_confirm)).performClick();
			}
		}
	}
	
	public static interface OnFoodPickedListener{
		/**
		 * 当选中菜品后，回调此函数通知选中的Food信息
		 * @param food 选中Food的信息
		 */
		public void onFoodPicked(OrderFood food);
		
	}
	
}

