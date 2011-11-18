package com.wireless.ui.view;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.ui.R;
import com.wireless.ui.dialog.AskPwdDialog;

public class OrderFoodListView extends ExpandableListView{

	public final static int PICK_TASTE = 1;
	public final static int PICK_FOOD = 2;
	
	private OnOperListener _operListener;
	private Context _context;
	private List<OrderFood> _foods;
	private int _selectedPos;
	private byte _type = Type.INSERT_ORDER;
	private BaseExpandableListAdapter _adapter;
	
	public OrderFoodListView(Context context, AttributeSet attrs){
		super(context, attrs);
		_context = context;
		/**
		 * 选择每个菜品的操作
		 */
		setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
										int groupPosition, int childPosition, long id) {
				if(_type == Type.INSERT_ORDER){
					_selectedPos = childPosition;
					new ExtOperDialg(_foods.get(childPosition)).show();
					return true;
					
				}else if(_type == Type.UPDATE_ORDER){
					_selectedPos = childPosition;
					new ExtOperDialg(_foods.get(childPosition)).show();
					return true;
					
				}else{
					return false;
				}
			}
		});
	}
	
	@Override
	public void onDraw(Canvas canvas){  
		super.onDraw(canvas);
	}
	
	/**
	 * 设置ListView的类型，目前分为"新点菜"和"已点菜"两种
	 * @param type
	 * 			One of values blew.<br>
	 * 			Type.INSERT_ORDER - 新点菜
	 * 			Type.UPDATE_ORDER - 已点菜
	 */
	public void setType(int type){
		if(type == Type.INSERT_ORDER){
			_type = Type.INSERT_ORDER;
		}else if(type == Type.UPDATE_ORDER){
			_type = Type.UPDATE_ORDER;
		}else{
			_type = Type.INSERT_ORDER;
		}
	}
	
	/**
	 * 设置菜品操作的回调接口
	 * @param operListener
	 */
	public void setOperListener(OnOperListener operListener){
		_operListener = operListener;
	}
	     
	/**
	 * 在source data变化的时候，调用此函数来更新ListView的数据。
	 */
	public void notifyDataChanged(){
		if(_adapter != null){
			_adapter.notifyDataSetChanged();
		}
	}

	/**
	 * 函数用于更新ListView的source data，第一次调用此函数的时候，会中同时创建相应的Adapter。
	 * @param foods
	 * 			在ListView上显示的菜品数据
	 */
	public void notifyDataChanged(List<OrderFood> foods){
		if(foods != null){
			_foods = foods;
			if(_adapter != null){
				_adapter.notifyDataSetChanged();
			}else{
				if(_type == Type.INSERT_ORDER){
					_adapter = new Adapter("新点菜");
				}else{
					_adapter = new Adapter("已点菜");
				}
				setAdapter(_adapter);				
			}
		}else{
			throw new NullPointerException();
		}
	}
	
	/**
	 * 此函数用于更新选中的菜品，比如"口味"操作，需要从TasteActivity将结果回传ListView，
	 * 这种情况就调用此函数来更新选中的菜品
	 * @param food
	 */
	public void notifyDataChanged(OrderFood food){
		if(food != null){
			if(_adapter != null){
				_foods.set(_selectedPos, food);
				_adapter.notifyDataSetChanged();
			}
		}else{
			throw new NullPointerException();
		}

	}
	
	public class Adapter extends BaseExpandableListAdapter{

		private String _groupTitle;
		
		public Adapter(String groupTitle){
			_groupTitle = groupTitle;
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
		public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			View view = View.inflate(_context, R.layout.dropchilditem, null);
			OrderFood food = _foods.get(childPosition);
			//show the name to each food
			String status = "";
			if(food.isSpecial()){
				status = "特";
			}
			if(food.isRecommend()){
				if(status.length() == 0){
					status = "荐";
				}else{
					status = status + ",荐";
				}
			}
			if(food.isGift()){
				if(status.length() == 0){
					status = "赠";
				}else{
					status = status + ",赠";
				}
			}
			if(status.length() != 0){
				status = "(" + status + ")";
			}
			
			String tempStatus = null;
			if(food.isTemporary){
				tempStatus = "(临)";
			}else{
				tempStatus = "";
			}
			
			String hangStatus = null;
			if(food.hangStatus == OrderFood.FOOD_HANG_UP){
				hangStatus = "叫";
			}else if(food.hangStatus == OrderFood.FOOD_IMMEDIATE){
				hangStatus = "即";
			}else{
				hangStatus = "";
			}
			if(hangStatus.length() != 0){
				hangStatus = "(" + hangStatus + ")";
			}
			
			String hurriedStatus = null;
			if(food.isHurried){
				hurriedStatus = "(催)";
			}else{
				hurriedStatus = "";
			}
			((TextView) view.findViewById(R.id.foodname)).setText(tempStatus + hangStatus + hurriedStatus + food.name + status);
			//show the order amount to each food
			((TextView) view.findViewById(R.id.accountvalue)).setText(Util.float2String2(food.getCount()));
			//show the price to each food
			((TextView) view.findViewById(R.id.pricevalue)).setText(Util.float2String(food.calcPrice2()));
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
					/**
					 * "新点菜"时直接显示删菜数量Dialog
					 */
					@Override
					public void onClick(View v) {
						new AskCancelAmountDialog(_foods.get(childPosition)).show();
					}
				});

				//"口味"操作
				ImageView addTasteImgView = (ImageView)view.findViewById(R.id.addtaste);
				addTasteImgView.setBackgroundResource(R.drawable.commit);
				addTasteImgView.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						_selectedPos = childPosition;
						if(_operListener != null){
							_operListener.OnPickTaste(_foods.get(childPosition));
						}
					}
				});
				
			}else{
				//"退菜"操作
				ImageView cancelFoodImgView = (ImageView)view.findViewById(R.id.deletefood);
				cancelFoodImgView.setBackgroundResource(R.drawable.commit);
				cancelFoodImgView.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						/**
						 * 提示输入权限密码2，验证通过的情况下显示删菜数量Dialog
						 */
						new AskPwdDialog(_context, AskPwdDialog.PWD_3){
							@Override
							protected void onPwdPass(Context context){
								dismiss();
								new AskCancelAmountDialog(_foods.get(childPosition)).show();
							}
						}.show();
					}
				});

			}
			return view;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
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
			
			/**
			 * "新点菜"的Group显示"点菜"Button
			 */
			if(_type == Type.INSERT_ORDER){
				ImageView orderImg = (ImageView)view.findViewById(R.id.orderimage);
				orderImg.setBackgroundResource(R.drawable.commit);
				
				orderImg.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						if(_operListener != null){
							_operListener.OnPickFood();
						}
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
			return true;
		}
		
	}
	
	/**
	 * 提示输入删除数量的Dialog
	 */
	private class AskCancelAmountDialog extends Dialog{
	
		AskCancelAmountDialog(final OrderFood selectedFood) {
			super(_context, R.style.FullHeightDialog);
			
			View view = LayoutInflater.from(_context).inflate(R.layout.alert, null);
			setContentView(view);
			getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView)view.findViewById(R.id.ordername)).setText("请输入" + selectedFood.name + "的删除数量");
			
			//删除数量默认为此菜品的点菜数量
			final EditText cancelEdtTxt = (EditText)view.findViewById(R.id.mycount);			
			cancelEdtTxt.setText(Util.float2String2(selectedFood.getCount()));
			
			//"确定"Button
			Button okBtn = (Button)view.findViewById(R.id.confirm);
			okBtn.setText("确定");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					float foodAmount = selectedFood.getCount();
					float cancelAmount = Float.parseFloat(cancelEdtTxt.getText().toString());
					
					if(foodAmount == cancelAmount){
						/**
						 * 如果数量相等，则从列表中删除此菜
						 */
						_foods.remove(selectedFood);
						_adapter.notifyDataSetChanged();
						dismiss();
						Toast.makeText(_context, "删除" + selectedFood.name + cancelAmount + "份成功", 1).show();
						
					}else if(foodAmount > cancelAmount){
						/**
						 * 如果删除数量少于已点数量，则相应减去删除数量
						 */
						selectedFood.setCount(foodAmount - cancelAmount);
						_adapter.notifyDataSetChanged();
						dismiss();
						Toast.makeText(_context, "删除" + selectedFood.name + cancelAmount + "份成功", 1).show();
						
					}else{
						new AlertDialog.Builder(_context)
							.setTitle("提示")
							.setMessage("你输入的删除数量大于已点数量, 请重新输入")
							.setNeutralButton("确定", null)
							.show();
					}
				}
			});
			
			//"取消"Button
			Button cancelBtn = (Button)view.findViewById(R.id.alert_cancel);
			cancelBtn.setText("取消");
			cancelBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}		
	}
	
	/**
	 * 点击菜品列表后的扩展功能 Dialog
	 */
	private class ExtOperDialg extends Dialog{

		ExtOperDialg(final OrderFood selectedFood) {
			super(_context, R.style.FullHeightDialog);
			final View view =LayoutInflater.from(_context).inflate(R.layout.item_alert, null);
			setContentView(view);
			getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView)view.findViewById(R.id.ordername)).setText("请选择" + selectedFood.name + "的操作");
			if(_type == Type.INSERT_ORDER){
				/**
				 * 新点菜是扩展功能为"删菜"、"口味"、"叫起/取消叫起"
				 */
				//删菜功能
				((TextView)view.findViewById(R.id.item1Txt)).setText("删菜");
				((RelativeLayout)view.findViewById(R.id.r1)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						dismiss();
						new AskCancelAmountDialog(selectedFood).show();							
					}
				});
				
				//口味功能
				((TextView)view.findViewById(R.id.item2Txt)).setText("口味");
				((RelativeLayout)view.findViewById(R.id.r2)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub							
						if(_operListener != null){
							_operListener.OnPickTaste(selectedFood);
						}
					}
				});
				
				//叫起/取消叫起
				if(selectedFood.hangStatus == OrderFood.FOOD_NORMAL){
					((TextView)view.findViewById(R.id.item3Txt)).setText("叫起");						
				}else{
					((TextView)view.findViewById(R.id.item3Txt)).setText("取消叫起");
				}
				((RelativeLayout)view.findViewById(R.id.r3)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						if(selectedFood.hangStatus == OrderFood.FOOD_NORMAL){
							selectedFood.hangStatus = OrderFood.FOOD_HANG_UP;
							((TextView)view.findViewById(R.id.item3Txt)).setText("取消叫起");
						}else{
							selectedFood.hangStatus = OrderFood.FOOD_NORMAL;
							((TextView)view.findViewById(R.id.item3Txt)).setText("叫起");								
						}
					}
				});
				
			}else{
				/**
				 * 已点菜的扩展功能为"退菜"、"即起"、"催菜/取消催菜"
				 */
				//退菜功能
				((TextView)view.findViewById(R.id.item1Txt)).setText("退菜");
				((RelativeLayout)view.findViewById(R.id.r1)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						dismiss();
						new AskPwdDialog(_context, AskPwdDialog.PWD_3){							
							@Override
							protected void onPwdPass(Context context){
								dismiss();
								new AskCancelAmountDialog(selectedFood).show();
							}
						}.show();
					}
				});
				
				//如果菜品是叫起状态，显示"即起"功能
				if(selectedFood.hangStatus == OrderFood.FOOD_HANG_UP){
					((TextView)view.findViewById(R.id.item2Txt)).setText("即起");
					((RelativeLayout)view.findViewById(R.id.r2)).setOnClickListener(new View.OnClickListener() {						
						@Override
						public void onClick(View arg0) {
			    			if(selectedFood.hangStatus == OrderFood.FOOD_HANG_UP){
			    				selectedFood.hangStatus = OrderFood.FOOD_IMMEDIATE;
								((TextView)view.findViewById(R.id.item2Txt)).setText("即起");
			    				
			    			}else if(selectedFood.hangStatus == OrderFood.FOOD_IMMEDIATE){
			    				selectedFood.hangStatus = OrderFood.FOOD_HANG_UP;
								((TextView)view.findViewById(R.id.item2Txt)).setText("重新叫起");
			    			}						
						}
					});
					
					//催菜/取消催菜功能
					if(selectedFood.isHurried){
						((TextView)view.findViewById(R.id.item3Txt)).setText("取消催菜");							
					}else{
						((TextView)view.findViewById(R.id.item3Txt)).setText("催菜");						
					}
					((RelativeLayout)view.findViewById(R.id.r3)).setOnClickListener(new View.OnClickListener() {						
						@Override
						public void onClick(View arg0) {
							if(selectedFood.isHurried){
								selectedFood.isHurried = false;
								((TextView)view.findViewById(R.id.item3Txt)).setText("催菜");							
							}else{
								selectedFood.isHurried = true;
								((TextView)view.findViewById(R.id.item3Txt)).setText("取消催菜");									
							}
						}
					});
					
				}else{
					
					//催菜/取消催菜功能
					if(selectedFood.isHurried){
						((TextView)view.findViewById(R.id.item2Txt)).setText("取消催菜");							
					}else{
						((TextView)view.findViewById(R.id.item2Txt)).setText("催菜");						
					}
					((RelativeLayout)view.findViewById(R.id.r2)).setOnClickListener(new View.OnClickListener() {						
						@Override
						public void onClick(View arg0) {
							if(selectedFood.isHurried){
								selectedFood.isHurried = false;
								((TextView)view.findViewById(R.id.item2Txt)).setText("催菜");	
					
							}else{
								selectedFood.isHurried = true;
								((TextView)view.findViewById(R.id.item2Txt)).setText("取消催菜");									
							}						
						}
					});
					
					((RelativeLayout)view.findViewById(R.id.r3)).setVisibility(View.GONE);
				}					
			}
			
			//返回Button
			Button cancelBtn = (Button)view.findViewById(R.id.back);
			cancelBtn.setText("返回");				
			cancelBtn.setOnClickListener(new View.OnClickListener() {					
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}
		
		@Override
		protected void onStop(){
			_adapter.notifyDataSetChanged();
		}		
	}
	
	public static interface OnOperListener{
		public void OnPickTaste(OrderFood selectedFood);
		public void OnPickFood();
	}
	
}
