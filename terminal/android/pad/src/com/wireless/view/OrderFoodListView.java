package com.wireless.view;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.dialog.AskPwdDialog;
import com.wireless.pack.Type;
import com.wireless.pad.R;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.tasteMgr.TasteGroup;
import com.wireless.pojo.util.NumericUtil;

public class OrderFoodListView extends ExpandableListView {

	public final static int PICK_TASTE = 1;
	public final static int PICK_FOOD = 2;

	private OnOperListener _operListener;
	private OnChangedListener _chgListener;
	private List<OrderFood> _foods = new ArrayList<OrderFood>();
	private int _selectedPos;
	private byte _type = Type.INSERT_ORDER;
	private BaseExpandableListAdapter _adapter;

	public OrderFoodListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	/**
	 * 设置ListView的类型，目前分为"新点菜"和"已点菜"两种
	 * 
	 * @param type
	 *            One of values blew.<br>
	 *            Type.INSERT_ORDER - 新点菜 Type.UPDATE_ORDER - 已点菜
	 */
	public void setType(int type) {
		if (type == Type.INSERT_ORDER) {
			_type = Type.INSERT_ORDER;
		} else if (type == Type.UPDATE_ORDER) {
			_type = Type.UPDATE_ORDER;
		} else {
			_type = Type.INSERT_ORDER;
		}
	}

	/**
	 * 设置菜品操作的回调接口
	 * 
	 * @param operListener
	 */
	public void setOperListener(OnOperListener operListener) {
		_operListener = operListener;
	}

	/**
	 * 设置数据源变化的回调接口
	 * 
	 * @param chgListener
	 */
	public void setChangedListener(OnChangedListener chgListener) {
		_chgListener = chgListener;
	}

	/**
	 * 取得List中的数据源（就是菜品的List信息）
	 * 
	 * @return OrderFood的List
	 */
	public List<OrderFood> getSourceData() {
		return _foods;
	}

	/**
	 * 在source data变化的时候，调用此函数来更新ListView的数据。
	 */
	public void notifyDataChanged() {
		if (_adapter != null) {
			_adapter.notifyDataSetChanged();
		}
	}

	/**
	 * 添加多个菜品到已经菜的List中
	 * 
	 * @param foods
	 *            新点菜品
	 */
	public void addFoods(List<OrderFood> foods) {
		for (OrderFood food : foods) {
			addFood(food);
		}
	}

	/**
	 * 添加菜品到已点菜的List中
	 * 
	 * @param food
	 *            选中的菜品信息
	 */
	private void addFood(OrderFood food) {

		int index = _foods.indexOf(food);

		if (index != -1) {
			/**
			 * 如果原来的菜品列表中已包含有相同的菜品， 则将新点菜的数量累加到原来的菜品中， 否则就作为新点菜品添加到菜品列表中
			 */
			OrderFood pickedFood = _foods.get(index);

			float orderAmount = food.getCount() + pickedFood.getCount();
			if (orderAmount > 255) {
				Toast.makeText(getContext(), "对不起，\"" + food.toString() + "\"最多只能点255份", Toast.LENGTH_SHORT).show();
				// pickedFood.setCount(new Float(255));
			} else {
				Toast.makeText(getContext(), 
							  "添加" + (food.isHangup() ? "并叫起\"" : "\"") + 
							  food.toString() + "\"" + NumericUtil.float2String2(food.getCount()) + "份", Toast.LENGTH_SHORT).show();
				pickedFood.setCount(orderAmount);
				_foods.set(index, pickedFood);
				// 刷新菜品列表
				notifyDataChanged();
			}
		} else {
			if (food.getCount() > 255) {
				Toast.makeText(getContext(), "对不起，\"" + food.toString() + "\"最多只能点255份", Toast.LENGTH_SHORT).show();

			} else {
				Toast.makeText(getContext(),
							   "新增" + (food.isHangup() ? "并叫起\"" : "\"") + 
							   food.toString() + "\"" + NumericUtil.float2String2(food.getCount()) + "份", Toast.LENGTH_SHORT).show();
				_foods.add(food);
				// 刷新菜品列表
				notifyDataChanged();

			}
		}
	}

	/**
	 * 函数用于更新ListView的source data，第一次调用此函数的时候，会中同时创建相应的Adapter。
	 * 
	 * @param foods
	 *            在ListView上显示的菜品数据
	 */
	public void notifyDataChanged(List<OrderFood> foods) {
		if (foods != null) {
			_foods = foods;
			trim();
			if (_adapter != null) {
				_adapter.notifyDataSetChanged();
			} else {
				if (_type == Type.INSERT_ORDER) {
					_adapter = new Adapter("新点菜") {
						@Override
						public void notifyDataSetChanged() {
							trim();
							super.notifyDataSetChanged();
							if (_chgListener != null) {
								_chgListener.onSourceChanged();
							}
						}
					};
				} else {
					_adapter = new Adapter("已点菜") {
						@Override
						public void notifyDataSetChanged() {
							trim();
							super.notifyDataSetChanged();
							if (_chgListener != null) {
								_chgListener.onSourceChanged();
							}
						}
					};
				}
				setAdapter(_adapter);
				if (_chgListener != null) {
					_chgListener.onSourceChanged();
				}
			}

		} else {
			throw new NullPointerException();
		}
	}

	/**
	 * 此函数用于更新选中的菜品，比如"口味"操作，需要从TasteActivity将结果回传ListView， 这种情况就调用此函数来更新选中的菜品
	 * 
	 * @param food
	 */
	public void notifyDataChanged(OrderFood food) {
		if (food != null && _adapter != null) {
			_foods.set(_selectedPos, food);
			// trim(food);
			_adapter.notifyDataSetChanged();

		} else {
			throw new NullPointerException();
		}
	}

	private void trim() {
		Iterator<OrderFood> iter = _foods.iterator();
		HashMap<OrderFood, Integer> foodMap = new HashMap<OrderFood, Integer>();
		while (iter.hasNext()) {
			OrderFood food = iter.next();
			if (foodMap.containsKey(food)) {
				int amount = foodMap.get(food).intValue()
						+ NumericUtil.float2Int(food.getCount());
				foodMap.put(food, amount);
			} else {
				foodMap.put(food, NumericUtil.float2Int(food.getCount()));
			}
		}
		if (_foods.size() != foodMap.size()) {
			_foods.clear();
			Iterator<Map.Entry<OrderFood, Integer>> iter2 = foodMap.entrySet()
					.iterator();
			while (iter2.hasNext()) {
				Map.Entry<OrderFood, Integer> entry = iter2.next();
				OrderFood food = entry.getKey();
				food.setCount(NumericUtil.int2Float(entry.getValue().intValue()));
				_foods.add(food);
			}
		}
	}

	public class Adapter extends BaseExpandableListAdapter {

		private String _groupTitle;

		public Adapter(String groupTitle) {
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
		public View getChildView(int groupPosition, final int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			View view = View.inflate(getContext(), R.layout.dropchilditem, null);
			final OrderFood food = _foods.get(childPosition);
			// show the name to each food
			String status = "";
			if (food.asFood().isSpecial()) {
				status = "特";
			}
			if (food.asFood().isRecommend()) {
				if (status.length() == 0) {
					status = "荐";
				} else {
					status = status + ",荐";
				}
			}
			if (food.asFood().isGift()) {
				if (status.length() == 0) {
					status = "赠";
				} else {
					status = status + ",赠";
				}
			}
			if (status.length() != 0) {
				status = "(" + status + ")";
			}

			String tempStatus = null;
			if (food.isTemp()) {
				tempStatus = "(临)";
			} else {
				tempStatus = "";
			}

			String hangStatus = null;
			if (food.isHangup()) {
				hangStatus = "叫";
			} else {
				hangStatus = "";
			}
			if (hangStatus.length() != 0) {
				hangStatus = "(" + hangStatus + ")";
			}

			String hurriedStatus = null;
			if (food.isHurried()) {
				hurriedStatus = "(催)";
			} else {
				hurriedStatus = "";
			}
			((TextView) view.findViewById(R.id.foodname)).setText(tempStatus
					+ hangStatus + hurriedStatus + food.getName() + status);
			// show the order amount to each food
			((TextView) view.findViewById(R.id.accountvalue)).setText(NumericUtil
					.float2String2(food.getCount()));
			// show the price to each food
			((TextView) view.findViewById(R.id.pricevalue))
					.setText(NumericUtil.CURRENCY_SIGN
							+ NumericUtil.float2String2(food.calcPriceWithTaste()));
			// show the taste to each food
			if(food.hasTaste()){
				((TextView) view.findViewById(R.id.taste)).setText(food.getTasteGroup().getTastePref());
			}else{
				((TextView) view.findViewById(R.id.taste)).setText(TasteGroup.NO_TASTE_PREF);
			}
			/**
			 * "新点菜"的ListView显示"删菜"和"口味" "已点菜"的ListView显示"退菜"和"催菜"
			 */
			if (_type == Type.INSERT_ORDER) {

				// 新点菜中"+"操作
				((ImageView) view.findViewById(R.id.add2))
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								OrderFood selectedFood = _foods.get(childPosition);
								float amount = selectedFood.getCount() + 1;
								if(amount <= 255){
									selectedFood.setCount(selectedFood.getCount() + 1);
								}else{
									Toast.makeText(getContext(), selectedFood.getName() + "只能最多只能点255份", Toast.LENGTH_SHORT).show();
								}
								_adapter.notifyDataSetChanged();
							}
						});
				
				//新点菜中"-"操作
				((ImageView) view.findViewById(R.id.jian2)).setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						OrderFood selectedFood = _foods.get(childPosition);
						if (selectedFood.getCount() > 1) {
							selectedFood.setCount(selectedFood.getCount() - 1);
							_adapter.notifyDataSetChanged();
						}else{
							Toast.makeText(getContext(), selectedFood.getName() + "点菜数量不能小于1", Toast.LENGTH_SHORT).show();
						}
					}
				});
				
				// 新点菜中"叫"的操作
				((ImageView) view.findViewById(R.id.jiao)).setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						OrderFood food = _foods.get(childPosition);
						if (food.isHangup()) {
							food.setHangup(false);
							Toast.makeText(getContext(), "取消叫起" + food.getName(), Toast.LENGTH_SHORT).show();
						}else{
							food.setHangup(true);
							Toast.makeText(getContext(), "叫起" + food.getName(), Toast.LENGTH_SHORT).show();
						}
						_adapter.notifyDataSetChanged();
					}
				});
				
				//新点菜中的"删"操作
				ImageView delFoodImgView = (ImageView) view.findViewById(R.id.deletefood);
				delFoodImgView.setBackgroundResource(R.drawable.delete_selector);
				delFoodImgView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						final OrderFood selectedFood = _foods.get(childPosition);

						new AlertDialog.Builder(getContext())
							.setTitle("提示")
							.setMessage("是否确定取消" + selectedFood.getName() + "?")
							.setNeutralButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,	int which){
										_foods.remove(selectedFood);
										Toast.makeText(getContext(), "取消" + food.getName(), Toast.LENGTH_SHORT).show();
										_adapter.notifyDataSetChanged();
									}
								})
							.setNegativeButton("取消", null)
							.show();

					}
				});

				// 新点菜中的"口味"操作
				ImageView addTasteImgView = (ImageView) view.findViewById(R.id.addtaste);
				addTasteImgView.setBackgroundResource(R.drawable.taste_selector);
				addTasteImgView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_selectedPos = childPosition;
						if (_operListener != null) {
							if (_foods.get(childPosition).isTemp()) {
								Toast.makeText(getContext(), "临时菜不能添加口味", Toast.LENGTH_SHORT).show();
							} else {
								_operListener.onPickTaste(_foods.get(childPosition));
							}
						}
					}
				});
				
				
				//新点菜中"="操作
				ImageView equlesImgView = (ImageView) view.findViewById(R.id.equlefood);
				equlesImgView.setBackgroundResource(R.drawable.eques_selector);
				equlesImgView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {	
						_selectedPos = childPosition;
						//new AskOrderAmountDialog(_foods.get(childPosition)).show();
						showOrderAmountDialog(_foods.get(childPosition));
					}
				});
				

			} else {
				// 已点菜的不需要的按钮进行隐藏
				view.findViewById(R.id.add2).setVisibility(View.GONE);
				view.findViewById(R.id.jian2).setVisibility(View.GONE);
				view.findViewById(R.id.equlefood).setVisibility(View.GONE);
				view.findViewById(R.id.jiao).setVisibility(View.GONE);
				
				// 已点菜中"退"的操作
				ImageView cancelFoodImgView = (ImageView) view
						.findViewById(R.id.deletefood);
				cancelFoodImgView
						.setBackgroundResource(R.drawable.tuicai_selector);
				cancelFoodImgView
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (WirelessOrder.restaurant.hasPwd5()) {
									/**
									 * 提示输入权限密码2，验证通过的情况下显示删菜数量Dialog
									 */
									new AskPwdDialog(getContext(), AskPwdDialog.PWD_5) {
										@Override
										protected void onPwdPass(Context context) {
										
											showCancelAmountDialog(_foods.get(childPosition));
											//new AskCancelAmountDialog(_foods.get(childPosition)).show();
										}
									};
								} else {
									//new AskCancelAmountDialog(_foods.get(childPosition)).show();
									showCancelAmountDialog(_foods.get(childPosition));
								}
							}
						});
				
				// 已点菜中"催"的操作
				ImageView addTasteImgView = (ImageView) view
						.findViewById(R.id.addtaste);
				addTasteImgView
						.setBackgroundResource(R.drawable.cuicai_selector);
				addTasteImgView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (food.isHurried()) {
							food.setHurried(false);
							Toast.makeText(getContext(), food.getName() + "取消催菜", Toast.LENGTH_SHORT).show();

						} else {
							food.setHurried(true);
							Toast.makeText(getContext(), food.getName() + "催菜", Toast.LENGTH_SHORT).show();
						}
						_adapter.notifyDataSetChanged();
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
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			View view = View.inflate(getContext(), R.layout.dropgrounpitem, null);
			((TextView) view.findViewById(R.id.grounname)).setText(_groupTitle);

			/**
			 * "新点菜"的Group显示"点菜"Button
			 */
			if (_type == Type.INSERT_ORDER) {
				/**
				 * 点击点菜按钮
				 */
				ImageView orderImg = (ImageView) view
						.findViewById(R.id.orderimage);
				orderImg.setBackgroundResource(R.drawable.order_selector);

				orderImg.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (_operListener != null) {
							_operListener.onPickFood();
						}
					}
				});
				/**
				 * 点击全单叫起按钮
				 */
				ImageView hurriedImgView = (ImageView) view
						.findViewById(R.id.operateimage);
				hurriedImgView
						.setBackgroundResource(R.drawable.jiaoqi_selector);

				hurriedImgView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (_foods.size() > 0) {
							new AlertDialog.Builder(getContext())
									.setTitle("提示")
									.setMessage(_foods.get(0).isHangup() ? "确定全单叫起吗?" : "确定全单取消叫起吗?")
									.setNeutralButton(
											"确定",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog,	int which) {
													for (int i = 0; i < _foods.size(); i++) {
														OrderFood food = _foods.get(i);
														food.toggleHangup();
													}
													_adapter.notifyDataSetChanged();
												}
											}).setNegativeButton("取消", null)
									.show();
						}
					}
				});

			} else {
				view.findViewById(R.id.orderimage).setVisibility(View.INVISIBLE);
			}

			if (isExpanded) {
				((ImageView) view.findViewById(R.id.arrow))
						.setBackgroundResource(R.drawable.point);
			} else {
				((ImageView) view.findViewById(R.id.arrow))
						.setBackgroundResource(R.drawable.point02);
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
	private void showCancelAmountDialog(final OrderFood selectedFood){
		final EditText amountEdtTxt = new EditText(getContext());
		amountEdtTxt.setInputType(InputType.TYPE_CLASS_NUMBER);
		// 删除数量默认为此菜品的点菜数量
		amountEdtTxt.setText(NumericUtil.float2String2(selectedFood.getCount()));
		// 光标置到数量最后面
		amountEdtTxt.setSelection(amountEdtTxt.getText().length());
		
		final AlertDialog cancelAmtDialog = new AlertDialog.Builder(getContext())
			.setTitle("请输入"+ selectedFood.getName() + "的删除数量")
			.setView(amountEdtTxt)
			.setNeutralButton("确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,	int which){					
						
					}
				})
			.setNegativeButton("取消", null)
			.create();
		
		cancelAmtDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				cancelAmtDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						float foodAmount = selectedFood.getCount();
						try{
							float cancelAmount = Float.parseFloat(amountEdtTxt.getText().toString());
	
							if (foodAmount == cancelAmount) {
								/**
								 * 如果数量相等，则从列表中删除此菜
								 */
								_foods.remove(selectedFood);
								_adapter.notifyDataSetChanged();
								Toast.makeText(getContext(), "删除\"" + selectedFood.toString() + "\""	+ cancelAmount + "份成功", Toast.LENGTH_SHORT).show();
								cancelAmtDialog.dismiss();
								
							} else if (foodAmount > cancelAmount) {
								/**
								 * 如果删除数量少于已点数量，则相应减去删除数量
								 */
								selectedFood.setCount(foodAmount - cancelAmount);
								_adapter.notifyDataSetChanged();
								Toast.makeText(getContext(), "删除\"" + selectedFood.toString() + "\"" + cancelAmount + "份成功", Toast.LENGTH_SHORT).show();
								cancelAmtDialog.dismiss();
	
							} else {
								new AlertDialog.Builder(getContext()).setTitle("提示")
									.setMessage("你输入的删除数量大于已点数量, 请重新输入")
									.setNeutralButton("确定", null)
									.show();
							}							
						}catch(NumberFormatException e){
							Toast.makeText(getContext(), "你输入的删除数量不正确, 请重新输入", Toast.LENGTH_SHORT).show();							
						}
					}
				});
			}
		});
		
		cancelAmtDialog.show();
		
	}
	
	/**
	 * 提示输入点菜数量的Dialog
	 * @author Ying.Zhang
	 *
	 */
//	private class AskOrderAmountDialog extends Dialog{
//
//		
//		public AskOrderAmountDialog(final OrderFood selectedFood) {
//			super(OrderFoodListView.this.getContext(), R.style.FullHeightDialog);			
//			
//			setContentView(R.layout.order_confirm);
//			
//			findViewById(R.id.orderHurriedChk).setVisibility(View.GONE);
//			
//			((TextView)findViewById(R.id.orderTitleTxt)).setText("请输入" + selectedFood.name + "的点菜数量");
//
//			EditText amountEdtTxt = ((EditText)findViewById(R.id.amountEdtTxt));
//			amountEdtTxt.setText("");
//
//			
//			//"确定"Button
//			Button okBtn = (Button)findViewById(R.id.orderConfirmBtn);
//			okBtn.setText("确定");
//			okBtn.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {	
//					try{
//						float orderAmount = Float.parseFloat(((EditText)findViewById(R.id.amountEdtTxt)).getText().toString());
//						
//		       			if(orderAmount > 255){
//		       				Toast.makeText(getContext(), "对不起，\"" + selectedFood.toString() + "\"最多只能点255份", 0).show();
//		       			}else{
//		       				selectedFood.setCount(orderAmount);
//		       				_adapter.notifyDataSetChanged();
//							dismiss();
//		       			}
//					
//					}catch(NumberFormatException e){
//						Toast.makeText(getContext(), "您输入的数量格式不正确，请重新输入", 0).show();
//					}
//				}
//			});
//			
//			//"取消"Button
//			Button cancelBtn = (Button)findViewById(R.id.orderCancelBtn);
//			cancelBtn.setText("取消");
//			cancelBtn.setOnClickListener(new View.OnClickListener(){
//				@Override
//				public void onClick(View v) {
//					dismiss();
//				}
//			});
//
//		}		
//	}
	
	
	/**
	 * 提示输入点菜数量的Dialog
	 * @author FaRong.Zhang
	 *
	 */
	public void showOrderAmountDialog(final OrderFood selectedFood){
		
		final EditText orderAmtEdtTxt = new EditText(getContext());

		orderAmtEdtTxt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		
		final AlertDialog orderAmtDialog = new AlertDialog.Builder(getContext())
			.setTitle("请输入" + selectedFood.getName() + "的点菜数量")
			.setView(orderAmtEdtTxt)
			.setNeutralButton("确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,	int which){

					}
				})
			.setNegativeButton("取消", null)
			.create();
		
		orderAmtDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				orderAmtDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						try{
							float orderAmount = Float.parseFloat(orderAmtEdtTxt.getText().toString());
							
			       			if(orderAmount > 255){
			       				Toast.makeText(getContext(), "对不起，\"" + selectedFood.toString() + "\"最多只能点255份", Toast.LENGTH_SHORT).show();
			       			}else{
			       				selectedFood.setCount(orderAmount);
			       				_adapter.notifyDataSetChanged();
								orderAmtDialog.dismiss();
			       			}
						
						}catch(NumberFormatException e){
							Toast.makeText(getContext(), "您输入的数量格式不正确，请重新输入", Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		});
		
		orderAmtDialog.show();
	}
	
	
	public static interface OnChangedListener {
		public void onSourceChanged();
	}

	public static interface OnOperListener {
		public void onPickTaste(OrderFood selectedFood);

		public void onPickFood();
	}

}
