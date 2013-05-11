package com.wireless.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.ui.R;

public class AskOrderAmountDialog extends Dialog{

	private OrderFood mSelectedFood;
	private OnFoodPickedListener mFoodPickedListener;
	
	public AskOrderAmountDialog(Context context , Food food, OnFoodPickedListener listener, final EditText searchEditText) {
		super(context);
		
		mSelectedFood = new OrderFood(food);
		mFoodPickedListener = listener;
		
		setContentView(R.layout.ask_order_amount_dialog);
		
		setTitle(mSelectedFood.getName());
		final EditText countEditText = (EditText)findViewById(R.id.editText_askOrderAmount_amount);
		//���ʱȫѡ FIXME ��ͬ���ӱ��ֲ�ͬ���еĿ���ȫѡ���е�������
//		countEditText.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				((EditText)findViewById(R.id.editText_askOrderAmount_amount)).selectAll();
//			}
//		});
		
		//�����Ӱ�ť
		((ImageButton) findViewById(R.id.button_askOrderAmount_plus)).setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				try{
					float curNum = Float.parseFloat(countEditText.getText().toString());
					if(++curNum <= 255){
						countEditText.setText(NumericUtil.float2String2(curNum));
					}else{
						Toast.makeText(getContext(), "����������ܳ���255", Toast.LENGTH_SHORT).show();
					}
				}catch(NumberFormatException e){
					
				}
				if(!countEditText.getText().toString().equals(""))
				{
					float curNum = Float.parseFloat(countEditText.getText().toString());
					countEditText.setText(NumericUtil.float2String2(curNum));
				}
			}
		});
		//��������ť
		((ImageButton) findViewById(R.id.button_askOrderAmount_minus)).setOnClickListener(new View.OnClickListener(){

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
		
		//"ȷ��"Button
		Button okBtn = (Button)findViewById(R.id.button_askOrderAmount_confirm);
		okBtn.setText("ȷ��");
		okBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {			
				onPick(false, false, searchEditText);
			}
		});
		
		//"��ζ"Button
		Button tasteBtn = (Button)findViewById(R.id.button_askOrderAmount_taste);
		tasteBtn.setOnClickListener(new View.OnClickListener() {				
			@Override
			public void onClick(View arg0) {
				onPick(true, false, searchEditText);
			}
		});
		//Ʒע
		((Button) findViewById(R.id.button_askOrderAmount_tempTaste)).setOnClickListener(new View.OnClickListener() {				
			@Override
			public void onClick(View arg0) {
				onPick(true, true, searchEditText);
			}
		});
		
		
		//"ȡ��"Button
		Button cancelBtn = (Button)findViewById(R.id.button_askOrderAmount_cancel);
		cancelBtn.setText("ȡ��");
		cancelBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		//"����"CheckBox
		CheckBox hurriedChkBox = (CheckBox)findViewById(R.id.checkBox_askOrderAmount_hurry);
		hurriedChkBox.setText("����");
		hurriedChkBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					mSelectedFood.setHangup(true);
					Toast.makeText(getContext(), "����\"" + mSelectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
				}else{
					mSelectedFood.setHangup(false);
					Toast.makeText(getContext(), "ȡ������\"" + mSelectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		//���ÿ�ζ�б�
		GridView tasteGridView = (GridView) findViewById(R.id.gridView_askOrderAmount_dialog);
		//��ʾ������
		if(food.hasPopTastes()){
			
			final List<Taste> popTastes = new ArrayList<Taste>(food.getPopTastes());
			//ֻ��ʾǰ8�����ÿ�ζ
			while(popTastes.size() > 8){
				popTastes.remove(popTastes.size() - 1);
			}
			
			tasteGridView.setAdapter(new BaseAdapter() {
				
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					View view = getLayoutInflater().inflate(R.layout.ask_order_amount_dialog_item, null);
					CheckBox checkBox = (CheckBox) view;
					Taste thisTaste = popTastes.get(position);
					checkBox.setTag(thisTaste);
					//���ÿ�ζ��
					checkBox.setText(thisTaste.getPreference());
					
					checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							Taste thisTaste = (Taste) buttonView.getTag();
							//�жϰ�ť״̬������ζ��ӽ���Ʒ���Ƴ�
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
		}
	}
	/**
	 * 
	 * @param selectedFood
	 * @param pickTaste
	 */
	private void onPick(boolean pickTaste, boolean isTempTaste, EditText searchEditText){
		try{
			float orderAmount = Float.parseFloat(((EditText)findViewById(R.id.editText_askOrderAmount_amount)).getText().toString());
			
   			if(orderAmount > 255){
   				Toast.makeText(getContext(), "�Բ���\"" + mSelectedFood.toString() + "\"���ֻ�ܵ�255��", Toast.LENGTH_SHORT).show();
   			}else{
   				mSelectedFood.setCount(orderAmount);
   				if(mFoodPickedListener != null){	
   					if(pickTaste){
   						mFoodPickedListener.onPickedWithTaste(mSelectedFood, isTempTaste);
   					}else{
   						mFoodPickedListener.onPicked(mSelectedFood);
   					}
   				}
				dismiss();
//				//������������
				if(searchEditText != null){
					searchEditText.setText("");
				}
   			}
			
		}catch(NumberFormatException e){
			Toast.makeText(getContext(), "�������������ʽ����ȷ������������", Toast.LENGTH_SHORT).show();
		}
	}
	
	public static interface OnFoodPickedListener{
		/**
		 * ��PickFoodListViewѡ�в�Ʒ�󣬻ص��˺���֪ͨActivityѡ�е�Food��Ϣ
		 * @param food ѡ��Food����Ϣ
		 */
		public void onPicked(OrderFood food);
		
		/**
		 * ��PickFoodListViewѡ�в�Ʒ�󣬻ص��˺���֪ͨActivityѡ�е�Food��Ϣ������ת����ζActivity
		 * @param food
		 * 			ѡ��Food����Ϣ
		 */
		public void onPickedWithTaste(OrderFood food, boolean isTempTaste);
	}
}
