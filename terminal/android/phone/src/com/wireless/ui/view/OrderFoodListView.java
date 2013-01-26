package com.wireless.ui.view;

import java.util.ArrayList;
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
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.excep.BusinessException;
import com.wireless.protocol.NumericUtil;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.TasteGroup;
import com.wireless.protocol.Type;
import com.wireless.ui.R;
import com.wireless.ui.dialog.AskPwdDialog;

public class OrderFoodListView extends ExpandableListView{

	public final static int PICK_TASTE = 1;
	public final static int PICK_FOOD = 2;
	private static final String KEY_THE_FOOD = "keyTheFood";
	private static final String KEY_IS_OFFSET = "keyIsOffset";
	
	private OnOperListener mOperListener;
	private OnChangedListener mChgListener;
//	private OrderFood[] mSrcFoods;
	private Order mTmpOrder = new Order();;
	private int mSelectedPos;
	private byte mType = Type.INSERT_ORDER;
	private BaseExpandableListAdapter mAdapter;
	private ArrayList<HashMap<String,Object>> mFoodsWithOffset = new ArrayList<HashMap<String,Object>>();
	private AllMarkClickListener mAllMarkClickListener;
	private Taste[] mOldAllTastes;

	public OrderFoodListView(Context context, AttributeSet attrs){
		super(context, attrs);
		/**
		 * ѡ��ÿ����Ʒ�Ĳ���
		 */
		setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				@SuppressWarnings("unchecked")
				HashMap<String,Object> map = (HashMap<String, Object>) v.getTag();
				if(map.containsKey(KEY_IS_OFFSET)){
					return true;
				} else if(mType == Type.INSERT_ORDER){
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
	 * ���ò�Ʒ�����Ļص��ӿ�
	 * @param operListener
	 */
	public void setOperListener(OnOperListener operListener){
		mOperListener = operListener;
	}
	     
	/**
	 * ��������Դ�仯�Ļص��ӿ�
	 * @param chgListener
	 */
	public void setChangedListener(OnChangedListener chgListener){
		mChgListener = chgListener;
	}
	
	public void addFood(OrderFood foodToAdd) throws BusinessException{
		mTmpOrder.addFood(foodToAdd);
		refreshOffsetFoods(mTmpOrder.foods);
		notifyDataChanged();
	}
	
	public void addFoods(OrderFood[] foodsToAdd){
		mTmpOrder.addFoods(foodsToAdd);
		refreshOffsetFoods(mTmpOrder.foods);
		notifyDataChanged();
	}
	
	/**
	 * �˺������ڸ���ѡ�еĲ�Ʒ������"��ζ"��������Ҫ��TasteActivity������ش�ListView��
	 * ��������͵��ô˺���������ѡ�еĲ�Ʒ
	 * @param food
	 */
	public void setFood(OrderFood foodToSet){
		if(foodToSet != null && mAdapter != null){
			mTmpOrder.foods[mSelectedPos] = foodToSet;
			
			refreshOffsetFoods(mTmpOrder.foods);
			mAdapter.notifyDataSetChanged();
		
		}else{
			throw new NullPointerException();
		}
	}
	
	public void setFoods(OrderFood[] foods){
		mTmpOrder.foods = foods;
		refreshOffsetFoods(foods);
		notifyDataChanged();
	}
	
	private void refreshOffsetFoods(OrderFood[] foods){
		mFoodsWithOffset = new ArrayList<HashMap<String,Object>>();
		
		for(OrderFood food : foods)
		{
			if(food.getCount() > 0f){
				HashMap<String,Object> foodMap = new HashMap<String,Object>();
				foodMap.put(KEY_THE_FOOD, food);
				mFoodsWithOffset.add(foodMap);
			}
			if(food.getDelta() > 0f && mType == Type.UPDATE_ORDER)
			{
				HashMap<String,Object> offsetFoodMap = new HashMap<String,Object>();
				offsetFoodMap.put(KEY_THE_FOOD, food);
				offsetFoodMap.put(KEY_IS_OFFSET, true);
				mFoodsWithOffset.add(offsetFoodMap);
			}
		}
	}
	
	public void setAllTaste(Taste[] tastes){
		//Ϊ�����µ�˺��ѵ����ӿ�ζ
		for(HashMap<String, Object> map:mFoodsWithOffset){
			OrderFood food = (OrderFood) map.get(KEY_THE_FOOD);
			if(!food.hasTaste()){
				food.makeTasetGroup(tastes, null);
			}
			for(Taste taste: tastes){
				food.getTasteGroup().addTaste(taste);
			}
			
		}
		mAdapter.notifyDataSetChanged();
		mOldAllTastes = tastes;
	}
	/**
	 * ��ʼ���ؼ�
	 * @param type
	 * 			One of values blew.<br>
	 * 			Type.INSERT_ORDER - �µ��
	 * 			Type.UPDATE_ORDER - �ѵ��
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
			mAdapter = new Adapter("�µ��"){
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
			mAdapter = new Adapter("�ѵ��"){
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
	 * ȡ��List�е�����Դ�����ǲ�Ʒ��List��Ϣ��
	 * @return
	 * 		OrderFood��List
	 */
	public OrderFood[] getSourceData(){
		return mTmpOrder.foods;
	}
	
	/**
	 * ��source data�仯��ʱ�򣬵��ô˺���������ListView�����ݡ�
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
		private PopupWindow mPopup;
		private boolean isHangUp = false;
		
		public Adapter(String groupTitle){
			mGroupTitle = groupTitle;
		}
		
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mFoodsWithOffset.get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			View view;
			if(convertView == null){
				view = View.inflate(getContext(), R.layout.order_activity_child_item, null);
			}else{
				view = convertView;
			}
			
			HashMap<String,Object> foodMap = mFoodsWithOffset.get(childPosition);
			final OrderFood food = (OrderFood) foodMap.get(KEY_THE_FOOD);
			view.setTag(foodMap);
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
			
			String comboStatus = null;
			if(food.isCombo()){
				comboStatus = "(��)";
			}else{
				comboStatus = "";
			}
			
			((TextView) view.findViewById(R.id.foodname)).setText(comboStatus + tempStatus + hangStatus + hurriedStatus + food.getName() + status);
			
			Button restoreBtn = (Button) view.findViewById(R.id.button_orderFoodListView_childItem_restore);
			//�����Ƿ����˲�����ʾ
			if(foodMap.containsKey(KEY_IS_OFFSET)){ 
				((TextView) view.findViewById(R.id.accountvalue)).setText(NumericUtil.float2String2(food.getDelta()));
				view.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.VISIBLE);
				//ȡ���˲˰�ť
				restoreBtn.setVisibility(View.VISIBLE);
				restoreBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						HashMap<String,Object> map =  mFoodsWithOffset.get(childPosition);
						OrderFood food = (OrderFood)map.get(KEY_THE_FOOD);
						try {
							food.addCount(food.getDelta());						
							refreshOffsetFoods(mTmpOrder.foods);
							mAdapter.notifyDataSetChanged();
						} catch (BusinessException e) {
							Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					}
				});
			} else {
				restoreBtn.setVisibility(View.INVISIBLE);
				//show the order amount to each food
				((TextView) view.findViewById(R.id.accountvalue)).setText(NumericUtil.float2String2(food.getCount()));
				view.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.INVISIBLE);
			}
			//show the price to each food
			((TextView) view.findViewById(R.id.pricevalue)).setText(NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(food.calcPriceWithTaste()));
			//show the taste to each food
			((TextView)view.findViewById(R.id.taste)).setText(food.hasTaste() ? food.getTasteGroup().getTastePref() : TasteGroup.NO_TASTE_PREF);
			/**
			 * "�µ��"��ListView��ʾ"ɾ��"��"��ζ"
			 * "�ѵ��"��ListView��ʾ"�˲�"��"�߲�"
			 */
			if(mType == Type.INSERT_ORDER){
				//"ɾ��"����			 
				ImageView delFoodImgView = (ImageView)view.findViewById(R.id.deletefood);
				delFoodImgView.setBackgroundResource(R.drawable.delete_selector);
				delFoodImgView.setOnClickListener(new View.OnClickListener() {
					/**
					 * "�µ��"ʱֱ����ʾɾ������Dialog
					 */
					@Override
					public void onClick(View v) {
						HashMap<String,Object> map =  mFoodsWithOffset.get(childPosition);
						OrderFood food = (OrderFood)map.get(KEY_THE_FOOD);
						new AskCancelAmountDialog(food,true).show();
					}
				});

				//"����"����
				ImageView addTasteImgView = (ImageView)view.findViewById(R.id.addtaste);
				addTasteImgView.setBackgroundResource(R.drawable.amount_selector);
				addTasteImgView.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						mSelectedPos = childPosition;
						HashMap<String,Object> map =  mFoodsWithOffset.get(childPosition);
						OrderFood food = (OrderFood)map.get(KEY_THE_FOOD);
						new AskCancelAmountDialog(food,false).show();
					}
				});
				
			}else{
				ImageView cancelFoodImgView = (ImageView)view.findViewById(R.id.deletefood);
				cancelFoodImgView.setBackgroundResource(R.drawable.tuicai_selector);
				
				ImageView addTasteImgView = (ImageView)view.findViewById(R.id.addtaste);
				addTasteImgView.setBackgroundResource(R.drawable.cuicai_selector);
				
				if(foodMap.containsKey(KEY_IS_OFFSET)){
					cancelFoodImgView.setVisibility(View.INVISIBLE);
					addTasteImgView.setVisibility(View.INVISIBLE);
				} else {
					cancelFoodImgView.setVisibility(View.VISIBLE);
					addTasteImgView.setVisibility(View.VISIBLE);
					
					//"�˲�"����
					cancelFoodImgView.setOnClickListener(new View.OnClickListener() {				
						@Override 
						public void onClick(View v) {
							if(WirelessOrder.restaurant.pwd5 != null){
								/**
								 * ��ʾ�˲�Ȩ�����룬��֤ͨ�����������ʾɾ������Dialog
								 */
								new AskPwdDialog(getContext(), AskPwdDialog.PWD_5){
									@Override
									protected void onPwdPass(Context context){
										dismiss();
										HashMap<String,Object> map =  mFoodsWithOffset.get(childPosition);
										OrderFood food = (OrderFood)map.get(KEY_THE_FOOD);
										new AskCancelAmountDialog(food,true).show();
									}
								}.show();
							}else{
								HashMap<String,Object> map =  mFoodsWithOffset.get(childPosition);
								OrderFood food = (OrderFood)map.get(KEY_THE_FOOD);
								new AskCancelAmountDialog(food,true).show();
							}
						}
					});
					//"�߲�"����
					addTasteImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							if(food.isHurried){
								food.isHurried = false;
								Toast.makeText(getContext(), "ȡ���߲˳ɹ�", Toast.LENGTH_SHORT).show();
								mAdapter.notifyDataSetChanged();
					
							}else{
								food.isHurried = true;
								Toast.makeText(getContext(), "�߲˳ɹ�", Toast.LENGTH_SHORT).show();	
								mAdapter.notifyDataSetChanged();
							}			
						}
					}); 
				}
			}
			return view;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mFoodsWithOffset.size();
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
			View layout;
			if(convertView == null){
				layout = View.inflate(getContext(), R.layout.dropgrounpitem, null);
			}else{
				layout = convertView;
			}
			
			((TextView)layout.findViewById(R.id.grounname)).setText(mGroupTitle);
			
			/**
			 * "�µ��"��Group��ʾ"���"Button
			 */
			if(mType == Type.INSERT_ORDER){
				
				if(mPopup == null){
					View popupLayout = View.inflate(getContext(), R.layout.order_activity_operate_popup, null);
					mPopup = new PopupWindow(popupLayout, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					mPopup.setOutsideTouchable(true);
					mPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_small));
					mPopup.update();
					//ȫ������ť
					Button hangUpBtn = (Button) popupLayout.findViewById(R.id.button_orderActivity_operate_popup_callUp);
					hangUpBtn.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							if(isHangUp){
								for(HashMap<String, Object> map:mFoodsWithOffset){
									OrderFood food = (OrderFood) map.get(KEY_THE_FOOD);
									if(food.hangStatus == OrderFood.FOOD_HANG_UP){
										food.hangStatus = OrderFood.FOOD_NORMAL;
									}	
								}
								isHangUp = false; 
								mPopup.dismiss();
								mAdapter.notifyDataSetChanged();
							}
							else if(mFoodsWithOffset.size() > 0){
								new AlertDialog.Builder(getContext())
									.setTitle("��ʾ")
									.setMessage("ȷ��ȫ��������?")
									.setNeutralButton("ȷ��", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog,	int which){
												for(HashMap<String, Object> map:mFoodsWithOffset){
													OrderFood food = (OrderFood) map.get(KEY_THE_FOOD);
													if(food.hangStatus == OrderFood.FOOD_NORMAL){
														food.hangStatus = OrderFood.FOOD_HANG_UP;
													}							
												}
												isHangUp = true;
												mAdapter.notifyDataSetChanged();
												mPopup.dismiss();
											}
										})
										.setNegativeButton("ȡ��", null)
										.show();	
							}	
							else {
								Toast.makeText(getContext(), "û���µ�ˣ��޷�����", Toast.LENGTH_SHORT).show();
							}
						}
					});
					//ȫ����ע
					Button allRemarkBtn = (Button) popupLayout.findViewById(R.id.button_orderActivity_operate_popup_remark);
					allRemarkBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if(mOldAllTastes != null){
								for(HashMap<String, Object> map:mFoodsWithOffset){
									OrderFood food = (OrderFood) map.get(KEY_THE_FOOD);
									if(food.hasNormalTaste()){
										for(Taste t:mOldAllTastes){
											food.getTasteGroup().removeTaste(t); 
										}
									}
								}
								mOldAllTastes = null;
								mAdapter.notifyDataSetChanged();
								mPopup.dismiss();
							}
							else if(!mFoodsWithOffset.isEmpty()){
								if(mAllMarkClickListener != null)
								{
									mAllMarkClickListener.allMarkClick();
								}
//								Intent intent = new Intent(getContext(), PickTasteActivity.class);
//								Bundle bundle = new Bundle(); 
//								OrderFood dummyFood = new OrderFood();
//								dummyFood.name = "ȫ����ע";
//								bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(dummyFood));
//								bundle.putString(PickTasteActivity.INIT_TAG, PickTasteActivity.TAG_TASTE);
//								bundle.putBoolean(PickTasteActivity.PICK_ALL_ORDER_TASTE, true);
//								intent.putExtras(bundle);
//								startActivityForResult(intent, OrderActivity.ALL_ORDER_REMARK);
								mPopup.dismiss();
							} else {
								Toast.makeText(getContext(), "�˲�̨��δ��ˣ��޷���ӱ�ע", Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
				
				View orderOperateBtn = layout.findViewById(R.id.button_orderActivity_opera);
				orderOperateBtn.setVisibility(View.VISIBLE);
				orderOperateBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Button allRemarkBtn = (Button) mPopup.getContentView().findViewById(R.id.button_orderActivity_operate_popup_remark);
						if(mOldAllTastes != null)
							allRemarkBtn.setText("ȡ����ע");
						else allRemarkBtn.setText("��ע");
						
						Button hangUpBtn = (Button) mPopup.getContentView().findViewById(R.id.button_orderActivity_operate_popup_callUp);
						if(isHangUp ){
							hangUpBtn.setText("ȡ������");
						} else hangUpBtn.setText("����");
						
						mPopup.showAsDropDown(v);
					}
				});
				
				/**
				 * �����˰�ť
				 */
				ImageView orderImg = (ImageView)layout.findViewById(R.id.orderimage);
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
				 * ���ȫ������ť
				 */
//				ImageView hurriedImgView = (ImageView)layout.findViewById(R.id.operateimage);
//				hurriedImgView.setVisibility(View.GONE);
//				hurriedImgView.setBackgroundResource(R.drawable.jiaoqi_selector);
//				
//				hurriedImgView.setOnClickListener(new View.OnClickListener() {				
//					@Override
//					public void onClick(View v) {						
//						if(mFoodsWithOffset.size() > 0){
//							new AlertDialog.Builder(getContext())
//								.setTitle("��ʾ")
//								.setMessage("ȷ��ȫ��������?")
//								.setNeutralButton("ȷ��", new DialogInterface.OnClickListener() {
//										@Override
//										public void onClick(DialogInterface dialog,	int which){
//											for(int i = 0; i < mFoodsWithOffset.size(); i++){
//												HashMap<String,Object> map =  mFoodsWithOffset.get(i);
//												OrderFood food = (OrderFood)map.get(KEY_THE_FOOD);
//												if(food.hangStatus == OrderFood.FOOD_NORMAL){
//													food.hangStatus = OrderFood.FOOD_HANG_UP;
//												}							
//											}
//											mAdapter.notifyDataSetChanged();
//										}
//									})
//									.setNegativeButton("ȡ��", null)
//									.show();	
//						}						
//					}
//				});
				
			}else{
				boolean hasHangupFood = false;
				for(int i = 0; i < mFoodsWithOffset.size(); i++){
					HashMap<String,Object> map =  mFoodsWithOffset.get(i);
					OrderFood food = (OrderFood)map.get(KEY_THE_FOOD);
					if(food.hangStatus == OrderFood.FOOD_HANG_UP){
						hasHangupFood = true;
						break;
					}
				}
				
				if(hasHangupFood){
					/**
					 * ���ȫ������ť
					 */
					ImageView immediateImgView = (ImageView)layout.findViewById(R.id.orderimage);
					immediateImgView.setVisibility(View.VISIBLE);
					immediateImgView.setBackgroundResource(R.drawable.jiqi_selector);
					immediateImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {						
							if(mFoodsWithOffset.size() > 0){
								new AlertDialog.Builder(getContext())
								.setTitle("��ʾ")
								.setMessage("ȷ��ȫ��������?")
								.setNeutralButton("ȷ��", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,	int which){
											for(int i = 0; i < mFoodsWithOffset.size(); i++){
												HashMap<String,Object> map =  mFoodsWithOffset.get(i);
												OrderFood food = (OrderFood)map.get(KEY_THE_FOOD);
												if(food.hangStatus == OrderFood.FOOD_HANG_UP){
													food.hangStatus = OrderFood.FOOD_IMMEDIATE;
												}								
											}
											mAdapter.notifyDataSetChanged();
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
					((ImageView)layout.findViewById(R.id.orderimage)).setVisibility(View.INVISIBLE);
				}
			}
			
//			if(isExpanded){
//				((ImageView)view.findViewById(R.id.arrow)).setBackgroundResource(R.drawable.point);
//			}else{
//				((ImageView)view.findViewById(R.id.arrow)).setBackgroundResource(R.drawable.point02);
//			}
			return layout;
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
	
	/*
	 * ��ʾ����ɾ��������Dialog
	 */
	private class AskCancelAmountDialog extends Dialog{
	
		AskCancelAmountDialog(final OrderFood oriFood, boolean isCancel) {
			super(OrderFoodListView.this.getContext(), R.style.FullHeightDialog);
			
			View view = LayoutInflater.from(getContext()).inflate(R.layout.alert, null);
			setContentView(view);
			//getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			
			((TextView)findViewById(R.id.table)).setText("������");
			//ɾ������Ĭ��Ϊ�˲�Ʒ�ĵ������
			final EditText cancelEdtTxt = (EditText)view.findViewById(R.id.mycount);			
			cancelEdtTxt.setText(NumericUtil.float2String2(oriFood.getCount()));
			//������ȫѡ
			cancelEdtTxt.selectAll();
			
			cancelEdtTxt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cancelEdtTxt.selectAll();
				}
			});
			
			//���������
           getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); 
           InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
           imm.showSoftInput(cancelEdtTxt, 0); //��ʾ�����
           imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			
			//"ȷ��"Button
			Button okBtn = (Button)view.findViewById(R.id.confirm);
			okBtn.setText("ȷ��");
			
			if(isCancel){
				((TextView)view.findViewById(R.id.ordername)).setText("������" + oriFood.getName() + "��ɾ������");
				okBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						try{
							float cancelAmount = Float.parseFloat(cancelEdtTxt.getText().toString());

							oriFood.removeCount(cancelAmount);
							//�µ���У������Ʒ����Ϊ��ģ���ɾ��
							if(mType == Type.INSERT_ORDER && oriFood.getCount() <= 0){
								mTmpOrder.remove(oriFood);
							}
							
							refreshOffsetFoods(mTmpOrder.foods);
							mAdapter.notifyDataSetChanged();
							
							dismiss();
							
						}catch(BusinessException e){
							Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
							
						}catch(NumberFormatException e){
							Toast.makeText(getContext(), "������ɾ����������ȷ", Toast.LENGTH_LONG).show();
						}

					}
				});
			}
			else {
				((TextView)view.findViewById(R.id.ordername)).setText("����" + oriFood.getName() + "������");
				okBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						float amount = Float.parseFloat(cancelEdtTxt.getText().toString());
						if(amount == 0f){
							Toast.makeText(OrderFoodListView.this.getContext(), "�������������ȷ", Toast.LENGTH_SHORT).show();
						} else {
							oriFood.setCount(amount);
							refreshOffsetFoods(mTmpOrder.foods);
							mAdapter.notifyDataSetChanged();
							
							dismiss();
						}
					}
				});
			}
			
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
	 * ��ʾ����������Dialog
	 */
	private class AskOrderAmountDialog extends Dialog{
	
		AskOrderAmountDialog(final OrderFood selectedFood) {
			super(OrderFoodListView.this.getContext(), R.style.FullHeightDialog);
			
			View view = LayoutInflater.from(getContext()).inflate(R.layout.alert, null);
			setContentView(view);
			//getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView)view.findViewById(R.id.ordername)).setText("������" + selectedFood.getName() + "������");
			
			((TextView)findViewById(R.id.table)).setText("������");
			//��������Ĭ��Ϊ�˲�Ʒ�ĵ������
			final EditText amountEdtTxt = (EditText)view.findViewById(R.id.mycount);
			amountEdtTxt.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					amountEdtTxt.selectAll();
				}
			});
			
			//"ȷ��"Button
			Button okBtn = (Button)view.findViewById(R.id.confirm);
			okBtn.setText("ȷ��");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try{
						float amount = Float.parseFloat(amountEdtTxt.getText().toString());
						if(amount > 255){
							Toast.makeText(getContext(), "�Բ���\"" + selectedFood.toString() + "\"���ֻ�ܵ�255��", Toast.LENGTH_SHORT).show();
						}else{
							selectedFood.setCount(amount);
							mAdapter.notifyDataSetChanged();
							Toast.makeText(getContext(), "����\"" + selectedFood.toString() + "\"" + "����Ϊ" + amount + "��", Toast.LENGTH_LONG).show();
							dismiss();
						}							
						
					}catch(NumberFormatException e){
						Toast.makeText(getContext(), "�������������ʽ����ȷ������������", Toast.LENGTH_SHORT).show();
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
			
			//���������
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); 
	        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	        imm.showSoftInput(amountEdtTxt, 0); //��ʾ�����
	        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		}		
	}
	
	/**
	 * �����Ʒ�б�����չ���� Dialog
	 */
	private class ExtOperDialg extends Dialog{

		
		ExtOperDialg(final OrderFood selectedFood) {
			super(OrderFoodListView.this.getContext(), R.style.FullHeightDialog);
			setContentView(R.layout.item_alert);
			getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView)findViewById(R.id.ordername)).setText("��ѡ��" + selectedFood.getName() + "�Ĳ���");
			if(mType == Type.INSERT_ORDER){
				/**
				 * �µ������չ����Ϊ"ɾ��"��"��ζ"��"����/ȡ������"����������
				 */
				//ɾ�˹���
				((TextView)findViewById(R.id.item1Txt)).setText("ɾ��");
				((RelativeLayout)findViewById(R.id.r1)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						dismiss();
						new AskCancelAmountDialog(selectedFood,true).show();							
					}
				});
				
				//��ζ����
				((TextView)findViewById(R.id.item2Txt)).setText("��ζ");
				((RelativeLayout)findViewById(R.id.r2)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						if(mOperListener != null){
							dismiss();
							mOperListener.onPickTaste(selectedFood);
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
				
				//����
				((RelativeLayout)findViewById(R.id.r4)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						dismiss();
						new AskOrderAmountDialog(selectedFood).show();							
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
						if(WirelessOrder.restaurant.pwd5 != null){
							new AskPwdDialog(getContext(), AskPwdDialog.PWD_5){							
								@Override
								protected void onPwdPass(Context context){
									dismiss();
									new AskCancelAmountDialog(selectedFood,true).show();
								}
							}.show();
						}else{
							new AskCancelAmountDialog(selectedFood,true).show(); 
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
					
					((ImageView)findViewById(R.id.line3)).setVisibility(View.GONE);
					((ImageView)findViewById(R.id.line4)).setVisibility(View.GONE);
					((RelativeLayout)findViewById(R.id.r3)).setVisibility(View.GONE);
					//����
					((RelativeLayout)findViewById(R.id.r4)).setVisibility(View.GONE);
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
			//trim(_selectedFood);
			mAdapter.notifyDataSetChanged();
		}		
	}
	public void setAllMarkClickListener(AllMarkClickListener l){
		mAllMarkClickListener = l;
	}
	
	public static interface OnChangedListener{
		public void onSourceChanged();
	}
	
	public static interface OnOperListener{
		public void onPickTaste(OrderFood selectedFood);
		public void onPickFood();
	}
	
	public static interface AllMarkClickListener{
		void allMarkClick();
	}
	
}
