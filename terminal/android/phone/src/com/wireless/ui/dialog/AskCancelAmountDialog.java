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
import com.wireless.excep.BusinessException;
import com.wireless.protocol.CancelReason;
import com.wireless.protocol.OrderFood;
import com.wireless.ui.R;

public class AskCancelAmountDialog extends Dialog {
	private OrderFood mTheFood;
	private boolean isOriFood;
	private EditText mAmountEditText;
	private OnAmountChangeListener mOnAmountChangeListener;

	public AskCancelAmountDialog(Activity context, OrderFood oriFood, boolean isOrigineFood){
		super(context);
		
		try{
			mOnAmountChangeListener = (OnAmountChangeListener)context;
		} catch(ClassCastException e){
			
		}
		this.mTheFood = oriFood;
		this.isOriFood = isOrigineFood;
		
		setContentView(R.layout.ask_cancel_amount_dialog);
		setTitle("请输入要删除的数量：");
		
		mAmountEditText = (EditText) findViewById(R.id.editText_ask_cancel_amountDialog);
		//点击时全选
		mAmountEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mAmountEditText.post(new Runnable(){
					@Override
					public void run() {
						mAmountEditText.selectAll();
					}
				});
			}
		});
		if(WirelessOrder.foodMenu.reasons == null || WirelessOrder.foodMenu.reasons.length == 0){
			findViewById(R.id.textView_ask_cancel_amountDialog_hint).setVisibility(View.INVISIBLE);
		}
		final GridView gridView = (GridView) findViewById(R.id.gridView_ask_cancel_amountDialog);

		gridView.setAdapter(new BaseAdapter() {
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View layout = getLayoutInflater().inflate(R.layout.ask_order_amount_dialog_item, null);
				CheckBox checkBox = (CheckBox) layout;
				
				CancelReason reason = WirelessOrder.foodMenu.reasons[position];
				checkBox.setTag(reason);
				checkBox.setText(reason.getReason());
				
				checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						CancelReason reason = (CancelReason)buttonView.getTag();
						if(mTheFood.hasCancelReason() && !reason.equals(mTheFood.getCancelReason())){
							Toast.makeText(getContext(), "只能选择一个原因", Toast.LENGTH_SHORT).show();
						} 
						else {
							if(isChecked){
								buttonView.setBackgroundColor(buttonView.getResources().getColor(R.color.orange));
								mTheFood.setCancelReason(reason);
							} else {
								buttonView.setBackgroundColor(buttonView.getResources().getColor(R.color.green));
								mTheFood.setCancelReason(null);
							}
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
				return WirelessOrder.foodMenu.reasons[position];
			}
			
			@Override
			public int getCount() {
				return WirelessOrder.foodMenu.reasons.length;
			}
		});
		//超过8个时限定高度
		if(WirelessOrder.foodMenu.reasons != null && WirelessOrder.foodMenu.reasons.length > 8){
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
		
		findViewById(R.id.button_ask_cancel_amountDialog_right).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
	
	public interface OnAmountChangeListener{
		void onAmountChange(OrderFood food, boolean isOriFood);
	}
	
	public void setOnAmountChangeListener(OnAmountChangeListener l){
		mOnAmountChangeListener = l;
	}
	
}
