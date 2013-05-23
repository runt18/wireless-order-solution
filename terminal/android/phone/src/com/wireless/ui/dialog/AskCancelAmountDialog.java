package com.wireless.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
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
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.R;

public class AskCancelAmountDialog extends Dialog {
	private OrderFood mTheFood;
	private boolean isOriFood;
	private EditText mAmountEditText;
	private OnAmountChangeListener mOnAmountChangeListener;
	private CancelReason mOriReason;
	private CompoundButton mReasonBtn;

	public AskCancelAmountDialog(Activity context, OrderFood oriFood, boolean isOrigineFood){
		super(context);
		
		try{
			mOnAmountChangeListener = (OnAmountChangeListener)context;
		} catch(ClassCastException e){
			
		}
		this.mTheFood = oriFood;
		this.isOriFood = isOrigineFood;
		if(mTheFood.hasCancelReason())
			mOriReason = mTheFood.getCancelReason();
		else mOriReason = null;
		
		setContentView(R.layout.ask_cancel_amount_dialog);
		setTitle("请输入退菜的数量：");
		
		mAmountEditText = (EditText) findViewById(R.id.editText_ask_cancel_amountDialog);
		mAmountEditText.setText(NumericUtil.float2String2(oriFood.getCount()));
		
		//数量加
		findViewById(R.id.button_ask_cancel_amountDialog_plus).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					float curNum = Float.parseFloat(mAmountEditText.getText().toString());
					if(++ curNum > mTheFood.getCount()){
						Toast.makeText(getContext(), "退菜数量不能超过已点数量", Toast.LENGTH_SHORT).show();
					} else if(curNum <= 255){
						mAmountEditText.setText(NumericUtil.float2String2(curNum));
					}else{
						Toast.makeText(getContext(), "点菜数量不能超过255", Toast.LENGTH_SHORT).show();
					}
				}catch(NumberFormatException e){
					
				}
				if(!mAmountEditText.getText().toString().equals(""))
				{
					float curNum = Float.parseFloat(mAmountEditText.getText().toString());
					mAmountEditText.setText(NumericUtil.float2String2(curNum));
				}				
			}
		});
		//数量减
		findViewById(R.id.button_ask_cancel_amountDialog_minus).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try{
					float curNum = Float.parseFloat(mAmountEditText.getText().toString());
					if(--curNum >= 1.0f){
						mAmountEditText.setText(NumericUtil.float2String2(curNum));
					}
				}catch(NumberFormatException e){
					 
				}				
			}
		});
		
		final GridView gridView = (GridView) findViewById(R.id.gridView_ask_cancel_amountDialog);
		
		if(WirelessOrder.foodMenu.reasons == null || WirelessOrder.foodMenu.reasons.size() == 0 || !isOriFood){
			findViewById(R.id.textView_ask_cancel_amountDialog_hint).setVisibility(View.INVISIBLE);
			gridView.setVisibility(View.GONE); 
		} else {

			gridView.setAdapter(new BaseAdapter() {
				
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					final View layout = getLayoutInflater().inflate(R.layout.ask_order_amount_dialog_item, null);
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
			if(WirelessOrder.foodMenu.reasons != null && WirelessOrder.foodMenu.reasons.size() > 8){
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
		}
		//确定按钮
		findViewById(R.id.button_ask_cancel_amountDialog_left).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					float cancelAmount = Float.parseFloat(mAmountEditText.getText().toString());

					mTheFood.removeCount(cancelAmount);							
						
					if(mOnAmountChangeListener != null){
						mOnAmountChangeListener.onAmountChange(mTheFood, isOriFood);
					}
					
					dismiss();
					
				}catch(BusinessException e){
					Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
					
				}catch(NumberFormatException e){
					Toast.makeText(getContext(), "你输入删菜数量不正确", Toast.LENGTH_LONG).show();
				}
				
			}
		});
		//取消按钮
		findViewById(R.id.button_ask_cancel_amountDialog_right).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mOriReason != null)
				{
					mTheFood.setCancelReason(mOriReason);
				} else {
					mTheFood.setCancelReason(null);
				}
				dismiss();
			}
		});
		
//		mAmountEditText.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				mAmountEditText.selectAll();
//			}
//		});
//		//弹出软键盘
//      getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); 
//      InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//      imm.showSoftInput(mAmountEditText, 0); //显示软键盘
//      imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	@Override
	public void show() {
		super.show();
		 ((EditText) findViewById(R.id.editText_ask_cancel_amountDialog)).selectAll();
	}

	public interface OnAmountChangeListener{
		void onAmountChange(OrderFood food, boolean isOriFood);
	}
	
	public void setOnAmountChangeListener(OnAmountChangeListener l){
		mOnAmountChangeListener = l;
	}
	
}
