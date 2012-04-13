package com.wireless.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.pad.R;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;


public class TempListView extends ListView {
	
	private List<OrderFood> _tmpFoods = new ArrayList<OrderFood>();
	private BaseAdapter _adapter = new Adapter();
	
	public TempListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setAdapter(_adapter);		
	}

	
	/**
	 * ����ʱ���б���ɾ����������Ч����ʱ��Ʒ(������Ʒ���ͼ�Ǯ)
	 * @return
	 * 		������Ч����ʱ��Ʒ��Ϣ
	 */
	public List<OrderFood> removeValidFoods(){
		ArrayList<OrderFood> validFoods = new ArrayList<OrderFood>();
		//filter the temporary foods with food name and price
		Iterator<OrderFood> iter = _tmpFoods.iterator();
		while (iter.hasNext()) {
			OrderFood food = iter.next();
			if(!(food.name.equals("") || food.getPrice() > 9999)) {
				validFoods.add(food);
				iter.remove();
			}
		}
		_adapter.notifyDataSetChanged();
		return validFoods;
	}
	
	/**
	 * ����һ����ʱ��
	 */
	public void addTemp(){
		OrderFood tmpFood = new OrderFood();
		tmpFood.isTemporary = true;
		tmpFood.aliasID = Util.genTempFoodID();
		tmpFood.hangStatus = OrderFood.FOOD_NORMAL;
		tmpFood.setPrice(new Float(10000));
		tmpFood.setCount(new Float(1));
		_tmpFoods.add(tmpFood);
		_adapter.notifyDataSetChanged();
		//���������
		((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindowToken(), 0);
		//���������һ��
		post( new Runnable() {     
			@Override
			public void run() { 
				smoothScrollToPosition(getCount());
			}
		});
	}
	
	/**
	 * 
	 * @param foods
	 */
	public void notifyDataChanged(){
		_adapter.notifyDataSetChanged();
	}	
	
	
	private class Adapter extends BaseAdapter{

		@Override
		public int getCount() {
			return _tmpFoods.size();
		}

		@Override
		public Object getItem(int position) {
			return _tmpFoods.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

	
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			final OrderFood food = _tmpFoods.get(position);
			
			View view;			
			
			if(convertView == null){
				view = LayoutInflater.from(getContext()).inflate(R.layout.temp_item, null);				
			}else{
				view = convertView;
			}
			
			((TextView)view.findViewById(R.id.occasin)).setText("��ʱ��" + (position + 1));			
			
			/**
			 * ������ֵ
			 */
			EditText foodNameEdtTxt = (EditText)view.findViewById(R.id.occasiname);
			//������ʱ������ǰɾ���ı��������
			if(foodNameEdtTxt.getTag() != null){
				foodNameEdtTxt.removeTextChangedListener((TextWatcher)foodNameEdtTxt.getTag());
			}
			foodNameEdtTxt.setText(food.name);
			TextWatcher textWatcher = new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
					
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					food.name = s.toString().replace(",", ";").replace("��", "��").trim();
					_tmpFoods.set(position, food);
				}
			};
			
			foodNameEdtTxt.setTag(textWatcher);
			foodNameEdtTxt.addTextChangedListener(textWatcher);		

			
			/**
			 * ��Ǯ��ֵ
			 */
			final EditText foodPriceEdtTxt = (EditText)view.findViewById(R.id.occasiprice);
			//������ʱ�˼�Ǯǰɾ���ı��������
			if(foodPriceEdtTxt.getTag() != null){
				foodPriceEdtTxt.removeTextChangedListener((TextWatcher)foodPriceEdtTxt.getTag());
			}
			foodPriceEdtTxt.setText(food.getPrice() > 9999 ? "" : Util.float2String2(food.getPrice()));
			
			textWatcher = new TextWatcher() {				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					
				}				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
					
				}				
				@Override
				public void afterTextChanged(Editable s) {
					if(s.toString().length() != 0){
						try{
							Float price = Float.parseFloat(s.toString());
							if(price >= 0 && price < 9999){
								food.setPrice(price);
								_tmpFoods.set(position, food);
							}else{
								foodPriceEdtTxt.setText(food.getPrice() > 9999 ? "" : Util.float2String2(food.getPrice()));
								foodPriceEdtTxt.setSelection(foodPriceEdtTxt.getText().length());
								Toast.makeText(getContext(), "��ʱ��" + 
											   (food.name.length() == 0 ? (position + 1) : "(" + food.name + ")") + 
											   "�ļ۸�Χ��0��9999", 0).show();
							}
							
						}catch(NumberFormatException e){
							foodPriceEdtTxt.setText(food.getPrice() > 9999 ? "" : Util.float2String2(food.getPrice()));
							foodPriceEdtTxt.setSelection(foodPriceEdtTxt.getText().length());
							Toast.makeText(getContext(), "��������ʱ��" + 
										  (food.name.length() == 0 ? (position + 1) : "(" + food.name + ")") + 
										  "�ļ�Ǯ��ʽ����ȷ������������", 0).show();
						}						
					}
				}
			};
			
			foodPriceEdtTxt.setTag(textWatcher);
			foodPriceEdtTxt.addTextChangedListener(textWatcher);			
			
			/**
			 * ������ֵ
			 */
			final EditText foodAmountEdtTxt = (EditText)view.findViewById(R.id.occasicount);
			//������ʱ������ǰɾ���ı��������
			if(foodAmountEdtTxt.getTag() != null){
				foodAmountEdtTxt.removeTextChangedListener((TextWatcher)foodAmountEdtTxt.getTag());
			}
			foodAmountEdtTxt.setText(Util.float2String2(food.getCount()));
			
			textWatcher = new TextWatcher() {				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					
				}				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
					
				}				
				@Override
				public void afterTextChanged(Editable s) {
					if(s.toString().length() != 0){
						try{
							Float amount = Float.parseFloat(s.toString());
							if(amount > 0 && amount <= 255){
								food.setCount(amount);
								_tmpFoods.set(position, food);
							}else{
								foodAmountEdtTxt.setText(Util.float2String2(food.getCount()));
								foodAmountEdtTxt.setSelection(foodAmountEdtTxt.getText().length());
								Toast.makeText(getContext(), "��ʱ��" + 
										   (food.name.length() == 0 ? (position + 1) : "(" + food.name + ")") + 
											  "��������Χ��1��255", 0).show();
							}
						}catch(NumberFormatException e){
							foodAmountEdtTxt.setText(Util.float2String2(food.getCount()));
							foodAmountEdtTxt.setSelection(foodAmountEdtTxt.getText().length());
							Toast.makeText(getContext(), "��������ʱ��" + 
										  (food.name.length() == 0 ? (position + 1) : "(" + food.name + ")") + 
										  "��������ʽ����ȷ������������", 0).show();
						}						
					}
				}
			};
	       
			foodAmountEdtTxt.setTag(textWatcher);
			foodAmountEdtTxt.addTextChangedListener(textWatcher);	
	        
			
			/**
			 * ���ɾ���˰�ť
			 */
			ImageView removefood = (ImageView)view.findViewById(R.id.remove);
			removefood.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {		
					_tmpFoods.remove(position);
					_adapter.notifyDataSetChanged();
					//���������
					((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindowToken(), 0);
				}
			});
			return view;
		}		
		
	}
}
