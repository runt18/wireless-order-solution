package com.wireless.view;



import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.pad.R;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;


public class PickFoodListView extends GridView {

	public final static int TAG_NUM = 0;
	public final static int TAG_PINYIN = 1;
	public final static int TAG_OCCASINAL = 2;
	
	private Context _context;
	private Food[] _foods = null;
	private BaseAdapter _adapter = null;
	private OnFoodPickedListener _foodPickedListener;
	//private int _tag;

	

	public PickFoodListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		_context = context;

		/**
		 * ���ÿ����Ʒ�󵯳��������Dialog
		 */
		setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,	long rowID) {
				if(_foods[position].isSellOut()){
					Toast.makeText(_context, "�Բ���" + _foods[position].getName() + "�Ѿ�����", 0).show();
				}else{
					//new AskOrderAmountDialog(_foods[position]).show();
					showOrderAmountDialog(_foods[position]);
				}	   
			}
		});
	}
	
	/**
	 * ȡ��ListView������Դ
	 * @return
	 */
	public Food[] getSourceData(){
		return _foods;
	}
	
	/**
	 * 
	 * @param foods
	 */
	public void notifyDataChanged(Food[] foods, int tag){
		_foods = foods;
		//_tag = tag;
		if(_adapter != null){
			_adapter.notifyDataSetChanged();
		}else{
			setAdapter(new Adapter());
		}
	}	
	

	/**
	 * ��ʾ������������Dialog
	 * @author Ying.Zhang
	 *
	 */
	private void showOrderAmountDialog(Food food){
		
		final OrderFood _selectedFood = new OrderFood(food);
		final EditText editText = new EditText(_context);
		/***
		 * ��ʼ��һ��View
		 * */
		LayoutInflater  inflater = LayoutInflater.from(_context);
		View view = inflater.inflate(R.layout.order_askconfirm, null);
		
		final EditText amountEdtTxt = ((EditText)view.findViewById(R.id.amountEdtTxt));
		//FIXME
		amountEdtTxt.setText("1");
		amountEdtTxt.setSelection(amountEdtTxt.getText().length());

		
		//"����"CheckBox
		CheckBox hurriedChkBox = (CheckBox)view.findViewById(R.id.orderHurriedChk);
		hurriedChkBox.setText("����");
		hurriedChkBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){				
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					_selectedFood.hangStatus = OrderFood.FOOD_HANG_UP;
					Toast.makeText(_context, "����\"" + _selectedFood.toString() + "\"", 0).show();
				}else{
					_selectedFood.hangStatus = OrderFood.FOOD_NORMAL;
					Toast.makeText(_context, "ȡ������\"" + _selectedFood.toString() + "\"", 0).show();
				}
				
			}
		});
		
		new AlertDialog.Builder(_context)
			.setTitle("������" + _selectedFood.getName() + "�ĵ������")
			.setView(editText)
			.setView(view)
			.setNeutralButton("ȷ��",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,	int which){
						
						try{
							float orderAmount = Float.parseFloat(amountEdtTxt.getText().toString());
							
			       			if(orderAmount > 255){
			       				Toast.makeText(_context, "�Բ���\"" + _selectedFood.toString() + "\"���ֻ�ܵ�255��", 0).show();
			       			}else{
			       				_selectedFood.setCount(orderAmount);
			       				if(_foodPickedListener != null){			
			       					_foodPickedListener.onPicked(_selectedFood);
			       				}
								
			       			}
							
						}catch(NumberFormatException e){
							Toast.makeText(_context, "�������������ʽ����ȷ������������", 0).show();
						}
						
					}
				})
			.setNegativeButton("ȡ��", null)
			.show();
	}
	
	
	/**
	 * ���õ���ĳ����Ʒ��Ļص�����
	 * @param foodPickedListener
	 */
	public void setFoodPickedListener(OnFoodPickedListener foodPickedListener){
		_foodPickedListener = foodPickedListener;
	}
	
	private class Adapter extends BaseAdapter{
		@Override
		public int getCount() {			
			return _foods.length;
		}

		@Override
		public Object getItem(int position) {
			return _foods[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			
			if(convertView == null){
				view = LayoutInflater.from(_context).inflate(R.layout.fooditem, null);
			}else{
				view = convertView;
			}
			
			StringBuffer status = new StringBuffer();
			if(_foods[position].isSpecial()){
				status.append("��");
			}
			if(_foods[position].isRecommend()){
				if(status.length() == 0){
					status.append("��");
				}else{
					status.append(",��");
				}
			}
			if(_foods[position].isGift()){
				if(status.length() == 0){
					status.append("��");
				}else{
					status.append(",��");
				}
			}
			if(_foods[position].isSellOut()){
				if(status.length() == 0){
					status.append("ͣ");
				}else{
					status.append(",ͣ");
				}				
			}
			if(status.length() != 0){
				status.insert(0, "(").insert(status.length(), ")");
			}
			
			((TextView)view.findViewById(R.id.foodname)).setText(_foods[position].getName());
			((TextView)view.findViewById(R.id.foodpinyins)).setText(status);
			((TextView)view.findViewById(R.id.foodprices)).setText(Util.CURRENCY_SIGN + Float.toString(_foods[position].getPrice()));
			
			return view;
		}
	}	
	
	public static interface OnFoodPickedListener{
		/**
		 * ��PickFoodListViewѡ�в�Ʒ�󣬻ص��˺���֪ͨActivityѡ�е�Food��Ϣ
		 * @param food
		 * 			ѡ��Food����Ϣ
		 */
		public void onPicked(OrderFood food);
		
		/**
		 * ��PickFoodListViewѡ�в�Ʒ�󣬻ص��˺���֪ͨActivityѡ�е�Food��Ϣ������ת����ζActivity
		 * @param food
		 * 			ѡ��Food����Ϣ
		 */
		public void onPickedWithTaste(OrderFood food);
	}

}
