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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.ui.R;

public class PickFoodListView extends ListView {

	private Context _context;
	private Food[] _foods = null;
	private BaseAdapter _adapter = null;
	private OnFoodPickedListener _foodPickedListener;
	
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
	 * 
	 * @param foods
	 */
	public void notifyDataChanged(Food[] foods){
		_foods = foods;
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
			
			((TextView)view.findViewById(R.id.foodname)).setText(_foods[position].name+status);
			((TextView)view.findViewById(R.id.foodprice)).setText(Util.CURRENCY_SIGN +Float.toString(_foods[position].getPrice()));
			
			return view;
		}
	}
	
	/**
	 * ��ʾ������������Dialog
	 * @author Ying.Zhang
	 *
	 */
	private class AskOrderAmountDialog extends Dialog{

		public AskOrderAmountDialog(final Food selectedFood) {
			super(_context, R.style.FullHeightDialog);
			
			setContentView(R.layout.alert);
			
			getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			
			((TextView)findViewById(R.id.ordername)).setText("������" + selectedFood.name + "�ĵ������");
			((EditText)findViewById(R.id.mycount)).setText("1");
			
			//"ȷ��"Button
			Button okBtn = (Button)findViewById(R.id.confirm);
			okBtn.setText("ȷ��");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					OrderFood food = new OrderFood(selectedFood);

					float orderAmount = Float.parseFloat(((EditText)findViewById(R.id.mycount)).getText().toString());
					food.setCount(orderAmount);
					
					if(_foodPickedListener != null){
						_foodPickedListener.OnPicked(food);
					}
					
					dismiss();
				}
			});
			
			//"ȡ��"Button
			Button cancelBtn = (Button)findViewById(R.id.alert_cancel);
			cancelBtn.setText("ȡ��");
			cancelBtn.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}
		
	}
	
	public static interface OnFoodPickedListener{
		/**
		 * ��PickFoodListViewѡ�в�Ʒ�󣬻ص��˺���֪ͨActivityѡ�е�Food��Ϣ
		 * @param food
		 * 			ѡ��Food����Ϣ
		 */
		public void OnPicked(OrderFood food);
	}

}
