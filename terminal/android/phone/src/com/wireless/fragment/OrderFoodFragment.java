package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.parcel.TasteGroupParcel;
import com.wireless.pojo.dishesOrder.ComboOrderFood;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.ComboFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.regionMgr.Table;
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
import com.wireless.ui.dialog.SetOrderAmountDialog;
import com.wireless.ui.dialog.SetOrderAmountDialog.OnAmountChangedListener;

public class OrderFoodFragment extends Fragment implements OnCancelAmountChangedListener,
														   OnAmountChangedListener,
														   OnAmountAddedListener,
														   OnFoodPickedListener{

	public static interface OnButtonClickedListener{
		public void onPickFoodClicked();
	}
	
	public static interface OnOrderChangedListener{
		public void onOrderChanged(Order oriOrder, List<OrderFood> newFoodList);
	}
	
	private OnButtonClickedListener mBtnClickedListener;
	
	private OnOrderChangedListener mOrderChangedListener;
	
	public final static int PICK_FOOD = 0;
	public final static int PICK_TASTE = 1;
	public final static int PICK_ALL_FOOD_TASTE = 2;
	
	public final static String TAG = "OrderFoodFragment";
	
	// 列表项的显示标签
	private static final String ITEM_FOOD_NAME = "item_food_name";
	private static final String ITEM_FOOD_PRICE = "item_new_food_price";
	private static final String ITEM_FOOD_AMOUNT = "item_food_count";
	private static final String ITEM_FOOD_DELTA = "item_food_delta";
	private static final String ITEM_FOOD_TASTE = "item_food_taste";
	private static final String ITEM_THE_FOOD = "item_the_food";
	private static final String ITEM_IS_ORI_FOOD = "item_is_original";
	private static final String ITEM_IS_DELTA = "item_is_delta";
	
	private static final String ITEM_GROUP_NAME = "item_group_name";
	
	private QueryOrderTask mQueryOrderTask;
	
	private boolean isHangUp = false;
	
	private FoodListHandler mFoodListHandler;
	
	//全单备注的口味
	private List<Taste> mAllFoodTastes = new ArrayList<Taste>();
	
	//新点菜品信息
	private List<OrderFood> mNewFoodList = new ArrayList<OrderFood>();
	
	//选中要操作的菜品
	private OrderFood mSelectedFood;
	
	//已点菜品信息
	private Order mOriOrder;
	
	private final static String TBL_ALIAS_KEY = "TableAliasKey";

	/*
	 * 显示点菜的列表的handler 负责更新点菜的显示
	 */
	private static class FoodListHandler extends Handler {
		
		private static final String[] ITEM_TAGS = { 
			ITEM_FOOD_NAME,
			ITEM_FOOD_AMOUNT, 
			ITEM_FOOD_PRICE,
			ITEM_FOOD_TASTE
		};
		
		private static final int[] ITEM_TARGETS = {
			R.id.txtView_foodName_orderChildItem,		//菜名
			R.id.txtView_amountValue_orderChildItem,	//数量
			R.id.txtView_priceValue_orderChildItem,		//价钱
			R.id.txtView_taste_orderChildItem			//口味显示
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
			groupMap.put(ITEM_GROUP_NAME, "新点菜");
			groupData.add(groupMap);
			
			List<Map<String, ?>> newFoodDatas = new ArrayList<Map<String,?>>();
			if(!ofFgm.mNewFoodList.isEmpty()){
				for(OrderFood f : ofFgm.mNewFoodList){
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(ITEM_FOOD_NAME, f.getName());
					map.put(ITEM_FOOD_AMOUNT, String.valueOf(f.getCount()));
					map.put(ITEM_FOOD_PRICE, NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(f.calcPriceBeforeDiscount()));
					map.put(ITEM_FOOD_TASTE, f.getTasteGroup().getPreference());
					map.put(ITEM_THE_FOOD, f);
					newFoodDatas.add(map);
				}
			}
			childData.add(newFoodDatas);

			//如果有已点菜
			if(ofFgm.mOriOrder != null && ofFgm.mOriOrder.hasOrderFood()){
				
				groupMap = new HashMap<String, Object>();
				groupMap.put(ITEM_GROUP_NAME, "已点菜");
				groupMap.put(ITEM_IS_ORI_FOOD, true);
				groupData.add(groupMap);
				
				//有退菜和加菜的菜品显示在最上面
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
					if(of.getCount() != 0f){
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
						map.put(ITEM_FOOD_TASTE, of.hasCancelReason() ? of.getCancelReason().getReason() : "无退菜原因");
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
						map.put(ITEM_FOOD_TASTE, "加" + NumericUtil.float2String2(Math.abs(of.getDelta())) + "份");
						map.put(ITEM_THE_FOOD, of);
						map.put(ITEM_FOOD_DELTA, Float.valueOf(of.getDelta()));
						map.put(ITEM_IS_DELTA, true);
						pickedFoodDatas.add(map);
					}
					
				}
				childData.add(pickedFoodDatas);
				
			}
			
			FoodExpandableAdapter adapter = ofFgm.new FoodExpandableAdapter(ofFgm.getActivity(), 
					groupData, R.layout.order_activity_drop_group, GROUP_ITEM_TAGS, GROUP_ITEM_ID, 
					childData, R.layout.order_activity_child_item, ITEM_TAGS, ITEM_TARGETS);
			
			ExpandableListView listView = (ExpandableListView) mFragment.get().getView().findViewById(R.id.expandableListView_orderActivity);
			listView.setAdapter(adapter);
			
			for(int i = 0; i < groupData.size(); i++){
				listView.expandGroup(i);
			}
			
			calcTotal();
			
			//回调通知Order发生变化
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
		
		private final List<? extends Map<String, ?>> mGroupData;
		private final List<? extends List<? extends Map<String, ?>>> mChildData;
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
			View layout = super.getChildView(groupPosition, childPosition, isLastChild,	convertView, parent);
			Map<String, ?> map = mChildData.get(groupPosition).get(childPosition);
			final OrderFood of = (OrderFood) map.get(ITEM_THE_FOOD);
			layout.setTag(map);
			
			StringBuilder detail = new StringBuilder();
			
			if(of.asFood().isCombo()){
				detail.append("(套)");
			}
			if(of.isHurried()){
				detail.append("(催)");
			}
			if(of.isTemp()){
				detail.append("(临)");
			}
			if(of.isHangup()){
				detail.append("(叫)");
			}
			if(of.isGift()){
				detail.append("(赠)");
			}
			
			detail.append(of.getName());
			
			//show the name to each food
			StringBuilder status = new StringBuilder();
			if(of.asFood().isSpecial()){
				status.append("特");
			}
			if(status.length() != 0){
				status.insert(0, "(").append(")");
			}
			
			detail.append(status);
			
			((TextView) layout.findViewById(R.id.txtView_foodName_orderChildItem)).setText(detail);
			
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
					tv.setText("│― " + (cof != null ? cof.toString() : comboFood.getName()));
					comboLinearLayout.addView(tv);
				}
			}else{
				((LinearLayout)layout.findViewById(R.id.linearLayout_comboList_orderChildItem)).setVisibility(View.GONE);
			}
			
			//如果是新点菜
			if(!map.containsKey(ITEM_IS_ORI_FOOD)){
				
				layout.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						AskOrderAmountDialog.newInstance(of, ActionType.MODIFY, getId()).show(getFragmentManager(), AskOrderAmountDialog.TAG);
					}
				});
				
				//"口味"操作			 
				ImageView delFoodImgView = (ImageView)layout.findViewById(R.id.imgView_left_orderFoodListView_childItem);
				delFoodImgView.setTag(of);
				delFoodImgView.setBackgroundResource(R.drawable.taste_word_selector);
				
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(View v) {
						mSelectedFood = (OrderFood) v.getTag();
						if(mSelectedFood.isTemp()){
							Toast.makeText(getActivity(), "临时菜不能添加口味", Toast.LENGTH_SHORT).show();
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
	
				//"数量"操作
				ImageView amountImgView = (ImageView)layout.findViewById(R.id.imgView_right_orderFoodListView_childItem);
				amountImgView.setBackgroundResource(R.drawable.amount_selector);
				amountImgView.setTag(of);
				amountImgView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						SetOrderAmountDialog.newInstance((OrderFood)v.getTag(), OrderFoodFragment.this.getId()).show(getFragmentManager(), SetOrderAmountDialog.TAG);
					}
				});
				
			}else {//已点菜
				
				//"退菜"Button
				ImageView cancelImgView = (ImageView) layout.findViewById(R.id.imgView_left_orderFoodListView_childItem);
				cancelImgView.setBackgroundResource(R.drawable.tuicai_selector);
				
				//"加菜"Button
				ImageView addImgView = (ImageView) layout.findViewById(R.id.imgView_right_orderFoodListView_childItem);
				addImgView.setBackgroundResource(R.drawable.amount_selector);
				
				//"取消退菜"or"取消加菜"Button
				Button restoreBtn = (Button) layout.findViewById(R.id.button_orderFoodListView_childItem_restore);
				
				//Check if delta exist
				if(map.containsKey(ITEM_IS_DELTA)){
					//delta > 0 表示是退菜
					if((Float)map.get(ITEM_FOOD_DELTA) > 0f){
						cancelImgView.setVisibility(View.INVISIBLE);
						addImgView.setVisibility(View.INVISIBLE);
						
						((TextView) layout.findViewById(R.id.txtView_amountValue_orderChildItem)).setText(NumericUtil.float2String2(of.getDelta()));
						layout.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.VISIBLE);
						//'取消退菜'按钮
						restoreBtn.setText("取消退菜");
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
						//delta < 0 表示是加菜
						cancelImgView.setVisibility(View.INVISIBLE);
						addImgView.setVisibility(View.INVISIBLE);
						
						//layout.setBackgroundColor(Color.LTGRAY);
						((TextView)layout.findViewById(R.id.txtView_taste_orderChildItem)).setTextColor(getResources().getColor(R.color.green));
						((TextView) layout.findViewById(R.id.txtView_amountValue_orderChildItem)).setText(NumericUtil.float2String2(Math.abs(of.getDelta())));
						layout.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.INVISIBLE);
						//'取消加菜'按钮
						restoreBtn.setText("取消加菜");
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
					//Delta不存在表示菜品没有变化
					
					if((Float)map.get(ITEM_FOOD_DELTA) < 0f){
						//delta < 0 表示是加菜, 只显示加菜Button
						cancelImgView.setVisibility(View.INVISIBLE);
						addImgView.setVisibility(View.VISIBLE);
						
					}else if((Float)map.get(ITEM_FOOD_DELTA) > 0f){
						//delta > 0 表示是退菜, 只显示退菜Button
						cancelImgView.setVisibility(View.VISIBLE);
						addImgView.setVisibility(View.INVISIBLE);
					}else{
						//delta == 0表示普通状态, 显示加/退菜Button
						cancelImgView.setVisibility(View.VISIBLE);
						addImgView.setVisibility(View.VISIBLE);
					}
					
					restoreBtn.setVisibility(View.INVISIBLE);
					
					//show the order amount to each food
					((TextView) layout.findViewById(R.id.txtView_amountValue_orderChildItem)).setText(NumericUtil.float2String2(of.getCount()));
					layout.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.INVISIBLE);
					//"退菜"操作
					cancelImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							AskCancelAmountDialog.newInstance(of, getId()).show(getFragmentManager(), AskCancelAmountDialog.TAG);
						}
					});
					
					//"加菜"操作
					addImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							AddOrderAmountDialog.newInstance(of, getId()).show(getFragmentManager(), AddOrderAmountDialog.TAG);
						}
					});
					
					//"催菜"操作
					layout.setOnClickListener(new OnClickListener(){
						
						@Override
						public void onClick(View v) {
							new AlertDialog.Builder(getActivity())
							.setTitle("提示")
							.setMessage("催" + of.getName() + "吗？")
							.setNeutralButton("是", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,	int which){
										of.setHurried(true);
										Toast.makeText(getActivity(), "催菜成功", Toast.LENGTH_SHORT).show();	
										mFoodListHandler.sendEmptyMessage(0);
									}
								})
								.setNegativeButton("否", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,	int which){
										of.setHurried(false);
										mFoodListHandler.sendEmptyMessage(0);
									}
								})
								.show();	
						}
						
					});
				}
			}
			return layout;
		}
	
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,	View convertView, ViewGroup parent) {
			View layout = super.getGroupView(groupPosition, isExpanded, convertView, parent);
			Map<String, ?> map = mGroupData.get(groupPosition);
			
			if(map.containsKey(ITEM_IS_ORI_FOOD)){
				
				/**
				 * 已点菜的Group不需要显示Button
				 */
				layout.findViewById(R.id.button_orderActivity_opera).setVisibility(View.GONE);
				((ImageView)layout.findViewById(R.id.imgView_right_orderDropGroup)).setVisibility(View.INVISIBLE);
				((ImageView) layout.findViewById(R.id.imgView_left_orderDropGroup)).setVisibility(View.INVISIBLE);
				
			}else{
	
				/**
				 * 新点菜的Group显示"点菜"、"全单"Button
				 */
				//点菜Button
				ImageView orderImg = (ImageView)layout.findViewById(R.id.imgView_right_orderDropGroup);
				orderImg.setVisibility(View.VISIBLE);
				orderImg.setBackgroundResource(R.drawable.order_selector);
				
				orderImg.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						if(mBtnClickedListener != null){
							mBtnClickedListener.onPickFoodClicked();
						}else{
							// 跳转到选菜Activity
							Intent intent = new Intent(getActivity(), PickFoodActivity.class);
							startActivityForResult(intent, PICK_FOOD);
						}
					}
				});
				
				if(mPopup == null){
					View popupLayout = getActivity().getLayoutInflater().inflate(R.layout.order_activity_operate_popup, parent, false);
					mPopup = new PopupWindow(popupLayout, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					mPopup.setOutsideTouchable(true);
					mPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_small));
					mPopup.update();
					
					//全单叫起Button
					Button hangUpBtn = (Button) popupLayout.findViewById(R.id.button_orderActivity_operate_popup_callUp);
					if(isHangUp){
						hangUpBtn.setText("取消叫起");
					} else{
						hangUpBtn.setText("叫起");
					}
					
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
									.setTitle("提示")
									.setMessage("确定全单叫起吗?")
									.setNeutralButton("确定", new DialogInterface.OnClickListener() {
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
										.setNegativeButton("取消", null)
										.show();	
							}	
							else {
								Toast.makeText(getActivity(), "没有新点菜，无法叫起", Toast.LENGTH_SHORT).show();
							}
						}
					});
					
					//全单备注Button
					Button allRemarkBtn = (Button) popupLayout.findViewById(R.id.button_orderActivity_operate_popup_remark);
					allRemarkBtn.setText("备注");
					allRemarkBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if(!mNewFoodList.isEmpty()){
								Intent intent = new Intent(getActivity(), PickTasteActivity.class);
								Bundle bundle = new Bundle(); 
								OrderFood dummyFood = new OrderFood();
								dummyFood.asFood().setName("全单备注");
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
								Toast.makeText(getActivity(), "此餐台还未点菜，无法添加备注", Toast.LENGTH_SHORT).show();
							}
						}
					});
					
					//分席Button
					((Button)popupLayout.findViewById(R.id.button_orderActivity_operate_popup_multi)).setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if(mNewFoodList.isEmpty()){
								Toast.makeText(getActivity(), "您还没有点菜", Toast.LENGTH_SHORT).show();
							}else{
								final EditText edtTextMulti = new EditText(getActivity());
								edtTextMulti.setKeyListener(new DigitsKeyListener(false, false));
								
								Dialog dialog = new AlertDialog.Builder(getActivity()).setTitle("请输入分席数量")
									.setIcon(android.R.drawable.ic_dialog_info)
									.setView(edtTextMulti)
									.setPositiveButton("确定", new DialogInterface.OnClickListener(){
										@Override
										public void onClick(DialogInterface dialog, int which) {
											int amount = 0;
											try{
												amount = Integer.parseInt(edtTextMulti.getText().toString());
												if(amount <= 1){
													throw new NumberFormatException();
												}
												
												for(OrderFood of : mNewFoodList){
													of.setCount(amount);
													if(of.hasTmpTaste()){
														Taste tmpTaste = of.getTasteGroup().getTmpTaste();
														if(!tmpTaste.getPreference().contains("分席上")){
															tmpTaste.setPreference((tmpTaste.getPreference().length() != 0 ? tmpTaste.getPreference() + "," : "") + "分席上");
														}
													}else{
														of.setTmpTaste(Taste.newTmpTaste("分席上", 0));
													}
												}
												mFoodListHandler.sendEmptyMessage(0);
												
											}catch(NumberFormatException e){
												Toast.makeText(getActivity(), "您输入的分席数量不正确", Toast.LENGTH_SHORT).show();
											}finally{
												mPopup.dismiss();
											}
										}
		
									})
									.setNegativeButton("取消", null).show();		
								
								//只用下面这一行弹出对话框时需要点击输入框才能弹出软键盘
								dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
								//加上下面这一行弹出对话框时软键盘随之弹出
								dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
							}
	
						}
					});
				}
				
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
		
		//执行请求更新沽清菜品
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
				//口味改变时通知ListView进行更新
				TasteGroupParcel tgParcel = data.getParcelableExtra(TasteGroupParcel.KEY_VALUE);
				if(tgParcel.asTasteGroup() != null){
					mSelectedFood.setTasteGroup(tgParcel.asTasteGroup());
				}else{
					mSelectedFood.clearTasetGroup();
				}
				
			}else if(requestCode == OrderFoodFragment.PICK_FOOD){
				//选菜改变时通知新点菜的ListView进行更新
				OrderParcel orderParcel = data.getParcelableExtra(OrderParcel.KEY_VALUE);
				mNewFoodList.addAll(orderParcel.asOrder().getOrderFoods());
				
			}else if(requestCode == PICK_ALL_FOOD_TASTE){
				//全单备注改变时更新所有新点菜的口味
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
					//为所有新点菜添加口味,临时菜除外
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
	
	public Order buildRequestOrder(int tableAlias, int customNum){
		if(mOriOrder != null){
			Order reqOrder = new Order(mOriOrder.getOrderFoods());
			
			reqOrder.setId(mOriOrder.getId());
			reqOrder.setOrderDate(mOriOrder.getOrderDate());
			reqOrder.setCustomNum(customNum);
			reqOrder.setDestTbl(new Table(tableAlias));
			
			//如果有新点菜，则添加进账单
			if(!mNewFoodList.isEmpty()){
				try{
					reqOrder.addFoods(mNewFoodList, WirelessOrder.loginStaff);
				}catch(BusinessException e){
					Toast.makeText(OrderFoodFragment.this.getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
			
			return reqOrder;
		//新下单
		}else{
			Order reqOrder = new Order(mNewFoodList, tableAlias, customNum);
			return reqOrder;
		}
	}
	
	public Order buildNewOrder(int tableAlias, int customNum){
		return new Order(mNewFoodList, tableAlias, customNum);
	}
	
	public boolean hasOrderFood(){
		if(mOriOrder != null){
			for(OrderFood of : mOriOrder.getOrderFoods()){
				if(of.getCount() > 0){
					return true;
				}
			}
		}
		for(OrderFood of : mNewFoodList){
			if(of.getCount() > 0){
				return true;
			}
		}
		return false;
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
	 * 执行请求对应餐台的账单信息 
	 */
	private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{
		private ProgressDialog mProgressDialog;

		QueryOrderTask(int tableAlias){
			super(WirelessOrder.loginStaff, tableAlias, WirelessOrder.foodMenu);
			mProgressDialog = ProgressDialog.show(getActivity(), "", "正在读取账单...请稍后", true);
		}
		
		@Override
		public void onSuccess(Order order){
			mProgressDialog.dismiss();
			
			mOriOrder = order;
			//更新沽清菜品
			new QuerySellOutTask().execute();
			
			mFoodListHandler.sendEmptyMessage(0);
		}
		
		@Override
		public void onFail(BusinessException e){
			mProgressDialog.dismiss();
			
			if(mBusinessException.getErrCode().equals(FrontBusinessError.ORDER_NOT_EXIST)){
				mOriOrder = null;
				
			}else{
				new AlertDialog.Builder(getActivity()).setTitle("更新账单失败")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("刷新", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mQueryOrderTask = new QueryOrderTask(mTblAlias);
							mQueryOrderTask.execute();
						}
					})
					.setNegativeButton("退出", new DialogInterface.OnClickListener() {
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
	 * 请求更新沽清菜品
	 */
	private class QuerySellOutTask extends com.wireless.lib.task.QuerySellOutTask{
		
		QuerySellOutTask(){
			super(WirelessOrder.loginStaff, WirelessOrder.foodMenu.foods);
		}
		
		@Override
		public void onSuccess(List<Food> sellOutFoods){
			Toast.makeText(getActivity(), "沽清菜品更新成功", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onFail(BusinessException e){
			Toast.makeText(getActivity(), "沽清菜品更新失败", Toast.LENGTH_SHORT).show();				
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
}

