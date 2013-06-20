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
 * ��ʾ����������Dialog
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
		
		getDialog().setTitle("������" + mSelectedFood.getName() + "������");
		
		View view = inflater.inflate(R.layout.set_order_amount_dialog, null);
		
		//����Ĭ��Ϊ�˲�Ʒ�ĵ������
		final EditText amountEditTxt = (EditText)view.findViewById(R.id.editText_set_amountDialog);
		amountEditTxt.setText(NumericUtil.float2String2(mSelectedFood.getCount()));
		
		//������
		view.findViewById(R.id.button_set_amountDialog_plus).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					float curNum = Float.parseFloat(amountEditTxt.getText().toString());
					curNum++;
					if(curNum > 255){
						Toast.makeText(getActivity(), "����������ܳ���255", Toast.LENGTH_SHORT).show();
					}else{
						amountEditTxt.setText(NumericUtil.float2String2(curNum));
					}
				}catch(NumberFormatException e){
					
				}
			}
		});
		
		//������
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
		
		//"ȷ��"Button
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
					Toast.makeText(getActivity(), "����\"" + mSelectedFood.toString() + "\"" + "����Ϊ" + amount + "��", Toast.LENGTH_LONG).show();
					dismiss();
				}catch(NumberFormatException e){
					Toast.makeText(getActivity(), "�������������ʽ����ȷ������������", Toast.LENGTH_SHORT).show();
				}					
			}
		});
		
		//"ɾ��"Button
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
		
		//"ȡ��"Button
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