package com.wireless.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.ui.R;

public class PickFoodListView extends ListView {

	public final static int TAG_NUM = 0;
	public final static int TAG_PINYIN = 1;
	public final static int TAG_OCCASINAL = 2;
	
	private Context _context;
	private Food[] _foods = null;
	private BaseAdapter _adapter = null;
	private OnFoodPickedListener _foodPickedListener;
	private int _tag;

	

	public PickFoodListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		_context = context;


		/**
		 * ���ÿ����Ʒ�󵯳��������Dialog
		 */
		setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,	long rowID) {
				new AskOrderAmountDialog(_foods[position]).show();
			   
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
		_tag = tag;
		if(_adapter != null){
			_adapter.notifyDataSetChanged();
		}else{
			setAdapter(new Adapter());
		}
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
			
			String status = "";
			if(_foods[position].isSpecial()){
				status = "��";
			}
			if(_foods[position].isRecommend()){
				if(status.length() == 0){
					status = "��";
				}else{
					status = status + ",��";
				}
			}
			if(_foods[position].isGift()){
				if(status.length() == 0){
					status = "��";
				}else{
					status = status + ",��";
				}
			}
			if(status.length() != 0){
				status = "(" + status + ")";
			}
			
			((TextView)view.findViewById(R.id.foodname)).setText(_foods[position].name + status);
			if(_tag == TAG_NUM){
				((TextView)view.findViewById(R.id.foodpinyin)).setText("��ţ�");
				((TextView)view.findViewById(R.id.foodpinyins)).setText(String.valueOf(_foods[position].foodAlias));
			}else{
				((TextView)view.findViewById(R.id.foodpinyin)).setText("ƴ����");
				((TextView)view.findViewById(R.id.foodpinyins)).setText(_foods[position].pinyin);
			}
			((TextView)view.findViewById(R.id.foodprices)).setText(Util.CURRENCY_SIGN + Float.toString(_foods[position].getPrice()));
			
			return view;
		}
	}
	
	/**
	 * ��ʾ������������Dialog
	 * @author Ying.Zhang
	 *
	 */
	private class AskOrderAmountDialog extends Dialog{

		private OrderFood _selectedFood;
		
		public AskOrderAmountDialog(Food food) {
			super(_context, R.style.FullHeightDialog);
			
			_selectedFood = new OrderFood(food);
			
			setContentView(R.layout.order_confirm);
			
			//getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			
			((TextView)findViewById(R.id.orderTitleTxt)).setText("������" + _selectedFood.name + "�ĵ������");
			((EditText)findViewById(R.id.amountEdtTxt)).setText("1");
			
			//"ȷ��"Button
			Button okBtn = (Button)findViewById(R.id.orderConfirmBtn);
			okBtn.setText("ȷ��");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {			
					onPick(false);
				}
			});
			
			//"��ζ"Button
			Button tasteBtn = (Button)findViewById(R.id.orderTasteBtn);
			tasteBtn.setText("��ζ");
			tasteBtn.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View arg0) {
					onPick(true);
				}
			});
			
			//"ȡ��"Button
			Button cancelBtn = (Button)findViewById(R.id.orderCancelBtn);
			cancelBtn.setText("ȡ��");
			cancelBtn.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
			
			//"����"CheckBox
			CheckBox hurriedChkBox = (CheckBox)findViewById(R.id.orderHurriedChk);
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
		}
		
		/**
		 * 
		 * @param selectedFood
		 * @param pickTaste
		 */
		private void onPick(boolean pickTaste){
			try{
				float orderAmount = Float.parseFloat(((EditText)findViewById(R.id.amountEdtTxt)).getText().toString());
				
       			if(orderAmount > 255){
       				Toast.makeText(_context, "�Բ���\"" + _selectedFood.toString() + "\"���ֻ�ܵ�255��", 0).show();
       			}else{
       				_selectedFood.setCount(orderAmount);
       				if(_foodPickedListener != null){	
       					if(pickTaste){
       						_foodPickedListener.onPickedWithTaste(_selectedFood);
       					}else{
       						_foodPickedListener.onPicked(_selectedFood);
       					}
       				}
					dismiss();
       			}
				
			}catch(NumberFormatException e){
				Toast.makeText(_context, "�������������ʽ����ȷ������������", 0).show();
			}
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
