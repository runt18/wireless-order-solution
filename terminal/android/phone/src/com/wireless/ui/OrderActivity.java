package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.excep.BusinessException;
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Table;
import com.wireless.protocol.Taste;
import com.wireless.protocol.TasteGroup;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.ui.dialog.AskCancelAmountDialog;
import com.wireless.ui.dialog.AskCancelAmountDialog.OnAmountChangeListener;
import com.wireless.ui.dialog.AskPwdDialog;
import com.wireless.ui.view.OrderFoodListView;

public class OrderActivity extends Activity implements OnAmountChangeListener{
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
//	private static final String ITEM_GROUP_IS_ORI_GROUP = "item_group_is_item_group";
	
	
	private static final int MSG_REFRESH_LIST = 122;
	protected static final int ALL_ORDER_REMARK = 123;
	
	private FoodListHandler mFoodListHandler;
	private AsyncTask<FoodMenu, Void, Order> mQueryOrderTask;
	
	private ArrayList<OrderFood> mNewFoodList;
	private Order mOriOrder;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.order_activity);
		
		/*
		 * "返回"Button
		 */
		TextView title = (TextView) findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("账单");

		TextView left = (TextView) findViewById(R.id.textView_left);
		left.setText("返回");
		left.setVisibility(View.VISIBLE);

		ImageButton backBtn = (ImageButton) findViewById(R.id.btn_left);
		backBtn.setVisibility(View.VISIBLE);
		backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onBackPressed();
			}
		});
		
		//set the table No
		((EditText)findViewById(R.id.editText_orderActivity_tableNum)).setText(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID));
		//set the default customer to 1
		((EditText)findViewById(R.id.editText_orderActivity_customerNum)).setText("1");
		
		TextView rightTxtView = (TextView)findViewById(R.id.textView_right);
		rightTxtView.setText("提交");
		rightTxtView.setVisibility(View.VISIBLE);
		
		/*
		 * 下单"提交"Button
		 */
		ImageButton commitBtn = (ImageButton)findViewById(R.id.btn_right);
		commitBtn.setVisibility(View.VISIBLE);
		commitBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//下单逻辑
				String tableIdString = ((EditText)findViewById(R.id.editText_orderActivity_tableNum)).getText().toString();
				
				//如果餐台非空则继续，否则提示
				if(!tableIdString.equals("")){
						
					int tableid = Integer.parseInt(tableIdString);
					
					int customNum = 1;
					String custNumString = ((EditText)findViewById(R.id.editText_orderActivity_customerNum)).getText().toString();
					//如果人数为空，则默认为1
					if(!custNumString.equals("")){
						customNum = Integer.parseInt(custNumString);
					}
					//改单
					if(mOriOrder != null){
						Order reqOrder = new Order(mOriOrder.foods, tableid, customNum);		
						reqOrder.orderDate = mOriOrder.orderDate;
						//如果有新点菜，则添加进账单
						if(!mNewFoodList.isEmpty()){
							reqOrder.addFoods(mNewFoodList.toArray(new OrderFood[mNewFoodList.size()]));
						}
						//判断账单是否为空或全是退菜
						if(reqOrder.foods.length != 0){
							//如果全是退菜则提示空单
							for (int i = 0; i < reqOrder.foods.length; i++) {
								if(reqOrder.foods[i].getCount() > 0f ){
									new CommitOrderTask(reqOrder).execute(Type.UPDATE_ORDER);
									break;
								}
								if(i == reqOrder.foods.length - 1){
									Toast.makeText(OrderActivity.this, "请不要提交空单", Toast.LENGTH_SHORT).show();									
								}
							}
						} else {
							Toast.makeText(OrderActivity.this, "您还未点菜，不能下单。", Toast.LENGTH_SHORT).show();
						}
					//新下单
					}else{
						Order reqOrder = new Order(mNewFoodList.toArray(new OrderFood[mNewFoodList.size()]), tableid, customNum);
						if(reqOrder.foods.length != 0){
							new CommitOrderTask(reqOrder).execute(Type.INSERT_ORDER);
						}else{
							Toast.makeText(OrderActivity.this, "您还未点菜，不能下单。", Toast.LENGTH_SHORT).show();
						}
					}
				} else {
					Toast.makeText(OrderActivity.this, "请输入正确的餐台号", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		//执行请求更新沽清菜品
		new QuerySellOutTask().execute(WirelessOrder.foodMenu.foods);
		
		mFoodListHandler = new FoodListHandler(this);
		mNewFoodList = new ArrayList<OrderFood>();
		mQueryOrderTask = new QueryOrderTask(Integer.valueOf(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID))).execute(WirelessOrder.foodMenu);

		mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
		
		/*
		 * 选择每个菜品的操作
		 */
		
		ExpandableListView listView = (ExpandableListView) findViewById(R.id.expandableListView_orderActivity);
		listView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				@SuppressWarnings("unchecked")
				HashMap<String,Object> map = (HashMap<String, Object>) v.getTag();
				if(map.containsKey(ITEM_IS_OFFSET)){
					return true;
				} else if(map.containsKey(ITEM_IS_ORI_FOOD)){
//					mSelectedPos = childPosition;
					new ExtOperDialg((OrderFood)map.get(ITEM_THE_FOOD), true).show();
					return true;
				} else {
					new ExtOperDialg((OrderFood)map.get(ITEM_THE_FOOD), false).show();
					return true;
				}
			}
		});
		
		listView.setOnScrollListener(new OnScrollListener() {				
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((findViewById(R.id.editText_orderActivity_tableNum)).getWindowToken(), 0);
			}
				
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
		});	
		
	}

	@Override
	public void onBackPressed() {
		if(!mNewFoodList.isEmpty()){			
			new AlertDialog.Builder(this)
			.setTitle("提示")
			.setMessage("账单还未提交，是否确认退出?")
			.setNeutralButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which){
							finish();
						}
					})
			.setNegativeButton("取消", null)
			.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
					return true;
				}
			}).show();
		}else{
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		mQueryOrderTask.cancel(true);
		super.onDestroy();
	}

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
			R.id.foodname,
			R.id.accountvalue,
			R.id.pricevalue,
			R.id.taste//口味显示
		};
		
		private static final String[] GROUP_ITEM_TAGS = { ITEM_GROUP_NAME };
		private static final int[] GROUP_ITEM_ID = { R.id.grounname };
		
		private WeakReference<OrderActivity> mActivity;
		private ExpandableListView mListView;

		FoodListHandler(OrderActivity activity) {
			mActivity = new WeakReference<OrderActivity>(activity);
			
			mListView = (ExpandableListView) activity.findViewById(R.id.expandableListView_orderActivity);
		}

		@Override
		public void handleMessage(Message msg) {
			OrderActivity act = mActivity.get();
			
			List<Map<String, ?>> groupData = new ArrayList<Map<String, ?>>();
			List<List<Map<String, ?>>> childData =  new ArrayList<List<Map<String, ?>>>();
			
			HashMap<String, Object> map2 = new HashMap<String, Object>();
			map2.put(ITEM_GROUP_NAME, "新点菜");
			groupData.add(map2);
			
			List<Map<String, ?>> newFoodDatas = new ArrayList<Map<String,?>>();
			if(!act.mNewFoodList.isEmpty()){
				for(OrderFood f : act.mNewFoodList)
				{
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(ITEM_FOOD_NAME, f.name);
					map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
					map.put(ITEM_FOOD_SUM_PRICE, String.valueOf(Util.float2String2(f.calcPriceWithTaste())));
					map.put(ITEM_FOOD_TASTE, f.hasTaste() ? f.getTasteGroup().getTastePref() : TasteGroup.NO_TASTE_PREF);
					map.put(ITEM_THE_FOOD, f);
					newFoodDatas.add(map);
				}
			}
			childData.add(newFoodDatas);

			//如果有已点菜
			if(act.mOriOrder != null && act.mOriOrder.foods.length != 0){
				List<Map<String, ?>> pickedFoodDatas = new ArrayList<Map<String,?>>();
				for(OrderFood f : act.mOriOrder.foods)
				{
					if(f.getCount() != 0f){
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put(ITEM_IS_ORI_FOOD, true);
						map.put(ITEM_FOOD_NAME, f.name);
						map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
						map.put(ITEM_FOOD_SUM_PRICE, Util.float2String2(f.calcPriceWithTaste()));
						map.put(ITEM_FOOD_TASTE, f.hasTaste() ? f.getTasteGroup().getTastePref() : TasteGroup.NO_TASTE_PREF);
						map.put(ITEM_THE_FOOD, f);
						pickedFoodDatas.add(map);
					}
					if(f.getDelta() > 0f)
					{
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put(ITEM_IS_ORI_FOOD, true);
						map.put(ITEM_FOOD_NAME, f.name); 
						map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
						map.put(ITEM_FOOD_SUM_PRICE, Util.float2String2(f.calcPriceWithTaste()));
//						map.put(ITEM_FOOD_TASTE, f.hasTaste() ? f.getTasteGroup().getTastePref() : TasteGroup.NO_TASTE_PREF);
						map.put(ITEM_FOOD_TASTE, f.hasCancelReason() ? f.getCancelReason().getReason() : "没有添加退菜理由");
						map.put(ITEM_THE_FOOD, f);
						map.put(ITEM_IS_OFFSET, true);
						map.put(ITEM_FOOD_OFFSET, Util.float2String2(f.getDelta()));
						pickedFoodDatas.add(map);
					}
				}
				childData.add(pickedFoodDatas);
				
				HashMap<String, Object> map1 = new HashMap<String, Object>();
				map1.put(ITEM_GROUP_NAME, "已点菜");
				map1.put(ITEM_IS_ORI_FOOD, true);
				groupData.add(map1);
			}
			
			FoodExpandableAdapter adapter = act.new FoodExpandableAdapter(act, 
					groupData, R.layout.dropgrounpitem, GROUP_ITEM_TAGS, GROUP_ITEM_ID, 
					childData, R.layout.order_activity_child_item, ITEM_TAGS, ITEM_TARGETS);
			
			mListView.setAdapter(adapter);
			
			for(int i=0;i< groupData.size();i++)
			{
				mListView.expandGroup(i);
			}
			calcTotalPrice();
		}
		
		private void calcTotalPrice(){
			OrderActivity act = mActivity.get();
			
			float totalPrice = 0;
			if(!act.mNewFoodList.isEmpty())
				totalPrice += new Order(act.mNewFoodList.toArray(new OrderFood[act.mNewFoodList.size()])).calcTotalPrice();
			if(act.mOriOrder != null && act.mOriOrder.foods.length != 0)
				totalPrice += act.mOriOrder.calcTotalPrice();
			
			((TextView) act.findViewById(R.id.textview_orderActivity_sumPirce)).setText(Util.float2String2((float)Math.round(totalPrice * 100) / 100));
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode == RESULT_OK){			
			if(requestCode == OrderFoodListView.PICK_TASTE){
				/**
				 * 口味改变时通知ListView进行更新
				 */
				FoodParcel foodParcel = data.getParcelableExtra(FoodParcel.KEY_VALUE);
				mNewFoodList.add(foodParcel);
				mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
				
			}else if(requestCode == OrderFoodListView.PICK_FOOD){
				/**
				 * 选菜改变时通知新点菜的ListView进行更新
				 */
				OrderParcel orderParcel = data.getParcelableExtra(OrderParcel.KEY_VALUE);
				for(OrderFood f: orderParcel.foods)
					mNewFoodList.add(f);
				mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
			}
			//全单备注
			else if(requestCode ==  OrderActivity.ALL_ORDER_REMARK){
				FoodParcel foodParcel = data.getParcelableExtra(FoodParcel.KEY_VALUE);
				if(foodParcel.hasTaste()){
					Taste[] tempTastes = foodParcel.getTasteGroup().getNormalTastes();
					//为所有新点菜和已点菜添加口味
					for(OrderFood food : mNewFoodList){
						if(!food.hasTaste()){
							food.makeTasetGroup(tempTastes, null);
						} else {
							for(Taste taste: tempTastes){
								food.getTasteGroup().addTaste(taste);
							}
						}
					}
					if(mOriOrder != null){
						for(OrderFood food : mOriOrder.foods){
							if(!food.hasTaste()){
								food.makeTasetGroup(tempTastes, null);
							} else {
								for(Taste taste: tempTastes){
									food.getTasteGroup().addTaste(taste);
								}
							}
						}
					}
					mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);

				}
			}
		}
	}
	
	class FoodExpandableAdapter extends SimpleExpandableListAdapter{
		private List<? extends Map<String, ?>> mGroupData;
		private List<? extends List<? extends Map<String, ?>>> mChildData;
		private PopupWindow mPopup;

		public FoodExpandableAdapter(Context context,
				List<? extends Map<String, ?>> groupData, int groupLayout,
				String[] groupFrom, int[] groupTo,
				List<? extends List<? extends Map<String, ?>>> childData,
				int childLayout, String[] childFrom, int[] childTo) {
			super(context, groupData, groupLayout, groupFrom, groupTo, childData,
					childLayout, childFrom, childTo);
			mGroupData = groupData;
			mChildData = childData;
		}

		
		@Override
		public int getChildrenCount(int groupPosition) {
			switch(groupPosition)
			{
			case 0:
				if(mNewFoodList.isEmpty())
					return 0;
				else break;
			case 1:
				if(mOriOrder == null || mOriOrder.foods.length == 0)
					return 0;
				else break;
			}
			return super.getChildrenCount(groupPosition);
		}


		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			View layout = super.getChildView(groupPosition, childPosition, isLastChild,
					convertView, parent);
			Map<String, ?> map = mChildData.get(groupPosition).get(childPosition);
			final OrderFood food = (OrderFood) map.get(ITEM_THE_FOOD);
			layout.setTag(map);
			
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
			
			((TextView) layout.findViewById(R.id.foodname)).setText(comboStatus + tempStatus + hangStatus + hurriedStatus + food.name + status);
			
			//如果是新点菜
			if(!map.containsKey(ITEM_IS_ORI_FOOD)){
				//"删菜"操作			 
				ImageView delFoodImgView = (ImageView)layout.findViewById(R.id.deletefood);
				delFoodImgView.setTag(food);
				delFoodImgView.setBackgroundResource(R.drawable.delete_selector);
				
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(View v) {
						OrderFood food = (OrderFood) v.getTag();
						if(food != null)
							new AskCancelAmountDialog(OrderActivity.this, food,false).show();
					}
				};
				
				delFoodImgView.setOnClickListener(listener);
	
				//"数量"操作
				ImageView addTasteImgView = (ImageView)layout.findViewById(R.id.addtaste);
				addTasteImgView.setBackgroundResource(R.drawable.amount_selector);
				addTasteImgView.setTag(food);
				addTasteImgView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						final OrderFood food = (OrderFood) v.getTag();
						new AskOrderAmountDialog(food, false).show();							
					}
				});
			} 
			//已点菜
			else {
				ImageView cancelFoodImgView = (ImageView) layout.findViewById(R.id.deletefood);
				cancelFoodImgView.setBackgroundResource(R.drawable.tuicai_selector);
				
				ImageView addTasteImgView = (ImageView) layout.findViewById(R.id.addtaste);
				addTasteImgView.setBackgroundResource(R.drawable.cuicai_selector);
				Button restoreBtn = (Button) layout.findViewById(R.id.button_orderFoodListView_childItem_restore);
				//如果是退菜
				if(map.containsKey(ITEM_IS_OFFSET)){
					cancelFoodImgView.setVisibility(View.INVISIBLE);
					addTasteImgView.setVisibility(View.INVISIBLE);
					
					((TextView) layout.findViewById(R.id.accountvalue)).setText(Util.float2String2(food.getDelta()));
					layout.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.VISIBLE);
					//取消退菜按钮
					restoreBtn.setVisibility(View.VISIBLE);
					restoreBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								food.addCount(food.getDelta());		
								food.setCancelReason(null);
								mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
							} catch (BusinessException e) {
								Toast.makeText(OrderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
							}
						}
					});
				//不是退菜
				} else {
					cancelFoodImgView.setVisibility(View.VISIBLE);
					addTasteImgView.setVisibility(View.VISIBLE);
					
					restoreBtn.setVisibility(View.INVISIBLE);
					//show the order amount to each food
					((TextView) layout.findViewById(R.id.accountvalue)).setText(Util.float2String2(food.getCount()));
					layout.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.INVISIBLE);
					//"退菜"操作
					cancelFoodImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							if(WirelessOrder.restaurant.pwd5 != null){
								/**
								 * 提示退菜权限密码，验证通过的情况下显示删菜数量Dialog
								 */
								new AskPwdDialog(OrderActivity.this, AskPwdDialog.PWD_5){
									@Override
									protected void onPwdPass(Context context){
										dismiss();
										new AskCancelAmountDialog(OrderActivity.this,food,true).show();
									}
								}.show();
							}else{
								new com.wireless.ui.dialog.AskCancelAmountDialog(OrderActivity.this,food,true).show();
							}
						}
					});
					//"催菜"操作
					addTasteImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							if(food.isHurried){
								food.isHurried = false;
								Toast.makeText(OrderActivity.this, "取消催菜成功", Toast.LENGTH_SHORT).show();
								mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
							}else{
								food.isHurried = true;
								Toast.makeText(OrderActivity.this, "催菜成功", Toast.LENGTH_SHORT).show();	
								mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
							}			
						}
					}); 
				}
			}
			return layout;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			View layout = super.getGroupView(groupPosition, isExpanded, convertView, parent);
			Map<String, ?> map = mGroupData.get(groupPosition);
			//如果是新点菜
			if(!map.containsKey(ITEM_IS_ORI_FOOD)){
				/*
				 * 点击点菜按钮
				 */
				ImageView orderImg = (ImageView)layout.findViewById(R.id.orderimage);
				orderImg.setVisibility(View.VISIBLE);
				orderImg.setBackgroundResource(R.drawable.order_selector);
				
				orderImg.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						// 跳转到选菜Activity
						Intent intent = new Intent(OrderActivity.this, PickFoodActivity.class);
						startActivityForResult(intent, OrderFoodListView.PICK_FOOD);
					}
				});
				
				if(mPopup == null){
					View popupLayout = getLayoutInflater().inflate(R.layout.order_activity_operate_popup, null);
					mPopup = new PopupWindow(popupLayout, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					mPopup.setOutsideTouchable(true);
					mPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_small));
					mPopup.update();
					//全单叫起按钮
					popupLayout.findViewById(R.id.button_orderActivity_operate_popup_callUp).setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {						
							if(mNewFoodList.size() > 0){
								new AlertDialog.Builder(OrderActivity.this)
									.setTitle("提示")
									.setMessage("确定全单叫起吗?")
									.setNeutralButton("确定", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog,	int which){
												for(int i = 0; i < mNewFoodList.size(); i++){
													OrderFood food = mNewFoodList.get(i);
													if(food.hangStatus == OrderFood.FOOD_NORMAL){
														food.hangStatus = OrderFood.FOOD_HANG_UP;
													}							
												}
												mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
												mPopup.dismiss();
											}
										})
										.setNegativeButton("取消", null)
										.show();	
							}	
							else {
								Toast.makeText(OrderActivity.this, "没有新点菜，无法叫起", Toast.LENGTH_SHORT).show();
							}
						}
					});
					//全单备注
					popupLayout.findViewById(R.id.button_orderActivity_operate_popup_remark).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if(!mNewFoodList.isEmpty() || mOriOrder != null && mOriOrder.foods.length != 0){
								Intent intent = new Intent(OrderActivity.this, PickTasteActivity.class);
								Bundle bundle = new Bundle(); 
								OrderFood dummyFood = new OrderFood();
								dummyFood.name = "全单备注";
								bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(dummyFood));
								bundle.putString(PickTasteActivity.INIT_TAG, PickTasteActivity.TAG_TASTE);
								bundle.putBoolean(PickTasteActivity.PICK_ALL_ORDER_TASTE, true);
								intent.putExtras(bundle);
								startActivityForResult(intent, OrderActivity.ALL_ORDER_REMARK);
								mPopup.dismiss();
							} else {
								Toast.makeText(OrderActivity.this, "此餐台还未点菜，无法添加备注", Toast.LENGTH_SHORT).show();
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
			else {
				boolean hasHangupFood = false;
				final List<? extends Map<String, ?>> foods = mChildData.get(groupPosition);
				for(int i = 0; i < foods.size(); i++){
					@SuppressWarnings("unchecked")
					HashMap<String,Object> amap =  (HashMap<String, Object>) foods.get(i);
					OrderFood food = (OrderFood) amap.get(ITEM_THE_FOOD);
					if(food.hangStatus == OrderFood.FOOD_HANG_UP){
						hasHangupFood = true;
						break;
					}
				}
				layout.findViewById(R.id.button_orderActivity_opera).setVisibility(View.GONE);
				
				if(hasHangupFood){
					/**
					 * 点击全单即起按钮
					 */
					ImageView immediateImgView = (ImageView)layout.findViewById(R.id.orderimage);
					immediateImgView.setVisibility(View.VISIBLE);
					immediateImgView.setBackgroundResource(R.drawable.jiqi_selector);
					immediateImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {						
							if(foods.size() > 0){
								new AlertDialog.Builder(OrderActivity.this)
								.setTitle("提示")
								.setMessage("确定全单即起吗?")
								.setNeutralButton("确定", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,	int which){
											for(int i = 0; i < foods.size(); i++){
												@SuppressWarnings("unchecked")
												HashMap<String,Object> amap =  (HashMap<String, Object>) foods.get(i);
												OrderFood food = (OrderFood) amap.get(ITEM_THE_FOOD);
												if(food.hangStatus == OrderFood.FOOD_HANG_UP){
													food.hangStatus = OrderFood.FOOD_IMMEDIATE;
												}								
											}
											mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
										}
									})
									.setNegativeButton("取消", null)
									.show();	
							}
						}
					});
					((ImageView) layout.findViewById(R.id.operateimage)).setVisibility(View.INVISIBLE);
				}else{
					/*
					 * 如果没有叫起的菜品则不显示叫起Button
					 */
					((ImageView)layout.findViewById(R.id.orderimage)).setVisibility(View.INVISIBLE);
					((ImageView) layout.findViewById(R.id.operateimage)).setVisibility(View.INVISIBLE);
				}
			}
			return layout;
		}
		
	}
	
	/**
	 * 点击菜品列表后的扩展功能 Dialog
	 */
	private class ExtOperDialg extends Dialog{

		
		ExtOperDialg(final OrderFood selectedFood, final boolean isOriFood) {
			super(OrderActivity.this, R.style.FullHeightDialog);
			setContentView(R.layout.item_alert);
			getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView)findViewById(R.id.ordername)).setText("请选择" + selectedFood.name + "的操作");
			if(!isOriFood){
				/**
				 * 新点菜是扩展功能为"删菜"、"口味"、"叫起/取消叫起"、“数量”
				 */
				//删菜功能
				((TextView)findViewById(R.id.item1Txt)).setText("删菜");
				((RelativeLayout)findViewById(R.id.r1)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						dismiss();
						new AskCancelAmountDialog(OrderActivity.this,selectedFood,isOriFood).show();							
					}
				});
				
				//口味功能
				((TextView)findViewById(R.id.item2Txt)).setText("口味");
				((RelativeLayout)findViewById(R.id.r2)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						onPickTaste(selectedFood);
						mNewFoodList.remove(selectedFood);
						dismiss();
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
						new AskOrderAmountDialog(selectedFood, isOriFood).show();							
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
									new AskCancelAmountDialog(OrderActivity.this,selectedFood,isOriFood).show();
								}
							}.show();
						}else{
							new AskCancelAmountDialog(OrderActivity.this,selectedFood,isOriFood).show(); 
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
//			mAdapter.notifyDataSetChanged();
			mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
		}		
	}
	
	/**
	 * 提示输入数量的Dialog
	 */
	private class AskOrderAmountDialog extends Dialog{
	
		AskOrderAmountDialog(final OrderFood selectedFood, boolean isOriFood) {
			super(OrderActivity.this, R.style.FullHeightDialog);
			
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
							mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
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
	
	/*
	 * 选择相应菜品的"口味"操作，跳转到口味Activity进行口味的添加、删除操作
	 */
	private void onPickTaste(OrderFood selectedFood) {
		if(selectedFood.isTemporary){
			Toast.makeText(this, "临时菜不能添加口味", Toast.LENGTH_SHORT).show();
		}else{
			Intent intent = new Intent(OrderActivity.this, PickTasteActivity.class);
			Bundle bundle = new Bundle();
			bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(selectedFood));
			intent.putExtras(bundle);
			startActivityForResult(intent, OrderFoodListView.PICK_TASTE);			
		}
	}
	private class CommitOrderTask extends com.wireless.lib.task.CommitOrderTask{

		private ProgressDialog mProgressDialog;

		public CommitOrderTask(Order reqOrder) {
			super(reqOrder);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = ProgressDialog.show(OrderActivity.this,"", "查询" + mReqOrder.getDestTbl().getAliasId() + "号账单信息...请稍候");
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mProgressDialog.cancel();
			
			if(mBusinessException == null){
				Toast.makeText(OrderActivity.this, mReqOrder.getDestTbl().getAliasId() + "号餐台下单成功", Toast.LENGTH_SHORT).show();
				finish();
			}else{
				if(mOriOrder != null){

					if(mBusinessException.getErrCode() == ErrorCode.TABLE_IDLE){
						//如果是改单，并且返回是餐台空闲的错误状态，
						//则提示用户，并清空购物车中的原账单
						new AlertDialog.Builder(OrderActivity.this)
							.setTitle("提示")
							.setMessage(mReqOrder.getDestTbl().getAliasId() + "号餐台已经结帐，已点菜信息将刷新，新点菜信息将会保留")
							.setNeutralButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,	int which){
										mOriOrder = null;
										mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
									}
								})
							.show();

						
					}else if(mBusinessException.getErrCode() == ErrorCode.ORDER_EXPIRED){
						//如果是改单，并且返回是账单过期的错误状态，
						//则提示用户重新请求账单，再次确认提交
						final Table destTbl = mReqOrder.destTbl;
						new AlertDialog.Builder(OrderActivity.this)
							.setTitle("提示")
							.setMessage(mReqOrder.getDestTbl().getAliasId() + "号餐台的账单信息已经更新，已点菜信息将刷新，新点菜信息将会保留")
							.setNeutralButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,	int which){
										new QueryOrderTask(destTbl.getAliasId()).execute(WirelessOrder.foodMenu);
									}
								})
							.show();
						
					}else{
						new AlertDialog.Builder(OrderActivity.this)
							.setTitle("提示")
							.setMessage(mBusinessException.getMessage())
							.setNeutralButton("确定", null)
							.show();
					}
				}else{
					if(mBusinessException.getErrCode() == ErrorCode.TABLE_BUSY){
						//如果是新下单，并且返回是餐台就餐的错误状态，
						//则提示用户重新请求账单，再次确认提交
						final Table destTbl = mReqOrder.destTbl;
						new AlertDialog.Builder(OrderActivity.this)
							.setTitle("提示")
							.setMessage(mReqOrder.getDestTbl().getAliasId() + "号餐台的账单信息已经更新，已点菜信息将刷新，新点菜信息将会保留")
							.setNeutralButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,	int which){
										new QueryOrderTask(destTbl.getAliasId()).execute(WirelessOrder.foodMenu);
									}
								})
							.show();
					}else{
						new AlertDialog.Builder(OrderActivity.this)
							.setTitle("提示")
							.setMessage(mBusinessException.getMessage())
							.setNeutralButton("确定", null)
							.show();
					}
				}
			}	
		}
		
		
	}
	
	/*
	 * 执行请求对应餐台的账单信息 
	 */
	private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{
		QueryOrderTask(int tableAlias){
			super(tableAlias);
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则迁移到改单页面
		 */
		@Override
		protected void onPostExecute(Order order){
			if(mBusinessException != null){
				
			}else{
				
				mOriOrder = order;
				
				mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
				/*
				 * 请求账单成功则更新相关的控件
				 */
				//set date source to original food list view
				
				//set the table ID
				((EditText)findViewById(R.id.editText_orderActivity_tableNum)).setText(Integer.toString(mOriOrder.getDestTbl().getAliasId()));
				//set the amount of customer
				((EditText)findViewById(R.id.editText_orderActivity_customerNum)).setText(Integer.toString(mOriOrder.getCustomNum()));	
				//更新沽清菜品
				new QuerySellOutTask().execute(WirelessOrder.foodMenu.foods);
			}			
		}		
	}
	
	
	/*
	 * 请求更新沽清菜品
	 */
	private class QuerySellOutTask extends com.wireless.lib.task.QuerySellOutTask{
		@Override
		protected void onPostExecute(Food[] sellOutFoods){
			if(mErrMsg != null){
				Toast.makeText(OrderActivity.this, "沽清菜品更新失败", Toast.LENGTH_SHORT).show();				
			}else{
				Toast.makeText(OrderActivity.this, "沽清菜品更新成功", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onAmountChange(OrderFood food, boolean isOriFood) {
		//新点菜中，如果菜品数量为零的，则删除
		if(!isOriFood && food.getCount() <= 0){
			mNewFoodList.remove(food);
		}
		
		mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
	}
}
