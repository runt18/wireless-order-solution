package com.wireless.ui.view;

import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.excep.BusinessException;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.ui.R;
import com.wireless.ui.dialog.AskPwdDialog;

public class OrderFoodListView extends ExpandableListView{

	public final static int PICK_TASTE = 1;
	public final static int PICK_FOOD = 2;
	
	private OnOperListener mOperListener;
	private OnChangedListener mChgListener;
//	private OrderFood[] mSrcFoods;
	private Order mTmpOrder = new Order();;
	private int mSelectedPos;
	private byte mType = Type.INSERT_ORDER;
	private BaseExpandableListAdapter mAdapter;
	
	public OrderFoodListView(Context context, AttributeSet attrs){
		super(context, attrs);
		/**
		 * 选择每个菜品的操作
		 */
		setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				if(mType == Type.INSERT_ORDER){
					mSelectedPos = childPosition;
					new ExtOperDialg(mTmpOrder.foods[childPosition]).show();
					return true;
					
				}else if(mType == Type.UPDATE_ORDER){
					mSelectedPos = childPosition;
					new ExtOperDialg(mTmpOrder.foods[childPosition]).show();
					return true;
					
				}else{
					return false;
				}
			}
		});
	}
	
	/**
	 * 设置菜品操作的回调接口
	 * @param operListener
	 */
	public void setOperListener(OnOperListener operListener){
		mOperListener = operListener;
	}
	     
	/**
	 * 设置数据源变化的回调接口
	 * @param chgListener
	 */
	public void setChangedListener(OnChangedListener chgListener){
		mChgListener = chgListener;
	}
	
	public void addFood(OrderFood foodToAdd) throws BusinessException{
		mTmpOrder.addFood(foodToAdd);
		notifyDataChanged();
	}
	
	public void addFoods(OrderFood[] foodsToAdd){
		mTmpOrder.addFoods(foodsToAdd);
		notifyDataChanged();
	}
	
	/**
	 * 此函数用于更新选中的菜品，比如"口味"操作，需要从TasteActivity将结果回传ListView，
	 * 这种情况就调用此函数来更新选中的菜品
	 * @param food
	 */
	public void setFood(OrderFood foodToSet){
		if(foodToSet != null && mAdapter != null){
			mTmpOrder.foods[mSelectedPos] = foodToSet;
			mAdapter.notifyDataSetChanged();
		
		}else{
			throw new NullPointerException();
		}
	}
	
	public void setFoods(OrderFood[] foods){
		mTmpOrder.foods = foods;
		notifyDataChanged();
	}
	
	/**
	 * 初始化控件
	 * @param type
	 * 			One of values blew.<br>
	 * 			Type.INSERT_ORDER - 新点菜
	 * 			Type.UPDATE_ORDER - 已点菜
	 */
	public void init(int type){
		if(type == Type.INSERT_ORDER){
			mType = Type.INSERT_ORDER;
		}else if(type == Type.UPDATE_ORDER){
			mType = Type.UPDATE_ORDER;
		}else{
			throw new IllegalArgumentException("The type is NOT valid.");
		}
		
		if(mType == Type.INSERT_ORDER){
			mAdapter = new Adapter("新点菜"){
				@Override
				public void notifyDataSetChanged(){
					trim();
					super.notifyDataSetChanged();
					if(mChgListener != null){
						mChgListener.onSourceChanged();
					}
				}
			};
		}else{
			mAdapter = new Adapter("已点菜"){
				@Override
				public void notifyDataSetChanged(){
					trim();
					super.notifyDataSetChanged();
					if(mChgListener != null){
						mChgListener.onSourceChanged();
					}
				}						
			};
		}
		setAdapter(mAdapter);	
		if(mChgListener != null){
			mChgListener.onSourceChanged();
		}
		
	}
	
	/**
	 * 取得List中的数据源（就是菜品的List信息）
	 * @return
	 * 		OrderFood的List
	 */
	public OrderFood[] getSourceData(){
		return mTmpOrder.foods;
	}
	
	/**
	 * 在source data变化的时候，调用此函数来更新ListView的数据。
	 */
	private void notifyDataChanged(){
		if(mAdapter != null){
			mAdapter.notifyDataSetChanged();
		}
	}

	private void trim(){
		HashMap<OrderFood, OrderFood> foodMap = new HashMap<OrderFood, OrderFood>();
		for(OrderFood food : mTmpOrder.foods){
			if(foodMap.containsKey(food)){
				float amount = foodMap.get(food).getCount() + food.getCount();
				food.setCount((float)Math.round(amount * 100) / 100);
				foodMap.put(food, food);
			}else{
				foodMap.put(food, food);
			}			
		}
		if(mTmpOrder.foods.length != foodMap.size()){
			mTmpOrder.foods = foodMap.values().toArray(new OrderFood[foodMap.values().size()]);
		}
	}

	private class Adapter extends BaseExpandableListAdapter{

		private String mGroupTitle;
		
		public Adapter(String groupTitle){
			mGroupTitle = groupTitle;
		}
		
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mTmpOrder.foods[childPosition];
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			View view;
			if(convertView == null){
				view = View.inflate(getContext(), R.layout.dropchilditem, null);
			}else{
				view = convertView;
			}
			
			final OrderFood food = mTmpOrder.foods[childPosition];
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
			
			String comboStatus = null;
			if(food.isCombo()){
				comboStatus = "(套)";
			}else{
				comboStatus = "";
			}
			
			((TextView) view.findViewById(R.id.foodname)).setText(comboStatus + tempStatus + hangStatus + hurriedStatus + food.name + status);
			//show the order amount to each food
			((TextView) view.findViewById(R.id.accountvalue)).setText(Util.float2String2(food.getCount()));
			//show the price to each food
			((TextView) view.findViewById(R.id.pricevalue)).setText(Util.CURRENCY_SIGN + Util.float2String2(food.calcPriceWithTaste()));
			//show the taste to each food
			((TextView)view.findViewById(R.id.taste)).setText(food.getTastePref());
			/**
			 * "新点菜"的ListView显示"删菜"和"口味"
			 * "已点菜"的ListView显示"退菜"和"催菜"
			 */
			if(mType == Type.INSERT_ORDER){
				//"删菜"操作			 
				ImageView delFoodImgView = (ImageView)view.findViewById(R.id.deletefood);
				delFoodImgView.setBackgroundResource(R.drawable.delete_selector);
				delFoodImgView.setOnClickListener(new View.OnClickListener() {
					/**
					 * "新点菜"时直接显示删菜数量Dialog
					 */
					@Override
					public void onClick(View v) {
						new AskCancelAmountDialog(mTmpOrder.foods[childPosition]).show();
					}
				});

				//"数量"操作
				ImageView addTasteImgView = (ImageView)view.findViewById(R.id.addtaste);
				addTasteImgView.setBackgroundResource(R.drawable.amount_selector);
				addTasteImgView.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						mSelectedPos = childPosition;
						new AskOrderAmountDialog(mTmpOrder.foods[childPosition]).show();
					}
				});
				
			}else{
				//"退菜"操作
				ImageView cancelFoodImgView = (ImageView)view.findViewById(R.id.deletefood);
				cancelFoodImgView.setBackgroundResource(R.drawable.tuicai_selector);
				cancelFoodImgView.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						if(WirelessOrder.restaurant.pwd5 != null){
							/**
							 * 提示退菜权限密码，验证通过的情况下显示删菜数量Dialog
							 */
							new AskPwdDialog(getContext(), AskPwdDialog.PWD_5){
								@Override
								protected void onPwdPass(Context context){
									dismiss();
									new AskCancelAmountDialog(mTmpOrder.foods[childPosition]).show();
								}
							}.show();
						}else{
							new AskCancelAmountDialog(mTmpOrder.foods[childPosition]).show();
						}
					}
				});
				//"催菜"操作
				ImageView addTasteImgView = (ImageView)view.findViewById(R.id.addtaste);
				addTasteImgView.setBackgroundResource(R.drawable.cuicai_selector);
				addTasteImgView.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						if(food.isHurried){
							food.isHurried = false;
							Toast.makeText(getContext(), "取消催菜成功", Toast.LENGTH_SHORT).show();
							mAdapter.notifyDataSetChanged();
				
						}else{
							food.isHurried = true;
							Toast.makeText(getContext(), "催菜成功", Toast.LENGTH_SHORT).show();	
							mAdapter.notifyDataSetChanged();
						}			
					}
				}); 
			}
			return view;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mTmpOrder.foods.length;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mGroupTitle;
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
			View view;
			if(convertView == null){
				view = View.inflate(getContext(), R.layout.dropgrounpitem, null);
			}else{
				view = convertView;
			}
			
			((TextView)view.findViewById(R.id.grounname)).setText(mGroupTitle);
			
			/**
			 * "新点菜"的Group显示"点菜"Button
			 */
			if(mType == Type.INSERT_ORDER){
				/**
				 * 点击点菜按钮
				 */
				ImageView orderImg = (ImageView)view.findViewById(R.id.orderimage);
				orderImg.setBackgroundResource(R.drawable.order_selector);
				
				orderImg.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						if(mOperListener != null){
							mOperListener.onPickFood();
						}
					}
				});
				/**
				 * 点击全单叫起按钮
				 */
				ImageView hurriedImgView = (ImageView)view.findViewById(R.id.operateimage);
				hurriedImgView.setBackgroundResource(R.drawable.jiaoqi_selector);
				
				hurriedImgView.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {						
						if(mTmpOrder.foods.length > 0){
							new AlertDialog.Builder(getContext())
								.setTitle("提示")
								.setMessage("确定全单叫起吗?")
								.setNeutralButton("确定", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,	int which){
											for(int i = 0; i < mTmpOrder.foods.length; i++){
												OrderFood food = mTmpOrder.foods[i];
												if(food.hangStatus == OrderFood.FOOD_NORMAL){
													food.hangStatus = OrderFood.FOOD_HANG_UP;
												}							
											}
											mAdapter.notifyDataSetChanged();
										}
									})
									.setNegativeButton("取消", null)
									.show();	
						}						
					}
				});
				
			}else{
				boolean hasHangupFood = false;
				for(int i = 0; i < mTmpOrder.foods.length; i++){
					if(mTmpOrder.foods[i].hangStatus == OrderFood.FOOD_HANG_UP){
						hasHangupFood = true;
						break;
					}
				}
				
				if(hasHangupFood){
					/**
					 * 点击全单即起按钮
					 */
					ImageView immediateImgView = (ImageView)view.findViewById(R.id.orderimage);
					immediateImgView.setVisibility(View.VISIBLE);
					immediateImgView.setBackgroundResource(R.drawable.jiqi_selector);
					immediateImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {						
							if(mTmpOrder.foods.length > 0){
								new AlertDialog.Builder(getContext())
								.setTitle("提示")
								.setMessage("确定全单即起吗?")
								.setNeutralButton("确定", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,	int which){
											for(int i = 0; i < mTmpOrder.foods.length; i++){
												OrderFood food = mTmpOrder.foods[i];
												if(food.hangStatus == OrderFood.FOOD_HANG_UP){
													food.hangStatus = OrderFood.FOOD_IMMEDIATE;
												}								
											}
											mAdapter.notifyDataSetChanged();
										}
									})
									.setNegativeButton("取消", null)
									.show();	
							}
						}
					});
				}else{
					/**
					 * 如果没有叫起的菜品则不显示叫起Button
					 */
					((ImageView)view.findViewById(R.id.orderimage)).setVisibility(View.INVISIBLE);
				}
			}
			
			if(isExpanded){
				((ImageView)view.findViewById(R.id.arrow)).setBackgroundResource(R.drawable.point);
			}else{
				((ImageView)view.findViewById(R.id.arrow)).setBackgroundResource(R.drawable.point02);
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
			super(OrderFoodListView.this.getContext(), R.style.FullHeightDialog);
			
			View view = LayoutInflater.from(getContext()).inflate(R.layout.alert, null);
			setContentView(view);
			//getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView)view.findViewById(R.id.ordername)).setText("请输入" + selectedFood.name + "的删除数量");
			
			((TextView)findViewById(R.id.table)).setText("数量：");
			//删除数量默认为此菜品的点菜数量
			final EditText cancelEdtTxt = (EditText)view.findViewById(R.id.mycount);			
			cancelEdtTxt.setText(Util.float2String2(selectedFood.getCount()));
			//弹出后全选
			cancelEdtTxt.selectAll();
			
			cancelEdtTxt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cancelEdtTxt.selectAll();
				}
			});
			
			//弹出软键盘
           getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); 
           InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
           imm.showSoftInput(cancelEdtTxt, 0); //显示软键盘
           imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			
			//"确定"Button
			Button okBtn = (Button)view.findViewById(R.id.confirm);
			okBtn.setText("确定");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try{
						float foodAmount = selectedFood.getCount();
						float cancelAmount = Float.parseFloat(cancelEdtTxt.getText().toString());
						
						if(foodAmount == cancelAmount){
							/**
							 * 如果数量相等，则从列表中删除此菜
							 */
							mTmpOrder.remove(selectedFood);
							mAdapter.notifyDataSetChanged();
							dismiss();
							Toast.makeText(getContext(), "删除\"" + selectedFood.toString() + "\"" + cancelAmount + "份成功", Toast.LENGTH_LONG).show();
							
						}else if(foodAmount > cancelAmount){
							/**
							 * 如果删除数量少于已点数量，则相应减去删除数量
							 */
							selectedFood.setCount(foodAmount - cancelAmount);
							mAdapter.notifyDataSetChanged();
							dismiss();
							Toast.makeText(getContext(), "删除\"" + selectedFood.toString() + "\"" + cancelAmount + "份成功", Toast.LENGTH_LONG).show();
							
						}else{
							Toast.makeText(getContext(), "输入的删除数量大于已点数量, 请重新输入", Toast.LENGTH_LONG).show();
						}
						
					}catch(NumberFormatException e){
						Toast.makeText(getContext(), "你输入删菜数量不正确", Toast.LENGTH_LONG).show();
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
	 * 提示输入数量的Dialog
	 */
	private class AskOrderAmountDialog extends Dialog{
	
		AskOrderAmountDialog(final OrderFood selectedFood) {
			super(OrderFoodListView.this.getContext(), R.style.FullHeightDialog);
			
			View view = LayoutInflater.from(getContext()).inflate(R.layout.alert, null);
			setContentView(view);
			//getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView)view.findViewById(R.id.ordername)).setText("请输入" + selectedFood.name + "的数量");
			
			((TextView)findViewById(R.id.table)).setText("数量：");
			//增加数量默认为此菜品的点菜数量
			final EditText amountEdtTxt = (EditText)view.findViewById(R.id.mycount);
			amountEdtTxt.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					amountEdtTxt.selectAll();
				}
			});
			
			//"确定"Button
			Button okBtn = (Button)view.findViewById(R.id.confirm);
			okBtn.setText("确定");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try{
						float amount = Float.parseFloat(amountEdtTxt.getText().toString());
						if(amount > 255){
							Toast.makeText(getContext(), "对不起，\"" + selectedFood.toString() + "\"最多只能点255份", Toast.LENGTH_SHORT).show();
						}else{
							selectedFood.setCount(amount);
							mAdapter.notifyDataSetChanged();
							Toast.makeText(getContext(), "设置\"" + selectedFood.toString() + "\"" + "数量为" + amount + "份", Toast.LENGTH_LONG).show();
							dismiss();
						}							
						
					}catch(NumberFormatException e){
						Toast.makeText(getContext(), "您输入的数量格式不正确，请重新输入", Toast.LENGTH_SHORT).show();
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
			
			//弹出软键盘
	           getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); 
	           InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	           imm.showSoftInput(amountEdtTxt, 0); //显示软键盘
	           imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		}		
	}
	
	/**
	 * 点击菜品列表后的扩展功能 Dialog
	 */
	private class ExtOperDialg extends Dialog{

		
		ExtOperDialg(final OrderFood selectedFood) {
			super(OrderFoodListView.this.getContext(), R.style.FullHeightDialog);
			setContentView(R.layout.item_alert);
			getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView)findViewById(R.id.ordername)).setText("请选择" + selectedFood.name + "的操作");
			if(mType == Type.INSERT_ORDER){
				/**
				 * 新点菜是扩展功能为"删菜"、"口味"、"叫起/取消叫起"、“数量”
				 */
				//删菜功能
				((TextView)findViewById(R.id.item1Txt)).setText("删菜");
				((RelativeLayout)findViewById(R.id.r1)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						dismiss();
						new AskCancelAmountDialog(selectedFood).show();							
					}
				});
				
				//口味功能
				((TextView)findViewById(R.id.item2Txt)).setText("口味");
				((RelativeLayout)findViewById(R.id.r2)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						if(mOperListener != null){
							dismiss();
							mOperListener.onPickTaste(selectedFood);
						}
					}
				});
				
				//叫起/取消叫起
				if(selectedFood.hangStatus == OrderFood.FOOD_NORMAL){
					((TextView)findViewById(R.id.item3Txt)).setText("叫起");						
				}else{
					((TextView)findViewById(R.id.item3Txt)).setText("取消叫起");
				}
				((RelativeLayout)findViewById(R.id.r3)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						if(selectedFood.hangStatus == OrderFood.FOOD_NORMAL){
							selectedFood.hangStatus = OrderFood.FOOD_HANG_UP;
							((TextView)findViewById(R.id.item3Txt)).setText("取消叫起");
							dismiss();
						}else{
							selectedFood.hangStatus = OrderFood.FOOD_NORMAL;
							((TextView)findViewById(R.id.item3Txt)).setText("叫起");		
							dismiss();
						}
					}
				});
				
				//数量
				((RelativeLayout)findViewById(R.id.r4)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						dismiss();
						new AskOrderAmountDialog(selectedFood).show();							
					}
				});
				
				
			}else{
				/**
				 * 已点菜的扩展功能为"退菜"、"即起"、"催菜/取消催菜"
				 */
				//退菜功能
				((TextView)findViewById(R.id.item1Txt)).setText("退菜");
				((RelativeLayout)findViewById(R.id.r1)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						dismiss();
						if(WirelessOrder.restaurant.pwd5 != null){
							new AskPwdDialog(getContext(), AskPwdDialog.PWD_5){							
								@Override
								protected void onPwdPass(Context context){
									dismiss();
									new AskCancelAmountDialog(selectedFood).show();
								}
							}.show();
						}else{
							new AskCancelAmountDialog(selectedFood).show(); 
						}
					}
				});
				
				//如果菜品是叫起状态，显示"即起"功能
				if(selectedFood.hangStatus == OrderFood.FOOD_HANG_UP || selectedFood.hangStatus == OrderFood.FOOD_IMMEDIATE){
					if(selectedFood.hangStatus == OrderFood.FOOD_HANG_UP){
						((TextView)findViewById(R.id.item2Txt)).setText("即起");						
					}else{
						((TextView)findViewById(R.id.item2Txt)).setText("重新叫起");
					}
					((RelativeLayout)findViewById(R.id.r2)).setOnClickListener(new View.OnClickListener() {						
						@Override
						public void onClick(View arg0) {
			    			if(selectedFood.hangStatus == OrderFood.FOOD_HANG_UP){
			    				selectedFood.hangStatus = OrderFood.FOOD_IMMEDIATE;
								((TextView)findViewById(R.id.item2Txt)).setText("即起");
								dismiss();
			    				
			    			}else if(selectedFood.hangStatus == OrderFood.FOOD_IMMEDIATE){
			    				selectedFood.hangStatus = OrderFood.FOOD_HANG_UP;
								((TextView)findViewById(R.id.item2Txt)).setText("重新叫起");
								dismiss();
			    			}						
						}
					});
					
					//催菜/取消催菜功能
					if(selectedFood.isHurried){
						((TextView)findViewById(R.id.item3Txt)).setText("取消催菜");							
					}else{
						((TextView)findViewById(R.id.item3Txt)).setText("催菜");						
					}
					((RelativeLayout)findViewById(R.id.r3)).setOnClickListener(new View.OnClickListener() {						
						@Override
						public void onClick(View arg0) {
							if(selectedFood.isHurried){
								selectedFood.isHurried = false;
								((TextView)findViewById(R.id.item3Txt)).setText("催菜");	
								dismiss();
								
							}else{
								selectedFood.isHurried = true;
								((TextView)findViewById(R.id.item3Txt)).setText("取消催菜");	
								dismiss();
							}
						}
					});
					
				}else{
					
					//催菜/取消催菜功能
					if(selectedFood.isHurried){
						((TextView)findViewById(R.id.item2Txt)).setText("取消催菜");							
					}else{
						((TextView)findViewById(R.id.item2Txt)).setText("催菜");						
					}
					((RelativeLayout)findViewById(R.id.r2)).setOnClickListener(new View.OnClickListener() {						
						@Override
						public void onClick(View arg0) {
							if(selectedFood.isHurried){
								selectedFood.isHurried = false;
								((TextView)findViewById(R.id.item2Txt)).setText("催菜");	
								dismiss();
					
							}else{
								selectedFood.isHurried = true;
								((TextView)findViewById(R.id.item2Txt)).setText("取消催菜");	
								dismiss();
							}						
						}
					});
					
					((ImageView)findViewById(R.id.line3)).setVisibility(View.GONE);
					((ImageView)findViewById(R.id.line4)).setVisibility(View.GONE);
					((RelativeLayout)findViewById(R.id.r3)).setVisibility(View.GONE);
					//数量
					((RelativeLayout)findViewById(R.id.r4)).setVisibility(View.GONE);
				}					
			}
			
			//返回Button
			Button cancelBtn = (Button)findViewById(R.id.back);		
			cancelBtn.setOnClickListener(new View.OnClickListener() {					
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}
		
		@Override
		protected void onStop(){
			//trim(_selectedFood);
			mAdapter.notifyDataSetChanged();
		}		
	}
	
	public static interface OnChangedListener{
		public void onSourceChanged();
	}
	
	public static interface OnOperListener{
		public void onPickTaste(OrderFood selectedFood);
		public void onPickFood();
	}
	
}
