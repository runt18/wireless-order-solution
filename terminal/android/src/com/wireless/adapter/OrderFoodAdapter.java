package com.wireless.adapter;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wireless.protocol.Food;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.ui.R;

public class OrderFoodAdapter extends BaseExpandableListAdapter{

	private String _groupTitle;
	private Context _context;
	private List<Food> _foods;
	private byte _type = Type.INSERT_ORDER;
	
	public OrderFoodAdapter(Context context, String groupTitle, List<Food> foods, byte type){
		_context = context;
		_groupTitle = groupTitle;
		_foods = foods;
		_type = type;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return _foods.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		View view = View.inflate(_context, R.layout.dropchilditem, null);
		Food food = _foods.get(childPosition);
		//show the name to each food
		((TextView) view.findViewById(R.id.foodname)).setText(food.name);
		//show the order amount to each food
		((TextView) view.findViewById(R.id.accountvalue)).setText(Util.float2String2(food.getCount()));
		//show the price to each food
		((TextView) view.findViewById(R.id.pricevalue)).setText(Util.float2String(food.totalPrice2()));
		//show the taste to each food
		((TextView)view.findViewById(R.id.taste)).setText(food.tastePref);
		/**
		 * "新点菜"的ListView显示"删菜"和"口味"
		 * "已点菜"的ListView显示"退菜"和"催菜"
		 */
		if(_type == Type.INSERT_ORDER){
			//"删菜"操作			 
			ImageView delFoodImgView = (ImageView)view.findViewById(R.id.deletefood);
			delFoodImgView.setBackgroundResource(R.drawable.commit);
			delFoodImgView.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub					
				}
			});

			//"口味"操作
			ImageView addTasteImgView = (ImageView)view.findViewById(R.id.addtaste);
			addTasteImgView.setBackgroundResource(R.drawable.commit);
			addTasteImgView.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub					
				}
			});
			
		}else{
			//"退菜"操作
			ImageView cancelFoodImgView = (ImageView)view.findViewById(R.id.deletefood);
			cancelFoodImgView.setBackgroundResource(R.drawable.commit);
			cancelFoodImgView.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub					
				}
			});

		}
		return view;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return _foods.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return _groupTitle;
	}

	@Override
	public int getGroupCount() {
		return 1;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		View view = View.inflate(_context, R.layout.dropgrounpitem, null);
		((TextView)view.findViewById(R.id.grounname)).setText(_groupTitle);
		
		if(_type == Type.INSERT_ORDER){
			ImageView orderImg = (ImageView)view.findViewById(R.id.orderimage);
			orderImg.setBackgroundResource(R.drawable.commit);
			
			orderImg.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					drop.orderfood();
				}
			});
		}
		return view;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return false;
	}

	private class AskAmountDialog extends Dialog{

		public AskAmountDialog() {
			super(_context, R.style.FullHeightDialog);
			View view = LayoutInflater.from(_context).inflate(R.layout.alert, null);
			setContentView(view);
			getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView)view.findViewById(R.id.ordername)).setText("请输入" + list.get(position).name+ "的删除数量");
		}
		
	}
	
}
