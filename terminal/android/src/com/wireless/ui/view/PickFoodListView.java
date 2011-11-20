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
		 * 点击每个菜品后弹出点菜数量Dialog
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
	 * 设置点完某个菜品后的回调函数
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
				status = "特";
			}
			if(_foods[position].isRecommend()){
				if(status.length() == 0){
					status = "荐";
				}else{
					status = status + ",荐";
				}
			}
			if(_foods[position].isGift()){
				if(status.length() == 0){
					status = "赠";
				}else{
					status = status + ",赠";
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
	 * 提示输入点菜数量的Dialog
	 * @author Ying.Zhang
	 *
	 */
	private class AskOrderAmountDialog extends Dialog{

		public AskOrderAmountDialog(final Food selectedFood) {
			super(_context, R.style.FullHeightDialog);
			
			setContentView(R.layout.alert);
			
			getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			
			((TextView)findViewById(R.id.ordername)).setText("请输入" + selectedFood.name + "的点菜数量");
			((EditText)findViewById(R.id.mycount)).setText("1");
			
			//"确定"Button
			Button okBtn = (Button)findViewById(R.id.confirm);
			okBtn.setText("确定");
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
			
			//"取消"Button
			Button cancelBtn = (Button)findViewById(R.id.alert_cancel);
			cancelBtn.setText("取消");
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
		 * 当PickFoodListView选中菜品后，回调此函数通知Activity选中的Food信息
		 * @param food
		 * 			选中Food的信息
		 */
		public void OnPicked(OrderFood food);
	}

}
