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
		 * ѡ��ÿ����Ʒ�Ĳ���
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
	 * ����ListView�����ͣ�Ŀǰ��Ϊ"�µ��"��"�ѵ��"����
	 * 
	 * @param type
	 *            One of values blew.<br>
	 *            Type.INSERT_ORDER - �µ�� Type.UPDATE_ORDER - �ѵ��
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
	 * ���ò�Ʒ�����Ļص��ӿ�
	 * 
	 * @param operListener
	 */
	public void setOperListener(OnOperListener operListener) {
		_operListener = operListener;
	}

	/**
	 * ��������Դ�仯�Ļص��ӿ�
	 * 
	 * @param chgListener
	 */
	public void setChangedListener(OnChangedListener chgListener) {
		_chgListener = chgListener;
	}

	/**
	 * ȡ��List�е�����Դ�����ǲ�Ʒ��List��Ϣ��
	 * 
	 * @return OrderFood��List
	 */
	public List<OrderFood> getSourceData() {
		return _foods;
	}

	/**
	 * ��source data�仯��ʱ�򣬵��ô˺���������ListView�����ݡ�
	 */
	public void notifyDataChanged() {
		if (_adapter != null) {
			_adapter.notifyDataSetChanged();
		}
	}

	/**
	 * ��Ӷ����Ʒ���Ѿ��˵�List��
	 * 
	 * @param foods
	 *            �µ��Ʒ
	 */
	public void addFoods(OrderFood[] foods) {
		for (OrderFood food : foods) {
			addFood(food);
		}
	}

	/**
	 * ��Ӳ�Ʒ���ѵ�˵�List��
	 * 
	 * @param food
	 *            ѡ�еĲ�Ʒ��Ϣ
	 */
	private void addFood(OrderFood food) {

		int index = _foods.indexOf(food);

		if (index != -1) {
			/**
			 * ���ԭ���Ĳ�Ʒ�б����Ѱ�������ͬ�Ĳ�Ʒ�� ���µ�˵������ۼӵ�ԭ���Ĳ�Ʒ�У� �������Ϊ�µ��Ʒ��ӵ���Ʒ�б���
			 */
			OrderFood pickedFood = _foods.get(index);

			float orderAmount = food.getCount() + pickedFood.getCount();
			if (orderAmount > 255) {
				Toast.makeText(_context,
						"�Բ���\"" + food.toString() + "\"���ֻ�ܵ�255��", 0).show();
				// pickedFood.setCount(new Float(255));
			} else {
				Toast.makeText(
						_context,
						"���"
								+ (food.hangStatus == OrderFood.FOOD_HANG_UP ? "������\""
										: "\"") + food.toString() + "\""
								+ Util.float2String2(food.getCount()) + "��", 0)
						.show();
				pickedFood.setCount(orderAmount);
				_foods.set(index, pickedFood);
				// ˢ�²�Ʒ�б�
				notifyDataChanged();
			}
		} else {
			if (food.getCount() > 255) {
				Toast.makeText(_context,
						"�Բ���\"" + food.toString() + "\"���ֻ�ܵ�255��", 0).show();

			} else {
				Toast.makeText(
						_context,
						"����"
								+ (food.hangStatus == OrderFood.FOOD_HANG_UP ? "������\""
										: "\"") + food.toString() + "\""
								+ Util.float2String2(food.getCount()) + "��", 0)
						.show();
				_foods.add(food);
				// ˢ�²�Ʒ�б�
				notifyDataChanged();

			}
		}
	}

	/**
	 * �������ڸ���ListView��source data����һ�ε��ô˺�����ʱ�򣬻���ͬʱ������Ӧ��Adapter��
	 * 
	 * @param foods
	 *            ��ListView����ʾ�Ĳ�Ʒ����
	 */
	public void notifyDataChanged(List<OrderFood> foods) {
		if (foods != null) {
			_foods = foods;
			trim();
			if (_adapter != null) {
				_adapter.notifyDataSetChanged();
			} else {
				if (_type == Type.INSERT_ORDER) {
					_adapter = new Adapter("�µ��") {
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
					_adapter = new Adapter("�ѵ��") {
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
	 * �˺������ڸ���ѡ�еĲ�Ʒ������"��ζ"��������Ҫ��TasteActivity������ش�ListView�� ��������͵��ô˺���������ѡ�еĲ�Ʒ
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
	// Toast.makeText(_context, "�Բ���\"" + firstFood.toString() + "\"���ֻ�ܵ�255��",
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
				status = "��";
			}
			if (food.isRecommend()) {
				if (status.length() == 0) {
					status = "��";
				} else {
					status = status + ",��";
				}
			}
			if (food.isGift()) {
				if (status.length() == 0) {
					status = "��";
				} else {
					status = status + ",��";
				}
			}
			if (status.length() != 0) {
				status = "(" + status + ")";
			}

			String tempStatus = null;
			if (food.isTemporary) {
				tempStatus = "(��)";
			} else {
				tempStatus = "";
			}

			String hangStatus = null;
			if (food.hangStatus == OrderFood.FOOD_HANG_UP) {
				hangStatus = "��";
			} else if (food.hangStatus == OrderFood.FOOD_IMMEDIATE) {
				hangStatus = "��";
			} else {
				hangStatus = "";
			}
			if (hangStatus.length() != 0) {
				hangStatus = "(" + hangStatus + ")";
			}

			String hurriedStatus = null;
			if (food.isHurried) {
				hurriedStatus = "(��)";
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
			 * "�µ��"��ListView��ʾ"ɾ��"��"��ζ" "�ѵ��"��ListView��ʾ"�˲�"��"�߲�"
			 */
			if (_type == Type.INSERT_ORDER) {
				// "ɾ��"����

				// �µ���неĲ���
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

				// �µ��
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
					 * "�µ��"ʱֱ����ʾɾ������Dialog
					 */
					@Override
					public void onClick(View v) {

						OrderFood selectedFood = _foods.get(childPosition);

						/**
						 * ���������ȣ�����б���ɾ���˲�
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
									 * ���������ȣ�����б���ɾ���˲�
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

				// "��ζ"����
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
								Toast.makeText(_context, "��ʱ�˲�����ӿ�ζ", 0).show();
							} else {
								_operListener.onPickTaste(_foods
										.get(childPosition));
							}
						}
					}
				});

			} else {
				// "�˲�"����
				// �ѵ��
				view.findViewById(R.id.add2).setVisibility(View.GONE);
				view.findViewById(R.id.jian2).setVisibility(View.GONE);

				((ImageView) view.findViewById(R.id.jiao)).setBackgroundResource(R.drawable.ji_selector);
				
				// �ѵ���неĲ���
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
									 * ��ʾ����Ȩ������2����֤ͨ�����������ʾɾ������Dialog
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
				// "�߲�"����
				ImageView addTasteImgView = (ImageView) view
						.findViewById(R.id.addtaste);
				addTasteImgView
						.setBackgroundResource(R.drawable.cuicai_selector);
				addTasteImgView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (food.isHurried) {
							food.isHurried = false;
							Toast.makeText(_context, "ȡ���߲˳ɹ�", 0).show();
							_adapter.notifyDataSetChanged();

						} else {
							food.isHurried = true;
							Toast.makeText(_context, "�߲˳ɹ�", 0).show();
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
			 * "�µ��"��Group��ʾ"���"Button
			 */
			if (_type == Type.INSERT_ORDER) {
				/**
				 * �����˰�ť
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
				 * ���ȫ������ť
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
									.setTitle("��ʾ")
									.setMessage("ȷ��ȫ��������?")
									.setNeutralButton(
											"ȷ��",
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
											}).setNegativeButton("ȡ��", null)
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
					 * ���ȫ������ť
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
												.setTitle("��ʾ")
												.setMessage("ȷ��ȫ��������?")
												.setNeutralButton(
														"ȷ��",
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
												.setNegativeButton("ȡ��", null)
												.show();
									}
								}
							});
				} else {
					/**
					 * ���û�н���Ĳ�Ʒ����ʾ����Button
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
	 * ��ʾ����ɾ��������Dialog
	 */
	private class AskCancelAmountDialog extends Dialog {

		AskCancelAmountDialog(final OrderFood selectedFood) {
			super(_context, R.style.FullHeightDialog);

			// View view = LayoutInflater.from(_context).inflate(R.layout.alert,
			// null);
			setContentView(R.layout.alert);
			// getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView) findViewById(R.id.ordername)).setText("������"
					+ selectedFood.name + "��ɾ������");

			((TextView) findViewById(R.id.table)).setText("������");
			// ɾ������Ĭ��Ϊ�˲�Ʒ�ĵ������
			final EditText cancelEdtTxt = (EditText) findViewById(R.id.mycount);
			cancelEdtTxt.setText(Util.float2String2(selectedFood.getCount()));

			// "ȷ��"Button
			Button okBtn = (Button) findViewById(R.id.confirm);
			okBtn.setText("ȷ��");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					float foodAmount = selectedFood.getCount();
					float cancelAmount = Float.parseFloat(cancelEdtTxt
							.getText().toString());

					if (foodAmount == cancelAmount) {
						/**
						 * ���������ȣ�����б���ɾ���˲�
						 */
						_foods.remove(selectedFood);
						_adapter.notifyDataSetChanged();
						dismiss();
						Toast.makeText(
								_context,
								"ɾ��\"" + selectedFood.toString() + "\""
										+ cancelAmount + "�ݳɹ�", 1).show();

					} else if (foodAmount > cancelAmount) {
						/**
						 * ���ɾ�����������ѵ�����������Ӧ��ȥɾ������
						 */
						selectedFood.setCount(foodAmount - cancelAmount);
						_adapter.notifyDataSetChanged();
						dismiss();
						Toast.makeText(
								_context,
								"ɾ��\"" + selectedFood.toString() + "\""
										+ cancelAmount + "�ݳɹ�", 1).show();

					} else {
						new AlertDialog.Builder(_context).setTitle("��ʾ")
								.setMessage("�������ɾ�����������ѵ�����, ����������")
								.setNeutralButton("ȷ��", null).show();
					}
				}
			});

			// "ȡ��"Button
			Button cancelBtn = (Button) findViewById(R.id.alert_cancel);
			cancelBtn.setText("ȡ��");
			cancelBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}
	}

	/**
	 * �����Ʒ�б�����չ���� Dialog
	 */
	private class ExtOperDialg extends Dialog {

		ExtOperDialg(final OrderFood selectedFood) {
			super(_context, R.style.FullHeightDialog);
			setContentView(R.layout.item_alert);
			// getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView) findViewById(R.id.ordername)).setText("��ѡ��"
					+ selectedFood.name + "�Ĳ���");
			if (_type == Type.INSERT_ORDER) {
				/**
				 * �µ������չ����Ϊ"ɾ��"��"��ζ"��"����/ȡ������"
				 */
				// ɾ�˹���
				((TextView) findViewById(R.id.item1Txt)).setText("ɾ��");
				((RelativeLayout) findViewById(R.id.r1))
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View arg0) {
								dismiss();
								new AskCancelAmountDialog(selectedFood).show();
							}
						});

				// ��ζ����
				((TextView) findViewById(R.id.item2Txt)).setText("��ζ");
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

				// ����/ȡ������
				if (selectedFood.hangStatus == OrderFood.FOOD_NORMAL) {
					((TextView) findViewById(R.id.item3Txt)).setText("����");
				} else {
					((TextView) findViewById(R.id.item3Txt)).setText("ȡ������");
				}
				((RelativeLayout) findViewById(R.id.r3))
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View arg0) {
								if (selectedFood.hangStatus == OrderFood.FOOD_NORMAL) {
									selectedFood.hangStatus = OrderFood.FOOD_HANG_UP;
									((TextView) findViewById(R.id.item3Txt))
											.setText("ȡ������");
									dismiss();
								} else {
									selectedFood.hangStatus = OrderFood.FOOD_NORMAL;
									((TextView) findViewById(R.id.item3Txt))
											.setText("����");
									dismiss();
								}
							}
						});

			} else {
				/**
				 * �ѵ�˵���չ����Ϊ"�˲�"��"����"��"�߲�/ȡ���߲�"
				 */
				// �˲˹���
				((TextView) findViewById(R.id.item1Txt)).setText("�˲�");
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

				// �����Ʒ�ǽ���״̬����ʾ"����"����
				if (selectedFood.hangStatus == OrderFood.FOOD_HANG_UP
						|| selectedFood.hangStatus == OrderFood.FOOD_IMMEDIATE) {
					if (selectedFood.hangStatus == OrderFood.FOOD_HANG_UP) {
						((TextView) findViewById(R.id.item2Txt)).setText("����");
					} else {
						((TextView) findViewById(R.id.item2Txt))
								.setText("���½���");
					}
					((RelativeLayout) findViewById(R.id.r2))
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View arg0) {
									if (selectedFood.hangStatus == OrderFood.FOOD_HANG_UP) {
										selectedFood.hangStatus = OrderFood.FOOD_IMMEDIATE;
										((TextView) findViewById(R.id.item2Txt))
												.setText("����");
										dismiss();

									} else if (selectedFood.hangStatus == OrderFood.FOOD_IMMEDIATE) {
										selectedFood.hangStatus = OrderFood.FOOD_HANG_UP;
										((TextView) findViewById(R.id.item2Txt))
												.setText("���½���");
										dismiss();
									}
								}
							});

					// �߲�/ȡ���߲˹���
					if (selectedFood.isHurried) {
						((TextView) findViewById(R.id.item3Txt))
								.setText("ȡ���߲�");
					} else {
						((TextView) findViewById(R.id.item3Txt)).setText("�߲�");
					}
					((RelativeLayout) findViewById(R.id.r3))
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View arg0) {
									if (selectedFood.isHurried) {
										selectedFood.isHurried = false;
										((TextView) findViewById(R.id.item3Txt))
												.setText("�߲�");
										dismiss();

									} else {
										selectedFood.isHurried = true;
										((TextView) findViewById(R.id.item3Txt))
												.setText("ȡ���߲�");
										dismiss();
									}
								}
							});

				} else {

					// �߲�/ȡ���߲˹���
					if (selectedFood.isHurried) {
						((TextView) findViewById(R.id.item2Txt))
								.setText("ȡ���߲�");
					} else {
						((TextView) findViewById(R.id.item2Txt)).setText("�߲�");
					}
					((RelativeLayout) findViewById(R.id.r2))
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View arg0) {
									if (selectedFood.isHurried) {
										selectedFood.isHurried = false;
										((TextView) findViewById(R.id.item2Txt))
												.setText("�߲�");
										dismiss();

									} else {
										selectedFood.isHurried = true;
										((TextView) findViewById(R.id.item2Txt))
												.setText("ȡ���߲�");
										dismiss();
									}
								}
							});

					((RelativeLayout) findViewById(R.id.r3))
							.setVisibility(View.GONE);
				}
			}

			// ����Button
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
