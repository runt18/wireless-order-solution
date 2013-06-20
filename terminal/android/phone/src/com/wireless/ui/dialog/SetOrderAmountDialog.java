package com.wireless.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.R;

/**
 * 提示输入数量的Dialog
 */
public class SetOrderAmountDialog extends DialogFragment{

	public static interface OnAmountChangedListener{
		public void onAmountChanged(OrderFood food);
	}
	
	public final static String TAG = "AskOrderAmountDialog";
	
	private OnAmountChangedListener mOnAmountChangedListener;
	
	private OrderFood mSelectedFood;

	private final static String PARENT_FGM_ID_KEY = "ParentFgmIdKey";
	
	public static SetOrderAmountDialog newInstance(OrderFood selectedFood, int parentFgmId){
		SetOrderAmountDialog fgm = new SetOrderAmountDialog();
		Bundle bundle = new Bundle();
		bundle.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(selectedFood));
		bundle.putInt(PARENT_FGM_ID_KEY, parentFgmId);
		fgm.setArguments(bundle);
		return fgm;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OrderFoodParcel ofParcel = getArguments().getParcelable(OrderFoodParcel.KEY_VALUE);
		mSelectedFood = ofParcel.asOrderFood();
		
        // Verify that the parent fragment implements the callback interface
        try {
            // Instantiate the OnAmountChangedListener so we can send events to the host
        	mOnAmountChangedListener = (OnAmountChangedListener)getFragmentManager().findFragmentById(getArguments().getInt(PARENT_FGM_ID_KEY));
        } catch (ClassCastException e) {
            // The parent fragment doesn't implement the interface, throw exception
            throw new ClassCastException("the parent fragment must implement OnAmountChangedListener");
        }
	}		        
	
	@Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		getDialog().setTitle("请输入" + mSelectedFood.getName() + "的数量");
		
		View view = inflater.inflate(R.layout.set_order_amount_dialog, null);
		
		//设置默认为此菜品的点菜数量
		final EditText amountEditTxt = (EditText)view.findViewById(R.id.editText_set_amountDialog);
		amountEditTxt.setText(NumericUtil.float2String2(mSelectedFood.getCount()));
		
		//数量加
		view.findViewById(R.id.button_set_amountDialog_plus).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					float curNum = Float.parseFloat(amountEditTxt.getText().toString());
					curNum++;
					if(curNum > 255){
						Toast.makeText(getActivity(), "点菜数量不能超过255", Toast.LENGTH_SHORT).show();
					}else{
						amountEditTxt.setText(NumericUtil.float2String2(curNum));
					}
				}catch(NumberFormatException e){
					
				}
			}
		});
		
		//数量减
		view.findViewById(R.id.button_set_amountDialog_minus).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try{
					float curNum = Float.parseFloat(amountEditTxt.getText().toString());
					if(--curNum >= 1.0f){
						amountEditTxt.setText(NumericUtil.float2String2(curNum));
					}
				}catch(NumberFormatException e){
					 
				}				
			}
		});
		
		//"确定"Button
		Button okBtn = (Button)view.findViewById(R.id.button_set_amountDialog_left);
		okBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					float amount = Float.parseFloat(amountEditTxt.getText().toString());
					mSelectedFood.setCount(amount);
					if(mOnAmountChangedListener != null){
						mOnAmountChangedListener.onAmountChanged(mSelectedFood);
					}
					Toast.makeText(getActivity(), "设置\"" + mSelectedFood.toString() + "\"" + "数量为" + amount + "份", Toast.LENGTH_LONG).show();
					dismiss();
				}catch(NumberFormatException e){
					Toast.makeText(getActivity(), "您输入的数量格式不正确，请重新输入", Toast.LENGTH_SHORT).show();
				}					
			}
		});
		
		//"删除"Button
		Button removeBtn = (Button)view.findViewById(R.id.button_set_amountDialog_mid);
		removeBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mSelectedFood.setCount(0);
				if(mOnAmountChangedListener != null){
					mOnAmountChangedListener.onAmountChanged(mSelectedFood);
				}
				dismiss();
			}
			
		});
		
		//"取消"Button
		Button cancelBtn = (Button)view.findViewById(R.id.button_set_amountDialog_right);
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
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(amountEditTxt, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
            }
        });
		amountEditTxt.requestFocus();
		
		return view;
	}
	
	public SetOrderAmountDialog() {
		
	}		
}