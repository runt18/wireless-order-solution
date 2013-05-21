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
	public void addFoods(List<OrderFood> foods) {
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
				Toast.makeText(getContext(), "�Բ���\"" + food.toString() + "\"���ֻ�ܵ�255��", Toast.LENGTH_SHORT).show();
				// pickedFood.setCount(new Float(255));
			} else {
				Toast.makeText(getContext(), 
							  "���" + (food.isHangup() ? "������\"" : "\"") + 
							  food.toString() + "\"" + NumericUtil.float2String2(food.getCount()) + "��", Toast.LENGTH_SHORT).show();
				pickedFood.setCount(orderAmount);
				_foods.set(index, pickedFood);
				// ˢ�²�Ʒ�б�
				notifyDataChanged();
			}
		} else {
			if (food.getCount() > 255) {
				Toast.makeText(getContext(), "�Բ���\"" + food.toString() + "\"���ֻ�ܵ�255��", Toast.LENGTH_SHORT).show();

			} else {
				Toast.makeText(getContext(),
							   "����" + (food.isHangup() ? "������\"" : "\"") + 
							   food.toString() + "\"" + NumericUtil.float2String2(food.getCount()) + "��", Toast.LENGTH_SHORT).show();
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
				status = "��";
			}
			if (food.asFood().isRecommend()) {
				if (status.length() == 0) {
					status = "��";
				} else {
					status = status + ",��";
				}
			}
			if (food.asFood().isGift()) {
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
			if (food.isTemp()) {
				tempStatus = "(��)";
			} else {
				tempStatus = "";
			}

			String hangStatus = null;
			if (food.isHangup()) {
				hangStatus = "��";
			} else {
				hangStatus = "";
			}
			if (hangStatus.length() != 0) {
				hangStatus = "(" + hangStatus + ")";
			}

			String hurriedStatus = null;
			if (food.isHurried()) {
				hurriedStatus = "(��)";
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
			 * "�µ��"��ListView��ʾ"ɾ��"��"��ζ" "�ѵ��"��ListView��ʾ"�˲�"��"�߲�"
			 */
			if (_type == Type.INSERT_ORDER) {

				// �µ����"+"����
				((ImageView) view.findViewById(R.id.add2))
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								OrderFood selectedFood = _foods.get(childPosition);
								float amount = selectedFood.getCount() + 1;
								if(amount <= 255){
									selectedFood.setCount(selectedFood.getCount() + 1);
								}else{
									Toast.makeText(getContext(), selectedFood.getName() + "ֻ�����ֻ�ܵ�255��", Toast.LENGTH_SHORT).show();
								}
								_adapter.notifyDataSetChanged();
							}
						});
				
				//�µ����"-"����
				((ImageView) view.findViewById(R.id.jian2)).setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						OrderFood selectedFood = _foods.get(childPosition);
						if (selectedFood.getCount() > 1) {
							selectedFood.setCount(selectedFood.getCount() - 1);
							_adapter.notifyDataSetChanged();
						}else{
							Toast.makeText(getContext(), selectedFood.getName() + "�����������С��1", Toast.LENGTH_SHORT).show();
						}
					}
				});
				
				// �µ����"��"�Ĳ���
				((ImageView) view.findViewById(R.id.jiao)).setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						OrderFood food = _foods.get(childPosition);
						if (food.isHangup()) {
							food.setHangup(false);
							Toast.makeText(getContext(), "ȡ������" + food.getName(), Toast.LENGTH_SHORT).show();
						}else{
							food.setHangup(true);
							Toast.makeText(getContext(), "����" + food.getName(), Toast.LENGTH_SHORT).show();
						}
						_adapter.notifyDataSetChanged();
					}
				});
				
				//�µ���е�"ɾ"����
				ImageView delFoodImgView = (ImageView) view.findViewById(R.id.deletefood);
				delFoodImgView.setBackgroundResource(R.drawable.delete_selector);
				delFoodImgView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						final OrderFood selectedFood = _foods.get(childPosition);

						new AlertDialog.Builder(getContext())
							.setTitle("��ʾ")
							.setMessage("�Ƿ�ȷ��ȡ��" + selectedFood.getName() + "?")
							.setNeutralButton("ȷ��",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,	int which){
										_foods.remove(selectedFood);
										Toast.makeText(getContext(), "ȡ��" + food.getName(), Toast.LENGTH_SHORT).show();
										_adapter.notifyDataSetChanged();
									}
								})
							.setNegativeButton("ȡ��", null)
							.show();

					}
				});

				// �µ���е�"��ζ"����
				ImageView addTasteImgView = (ImageView) view.findViewById(R.id.addtaste);
				addTasteImgView.setBackgroundResource(R.drawable.taste_selector);
				addTasteImgView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						_selectedPos = childPosition;
						if (_operListener != null) {
							if (_foods.get(childPosition).isTemp()) {
								Toast.makeText(getContext(), "��ʱ�˲�����ӿ�ζ", Toast.LENGTH_SHORT).show();
							} else {
								_operListener.onPickTaste(_foods.get(childPosition));
							}
						}
					}
				});
				
				
				//�µ����"="����
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
				// �ѵ�˵Ĳ���Ҫ�İ�ť��������
				view.findViewById(R.id.add2).setVisibility(View.GONE);
				view.findViewById(R.id.jian2).setVisibility(View.GONE);
				view.findViewById(R.id.equlefood).setVisibility(View.GONE);
				view.findViewById(R.id.jiao).setVisibility(View.GONE);
				
				// �ѵ����"��"�Ĳ���
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
									 * ��ʾ����Ȩ������2����֤ͨ�����������ʾɾ������Dialog
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
				
				// �ѵ����"��"�Ĳ���
				ImageView addTasteImgView = (ImageView) view
						.findViewById(R.id.addtaste);
				addTasteImgView
						.setBackgroundResource(R.drawable.cuicai_selector);
				addTasteImgView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (food.isHurried()) {
							food.setHurried(false);
							Toast.makeText(getContext(), food.getName() + "ȡ���߲�", Toast.LENGTH_SHORT).show();

						} else {
							food.setHurried(true);
							Toast.makeText(getContext(), food.getName() + "�߲�", Toast.LENGTH_SHORT).show();
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
							new AlertDialog.Builder(getContext())
									.setTitle("��ʾ")
									.setMessage(_foods.get(0).isHangup() ? "ȷ��ȫ��������?" : "ȷ��ȫ��ȡ��������?")
									.setNeutralButton(
											"ȷ��",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog,	int which) {
													for (int i = 0; i < _foods.size(); i++) {
														OrderFood food = _foods.get(i);
														food.toggleHangup();
													}
													_adapter.notifyDataSetChanged();
												}
											}).setNegativeButton("ȡ��", null)
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
	 * ��ʾ����ɾ��������Dialog
	 */	
	private void showCancelAmountDialog(final OrderFood selectedFood){
		final EditText amountEdtTxt = new EditText(getContext());
		amountEdtTxt.setInputType(InputType.TYPE_CLASS_NUMBER);
		// ɾ������Ĭ��Ϊ�˲�Ʒ�ĵ������
		amountEdtTxt.setText(NumericUtil.float2String2(selectedFood.getCount()));
		// ����õ����������
		amountEdtTxt.setSelection(amountEdtTxt.getText().length());
		
		final AlertDialog cancelAmtDialog = new AlertDialog.Builder(getContext())
			.setTitle("������"+ selectedFood.getName() + "��ɾ������")
			.setView(amountEdtTxt)
			.setNeutralButton("ȷ��",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,	int which){					
						
					}
				})
			.setNegativeButton("ȡ��", null)
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
								 * ���������ȣ�����б���ɾ���˲�
								 */
								_foods.remove(selectedFood);
								_adapter.notifyDataSetChanged();
								Toast.makeText(getContext(), "ɾ��\"" + selectedFood.toString() + "\""	+ cancelAmount + "�ݳɹ�", Toast.LENGTH_SHORT).show();
								cancelAmtDialog.dismiss();
								
							} else if (foodAmount > cancelAmount) {
								/**
								 * ���ɾ�����������ѵ�����������Ӧ��ȥɾ������
								 */
								selectedFood.setCount(foodAmount - cancelAmount);
								_adapter.notifyDataSetChanged();
								Toast.makeText(getContext(), "ɾ��\"" + selectedFood.toString() + "\"" + cancelAmount + "�ݳɹ�", Toast.LENGTH_SHORT).show();
								cancelAmtDialog.dismiss();
	
							} else {
								new AlertDialog.Builder(getContext()).setTitle("��ʾ")
									.setMessage("�������ɾ�����������ѵ�����, ����������")
									.setNeutralButton("ȷ��", null)
									.show();
							}							
						}catch(NumberFormatException e){
							Toast.makeText(getContext(), "�������ɾ����������ȷ, ����������", Toast.LENGTH_SHORT).show();							
						}
					}
				});
			}
		});
		
		cancelAmtDialog.show();
		
	}
	
	/**
	 * ��ʾ������������Dialog
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
//			((TextView)findViewById(R.id.orderTitleTxt)).setText("������" + selectedFood.name + "�ĵ������");
//
//			EditText amountEdtTxt = ((EditText)findViewById(R.id.amountEdtTxt));
//			amountEdtTxt.setText("");
//
//			
//			//"ȷ��"Button
//			Button okBtn = (Button)findViewById(R.id.orderConfirmBtn);
//			okBtn.setText("ȷ��");
//			okBtn.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {	
//					try{
//						float orderAmount = Float.parseFloat(((EditText)findViewById(R.id.amountEdtTxt)).getText().toString());
//						
//		       			if(orderAmount > 255){
//		       				Toast.makeText(getContext(), "�Բ���\"" + selectedFood.toString() + "\"���ֻ�ܵ�255��", 0).show();
//		       			}else{
//		       				selectedFood.setCount(orderAmount);
//		       				_adapter.notifyDataSetChanged();
//							dismiss();
//		       			}
//					
//					}catch(NumberFormatException e){
//						Toast.makeText(getContext(), "�������������ʽ����ȷ������������", 0).show();
//					}
//				}
//			});
//			
//			//"ȡ��"Button
//			Button cancelBtn = (Button)findViewById(R.id.orderCancelBtn);
//			cancelBtn.setText("ȡ��");
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
	 * ��ʾ������������Dialog
	 * @author FaRong.Zhang
	 *
	 */
	public void showOrderAmountDialog(final OrderFood selectedFood){
		
		final EditText orderAmtEdtTxt = new EditText(getContext());

		orderAmtEdtTxt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		
		final AlertDialog orderAmtDialog = new AlertDialog.Builder(getContext())
			.setTitle("������" + selectedFood.getName() + "�ĵ������")
			.setView(orderAmtEdtTxt)
			.setNeutralButton("ȷ��",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,	int which){

					}
				})
			.setNegativeButton("ȡ��", null)
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
			       				Toast.makeText(getContext(), "�Բ���\"" + selectedFood.toString() + "\"���ֻ�ܵ�255��", Toast.LENGTH_SHORT).show();
			       			}else{
			       				selectedFood.setCount(orderAmount);
			       				_adapter.notifyDataSetChanged();
								orderAmtDialog.dismiss();
			       			}
						
						}catch(NumberFormatException e){
							Toast.makeText(getContext(), "�������������ʽ����ȷ������������", Toast.LENGTH_SHORT).show();
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
