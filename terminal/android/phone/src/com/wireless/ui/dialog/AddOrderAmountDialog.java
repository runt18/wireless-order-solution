package com.wireless.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.R;

public class AddOrderAmountDialog extends DialogFragment {
	
	public static interface OnAmountAddedListener{
		public void onAmountAdded(OrderFood food);
	}
	
	public final static String TAG = "AddOrderAmountDialog";
	
	private OnAmountAddedListener mOnAmountAddedListener;
	
	private OrderFood mSelectedFood;

	private final static String PARENT_FGM_ID_KEY = "ParentFgmIdKey";
	
	public static AddOrderAmountDialog newInstance(OrderFood selectedFood, int parentFgmId){
		AddOrderAmountDialog fgm = new AddOrderAmountDialog();
		Bundle bundle = new Bundle();
		bundle.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(selectedFood));
		bundle.putInt(PARENT_FGM_ID_KEY, parentFgmId);
		fgm.setArguments(bundle);
		return fgm;
	}
	
	public AddOrderAmountDialog() {
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OrderFoodParcel ofParcel = getArguments().getParcelable(OrderFoodParcel.KEY_VALUE);
		mSelectedFood = ofParcel.asOrderFood();
		
        // Instantiate the OnAmountChangedListener so we can send events to the host
        mOnAmountAddedListener = (OnAmountAddedListener)getFragmentManager().findFragmentById(getArguments().getInt(PARENT_FGM_ID_KEY));
	}		        
	
	@Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		getDialog().setTitle("请输入" + mSelectedFood.getName() + "的数量");
		
		View view = inflater.inflate(R.layout.add_order_amount_dialog, container);
		
		//设置默认为此菜品的点菜数量
		final EditText amountEditTxt = (EditText)view.findViewById(R.id.editText_add_amountDialog);
		amountEditTxt.setText(NumericUtil.float2String2(mSelectedFood.getCount()));
		
		//数量加
		view.findViewById(R.id.button_add_amountDialog_plus).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					float curNum = Float.parseFloat(amountEditTxt.getText().toString());
					curNum++;
					amountEditTxt.setText(NumericUtil.float2String2(curNum));
					amountEditTxt.selectAll();
				}catch(NumberFormatException e){
					
				}
			}
		});
		
		//数量减
		view.findViewById(R.id.button_add_amountDialog_minus).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try{
					float curNum = Float.parseFloat(amountEditTxt.getText().toString());
					curNum--;
					if(curNum >= 1.0f && curNum >= mSelectedFood.getCount()){
						amountEditTxt.setText(NumericUtil.float2String2(curNum));
						amountEditTxt.selectAll();
					}else{
						Toast.makeText(getActivity(), "对不起, 设置\"" + mSelectedFood.toString() + "\"" + "的数量不能少于已点数量", Toast.LENGTH_LONG).show();
					}
				}catch(NumberFormatException e){
					 
				}				
			}
		}); 
		
		//"确定"Button
		Button okBtn = (Button)view.findViewById(R.id.button_add_amountDialog_left);
		okBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					float amount = Float.parseFloat(amountEditTxt.getText().toString());
					float delta = amount - mSelectedFood.getCount();
					if(delta < 0){
						Toast.makeText(getActivity(), "对不起, 设置\"" + mSelectedFood.toString() + "\"" + "的数量不能少于已点数量", Toast.LENGTH_LONG).show();
					}else{
						mSelectedFood.addCount(delta);
						if(mOnAmountAddedListener != null){
							mOnAmountAddedListener.onAmountAdded(mSelectedFood);
						}
						Toast.makeText(getActivity(), "设置\"" + mSelectedFood.toString() + "\"" + "数量为" + amount + "份", Toast.LENGTH_LONG).show();
						dismiss();
					}
				}catch(NumberFormatException e){
					Toast.makeText(getActivity(), "您输入的数量格式不正确，请重新输入", Toast.LENGTH_SHORT).show();
				} 		
			}
		});
		
		//"取消"Button
		Button cancelBtn = (Button)view.findViewById(R.id.button_add_amountDialog_right);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
        // Request focus and show soft keyboard automatically
		amountEditTxt.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            	amountEditTxt.post(new Runnable() {
                    @Override
                    public void run() {
                    	amountEditTxt.selectAll();
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(amountEditTxt, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
            }
        });
		amountEditTxt.requestFocus();
		
		return view;
	}
	
}
