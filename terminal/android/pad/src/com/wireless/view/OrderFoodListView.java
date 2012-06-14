package com.wireless.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.util.AttributeSet;
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

import com.wireless.common.WirelessOrder;
import com.wireless.dialog.AskPwdDialog;
import com.wireless.pad.R;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;

public class OrderFoodListView extends ExpandableListView {

	public final static int PICK_TASTE = 1;
	public final static int PICK_FOOD = 2;

	private OnOperListener _operListener;
	private OnChangedListener _chgListener;
	private Context _context;
	private List<OrderFood> _foods = new ArrayList<OrderFood>();
	private int _selectedPos;
	private byte _type = Type.INSERT_ORDER;
	private BaseExpandableListAdapter _adapter;

	public OrderFoodListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		_context = context;
		/**
		 * 选择每个菜品的操作
		 */
		// setOnChildClickListener(new OnChildClickListener() {
		// @Override
		// public boolean onChildClick(ExpandableListView parent, View v,
		// int groupPosition, int childPosition, long id) {
		// if (_type == Type.INSERT_ORDER) {
		// _selectedPos = childPosition;
		// new ExtOperDialg(_foods.get(childPosition)).show();
		// return true;
		//
		// } else if (_type == Type.UPDATE_ORDER) {
		// _selectedPos = childPosition;
		// new ExtOperDialg(_foods.get(childPosition)).show();
		// return true;
		//
		// } else {
		// return false;
		// }
		// }
		// });
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
	public void addFoods(OrderFood[] foods) {
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
				Toast.makeText(_context,
						"对不起，\"" + food.toString() + "\"最多只能点255份", 0).show();
				// pickedFood.setCount(new Float(255));
			} else {
				Toast.makeText(
						_context,
						"添加"
								+ (food.hangStatus == OrderFood.FOOD_HANG_UP ? "并叫起\""
										: "\"") + food.toString() + "\""
								+ Util.float2String2(food.getCount()) + "份", 0)
						.show();
				pickedFood.setCount(orderAmount);
				_foods.set(index, pickedFood);
				// 刷新菜品列表
				notifyDataChanged();
			}
		} else {
			if (food.getCount() > 255) {
				Toast.makeText(_context,
						"对不起，\"" + food.toString() + "\"最多只能点255份", 0).show();

			} else {
				Toast.makeText(
						_context,
						"新增"
								+ (food.hangStatus == OrderFood.FOOD_HANG_UP ? "并叫起\""
										: "\"") + food.toString() + "\""
								+ Util.float2String2(food.getCount()) + "份", 0)
						.show();
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
						+ Util.float2Int(food.getCount());
				foodMap.put(food, amount);
			} else {
				foodMap.put(food, Util.float2Int(food.getCount()));
			}
		}
		if (_foods.size() != foodMap.size()) {
			_foods.clear();
			Iterator<Map.Entry<OrderFood, Integer>> iter2 = foodMap.entrySet()
					.iterator();
			while (iter2.hasNext()) {
				Map.Entry<OrderFood, Integer> entry = iter2.next();
				OrderFood food = entry.getKey();
				food.setCount(Util.int2Float(entry.getValue().intValue()));
				_foods.add(food);
			}
		}
	}

	// private void trim(OrderFood food){
	// /**
	// * Keep track of the first food and position in the list that matched the
	// food modified.
	// * Combine the food if there is the same food exist in the following
	// position.
	// */
	// int pos = -1;
	// OrderFood firstFood = null;
	// int firstPos = -1;
	// int srchPos = 0;
	// int nCount = 0;
	// List<OrderFood> tmpFoods = _foods.subList(0, _foods.size());
	// while((pos = tmpFoods.indexOf(food)) != -1){
	// nCount++;
	// if(nCount == 1){
	// firstFood = tmpFoods.get(pos);
	// firstPos = pos;
	// }else{
	// int count = Util.float2Int(firstFood.getCount()) +
	// Util.float2Int(tmpFoods.get(pos).getCount());
	// if((count / 100) > 255){
	// Toast.makeText(_context, "对不起，\"" + firstFood.toString() + "\"最多只能点255份",
	// 0).show();
	// firstFood.setCount(new Float(255));
	// }else{
	// firstFood.setCount(Util.int2Float(count));
	// }
	// _foods.set(firstPos, firstFood);
	// _foods.remove(srchPos + pos);
	// }
	// srchPos += pos + 1;
	// if(srchPos >= _foods.size()){
	// break;
	// }else{
	// tmpFoods = _foods.subList(srchPos, _foods.size());
	// }
	// }
	// }

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
			View view = View.inflate(_context, R.layout.dropchilditem, null);
			final OrderFood food = _foods.get(childPosition);
			// show the name to each food
			String status = "";
			if (food.isSpecial()) {
				status = "特";
			}
			if (food.isRecommend()) {
				if (status.length() == 0) {
					status = "荐";
				} else {
					status = status + ",荐";
				}
			}
			if (food.isGift()) {
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
			if (food.isTemporary) {
				tempStatus = "(临)";
			} else {
				tempStatus = "";
			}

			String hangStatus = null;
			if (food.hangStatus == OrderFood.FOOD_HANG_UP) {
				hangStatus = "叫";
			} else if (food.hangStatus == OrderFood.FOOD_IMMEDIATE) {
				hangStatus = "即";
			} else {
				hangStatus = "";
			}
			if (hangStatus.length() != 0) {
				hangStatus = "(" + hangStatus + ")";
			}

			String hurriedStatus = null;
			if (food.isHurried) {
				hurriedStatus = "(催)";
			} else {
				hurriedStatus = "";
			}
			((TextView) view.findViewById(R.id.foodname)).setText(tempStatus
					+ hangStatus + hurriedStatus + food.name + status);
			// show the order amount to each food
			((TextView) view.findViewById(R.id.accountvalue)).setText(Util
					.float2String2(food.getCount()));
			// show the price to each food
			((TextView) view.findViewById(R.id.pricevalue))
					.setText(Util.CURRENCY_SIGN
							+ Util.float2String2(food.calcPriceWithTaste()));
			// show the taste to each food
			((TextView) view.findViewById(R.id.taste)).setText(food.getTastePref());
			/**
			 * "新点菜"的ListView显示"删菜"和"口味" "已点菜"的ListView显示"退菜"和"催菜"
			 */
			if (_type == Type.INSERT_ORDER) {
				// "删菜"操作

				// 新点菜中叫的操作
				((ImageView) view.findViewById(R.id.jiao))
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								OrderFood food = _foods.get(childPosition);
								if (food.hangStatus == OrderFood.FOOD_NORMAL) {
									food.hangStatus = OrderFood.FOOD_HANG_UP;
								}
								_adapter.notifyDataSetChanged();
							}
						});

				// 新点菜
				((ImageView) view.findViewById(R.id.add2))
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								OrderFood selectedFood = _foods
										.get(childPosition);
								selectedFood.setCount(selectedFood.getCount() + 1.0f);
								final TextView counttxt = (TextView) findViewById(R.id.accountvalue);
								counttxt.setText((Util
										.float2String2(selectedFood.getCount())));
							}
						});
				ImageView delFoodImgView = (ImageView) view
						.findViewById(R.id.deletefood);
				delFoodImgView
						.setBackgroundResource(R.drawable.delete_selector);
				delFoodImgView.setOnClickListener(new View.OnClickListener() {
					/**
					 * "新点菜"时直接显示删菜数量Dialog
					 */
					@Override
					public void onClick(View v) {

						OrderFood selectedFood = _foods.get(childPosition);

						/**
						 * 如果数量相等，则从列表中删除此菜
						 */
						_foods.remove(selectedFood);
						_adapter.notifyDataSetChanged();

					}
				});
				((ImageView) view.findViewById(R.id.jian2))
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								OrderFood selectedFood = _foods
										.get(childPosition);
								if (selectedFood.getCount() == 1.0f) {

									OrderFood selectedFood2 = _foods
											.get(childPosition);
									/**
									 * 如果数量相等，则从列表中删除此菜
									 */
									_foods.remove(selectedFood2);
									_adapter.notifyDataSetChanged();
									return;
								}
								selectedFood.setCount(selectedFood.getCount() - 1.0f);

								final TextView counttxt = (TextView) findViewById(R.id.accountvalue);
								counttxt.setText((Util
										.float2String2(selectedFood.getCount())));
							}
						});

				// view.findViewById(R.id.jiao).setVisibility(View.GONE);

				// "口味"操作
				ImageView addTasteImgView = (ImageView) view
						.findViewById(R.id.addtaste);
				addTasteImgView
						.setBackgroundResource(R.drawable.taste_selector);
				addTasteImgView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_selectedPos = childPosition;
						if (_operListener != null) {
							if (_foods.get(childPosition).isTemporary) {
								Toast.makeText(_context, "临时菜不能添加口味", 0).show();
							} else {
								_operListener.onPickTaste(_foods
										.get(childPosition));
							}
						}
					}
				});

			} else {
				// "退菜"操作
				// 已点菜
				view.findViewById(R.id.add2).setVisibility(View.GONE);
				view.findViewById(R.id.jian2).setVisibility(View.GONE);

				((ImageView) view.findViewById(R.id.jiao)).setBackgroundResource(R.drawable.ji_selector);
				
				// 已点菜中叫的操作
				((ImageView) view.findViewById(R.id.jiao))
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
									OrderFood food = _foods.get(childPosition);
									if (food.hangStatus == OrderFood.FOOD_HANG_UP) {
										food.hangStatus = OrderFood.FOOD_IMMEDIATE;
									}
								_adapter.notifyDataSetChanged();
							}
						});

				ImageView cancelFoodImgView = (ImageView) view
						.findViewById(R.id.deletefood);
				cancelFoodImgView
						.setBackgroundResource(R.drawable.tuicai_selector);
				cancelFoodImgView
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (WirelessOrder.restaurant.pwd3 != null) {
									/**
									 * 提示输入权限密码2，验证通过的情况下显示删菜数量Dialog
									 */
									new AskPwdDialog(_context,
											AskPwdDialog.PWD_5) {
										@Override
										protected void onPwdPass(Context context) {
											dismiss();
											new AskCancelAmountDialog(_foods
													.get(childPosition)).show();
										}
									}.show();
								} else {
									new AskCancelAmountDialog(_foods
											.get(childPosition)).show();
								}
							}
						});
				// "催菜"操作
				ImageView addTasteImgView = (ImageView) view
						.findViewById(R.id.addtaste);
				addTasteImgView
						.setBackgroundResource(R.drawable.cuicai_selector);
				addTasteImgView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (food.isHurried) {
							food.isHurried = false;
							Toast.makeText(_context, "取消催菜成功", 0).show();
							_adapter.notifyDataSetChanged();

						} else {
							food.isHurried = true;
							Toast.makeText(_context, "催菜成功", 0).show();
							_adapter.notifyDataSetChanged();
						}
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
			View view = View.inflate(_context, R.layout.dropgrounpitem, null);
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
							new AlertDialog.Builder(_context)
									.setTitle("提示")
									.setMessage("确定全单叫起吗?")
									.setNeutralButton(
											"确定",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													for (int i = 0; i < _foods
															.size(); i++) {
														OrderFood food = _foods
																.get(i);
														if (food.hangStatus == OrderFood.FOOD_NORMAL) {
															food.hangStatus = OrderFood.FOOD_HANG_UP;
														}
													}
													_adapter.notifyDataSetChanged();
												}
											}).setNegativeButton("取消", null)
									.show();
						}
					}
				});

			} else {
				boolean hasHangupFood = false;
				for (int i = 0; i < _foods.size(); i++) {
					if (_foods.get(i).hangStatus == OrderFood.FOOD_HANG_UP) {
						hasHangupFood = true;
						break;
					}
				}

				if (hasHangupFood) {
					/**
					 * 点击全单即起按钮
					 */
					ImageView immediateImgView = (ImageView) view
							.findViewById(R.id.orderimage);
					immediateImgView.setVisibility(View.VISIBLE);
					immediateImgView
							.setBackgroundResource(R.drawable.jiqi_selector);
					immediateImgView
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									if (_foods.size() > 0) {
										new AlertDialog.Builder(_context)
												.setTitle("提示")
												.setMessage("确定全单即起吗?")
												.setNeutralButton(
														"确定",
														new DialogInterface.OnClickListener() {
															@Override
															public void onClick(
																	DialogInterface dialog,
																	int which) {
																for (int i = 0; i < _foods
																		.size(); i++) {
																	OrderFood food = _foods
																			.get(i);
																	if (food.hangStatus == OrderFood.FOOD_HANG_UP) {
																		food.hangStatus = OrderFood.FOOD_IMMEDIATE;
																	}
																}
																_adapter.notifyDataSetChanged();
															}
														})
												.setNegativeButton("取消", null)
												.show();
									}
								}
							});
				} else {
					/**
					 * 如果没有叫起的菜品则不显示叫起Button
					 */
					((ImageView) view.findViewById(R.id.orderimage))
							.setVisibility(View.INVISIBLE);
				}
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
	private class AskCancelAmountDialog extends Dialog {

		AskCancelAmountDialog(final OrderFood selectedFood) {
			super(_context, R.style.FullHeightDialog);

			// View view = LayoutInflater.from(_context).inflate(R.layout.alert,
			// null);
			setContentView(R.layout.alert);
			// getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView) findViewById(R.id.ordername)).setText("请输入"
					+ selectedFood.name + "的删除数量");

			((TextView) findViewById(R.id.table)).setText("数量：");
			// 删除数量默认为此菜品的点菜数量
			final EditText cancelEdtTxt = (EditText) findViewById(R.id.mycount);
			cancelEdtTxt.setText(Util.float2String2(selectedFood.getCount()));

			// "确定"Button
			Button okBtn = (Button) findViewById(R.id.confirm);
			okBtn.setText("确定");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					float foodAmount = selectedFood.getCount();
					float cancelAmount = Float.parseFloat(cancelEdtTxt
							.getText().toString());

					if (foodAmount == cancelAmount) {
						/**
						 * 如果数量相等，则从列表中删除此菜
						 */
						_foods.remove(selectedFood);
						_adapter.notifyDataSetChanged();
						dismiss();
						Toast.makeText(
								_context,
								"删除\"" + selectedFood.toString() + "\""
										+ cancelAmount + "份成功", 1).show();

					} else if (foodAmount > cancelAmount) {
						/**
						 * 如果删除数量少于已点数量，则相应减去删除数量
						 */
						selectedFood.setCount(foodAmount - cancelAmount);
						_adapter.notifyDataSetChanged();
						dismiss();
						Toast.makeText(
								_context,
								"删除\"" + selectedFood.toString() + "\""
										+ cancelAmount + "份成功", 1).show();

					} else {
						new AlertDialog.Builder(_context).setTitle("提示")
								.setMessage("你输入的删除数量大于已点数量, 请重新输入")
								.setNeutralButton("确定", null).show();
					}
				}
			});

			// "取消"Button
			Button cancelBtn = (Button) findViewById(R.id.alert_cancel);
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
	private class ExtOperDialg extends Dialog {

		ExtOperDialg(final OrderFood selectedFood) {
			super(_context, R.style.FullHeightDialog);
			setContentView(R.layout.item_alert);
			// getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView) findViewById(R.id.ordername)).setText("请选择"
					+ selectedFood.name + "的操作");
			if (_type == Type.INSERT_ORDER) {
				/**
				 * 新点菜是扩展功能为"删菜"、"口味"、"叫起/取消叫起"
				 */
				// 删菜功能
				((TextView) findViewById(R.id.item1Txt)).setText("删菜");
				((RelativeLayout) findViewById(R.id.r1))
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View arg0) {
								dismiss();
								new AskCancelAmountDialog(selectedFood).show();
							}
						});

				// 口味功能
				((TextView) findViewById(R.id.item2Txt)).setText("口味");
				((RelativeLayout) findViewById(R.id.r2))
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View arg0) {
								if (_operListener != null) {
									dismiss();
									_operListener.onPickTaste(selectedFood);
								}
							}
						});

				// 叫起/取消叫起
				if (selectedFood.hangStatus == OrderFood.FOOD_NORMAL) {
					((TextView) findViewById(R.id.item3Txt)).setText("叫起");
				} else {
					((TextView) findViewById(R.id.item3Txt)).setText("取消叫起");
				}
				((RelativeLayout) findViewById(R.id.r3))
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View arg0) {
								if (selectedFood.hangStatus == OrderFood.FOOD_NORMAL) {
									selectedFood.hangStatus = OrderFood.FOOD_HANG_UP;
									((TextView) findViewById(R.id.item3Txt))
											.setText("取消叫起");
									dismiss();
								} else {
									selectedFood.hangStatus = OrderFood.FOOD_NORMAL;
									((TextView) findViewById(R.id.item3Txt))
											.setText("叫起");
									dismiss();
								}
							}
						});

			} else {
				/**
				 * 已点菜的扩展功能为"退菜"、"即起"、"催菜/取消催菜"
				 */
				// 退菜功能
				((TextView) findViewById(R.id.item1Txt)).setText("退菜");
				((RelativeLayout) findViewById(R.id.r1))
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View arg0) {
								dismiss();
								if (WirelessOrder.restaurant.pwd3 != null) {
									new AskPwdDialog(_context,
											AskPwdDialog.PWD_5) {
										@Override
										protected void onPwdPass(Context context) {
											dismiss();
											new AskCancelAmountDialog(
													selectedFood).show();
										}
									}.show();
								} else {
									new AskCancelAmountDialog(selectedFood)
											.show();
								}
							}
						});

				// 如果菜品是叫起状态，显示"即起"功能
				if (selectedFood.hangStatus == OrderFood.FOOD_HANG_UP
						|| selectedFood.hangStatus == OrderFood.FOOD_IMMEDIATE) {
					if (selectedFood.hangStatus == OrderFood.FOOD_HANG_UP) {
						((TextView) findViewById(R.id.item2Txt)).setText("即起");
					} else {
						((TextView) findViewById(R.id.item2Txt))
								.setText("重新叫起");
					}
					((RelativeLayout) findViewById(R.id.r2))
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View arg0) {
									if (selectedFood.hangStatus == OrderFood.FOOD_HANG_UP) {
										selectedFood.hangStatus = OrderFood.FOOD_IMMEDIATE;
										((TextView) findViewById(R.id.item2Txt))
												.setText("即起");
										dismiss();

									} else if (selectedFood.hangStatus == OrderFood.FOOD_IMMEDIATE) {
										selectedFood.hangStatus = OrderFood.FOOD_HANG_UP;
										((TextView) findViewById(R.id.item2Txt))
												.setText("重新叫起");
										dismiss();
									}
								}
							});

					// 催菜/取消催菜功能
					if (selectedFood.isHurried) {
						((TextView) findViewById(R.id.item3Txt))
								.setText("取消催菜");
					} else {
						((TextView) findViewById(R.id.item3Txt)).setText("催菜");
					}
					((RelativeLayout) findViewById(R.id.r3))
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View arg0) {
									if (selectedFood.isHurried) {
										selectedFood.isHurried = false;
										((TextView) findViewById(R.id.item3Txt))
												.setText("催菜");
										dismiss();

									} else {
										selectedFood.isHurried = true;
										((TextView) findViewById(R.id.item3Txt))
												.setText("取消催菜");
										dismiss();
									}
								}
							});

				} else {

					// 催菜/取消催菜功能
					if (selectedFood.isHurried) {
						((TextView) findViewById(R.id.item2Txt))
								.setText("取消催菜");
					} else {
						((TextView) findViewById(R.id.item2Txt)).setText("催菜");
					}
					((RelativeLayout) findViewById(R.id.r2))
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View arg0) {
									if (selectedFood.isHurried) {
										selectedFood.isHurried = false;
										((TextView) findViewById(R.id.item2Txt))
												.setText("催菜");
										dismiss();

									} else {
										selectedFood.isHurried = true;
										((TextView) findViewById(R.id.item2Txt))
												.setText("取消催菜");
										dismiss();
									}
								}
							});

					((RelativeLayout) findViewById(R.id.r3))
							.setVisibility(View.GONE);
				}
			}

			// 返回Button
			Button cancelBtn = (Button) findViewById(R.id.back);
			cancelBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}

		@Override
		protected void onStop() {
			// trim(_selectedFood);
			_adapter.notifyDataSetChanged();
		}
	}

	public static interface OnChangedListener {
		public void onSourceChanged();
	}

	public static interface OnOperListener {
		public void onPickTaste(OrderFood selectedFood);

		public void onPickFood();
	}

}
