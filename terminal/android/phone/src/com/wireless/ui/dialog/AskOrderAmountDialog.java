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
		//点击时全选 FIXME 不同机子表现不同，有的可以全选，有的有问题
//		countEditText.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				((EditText)findViewById(R.id.editText_askOrderAmount_amount)).selectAll();
//			}
//		});
		
		//数量加按钮
		((ImageButton) findViewById(R.id.button_askOrderAmount_plus)).setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				try{
					float curNum = Float.parseFloat(countEditText.getText().toString());
					if(++curNum <= 255){
						countEditText.setText(NumericUtil.float2String2(curNum));
					}else{
						Toast.makeText(getContext(), "点菜数量不能超过255", Toast.LENGTH_SHORT).show();
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
		//数量减按钮
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
		
		//"确定"Button
		Button okBtn = (Button)findViewById(R.id.button_askOrderAmount_confirm);
		okBtn.setText("确定");
		okBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {			
				onPick(false, false, searchEditText);
			}
		});
		
		//"口味"Button
		Button tasteBtn = (Button)findViewById(R.id.button_askOrderAmount_taste);
		tasteBtn.setOnClickListener(new View.OnClickListener() {				
			@Override
			public void onClick(View arg0) {
				onPick(true, false, searchEditText);
			}
		});
		//品注
		((Button) findViewById(R.id.button_askOrderAmount_tempTaste)).setOnClickListener(new View.OnClickListener() {				
			@Override
			public void onClick(View arg0) {
				onPick(true, true, searchEditText);
			}
		});
		
		
		//"取消"Button
		Button cancelBtn = (Button)findViewById(R.id.button_askOrderAmount_cancel);
		cancelBtn.setText("取消");
		cancelBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		//"叫起"CheckBox
		CheckBox hurriedChkBox = (CheckBox)findViewById(R.id.checkBox_askOrderAmount_hurry);
		hurriedChkBox.setText("叫起");
		hurriedChkBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					mSelectedFood.setHangup(true);
					Toast.makeText(getContext(), "叫起\"" + mSelectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
				}else{
					mSelectedFood.setHangup(false);
					Toast.makeText(getContext(), "取消叫起\"" + mSelectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		//常用口味列表
		GridView tasteGridView = (GridView) findViewById(R.id.gridView_askOrderAmount_dialog);
		//显示的数量
		if(food.hasPopTastes()){
			
			final List<Taste> popTastes = new ArrayList<Taste>(food.getPopTastes());
			//只显示前8个常用口味
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
					//设置口味名
					checkBox.setText(thisTaste.getPreference());
					
					checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							Taste thisTaste = (Taste) buttonView.getTag();
							//判断按钮状态，将口味添加进菜品或移除
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
   				Toast.makeText(getContext(), "对不起，\"" + mSelectedFood.toString() + "\"最多只能点255份", Toast.LENGTH_SHORT).show();
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
//				//将搜索项清零
				if(searchEditText != null){
					searchEditText.setText("");
				}
   			}
			
		}catch(NumberFormatException e){
			Toast.makeText(getContext(), "您输入的数量格式不正确，请重新输入", Toast.LENGTH_SHORT).show();
		}
	}
	
	public static interface OnFoodPickedListener{
		/**
		 * 当PickFoodListView选中菜品后，回调此函数通知Activity选中的Food信息
		 * @param food 选中Food的信息
		 */
		public void onPicked(OrderFood food);
		
		/**
		 * 当PickFoodListView选中菜品后，回调此函数通知Activity选中的Food信息，并跳转到口味Activity
		 * @param food
		 * 			选中Food的信息
		 */
		public void onPickedWithTaste(OrderFood food, boolean isTempTaste);
	}
}
