package com.wireless.ui.view;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.ui.R;
import com.wireless.ui.dialog.AskPwdDialog;

public class OrderFoodListView extends ExpandableListView{

	public final static int PICK_TASTE = 1;
	public final static int PICK_FOOD = 2;
	
	private OnOperListener _operListener;
	private OnChangedListener _chgListener;
	private Context _context;
	private List<OrderFood> _foods = new ArrayList<OrderFood>();
	private int _selectedPos;
	private byte _type = Type.INSERT_ORDER;
	private BaseExpandableListAdapter _adapter;
	
	public OrderFoodListView(Context context, AttributeSet attrs){
		super(context, attrs);
		_context = context;
		/**
		 * ѡ��ÿ����Ʒ�Ĳ���
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
	 * ����ListView�����ͣ�Ŀǰ��Ϊ"�µ��"��"�ѵ��"����
	 * @param type
	 * 			One of values blew.<br>
	 * 			Type.INSERT_ORDER - �µ��
	 * 			Type.UPDATE_ORDER - �ѵ��
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
	 * ���ò�Ʒ�����Ļص��ӿ�
	 * @param operListener
	 */
	public void setOperListener(OnOperListener operListener){
		_operListener = operListener;
	}
	     
	/**
	 * ��������Դ�仯�Ļص��ӿ�
	 * @param chgListener
	 */
	public void setChangedListener(OnChangedListener chgListener){
		_chgListener = chgListener;
	}
	
	/**
	 * ȡ��List�е�����Դ�����ǲ�Ʒ��List��Ϣ��
	 * @return
	 * 		OrderFood��List
	 */
	public List<OrderFood> getSourceData(){
		return _foods;
	}
	
	/**
	 * ��source data�仯��ʱ�򣬵��ô˺���������ListView�����ݡ�
	 */
	public void notifyDataChanged(){
		if(_adapter != null){
			_adapter.notifyDataSetChanged();
		}
	}

	/**
	 * �������ڸ���ListView��source data����һ�ε��ô˺�����ʱ�򣬻���ͬʱ������Ӧ��Adapter��
	 * @param foods
	 * 			��ListView����ʾ�Ĳ�Ʒ����
	 */
	public void notifyDataChanged(List<OrderFood> foods){
		if(foods != null){
			_foods = foods;
			if(_adapter != null){
				_adapter.notifyDataSetChanged();
			}else{
				if(_type == Type.INSERT_ORDER){
					_adapter = new Adapter("�µ��"){
						@Override
						public void notifyDataSetChanged(){
							super.notifyDataSetChanged();
							if(_chgListener != null){
								_chgListener.onSourceChanged();
							}
						}
					};
				}else{
					_adapter = new Adapter("�ѵ��"){
						@Override
						public void notifyDataSetChanged(){
							super.notifyDataSetChanged();
							if(_chgListener != null){
								_chgListener.onSourceChanged();
							}
						}						
					};
				}
				setAdapter(_adapter);	
				if(_chgListener != null){
					_chgListener.onSourceChanged();
				}
			}

		}else{
			throw new NullPointerException();
		}
	}
	
	/**
	 * �˺������ڸ���ѡ�еĲ�Ʒ������"��ζ"��������Ҫ��TasteActivity������ش�ListView��
	 * ��������͵��ô˺���������ѡ�еĲ�Ʒ
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
			final OrderFood food = _foods.get(childPosition);
			//show the name to each food
			String status = "";
			if(food.isSpecial()){
				status = "��";
			}
			if(food.isRecommend()){
				if(status.length() == 0){
					status = "��";
				}else{
					status = status + ",��";
				}
			}
			if(food.isGift()){
				if(status.length() == 0){
					status = "��";
				}else{
					status = status + ",��";
				}
			}
			if(status.length() != 0){
				status = "(" + status + ")";
			}
			
			String tempStatus = null;
			if(food.isTemporary){
				tempStatus = "(��)";
			}else{
				tempStatus = "";
			}
			
			String hangStatus = null;
			if(food.hangStatus == OrderFood.FOOD_HANG_UP){
				hangStatus = "��";
			}else if(food.hangStatus == OrderFood.FOOD_IMMEDIATE){
				hangStatus = "��";
			}else{
				hangStatus = "";
			}
			if(hangStatus.length() != 0){
				hangStatus = "(" + hangStatus + ")";
			}
			
			String hurriedStatus = null;
			if(food.isHurried){
				hurriedStatus = "(��)";
			}else{
				hurriedStatus = "";
			}
			((TextView) view.findViewById(R.id.foodname)).setText(tempStatus + hangStatus + hurriedStatus + food.name + status);
			//show the order amount to each food
			((TextView) view.findViewById(R.id.accountvalue)).setText(Util.float2String2(food.getCount()));
			//show the price to each food
			((TextView) view.findViewById(R.id.pricevalue)).setText(Util.CURRENCY_SIGN + Util.float2String2(food.calcPrice2()));
			//show the taste to each food
			((TextView)view.findViewById(R.id.taste)).setText(food.tastePref);
			/**
			 * "�µ��"��ListView��ʾ"ɾ��"��"��ζ"
			 * "�ѵ��"��ListView��ʾ"�˲�"��"�߲�"
			 */
			if(_type == Type.INSERT_ORDER){
				//"ɾ��"����			 
				ImageView delFoodImgView = (ImageView)view.findViewById(R.id.deletefood);
				delFoodImgView.setBackgroundResource(R.drawable.delete_selector);
				delFoodImgView.setOnClickListener(new View.OnClickListener() {
					/**
					 * "�µ��"ʱֱ����ʾɾ������Dialog
					 */
					@Override
					public void onClick(View v) {
						new AskCancelAmountDialog(_foods.get(childPosition)).show();
					}
				});

				//"��ζ"����
				ImageView addTasteImgView = (ImageView)view.findViewById(R.id.addtaste);
				addTasteImgView.setBackgroundResource(R.drawable.taste_selector);
				addTasteImgView.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						_selectedPos = childPosition;
						if(_operListener != null){
							if(_foods.get(childPosition).isTemporary){
								Toast.makeText(_context, "��ʱ�˲�����ӿ�ζ", 0).show();
							}else{
								_operListener.onPickTaste(_foods.get(childPosition));								
							}
						}
					}
				});
				
			}else{
				//"�˲�"����
				ImageView cancelFoodImgView = (ImageView)view.findViewById(R.id.deletefood);
				cancelFoodImgView.setBackgroundResource(R.drawable.tuicai_selector);
				cancelFoodImgView.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						if(WirelessOrder.restaurant.pwd3 != null){
							/**
							 * ��ʾ����Ȩ������2����֤ͨ�����������ʾɾ������Dialog
							 */
							new AskPwdDialog(_context, AskPwdDialog.PWD_3){
								@Override
								protected void onPwdPass(Context context){
									dismiss();
									new AskCancelAmountDialog(_foods.get(childPosition)).show();
								}
							}.show();
						}else{
							new AskCancelAmountDialog(_foods.get(childPosition)).show();
						}
					}
				});
				//"�߲�"����
				ImageView addTasteImgView = (ImageView)view.findViewById(R.id.addtaste);
				addTasteImgView.setBackgroundResource(R.drawable.cuicai_selector);
				addTasteImgView.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						if(food.isHurried){
							food.isHurried = false;
							Toast.makeText(_context, "ȡ���߲˳ɹ�", 0).show();
							_adapter.notifyDataSetChanged();
				
						}else{
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
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			View view = View.inflate(_context, R.layout.dropgrounpitem, null);
			((TextView)view.findViewById(R.id.grounname)).setText(_groupTitle);
			
			/**
			 * "�µ��"��Group��ʾ"���"Button
			 */
			if(_type == Type.INSERT_ORDER){
				/**
				 * �����˰�ť
				 */
				ImageView orderImg = (ImageView)view.findViewById(R.id.orderimage);
				orderImg.setBackgroundResource(R.drawable.order_selector);
				
				orderImg.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						if(_operListener != null){
							_operListener.onPickFood();
						}
					}
				});
				/**
				 * ���ȫ������ť
				 */
				ImageView hurriedImgView = (ImageView)view.findViewById(R.id.operateimage);
				hurriedImgView.setBackgroundResource(R.drawable.jiaoqi_selector);
				
				hurriedImgView.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {						
						if(_foods.size() > 0){
							new AlertDialog.Builder(_context)
								.setTitle("��ʾ")
								.setMessage("ȷ��ȫ��������?")
								.setNeutralButton("ȷ��", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,	int which){
											for(int i = 0; i < _foods.size(); i++){
												OrderFood food = _foods.get(i);
												if(food.hangStatus == OrderFood.FOOD_NORMAL){
													food.hangStatus = OrderFood.FOOD_HANG_UP;
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
				
			}else{
				boolean hasHangupFood = false;
				for(int i = 0; i < _foods.size(); i++){
					if(_foods.get(i).hangStatus == OrderFood.FOOD_HANG_UP){
						hasHangupFood = true;
						break;
					}
				}
				
				if(hasHangupFood){
					/**
					 * ���ȫ������ť
					 */
					ImageView immediateImgView = (ImageView)view.findViewById(R.id.orderimage);
					immediateImgView.setVisibility(View.VISIBLE);
					immediateImgView.setBackgroundResource(R.drawable.jiqi_selector);
					immediateImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {						
							if(_foods.size() > 0){
								new AlertDialog.Builder(_context)
								.setTitle("��ʾ")
								.setMessage("ȷ��ȫ��������?")
								.setNeutralButton("ȷ��", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,	int which){
											for(int i = 0; i < _foods.size(); i++){
												OrderFood food = _foods.get(i);
												if(food.hangStatus == OrderFood.FOOD_HANG_UP){
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
				}else{
					/**
					 * ���û�н���Ĳ�Ʒ����ʾ����Button
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
	 * ��ʾ����ɾ��������Dialog
	 */
	private class AskCancelAmountDialog extends Dialog{
	
		AskCancelAmountDialog(final OrderFood selectedFood) {
			super(_context, R.style.FullHeightDialog);
			
			View view = LayoutInflater.from(_context).inflate(R.layout.alert, null);
			setContentView(view);
			//getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView)view.findViewById(R.id.ordername)).setText("������" + selectedFood.name + "��ɾ������");
			
			((TextView)findViewById(R.id.table)).setText("������");
			//ɾ������Ĭ��Ϊ�˲�Ʒ�ĵ������
			final EditText cancelEdtTxt = (EditText)view.findViewById(R.id.mycount);			
			cancelEdtTxt.setText(Util.float2String2(selectedFood.getCount()));
			
			//"ȷ��"Button
			Button okBtn = (Button)view.findViewById(R.id.confirm);
			okBtn.setText("ȷ��");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					float foodAmount = selectedFood.getCount();
					float cancelAmount = Float.parseFloat(cancelEdtTxt.getText().toString());
					
					if(foodAmount == cancelAmount){
						/**
						 * ���������ȣ�����б���ɾ���˲�
						 */
						_foods.remove(selectedFood);
						_adapter.notifyDataSetChanged();
						dismiss();
						Toast.makeText(_context, "ɾ��\"" + selectedFood.toString() + "\"" + cancelAmount + "�ݳɹ�", 1).show();
						
					}else if(foodAmount > cancelAmount){
						/**
						 * ���ɾ�����������ѵ�����������Ӧ��ȥɾ������
						 */
						selectedFood.setCount(foodAmount - cancelAmount);
						_adapter.notifyDataSetChanged();
						dismiss();
						Toast.makeText(_context, "ɾ��\"" + selectedFood.toString() + "\"" + cancelAmount + "�ݳɹ�", 1).show();
						
					}else{
						new AlertDialog.Builder(_context)
							.setTitle("��ʾ")
							.setMessage("�������ɾ�����������ѵ�����, ����������")
							.setNeutralButton("ȷ��", null)
							.show();
					}
				}
			});
			
			//"ȡ��"Button
			Button cancelBtn = (Button)view.findViewById(R.id.alert_cancel);
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
	private class ExtOperDialg extends Dialog{

		ExtOperDialg(final OrderFood selectedFood) {
			super(_context, R.style.FullHeightDialog);
			setContentView(R.layout.item_alert);
			getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView)findViewById(R.id.ordername)).setText("��ѡ��" + selectedFood.name + "�Ĳ���");
			if(_type == Type.INSERT_ORDER){
				/**
				 * �µ������չ����Ϊ"ɾ��"��"��ζ"��"����/ȡ������"
				 */
				//ɾ�˹���
				((TextView)findViewById(R.id.item1Txt)).setText("ɾ��");
				((RelativeLayout)findViewById(R.id.r1)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						dismiss();
						new AskCancelAmountDialog(selectedFood).show();							
					}
				});
				
				//��ζ����
				((TextView)findViewById(R.id.item2Txt)).setText("��ζ");
				((RelativeLayout)findViewById(R.id.r2)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						if(_operListener != null){
							dismiss();
							_operListener.onPickTaste(selectedFood);
						}
					}
				});
				
				//����/ȡ������
				if(selectedFood.hangStatus == OrderFood.FOOD_NORMAL){
					((TextView)findViewById(R.id.item3Txt)).setText("����");						
				}else{
					((TextView)findViewById(R.id.item3Txt)).setText("ȡ������");
				}
				((RelativeLayout)findViewById(R.id.r3)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						if(selectedFood.hangStatus == OrderFood.FOOD_NORMAL){
							selectedFood.hangStatus = OrderFood.FOOD_HANG_UP;
							((TextView)findViewById(R.id.item3Txt)).setText("ȡ������");
							dismiss();
						}else{
							selectedFood.hangStatus = OrderFood.FOOD_NORMAL;
							((TextView)findViewById(R.id.item3Txt)).setText("����");		
							dismiss();
						}
					}
				});
				
			}else{
				/**
				 * �ѵ�˵���չ����Ϊ"�˲�"��"����"��"�߲�/ȡ���߲�"
				 */
				//�˲˹���
				((TextView)findViewById(R.id.item1Txt)).setText("�˲�");
				((RelativeLayout)findViewById(R.id.r1)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						dismiss();
						if(WirelessOrder.restaurant.pwd3 != null){
							new AskPwdDialog(_context, AskPwdDialog.PWD_3){							
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
				
				//�����Ʒ�ǽ���״̬����ʾ"����"����
				if(selectedFood.hangStatus == OrderFood.FOOD_HANG_UP || selectedFood.hangStatus == OrderFood.FOOD_IMMEDIATE){
					if(selectedFood.hangStatus == OrderFood.FOOD_HANG_UP){
						((TextView)findViewById(R.id.item2Txt)).setText("����");						
					}else{
						((TextView)findViewById(R.id.item2Txt)).setText("���½���");
					}
					((RelativeLayout)findViewById(R.id.r2)).setOnClickListener(new View.OnClickListener() {						
						@Override
						public void onClick(View arg0) {
			    			if(selectedFood.hangStatus == OrderFood.FOOD_HANG_UP){
			    				selectedFood.hangStatus = OrderFood.FOOD_IMMEDIATE;
								((TextView)findViewById(R.id.item2Txt)).setText("����");
								dismiss();
			    				
			    			}else if(selectedFood.hangStatus == OrderFood.FOOD_IMMEDIATE){
			    				selectedFood.hangStatus = OrderFood.FOOD_HANG_UP;
								((TextView)findViewById(R.id.item2Txt)).setText("���½���");
								dismiss();
			    			}						
						}
					});
					
					//�߲�/ȡ���߲˹���
					if(selectedFood.isHurried){
						((TextView)findViewById(R.id.item3Txt)).setText("ȡ���߲�");							
					}else{
						((TextView)findViewById(R.id.item3Txt)).setText("�߲�");						
					}
					((RelativeLayout)findViewById(R.id.r3)).setOnClickListener(new View.OnClickListener() {						
						@Override
						public void onClick(View arg0) {
							if(selectedFood.isHurried){
								selectedFood.isHurried = false;
								((TextView)findViewById(R.id.item3Txt)).setText("�߲�");	
								dismiss();
								
							}else{
								selectedFood.isHurried = true;
								((TextView)findViewById(R.id.item3Txt)).setText("ȡ���߲�");	
								dismiss();
							}
						}
					});
					
				}else{
					
					//�߲�/ȡ���߲˹���
					if(selectedFood.isHurried){
						((TextView)findViewById(R.id.item2Txt)).setText("ȡ���߲�");							
					}else{
						((TextView)findViewById(R.id.item2Txt)).setText("�߲�");						
					}
					((RelativeLayout)findViewById(R.id.r2)).setOnClickListener(new View.OnClickListener() {						
						@Override
						public void onClick(View arg0) {
							if(selectedFood.isHurried){
								selectedFood.isHurried = false;
								((TextView)findViewById(R.id.item2Txt)).setText("�߲�");	
								dismiss();
					
							}else{
								selectedFood.isHurried = true;
								((TextView)findViewById(R.id.item2Txt)).setText("ȡ���߲�");	
								dismiss();
							}						
						}
					});
					
					((RelativeLayout)findViewById(R.id.r3)).setVisibility(View.GONE);
				}					
			}
			
			//����Button
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
			_adapter.notifyDataSetChanged();
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
