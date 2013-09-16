package com.wireless.ui.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.R;

public class AskCancelAmountDialog extends DialogFragment {
	
	public static interface OnCancelAmountChangedListener{
		/**
		 * Called when cancel amount is changed.
		 * @param food
		 */
		public void onCancelAmountChanged(OrderFood food);
	}
	
	public final static String TAG = "AskCancelAmountDialog";
	
	private final static String PARENT_FGM_ID_PARAM_KEY = "parent_fgm_id_param_key";
	
	private OrderFood mTheFood;
	private OnCancelAmountChangedListener mOnAmountChangeListener;
	private CancelReason mOriReason;
	private CompoundButton mReasonBtn;

	public static AskCancelAmountDialog newInstance(OrderFood of, int parentFgmId){
		AskCancelAmountDialog fgm = new AskCancelAmountDialog();
		Bundle bundle = new Bundle();
		bundle.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(of));
		bundle.putInt(PARENT_FGM_ID_PARAM_KEY, parentFgmId);
		fgm.setArguments(bundle);
		return fgm;
	}
	
	public AskCancelAmountDialog(){
		
	}
	
	@Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		getDialog().setTitle("请输入退菜数量和原因");
		
		try{
			mOnAmountChangeListener = (OnCancelAmountChangedListener)getFragmentManager().findFragmentById(getArguments().getInt(PARENT_FGM_ID_PARAM_KEY));
		}catch(ClassCastException e){
			throw new ClassCastException("the parent fragment must implement OnAmountChangeListener ");
		}
		
		final View view = inflater.inflate(R.layout.ask_cancel_amount_dialog, null);
		
		OrderFoodParcel ofParcel = getArguments().getParcelable(OrderFoodParcel.KEY_VALUE);
		this.mTheFood = ofParcel.asOrderFood();
		
		if(mTheFood.hasCancelReason()){
			mOriReason = mTheFood.getCancelReason();
		}else{
			mOriReason = null;
		}

		final EditText amountEditTxt = (EditText)view.findViewById(R.id.editText_ask_cancel_amountDialog);
		amountEditTxt.setText(NumericUtil.float2String2(mTheFood.getCount()));
		
		//数量加
		view.findViewById(R.id.button_ask_cancel_amountDialog_plus).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					float curNum = Float.parseFloat(amountEditTxt.getText().toString());
					if(++curNum > mTheFood.getCount()){
						Toast.makeText(getActivity(), "退菜数量不能超过已点数量", Toast.LENGTH_SHORT).show();
					} else if(curNum <= 255){
						amountEditTxt.setText(NumericUtil.float2String2(curNum));
					}else{
						Toast.makeText(getActivity(), "点菜数量不能超过255", Toast.LENGTH_SHORT).show();
					}
				}catch(NumberFormatException e){
					
				}
				if(!amountEditTxt.getText().toString().equals("")){
					float curNum = Float.parseFloat(amountEditTxt.getText().toString());
					amountEditTxt.setText(NumericUtil.float2String2(curNum));
				}				
			}
		});
		
		//数量减
		view.findViewById(R.id.button_ask_cancel_amountDialog_minus).setOnClickListener(new View.OnClickListener() {
			
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
		
		//退菜原因GridView
		final GridView gridView = (GridView)view.findViewById(R.id.gridView_ask_cancel_amountDialog);
		
		gridView.setAdapter(new BaseAdapter() {
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				final View layout = inflater.inflate(R.layout.ask_order_amount_dialog_item, null);
				CheckBox checkBox = (CheckBox) layout;
				
				CancelReason reason = WirelessOrder.foodMenu.reasons.get(position);
				checkBox.setTag(reason);
				checkBox.setText(reason.getReason());
				
				checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						CancelReason reason = (CancelReason)buttonView.getTag();
						//清除之前的状态
						if(mReasonBtn != null){
							mReasonBtn.setBackgroundColor(buttonView.getResources().getColor(R.color.green));
							mTheFood.setCancelReason(null);
							mReasonBtn.setChecked(false);
						}
						if(isChecked){
							buttonView.setBackgroundColor(buttonView.getResources().getColor(R.color.orange));
							mTheFood.setCancelReason(reason);
							mReasonBtn = buttonView;
						} else {
							buttonView.setBackgroundColor(buttonView.getResources().getColor(R.color.green));
							mTheFood.setCancelReason(null);
							mReasonBtn = null;
						}
					}
				});
				return layout;
			}
			
			@Override
			public long getItemId(int position) {
				return position;
			}
			
			@Override
			public Object getItem(int position) {
				return WirelessOrder.foodMenu.reasons.get(position);
			}
			
			@Override
			public int getCount() {
				return WirelessOrder.foodMenu.reasons.size();
			}
		});
		
		//超过8个时限定高度
		if(WirelessOrder.foodMenu.reasons.size() > 8){
			gridView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					if(gridView.getHeight() > 0){
						LayoutParams params = gridView.getLayoutParams();
						params.height = 210;
						gridView.setLayoutParams(params);
						gridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
				}
			});
		}
		
		//确定按钮
		view.findViewById(R.id.button_ask_cancel_amountDialog_left).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					float cancelAmount = Float.parseFloat(amountEditTxt.getText().toString());

					//FIXME
//					if(mTheFood.getDelta() > 0){
//						mTheFood.addCount(mTheFood.getDelta());
//						cancelAmount += mTheFood.getDelta();
//					}
					mTheFood.removeCount(cancelAmount, WirelessOrder.loginStaff);							
						
					if(mOnAmountChangeListener != null){
						mOnAmountChangeListener.onCancelAmountChanged(mTheFood);
					}
					
					dismiss();
					
				}catch(BusinessException e){
					Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
					
				}catch(NumberFormatException e){
					Toast.makeText(getActivity(), "你输入删菜数量不正确", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		//取消按钮
		view.findViewById(R.id.button_ask_cancel_amountDialog_right).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mOriReason != null){
					mTheFood.setCancelReason(mOriReason);
				} else {
					mTheFood.setCancelReason(null);
				}
				dismiss();
			}
		});
		
		amountEditTxt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				amountEditTxt.selectAll();
			}
		});
		
		return view;
	}
	
	@Override 
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		((EditText)getView().findViewById(R.id.editText_ask_cancel_amountDialog)).selectAll();
	}
	

	
}
