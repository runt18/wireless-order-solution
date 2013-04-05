package com.wireless.view;



import java.util.List;

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
import com.wireless.util.NumericUtil;


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
		 * 点击每个菜品后弹出点菜数量Dialog
		 */
		setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,	long rowID) {
				if(_foods[position].isSellOut()){
					Toast.makeText(_context, "对不起，" + _foods[position].getName() + "已经售完", Toast.LENGTH_SHORT).show();
				}else{
					//new AskOrderAmountDialog(_foods[position]).show();
					showOrderAmountDialog(_foods[position]);
				}	   
			}
		});
	}
	
	/**
	 * 取得ListView的数据源
	 * @return
	 */
	public Food[] getSourceData(){
		return _foods;
	}
	
	public void notifyDataChanged(List<Food> foods, int tag){
		_foods = foods.toArray(new Food[foods.size()]);
		//_tag = tag;
		if(_adapter != null){
			_adapter.notifyDataSetChanged();
		}else{
			setAdapter(new Adapter());
		}
	}

	/**
	 * 提示输入点菜数量的Dialog
	 * @author Ying.Zhang
	 *
	 */
	private void showOrderAmountDialog(Food food){
		
		final OrderFood _selectedFood = new OrderFood(food);
		final EditText editText = new EditText(_context);
		/***
		 * 初始化一个View
		 * */
		LayoutInflater  inflater = LayoutInflater.from(_context);
		View view = inflater.inflate(R.layout.order_askconfirm, null);
		
		final EditText amountEdtTxt = ((EditText)view.findViewById(R.id.amountEdtTxt));
		//FIXME
		amountEdtTxt.setText("1");
		amountEdtTxt.setSelection(amountEdtTxt.getText().length());

		
		//"叫起"CheckBox
		CheckBox hurriedChkBox = (CheckBox)view.findViewById(R.id.orderHurriedChk);
		hurriedChkBox.setText("叫起");
		hurriedChkBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){				
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					_selectedFood.setHangup(true);
					Toast.makeText(_context, "叫起\"" + _selectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
				}else{
					_selectedFood.setHangup(false);
					Toast.makeText(_context, "取消叫起\"" + _selectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		new AlertDialog.Builder(_context)
			.setTitle("请输入" + _selectedFood.getName() + "的点菜数量")
			.setView(editText)
			.setView(view)
			.setNeutralButton("确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,	int which){
						
						try{
							float orderAmount = Float.parseFloat(amountEdtTxt.getText().toString());
							
			       			if(orderAmount > 255){
			       				Toast.makeText(_context, "对不起，\"" + _selectedFood.toString() + "\"最多只能点255份", Toast.LENGTH_SHORT).show();
			       			}else{
			       				_selectedFood.setCount(orderAmount);
			       				if(_foodPickedListener != null){			
			       					_foodPickedListener.onPicked(_selectedFood);
			       				}
								
			       			}
							
						}catch(NumberFormatException e){
							Toast.makeText(_context, "您输入的数量格式不正确，请重新输入", Toast.LENGTH_SHORT).show();
						}
						
					}
				})
			.setNegativeButton("取消", null)
			.show();
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
			
			StringBuffer status = new StringBuffer();
			if(_foods[position].isSpecial()){
				status.append("特");
			}
			if(_foods[position].isRecommend()){
				if(status.length() == 0){
					status.append("荐");
				}else{
					status.append(",荐");
				}
			}
			if(_foods[position].isGift()){
				if(status.length() == 0){
					status.append("赠");
				}else{
					status.append(",赠");
				}
			}
			if(_foods[position].isSellOut()){
				if(status.length() == 0){
					status.append("停");
				}else{
					status.append(",停");
				}				
			}
			if(status.length() != 0){
				status.insert(0, "(").insert(status.length(), ")");
			}
			
			((TextView)view.findViewById(R.id.foodname)).setText(_foods[position].getName());
			((TextView)view.findViewById(R.id.foodpinyins)).setText(status);
			((TextView)view.findViewById(R.id.foodprices)).setText(NumericUtil.CURRENCY_SIGN + Float.toString(_foods[position].getPrice()));
			
			return view;
		}
	}	
	
	public static interface OnFoodPickedListener{
		/**
		 * 当PickFoodListView选中菜品后，回调此函数通知Activity选中的Food信息
		 * @param food
		 * 			选中Food的信息
		 */
		public void onPicked(OrderFood food);
		
		/**
		 * 当PickFoodListView选中菜品后，回调此函数通知Activity选中的Food信息，并跳转到口味Activity
		 * @param food
		 * 			选中Food的信息
		 */
		public void onPickedWithTaste(OrderFood food);
	}

}
