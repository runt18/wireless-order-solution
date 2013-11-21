package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.tasteMgr.TasteGroup;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.PickFoodActivity;
import com.wireless.ui.PickTasteActivity;
import com.wireless.ui.R;
import com.wireless.ui.dialog.AskCancelAmountDialog;
import com.wireless.ui.dialog.AskCancelAmountDialog.OnCancelAmountChangedListener;
import com.wireless.ui.dialog.SetOrderAmountDialog;
import com.wireless.ui.dialog.SetOrderAmountDialog.OnAmountChangedListener;

public class OrderFoodFragment extends Fragment implements OnCancelAmountChangedListener,
														   OnAmountChangedListener{

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
	private static final String ITEM_FOOD_SUM_PRICE = "item_new_food_price";
	private static final String ITEM_FOOD_COUNT = "item_food_count";
	private static final String ITEM_FOOD_OFFSET = "item_food_offset";
	private static final String ITEM_FOOD_TASTE = "item_food_taste";
	private static final String ITEM_THE_FOOD = "item_the_food";
	private static final String ITEM_IS_ORI_FOOD = "itemIsOriFood";
	private static final String ITEM_IS_OFFSET = "item_is_offset";
	
	private static final String ITEM_GROUP_NAME = "item_group_name";
	
	private QueryOrderTask mQueryOrderTask;
	
	private boolean isHangUp = false;
	
	private FoodListHandler mFoodListHandler;
	
	//全单备注的口味
	private List<Taste> mAllFoodTastes = new ArrayList<Taste>();
	
	//新点菜品信息
	private List<OrderFood> mNewFoodList = new ArrayList<OrderFood>();
	
	//已点菜品信息
	private Order mOriOrder;
	
	private final static String TBL_ALIAS_KEY = "TableAliasKey";

	/*
	 * 显示点菜的列表的handler 负责更新点菜的显示
	 */
	private static class FoodListHandler extends Handler {
		
		private static final String[] ITEM_TAGS = { 
			ITEM_FOOD_NAME,
			ITEM_FOOD_COUNT, 
			ITEM_FOOD_SUM_PRICE,
			ITEM_FOOD_TASTE
		};
		
		private static final int[] ITEM_TARGETS = {
			R.id.txtView_foodName_orderChildItem,		//菜名
			R.id.txtView_amountValue_orderChildItem,	//数量
			R.id.txtView_priceValue_orderChildItem,		//价钱
			R.id.txtView_taste_orderChildItem			//口味显示
		};
		
		private static final String[] GROUP_ITEM_TAGS = { ITEM_GROUP_NAME };
		private static final int[] GROUP_ITEM_ID = { R.id.grounname };
		
		private WeakReference<OrderFoodFragment> mFragment;

		FoodListHandler(OrderFoodFragment fragment) {
			mFragment = new WeakReference<OrderFoodFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			OrderFoodFragment ofFgm = mFragment.get();
			
			List<Map<String, ?>> groupData = new ArrayList<Map<String, ?>>();
			List<List<Map<String, ?>>> childData =  new ArrayList<List<Map<String, ?>>>();
			
			HashMap<String, Object> groupMap = new HashMap<String, Object>();
			groupMap.put(ITEM_GROUP_NAME, "新点菜");
			groupData.add(groupMap);
			
			List<Map<String, ?>> newFoodDatas = new ArrayList<Map<String,?>>();
			if(!ofFgm.mNewFoodList.isEmpty()){
				for(OrderFood f : ofFgm.mNewFoodList){
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(ITEM_FOOD_NAME, f.getName());
					map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
					map.put(ITEM_FOOD_SUM_PRICE, NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(f.calcPriceBeforeDiscount()));
					map.put(ITEM_FOOD_TASTE, f.hasTaste() ? f.getTasteGroup().getTastePref() : TasteGroup.NO_TASTE_PREF);
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
				
				//有退菜的菜品显示在最上面
				Comparator<OrderFood> comp = new Comparator<OrderFood>(){
					@Override
					public int compare(OrderFood lhs, OrderFood rhs) {
						if(lhs.getDelta() > rhs.getDelta()){
							return -1;
						}else{
							return 0;
						}
					}
				};
				
				List<Map<String, ?>> pickedFoodDatas = new ArrayList<Map<String, ?>>();
				for(OrderFood f : ofFgm.mOriOrder.getOrderFoods(comp)){
					if(f.getCount() != 0f){
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put(ITEM_IS_ORI_FOOD, true);
						map.put(ITEM_FOOD_NAME, f.getName());
						map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
						map.put(ITEM_FOOD_SUM_PRICE, NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(f.calcPriceBeforeDiscount()));
						map.put(ITEM_FOOD_TASTE, f.hasTaste() ? f.getTasteGroup().getTastePref() : TasteGroup.NO_TASTE_PREF);
						map.put(ITEM_THE_FOOD, f);
						pickedFoodDatas.add(map);
					}
					
					if(f.getDelta() > 0f){
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put(ITEM_IS_ORI_FOOD, true);
						map.put(ITEM_FOOD_NAME, f.getName()); 
						map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
						map.put(ITEM_FOOD_SUM_PRICE, NumericUtil.float2String2(f.calcPriceWithTaste()));
						map.put(ITEM_FOOD_TASTE, f.hasCancelReason() ? f.getCancelReason().getReason() : "没有退菜原因");
						map.put(ITEM_THE_FOOD, f);
						map.put(ITEM_IS_OFFSET, true);
						map.put(ITEM_FOOD_OFFSET, NumericUtil.float2String2(f.getDelta()));
						pickedFoodDatas.add(map);
					}
				}
				childData.add(pickedFoodDatas);
				
			}
			
			FoodExpandableAdapter adapter = ofFgm.new FoodExpandableAdapter(ofFgm.getActivity(), 
					groupData, R.layout.dropgrounpitem, GROUP_ITEM_TAGS, GROUP_ITEM_ID, 
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
	
		private List<? extends Map<String, ?>> mGroupData;
		private class FoodExpandableAdapter extends SimpleExpandableListAdapter{
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
			View layout = super.getChildView(groupPosition, childPosition, isLastChild,	convertView, parent);
			Map<String, ?> map = mChildData.get(groupPosition).get(childPosition);
			final OrderFood food = (OrderFood) map.get(ITEM_THE_FOOD);
			layout.setTag(map);
			
			//show the name to each food
			String status = "";
			if(food.asFood().isSpecial()){
				status = "特";
			}
			if(food.asFood().isRecommend()){
				if(status.length() == 0){
					status = "荐";
				}else{
					status = status + ",荐";
				}
			}
			if(food.asFood().isGift()){
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
			if(food.isTemp()){
				tempStatus = "(临)";
			}else{
				tempStatus = "";
			}
			
			String hangStatus = null;
			if(food.isHangup()){
				hangStatus = "叫";
			}else{
				hangStatus = "";
			}
			if(hangStatus.length() != 0){
				hangStatus = "(" + hangStatus + ")";
			}
			
			String hurriedStatus = null;
			if(food.isHurried()){
				hurriedStatus = "(催)";
			}else{
				hurriedStatus = "";
			}
			
			String comboStatus = null;
			if(food.asFood().isCombo()){
				comboStatus = "(套)";
			}else{
				comboStatus = "";
			}
			
			((TextView) layout.findViewById(R.id.txtView_foodName_orderChildItem)).setText(comboStatus + tempStatus + hangStatus + hurriedStatus + food.getName() + status);
			
			//如果是新点菜
			if(!map.containsKey(ITEM_IS_ORI_FOOD)){
				
				layout.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						new AlertDialog.Builder(getActivity())
						.setTitle("提示")
						.setMessage("叫起" + food.getName() + "吗？")
						.setNeutralButton("是", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,	int which){
									food.setHangup(true);
									mFoodListHandler.sendEmptyMessage(0);
								}
							})
							.setNegativeButton("否", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,	int which){
									food.setHangup(false);
									mFoodListHandler.sendEmptyMessage(0);
								}
							})
							.show();	
					}
					
				});
				
				//"口味"操作			 
				ImageView delFoodImgView = (ImageView)layout.findViewById(R.id.imgView_left_orderFoodListView_childItem);
				delFoodImgView.setTag(food);
				delFoodImgView.setBackgroundResource(R.drawable.taste_word_selector);
				
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(View v) {
						OrderFood food = (OrderFood) v.getTag();
						if(food.isTemp()){
							Toast.makeText(getActivity(), "临时菜不能添加口味", Toast.LENGTH_SHORT).show();
						}else{
							mNewFoodList.remove(food);
							Intent intent = new Intent(getActivity(), PickTasteActivity.class);
							Bundle bundle = new Bundle(); 
							bundle.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(food));
							intent.putExtras(bundle);
							startActivityForResult(intent, PICK_TASTE);
						}
					}
				};
				
				delFoodImgView.setOnClickListener(listener);
	
				//"数量"操作
				ImageView addTasteImgView = (ImageView)layout.findViewById(R.id.imgView_right_orderFoodListView_childItem);
				addTasteImgView.setBackgroundResource(R.drawable.amount_selector);
				addTasteImgView.setTag(food);
				addTasteImgView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						SetOrderAmountDialog.newInstance((OrderFood)v.getTag(), OrderFoodFragment.this.getId()).show(getFragmentManager(), SetOrderAmountDialog.TAG);
					}
				});
				
			}else {//已点菜
				
				//"退菜"Button
				ImageView cancelFoodImgView = (ImageView) layout.findViewById(R.id.imgView_left_orderFoodListView_childItem);
				cancelFoodImgView.setBackgroundResource(R.drawable.tuicai_selector);
				
				//"催菜"Button
				ImageView hurriedImgView = (ImageView) layout.findViewById(R.id.imgView_right_orderFoodListView_childItem);
				hurriedImgView.setBackgroundResource(R.drawable.cuicai_selector);
				
				//"取消退菜"Button
				Button restoreBtn = (Button) layout.findViewById(R.id.button_orderFoodListView_childItem_restore);
				
				//如果是退菜
				if(map.containsKey(ITEM_IS_OFFSET)){
					cancelFoodImgView.setVisibility(View.INVISIBLE);
					hurriedImgView.setVisibility(View.INVISIBLE);
					
					((TextView) layout.findViewById(R.id.txtView_amountValue_orderChildItem)).setText(NumericUtil.float2String2(food.getDelta()));
					layout.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.VISIBLE);
					//取消退菜按钮
					restoreBtn.setVisibility(View.VISIBLE); 
					restoreBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								food.addCount(food.getDelta());		
								food.setCancelReason(null);
								mFoodListHandler.sendEmptyMessage(0);
							} catch (BusinessException e) {
								Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
							}
						}
					});
				//不是退菜
				} else {
					cancelFoodImgView.setVisibility(View.VISIBLE);
					hurriedImgView.setVisibility(View.VISIBLE);
					
					restoreBtn.setVisibility(View.INVISIBLE);
					//show the order amount to each food
					((TextView) layout.findViewById(R.id.txtView_amountValue_orderChildItem)).setText(NumericUtil.float2String2(food.getCount()));
					layout.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.INVISIBLE);
					//"退菜"操作
					cancelFoodImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							AskCancelAmountDialog.newInstance(food, getId()).show(getFragmentManager(), AskCancelAmountDialog.TAG);
						}
					});
					//"催菜"操作
					hurriedImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							if(food.isHurried()){
								food.setHurried(false);
								Toast.makeText(getActivity(), "取消催菜成功", Toast.LENGTH_SHORT).show();
								mFoodListHandler.sendEmptyMessage(0);
							}else{
								food.setHurried(true);
								Toast.makeText(getActivity(), "催菜成功", Toast.LENGTH_SHORT).show();	
								mFoodListHandler.sendEmptyMessage(0);
							}			
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
				((ImageView)layout.findViewById(R.id.orderimage)).setVisibility(View.INVISIBLE);
				((ImageView) layout.findViewById(R.id.operateimage)).setVisibility(View.INVISIBLE);
				
			}else{

				/**
				 * 新点菜的Group显示"点菜"、"全单"Button
				 */
				//点菜Button
				ImageView orderImg = (ImageView)layout.findViewById(R.id.orderimage);
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
					View popupLayout = getActivity().getLayoutInflater().inflate(R.layout.order_activity_operate_popup, null);
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
								dummyFood.makeTasteGroup(mAllFoodTastes, null);
								bundle.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(dummyFood));
								bundle.putString(PickTasteActivity.INIT_TAG, PickTasteActivity.TAG_TASTE);
								bundle.putBoolean(PickTasteActivity.PICK_ALL_ORDER_TASTE, true);
								intent.putExtras(bundle);
								startActivityForResult(intent, PICK_ALL_FOOD_TASTE);
								mPopup.dismiss();
							} else {
								Toast.makeText(getActivity(), "此餐台还未点菜，无法添加备注", Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
				
				View orderOperateBtn = layout.findViewById(R.id.button_orderActivity_opera);
				orderOperateBtn.setVisibility(View.VISIBLE);
				orderOperateBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mPopup.showAsDropDown(v);
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
		return inflater.inflate(R.layout.order_food_activity, null);
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
				OrderFoodParcel foodParcel = data.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
				mNewFoodList.add(foodParcel.asOrderFood());
				
			}else if(requestCode == OrderFoodFragment.PICK_FOOD){
				//选菜改变时通知新点菜的ListView进行更新
				OrderParcel orderParcel = data.getParcelableExtra(OrderParcel.KEY_VALUE);
				mNewFoodList.addAll(orderParcel.asOrder().getOrderFoods());
				
			}else if(requestCode == PICK_ALL_FOOD_TASTE){
				//全单备注改变时更新所有新点菜的口味
				for(Taste t : mAllFoodTastes){
					for(OrderFood of : mNewFoodList){
						if(of.hasTaste() && !of.isTemp()){
							of.getTasteGroup().removeTaste(t);
						}
					}
				}
				
				mAllFoodTastes.clear();
				
				OrderFoodParcel foodParcel = data.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
				if(foodParcel.asOrderFood().hasTaste()){
					mAllFoodTastes.addAll(foodParcel.asOrderFood().getTasteGroup().getNormalTastes());
					//为所有新点菜添加口味,临时菜除外
					for(OrderFood food : mNewFoodList){
						if(!food.isTemp()){
							if(food.hasTaste()){
								for(Taste taste : mAllFoodTastes){
									food.getTasteGroup().addTaste(taste);
								}
							}else{
								food.makeTasteGroup(mAllFoodTastes, null);
							}
						}
					}
					mFoodListHandler.sendEmptyMessage(0);
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
		mQueryOrderTask = new QueryOrderTask(mOriOrder.getDestTbl().getAliasId());
		mQueryOrderTask.execute();
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
			mProgressDialog = ProgressDialog.show(getActivity(), "", "正在读取账单，请稍后", true);
		}
		
		@Override
		protected void onPostExecute(Order order){
			
			mProgressDialog.dismiss();
			
			if(mBusinessException != null){
				if(mBusinessException.getErrCode().equals(ProtocolError.ORDER_NOT_EXIST)){
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
			}else{
				
				mOriOrder = order;
				
				//更新沽清菜品
				new QuerySellOutTask().execute();
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
		protected void onPostExecute(Food[] sellOutFoods){
			if(mProtocolException != null){
				Toast.makeText(getActivity(), "沽清菜品更新失败", Toast.LENGTH_SHORT).show();				
			}else{
				Toast.makeText(getActivity(), "沽清菜品更新成功", Toast.LENGTH_SHORT).show();
			}
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
}

