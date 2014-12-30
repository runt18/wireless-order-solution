package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.lib.task.TransOrderFoodTask;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.parcel.TasteGroupParcel;
import com.wireless.pojo.dishesOrder.ComboOrderFood;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.menuMgr.ComboFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.PickFoodActivity;
import com.wireless.ui.PickTasteActivity;
import com.wireless.ui.R;
import com.wireless.ui.dialog.AddOrderAmountDialog;
import com.wireless.ui.dialog.AddOrderAmountDialog.OnAmountAddedListener;
import com.wireless.ui.dialog.AskCancelAmountDialog;
import com.wireless.ui.dialog.AskCancelAmountDialog.OnCancelAmountChangedListener;
import com.wireless.ui.dialog.AskOrderAmountDialog;
import com.wireless.ui.dialog.AskOrderAmountDialog.ActionType;
import com.wireless.ui.dialog.AskOrderAmountDialog.OnFoodPickedListener;
import com.wireless.ui.dialog.AskTableDialog;
import com.wireless.ui.dialog.AskTableDialog.OnTableSelectedListener;
import com.wireless.ui.dialog.SetOrderAmountDialog;
import com.wireless.ui.dialog.SetOrderAmountDialog.OnAmountChangedListener;

public class OrderFoodFragment extends Fragment implements OnCancelAmountChangedListener,
														   OnAmountChangedListener,
														   OnAmountAddedListener,
														   OnFoodPickedListener,
														   OnTableSelectedListener{

	public static interface OnButtonClickedListener{
		public void onPickFoodClicked();
	}
	
	public static interface OnOrderChangedListener{
		public void onOrderChanged(Order oriOrder, List<OrderFood> newFoodList);
	}
	
	public static interface OnCommitListener{
		public void preCommit();
		public void postSuccess(Order order);
		public void postFailed(BusinessException e, Order order);
	}
	
	private OnButtonClickedListener mBtnClickedListener;
	
	private OnOrderChangedListener mOrderChangedListener;
	
	private OnCommitListener mCommitListener;
	
	public final static int PICK_FOOD = 0;
	public final static int PICK_TASTE = 1;
	public final static int PICK_ALL_FOOD_TASTE = 2;
	
	public final static String TAG = "OrderFoodFragment";
	
	// �б������ʾ��ǩ
	private static final String ITEM_FOOD_NAME = "item_food_name";
	private static final String ITEM_FOOD_PRICE = "item_new_food_price";
	private static final String ITEM_FOOD_AMOUNT = "item_food_count";
	private static final String ITEM_FOOD_DELTA = "item_food_delta";
	private static final String ITEM_FOOD_TASTE = "item_food_taste";
	private static final String ITEM_THE_FOOD = "item_the_food";
	private static final String ITEM_IS_ORI_FOOD = "item_is_original";
	private static final String ITEM_IS_DELTA = "item_is_delta";
	
	private static final String ITEM_GROUP_NAME = "item_group_name";
	
	//�Ƿ���ȫ������
	private boolean isHangUp = false;
	//�Ƿ��ѷ�ϯ
	private boolean isMulti = false;
	
	private QueryOrderTask mQueryOrderTask;
	
	private FoodListHandler mFoodListHandler;
	
	//ȫ����ע�Ŀ�ζ
	private List<Taste> mAllFoodTastes = new ArrayList<Taste>();
	
	//�µ��Ʒ��Ϣ
	private List<OrderFood> mNewFoodList = new ArrayList<OrderFood>();
	
	//ѡ��Ҫ�����Ĳ�Ʒ
	private OrderFood mSelectedFood;
	
	//Ҫת�˵Ĳ�Ʒ
	private List<OrderFood> mTransFoods = new ArrayList<OrderFood>();
	
	//�ѵ��Ʒ��Ϣ
	private Order mOriOrder;
	
	private final static String TBL_ALIAS_KEY = "TableAliasKey";

	/*
	 * ��ʾ��˵��б��handler ������µ�˵���ʾ
	 */
	private static class FoodListHandler extends Handler {
		
		private static final String[] ITEM_TAGS = { 
			ITEM_FOOD_NAME,
			ITEM_FOOD_AMOUNT, 
			ITEM_FOOD_PRICE,
			ITEM_FOOD_TASTE
		};
		
		private static final int[] ITEM_TARGETS = {
			R.id.txtView_foodName_orderChildItem,		//����
			R.id.txtView_amountValue_orderChildItem,	//����
			R.id.txtView_priceValue_orderChildItem,		//��Ǯ
			R.id.txtView_taste_orderChildItem			//��ζ��ʾ
		};
		
		private static final String[] GROUP_ITEM_TAGS = { ITEM_GROUP_NAME };
		private static final int[] GROUP_ITEM_ID = { R.id.txtView_name_orderDropGroup };
		
		private WeakReference<OrderFoodFragment> mFragment;

		FoodListHandler(OrderFoodFragment fragment) {
			mFragment = new WeakReference<OrderFoodFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			OrderFoodFragment ofFgm = mFragment.get();
			
			List<Map<String, ?>> groupData = new ArrayList<Map<String, ?>>();
			List<List<Map<String, ?>>> childData =  new ArrayList<List<Map<String, ?>>>();
			
			Map<String, Object> groupMap = new HashMap<String, Object>();
			groupMap.put(ITEM_GROUP_NAME, "�µ��");
			groupData.add(groupMap);
			
			List<Map<String, ?>> newFoodDatas = new ArrayList<Map<String,?>>();
			if(!ofFgm.mNewFoodList.isEmpty()){
				for(OrderFood f : ofFgm.mNewFoodList){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(ITEM_FOOD_NAME, f.getName());
					map.put(ITEM_FOOD_AMOUNT, String.valueOf(f.getCount()));
					map.put(ITEM_FOOD_PRICE, NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(f.calcPriceBeforeDiscount()));
					map.put(ITEM_FOOD_TASTE, f.getTasteGroup().getPreference());
					map.put(ITEM_THE_FOOD, f);
					newFoodDatas.add(map);
				}
			}
			childData.add(newFoodDatas);

			//������ѵ��
			if(ofFgm.mOriOrder != null && ofFgm.mOriOrder.hasOrderFood()){
				
				groupMap = new HashMap<String, Object>();
				groupMap.put(ITEM_GROUP_NAME, "�ѵ��");
				groupMap.put(ITEM_IS_ORI_FOOD, true);
				groupData.add(groupMap);
				
				//���˲˺ͼӲ˵Ĳ�Ʒ��ʾ��������
				Comparator<OrderFood> comp = new Comparator<OrderFood>(){
					@Override
					public int compare(OrderFood lhs, OrderFood rhs) {
						if(lhs.getDelta() != 0 && rhs.getDelta() != 0){
							if(lhs.getDelta() < rhs.getDelta()){
								return -1;
							}else if(lhs.getDelta() > rhs.getDelta()){
								return 1;
							}else{
								return 0;
							}
						}else if(lhs.getDelta() != 0 && rhs.getDelta() == 0){
							return -1;
						}else if(lhs.getDelta() == 0 && rhs.getDelta() != 0){
							return 1;
						}else{
							if(lhs.getId() < rhs.getId()){
								return -1;
							}else if(lhs.getId() > rhs.getId()){
								return 1;
							}else{
								return 0;
							}
						}
					}
				};
				
				List<Map<String, ?>> pickedFoodDatas = new ArrayList<Map<String, ?>>();
				for(OrderFood of : ofFgm.mOriOrder.getOrderFoods(comp)){
					if(of.getCount() > 0f){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put(ITEM_IS_ORI_FOOD, true);
						map.put(ITEM_FOOD_NAME, of.getName());
						map.put(ITEM_FOOD_AMOUNT, String.valueOf(of.getCount()));
						map.put(ITEM_FOOD_PRICE, NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(of.calcPriceBeforeDiscount()));
						map.put(ITEM_FOOD_TASTE, of.getTasteGroup().getPreference());
						map.put(ITEM_THE_FOOD, of);
						map.put(ITEM_FOOD_DELTA, Float.valueOf(of.getDelta()));
						pickedFoodDatas.add(map);
					}
					
					if(of.getDelta() > 0f){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put(ITEM_IS_ORI_FOOD, true);
						map.put(ITEM_FOOD_NAME, of.getName()); 
						map.put(ITEM_FOOD_AMOUNT, String.valueOf(of.getCount()));
						map.put(ITEM_FOOD_PRICE, NumericUtil.float2String2(of.calcUnitPrice() * Math.abs(of.getDelta())));
						map.put(ITEM_FOOD_TASTE, of.hasCancelReason() ? of.getCancelReason().getReason() : "���˲�ԭ��");
						map.put(ITEM_THE_FOOD, of);
						map.put(ITEM_FOOD_DELTA, Float.valueOf(of.getDelta()));
						map.put(ITEM_IS_DELTA, true);
						pickedFoodDatas.add(map);
						
					}else if(of.getDelta() < 0f){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put(ITEM_IS_ORI_FOOD, true);
						map.put(ITEM_FOOD_NAME, of.getName()); 
						map.put(ITEM_FOOD_AMOUNT, String.valueOf(of.getCount()));
						map.put(ITEM_FOOD_PRICE, NumericUtil.float2String2(of.calcUnitPrice() * Math.abs(of.getDelta())));
						map.put(ITEM_FOOD_TASTE, "��" + NumericUtil.float2String2(Math.abs(of.getDelta())) + "��");
						map.put(ITEM_THE_FOOD, of);
						map.put(ITEM_FOOD_DELTA, Float.valueOf(of.getDelta()));
						map.put(ITEM_IS_DELTA, true);
						pickedFoodDatas.add(map);
					}
					
				}
				childData.add(pickedFoodDatas);
				
			}
			
			ExpandableListView xplv = (ExpandableListView) mFragment.get().getView().findViewById(R.id.expandableListView_orderActivity);
			FoodExpandableAdapter adapter = ofFgm.new FoodExpandableAdapter(ofFgm.getActivity(), 
						groupData, R.layout.order_activity_drop_group, GROUP_ITEM_TAGS, GROUP_ITEM_ID, 
						childData, R.layout.order_activity_child_item, ITEM_TAGS, ITEM_TARGETS);
				
			xplv.setAdapter(adapter);
			
			for(int i = 0; i < groupData.size(); i++){
				xplv.expandGroup(i);
			}
			
			calcTotal();
			
			//�ص�֪ͨOrder�����仯
			if(mFragment.get().mOrderChangedListener != null){
				mFragment.get().mOrderChangedListener.onOrderChanged(mFragment.get().mOriOrder,
																	 mFragment.get().mNewFoodList);
			}
		}
		
		private void calcTotal(){
			OrderFoodFragment fragment = mFragment.get();
			
			float totalPrice = 0;
			if(!fragment.mNewFoodList.isEmpty()){
				totalPrice += new Order(fragment.mNewFoodList).calcPriceBeforeDiscount();
				((TextView) fragment.getView().findViewById(R.id.textView_orderActivity_newCount)).setText(String.valueOf(fragment.mNewFoodList.size()));
			}
			if(fragment.mOriOrder != null && fragment.mOriOrder.hasOrderFood()){
				totalPrice += fragment.mOriOrder.calcPriceBeforeDiscount();
				((TextView) fragment.getView().findViewById(R.id.textView_orderActivity_pickedCount)).setText(String.valueOf(fragment.mOriOrder.getOrderFoods().size()));
			}
			
			((TextView) fragment.getView().findViewById(R.id.textView_orderActivity_sumPirce)).setText(NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(totalPrice));
		}
	}
	
	
	private class FoodExpandableAdapter extends SimpleExpandableListAdapter{
		
		private List<? extends Map<String, ?>> mGroupData;
		private List<? extends List<? extends Map<String, ?>>> mChildData;
		private PopupWindow mPopup;
		
		public FoodExpandableAdapter(Context context,
									 List<? extends Map<String, ?>> groupData, int groupLayout,	String[] groupFrom, int[] groupTo,
									 List<? extends List<? extends Map<String, ?>>> childData, int childLayout, String[] childFrom, int[] childTo) {
			super(context, 
				  groupData, groupLayout, groupFrom, groupTo, 
				  childData, childLayout, childFrom, childTo);
			mGroupData = groupData;
			mChildData = childData;
		}
		
		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			final View layout = super.getChildView(groupPosition, childPosition, isLastChild,	convertView, parent);;
			
			Map<String, ?> map = mChildData.get(groupPosition).get(childPosition);
			final OrderFood of = (OrderFood) map.get(ITEM_THE_FOOD);
			layout.setTag(map);
			
			StringBuilder detail = new StringBuilder();
			
			if(of.asFood().isCombo()){
				detail.append("(��)");
			}
			if(of.isHurried()){
				detail.append("(��)");
			}
			if(of.isTemp()){
				detail.append("(��)");
			}
			if(of.isHangup()){
				detail.append("(��)");
			}
			if(of.isGift()){
				detail.append("(��)");
			}
			
			detail.append(of.getName());
			
			//show the name to each food
			StringBuilder status = new StringBuilder();
			if(of.asFood().isSpecial()){
				status.append("��");
			}
			if(status.length() != 0){
				status.insert(0, "(").append(")");
			}
			
			detail.append(status);
			//��ʾ����
			((TextView)layout.findViewById(R.id.txtView_foodName_orderChildItem)).setText(detail);
			
			if(of.asFood().isCombo()){
				LinearLayout comboLinearLayout = (LinearLayout)layout.findViewById(R.id.linearLayout_comboList_orderChildItem);
				comboLinearLayout.removeAllViews();
				comboLinearLayout.setVisibility(View.VISIBLE);
				for(ComboFood comboFood : of.asFood().getChildFoods()){
					ComboOrderFood cof = null;
					for(ComboOrderFood each : of.getCombo()){
						if(each.asComboFood().equals(comboFood)){
							cof = each;
							break;
						}
					}
					TextView tv = new TextView(getActivity());
					tv.setTextColor(getResources().getColor(R.color.blue));
					tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
					tv.setText("���D " + (cof != null ? cof.toString() : comboFood.getName()));
					comboLinearLayout.addView(tv);
				}
			}else{
				((LinearLayout)layout.findViewById(R.id.linearLayout_comboList_orderChildItem)).setVisibility(View.GONE);
			}
			
			//������µ��
			if(!map.containsKey(ITEM_IS_ORI_FOOD)){
				
				layout.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						AskOrderAmountDialog.newInstance(of, ActionType.MODIFY, getId()).show(getFragmentManager(), AskOrderAmountDialog.TAG);
					}
				});
				
				//"��ζ"����			 
				ImageView delFoodImgView = (ImageView)layout.findViewById(R.id.imgView_left_orderFoodListView_childItem);
				delFoodImgView.setTag(of);
				delFoodImgView.setBackgroundResource(R.drawable.taste_word_selector);
				
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(View v) {
						mSelectedFood = (OrderFood) v.getTag();
						if(mSelectedFood.isTemp()){
							Toast.makeText(getActivity(), "��ʱ�˲�����ӿ�ζ", Toast.LENGTH_SHORT).show();
						}else{
							Intent intent = new Intent(getActivity(), PickTasteActivity.class);
							Bundle bundle = new Bundle(); 
							bundle.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(mSelectedFood));
							intent.putExtras(bundle);
							startActivityForResult(intent, PICK_TASTE);
						}
					}
				};
				
				delFoodImgView.setOnClickListener(listener);
	
				//"����"����
				ImageView amountImgView = (ImageView)layout.findViewById(R.id.imgView_right_orderFoodListView_childItem);
				if(of.asFood().isWeigh()){
					amountImgView.setBackgroundResource(R.drawable.weight_selector);
				}else{
					amountImgView.setBackgroundResource(R.drawable.amount_selector);
				}
				amountImgView.setTag(of);
				amountImgView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						SetOrderAmountDialog.newInstance((OrderFood)v.getTag(), OrderFoodFragment.this.getId()).show(getFragmentManager(), SetOrderAmountDialog.TAG);
					}
				});
				
			}else {//�ѵ��
				
				//"�˲�"Button
				ImageView cancelImgView = (ImageView) layout.findViewById(R.id.imgView_left_orderFoodListView_childItem);
				cancelImgView.setBackgroundResource(R.drawable.tuicai_selector);
				
				//"�Ӳ�"Button
				ImageView addImgView = (ImageView) layout.findViewById(R.id.imgView_right_orderFoodListView_childItem);
				if(of.asFood().isWeigh()){
					addImgView.setBackgroundResource(R.drawable.weight_selector);
				}else{
					addImgView.setBackgroundResource(R.drawable.amount_selector);
				}
				
				//"ȡ���˲�"or"ȡ���Ӳ�"Button
				Button restoreBtn = (Button) layout.findViewById(R.id.button_orderFoodListView_childItem_restore);
				
				//Check if delta exist
				if(map.containsKey(ITEM_IS_DELTA)){
					//delta > 0 ��ʾ���˲�
					if((Float)map.get(ITEM_FOOD_DELTA) > 0f){
						cancelImgView.setVisibility(View.INVISIBLE);
						addImgView.setVisibility(View.INVISIBLE);
						
						((TextView) layout.findViewById(R.id.txtView_amountValue_orderChildItem)).setText(NumericUtil.float2String2(of.getDelta()));
						layout.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.VISIBLE);
						//'ȡ���˲�'��ť
						restoreBtn.setText("ȡ���˲�");
						restoreBtn.setVisibility(View.VISIBLE); 
						restoreBtn.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								of.setCount(of.getCount() + of.getDelta());
								of.setCancelReason(null);
								mFoodListHandler.sendEmptyMessage(0);
							}
						});
						
					}else if((Float)map.get(ITEM_FOOD_DELTA) < 0f){
						//delta < 0 ��ʾ�ǼӲ�
						cancelImgView.setVisibility(View.INVISIBLE);
						addImgView.setVisibility(View.INVISIBLE);
						
						//layout.setBackgroundColor(Color.LTGRAY);
						((TextView)layout.findViewById(R.id.txtView_taste_orderChildItem)).setTextColor(getResources().getColor(R.color.green));
						((TextView) layout.findViewById(R.id.txtView_amountValue_orderChildItem)).setText(NumericUtil.float2String2(Math.abs(of.getDelta())));
						layout.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.INVISIBLE);
						//'ȡ���Ӳ�'��ť
						restoreBtn.setText("ȡ���Ӳ�");
						restoreBtn.setVisibility(View.VISIBLE); 
						restoreBtn.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								of.setCount(of.getCount() - Math.abs(of.getDelta()));
								mFoodListHandler.sendEmptyMessage(0);
							}
						});
					}
				
				} else {
					//Delta�����ڱ�ʾ��Ʒû�б仯
					
					if((Float)map.get(ITEM_FOOD_DELTA) < 0f){
						//delta < 0 ��ʾ�ǼӲ�, ֻ��ʾ�Ӳ�Button
						cancelImgView.setVisibility(View.INVISIBLE);
						addImgView.setVisibility(View.VISIBLE);
						
					}else if((Float)map.get(ITEM_FOOD_DELTA) > 0f){
						//delta > 0 ��ʾ���˲�, ֻ��ʾ�˲�Button
						cancelImgView.setVisibility(View.VISIBLE);
						addImgView.setVisibility(View.INVISIBLE);
					}else{
						//delta == 0��ʾ��ͨ״̬, ��ʾ��/�˲�Button
						cancelImgView.setVisibility(View.VISIBLE);
						addImgView.setVisibility(View.VISIBLE);
					}
					
					restoreBtn.setVisibility(View.INVISIBLE);
					
					//show the order amount to each food
					((TextView) layout.findViewById(R.id.txtView_amountValue_orderChildItem)).setText(NumericUtil.float2String2(of.getCount()));
					layout.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.INVISIBLE);
					//"�˲�"����
					cancelImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							AskCancelAmountDialog.newInstance(of, getId()).show(getFragmentManager(), AskCancelAmountDialog.TAG);
						}
					});
					
					//"�Ӳ�"����
					addImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							AddOrderAmountDialog.newInstance(of, getId()).show(getFragmentManager(), AddOrderAmountDialog.TAG);
						}
					});
					
					//"�߲�"����
					layout.setOnClickListener(new OnClickListener(){
						
						@Override
						public void onClick(View v) {
							new AlertDialog
							   .Builder(getActivity())
							   .setTitle(of.getName())
							   .setItems(new String[] { of.isHurried() ? "ȡ���߲�" : "�߲�", "ת��" }, new DialogInterface.OnClickListener(){

								@Override
								public void onClick(DialogInterface dialog, int which) {
									mSelectedFood = of;
									if(which == 0){
										//�߲�
										if(mSelectedFood.isHurried()){
											mSelectedFood.setHurried(false);
										}else{
											mSelectedFood.setHurried(true);
											Toast.makeText(getActivity(), "�߲˳ɹ�", Toast.LENGTH_SHORT).show();	
										}
									}else if(which == 1){
										mTransFoods.clear();
										mTransFoods.add(mSelectedFood);
										AskTableDialog.newInstance(getId()).show(getFragmentManager(), AskTableDialog.TAG);
									}
									mFoodListHandler.sendEmptyMessage(0);
								}
								   
							   }).setNegativeButton("����", null).show();
							
						}
						
					});
				}
			}
			return layout;
		}
	
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,	View convertView, ViewGroup parent) {
			final View layout = super.getGroupView(groupPosition, isExpanded, convertView, parent);
			
			Map<String, ?> map = mGroupData.get(groupPosition);
			
			if(map.containsKey(ITEM_IS_ORI_FOOD)){
				
				/**
				 * �ѵ�˵�Group����Ҫ��ʾButton
				 */
				layout.findViewById(R.id.button_orderActivity_opera).setVisibility(View.GONE);
				((ImageView)layout.findViewById(R.id.imgView_right_orderDropGroup)).setVisibility(View.INVISIBLE);
				((ImageView) layout.findViewById(R.id.imgView_left_orderDropGroup)).setVisibility(View.INVISIBLE);
				
			}else{
	
				/**
				 * �µ�˵�Group��ʾ"���"��"ȫ��"Button
				 */
				//���Button
				ImageView orderImg = (ImageView)layout.findViewById(R.id.imgView_right_orderDropGroup);
				orderImg.setVisibility(View.VISIBLE);
				orderImg.setBackgroundResource(R.drawable.order_selector);
				
				orderImg.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						if(mBtnClickedListener != null){
							mBtnClickedListener.onPickFoodClicked();
						}else{
							// ��ת��ѡ��Activity
							Intent intent = new Intent(getActivity(), PickFoodActivity.class);
							startActivityForResult(intent, PICK_FOOD);
						}
					}
				});
				
				//����'��ע', '����', '��ϯ'��Button
				createPopup(parent);
				
				//ȫ��Button
				View orderOperateBtn = layout.findViewById(R.id.button_orderActivity_opera);
				orderOperateBtn.setVisibility(View.VISIBLE);
				orderOperateBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mPopup.showAsDropDown(v, -10, 0);
					}
				});
	
			}
			return layout;
		}
	
		private void createPopup(ViewGroup parent){
			View popupLayout = getActivity().getLayoutInflater().inflate(R.layout.order_activity_operate_popup, parent, false);
			//ȫ������Button
			Button hangUpBtn = (Button) popupLayout.findViewById(R.id.button_orderActivity_operate_popup_callUp);
			if(isHangUp){
				hangUpBtn.setText("ȡ������");
			} else{
				hangUpBtn.setText("����");
			}
			
			//��ϯButton
			final Button multiBtn = ((Button)popupLayout.findViewById(R.id.button_orderActivity_operate_popup_multi));
			if(isMulti){
				multiBtn.setText("ȡ����ϯ");
			}else{
				multiBtn.setText("��ϯ");
			}
			
			if(mPopup == null){
				
				mPopup = new PopupWindow(popupLayout, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				mPopup.setOutsideTouchable(true);
				mPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_small));
				mPopup.update();
				
				//ȫ������Button
				hangUpBtn.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						if(isHangUp){
							for(int i = 0; i < mNewFoodList.size(); i++){
								mNewFoodList.get(i).setHangup(false);
							}
							isHangUp = false; 
							mPopup.dismiss();
							mFoodListHandler.sendEmptyMessage(0);
						}
						else if(mNewFoodList.size() > 0){
							new AlertDialog.Builder(getActivity())
								.setTitle("��ʾ")
								.setMessage("ȷ��ȫ��������?")
								.setNeutralButton("ȷ��", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,	int which){
											for(int i = 0; i < mNewFoodList.size(); i++){
												mNewFoodList.get(i).setHangup(true);
											}
											isHangUp = true;
											mFoodListHandler.sendEmptyMessage(0);
											mPopup.dismiss();
										}
									})
									.setNegativeButton("ȡ��", null)
									.show();	
						}	
						else {
							Toast.makeText(getActivity(), "û���µ�ˣ��޷�����", Toast.LENGTH_SHORT).show();
						}
					}
				});
				
				//ȫ����עButton
				Button allRemarkBtn = (Button) popupLayout.findViewById(R.id.button_orderActivity_operate_popup_remark);
				allRemarkBtn.setText("��ע");
				allRemarkBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if(!mNewFoodList.isEmpty()){
							Intent intent = new Intent(getActivity(), PickTasteActivity.class);
							Bundle bundle = new Bundle(); 
							OrderFood dummyFood = new OrderFood();
							dummyFood.asFood().setName("ȫ����ע");
							for(Taste t : mAllFoodTastes){
								dummyFood.addTaste(t);
							}
							bundle.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(dummyFood));
							bundle.putInt(PickTasteActivity.PICK_TASTE_INIT_FGM, PickTasteActivity.POP_TASTE_FRAGMENT);
							bundle.putBoolean(PickTasteActivity.PICK_ALL_ORDER_TASTE, true);
							intent.putExtras(bundle);
							startActivityForResult(intent, PICK_ALL_FOOD_TASTE);
							mPopup.dismiss();
						} else {
							Toast.makeText(getActivity(), "�˲�̨��δ��ˣ��޷���ӱ�ע", Toast.LENGTH_SHORT).show();
						}
					}
				});
				
				//��ϯButton
				multiBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(mNewFoodList.isEmpty()){
							Toast.makeText(getActivity(), "����û�е��", Toast.LENGTH_SHORT).show();
						}else{
							if(isMulti == false){
								
								final EditText edtTextMulti = new EditText(getActivity());
								edtTextMulti.setKeyListener(new DigitsKeyListener(false, false));
								
								Dialog dialog = new AlertDialog.Builder(getActivity()).setTitle("�������ϯ����")
									.setIcon(android.R.drawable.ic_dialog_info)
									.setView(edtTextMulti)
									.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){
										@Override
										public void onClick(DialogInterface dialog, int which) {
											int amount = 0;
											try{
												amount = Integer.parseInt(edtTextMulti.getText().toString());
												if(amount > 1){
													for(OrderFood of : mNewFoodList){
														of.addCount(of.getCount() * (amount - 1));
														if(of.hasTmpTaste()){
															Taste tmpTaste = of.getTasteGroup().getTmpTaste();
															tmpTaste.setPreference((tmpTaste.getPreference().length() != 0 ? tmpTaste.getPreference() + "," : "") + ("��" + amount + "ϯ��"));
														}else{
															of.setTmpTaste(Taste.newTmpTaste("��" + amount + "ϯ��", 0));
														}
													}
													//����״̬Ϊ'�ѷ�ϯ'
													isMulti = true;
													mFoodListHandler.sendEmptyMessage(0);
												}

											}catch(NumberFormatException e){
												Toast.makeText(getActivity(), "������ķ�ϯ��������ȷ", Toast.LENGTH_SHORT).show();
											} 
										}
		
									})
									.setNegativeButton("ȡ��", null).show();		
								
								mPopup.dismiss();
								
								//ֻ��������һ�е����Ի���ʱ��Ҫ����������ܵ��������
								dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
								//����������һ�е����Ի���ʱ�������֮����
								dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
								
							}else{
								for(OrderFood of : mNewFoodList){
									of.setCount(of.getCount() - Math.abs(of.getDelta()));
									if(of.hasTmpTaste()){
										Taste tmpTaste = of.getTasteGroup().getTmpTaste();
										tmpTaste.setPreference(tmpTaste.getPreference().replaceAll("��.ϯ��", ""));
										if(tmpTaste.getPreference().length() == 0){
											of.setTmpTaste(null);
										}
									}
								}
								//����״̬Ϊ'δ��ϯ'
								isMulti = false;
								mFoodListHandler.sendEmptyMessage(0);
								mPopup.dismiss();
							}
						}

					}
				});
				
				if(mOriOrder != null){
					//ȫ��ת��Button
					popupLayout.findViewById(R.id.button_orderActivity_operate_popup_transfer).setOnClickListener(new View.OnClickListener() {
	
						@Override
						public void onClick(View arg0) {
							mTransFoods.clear();
							mTransFoods.addAll(mOriOrder.getOrderFoods());
							AskTableDialog.newInstance(getId()).show(getFragmentManager(), AskTableDialog.TAG);
							mPopup.dismiss();
						}
						
					});
				}else{
					popupLayout.findViewById(R.id.button_orderActivity_operate_popup_transfer).setVisibility(View.GONE);
				}
				
			}
		}
	}
	
	public static OrderFoodFragment newInstance(){
		OrderFoodFragment fgm = new OrderFoodFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(TBL_ALIAS_KEY, Integer.MIN_VALUE);
		fgm.setArguments(bundle);
		return fgm;
	}
		
	public static OrderFoodFragment newInstance(int tableAlias){
		OrderFoodFragment fgm = new OrderFoodFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(TBL_ALIAS_KEY, tableAlias);
		fgm.setArguments(bundle);
		return fgm;
	}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		
		try{
			mBtnClickedListener = (OnButtonClickedListener)activity;
		}catch(ClassCastException ignored){}
		
		try{
			mOrderChangedListener = (OnOrderChangedListener)activity;
		}catch(ClassCastException ignored){}
		
		try{
			mCommitListener = (OnCommitListener)activity;
		}catch(ClassCastException ignored){}
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		mFoodListHandler = new FoodListHandler(this);
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		return inflater.inflate(R.layout.order_food_activity, container, false);
	}

	
	@Override
	public void onActivityCreated (Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		//ִ��������¹����Ʒ
		new QuerySellOutTask().execute();
		
		Bundle bundle = getArguments();
		if(bundle != null){
			int tableAlias = bundle.getInt(TBL_ALIAS_KEY);
			if(tableAlias >= 0){
				mQueryOrderTask = new QueryOrderTask(tableAlias);
				mQueryOrderTask.execute();
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mQueryOrderTask != null){
			mQueryOrderTask.cancel(true);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode == Activity.RESULT_OK){			
			if(requestCode == OrderFoodFragment.PICK_TASTE){
				//��ζ�ı�ʱ֪ͨListView���и���
				TasteGroupParcel tgParcel = data.getParcelableExtra(TasteGroupParcel.KEY_VALUE);
				if(tgParcel.asTasteGroup() != null){
					mSelectedFood.setTasteGroup(tgParcel.asTasteGroup());
				}else{
					mSelectedFood.clearTasetGroup();
				}
				
			}else if(requestCode == OrderFoodFragment.PICK_FOOD){
				//ѡ�˸ı�ʱ֪ͨ�µ�˵�ListView���и���
				OrderParcel orderParcel = data.getParcelableExtra(OrderParcel.KEY_VALUE);
				mNewFoodList.addAll(orderParcel.asOrder().getOrderFoods());
				
			}else if(requestCode == PICK_ALL_FOOD_TASTE){
				//ȫ����ע�ı�ʱ���������µ�˵Ŀ�ζ
				for(Taste t : mAllFoodTastes){
					for(OrderFood of : mNewFoodList){
						if(!of.isTemp()){
							of.removeTaste(t);
						}
					}
				}
				
				mAllFoodTastes.clear();
				
				TasteGroupParcel tgParcel = data.getParcelableExtra(TasteGroupParcel.KEY_VALUE);
				if(tgParcel.asTasteGroup() != null){
					mAllFoodTastes.addAll(tgParcel.asTasteGroup().getNormalTastes());
					//Ϊ�����µ����ӿ�ζ,��ʱ�˳���
					for(OrderFood of : mNewFoodList){
						if(!of.isTemp()){
							for(Taste taste : mAllFoodTastes){
								of.addTaste(taste);
							}
						}
					}
				}
			}
			mFoodListHandler.sendEmptyMessage(0);
		}
	}
	
	private class CommitOrderTask extends com.wireless.lib.task.CommitOrderTask{

		public CommitOrderTask(Staff staff, Order.InsertBuilder builder, PrintOption printOption) {
			super(staff, builder, printOption);
		}

		public CommitOrderTask(Staff staff, Order.UpdateBuilder builder, PrintOption printOption) {
			super(staff, builder, printOption);
		}
		
		@Override
		protected void onPreExecute() {
			if(mCommitListener != null){
				mCommitListener.preCommit();
			}
		}
		
		@Override
		protected void onSuccess(Order reqOrder) {
			if(mCommitListener != null){
				mCommitListener.postSuccess(reqOrder);
			}			
		}

		@Override
		protected void onFail(BusinessException e, Order reqOrder) {
			if(mCommitListener != null){
				mCommitListener.postFailed(e, reqOrder);
			}			
		}
		
	}
	
	public void commitForce(Table.AliasBuilder tblBuilder, int customNum, PrintOption printOption) throws BusinessException{
		new CommitOrderTask(WirelessOrder.loginStaff, 
						    new Order.InsertBuilder(tblBuilder).addAll(mNewFoodList, WirelessOrder.loginStaff).setCustomNum(customNum).setForce(true),
						    printOption).execute();
	}
	
	public void commit(Table.AliasBuilder tblBuilder, int customNum, PrintOption printOption) throws BusinessException{
		if(mOriOrder != null){
			//�ĵ�
			new CommitOrderTask(WirelessOrder.loginStaff, 
								new Order.UpdateBuilder(mOriOrder.getId(), mOriOrder.getOrderDate())
										 .addAll(mNewFoodList, WirelessOrder.loginStaff)
										 .addAll(mOriOrder.getOrderFoods(), WirelessOrder.loginStaff)
										 .setCustomNum(customNum),
								printOption).execute();
		}else{
			//���µ�
			new CommitOrderTask(WirelessOrder.loginStaff, 
								new Order.InsertBuilder(tblBuilder).addAll(mNewFoodList, WirelessOrder.loginStaff).setCustomNum(customNum),
								printOption).execute();
		}
	}
	
	public List<OrderFood> getOriFoods(){
		if(mOriOrder != null){
			return mOriOrder.getOrderFoods();
		}else{
			return Collections.emptyList();
		}
	}
	
	public List<OrderFood> getNewFoods(){
		return Collections.unmodifiableList(mNewFoodList);
	}
	
	public boolean hasOrderFood(){
//		if(mOriOrder != null){
//			for(OrderFood of : mOriOrder.getOrderFoods()){
//				if(of.getCount() > 0){
//					return true;
//				}
//			}
//		}
//
//		for(OrderFood of : mNewFoodList){
//			if(of.getCount() > 0){
//				return true;
//			}
//		}
		
		if(mOriOrder != null && !mOriOrder.getOrderFoods().isEmpty()){
			return true;
		}else if(!mNewFoodList.isEmpty()){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean hasOriginalOrder(){
		return mOriOrder != null;
	}
	
	public boolean hasNewOrderFood(){
		return !mNewFoodList.isEmpty();
	}
	
	/**
	 * Refresh the original order.
	 */
	public void refresh(){
		int tableAlias = getArguments().getInt(TBL_ALIAS_KEY);
		if(tableAlias >= 0){
			mQueryOrderTask = new QueryOrderTask(tableAlias);
			mQueryOrderTask.execute();
		}
	}
	
	/**
	 * Reset the original order.
	 */
	public void reset(){
		mOriOrder = null;
		mFoodListHandler.sendEmptyMessage(0);
	}
	
	public void addFood(OrderFood of){
		mNewFoodList.add(of);
		mFoodListHandler.sendEmptyMessage(0);
	}
	
	public void addFoods(List<OrderFood> ofList){
		mNewFoodList.addAll(ofList);
		mFoodListHandler.sendEmptyMessage(0);
	}
	
	/**
	 * ִ�������Ӧ��̨���˵���Ϣ 
	 */
	private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{
		private ProgressDialog mProgressDialog;
		private final int mTblAlias;
		
		QueryOrderTask(int tableAlias){
			super(WirelessOrder.loginStaff, tableAlias, WirelessOrder.foodMenu);
			mProgressDialog = ProgressDialog.show(getActivity(), "", "���ڶ�ȡ�˵�...���Ժ�", true);
			mTblAlias = tableAlias;
		}
		
		@Override
		public void onSuccess(Order order){
			mProgressDialog.dismiss();
			
			mOriOrder = order;
			//���¹����Ʒ
			new QuerySellOutTask().execute();
			
			mFoodListHandler.sendEmptyMessage(0);
		}
		
		@Override
		public void onFail(BusinessException e){
			mProgressDialog.dismiss();
			
			if(e.getErrCode().equals(FrontBusinessError.ORDER_NOT_EXIST)){
				mOriOrder = null;
				
			}else{
				new AlertDialog.Builder(getActivity()).setTitle("�����˵�ʧ��")
					.setMessage(e.getMessage())
					.setPositiveButton("ˢ��", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mQueryOrderTask = new QueryOrderTask(mTblAlias);
							mQueryOrderTask.execute();
						}
					})
					.setNegativeButton("�˳�", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							getActivity().finish();
						}
					}).show();
			}
			
			mFoodListHandler.sendEmptyMessage(0);
		}
		
	}
	
	/**
	 * ������¹����Ʒ
	 */
	private class QuerySellOutTask extends com.wireless.lib.task.QuerySellOutTask{
		
		QuerySellOutTask(){
			super(WirelessOrder.loginStaff, WirelessOrder.foodMenu.foods);
		}
		
		@Override
		public void onSuccess(List<Food> sellOutFoods){
			Toast.makeText(getActivity(), "�����Ʒ���³ɹ�", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onFail(BusinessException e){
			Toast.makeText(getActivity(), "�����Ʒ����ʧ��", Toast.LENGTH_SHORT).show();				
		}
		
	}

	@Override
	public void onCancelAmountChanged(OrderFood food) {
		mFoodListHandler.sendEmptyMessage(0);
	}
	
	@Override
	public void onAmountChanged(OrderFood food) {
		//Remove the new food if the amount is zero.
		if(food.getCount() == 0){
			mNewFoodList.remove(food);
		}
		mFoodListHandler.sendEmptyMessage(0);
	}

	@Override
	public void onAmountAdded(OrderFood food) {
		mFoodListHandler.sendEmptyMessage(0);
	}

	@Override
	public void onFoodPicked(OrderFood food, ActionType type) {
		if(type == ActionType.MODIFY){
			mFoodListHandler.sendEmptyMessage(0);
		}
	}

	@Override
	public void onTableSelected(final Table selectedTable) {
		if(mOriOrder == null){
			return;
		}
		new TransOrderFoodTask(WirelessOrder.loginStaff, new Order.TransferBuilder(mOriOrder.getId(), new Table.AliasBuilder(selectedTable.getAliasId())).addAll(mTransFoods)) {
			
			private ProgressDialog mProgressDialog;
			
			@Override
			public void onPreExecute(){
				mProgressDialog = ProgressDialog.show(getActivity(), "", "����ת��...���Ժ�", true);
			}
			
			@Override
			protected void onSuccess() {
				mProgressDialog.dismiss();
				Toast.makeText(getActivity(), "��Ʒת��" + selectedTable.getAliasId() + "��̨", Toast.LENGTH_SHORT).show();
				mQueryOrderTask = new QueryOrderTask(mOriOrder.getDestTbl().getAliasId());
				mQueryOrderTask.execute();
			}
			
			@Override
			protected void onFail(BusinessException e) {
				mProgressDialog.dismiss();
				new AlertDialog.Builder(getActivity()).setTitle("ת��ʧ��")
				.setMessage(e.getMessage())
				.setNegativeButton("�˳�", null).show();
			}
		}.execute();
			
	}
}

